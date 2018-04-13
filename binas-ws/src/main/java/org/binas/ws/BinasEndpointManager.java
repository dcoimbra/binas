package org.binas.ws;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.Endpoint;

import org.binas.domain.exception.InvalidStationException;
import org.binas.station.ws.cli.StationClient;
import org.binas.station.ws.cli.StationClientException;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;

public class BinasEndpointManager {

	/** UDDI naming server location */
	private String uddiURL = null;
	/** Web Service name */
	private String wsName = null;

	/** Get UDDI URL */
	public String getUddiURL() {
		return uddiURL;
	}
	
	/** Get Web Service UDDI publication name */
	public String getWsName() {
		return wsName;
	}

	/** Web Service location to publish */
	private String wsURL = null;
	
	// /** Obtain WebServer URL */
	public String getWsURL() {
		return wsURL;
	}

	/** Port implementation */
	private BinasPortImpl portImpl = new BinasPortImpl(this);

	// /** Obtain Port implementation */
	public BinasPortType getPort() {
		return portImpl;
	}

	/** Web Service end point */
	private Endpoint endpoint = null;

	// /** UDDI Naming instance for contacting UDDI server */
	 private UDDINaming uddiNaming = null;

	// /** Get UDDI Naming instance for contacting UDDI server */
	UDDINaming getUddiNaming() {
	  return uddiNaming;
	}

	/** output option */
	private boolean verbose = true;

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/** constructor with provided UDDI location, WS name, and WS URL */
	public BinasEndpointManager(String uddiURL, String wsName, String wsURL) {
		this.uddiURL = uddiURL;
		this.wsName = wsName;
		this.wsURL = wsURL;
	}
	
	
	/* end point management */

	public void start() throws Exception {
		try {
			// publish end point
			endpoint = Endpoint.create(this.portImpl);
			if (verbose) {
				System.out.printf("Starting %s%n", wsURL);
			}
			endpoint.publish(wsURL);
		} catch (Exception e) {
			endpoint = null;
			if (verbose) {
				System.out.printf("Caught exception when starting: %s%n", e);
				e.printStackTrace();
			}
			throw e;
		}
		publishToUDDI();
	}

	public void awaitConnections() {
		if (verbose) {
			System.out.println("Awaiting connections");
			System.out.println("Press enter to shutdown");
		}
		try {
			System.in.read();
		} catch (IOException e) {
			if (verbose) {
				System.out.printf("Caught i/o exception when awaiting requests: %s%n", e);
			}
		}
	}

	public void stop() throws Exception {
		try {
			if (endpoint != null) {
				// stop end point
				endpoint.stop();
				if (verbose) {
					System.out.printf("Stopped %s%n", wsURL);
				}
			}
		} catch (Exception e) {
			if (verbose) {
				System.out.printf("Caught exception when stopping: %s%n", e);
			}
		}
		this.portImpl = null;
		unpublishFromUDDI();
	}

	/* UDDI */

	private void publishToUDDI() throws Exception {
		System.out.printf("Publishing '%s' to UDDI at %s%n", wsName, uddiURL);
		uddiNaming = new UDDINaming(uddiURL);
		uddiNaming.rebind(wsName, wsURL);
	}

	private void unpublishFromUDDI() {
		try {
			if (uddiNaming != null) {
				// delete from UDDI
				uddiNaming.unbind(wsName);
				System.out.printf("Deleted '%s' from UDDI%n", wsName);
			}
		} catch (Exception e) {
			System.out.printf("Caught exception when deleting: %s%n", e);
		}

	}
	

	/** Returns Map of StationClients and their IDs as key */
	private static Map<String, StationClient> stationClients = null;
    public Map<String, StationClient> getStationClients() {
    	try {
    		if(stationClients == null) {
    			stationClients = new HashMap<>();
    			System.out.printf("Looking for '%s'%n", wsName, "T07_Station%");
				Collection<UDDIRecord> endpointAddress = uddiNaming.listRecords("T07_Station%");
				System.out.println("listed records\n");
				for(UDDIRecord r : endpointAddress) {
						System.out.printf("Found %s%n", r.toString());
					try {
						StationClient sc = new StationClient(r.getUrl());
						stationClients.put(sc.getInfo().getId(), sc);
					} catch (StationClientException e) {e.printStackTrace(); System.out.println(e.getMessage());}
				}
    		}
		}catch (UDDINamingException e) {System.out.printf("UDDINamingException\n");}
    	
    	return stationClients;
    }
    
    /** returns a StationClient entity given it's station ID*/
    public StationClient getStationClientById(String stationId) throws InvalidStationException {
    	StationClient sc = getStationClients().get(stationId);
		if(sc == null)
			throw new InvalidStationException("Station doesn't exist");
		
		return sc;
    }
    
    /** resets all station entities to default values */
    public void testClearStationClients() {
    	for(StationClient sc : getStationClients().values()) {
    		sc.testClear();
    	}
    }
}
