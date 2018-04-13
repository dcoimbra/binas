package org.binas.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.binas.domain.exception.AlreadyHasBinaException;
import org.binas.domain.exception.BadInitException;
import org.binas.domain.exception.EmailExistsException;
import org.binas.domain.exception.FullStationException;
import org.binas.domain.exception.InvalidEmailException;
import org.binas.domain.exception.InvalidStationException;
import org.binas.domain.exception.NoBinaAvailException;
import org.binas.domain.exception.NoBinaRentedException;
import org.binas.domain.exception.NoCreditException;
import org.binas.domain.exception.UserNotExistsException;
import org.binas.station.ws.NoBinaAvail_Exception;
import org.binas.station.ws.NoSlotAvail_Exception;
import org.binas.station.ws.BadInit_Exception;
import org.binas.station.ws.cli.StationClient;
import org.binas.ws.CoordinatesView;
import org.binas.ws.StationView;

public class BinasManager {
	
	private static Map<String, BinasUser> users = new HashMap<>();
	private static Map<String, StationView> stationViews = new HashMap<>();



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

	public void rentBina(StationClient station, String email) throws NoCreditException, AlreadyHasBinaException, UserNotExistsException, InvalidStationException, NoBinaAvailException {
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
			throw new NoBinaAvailException("No bicicles available");
		}
		
	}
	
	public void returnBina(StationClient station, String email) throws UserNotExistsException, NoBinaRentedException, FullStationException, InvalidStationException {
		BinasUser user = getUser(email);
		int old_credit = user.getCredit();
		if(!user.isWithBina()) {
			throw new NoBinaRentedException("User currently has no bicicle");
		}
		try {
			int bonus = station.returnBina();
			user.setWithBina(false);
			user.setCredit(old_credit+bonus);
		} catch (NoSlotAvail_Exception e) {
			throw new FullStationException("Station is full");
		}
	}
	

	private BinasUser getUser(String email) throws UserNotExistsException {
		BinasUser user = users.get(email);
		if (user == null)
			throw new UserNotExistsException("User doesn't exist");
		return user;
	}

	public BinasUser activateUser(String email) throws EmailExistsException, InvalidEmailException {

		if(email == null){
			throw new InvalidEmailException("Email is invalid");
		}

		Pattern p = Pattern.compile("[a-z0-9]+@[a-z0-9]+\\.[a-z]+");
		Matcher match = p.matcher(email);

		if( !match.find() ){
			throw new InvalidEmailException("Email is invalid");
		}
		else if(!BinasUser.getEmails().add(email)){
			throw new EmailExistsException("Email already exists");
		}

		BinasUser user = new BinasUser(email, "pass");
		addUser(user);

		return user;

	}

	public StationView getInfoStation(StationClient station) throws InvalidStationException {

		StationView view = buildStationView(station.getInfo());

		if(view == null){
			throw new InvalidStationException("Station is invalid");
		}

		return view;
	}

	public List<StationView> listStations(Integer numberOfStations, CoordinatesView coordinates) {

		Map<Integer, List<StationView>> distances = new TreeMap<>();
		Integer x1 = coordinates.getX();
		Integer y1 = coordinates.getY();

		for (StationView station : stationViews.values()) {

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

	private List<StationView> ascendingStationViews(Integer numberOfStations, Map<Integer, List<StationView>> distances) {

		List<StationView> stationViewList = new ArrayList<>();

		for(Map.Entry<Integer, List<StationView>> entry : distances.entrySet()) {

			for(StationView view : entry.getValue()){
				stationViewList.add(view);

				if(stationViews.size() == numberOfStations){
					return stationViewList;
				}

			}

		}

		return stationViewList;
	}

	public void addUser(BinasUser user) {

		users.put(user.getEmail(), user);
	}

	public void addStationView(StationView station) {

		stationViews.put(station.getId(), station);
	}

	public void reset() {

		users.clear();
	}

	public void testInitStation(StationClient client, int x, int y, int capacity, int returnPrize) throws BadInitException {

		try {
			client.testInit(x, y, capacity, returnPrize);
		} catch (BadInit_Exception e) {
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
