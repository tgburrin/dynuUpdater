package dynuModels;

import java.util.ArrayList;
import java.util.Arrays;

import dynuUpdater.DNSRecordTypeEnum;

public class DNSIdRecordResponse {
	private Integer statusCode;
	private ArrayList<DNSIdRecord> dnsRecords;

	public ArrayList<DNSIdRecord> getDnsRecords() {
		return dnsRecords;
	}

	@Override
	public String toString() {
		StringBuffer rv = new StringBuffer();
		rv.append("Status: "+statusCode+"\n");
		rv.append("Body: \n");
		for(DNSIdRecord rec : dnsRecords) {
			if(Arrays.asList(DNSRecordTypeEnum.IPV4, DNSRecordTypeEnum.IPV6).contains(rec.getRecordType()))
				rv.append("\t"+rec.getDomainName()+" "+rec.getNodeName());
			else
				rv.append("\t"+rec.toString());
		}
		return rv.toString();
	}
}
