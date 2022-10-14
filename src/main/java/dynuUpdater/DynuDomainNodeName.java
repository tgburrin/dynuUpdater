package dynuUpdater;

public class DynuDomainNodeName extends DynuDomainName {
	private String nodeName;
	private Long ipv4RecordId;
	private Long ipv6RecordId;

	public DynuDomainNodeName(String domainName, String nodeName) {
		super(domainName);
		this.nodeName = nodeName;
	}

	public DynuDomainNodeName(DynuDomainName domain, String nodeName) {
		super(domain.getDomainName());
		this.setDomainId(domain.getDomainId());
		this.nodeName = nodeName;
	}

	public String getNodeName() {
		return nodeName;
	}

	public Long getIpv4RecordId() {
		return ipv4RecordId;
	}

	public void setIpv4RecordId(Long ipv4RecordId) {
		this.ipv4RecordId = ipv4RecordId;
	}

	public Long getIpv6RecordId() {
		return ipv6RecordId;
	}

	public void setIpv6RecordId(Long ipv6RecordId) {
		this.ipv6RecordId = ipv6RecordId;
	}

	@Override
	public String getFullName() {
		return String.join(".", nodeName, getDomainName());
	}

	@Override
	public String toString() {
		return "DynuDomainName [\n"
				+ " domainId=" + getDomainId()
				+ ",\n domainName=" + getDomainName()
				+ ",\n nodeName=" + nodeName
				+ ",\n ipv4Address=" + getIpv4Address()
				+ ",\n ipv4RecordId=" + ipv4RecordId
				+ ",\n ipv6Address=" + getIpv6Address()
				+ ",\n ipv6RecordId=" + ipv6RecordId
				+ "\n]";
	}
}
