package dynuUpdater;

public class DomainRootUpdateStrategy implements DomainNameUpdateStrategy {
	@Override
	public void updateDomainName(DynuClient client, DynuDomainName domainName) {
		client.maintainDynDomainNameRoot(domainName);
	}

}
