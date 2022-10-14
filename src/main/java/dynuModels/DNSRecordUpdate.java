package dynuModels;

import java.net.InetAddress;

import dynuUpdater.DNSRecordTypeEnum;

public class DNSRecordUpdate {
	private transient Long id;
	private Boolean state;
	private String nodeName;
	private DNSRecordTypeEnum recordType;
	private InetAddress ipv4Address;
	private InetAddress ipv6Address;

	public DNSRecordUpdate ( String domainName, String nodeName, InetAddress addr) throws Exception {
		this.nodeName = nodeName;
		this.state = true;

		if ( addr.getAddress().length == 4 ) {
			recordType = DNSRecordTypeEnum.IPV4;
			ipv4Address = addr;
		} else if ( addr.getAddress().length ==  16 ) {
			recordType = DNSRecordTypeEnum.IPV6;
			ipv6Address = addr;
		} else {
			throw new Exception("Unhandled address type with length "+addr.getAddress().length);
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getState() {
		return state;
	}

	public String getNodeName() {
		return nodeName;
	}

	public DNSRecordTypeEnum getRecordType() {
		return recordType;
	}

	public InetAddress getIpv4Address() {
		return ipv4Address;
	}

	public InetAddress getIpv6Address() {
		return ipv6Address;
	}

}
