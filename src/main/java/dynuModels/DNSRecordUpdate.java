package dynuModels;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import dynuUpdater.DNSRecordTypeEnum;
import dynuUpdater.DomainAddress;

public class DNSRecordUpdate {
	private transient Long id;
	private Boolean state;
	private String nodeName;
	private DNSRecordTypeEnum recordType;
	private InetAddress ipv4Address;
	private InetAddress ipv6Address;

	public DNSRecordUpdate ( String domainName, String nodeName, InetAddress addr) {
		this.nodeName = nodeName;
		this.state = true;

		if ( addr instanceof Inet4Address ) {
			recordType = DNSRecordTypeEnum.IPV4;
			ipv4Address = addr;
		} else if ( addr instanceof Inet6Address ) {
			recordType = DNSRecordTypeEnum.IPV6;
			ipv6Address = addr;
		}
	}

	public DNSRecordUpdate ( String domainName, String nodeName, DomainAddress addr) {
		this.nodeName = nodeName;
		this.state = true;

		recordType = addr.getType();
		if ( recordType == DNSRecordTypeEnum.IPV4 )
			ipv4Address = addr.getAddress();
		else if ( recordType == DNSRecordTypeEnum.IPV6 )
			ipv6Address = addr.getAddress();
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
