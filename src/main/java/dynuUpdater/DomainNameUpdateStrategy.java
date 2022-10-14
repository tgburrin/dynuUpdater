package dynuUpdater;

public interface DomainNameUpdateStrategy {
	public boolean updateDomainName (DynuClient client, DynuDomainName domainName, DomainAddress address);
}
