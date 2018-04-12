package org.binas.domain.exception;

/** Exception used to signal that a usar alraedy has a Bina. */
public class NoBinaRentedException extends Exception {
	private static final long serialVersionUID = 1L;

	public NoBinaRentedException() {
	}

	public NoBinaRentedException(String message) {
		super(message);
	}
}
