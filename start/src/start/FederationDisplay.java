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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;

class FederationDisplay extends JFrame {
	private final Federate federate;
	private final JPanel topPanel;
	private final JList listbox;
	private final DefaultListModel listModel;

	// Constructor of the display
	FederationDisplay(Federate federate, String federationName) {
		// the federate that this diplay belongs to
		this.federate = federate;
		
		// Set the frame characteristics
		setTitle(federationName);
		setSize(300, 300);
		setBackground(Color.gray);

		// Create a panel to hold all other components
		topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		getContentPane().add(topPanel);

		// Create a new listbox
		listModel = new DefaultListModel();
		listbox = new JList(listModel);
		topPanel.add(listbox, BorderLayout.CENTER);
	}

	void addValue(String name) {
		listModel.addElement(name);
		repaint();
	}

	void removeValue(String name) {
		listModel.removeElement(name);
		repaint();
	}
	
	@Override
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);

		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			Logger.getLogger(FederationDisplay.class.getName()).log(Level.INFO, "processWindowEvent=WINDOW_CLOSING ({0})", e.getID());

			// inform federate that windows is closing
			federate.setIsClosing();
		}
	}
}
