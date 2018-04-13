package org.binas.domain;

public class BinasUser {
	
	private final String email;
	private String password;
	private int credit;
	private boolean withBina;

	
	protected BinasUser(String email, String password) {
		this.email = email;
		this.password = password;

		setCredit(10);
	}
	
	public void changeCredit(int credit) {
		this.credit += credit;
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
	
	public int getCredit() {
		return credit;
	}
	
	public void setCredit(int credit) {
		this.credit = credit;
	}
	
	public boolean isWithBina() {
		return withBina;
	}
	
	public void setWithBina(boolean withBina) {
		this.withBina = withBina;
	}
	

	

}
