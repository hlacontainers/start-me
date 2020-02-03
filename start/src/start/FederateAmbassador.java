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
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.exceptions.FederateInternalError;

class FederateAmbassador extends NullFederateAmbassador {

	Federate federate;

	FederateAmbassador(Federate federate) {
		this.federate = federate;
	}

	@Override
	public void discoverObjectInstance(ObjectInstanceHandle theObject,
			ObjectClassHandle theObjectClass,
			String objectName)
			throws FederateInternalError {
		this.federate.addFederate(theObject, objectName);
	}

	@Override
	public void removeObjectInstance(ObjectInstanceHandle theObject,
			byte[] tag,
			OrderType sentOrdering,
			SupplementalRemoveInfo removeInfo)
			throws FederateInternalError {
		this.federate.removeFederate(theObject);
	}
}
