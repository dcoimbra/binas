package org.binas.ws;


public class BinasApp {

	public static void main(String[] args) throws Exception {
		


		if (args.length < 3) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + BinasApp.class.getName() + "wsName uddiURL");
			return;
		}

		String wsName = args[1];	//wsname
		String wsURL = args[2];
		String uddiURL = args[0];	//endereco uddi

		BinasEndpointManager endpoint = new BinasEndpointManager(uddiURL, wsName, wsURL);
		

		System.out.println(BinasApp.class.getSimpleName() + " running");

		try {

			endpoint.start();

			endpoint.awaitConnections();

			endpoint.stop();

		} catch (Exception e) {
			System.out.printf("Caught exception: %s%n", e);
			e.printStackTrace();

		}
	}

}