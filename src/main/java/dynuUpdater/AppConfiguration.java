package dynuUpdater;

import java.util.HashMap;
import java.util.HashSet;

public class AppConfiguration {
	private String accessToken;
	private String interfaceName;
	private Integer pollPeriod = 900;
	private HashSet<String> addressFamiles;
	private HashMap<String,HashSet<String>> targets;

	public String getAccessToken() {
		return accessToken;
	}

	public String getInterfaceName() {
		return interfaceName;
	}

	public HashSet<String> getAddressFamiles() {
		return addressFamiles;
	}

	public HashMap<String,HashSet<String>> getTargets() {
		return targets;
	}

	public Integer getPollPeriod() {
		return pollPeriod;
	}

	@Override
	public String toString() {
		return "AppConfiguration [accessToken=<removed>, pollPeriod="+pollPeriod+", interfaceName="
				+ interfaceName + ", addressFamiles=" + addressFamiles + ", targets=" + targets + "]";
	}
}
