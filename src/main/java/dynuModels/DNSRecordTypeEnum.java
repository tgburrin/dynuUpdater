package dynuModels;

public enum DNSRecordTypeEnum {
	UNKNOWN("UNKNOWN"),
	IPV6("AAAA"),
	IPV4("A"),
	NAME_ALIAS("CNAME"),

	CERTIFICATE_AUTHORITY("CAA"),
	HOST_INFORMATION("HINFO"),
	LOCATION("LOC"),
	MAIL_EXCHANGE("MX"),
	NAMESERVER("NS"),
	REVERSE_LOOKUP("PTR"),
	START_OF_AUTHORITY("SOA"),
	TEXT("TXT"),
	URI("URI")
	;

	private String recordCode;

	DNSRecordTypeEnum(String dnsRecordCode) { recordCode = dnsRecordCode; }

	@Override
	public String toString() {
		return this.recordCode;
	}

	public static DNSRecordTypeEnum fromCode(String code) {
		for(DNSRecordTypeEnum e : DNSRecordTypeEnum.values() )
			if ( e.recordCode.equals(code) )
				return e;
		return DNSRecordTypeEnum.UNKNOWN;
	}
}
