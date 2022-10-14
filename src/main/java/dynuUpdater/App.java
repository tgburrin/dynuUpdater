package dynuUpdater;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.google.gson.Gson;

public class App {
	private static Logger logger = Logger.getLogger(App.class.getName());
	private static boolean enableIPv4 = false;
	private static boolean enableIPv6 = false;

	private static ArrayList<DomainAddress> getInterfaceAddresses(NetworkInterface iface) {
		ArrayList<DomainAddress> addrs = new ArrayList<DomainAddress>();
		for( InetAddress ia : Collections.list(iface.getInetAddresses()) ) {
			if ( enableIPv4 && ia instanceof Inet4Address) {
				addrs.add(new DomainAddress(ia));
			} else if ( enableIPv6 && ia instanceof Inet6Address) {
				addrs.add(new DomainAddress(ia));
			}
		}
		return addrs;
	}

	private static ArrayList<DynuDomainName> getDomainNames (AppConfiguration ac) {
		ArrayList<DynuDomainName> rv = new ArrayList<DynuDomainName>();
		ArrayList<DynuDomainName> nodes = new ArrayList<DynuDomainName>();
		HashMap<String,HashSet<String>> targets = ac.getTargets();
		for(String domainName : targets.keySet()) {
			for(String nodeName : targets.get(domainName)) {
				DynuDomainName d = null;
				if ( nodeName == null ) {
					d = new DynuDomainName(domainName);
					rv.add(d);
				} else {
					d = new DynuDomainNodeName(domainName, nodeName);
					nodes.add(d);
				}
			}
		}
		// ensures that root domains are added before domain nodes
		if ( !nodes.isEmpty() )
			rv.addAll(nodes);
		return rv;
	}

    public static void main(String[] args) throws Exception {
        InputStream stream = App.class.getClassLoader()
                .getResourceAsStream("logging.properties");
        LogManager.getLogManager().readConfiguration(stream);

        Long pid = ProcessHandle.current().pid();
		BufferedReader br = new BufferedReader(new FileReader(args[0]));
		Gson gb = StandardGson.getGson(true);

		AppConfiguration ac = gb.fromJson(br, AppConfiguration.class);
		enableIPv4 = ac.getAddressFamiles().contains("ipv4");
		enableIPv6 = ac.getAddressFamiles().contains("ipv6");
		ArrayList<DynuDomainName> domainNames = getDomainNames(ac);

		NetworkInterface iface = null;
		for(NetworkInterface i : Collections.list(NetworkInterface.getNetworkInterfaces())) {
			if ( i.getName().equals(ac.getInterfaceName()))
				iface = i;
		}

		if ( iface == null ) {
			logger.log(Level.SEVERE, "The configured interface "+ac.getInterfaceName()+" could not be found");
			System.exit(1);
		}
		logger.info("Getting addresses for interface "+iface.getName());

		DynuClient dc = new DynuClient(ac.getAccessToken());

		logger.info("Starting process in pid "+pid);
		do {
			logger.info("Checking records...");
			ArrayList<DomainAddress> addresses = getInterfaceAddresses(iface);
			for(DynuDomainName dn : domainNames) {
				dc.maintainDomainName(dn, addresses);
			}
			// dc.maintainRecords();
			logger.info("Sleeping for "+ac.getPollPeriod()+"s...");
			Thread.sleep(ac.getPollPeriod() * 1000);
		} while ( true ); // run forever
	}
}
