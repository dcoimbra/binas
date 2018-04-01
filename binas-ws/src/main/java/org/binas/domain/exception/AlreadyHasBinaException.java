package org.binas.domain.exception;

/** Exception used to signal that a usar alraedy has a Bina. */
public class AlreadyHasBinaException extends Exception {
	private static final long serialVersionUID = 1L;

	public AlreadyHasBinaException() {
	}

	public AlreadyHasBinaException(String message) {
		super(message);
	}
}
