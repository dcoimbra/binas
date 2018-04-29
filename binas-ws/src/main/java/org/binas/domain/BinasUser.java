package org.binas.domain;

import java.util.concurrent.atomic.AtomicInteger;

public class BinasUser {
	
	private final String email;
	private String password;
	private boolean withBina;

	
	protected BinasUser(String email, String password) {
		this.email = email;
		this.password = password;
	}
	
	//getters and setters
	
	public String getEmail() {
		return email;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public boolean isWithBina() {
		return withBina;
	}
	
	public void setWithBina(boolean withBina) {
		this.withBina = withBina;
	}
	

	

}
