package org.binas.ws.cli;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class BinasClientApp {

    public static void main(String[] args) throws Exception {
        // Check arguments
        if (args.length == 0) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: java " + BinasClientApp.class.getName()
                    + " wsURL OR uddiURL wsName");
            return;
        }
        String uddiURL = null;
        String wsName = null;
        String wsURL = null;
        if (args.length == 1) {
            wsURL = args[0];
        } else if (args.length >= 2) {
            uddiURL = args[0];
            wsName = args[1];
        }

		System.out.println(BinasClientApp.class.getSimpleName() + " running");

        // Create client
        BinasClient client = null;

        if (wsURL != null) {
            System.out.printf("Creating client for server at %s%n", wsURL);
            client = new BinasClient(wsURL);
        } else if (uddiURL != null) {
            System.out.printf("Creating client using UDDI at %s for server with name %s%n",
                uddiURL, wsName);
            client = new BinasClient(uddiURL, wsName);
        }

        // the following remote invocations are just basic examples
        // the actual tests are made using JUnit

//		System.out.println("Invoke ping()...");
//		String result = client.testPing("client");
//		System.out.print(result);
		 
		boolean run = true;
		String email = "sdis@tecnico.pt";
		String station1 = "T07_Station1";
		client.testInitStation(station1, 20, 20, 20, 2);
		
		while (run) {
			System.out.println("\n[A] - Activate User\n"
							 + "[B] - Rent Bina\n"
							 + "[C] - Get Credit\n"
							 + "[R] - Return Bina\n"
							 + "[X] - Clear All Data\n"
							 + "[Q] - Quit Demo");
			
			BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
	        String s = bufferRead.readLine();
			try{
				switch(s.toUpperCase()) {
					case "A":
						System.out.println("activating user "+ email);
						client.activateUser(email);
						break;
					
					case "B":
						System.out.println("renting bina for "+email+" at station "+station1);
						client.rentBina(station1, email);
						break;
						
					case "C":
						System.out.println("credit of user "+email+":"+ client.getCredit(email));
						break;
						
					case "R":
						System.out.println("returning bina for "+email+" at station "+station1);
						client.returnBina(station1, email);
						break;
						
					case "X":
						client.testClear();
						client.testInitStation(station1, 20, 20, 20, 2);
						break;
						
					case "Q":
						run = false;
						break;
						
					default:
						System.out.println("Unknown option: " +s);	
				}
			} catch (Exception e) {
				System.out.println("Error: " + e.getMessage());
			}
					
		}
        
	 }
}

