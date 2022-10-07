package dynuUpdater;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

import com.google.gson.Gson;

import dynuModels.DNSIdRecord;
import dynuModels.DNSIdRecordResponse;
import dynuModels.DNSRecordTypeEnum;
import dynuModels.DNSRecordUpdate;
import dynuModels.DNSRootRecordResponse;
import dynuModels.DNSRootRecordUpdate;
import dynuModels.DynuException;

public class DynuClient {
	private static Logger logger = Logger.getLogger(DynuClient.class.getName());

	private static final String serviceHostname = "api.dynu.com";
	private static final String urlBase = "/v2";
	private static final String serviceUrl = "https://"+serviceHostname+urlBase;

	private String accessToken;
	private NetworkInterface iface;
	private HashMap<String,HashSet<String>> hostnames;

	private boolean enableIPv4;
	private boolean enableIPv6;

	private Gson jsonParser;
	private HttpClient httpClient;

	public DynuClient(String accessToken, NetworkInterface netInt) {
		this.accessToken = accessToken;
		this.iface = netInt;

		jsonParser = StandardGson.getGson();

		enableIPv4();
		disableIPv6();

		this.httpClient = HttpClient
				.newBuilder()
				.version(Version.HTTP_2)
				.build();
	}

	private DNSIdRecord getRootIdRecordByName(String hostname) throws IOException, InterruptedException {
		DNSIdRecord rv = null;

		HttpRequest request = HttpRequest
				.newBuilder()
				.uri(URI.create(serviceUrl+"/dns/getroot/"+hostname))
				.header("Content-Type", "application/json")
				.header("API-Key", accessToken)
				.GET()
				.build();
		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
		if ( response.statusCode() == 200 ) {
			rv = jsonParser.fromJson(response.body(), DNSIdRecord.class);
		}
		return rv;
	}

	private DNSRootRecordResponse getDNSRootRecord(Long domainId) throws IOException, InterruptedException {
		DNSRootRecordResponse rv = null;
		HttpRequest request = HttpRequest
				.newBuilder()
				.uri(URI.create(serviceUrl+"/dns/"+domainId))
				.header("Content-Type", "application/json")
				.header("API-Key", accessToken)
				.GET()
				.build();

		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
		if ( response.statusCode() == 200 ) {
			rv = jsonParser.fromJson(response.body(), DNSRootRecordResponse.class);
		}
		return rv;
	}

	public DNSRootRecordResponse updateDNSRootRecord(Long domainId, DNSRootRecordUpdate rootUpdate) throws IOException, InterruptedException {
		/*
		 * curl -X POST -d '{"name":"tgburrin.dynu.net","ipv4Address":"172.16.1.102","ipv6Address":"fe80:0:0:0:2c47:f690:1555:a16c","ipv4":true,"ipv6":true}'  "https://api.dynu.com/v2/dns/100" -H "Content-Type: application/json" -H  "accept: application/json" -H "API-Key: <removed>" -v
		 * A request to a non-existant / incorrect dns id results in the connection being immediately closed with no response
		 */

		DNSRootRecordResponse rv = null;
		HttpRequest request = HttpRequest
				.newBuilder()
				.uri(URI.create(serviceUrl+"/dns/"+domainId))
				.header("Content-Type", "application/json")
				.header("API-Key", accessToken)
				.POST(BodyPublishers.ofString(jsonParser.toJson(rootUpdate)))
				.build();

		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
		if ( response.statusCode() == 200 ) {
			rv = jsonParser.fromJson(response.body(), DNSRootRecordResponse.class);
		} else if( Arrays.asList(500, 501, 502).contains(response.statusCode()) ) {
			DynuException exception = jsonParser.fromJson(response.body(), DynuException.class);
			System.err.println(exception.getException().getMessage());
		}

		return rv;
	}

	private DNSIdRecordResponse getDNSRececordsForId(Long domainId) throws IOException, InterruptedException {
		DNSIdRecordResponse rv = null;
		HttpRequest request = HttpRequest
				.newBuilder()
				.uri(URI.create(serviceUrl+"/dns/"+domainId+"/record"))
				.header("Content-Type", "application/json")
				.header("API-Key", accessToken)
				.GET()
				.build();

		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
		if ( response.statusCode() == 200 ) {
			rv = jsonParser.fromJson(response.body(), DNSIdRecordResponse.class);
		}
		return rv;
	}

	private ArrayList<DNSIdRecord> getExistingNameRecords (Long domainId) throws IOException, InterruptedException {
		ArrayList<DNSIdRecord> aRecords = new ArrayList<DNSIdRecord>();
		DNSIdRecordResponse records = getDNSRececordsForId(domainId);
		for(DNSIdRecord rec : records.getDnsRecords())
			if ( Arrays.asList(DNSRecordTypeEnum.IPV4, DNSRecordTypeEnum.IPV6).contains(rec.getRecordType()) )
				aRecords.add(rec);
		return aRecords;
	}

