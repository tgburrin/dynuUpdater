package dynuModels;

import java.net.InetAddress;

import dynuUpdater.DomainAddress;

public class DNSRootRecordUpdate {
	private String name;
	private String group;
	private InetAddress ipv4Address;
	private InetAddress ipv6Address;
	private Boolean ipv4;
	private Boolean ipv6;

	public DNSRootRecordUpdate (String n) {
		name = n;
	}

	public String getName() {
		return name;
	}

	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}

	public InetAddress getIpv4Address() {
		return ipv4Address;
	}
	/*
	public String getIpv4Address() {
		String rv = null;
		if ( ipv4Address != null )
			rv = ipv4Address.getHostAddress();

		return rv;
	}
	*/
	public void setIpv4Address(DomainAddress ipv4Address) {
		if ( ipv4Address ==  null )
			setIpv4Address((InetAddress)null);
		else
			setIpv4Address(ipv4Address.getAddress());
	}

	public void setIpv4Address(InetAddress ipv4Address) {
		if ( ipv4Address ==  null )
			ipv4 = false;
		else
			ipv4 = true;
		this.ipv4Address = ipv4Address;
	}

	public InetAddress getIpv6Address() {
		return ipv6Address;
	}
	/*
	public String getIpv6Address() {
		String rv = null;
		if ( ipv6Address != null )
			rv = ipv6Address.getHostAddress().split("%")[0];
		return rv;
	}
	*/
	public void setIpv6Address(DomainAddress ipv6Address) {
		if ( ipv6Address ==  null )
			setIpv6Address((InetAddress)null);
		else
			setIpv6Address(ipv6Address.getAddress());
	}

	public void setIpv6Address(InetAddress ipv6Address) {
		if ( ipv6Address ==  null )
			ipv6 = false;
		else
			ipv6 = true;
		this.ipv6Address = ipv6Address;
	}

	public Boolean getIpv4() {
		return ipv4;
	}
	public Boolean getIpv6() {
		return ipv6;
	}
}
