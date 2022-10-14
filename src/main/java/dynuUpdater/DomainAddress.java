package dynuUpdater;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

public class DomainAddress {
	private InetAddress address;
	private DNSRecordTypeEnum type;

	public DomainAddress (InetAddress addr) {
		if ( addr instanceof Inet4Address) {
			type = DNSRecordTypeEnum.IPV4;
		} else if ( addr instanceof Inet6Address) {
			type = DNSRecordTypeEnum.IPV6;
		}
		address = addr;
	}

	public InetAddress getAddress() {
		return address;
	}

	public DNSRecordTypeEnum getType() {
		return type;
	}

	@Override
	public String toString() {
		return "DomainAddress [address=" + address.getHostAddress() + ", type=" + type + "]";
	}
}
