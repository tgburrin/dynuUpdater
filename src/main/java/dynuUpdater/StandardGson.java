package dynuUpdater;

import java.lang.reflect.Type;
import java.net.Inet6Address;
import java.time.Instant;
import java.time.OffsetDateTime;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class StandardGson {
	public static Gson getGson (Boolean fieldUnderscores) {
		JsonDeserializer<Instant> instDes = new JsonDeserializer<Instant>() {
			@Override
			public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
					throws JsonParseException {
				String ds = json.getAsString();
				if ( ds.matches(".*[+-][0-9]{2}:[0-9]{2}Z$") )
					return OffsetDateTime.parse(ds.replaceAll("Z$", "")).toInstant();
				else if ( ds.matches(".*[+-][0-9]{2}:[0-9]{2}$") )
					return OffsetDateTime.parse(ds.replaceAll("Z$", "")).toInstant();
				else if ( ds.matches(".*Z$") )
					return Instant.parse(ds);
				else // The timestamp might not contain the required UTC symbol 'Z' at the end, so lets append it
					return Instant.parse(ds+"Z");
			}
		};

		JsonSerializer<Inet6Address> inetSer = new JsonSerializer<Inet6Address>() {
			@Override
			public JsonElement serialize(Inet6Address src, Type typeOfSrc, JsonSerializationContext context) {
				return new JsonPrimitive(src.getHostAddress().split("%")[0]);
			}
		};

		JsonDeserializer<DNSRecordTypeEnum> dnsTypeDes = new JsonDeserializer<DNSRecordTypeEnum>() {

			@Override
			public DNSRecordTypeEnum deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
					throws JsonParseException {
				return DNSRecordTypeEnum.fromCode(json.getAsString());
			}
		};

		JsonSerializer<DNSRecordTypeEnum> dnsTypeSer = new JsonSerializer<DNSRecordTypeEnum>() {
			@Override
			public JsonElement serialize(DNSRecordTypeEnum src, Type typeOfSrc, JsonSerializationContext context) {
				return new JsonPrimitive(src.toString());
			}
		};

		GsonBuilder gb = new GsonBuilder()
				//.serializeNulls()
				.registerTypeAdapter(Instant.class, instDes)
				.registerTypeAdapter(Inet6Address.class, inetSer)
				.registerTypeAdapter(DNSRecordTypeEnum.class, dnsTypeSer)
				.registerTypeAdapter(DNSRecordTypeEnum.class, dnsTypeDes)
				;

		if ( fieldUnderscores )
			gb.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);

		return gb.create();
	}

	public static Gson getGson () {
		return getGson(false);
	}
}
