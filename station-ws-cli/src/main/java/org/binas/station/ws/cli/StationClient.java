package org.binas.station.ws.cli;

import org.binas.station.ws.*;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Response;
import java.util.Map;
import java.util.concurrent.Future;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

/**
 * Client port wrapper.
 *
 * Adds easier end point address configuration to the Port generated by
 * wsimport.
 */
public class StationClient implements StationPortType {

	/** WS service */
	StationService service = null;

	/** WS port (port type is the interface, port is the implementation) */
	StationPortType port = null;

	/** UDDI server URL */
	private String uddiURL = null;

	/** WS name */
	private String wsName = null;

	/** WS end point address */
	private String wsURL = null; // default value is defined inside WSDL

	public String getWsURL() {
		return wsURL;
	}

	/** output option **/
	private boolean verbose = false;

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/** constructor with provided web service URL */
	public StationClient(String wsURL) throws StationClientException {
		this.wsURL = wsURL;
		createStub();
	}

	/** constructor with provided UDDI location and name */
	public StationClient(String uddiURL, String wsName) throws StationClientException {
		this.uddiURL = uddiURL;
		this.wsName = wsName;
		uddiLookup();
		createStub();
	}

	/** UDDI lookup */
	private void uddiLookup() throws StationClientException {
		try {
			System.out.printf("Contacting UDDI at %s%n", uddiURL);
			UDDINaming uddiNaming = new UDDINaming(uddiURL);

			System.out.printf("Looking for '%s'%n", wsName);
			this.wsURL = uddiNaming.lookup(wsName);
			
			if (wsURL == null) {
				System.out.println("Not found!");
				throw new StationClientException();
			} else {
				System.out.printf("Found %s%n", wsURL);
			}
		}catch(UDDINamingException e){
			System.out.printf("UDDINamingException");

		}

	}


	/** Stub creation and configuration */
	private void createStub() {
		 if (verbose)
			 System.out.println("Creating stub ...");

		 this.service = new StationService();
		 port = service.getStationPort();
		
		 if (wsURL != null) {
			 if (verbose)
				 System.out.println("Setting endpoint address ...");
			 BindingProvider bindingProvider = (BindingProvider) port;
			 Map<String, Object> requestContext = bindingProvider.getRequestContext();
			 requestContext.put(ENDPOINT_ADDRESS_PROPERTY, this.wsURL);
		 }
	}

	// remote invocation methods ----------------------------------------------

	 @Override
	 public StationView getInfo() {
		 return port.getInfo();
	 }

	@Override
	public Response<GetBinaResponse> getBinaAsync() {
		return port.getBinaAsync();
	}

	@Override
	public Future<?> getBinaAsync(AsyncHandler<GetBinaResponse> asyncHandler) {
		return port.getBinaAsync(asyncHandler);
	}

	@Override
	 public void getBina() throws NoBinaAvail_Exception {
		 port.getBina();
	 }

	@Override
	public Response<ReturnBinaResponse> returnBinaAsync() {
		return port.returnBinaAsync();
	}

	@Override
	public Future<?> returnBinaAsync(AsyncHandler<ReturnBinaResponse> asyncHandler) {
		return port.returnBinaAsync(asyncHandler);
	}

	@Override
	 public int returnBina() throws NoSlotAvail_Exception {
		 return port.returnBina();
	 }

	@Override
	public Response<TestPingResponse> testPingAsync(String inputMessage) {
		return port.testPingAsync(inputMessage);
	}

	@Override
	public Future<?> testPingAsync(String inputMessage, AsyncHandler<TestPingResponse> asyncHandler) {
		return port.testPingAsync(inputMessage, asyncHandler);
	}


	// test control operations ------------------------------------------------

	 @Override
	 public String testPing(String inputMessage) {
		 return port.testPing(inputMessage);
	 }

	@Override
	public Response<TestClearResponse> testClearAsync() {
		return port.testClearAsync();
	}

	@Override
	public Future<?> testClearAsync(AsyncHandler<TestClearResponse> asyncHandler) {
		return port.testClearAsync(asyncHandler);
	}

	@Override
	 public void testClear() {
		 port.testClear();
	 }

	@Override
	public Response<TestInitResponse> testInitAsync(int x, int y, int capacity, int returnPrize) {
		return port.testInitAsync(x, y, capacity, returnPrize);
	}

	@Override
	public Future<?> testInitAsync(int x, int y, int capacity, int returnPrize, AsyncHandler<TestInitResponse> asyncHandler) {
		return port.testInitAsync(x, y, capacity, returnPrize, asyncHandler);
	}

	@Override
	 public void testInit(int x, int y, int capacity, int returnPrize) throws
	 BadInit_Exception {
		 port.testInit(x, y, capacity, returnPrize);
	 }

	@Override
	public Response<GetBalanceResponse> getBalanceAsync(String email) {
		return port.getBalanceAsync(email);
	}

	@Override
	public Future<?> getBalanceAsync(String email, AsyncHandler<GetBalanceResponse> asyncHandler) {
		return port.getBalanceAsync(email, asyncHandler);
	}

	@Override
	public ValTagPair getBalance (String email) {
		return port.getBalance(email);
	}

	@Override
	public Response<SetBalanceResponse> setBalanceAsync(String email, int balance, String tag) {
		return port.setBalanceAsync(email, balance, tag);
	}

	@Override
	public Future<?> setBalanceAsync(String email, int balance, String tag, AsyncHandler<SetBalanceResponse> asyncHandler) {
		return port.setBalanceAsync(email, balance, tag, asyncHandler);
	}

	@Override
	public void setBalance(String email, int balance, String tag) {
		port.setBalance(email, balance, tag);
		
	}

	@Override
	public Response<GetInfoResponse> getInfoAsync() {
		return port.getInfoAsync();
	}

	@Override
	public Future<?> getInfoAsync(AsyncHandler<GetInfoResponse> asyncHandler) {
		return port.getInfoAsync(asyncHandler);
	}

}
