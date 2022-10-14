package dynuUpdater;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;

import dynuModels.DNSIdRecord;
import dynuModels.DNSIdRecordResponse;
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

	private Gson jsonParser;
	private HttpClient httpClient;

	public DynuClient(String accessToken) {
		this.accessToken = accessToken;

		jsonParser = StandardGson.getGson();

		this.httpClient = HttpClient
				.newBuilder()
				.version(Version.HTTP_2)
				.build();
	}

	private static String getExceptionStack(Exception e) {
		ArrayList<String> msg = new ArrayList<String>();
		for(StackTraceElement stackEl : e.getStackTrace())
			msg.add(stackEl.toString());
		return "\tat "+String.join("\n\tat ", msg);
	}

	private DNSIdRecord getRootIdRecordByName(String hostname) throws IOException, InterruptedException, DynuClientException {
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
		} else if( Arrays.asList(500, 501, 502).contains(response.statusCode()) ) {
			DynuException exception = jsonParser.fromJson(response.body(), DynuException.class);
			//logger.log(Level.WARNING, exception.toString());
			throw new DynuClientException(exception);
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

	public DNSRootRecordResponse updateDNSRootRecord(Long domainId, DNSRootRecordUpdate rootUpdate) throws IOException, InterruptedException, DynuClientException {
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
			logger.log(Level.WARNING, exception.toString());
			throw new DynuClientException(exception);
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

	public DynuDomainNodeName getDNSRecordsForNode(DynuDomainName domain, String nodeName) throws DynuClientException, IOException, InterruptedException {
		if ( domain.getDomainId() == null )
			throw new DynuClientException("Invalid domain record passed with "+domain);

		return getDNSRecordsForNode(domain.getDomainId(), nodeName);
	}

	public DynuDomainNodeName getDNSRecordsForNode(Long domainId, String nodeName) throws DynuClientException, IOException, InterruptedException {
		if ( domainId == null )
			throw new DynuClientException("Invalid domain id passed");

		DynuDomainNodeName rv = null;
		HttpRequest request = HttpRequest
				.newBuilder()
				.uri(URI.create(serviceUrl+"/dns/"+domainId+"/record"))
				.header("Content-Type", "application/json")
				.header("API-Key", accessToken)
				.GET()
				.build();

		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
		if ( response.statusCode() == 200 ) {
			DNSIdRecordResponse recs = jsonParser.fromJson(response.body(), DNSIdRecordResponse.class);
			for(DNSIdRecord rec : recs.getDnsRecords()) {
				if ( rv == null ) {
					rv = new DynuDomainNodeName(rec.getDomainName(), nodeName);
					rv.setDomainId(domainId);
				}
				InetAddress addr = null;
				if ( rec.getRecordType() == DNSRecordTypeEnum.IPV4 ) {
					rv.setIpv4Address(new DomainAddress(rec.getIpv4Address()));
					rv.setIpv4RecordId(rec.getId());
				} else if ( rec.getRecordType() == DNSRecordTypeEnum.IPV6) {
					rv.setIpv6Address(new DomainAddress(rec.getIpv6Address()));
					rv.setIpv6RecordId(rec.getId());
				}
			}
		} else if( Arrays.asList(500, 501, 502).contains(response.statusCode()) ) {
			DynuException exception = jsonParser.fromJson(response.body(), DynuException.class);
			logger.log(Level.WARNING, exception.toString());
			throw new DynuClientException(exception);
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
			// catch only error responses
			if( Arrays.asList(401, 500, 502).contains(response.statusCode()) ) {
				rv = false;
				DynuException exception = jsonParser.fromJson(response.body(), DynuException.class);
				logger.log(Level.WARNING, exception.toString());
			}
		}
		return rv;
	}

	private DNSIdRecord maintainDNSRecordForId(Long domainId, DNSRecordUpdate update) throws IOException, InterruptedException, DynuClientException {
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
			logger.log(Level.WARNING, exception.toString());
			throw new DynuClientException(exception);
		}

		return rv;
	}

	private void maintainDynNodeName(DynuDomainNodeName dn) {
		if ( dn.getDomainId() == null ) {
			try {
				DNSIdRecord dIdRec = getRootIdRecordByName(dn.getDomainName());
				if ( dIdRec == null ) {
					logger.log(Level.WARNING, "Domain record for "+dn.getFullName()+" could not be located. Skipping...");
					return;
				} else {
					dn.setDomainId(dIdRec.getId());
				}
			} catch (IOException | InterruptedException | DynuClientException e) {
				logger.log(Level.WARNING, "Exception:\n"+e.getMessage()+"\n"+getExceptionStack(e));
				return;
			}
		}
		try {
			DynuDomainNodeName existing = getDNSRecordsForNode(dn, dn.getNodeName());
			System.out.println(dn);
			System.out.println(existing);
		} catch ( InterruptedException | DynuClientException | IOException e) {
			logger.log(Level.WARNING, "Exception:\n"+e.getMessage()+"\n"+getExceptionStack(e));
		}
	}

	private void maintainDynDomainName(DynuDomainName dn) {
		try {
			DNSIdRecord eRec = getRootIdRecordByName(dn.getDomainName());
			DNSRootRecordUpdate update = new DNSRootRecordUpdate(dn.getDomainName());

			if ( dn.getDomainId() == null )
				dn.setDomainId(eRec.getDomainId());

			Boolean sendUpdate = false;
			if ( eRec.getIpv4Address() == null && dn.getIpv4Address() != null ||
					eRec.getIpv4Address() != null && dn.getIpv4Address() == null ) {
				// A new address is being set
				sendUpdate = true;
				update.setIpv4Address(dn.getIpv4Address());
			} else if ( dn.getIpv4Address() != null && !dn.getIpv4Address().getAddress().equals(eRec.getIpv4Address()) ) {
				// An existing address is being updated
				sendUpdate = true;
				update.setIpv4Address(dn.getIpv4Address());
			}

			if ( eRec.getIpv6Address() == null && dn.getIpv6Address() != null ||
					eRec.getIpv6Address() != null && dn.getIpv6Address() == null) {
				sendUpdate = true;
				update.setIpv6Address(dn.getIpv6Address());
			} else if ( dn.getIpv6Address() != null && !dn.getIpv6Address().getAddress().equals(eRec.getIpv6Address()) ) {
				sendUpdate = true;
				update.setIpv6Address(dn.getIpv6Address());
			}
			System.out.println("Send Update: "+sendUpdate);
			System.out.println(jsonParser.toJson(update));

		} catch (IOException | InterruptedException | DynuClientException e) {
			logger.log(Level.WARNING, "Exception:\n"+e.getMessage()+"\n"+getExceptionStack(e));
		}
	}

	public void maintainDomainName(DynuDomainName dn, ArrayList<DomainAddress> addrList) {
		logger.log(Level.INFO, "Maintaining "+dn.getFullName());
		for(DomainAddress da : addrList) {
			if ( da.getType() == DNSRecordTypeEnum.IPV4 )
				dn.setIpv4Address(da);
			else if ( da.getType() == DNSRecordTypeEnum.IPV6 )
				dn.setIpv6Address(da);
		}
		if ( dn instanceof DynuDomainNodeName ) {
			maintainDynNodeName((DynuDomainNodeName) dn);
		} else {
			maintainDynDomainName(dn);
		}
	}
}
