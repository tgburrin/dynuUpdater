package dynuUpdater;

public class DomainNodeUpdateStrategy implements DomainNameUpdateStrategy {
	@Override
	public void updateDomainName(DynuClient client, DynuDomainName domainName) {
		client.maintainDynDomainNameNode((DynuDomainNodeName) domainName);
	}

}
