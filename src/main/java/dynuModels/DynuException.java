package dynuModels;

public class DynuException {
	public class DynuExceptionMessage {
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
	}
	private DynuExceptionMessage exception;

	public DynuExceptionMessage getException() {
		return exception;
	}
}
