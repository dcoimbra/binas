package org.binas.station.ws;

import javax.jws.WebService;

import org.binas.station.domain.Coordinates;
import org.binas.station.domain.Station;
import org.binas.station.domain.exception.BadInitException;
import org.binas.station.domain.exception.NoBinaAvailException;
import org.binas.station.domain.exception.NoSlotAvailException;

/**
 * This class implements the Web Service port type (interface). The annotations
 * below "map" the Java class to the WSDL definitions.
 */

 @WebService(
 endpointInterface = "org.binas.station.ws.StationPortType",
 wsdlLocation = "station.2_0.wsdl",
 name ="StationWebService",
 portName = "StationPort",
 targetNamespace="http://ws.station.binas.org/",
 serviceName = "StationService"
 )
public class StationPortImpl implements StationPortType {

	/**
	 * The Endpoint manager controls the Web Service instance during its whole
	 * lifecycle.
	 */
	private StationEndpointManager endpointManager;

	/** Constructor receives a reference to the endpoint manager. */
	public StationPortImpl(StationEndpointManager endpointManager) {
		this.endpointManager = endpointManager;
	}

	// Main operations -------------------------------------------------------

	 /** Retrieve information about station. */
	 @Override
	 public StationView getInfo() {
	 
		 return buildStationView(Station.getInstance());
	 }
	
	 /** Return a bike to the station. */
	 @Override
	 public int returnBina() throws NoSlotAvail_Exception {
		 try {
			 return Station.getInstance().returnBina();
			 
		 } catch(NoSlotAvailException e) {
			 throwNoSlotAvail(e.getMessage());
		 }
		 return -1;
	 }
	
	 /** Take a bike from the station. */
	 @Override
	 public void getBina() throws NoBinaAvail_Exception {
		 try {
			Station.getInstance().getBina();
			
		} catch (NoBinaAvailException e) {
			throwNoBinaAvail(e.getMessage());
		}
	 }

	// Test Control operations -----------------------------------------------

	 /** Diagnostic operation to check if service is running. */
	 @Override
	 public String testPing(String inputMessage) {
		 // If no input is received, return a default name.
		 if (inputMessage == null || inputMessage.trim().length() == 0)
			 inputMessage = "friend";
		
		 // If the station does not have a name, return a default.
		 String wsName = endpointManager.getWsName();
		 if (wsName == null || wsName.trim().length() == 0)
			 wsName = "Station";
		
		 // Build a string with a message to return.
		 StringBuilder builder = new StringBuilder();
		 builder.append("Hello ").append(inputMessage);
		 builder.append(" from ").append(wsName);
		 return builder.toString();
	 }
	
	 /** Return all station variables to default values. */
	 @Override
	 public void testClear() {
		 Station.getInstance().reset();
	 }
	
	 /** Set station variables with specific values. */
	 @Override
	 public void testInit(int x, int y, int capacity, int returnPrize) throws BadInit_Exception {
		 try {
			 Station.getInstance().init(x, y, capacity, returnPrize);
		 } catch (BadInitException e) {
			 throwBadInit("Invalid initialization values!");
		 }
	 }

	 /** Get a user's balance. */ 
	@Override
	public synchronized ValTagPair getBalance(String email) {

		Station station = Station.getInstance();
		return station.getValTagPair(email);

	}

	/** Set a user's balance. */
	@Override
	public synchronized void setBalance(String email, int balance, String tag) {

		if(balance < 0){
			return;
		}

		Station station = Station.getInstance();
		System.out.println("setting balance of "+balance+" for user "+email+" with tag "+tag);
			ValTagPair receivedValTagPair = buildValTagPair(balance, tag);
			ValTagPair valTag = station.getValTagPair(email);
			if(valTag == null) {												//verifica se o par <valor, tag> ja' existe para aquele email
				station.addValTagPair(email, buildValTagPair(balance, tag));	//se nao existir, cria um novo
			} else {
				ValTagPair newValTag = compareTags(valTag, receivedValTagPair);
				valTag.setBalance(newValTag.getBalance());
				valTag.setTag(newValTag.getTag());
			}
	}
		
	// View helpers ----------------------------------------------------------

	 /** Helper to convert a domain station to a view. */
	 private StationView buildStationView(Station station) {
		 StationView view = new StationView();
		 view.setId(station.getId());
		 view.setCoordinate(buildCoordinatesView(station.getCoordinates()));
		 view.setCapacity(station.getMaxCapacity());
		 view.setTotalGets(station.getTotalGets());
		 view.setTotalReturns(station.getTotalReturns());
		 view.setFreeDocks(station.getFreeDocks());
		 view.setAvailableBinas(station.getAvailableBinas());
		 return view;
	 }
	
	 /** Helper to convert a domain coordinates to a view. */
	 private CoordinatesView buildCoordinatesView(Coordinates coordinates) {
		 CoordinatesView view = new CoordinatesView();
		 view.setX(coordinates.getX());
		 view.setY(coordinates.getY());
		 return view;
	 }
	 
		private ValTagPair buildValTagPair(int balance, String tag) {
			ValTagPair view = new ValTagPair();
			view.setBalance(balance);
			view.setTag(tag);
		return view;
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
				System.out.println("sequences are equal, returning biggest cid");
				return (cid1.compareTo(cid2) > 0 ? valTag1 : valTag2); //if seqs are equal, compare cid part of tags
			}
			ValTagPair answer = seq1 > seq2 ? valTag1 : valTag2;
			System.out.println(seq1+">"+seq2+"?"+answer.getBalance()+" "+answer.getTag());
			return (answer);  // returns the valTag pair corresponding to maxTag
		}
	// Exception helpers -----------------------------------------------------

	 /** Helper to throw a new NoBinaAvail exception. */
	 private void throwNoBinaAvail(final String message) throws NoBinaAvail_Exception {
		 NoBinaAvail faultInfo = new NoBinaAvail();
		 faultInfo.message = message;
		 throw new NoBinaAvail_Exception(message, faultInfo);
	 }
	
	 /** Helper to throw a new NoSlotAvail exception. */
	 private void throwNoSlotAvail(final String message) throws NoSlotAvail_Exception {
		 NoSlotAvail faultInfo = new NoSlotAvail();
		 faultInfo.message = message;
		 throw new NoSlotAvail_Exception(message, faultInfo);
	 }
	
	 /** Helper to throw a new BadInit exception. */
	 private void throwBadInit(final String message) throws BadInit_Exception {
		 BadInit faultInfo = new BadInit();
		 faultInfo.message = message;
		 throw new BadInit_Exception(message, faultInfo);
	 }

}
