package org.binas.domain;

import org.binas.domain.exception.AlreadyHasBinaException;
import org.binas.domain.exception.NoCreditException;
import org.binas.domain.exception.UserNotExistsException;
import org.binas.ws.StationView;

import java.util.HashMap;
import java.util.Map;

public class BinasManager {
	
	private static Map<String, BinasUser> users = new HashMap<>();
	private static HashMap<String, StationView> stations = new HashMap();


	// Singleton -------------------------------------------------------------

	private BinasManager() {
	}

	/**
	 * SingletonHolder is loaded on the first execution of Singleton.getInstance()
	 * or the first access to SingletonHolder.INSTANCE, not before.
	 */
	private static class SingletonHolder {
		private static final BinasManager INSTANCE = new BinasManager();
	}

	public static synchronized BinasManager getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public int getCredit(String email) throws UserNotExistsException {
		return getUser(email).getCredit();
	}

	public void rentBina(String stationId, String email) throws NoCreditException, AlreadyHasBinaException, UserNotExistsException {
//		BinasUser user = getUser(email);
//		int credit = user.getCredit();
//		if ( credit < 1)
//			throw new NoCreditException("No credit available");
//		if(user.isWithBina())
//			throw new AlreadyHasBinaException("User already has Bina");
//		//TODO getStationClientById(stationId).rentBina();
//		user.setCredit(credit - 1);
	}
	
	private BinasUser getUser(String email) throws UserNotExistsException {
		BinasUser user = users.get(email);
		if (user == null)
			throw new UserNotExistsException("User doesn't exist");
		return user;
	}

	public HashMap<String, StationView> getStations() {
		return stations;
	}
}
