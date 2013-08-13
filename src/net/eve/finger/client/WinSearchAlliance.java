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

import java.util.List;

import net.eve.finger.shared.AllianceInfo;
import net.eve.finger.shared.LoginResult;

import com.extjs.gxt.desktop.client.Desktop;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.store.ListStore;


/**
 * Search form for alliances.
 * Requires 'desktop', 'sessionid', 'staticlogindata'
 * to be set in the GXT registry.
 */
public class WinSearchAlliance extends Window 
{
	private FormPanel frmSearch;
	private Button btnSearch;
	private ComboBox<BeanModel> cboAllianceName;	
		
	/**
	 * Stores the desktop we add windows to.
	 */
	private static Desktop desk;	
	
	/**
	 * Holds the alliance list used in the
	 * combo box.
	 */
	private static ListStore<BeanModel> allianceStore;
	
	/**
	 * Access to permission data
	 */
	private static LoginResult staticLoginData;
		
	static
	{	
		// Get the desktop
		desk = (Desktop)Registry.get("desktop");		
		
		// Get the static login data
		staticLoginData = (LoginResult)Registry.get("staticlogindata");
				
		// Convert the alliance list into something a combobox can use
		allianceStore = Utils.getAllianceStore(staticLoginData.getAllianceList());	
		
		// Remove the 'None' alliance
		allianceStore.remove(allianceStore.findModel("allianceName", "None"));
		allianceStore.commitChanges();
	}
	
	/**
	 * Default constructor.
	 * @wbp.parser.constructor
	 */
	public WinSearchAlliance() 
	{		
		// Build the UI
		createContents();
		
		// Setup the combo box
		cboAllianceName.setStore(allianceStore);
		cboAllianceName.setDisplayField("allianceName");
		
		// Select the first alliance by default
		if (allianceStore.getModels().size() > 0)
		{
			cboAllianceName.setValue(allianceStore.getAt(0));
		}
		
		cboAllianceName.focus();
	}
	
	/**
	 * Builds the UI
	 */
	private void createContents()
	{
		setSize("254", "100");
		setMinimizable(true);
		setHeading("Alliance Search");
		setLayout(new RowLayout(Orientation.VERTICAL));
		
		this.frmSearch = new FormPanel();
		this.frmSearch.setBodyBorder(false);
		this.frmSearch.setLabelWidth(35);
		this.frmSearch.setHeaderVisible(false);
		this.frmSearch.setHeading("New FormPanel");
		
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
		
		this.cboAllianceName = new ComboBox<BeanModel>();
		this.cboAllianceName.setTabIndex(1);
		this.cboAllianceName.setStore(new ListStore<BeanModel>());
		cboAllianceName.setMessageTarget("tooltip");
		cboAllianceName.setAllowBlank(false);
		cboAllianceName.setTriggerAction(TriggerAction.ALL);
		cboAllianceName.setSelectOnFocus(true);
		cboAllianceName.setForceSelection(true);
		cboAllianceName.setTypeAheadDelay(100);
		cboAllianceName.setTypeAhead(true);
		cboAllianceName.addListener(Events.KeyPress, new Listener<FieldEvent>() 
			{		
				@Override
				public void handleEvent(FieldEvent e)
				{
					if (e.getKeyCode() == KeyCodes.KEY_ENTER)
					{
						executeSearch();					
					}				
				}
			});
		
		this.frmSearch.add(this.cboAllianceName, new FormData("100%"));
		this.cboAllianceName.setFieldLabel("Name");
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
		
		// Is there a valid selection?
		List<BeanModel> selection = cboAllianceName.getSelection();
		if (selection.size() == 1)
		{
			// Yes, get the alliance
			BeanModel alliance = selection.get(0);
			
			// Get the alliance name			
			String allianceName = (String)alliance.get("allianceName");
		
			// Is this alliance already open?
			for (Window w : desk.getWindows())
			{
				if (w.getHeading().toLowerCase().equalsIgnoreCase(allianceName))
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
			
			// There isn't already a viewer, so create one and show it
			WinViewAlliance viewer = new WinViewAlliance((AllianceInfo)alliance.getBean());
			desk.addWindow(viewer);
			viewer.show();
			
			// Allow the user to do more searching
			btnSearch.setText("Search");
			frmSearch.setEnabled(true);
			
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
}