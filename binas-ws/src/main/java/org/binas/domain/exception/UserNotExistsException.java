package org.binas.domain.exception;

/** Exception used to signal that usar has no credit. */
public class UserNotExistsException extends Exception {
	private static final long serialVersionUID = 1L;

	public UserNotExistsException() {
	}

	public UserNotExistsException(String message) {
		super(message);
	}
}
