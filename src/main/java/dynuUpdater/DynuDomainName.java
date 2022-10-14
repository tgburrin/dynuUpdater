package dynuUpdater;

public class DynuDomainName {
	private Long domainId;
	private String domainName;
	private DomainAddress ipv4Address;
	private DomainAddress ipv6Address;

	public DynuDomainName (String domainName) {
		this.domainName = domainName;
	}

	public Long getDomainId() {
		return domainId;
	}
	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}
	public String getDomainName() {
		return domainName;
	}
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	public DomainAddress getIpv4Address() {
		return ipv4Address;
	}
	public void setIpv4Address(DomainAddress ipv4Address) {
		this.ipv4Address = ipv4Address;
	}
	public DomainAddress getIpv6Address() {
		return ipv6Address;
	}
	public void setIpv6Address(DomainAddress ipv6Address) {
		this.ipv6Address = ipv6Address;
	}

	public String getFullName() {
		return this.domainName;
	}

	@Override
	public String toString() {
		return "DynuDomainName [\n"
				+ " domainId=" + domainId
				+ ",\n domainName=" + domainName
				+ ",\n ipv4Address=" + ipv4Address
				+ ",\n ipv6Address=" + ipv6Address
				+ "\n]";
	}
}
