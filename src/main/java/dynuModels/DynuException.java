package dynuModels;

public class DynuException {
	private Integer statusCode;
	private String type;
	private String message;

	public Integer getStatusCode() {
		return statusCode;
	}
	public String getType() {
		return type;
	}
	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return type+" ("+statusCode+"): " + message.replaceAll("\n$", "");
	}
}
