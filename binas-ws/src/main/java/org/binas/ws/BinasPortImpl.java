package org.binas.ws;

import org.binas.domain.BinasManager;
import org.binas.domain.BinasUser;
import org.binas.domain.exception.UserNotExistsException;

import javax.jws.WebService;
import java.util.*;

@WebService(
endpointInterface = "org.binas.ws.BinasPortType",
wsdlLocation = "binas.1_0.wsdl",
name ="BinasWebService",
portName = "BinasPort",
targetNamespace="http://ws.binas.org/",
serviceName = "BinasService"
)
public class BinasPortImpl implements BinasPortType {

    @Override
    public List<StationView> listStations(Integer numberOfStations, CoordinatesView coordinates){

    	Map<Double, StationView> distances = new TreeMap<>();
    	HashMap<String, StationView> map = BinasManager.getInstance().getStations();
    	List<StationView> stationViews = new ArrayList<>();
		Integer x1 = coordinates.getX();
		Integer y1 = coordinates.getY();

		for (StationView station : map.values()) {

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

	@Override
	public StationView getInfoStation(String stationId) throws InvalidStation_Exception {
		try {
			HashMap<String, StationView> stations = BinasManager.getInstance().getStations();

			StationView view = stations.get(stationId);

			if(view == null){
				InvalidStation faultInfo = new InvalidStation();
				throw new InvalidStation_Exception("Station is valid", faultInfo);
			}

			return view;

		}catch(InvalidStation_Exception e){
			throwInvalidStation(e.getMessage());
			return null;
		}
	}


	@Override
    public int getCredit(String email) throws UserNotExists_Exception{
    	try {
    		return BinasManager.getInstance().getCredit(email);
    	} catch (UserNotExistsException e) {
    		throwUserNotExists(e.getMessage());
    		return 0;
    	}
    }


	@Override
    public UserView activateUser(String email) throws EmailExists_Exception, InvalidEmail_Exception{
		try {

			if(!BinasUser.getEmails().add(email)){
				EmailExists faultInfo = new EmailExists();
				throw new EmailExists_Exception("Email already exists", faultInfo);
			}
			else if(email.equals("")){
				InvalidEmail faultInfo = new InvalidEmail();
				throw new InvalidEmail_Exception("Email is invalid", faultInfo);
			}

			BinasUser user = new BinasUser(email, "hm");

			return buildUserView(user);

		}catch(EmailExists_Exception e){
			throwEmailExists(e.getMessage());
			return null;
		}catch(InvalidEmail_Exception e){
			throwInvalidEmail(e.getMessage());
			return null;
		}
    }

    @Override
    public void rentBina(String stationId, String email) throws AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception, UserNotExists_Exception{
//    	try{
//    		BinasManager.getInstance().rentBina(stationId, email);
//    	} catch (UserNotExistsException e) {
//			throwUserNotExists(e.getMessage());
//		} catch (NoCreditException e) {
//			throwNoCredit(e.getMessage());
//		} catch (AlreadyHasBinaException e) {
//			throwAlreadyHasBina(e.getMessage());
//		}
    }

    @Override
    public void returnBina(String stationId, String email) throws FullStation_Exception, InvalidStation_Exception, NoBinaRented_Exception, UserNotExists_Exception{
    	//TODO
    }

    @Override
    public String testPing(String inputMessage){
    	//TODO
        return "";
    }

    @Override
    public void testClear(){
    	//TODO
    }

    @Override
    public void testInitStation(String stationId, int x, int y, int capacity, int returnPrize) throws BadInit_Exception{
    	//TODO
    }

    @Override
    public void testInit(int userInitialPoints) throws BadInit_Exception{
    	//TODO
    }

    // Exceptions Helpers ---------------------------------------------------------------------------
    
	 /** Helper to throw a new BadInit exception. */
	 private void throwBadInit(final String message) throws BadInit_Exception {
		 BadInit faultInfo = new BadInit();
		 faultInfo.message = message;
		 throw new BadInit_Exception(message, faultInfo);
	 }
	 
	 /** Helper to throw a new NoBinaAvail exception. */
	 private void throwNoBinaAvail(final String message) throws NoBinaAvail_Exception {
		 NoBinaAvail faultInfo = new NoBinaAvail();
		 faultInfo.message = message;
		 throw new NoBinaAvail_Exception(message, faultInfo);
	 }
	 
	 /** Helper to throw a new NoBinaAvail exception. */
	 private void throwNoCredit(final String message) throws NoCredit_Exception {
		 NoCredit faultInfo = new NoCredit();
		 faultInfo.message = message;
		 throw new NoCredit_Exception(message, faultInfo);
	 }
	 
	 /** Helper to throw a new NoBinaAvail exception. */
	 private void throwAlreadyHasBina(final String message) throws AlreadyHasBina_Exception {
		 AlreadyHasBina faultInfo = new AlreadyHasBina();
		 faultInfo.message = message;
		 throw new AlreadyHasBina_Exception(message, faultInfo);
	 }
	 
	 /** Helper to throw a new NoBinaAvail exception. */
	 private void throwUserNotExists(final String message) throws UserNotExists_Exception {
		 UserNotExists faultInfo = new UserNotExists();
		 faultInfo.message = message;
		 throw new UserNotExists_Exception(message, faultInfo);
	 }

	 /** Helper to throw a new UserNotExists exception. */
	 private void throwInvalidStation(final String message) throws InvalidStation_Exception {
		InvalidStation faultInfo = new InvalidStation();
		faultInfo.message = message;
		throw new InvalidStation_Exception(message, faultInfo);
	 }

	/** Helper to throw a new InvalidEmail exception. */
	private void throwInvalidEmail(final String message) throws InvalidEmail_Exception {
		InvalidEmail faultInfo = new InvalidEmail();
		faultInfo.message = message;
		throw new InvalidEmail_Exception(message, faultInfo);
	 }

	/** Helper to throw a new UserEmailExists exception. */
	private void throwEmailExists(final String message) throws EmailExists_Exception {
		EmailExists faultInfo = new EmailExists();
		faultInfo.message = message;
		throw new EmailExists_Exception(message, faultInfo);
	}

//    FullStation_Exception
//    NoBinaRented_Exception

	//View Helpers
	private UserView buildUserView(BinasUser user) {
		UserView view = new UserView();
		view.setEmail(user.getEmail());
		view.setHasBina(user.isWithBina());
		view.setCredit(user.getCredit());

		return view;
	}
}
