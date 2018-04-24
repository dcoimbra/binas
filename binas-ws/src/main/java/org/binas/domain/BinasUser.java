package org.binas.domain;

import java.util.concurrent.atomic.AtomicInteger;

public class BinasUser {
	
	private final String email;
	private String password;
	private int credit;
	private boolean withBina;
	
	private static AtomicInteger initVal = new AtomicInteger(10);

	
	protected BinasUser(String email, String password) {
		this.email = email;
		this.password = password;

		setCredit(initVal.get());
	}
	
	public void changeCredit(int credit) {
		this.credit += credit;
	}
	
	//getters and setters
	public static void setinitVal(int initVal) {
		BinasUser.initVal.set(initVal);
	}
	
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