	private Boolean removeNameRecords(ArrayList<DNSIdRecord> removeList) throws IOException, InterruptedException {
		Boolean rv = true;
		for(DNSIdRecord rec : removeList) {
			String reqUrl = serviceUrl+"/dns/"+rec.getDomainId()+"/record/"+rec.getId();
			HttpRequest request = HttpRequest
					.newBuilder()
					.uri(URI.create(reqUrl))
					.header("Content-Type", "application/json")
					.header("API-Key", accessToken)
					.DELETE()
					.build();
			HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
			if ( response.statusCode() == 200 ) {
				// no positive handleing
			} else if( Arrays.asList(401, 500, 502).contains(response.statusCode()) ) {
				rv = false;
				DynuException exception = jsonParser.fromJson(response.body(), DynuException.class);
				System.err.println("While removing "+rec.getHostname()+": "+exception.getException().getMessage());
			}
		}
		return rv;
	}

	private DNSIdRecord maintainDNSRecordForId(Long domainId, DNSRecordUpdate update) throws IOException, InterruptedException {
		DNSIdRecord rv = null;
		String urlSuffix = update.getId() == null ? "" : "/"+update.getId();
		String urlPath = serviceUrl+"/dns/"+domainId+"/record"+urlSuffix;

		HttpRequest request = HttpRequest
				.newBuilder()
				.uri(URI.create(urlPath))
				.header("Content-Type", "application/json")
				.header("API-Key", accessToken)
				.POST(BodyPublishers.ofString(jsonParser.toJson(update)))
				.build();

		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
		if ( response.statusCode() == 200 ) {
			rv = jsonParser.fromJson(response.body(), DNSIdRecord.class);
		} else if( Arrays.asList(401, 500, 502).contains(response.statusCode()) ) {
			DynuException exception = jsonParser.fromJson(response.body(), DynuException.class);
			System.err.println("While maintaining "+update.getNodeName()+": "+exception.getException().getMessage());
		}

		return rv;
	}

	private ArrayList<InetAddress> getIPv4Addresses() {
		ArrayList<InetAddress> addrs = new ArrayList<InetAddress>();
		for( InetAddress ia : Collections.list(iface.getInetAddresses()) )
			if ( ia.getAddress().length == 4)
				addrs.add(ia);
		return addrs;
	}

	private ArrayList<InetAddress> getIPv6Addresses() {
		ArrayList<InetAddress> addrs = new ArrayList<InetAddress>();
		for( InetAddress ia : Collections.list(iface.getInetAddresses()) )
			if ( ia.getAddress().length == 16)
				addrs.add(ia);
		return addrs;
	}

	private boolean isInterfaceAddress(InetAddress testAddress) {
		for( InetAddress ia : Collections.list(iface.getInetAddresses()) ) {
			if ( ia.equals(testAddress) )
				return true;
		}
		return false;
	}

	public void enableIPv4() {
		enableIPv4 = true;
	}

	public void disableIPv4() {
		enableIPv4 = false;
	}

	public void enableIPv6() {
		enableIPv6 = true;
	}

	public void disableIPv6() {
		enableIPv6 = false;
	}

	public HashMap<String,HashSet<String>> getHostnames() {
		return hostnames;
	}

	public void setHostnames(HashMap<String,HashSet<String>> hostnames) {
		this.hostnames = hostnames;
	}

	private void maintainRootRecord(DNSIdRecord id) throws IOException, InterruptedException {
		if ( id != null ) {
			DNSRootRecordResponse rootId = getDNSRootRecord(id.getId());
			DNSRootRecordUpdate rootUpdate = new DNSRootRecordUpdate(rootId.getName());

			if ( enableIPv4 ) {
				InetAddress ipv4 = rootId.getIpv4Address();
				ArrayList<InetAddress> ipv4List = getIPv4Addresses();

				if ( ipv4 == null || !isInterfaceAddress(ipv4) ) {
					logger.info(id.getDomainName()+" "+ipv4.getHostAddress()+" -> "+ipv4List.get(0).getHostAddress());
					rootUpdate.setIpv4Address(ipv4List.get(0));
				}
			}
			if ( enableIPv6 ) {
				InetAddress ipv6 = rootId.getIpv6Address();
				ArrayList<InetAddress> ipv6List = getIPv6Addresses();

				if ( ipv6 == null || !isInterfaceAddress(ipv6)) {
					logger.info(id.getDomainName()+" "+ipv6.getHostAddress()+" -> "+ipv6List.get(0).getHostAddress());
					rootUpdate.setIpv6Address(ipv6List.get(0));
				}
			}

			if ( rootUpdate.getIpv4() || rootUpdate.getIpv6() )
				updateDNSRootRecord(id.getId(), rootUpdate);
		}
	}

