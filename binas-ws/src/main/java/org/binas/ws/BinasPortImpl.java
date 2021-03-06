package org.binas.ws;

import java.util.List;

import javax.jws.HandlerChain;
import javax.jws.WebService;

import org.binas.domain.BinasManager;
import org.binas.domain.BinasUser;
import org.binas.domain.exception.*;

@WebService(
endpointInterface = "org.binas.ws.BinasPortType",
wsdlLocation = "binas.1_0.wsdl",
name ="BinasWebService",
portName = "BinasPort",
targetNamespace="http://ws.binas.org/",
serviceName = "BinasService"
)

@HandlerChain(file = "/binas-ws_handler-chain.xml")
public class BinasPortImpl implements BinasPortType {

	private BinasEndpointManager endpointManager;

    public BinasPortImpl(BinasEndpointManager binasEndpointManager) {
    	this.endpointManager = binasEndpointManager;
	}

    public String getBinasWsID() {
    	return endpointManager.getWsName();
    }
    
	@Override
	/** returns a list with the k closest stations ordered by distance
	 * with k being the number of stations to present
	 * @param numberOfStations to list
	 * @param coordinates
	 * @return list of stations */
    public List<StationView> listStations(Integer numberOfStations, CoordinatesView coordinates){
		
		return BinasManager.getInstance().listStations(numberOfStations, coordinates, endpointManager.getStationClients().values());
    }

	@Override
	/**returns a StationView object given a Station Client entity
	 * containing all the info on said station until this moment
	 * @param stationId to get info from
	 * @return view corresponding StationView
	 * @throws InvalidStation_Exception */
	public StationView getInfoStation(String stationId) throws InvalidStation_Exception {
		try {
			checkValidStationId(stationId);
			return BinasManager.getInstance().getInfoStation(endpointManager.getStationClientById(stationId));

		}catch(InvalidStationException e){
			throwInvalidStation(e.getMessage());
			return null;
		}
	}

	/** validates whether a Station_id has the expected format
	 *
 	 * @param stationId to check
	 * @throws InvalidStation_Exception
	 */

	private void checkValidStationId(String stationId) throws InvalidStation_Exception {
		 if(stationId == null || stationId.equals("")) {
			 throwInvalidStation("No station referred");
		 }
		 if(!stationId.matches("T07_Station[1-9]+")) {
			 throwInvalidStation("Illegal station name - must follow T07_Station$");
		 }
		
	}

	@Override
	/** returns the available credit of the user associated to the given email
	 * @param email of the user
	 * @throws UserNotExistsException */
    public int getCredit(String email) throws UserNotExists_Exception {

        try {

            return BinasManager.getInstance().getCredit(email, endpointManager.getStationClients().values());

        } catch (UserNotExistsException e) {
            throwUserNotExists(e.getMessage());
            return -1;
        } catch (InvalidEmailException e) {
            e.printStackTrace();
            return -1;
        }
    }


	@Override
	/** activates a binas' user by registering his e-mail address
	 * @param email of the user
	 * @return user activated
	 * @throws EmailExistsException
	 * @throws InvalidEmailException */
    public UserView activateUser(String email) throws EmailExists_Exception, InvalidEmail_Exception{
		try {

			BinasUser user = BinasManager.getInstance().activateUser(email, endpointManager.getStationClients().values());

			return buildUserView(user);

		}catch(EmailExistsException e){
			throwEmailExists(e.getMessage());
			return null;
		}catch(InvalidEmailException e){
			throwInvalidEmail(e.getMessage());
			return null;
		}
    }

    @Override
	/** rents a bicycle for the given user at a given station
	 * by asking said station to release one bicycle if conditions are met
	 * @param station to rent from
	 * @param email of the renting user user
	 * @throws NoCredit_Exception
	 * @throws AlreadyHasBina_Exception
	 * @throws UserNotExists_Exception
	 * @throws NoBinaAvail_Exception
	 * @throws InvalidStation_Exception
	 * @throws NoCredit_Exception */
    public void rentBina(String stationId, String email) throws AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception, UserNotExists_Exception{
    	try{
    		checkValidStationId(stationId);
    		BinasManager.getInstance().rentBina(endpointManager.getStationClientById(stationId), email, endpointManager.getStationClients().values());
    		
    	} catch (UserNotExistsException e) {
			throwUserNotExists(e.getMessage());
		} catch (NoCreditException e) {
			throwNoCredit(e.getMessage());
		} catch (AlreadyHasBinaException e) {
			throwAlreadyHasBina(e.getMessage());
		} catch (NoBinaAvailException e) {
			throwNoBinaAvail(e.getMessage());
		} catch (InvalidStationException e) {
			System.out.printf("Station %s%n not found. Moving on...", stationId);
		}
    }

