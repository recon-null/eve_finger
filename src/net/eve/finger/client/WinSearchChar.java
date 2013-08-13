// This file is part of Finger for EVE.
//
// Finger for EVE is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// Finger for EVE is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.

// You should have received a copy of the GNU Affero General Public License
// along with Finger for EVE.  If not, see <http://www.gnu.org/licenses/>.

package net.eve.finger.client;

import net.eve.finger.shared.CharInfo;

import com.extjs.gxt.desktop.client.Desktop;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.google.gwt.event.dom.client.KeyCodes;

/**
 * Search form for characters.
 * Requires 'fingersvc', 'desktop', 'sessionid' 
 * to be set in the GXT registry.
 */
public class WinSearchChar extends Window 
{
	private FormPanel frmSearch;
	private TextField<String> txtCharName;
	private Button btnSearch;
	
	/**
	 * Access to the server.
	 */
	private static FingerServiceAsync fingerSvc;	
	
	/**
	 * Stores the desktop we add windows to.
	 */
	private static Desktop desk;
	
	/**
	 * Used to auth with the server.
	 */
	private static String sessionID;

		
	static
	{		
		// Get the desktop
		desk = (Desktop)Registry.get("desktop");
		
		// Get the session ID
		sessionID = (String)Registry.get("sessionid");
		
		// Get the RPC service
		fingerSvc = (FingerServiceAsync)Registry.get("fingersvc");		
	}
	
	/**
	 * Default constructor.
	 * @wbp.parser.constructor
	 */
	public WinSearchChar() 
	{		
		// Build the UI
		createContents();
		
		// Make sure the search field has focus
		txtCharName.focus();
	}
	
	/**
	 * Builds the UI
	 */
	private void createContents()
	{
        setSize("254", "100");
		setMinimizable(true);
		setHeading("Character Search");
		setLayout(new RowLayout(Orientation.VERTICAL));
		
		this.frmSearch = new FormPanel();
		this.frmSearch.setBodyBorder(false);
		this.frmSearch.setLabelWidth(35);
		this.frmSearch.setHeaderVisible(false);
		this.frmSearch.setHeading("New FormPanel");
		
		this.txtCharName = new TextField<String>();
		this.txtCharName.setSelectOnFocus(true);
		this.txtCharName.addListener(Events.KeyPress, new Listener<FieldEvent>() {
			public void handleEvent(FieldEvent e) 
			{
				if (e.getKeyCode() == KeyCodes.KEY_ENTER)
				{
					executeSearch();					
				}
			}
		});
		this.txtCharName.setTabIndex(1);
		this.frmSearch.add(this.txtCharName, new FormData("100%"));
		this.txtCharName.setFieldLabel("Name");
		
		this.btnSearch = new Button("Search");
		
		this.btnSearch.addSelectionListener(new SelectionListener<ButtonEvent>() 
			{
				public void componentSelected(ButtonEvent ce) 
				{
					executeSearch();
				}
			});
		
		this.btnSearch.setTabIndex(2);
		this.frmSearch.addButton(btnSearch);
		this.btnSearch.setWidth("68px");
		add(this.frmSearch, new RowData(Style.DEFAULT, 1.0, new Margins()));
	}

	/**
	 * Called when the search button is clicked
	 */
	private void executeSearch() 
	{
		// Indicate we are busy
		btnSearch.setText("Searching...");
		
		// Don't mess with the form while we're running
		// the search
		frmSearch.setEnabled(false);
		
		// Get the character name
		String charName = txtCharName.getValue();
		
		// There needs to be a name
		if (charName != null)
		{
			int charNameLen = charName.length();
			
			// Character names must be 4 or more characters,
			// and no more than 24
			if (charNameLen >= 4 && charNameLen <= 24)
			{
				// Is this character already open?
				for (Window w : desk.getWindows())
				{
					if (w.getHeading().toLowerCase().equalsIgnoreCase(charName))
					{
						// Already open, so no reason to go any further
						w.show();
						w.toFront();
						
						// Allow the user to do more searching
						btnSearch.setText("Search");
						frmSearch.setEnabled(true);
						return;
					}
				}
				
				fingerSvc.getCharInfoByName(sessionID, charName, new AsyncCall<CharInfo>()
						{
							@Override
							public void onFailure(Throwable caught) 
							{
								// Let the user know something went wrong
								super.onFailure(caught);
								
								// Allow the user to do more searching
								btnSearch.setText("Search");
								frmSearch.setEnabled(true);
							}
	
							@Override
							public void onSuccess(CharInfo result)
							{
								// If we have a result to display
								if (result != null)
								{
									// Create a char viewer
									WinViewChar viewer = new WinViewChar(result);								
									desk.addWindow(viewer);
									viewer.show();
								}
								else
								{
									// Either the result is above the user's pay grade,
									// or something failed gracefully on the server
									Utils.showSearchError();
								}
								
								// Allow the user to do more searching
								btnSearch.setText("Search");
								frmSearch.setEnabled(true);
							}				
						});
				
			} // End if
			else
			{
				// Character name is not in the valid range
				Utils.showSearchError();
				
				// Allow the user to do more searching
				btnSearch.setText("Search");
				frmSearch.setEnabled(true);			
			}
		}
		else
		{
			// Character name is null
			Utils.showSearchError();
			
			// Allow the user to do more searching
			btnSearch.setText("Search");
			frmSearch.setEnabled(true);				
		}
	}
}
