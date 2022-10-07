package dynuUpdater;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.google.gson.Gson;

public class App {
	private static Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) throws Exception {
        InputStream stream = App.class.getClassLoader()
                .getResourceAsStream("logging.properties");
        LogManager.getLogManager().readConfiguration(stream);

        Long pid = ProcessHandle.current().pid();
		BufferedReader br = new BufferedReader(new FileReader(args[0]));
		Gson gb = StandardGson.getGson(true);
		AppConfiguration ac = gb.fromJson(br, AppConfiguration.class);

		logger.info("Getting addresses for interface "+ac.getInterfaceName());

		NetworkInterface iface = null;
		for(NetworkInterface i : Collections.list(NetworkInterface.getNetworkInterfaces())) {
			if ( i.getName().equals(ac.getInterfaceName()))
				iface = i;
		}

		if ( iface == null ) {
			logger.log(Level.SEVERE, "The configured interface "+ac.getInterfaceName()+" could not be found");
			System.exit(1);
		}

		DynuClient dc = new DynuClient(ac.getAccessToken(), iface);
		dc.setHostnames(ac.getTargets());
		if ( ac.getAddressFamiles().contains("ipv4") )
			dc.enableIPv4();
		if ( ac.getAddressFamiles().contains("ipv6") )
			dc.enableIPv6();

		logger.info("Starting process in pid "+pid);
		//dc.maintainRecords();

		while ( true ) { // run forever
			logger.info("Checking records...");
			dc.maintainRecords();
			logger.info("Sleeping...");
			Thread.sleep(ac.getPollPeriod() * 1000);
		}
	}
}
