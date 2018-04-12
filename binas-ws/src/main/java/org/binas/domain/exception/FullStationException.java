package org.binas.domain.exception;

/** Exception used to signal that a usar alraedy has a Bina. */
public class FullStationException extends Exception {
	private static final long serialVersionUID = 1L;

	public FullStationException() {
	}

	public FullStationException(String message) {
		super(message);
	}
}
