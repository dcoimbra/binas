package org.binas.domain;

import org.binas.domain.exception.*;
import org.binas.station.ws.NoBinaAvail_Exception;
import org.binas.station.ws.NoSlotAvail_Exception;
import org.binas.station.ws.cli.StationClient;
import org.binas.ws.CoordinatesView;
import org.binas.ws.StationView;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BinasManager {
	
	private static Map<String, BinasUser> users = new HashMap<>();
	private static AtomicInteger seq = new AtomicInteger(1); //tag seq value


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

	/** returns the available credit of the user associated to the given email*/
	public synchronized int getCredit(String email) throws UserNotExistsException {
		return getUser(email).getCredit();
	}

	/** rents a bicycle for the given user at a given station 
	 * by asking said station to release one bicycle if conditions are met*/
	public synchronized void rentBina(StationClient station, String email) throws NoCreditException, AlreadyHasBinaException, UserNotExistsException, InvalidStationException, NoBinaAvailException {
		BinasUser user = getUser(email);
		int old_credit = user.getCredit();
		if ( old_credit < 1)
			throw new NoCreditException("No credit available");
		if(user.isWithBina())
			throw new AlreadyHasBinaException("User already has Bina");
		try {
			station.getBina();
			user.setWithBina(true);
			user.setCredit(old_credit - 1);
		} catch (NoBinaAvail_Exception e) {
			throw new NoBinaAvailException("No bicycles available");
		}
		
	}
	
	/** returns a bicycle from the given user at a given station 
	 * by asking said station to accept a bicycle if conditions are met*/
	public synchronized void returnBina(StationClient station, String email) throws UserNotExistsException, NoBinaRentedException, FullStationException, InvalidStationException {
		BinasUser user = getUser(email);
		int old_credit = user.getCredit();
		if(!user.isWithBina()) {
			throw new NoBinaRentedException("User currently has no bicycle");
		}
		try {
			int bonus = station.returnBina();
			user.setWithBina(false);
			user.setCredit(old_credit+bonus);
		} catch (NoSlotAvail_Exception e) {
			throw new FullStationException("Station is full");
		}
	}
	
	/** returns a binas' user given his registered e-mail address*/
	private synchronized BinasUser getUser(String email) throws UserNotExistsException {
		if(email == null || email.equals(""))
			throw new UserNotExistsException("No user referred");
		BinasUser user = users.get(email);
		if (user == null)
			throw new UserNotExistsException("User doesn't exist");
		return user;
	}

	/** activates a binas' user by registering his e-mail address*/
	public synchronized BinasUser activateUser(String email, Collection<StationClient> stationClients) throws EmailExistsException, InvalidEmailException {

		if(email == null){
			throw new InvalidEmailException("Email is invalid");
		}

		if( !email.matches("[a-zA-Z0-9]+(\\.[a-zA-Z0-9]+)?@[a-zA-Z0-9]+(\\.[a-zA-Z0-9]+)?")){
			throw new InvalidEmailException("Email is invalid");
		}
		
		if(users.containsKey(email)){
			throw new EmailExistsException("Email already exists");
		}

		for(StationClient sc : stationClients) {	 // verifies if the email is already registered in a station
			if(sc.getBalance(email) != null) {
				throw new EmailExistsException("Email already exists");
			}
		}
		
		BinasUser user = new BinasUser(email, "pass");
		addUser(user);
		
		String tag = newTag();
		for(StationClient sc : stationClients) {	//registers the new user on every station with a new tag
			sc.setBalance(email, user.getCredit(), tag);
		}

		return user;

	}

	private static String newTag() {
		return seq.incrementAndGet() + ":" + "T07_Binas"; //TODO add ws.name to tag - implement getter?
	}
	
	/**returns a StationView object given a Station Client entity
	 * containing all the info on said station until this moment*/
	public synchronized StationView getInfoStation(StationClient station) throws InvalidStationException {

		StationView view = buildStationView(station.getInfo());

		if(view == null){
			throw new InvalidStationException("Station is invalid");
		}

		return view;
	}
	
	/** returns a list with the k closest stations ordered by distance 
	 * with k being the number of stations to present */
	public synchronized List<StationView> listStations(Integer numberOfStations, CoordinatesView coordinates, Collection<StationClient> stationClients) {

		if(!checkArguments(numberOfStations, coordinates)){
			return new ArrayList<>();
		}
		else{
			Map<Integer, List<StationView>> distances = new TreeMap<>();
			Integer x1 = coordinates.getX();
			Integer y1 = coordinates.getY();

			for (StationView station : getStationViews(stationClients)) {

				CoordinatesView c = station.getCoordinate();
				Integer x2 = c.getX();
				Integer y2 = c.getY();

				int x = (int) Math.pow(Math.abs(x1 - x2), 2);
				int y = (int) Math.pow(Math.abs(y1 - y2), 2);
				int hipoQuadrada = x + y;

				if(distances.containsKey(hipoQuadrada)){
					distances.get(hipoQuadrada).add(station);
				}
				else{
					List<StationView> list = new ArrayList<>();
					list.add(station);

					distances.put(hipoQuadrada, list);

				}
			}

			return ascendingStationViews(numberOfStations, distances);
		}
	}

	/** auxiliary method for listStations: checks arguments validity*/
	private boolean checkArguments(Integer numberOfStations, CoordinatesView coordinates) {

		int x = coordinates.getX();
		int y = coordinates.getY();

		return ((0 <= x && x <= 99) && (0<= y && y <=99) && numberOfStations > 0);
	}

	/** auxiliary method for listStations: orders k stations by distance value
	 * from from largest to smallest*/
	private List<StationView> ascendingStationViews(Integer numberOfStations, Map<Integer, List<StationView>> distances) {

		List<StationView> stationViewList = new ArrayList<>();

		for(Map.Entry<Integer, List<StationView>> entry : distances.entrySet()) {

			for(StationView view : entry.getValue()){
				stationViewList.add(view);

				if(stationViewList.size() == numberOfStations){
					return stationViewList;
				}

			}

		}

		return stationViewList;
	}

	/** adds a e-mail address/user pair to the map of registered users*/
	public void addUser(BinasUser user) {

		users.put(user.getEmail(), user);
	}

	/** returns a list of stationViews given a collection of stationClients */
	public synchronized List<StationView> getStationViews(Collection<StationClient> stationClients) {
		List<StationView> stationViews = new ArrayList<>();
		for(StationClient sc : stationClients) {
			stationViews.add(buildStationView(sc.getInfo()));
		}
		return stationViews;
	}

	/** Delete all users.*/
	public void reset() {
		users.clear();
		BinasUser.setinitVal(10);

	}
	
    // Test methods ---------------------------------------------------------------------------
	/** Test related methods */
	
	public String testPing(String inputMessage, Collection<StationClient> stationClients) {
		String result = "Test Ping:\n";
		for (StationClient sc : stationClients) {
			result += sc.testPing(inputMessage)+"\n";
		}
		
		return result;
	}

	public synchronized void testInitStation(StationClient client, int x, int y, int capacity, int returnPrize) throws BadInitException {

		try {
			client.testInit(x, y, capacity, returnPrize);
		} catch (org.binas.station.ws.BadInit_Exception e) {
			throw new BadInitException(e.getMessage());
		}

	}

	public synchronized void testInit(int userInitialPoints) throws BadInitException {

		if (userInitialPoints < 0 ) {

			throw new BadInitException("Credit must be non negative");
		}

		BinasUser.setinitVal(userInitialPoints);
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
