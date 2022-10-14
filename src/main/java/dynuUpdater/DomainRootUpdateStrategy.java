package dynuUpdater;

public class DomainRootUpdateStrategy implements DomainNameUpdateStrategy {

	@Override
	public boolean updateDomainName(DynuClient client, DynuDomainName domainName, DomainAddress address) {
		// client.update domain root
		return false;
	}

}
