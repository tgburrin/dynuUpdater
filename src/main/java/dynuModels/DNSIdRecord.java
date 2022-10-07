package dynuModels;

import java.net.InetAddress;
import java.time.Instant;

public class DNSIdRecord {
	private Long id;
	private Long domainId;
	private String domainName;
	private String nodeName;
	private String hostname;

	private DNSRecordTypeEnum recordType;
	private Integer ttl;
	private Boolean state;
	private String content;
	private Instant updatedOn;
	private String host;

	private String statusCode;
	private InetAddress ipv4Address;
	private InetAddress ipv6Address;

	public Long getId() {
		return id;
	}

	public Long getDomainId() {
		return domainId;
	}

	public String getDomainName() {
		return domainName;
	}

	public String getNodeName() {
		return nodeName;
	}

	public String getHostname() {
		return hostname;
	}

	public DNSRecordTypeEnum getRecordType() {
		return recordType;
	}

	public Integer getTtl() {
		return ttl;
	}

	public Boolean getState() {
		return state;
	}

	public String getContent() {
		return content;
	}

	public Instant getUpdatedOn() {
		return updatedOn;
	}

	public String getHost() {
		return host;
	}

	public InetAddress getIpv4Address() {
		return ipv4Address;
	}

	public InetAddress getIpv6Address() {
		return ipv6Address;
	}

	@Override
	public String toString() {
		String rt = null;
		if ( recordType != null )
			rt = recordType.toString();

		return "DNSIdRecord [id=" + id + ", domainId=" + domainId + ", domainName=" + domainName + ", nodeName="
				+ nodeName + ", hostname=" + hostname + ", recordType=" + rt + ", ttl=" + ttl + ", state="
				+ state + ", content=" + content + ", updatedOn=" + updatedOn + ", host=" + host + ", statusCode="
				+ statusCode + "]";
	}

}
