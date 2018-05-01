package org.binas.domain;

import org.binas.domain.exception.*;
import org.binas.station.ws.*;
import org.binas.station.ws.cli.StationClient;
import org.binas.ws.CoordinatesView;
import org.binas.ws.StationView;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class BinasManager {
	
	private static Map<String, BinasUser> users = new HashMap<>();

	private static AtomicInteger initVal = new AtomicInteger(10);

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

	/** returns the available credit of the user associated to the given email
	 * @throws UserNotExistsException 
	 * @throws InvalidEmailException */
	public synchronized int getCredit(String email, Collection<StationClient> stationClients) throws UserNotExistsException, InvalidEmailException {
		getUser(email);

		ValTagPair vtp = getBalance(email, stationClients);

		if (vtp == null) {

			throw new InvalidEmailException("Invalid email");
		}

		return vtp.getBalance();
	}

	public static void setinitVal(int initVal) {
		BinasManager.initVal.set(initVal);
	}

	/** rents a bicycle for the given user at a given station 
	 * by asking said station to release one bicycle if conditions are met*/
	public synchronized void rentBina(StationClient station, String email, Collection<StationClient> stationClients) throws NoCreditException, AlreadyHasBinaException, UserNotExistsException, InvalidStationException, NoBinaAvailException, InvalidEmailException {
		BinasUser user = getUser(email);
		if(user.isWithBina())
			throw new AlreadyHasBinaException("User already has Bina");
		
		try {
			ValTagPair maxValTagPair = getBalance(email, stationClients);
			int old_credit = maxValTagPair.getBalance(); //user.getCredit();
			if ( old_credit < 1)
				throw new NoCreditException("No credit available");
			station.getBina();
			user.setWithBina(true);
			setBalance(email, old_credit-1, stationClients, maxValTagPair.getTag());//user.setCredit(old_credit - 1);
		} catch (NoBinaAvail_Exception e) {
			throw new NoBinaAvailException("No bicycles available");
		}
		
	}
	
	/** returns a bicycle from the given user at a given station 
	 * by asking said station to accept a bicycle if conditions are met*/
	public synchronized void returnBina(StationClient station, String email, Collection<StationClient> stationClients) throws UserNotExistsException, NoBinaRentedException, FullStationException, InvalidStationException, InvalidEmailException {
		BinasUser user = getUser(email);
		
		if(!user.isWithBina()) {
			throw new NoBinaRentedException("User currently has no bicycle");
		}
		try {
			ValTagPair maxValTagPair = getBalance(email, stationClients);
			int old_credit = maxValTagPair.getBalance();//user.getCredit();
			int bonus = station.returnBina();
			user.setWithBina(false);
			if(bonus != 0)
				setBalance(email, old_credit+bonus, stationClients, maxValTagPair.getTag());//user.setCredit(old_credit+bonus);
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

		checkEmail(email);
		
		if(users.containsKey(email)){
			throw new EmailExistsException("Email already exists");
		}

		 // verifies if the email is already registered in a station
		ValTagPair vtp = getBalance(email, stationClients);

		if(vtp != null) {
			throw new EmailExistsException("Email already exists");
		}
		
		BinasUser user = new BinasUser(email, "pass");
		addUser(user);
		setBalance(user.getEmail(), initVal.get(), stationClients, "0:T07_Binas");  //registers the new user in the replicas

		return user;

	}

	/** returns the <val,tag> corresponding to the maxTag stored in X replicas */
	private ValTagPair getBalance(String email, Collection<StationClient> stationClients) throws InvalidEmailException {

		checkEmail(email);

		ValTagPair maxValTagPair = null;
		List<ValTagPair> vtList = new ArrayList<>();
		
		int quorum = (stationClients.size()/2)+1;
		
		CountDownLatch semaphore = new CountDownLatch(quorum);
		
		for(StationClient sc : stationClients) {
			sc.getBalanceAsync(email, new AsyncHandler<GetBalanceResponse>() {
				
				@Override
				public  void handleResponse(Response<GetBalanceResponse> res) {
					try {
						synchronized (vtList) {
							if(vtList.size() == quorum) { System.out.println("----/ignoring answers/----"); return;}  //ignores responses when quorum is fulfilled
							System.out.println("trying to receive <val,tag> from " + Thread.currentThread().getId());
							vtList.add(res.get().getValTagPair());
							semaphore.countDown();
						}
						
					} catch (InterruptedException e) {
		                   System.out.println("Caught interrupted exception.");
//		                   System.out.print("Cause: ");
//		                   System.out.println(e.getCause());
		            } catch (ExecutionException e) {
		                   System.out.println("Caught execution exception.");
//		                   System.out.print("Cause: ");
//		                   System.out.println(e.getCause());
		            }
				}
			});
		}
		
		//Wait Q/2+1 responses
		try {
			System.out.println("get: awaiting for quorum...");
			semaphore.await();
			System.out.println("get: ...quorum reached! Moving on");
		} catch (InterruptedException e) {e.printStackTrace();}
		for(ValTagPair vtp : vtList) {
			maxValTagPair = compareTags(maxValTagPair, vtp); // if the value stored in that station is greater than the maxVal, then that <val,tag> is the new <maxVal, tag>.
		}		
		
		return maxValTagPair;			  //returns null if there is no user registered with that email
	}
	
	
	
	/** writes <val, new maxTag> in the X station replicas  */
	private synchronized void setBalance(String email, int balance, Collection<StationClient> stationClients, String maxTag) {

		System.out.println("\tstarting set Balance operation:");
		
		String newtag = updateTag(maxTag);
		
		int quorum = (stationClients.size()/2)+1;
		CountDownLatch semaphore = new CountDownLatch(quorum);
		
		for(StationClient sc : stationClients) {
			
			sc.setBalanceAsync(email, balance, newtag, new AsyncHandler<SetBalanceResponse>() {
				
				@Override
				public void handleResponse(Response<SetBalanceResponse> res) {
					try {
						res.get();
						semaphore.countDown();
					} catch (InterruptedException e) {
		                   System.out.println("Caught interrupted exception.");
//		                   System.out.print("Cause: ");
//		                   System.out.println(e.getCause());
		            } catch (ExecutionException e) {
		                   System.out.println("Caught execution exception.");
//		                   System.out.print("Cause: ");
//		                   System.out.println(e.getCause());
		            }
				}
			});
		}
		
		//Wait Q/2+1 acknowledges
		try {
			System.out.println("set: awaiting for quorum...");
			semaphore.await();
			System.out.println("set: ...quorum reached! Moving on");
		} catch (InterruptedException e) {e.printStackTrace();}
		
		System.out.println("\tset Balance operation completed!");
	}
	
	
	/*
	 * /** writes a new <val, maxTag> in the X replicas for already existing users 
	private void setBalance(String email, int balance, Collection<StationClient> stationClients) {
		ValTagPair maxValTagPair = getBalance(email, stationClients);
		setBalance(email, balance, stationClients, maxValTagPair.getTag());
	}*/
	
	
	/** updates a tag value by incrementing the seq part of that tag */
	private String updateTag(String tag) {
		String seq = tag.split(":")[0];		//selects the seq part of the given tag
		int seqNum = Integer.valueOf(seq);
		
		return ++seqNum + ":"+ tag.split(":")[1];
	}
	
	/**returns the <val,tag> corresponding to the maxTag between two tags */
	private static ValTagPair compareTags(ValTagPair valTag1, ValTagPair valTag2) {
		if(valTag1 == null)
			return valTag2;
		if(valTag2 == null)
			return valTag1;						//if any of the <val,tag> is null, it returns the other one
		
		String tag1 = valTag1.getTag();			
		String tag2 = valTag2.getTag();			// keep tags only
		
		String[] tag1parts = tag1.split(":");  // separate seq from cid part in tags
		int seq1 = Integer.parseInt(tag1parts[0]);
		String cid1 = tag1parts[1];
		
		String[] tag2parts = tag2.split(":");
		int seq2 = Integer.parseInt(tag2parts[0]);
		String cid2 = tag2parts[1];
		
		if(seq1 == seq2) {
			System.out.println("("+seq1+" == "+seq2+"), returning biggest cid");
			return (cid1.compareTo(cid2) > 0 ? valTag1 : valTag2); //if seqs are equal, compare cid part of tags
		}
		ValTagPair answer = seq1 > seq2 ? valTag1 : valTag2;
		System.out.println(seq1+">"+seq2+"?"+answer.getBalance()+" "+answer.getTag());
		return (answer);  // returns the valTag pair corresponding to maxTag
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

	/** auxiliary method for listStations: orders k stations by distance value
	 * from from smallest to largest*/
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
		setinitVal(10);

	}

	//Auxiliary Methods -----------------------------------------------------------------------------
	/** Check Email validity */
	private void checkEmail(String email) throws InvalidEmailException{

		if(email == null || email.equals("")){
			throw new InvalidEmailException("Email is invalid");
		}

		if( !email.matches("[a-zA-Z0-9]+(\\.[a-zA-Z0-9]+)?@[a-zA-Z0-9]+(\\.[a-zA-Z0-9]+)?")){
			throw new InvalidEmailException("Email is invalid");
		}
	}

	/** auxiliary method for listStations: checks arguments validity*/
	private boolean checkArguments(Integer numberOfStations, CoordinatesView coordinates) {

		int x = coordinates.getX();
		int y = coordinates.getY();

		return ((0 <= x && x <= 99) && (0<= y && y <=99) && numberOfStations > 0);
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

		setinitVal(userInitialPoints);
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
