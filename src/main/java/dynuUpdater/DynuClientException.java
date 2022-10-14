package dynuUpdater;

import dynuModels.DynuException;

public class DynuClientException extends Exception {
	public DynuClientException(DynuException ex) {
		super(ex.toString());
	}

	public DynuClientException(String msg) {
		super(msg);
	}
}