    @Override
	/** returns a bicycle from the given user at a given station
	 * by asking said station to accept a bicycle if conditions are met
	 * @param station to return Bina
	 * @param email of the user returning a Bina
	 * @throws UserNotExistsException
	 * @throws NoBinaRentedException
	 * @throws FullStationException
	 * @throws InvalidStation_Exception */
    public void returnBina(String stationId, String email) throws FullStation_Exception, InvalidStation_Exception, NoBinaRented_Exception, UserNotExists_Exception {
    	try {
    		checkValidStationId(stationId);
			BinasManager.getInstance().returnBina(endpointManager.getStationClientById(stationId), email, endpointManager.getStationClients().values());
		} catch (UserNotExistsException e) {
			throwUserNotExists(e.getMessage());
		} catch (NoBinaRentedException e) {
			throwNoBinaRented(e.getMessage());
		} catch (FullStationException e) {
			throwFullStation(e.getMessage());
		} catch (InvalidStationException e) {
			System.out.printf("Station %s not found. Moving on...", stationId);
		}
    }

	/** Delete all users. */
    @Override
    public void testClear(){
		BinasManager.getInstance().reset();
		endpointManager.testClearStationClients();
    }
    
    
    // Test methods ---------------------------------------------------------------------------
    /** Test related methods */
    @Override
    public String testPing(String inputMessage){
    	return BinasManager.getInstance().testPing(inputMessage, endpointManager.getStationClients().values());
    }
    
    @Override
    public void testInitStation(String stationId, int x, int y, int capacity, int returnPrize) throws BadInit_Exception{
    	try {
    		BinasManager.getInstance().testInitStation(endpointManager.getStationClientById(stationId), x, y, capacity, returnPrize);
	    } catch (BadInitException e) {
        	throwBadInit(e.getMessage());
		} catch (InvalidStationException e) {
			System.out.printf("Station %s not found. Moving on...", stationId);
		}
    }

    @Override
    public void testInit(int userInitialPoints) throws BadInit_Exception {

		try {
			BinasManager.getInstance().testInit(userInitialPoints);
		}

		catch (BadInitException e) {
			throwBadInit(e.getMessage());
		}
	}

	// View Helpers ---------------------------------------------------------------------------
	private UserView buildUserView(BinasUser user) throws InvalidEmail_Exception {
		UserView view = new UserView();
		view.setEmail(user.getEmail());
		view.setHasBina(user.isWithBina());
		try {
			view.setCredit(BinasManager.getInstance().getCredit(user.getEmail(), endpointManager.getStationClients().values()));
		} catch (UserNotExistsException e) {
			e.printStackTrace();
		}catch(InvalidEmailException e){
			throwInvalidEmail(e.getMessage());
		}

		return view;
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
	 
	 /** Helper to throw a new NoCredit exception. */
	 private void throwNoCredit(final String message) throws NoCredit_Exception {
		 NoCredit faultInfo = new NoCredit();
		 faultInfo.message = message;
		 throw new NoCredit_Exception(message, faultInfo);
	 }
	 
	 /** Helper to throw a new AlreadyHasBina exception. */
	 private void throwAlreadyHasBina(final String message) throws AlreadyHasBina_Exception {
		 AlreadyHasBina faultInfo = new AlreadyHasBina();
		 faultInfo.message = message;
		 throw new AlreadyHasBina_Exception(message, faultInfo);
	 }
	 
	 /** Helper to throw a new UserNoExists exception. */
	 private void throwUserNotExists(final String message) throws UserNotExists_Exception {
		 UserNotExists faultInfo = new UserNotExists();
		 faultInfo.message = message;
		 throw new UserNotExists_Exception(message, faultInfo);
	 }

	 /** Helper to throw a new InvaliStation exception. */
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

	/** Helper to throw a new EmailExists exception. */
	private void throwEmailExists(final String message) throws EmailExists_Exception {
		EmailExists faultInfo = new EmailExists();
		faultInfo.message = message;
		throw new EmailExists_Exception(message, faultInfo);
	}

	 /** Helper to throw a new NoBinaRented exception. */
	 private void throwNoBinaRented(final String message) throws NoBinaRented_Exception {
		 NoBinaRented faultInfo = new NoBinaRented();
		 faultInfo.message = message;
		 throw new NoBinaRented_Exception(message, faultInfo);
	 }
	 
	 /** Helper to throw a new FullStation exception. */
	 private void throwFullStation(final String message) throws FullStation_Exception {
		 FullStation faultInfo = new FullStation();
		 faultInfo.message = message;
		 throw new FullStation_Exception(message, faultInfo);
	 }
	
}
