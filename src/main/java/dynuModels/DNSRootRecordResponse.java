package dynuModels;

import java.net.InetAddress;
import java.time.Instant;

public class DNSRootRecordResponse {
	private Integer statusCode;
	private Long id;
	private String name;
	private InetAddress ipv4Address;
	private InetAddress ipv6Address;
	private Boolean ipv4;
	private Boolean ipv6;
	private Instant createdOn;
	private Instant updatedOn;

	public Integer getStatusCode() {
		return statusCode;
	}
	public Long getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public InetAddress getIpv4Address() {
		return ipv4Address;
	}
	public InetAddress getIpv6Address() {
		return ipv6Address;
	}
	public Boolean getIpv4() {
		return ipv4;
	}
	public Boolean getIpv6() {
		return ipv6;
	}
	public Instant getCreatedOn() {
		return createdOn;
	}
	public Instant getUpdatedOn() {
		return updatedOn;
	}
	@Override
	public String toString() {
		String ipv4Addr = null;
		if( ipv4Address != null )
			ipv4Addr = ipv4Address.getHostName();

		String ipv6Addr = null;
		if( ipv6Address != null )
			ipv6Addr = ipv6Address.getHostName().split("%")[0];

		return "DNSRootRecordResponse [statusCode=" + statusCode + ", id=" + id + ", name=" + name + ", ipv4Address="
				+ ipv4Addr + ", ipv6Address=" + ipv6Addr + ", ipv4=" + ipv4 + ", ipv6=" + ipv6 + ", createdOn="
				+ createdOn + ", updatedOn=" + updatedOn + "]";
	}
}
