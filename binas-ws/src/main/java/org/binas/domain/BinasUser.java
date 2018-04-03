package org.binas.domain;

import java.util.HashSet;
import java.util.Set;

public class BinasUser {
	
	private final String email;
	private String password;
	private int credit;
	private boolean withBina;
	private static Set<String> emails = new HashSet<>();

	
	public BinasUser(String email, String password) {
		this.email = email;
		this.password = password;

		this.getEmails().add(email);
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
	
	public static Set<String> getEmails(){ return emails;}

	

}
