package org.binas.domain;

import org.binas.domain.exception.*;
import org.binas.station.ws.cli.StationClient;
import org.binas.ws.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BinasManager {
	
	private static Map<String, BinasUser> users = new HashMap<>();
	private static HashMap<String, StationView> stationViews = new HashMap();
	private static HashMap<String, StationClient> stationClients = new HashMap();



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

	public BinasUser activateUser(String email) throws EmailExistsException, InvalidEmailException {

		Pattern p = Pattern.compile("[a-z]*@[a-z]*\\.[a-z]*");
		Matcher match = p.matcher(email);

		if(!match.find()){
			throw new InvalidEmailException("Email is invalid");
		}
		else if(!BinasUser.getEmails().add(email)){
			throw new EmailExistsException("Email already exists");
		}

		BinasUser user = new BinasUser(email, "pass");

		addUser(user);

		return user;

	}

	public StationView getInfoStation(String stationId) throws InvalidStationException {

		StationView view = stationViews.get(stationId);

		if(view == null){
			InvalidStation faultInfo = new InvalidStation();
			throw new InvalidStationException("Station is valid");
		}

		return view;
	}

	/*public List<StationView> listStations(Integer numberOfStations, CoordinatesView coordinates) {

		Map<Double, List<StationView>> distances = new TreeMap<>();
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

			distances.put(hipotenusa, );

		}

		/*for(Map.Entry<StationView, Double> entry : distances.entrySet()) {

			stationViews.add(entry.getKey());

			if(stationViews.size() == numberOfStations){
				break;
			}

		}

		return stationViews;
	}*/

	public HashMap<String, StationView> getStationViews() {
		return stationViews;
	}

	public void addUser(BinasUser user) {

		users.put(user.getEmail(), user);
	}

	public void addStationView(StationView station) {

		stationViews.put(station.getId(), station);
	}

	public void addStationClient(StationClient client) {

		stationClients.put(client.getInfo().getId(), client);
	}

	public void reset() {

		users.clear();
	}

	public void testInitStation(String stationId, int x, int y, int capacity, int returnPrize) throws BadInitException {

		StationClient client = stationClients.get(stationId);

		try {
			client.testInit(x, y, capacity, returnPrize);
		} catch (org.binas.station.ws.BadInit_Exception e) {
			throw new BadInitException(e.getMessage());
		}

		org.binas.station.ws.StationView oldStationView = client.getInfo();

		org.binas.ws.StationView newStationView = buildStationView(oldStationView);

		stationViews.put(newStationView.getId(), newStationView);
	}

	public void testInit() throws BadInitException {

		BinasUser user = null;
		try {
			user = activateUser("david@tecnico.pt");
		} catch (EmailExistsException e) {
			throw new BadInitException(e.getMessage());
		} catch (InvalidEmailException e) {
			throw new BadInitException(e.getMessage());
		}
	}

	/** Helper to build a Binas StationView from a Station StationView. */
	private org.binas.ws.StationView buildStationView(org.binas.station.ws.StationView view) {
		org.binas.ws.StationView newView = new StationView();
		newView.setId(view.getId());
		newView.setCoordinate(buildCoordinatesView(view.getCoordinate()));
		newView.setCapacity(view.getCapacity());
		newView.setTotalGets(view.getTotalGets());
		newView.setTotalReturns(view.getTotalReturns());
		newView.setFreeDocks(view.getFreeDocks());
		newView.setAvailableBinas(view.getAvailableBinas());
		return newView;
	}

	/** Helper to build a Binas CoordinatesView from a Station Coordinates view. */
	private org.binas.ws.CoordinatesView buildCoordinatesView(org.binas.station.ws.CoordinatesView view) {
		CoordinatesView newView = new CoordinatesView();
		newView.setX(view.getX());
		newView.setY(view.getY());
		return newView;
	}
}
