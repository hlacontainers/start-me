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
import hla.rti1516e.*;
import hla.rti1516e.exceptions.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

class Federate {

	protected String federationName = null;
	protected FederationDisplay display;
	protected RTIambassador rtiamb;
	protected FederateAmbassador fedamb;

	// object class and attribute handles
	protected AttributeHandleSet attributes;
	protected ObjectClassHandle federateHandle;
	protected AttributeHandle federateNameHandle;
	protected volatile boolean isClosing;
	protected volatile boolean isShuttingDown;

	// all federates
	protected HashMap<Integer, String> federates;

	Federate() throws RTIinternalError {
		this.isClosing = false;
		this.isShuttingDown = false;
		this.display = null;
		this.rtiamb = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
		this.fedamb = new FederateAmbassador(this);
		this.federates = new HashMap();
	}

	void connect(String settingsDesignator) throws RTIinternalError, InvalidLocalSettingsDesignator, UnsupportedCallbackModel, AlreadyConnected, CallNotAllowedFromWithinCallback, InterruptedException, ConnectionFailed {
		int ATTEMPTS = 10;
		for (int i = 0; i < ATTEMPTS; i++) {
			try {
				if (settingsDesignator == null) {
					rtiamb.connect(fedamb, CallbackModel.HLA_EVOKED);
				} else {
					rtiamb.connect(fedamb, CallbackModel.HLA_EVOKED, settingsDesignator);
				}
				Logger.getLogger(Federate.class.getName()).log(Level.INFO, "Connected to RTI");
				return;
			} catch (ConnectionFailed ex) {
				Thread.sleep(1000);
			}
		}
		throw new ConnectionFailed("Failed to connect after " + ATTEMPTS + " attempts");
	}

	private URL[] getFOMs(String fomDir) throws MalformedURLException {
		// determine number of FOMs
		File folder = new File(fomDir);
		File[] listOfFiles = folder.listFiles();
		int fomCount = 0;
		for (File listOfFile : listOfFiles) {
			if (!listOfFile.isFile()) {
				continue;
			}
			fomCount++;
		}

		Logger.getLogger(Federate.class.getName()).log(Level.INFO, "FOM: found {0} FOMs", fomCount);

		// create array to store them
		URL[] modules = new URL[fomCount];

		// now get them
		int fomNr = 0;
		for (File listOfFile : listOfFiles) {
			if (!listOfFile.isFile()) {
				continue;
			}
			URL fom = listOfFile.toURI().toURL();
			modules[fomNr++] = fom;
			Logger.getLogger(Federate.class.getName()).log(Level.INFO, "FOM[{0}]: {1}", new Object[]{fomNr, fom});
		}

		return modules;
	}

	void join(String federateType, String federationName, String fomDir) throws RTIexception, MalformedURLException {
		// Get the FOMs
		URL[] modules = getFOMs(fomDir);

		// Attempt to create a new federation
		try {
			rtiamb.createFederationExecution(federationName, modules);
			rtiamb.joinFederationExecution(federateType, federationName);
			Logger.getLogger(Federate.class.getName()).log(Level.INFO, "Created and joined federation");
		} catch (FederationExecutionAlreadyExists ex) {
			rtiamb.joinFederationExecution(federateType, federationName, modules);
			Logger.getLogger(Federate.class.getName()).log(Level.INFO, "Joined existing federation");
		}

		this.federationName = federationName;
	}

	void publishAndSubscribe() throws RTIexception {
		// get all the handle information for the attributes
		federateHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.HLAmanager.HLAfederate");
		federateNameHandle = rtiamb.getAttributeHandle(federateHandle, "HLAfederateName");

		// package the information into a handle set
		attributes = rtiamb.getAttributeHandleSetFactory().create();
		attributes.add(federateNameHandle);

		rtiamb.subscribeObjectClassAttributes(federateHandle, attributes);

		Logger.getLogger(Federate.class.getName()).log(Level.INFO, "Published and Subscribed");
	}

	void start(FederationDisplay display) throws RTIexception {
		Logger.getLogger(Federate.class.getName()).log(Level.INFO, "Start ...");

		this.display = display;

		for (;;) {
			if (this.isClosing) {
				Logger.getLogger(Federate.class.getName()).log(Level.INFO, "Closing - stop simulation loop");
				break;
			} else if (this.isShuttingDown) {
				System.out.println("Shutting down - stop simulation loop");
				break;
			}
			rtiamb.evokeCallback(1);
		}
	}

	void resign() throws RTIexception {
		rtiamb.resignFederationExecution(ResignAction.DELETE_OBJECTS);
		Logger.getLogger(Federate.class.getName()).log(Level.INFO, "Resigned from Federation");

		try {
			rtiamb.destroyFederationExecution(this.federationName);
			Logger.getLogger(Federate.class.getName()).log(Level.INFO, "Destroyed Federation");
		} catch (FederationExecutionDoesNotExist ex) {
			Logger.getLogger(Federate.class.getName()).log(Level.INFO, "No need to destroy federation, it doesn't exist");
		} catch (FederatesCurrentlyJoined ex) {
			Logger.getLogger(Federate.class.getName()).log(Level.INFO, "Didn't destroy federation, federates still joined");
		}
	}

	void disconnect() throws RTIexception {
		rtiamb.disconnect();
		Logger.getLogger(Federate.class.getName()).log(Level.INFO, "Disconnected from Federation");
	}

	void addFederate(ObjectInstanceHandle theObject, String objectName) {
		federates.put(theObject.hashCode(), objectName);
		if (display != null) {
			display.addValue(objectName);
		}
		Logger.getLogger(Federate.class.getName()).log(Level.INFO, "Added Federate {0}", objectName);
	}

	void removeFederate(ObjectInstanceHandle theObject) {
		String objectName = federates.remove(theObject.hashCode());
		Logger.getLogger(Federate.class.getName()).log(Level.INFO, "Removed Federate {0}", objectName);
		if (display != null) {
			display.removeValue(objectName);
		}
	}

	void setIsClosing() {
		this.isClosing = true;
	}

	void setIsShuttingDown() {
		this.isShuttingDown = true;
	}
}