	private void maintainDomainRecord(DNSIdRecord id, String nodeName, ArrayList<DNSIdRecord> existingRecords) throws Exception {
		ArrayList<InetAddress> ipv4List = new ArrayList<InetAddress>();
		ArrayList<InetAddress> ipv6List = new ArrayList<InetAddress>();

		DNSRecordUpdate ipv4Update = null;
		DNSRecordUpdate ipv6Update = null;

		if ( enableIPv4 ) {
			ipv4List = getIPv4Addresses();
			if (ipv4List.size() > 0)
				ipv4Update = new DNSRecordUpdate(id.getDomainName(), nodeName, ipv4List.get(0));
		}

		if ( enableIPv6 ) {
			ipv6List = getIPv6Addresses();
			if (ipv6List.size() > 0)
				ipv6Update = new DNSRecordUpdate(id.getDomainName(), nodeName, ipv6List.get(0));
		}

		// clear out records that can't be updated are aren't supported any longer
		ArrayList<DNSIdRecord> removeList = new ArrayList<DNSIdRecord>();
		for ( DNSIdRecord er : existingRecords ) {
			if ( ( er.getRecordType() == DNSRecordTypeEnum.IPV4 && (!enableIPv4 || ipv4List.size() == 0))
					|| ( er.getRecordType() == DNSRecordTypeEnum.IPV6 && (!enableIPv6 || ipv6List.size() == 0)) ){
				logger.info("Removing "+er.getHostname()+" "+er.getRecordType());
				removeList.add(er);
			}
		}
		existingRecords.removeAll(removeList);

		if ( removeList.size() > 0 )
			removeNameRecords(removeList);

		for ( DNSIdRecord er : existingRecords ) {
			if ( er.getRecordType() == DNSRecordTypeEnum.IPV4 && ipv4Update != null ) {
				if ( er.getIpv4Address().equals(ipv4Update.getIpv4Address()) ) {
					logger.finer("ipv4 address exists and is unchanged from "+er.getIpv4Address().getHostAddress());
					ipv4Update = null; // no update needed
				} else {
					logger.info(String.join(".", ipv4Update.getNodeName(), id.getDomainName())+" "+er.getIpv4Address().getHostAddress()+" -> "+ipv4Update.getIpv4Address().getHostAddress());
					ipv4Update.setId(er.getId());
				}
			} else if ( er.getRecordType() == DNSRecordTypeEnum.IPV6 && ipv6Update != null ) {
				if ( er.getIpv6Address().equals(ipv6Update.getIpv6Address()) ) {
					logger.finer("ipv6 address exists and is unchanged from "+er.getIpv6Address().getHostAddress());
					ipv6Update = null; // no update needed
				} else {
					logger.info(String.join(".", ipv6Update.getNodeName(), id.getDomainName())+" "+er.getIpv6Address().getHostAddress()+" -> "+ipv6Update.getIpv6Address().getHostAddress());
					ipv6Update.setId(er.getId());
				}
			}
		}

		if ( ipv4Update != null ) {
			if ( ipv4Update.getId() == null )
				logger.info("Adding "+String.join(".", ipv4Update.getNodeName(), id.getDomainName())+" "+ipv4Update.getIpv4Address().getHostAddress());
			maintainDNSRecordForId(id.getId(), ipv4Update);
		}

		if ( ipv6Update != null ) {
			if ( ipv6Update.getId() == null )
				logger.info("Adding "+String.join(".", ipv6Update.getNodeName(), id.getDomainName())+" "+ipv6Update.getIpv6Address().getHostAddress());
			maintainDNSRecordForId(id.getId(), ipv6Update);
		}
	}

	public void maintainRecords() throws Exception {
		for(String domainName : hostnames.keySet()) {
			DNSIdRecord id = getRootIdRecordByName(domainName);

			if ( id == null && !hostnames.get(domainName).contains(null) )
				throw new Exception("domain "+domainName+" could not be found and is not maintained");

			if ( hostnames.get(domainName).contains(null) ) {
				logger.info("Domain name "+domainName+" is to be maintained");
				maintainRootRecord(id);
			}

			if ( (hostnames.get(domainName).contains(null) && hostnames.get(domainName).size() > 1)
					|| (!hostnames.get(domainName).contains(null) && hostnames.get(domainName).size() >= 1) ) {
				ArrayList<DNSIdRecord> existingNameRecords = getExistingNameRecords(id.getId());

				for(String subDomain : hostnames.get(domainName)) {
					if ( subDomain == null )
						continue;
					logger.info("Host name "+String.join(".", subDomain, domainName)+" is to be maintained");
					ArrayList<DNSIdRecord> subExisting = new ArrayList<DNSIdRecord>();
					for(DNSIdRecord rec : existingNameRecords) {
						if ( rec.getNodeName().equals(subDomain) )
							subExisting.add(rec);
					}
					maintainDomainRecord(id, subDomain, subExisting);
				}
			}
		}
	}
}
