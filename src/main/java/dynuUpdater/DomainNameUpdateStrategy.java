package dynuUpdater;

public interface DomainNameUpdateStrategy {
	public void updateDomainName (DynuClient client, DynuDomainName domainName);
}
