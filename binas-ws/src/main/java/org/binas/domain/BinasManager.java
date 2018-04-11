package org.binas.domain;

import org.binas.domain.exception.AlreadyHasBinaException;
import org.binas.domain.exception.NoCreditException;
import org.binas.domain.exception.UserNotExistsException;
import org.binas.ws.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	public BinasUser activateUser(String email) throws EmailExists_Exception, InvalidEmail_Exception {

		Pattern p = Pattern.compile("[a-z]*@[a-z]*\\.[a-z]*");
		Matcher match = p.matcher(email);

		if(!match.find()){
			InvalidEmail faultInfo = new InvalidEmail();
			throw new InvalidEmail_Exception("Email is invalid", faultInfo);
		}
		else if(!BinasUser.getEmails().add(email)){

			EmailExists faultInfo = new EmailExists();
			throw new EmailExists_Exception("Email already exists", faultInfo);
		}

		return new BinasUser(email, "pass");

	}

	public StationView getInfoStation(String stationId) throws InvalidStation_Exception {

		StationView view = stations.get(stationId);

		if(view == null){
			InvalidStation faultInfo = new InvalidStation();
			throw new InvalidStation_Exception("Station is valid", faultInfo);
		}

		return view;
	}

	public List<StationView> listStations(Integer numberOfStations, CoordinatesView coordinates) {

		Map<Double, StationView> distances = new TreeMap<>();
		List<StationView> stationViews = new ArrayList<>();
		Integer x1 = coordinates.getX();
		Integer y1 = coordinates.getY();

		for (StationView station : stations.values()) {

			CoordinatesView c = station.getCoordinate();
			Integer x2 = c.getX();
			Integer y2 = c.getY();

			Double x = Math.pow(Math.abs(x1 - x2), 2);
			Double y = Math.pow(Math.abs(y1 - y2), 2);
			Double hipotenusa = Math.sqrt(x + y);

			distances.put(hipotenusa, station);

		}

		for(Map.Entry<Double, StationView> entry : distances.entrySet()) {

			stationViews.add(entry.getValue());

			if(stationViews.size() == numberOfStations){
				break;
			}

		}

		return stationViews;
	}

	public HashMap<String, StationView> getStations() {
		return stations;
	}
}
