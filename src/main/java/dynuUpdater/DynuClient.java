package dynuUpdater;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

		logger.log(Level.FINER, "Getting root id record for name "+hostname);

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

		logger.log(Level.FINER, "Getting root record for id "+domainId);

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

		logger.log(Level.FINER, "Sending update to root domain "+jsonParser.toJson(rootUpdate));

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

	public HashMap<Long,DomainAddress> getDNSRecordsForNode(DynuDomainName domain, String nodeName) throws DynuClientException, IOException, InterruptedException {
		if ( domain.getDomainId() == null )
			throw new DynuClientException("Invalid domain record passed with "+domain);

		return getDNSRecordsForNode(domain.getDomainId(), nodeName);
	}

	public HashMap<Long,DomainAddress> getDNSRecordsForNode(Long domainId, String nodeName) throws DynuClientException, IOException, InterruptedException {
		if ( domainId == null )
			throw new DynuClientException("Invalid domain id passed");

		HashMap<Long,DomainAddress> rv = new HashMap<Long,DomainAddress>();

		HttpRequest request = HttpRequest
				.newBuilder()
				.uri(URI.create(serviceUrl+"/dns/"+domainId+"/record"))
				.header("Content-Type", "application/json")
				.header("API-Key", accessToken)
				.GET()
				.build();

		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

		//logger.log(Level.FINEST, response.statusCode()+": "+response.body());

		if ( response.statusCode() == 200 ) {
			DNSIdRecordResponse recs = jsonParser.fromJson(response.body(), DNSIdRecordResponse.class);
			for(DNSIdRecord rec : recs.getDnsRecords()) {
				if ( rec.getRecordType() == DNSRecordTypeEnum.IPV4 ) {
					logger.log(Level.FINEST, "Existing record "+rec.getId()+ " "+rec.getIpv4Address().getHostAddress());
					rv.put(rec.getId(), new DomainAddress(rec.getIpv4Address()));
				} else if ( rec.getRecordType() == DNSRecordTypeEnum.IPV6 ) {
					logger.log(Level.FINEST, "Existing record "+rec.getId()+ " "+rec.getIpv6Address().getHostAddress());
					rv.put(rec.getId(), new DomainAddress(rec.getIpv6Address()));
				}
			}
		} else if( Arrays.asList(500, 501, 502).contains(response.statusCode()) ) {
			DynuException exception = jsonParser.fromJson(response.body(), DynuException.class);
			logger.log(Level.WARNING, exception.toString());
			throw new DynuClientException(exception);
		}
		return rv;
	}

	private void removeNameRecord(Long domainId, Long recordId) throws IOException, InterruptedException, DynuClientException {
		String reqUrl = serviceUrl+"/dns/"+domainId+"/record/"+recordId;
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
			DynuException exception = jsonParser.fromJson(response.body(), DynuException.class);
			throw new DynuClientException(exception);
		}
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

		logger.log(Level.FINER, "Updating domain record with "+jsonParser.toJson(update));

		HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

		logger.log(Level.FINEST, response.statusCode()+": "+response.body());

		if ( response.statusCode() == 200 ) {
			rv = jsonParser.fromJson(response.body(), DNSIdRecord.class);
		} else if( Arrays.asList(401, 500, 502).contains(response.statusCode()) ) {
			DynuException exception = jsonParser.fromJson(response.body(), DynuException.class);
			logger.log(Level.WARNING, exception.toString());
			throw new DynuClientException(exception);
		}

		return rv;
	}

	private static Long locateDomainAddressId(HashMap<Long,DomainAddress> daList, DomainAddress da) {
		logger.log(Level.FINEST, "Locating existing id for "+da);
		for(Long id : daList.keySet()) {
			if ( daList.get(id).getType() == da.getType() )
				logger.log(Level.FINEST, "Comparing "+da+" => "+daList.get(id)+"("+id+")");

			if ( daList.get(id).getType() == da.getType() && daList.get(id).equals(da) )
				return id;
		}
		return null;
	}

	private Long reconcileDNSRecords(DynuDomainNodeName dn, Long currentId, DomainAddress currentAddress, HashMap<Long,DomainAddress> eRec) throws IOException, InterruptedException, DynuClientException {
		Long rv = null;
		DNSRecordUpdate update = new DNSRecordUpdate(dn.getDomainName(), dn.getNodeName(), currentAddress);
		update.setId(currentId);

		if ( currentId == null ) {
			for(Long id : eRec.keySet()) {
				DomainAddress da = eRec.get(id);
				if ( da.getType() == currentAddress.getType() )
					removeNameRecord(dn.getDomainId(), id);
			}
			logger.log(Level.INFO, "Adding address "+currentAddress.getAddress().getHostAddress());
			DNSIdRecord resp = maintainDNSRecordForId(dn.getDomainId(), update);
			rv = resp.getId();

		} else {
			logger.log(Level.FINER, "Current active id is "+currentId);
			boolean found = false;
			for(Long id : eRec.keySet()) {
				DomainAddress da = eRec.get(id);
				if ( da.getType() == currentAddress.getType() ) {
					if ( da.getAddress() == null ) {
						logger.log(Level.FINER, "Removing broken address for id "+id);
						removeNameRecord(dn.getDomainId(), id);
					} else if ( id.equals(currentId) ) {
						found = true;
						rv = currentId;
						if ( !da.equals(currentAddress) ) {
							logger.log(Level.INFO, "Updating "+id+" "+da.getAddress().getHostAddress()+" -> "+currentAddress.getAddress().getHostAddress());
							maintainDNSRecordForId(dn.getDomainId(), update);
						} else {
							logger.log(Level.FINER, "No update required for id "+id+" "+da.getAddress().getHostAddress());
						}
					} else if ( !id.equals(currentId) ) {
						logger.log(Level.FINER, "Removing additional id "+id+" "+da.getAddress().getHostAddress());
						removeNameRecord(dn.getDomainId(), id);
					}
				}
			}
			if ( !found ) {
				logger.log(Level.INFO, "Sending update for id "+currentId+" "+currentAddress.getAddress().getHostAddress());
				DNSIdRecord resp = maintainDNSRecordForId(dn.getDomainId(), update);
				rv = resp.getId();
			} else {
				logger.log(Level.INFO, "No update required for "+currentAddress.getAddress().getHostAddress());
			}

		}
		return rv;
	}

	public void maintainDynDomainNameNode(DynuDomainNodeName dn) {
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
			HashMap<Long,DomainAddress> eRec = getDNSRecordsForNode(dn, dn.getNodeName());

			// IPv4
			if ( dn.getIpv4Address() != null ) {
				if ( dn.getIpv4RecordId() == null )
					dn.setIpv4RecordId(locateDomainAddressId(eRec, dn.getIpv4Address()));

				Long recId = reconcileDNSRecords(
						dn,
						dn.getIpv4RecordId(),
						dn.getIpv4Address(),
						eRec);

				dn.setIpv4RecordId(recId);
			} else {
				for(Long id : eRec.keySet()) {
					DomainAddress da = eRec.get(id);
					if ( da.getType() == DNSRecordTypeEnum.IPV4 ) {
						removeNameRecord(dn.getDomainId(), id);
						logger.log(Level.FINE, "Removing "+da.getAddress().getHostAddress());
					}
				}
			}

			// IPv6
			if ( dn.getIpv6Address() != null ) {
				if ( dn.getIpv6RecordId() == null )
					dn.setIpv6RecordId(locateDomainAddressId(eRec, dn.getIpv6Address()));
				Long recId = reconcileDNSRecords(
						dn,
						dn.getIpv6RecordId(),
						dn.getIpv6Address(),
						eRec);

				dn.setIpv6RecordId(recId);
			} else {
				for(Long id : eRec.keySet()) {
					DomainAddress da = eRec.get(id);
					if ( da.getType() == DNSRecordTypeEnum.IPV6 ) {
						logger.log(Level.FINE, "Removing "+da.getAddress().getHostAddress());
						removeNameRecord(dn.getDomainId(), id);
					}
				}
			}
		} catch ( InterruptedException | DynuClientException | IOException e) {
			logger.log(Level.WARNING, "Exception:\n"+e.getMessage()+"\n"+getExceptionStack(e));
		}
	}

	public void maintainDynDomainNameRoot(DynuDomainName dn) {
		try {
			if ( dn.getDomainId() == null ) {
				DNSIdRecord idRec = getRootIdRecordByName(dn.getDomainName());
				dn.setDomainId(idRec.getId());
			}

			DNSRootRecordResponse eRec = getDNSRootRecord(dn.getDomainId());
			logger.log(Level.FINEST, eRec.toString());
			DNSRootRecordUpdate update = new DNSRootRecordUpdate(dn.getDomainName());
			update.setIpv4Address(dn.getIpv4Address());
			update.setIpv6Address(dn.getIpv6Address());

			Boolean sendUpdate = false;
			// IPv4
			if ( eRec.getIpv4Address() == null && dn.getIpv4Address() != null ) {
				logger.log(Level.FINE, "A new ipv4 address is being set "+dn.getIpv4Address().getAddress().getHostAddress());
				sendUpdate = true;
			} else if ( eRec.getIpv4Address() != null && dn.getIpv4Address() == null ) {
				logger.log(Level.FINE, "An old ipv4 address is being removed");
				sendUpdate = true;
			} else if ( dn.getIpv4Address() != null && eRec.getIpv4Address() != null && !dn.getIpv4Address().getAddress().equals(eRec.getIpv4Address()) ) {
				logger.log(Level.INFO, "Updating ipv4 address "+eRec.getIpv4Address().getHostAddress()+" -> "+dn.getIpv4Address().getAddress().getHostAddress());
				sendUpdate = true;
			}

			// IPv6
			if ( eRec.getIpv6Address() == null && dn.getIpv6Address() != null ) {
				logger.log(Level.FINE, "A new ipv6 address is being set "+dn.getIpv6Address().getAddress().getHostAddress());
				sendUpdate = true;
			} else if ( eRec.getIpv6Address() != null && dn.getIpv6Address() == null ) {
				logger.log(Level.FINE, "An old ipv4 address is being removed");
				sendUpdate = true;
			} else if ( dn.getIpv6Address() != null && eRec.getIpv6Address() != null && !dn.getIpv6Address().getAddress().equals(eRec.getIpv6Address()) ) {
				logger.log(Level.INFO, "Updating ipv6 address "+eRec.getIpv6Address().getHostAddress()+" -> "+dn.getIpv6Address().getAddress().getHostAddress());
				sendUpdate = true;
			}

			if ( sendUpdate ) {
				updateDNSRootRecord(dn.getDomainId(), update);
			} else {
				logger.log(Level.INFO, "No updates are required for the existing addresses");
			}

		} catch (IOException | InterruptedException | DynuClientException e) {
			logger.log(Level.WARNING, "Exception:\n"+e.getMessage()+"\n"+getExceptionStack(e));
		}
	}
}
