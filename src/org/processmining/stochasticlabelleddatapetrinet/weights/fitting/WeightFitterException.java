package org.processmining.stochasticlabelleddatapetrinet.weights.fitting;

public class WeightFitterException extends Exception {

	private static final long serialVersionUID = 1858852872058672104L;

	WeightFitterException() {
		super();
	}

	WeightFitterException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	WeightFitterException(String message, Throwable cause) {
		super(message, cause);
	}

	WeightFitterException(String message) {
		super(message);
	}

	WeightFitterException(Throwable cause) {
		super(cause);
	}

}
