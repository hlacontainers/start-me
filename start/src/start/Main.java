/*
 * Copyright 2020 Tom van den Berg (TNO, The Netherlands).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package start;

/**
 *
 * @author Tom van den Berg (TNO, The Netherlands)
 */
import hla.rti1516e.exceptions.RTIexception;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

class Main {

	// the default federation name
	static private String federationName = "TheWorld";

	// create a (hopefully) unique federate type name for Portico
	static private String federateType = "Federate-" + new java.util.Date().getTime();
	
	// LRC settings designator
	static private String settingsDesignator = null;

	// set the X server DISPLAY
	static private String XdisplayName = null;

	// default directory where FOM are located
	static private String fomDir = "foms";

	// default master port number
	static private int listenPort = 0;

	// The federate
	static Federate fed = null;
	static Thread MainThread = null;
	static Thread ShutdownThread = null;

	public static void main(String[] args) throws Exception {
		// first set property before getting the logger
		System.setProperty("java.util.logging.config.file", "logging.properties");

		// Create a federate
		fed = new Federate();

		// register shutdown hook for JVM
		initShutdownHook();
		addShutdownHook();

		// Process environment settings
		processEnv();

		Logger.getLogger(Main.class.getName()).log(Level.INFO, "federationName  = {0}", federationName);
		Logger.getLogger(Main.class.getName()).log(Level.INFO, "fomDirectory    = {0}", fomDir);
		Logger.getLogger(Main.class.getName()).log(Level.INFO, "port            = {0}", listenPort);

		// Because Portico has a multi-threading issue we connect to the RTI before we create a display
		// Thus: connect, join, and publish/subscribe
		fed.connect(settingsDesignator);
		fed.join(federateType, federationName, fomDir);
		fed.publishAndSubscribe();

		// Open master port to signal we are ready
		if (listenPort != 0) {
			openPort(listenPort);
		}

		// Now create an instance of the display
		FederationDisplay display = null;
		if (XdisplayName != null) {
			try {
				display = new FederationDisplay(fed, federationName);
				display.setVisible(true);
			} catch (Throwable ex) {
				Logger.getLogger(Main.class.getName()).log(Level.INFO, ex.getMessage());
				Logger.getLogger(Main.class.getName()).log(Level.INFO, "DISPLAY problem for {0}, continue without", new Object[]{XdisplayName});
				display = null;
			}
		}

		// Start
		fed.start(display);

		// Clean up
		if (display != null) {
			display.dispose();
		}

		// ignore exceptions while cleaning up
		try {
			fed.resign();
		} catch (RTIexception ex) {
			Logger.getLogger(Main.class.getName()).log(Level.INFO, "resign exception: {0}", ex.getMessage());
		}

		try {
			fed.disconnect();
		} catch (RTIexception ex) {
			Logger.getLogger(Main.class.getName()).log(Level.INFO, "disconnect exception: {0}", ex.getMessage());
		}

		if (fed.isShuttingDown) {
			System.out.println("Main: Exit");
		} else {
			Logger.getLogger(Main.class.getName()).log(Level.INFO, "Exit");

			// remove the hook so that it doesn't get called on exit
			removeShutdownHook();
		}
	}

	/*
	** Open listenport to signal service readiness.
	*/
	static void openPort(int listenPort) {
		new Thread() {
			@Override
			public void run() {
				try {
					ServerSocket serverSocket = new ServerSocket(listenPort);
					Logger.getLogger(Main.class.getName()).log(Level.INFO, "Waiting for clients on port: {0}", listenPort);
					for (;;) {
						Socket socket = serverSocket.accept();
						InetSocketAddress addr = (InetSocketAddress) socket.getRemoteSocketAddress();
						Logger.getLogger(Main.class.getName()).log(Level.INFO, "Knock knock from: {0}", addr.getHostString());
					}
				} catch (IOException ex) {
					Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Exit");
				}
			}
		}.start();
	}

	static void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(ShutdownThread);
	}

	static void removeShutdownHook() {
		Runtime.getRuntime().removeShutdownHook(ShutdownThread);
	}

	static void initShutdownHook() {
		MainThread = Thread.currentThread();

		ShutdownThread = new Thread() {
			@Override
			public void run() {
				// use printlns because Logger is also being shutdown via its own shutdown hook

				System.out.println("Handler: Received shutdown signal");
				fed.setIsShuttingDown();
				System.out.println("Handler: Wait for federate to finish");
				try {
					MainThread.join(0);
				} catch (InterruptedException ex) {
					System.out.println("Handler: InterruptedException: " + ex.getMessage());
				}
				System.out.println("Handler: Exit");
			}
		};
	}

	/*
	** Process environment variables.
	 */
	static void processEnv() {		
		String value = System.getenv("FEDERATIONNAME");
		if (value != null && !value.isEmpty()) {
			federationName = value;
		}

		value = System.getenv("DISPLAY");
		if (value != null && !value.isEmpty()) {
			XdisplayName = value;
		}
		
		value = System.getenv("SETTINGS_DESIGNATOR");
		if (value != null && !value.isEmpty()) {
			settingsDesignator = value;
		}
		
		value = System.getenv("FOMDIR");
		if (value != null && !value.isEmpty()) {
			fomDir = value;
		}

		value = System.getenv("LISTENPORT");
		if (value != null && !value.isEmpty()) {
			listenPort = Integer.parseInt(value);
		}
	}
}
