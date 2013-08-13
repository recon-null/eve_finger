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

import net.eve.finger.shared.AccessGroup;
import net.eve.finger.shared.AllianceInfo;
import net.eve.finger.shared.CharInfo;
import net.eve.finger.shared.LoginResult;

import com.extjs.gxt.desktop.client.Desktop;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;

/**
 * Editor for characters.
 * Requires 'staticlogindata', 'fingersvc', 'desktop', 'sessionid' 
 * to be set in the GXT registry. 
 */
public class WinEditChar extends Window 
{
	private FormPanel frmCharUpdate;
	private CheckBox chkFC;
	private CheckBox chkScout;
	private CheckBox chkCyno;
	private CheckBox chkTitan;
	private CheckBox chkSuper;
	private Button btnUpdate;
	private ComboBox<BeanModel> cboAssocWith;
	private ComboBox<BeanModel> cboAccess;
	private SimpleComboBox<String> cboTimezone;
	
	/**
	 * The character info being edited.
	 */
	private CharInfo charInfo;
	
	/**
	 * The parent viewer.
	 */
	private WinViewChar viewer;	
	
	/**
	 * Holds the access groups used in the
	 * combo box.
	 */
	private static ListStore<BeanModel> accessStore;
	
	/**
	 * Holds the alliance list used in the
	 * assoc with combo box.
	 */
	private static ListStore<BeanModel> allianceStore;
	
	/**
	 * Access to the server.
	 */
	private static FingerServiceAsync fingerSvc;
	
	/**
	 * The user's session ID.
	 */
	private static String sessionID;
	
	/**
	 * Access to the desktop.
	 */
	private static Desktop desk;
	
	/**
	 * Error if input validation fails
	 */
	private static Dialog inputError = new Dialog();
	
	/**
	 * Error if a server side problem occurs.
	 */
	private static Dialog updateError = new Dialog();
		
	static
	{	
		// Get the login result
		LoginResult lr = (LoginResult)Registry.get("staticlogindata");
		
		// Convert the access group list into something a combobox can use
		accessStore = Utils.getAccessGroupStoreFiltered(lr.getAccessGroupList(),
														lr.getAccessGroupPower());
		
		// Convert the alliance list into something a combobox can use
		allianceStore = Utils.getAllianceStore(lr.getAllianceList());
		
		// Get the RPC service
		fingerSvc = (FingerServiceAsync)Registry.get("fingersvc");
		
		// Get the desktop
		desk = (Desktop)Registry.get("desktop");
		
		// Get the session ID
		sessionID = (String)Registry.get("sessionid");
		
		// Isn't it obvious?
		buildDialogs();
	}

	/**
	 * Default constructor.
	 * @wbp.parser.constructor
	 */
	public WinEditChar() 
	{
		setModal(true);
		setBlinkModal(false);		
		createContents();
		
		// Setup the timezone combobox
		cboTimezone.add("None");
		cboTimezone.add("NA West");
		cboTimezone.add("NA Central");
		cboTimezone.add("NA East");
		cboTimezone.add("Euro (near GMT)");		
	}
	
	/**
	 * Builds an editor for the given character parented to
	 * the givne viewer.
	 * @param viewer The parent viewer.
	 * @param charInfo The character to edit.
	 */
	public WinEditChar(WinViewChar viewer, CharInfo charInfo)
	{
		this();		
		
		// Set fields
		this.charInfo = charInfo;
		this.viewer = viewer;
		this.setHeading("Editing: " + charInfo.getCharName());
		chkFC.setValue(charInfo.getIsFC());
		chkScout.setValue(charInfo.getIsScout());
		chkCyno.setValue(charInfo.getIsCyno());
		chkTitan.setValue(charInfo.getIsTitan());
		chkSuper.setValue(charInfo.getIsSuper());
		
		// Set assoc with
		cboAssocWith.setStore(allianceStore);
		cboAssocWith.setDisplayField("allianceName");		
		cboAssocWith.setValue(allianceStore.findModel("allianceID", charInfo.getAssocWithAlliance()));
		
		// Set access
		cboAccess.setStore(accessStore);
		cboAccess.setDisplayField("name");
		cboAccess.setValue(accessStore.findModel("id", charInfo.getNeededAccess()));
		
		// Set timezone
		String timezone = charInfo.getTimezone();
		if (timezone != null)
		{			
			cboTimezone.setValue(cboTimezone.findModel(timezone));
		}
		else
		{
			cboTimezone.setValue(cboTimezone.findModel("None"));			
		}		
	}
	
	/**
	 * Builds the UI
	 */
	private void createContents()
	{
		setSize("240px", "293px");
		setHeading("Editing [charName]");
		setLayout(new RowLayout(Orientation.VERTICAL));		
		
		this.frmCharUpdate = new FormPanel();
		this.frmCharUpdate.setPadding(5);
		this.frmCharUpdate.setFrame(true);
		this.frmCharUpdate.setLabelWidth(70);
		this.frmCharUpdate.setHeaderVisible(false);
		this.frmCharUpdate.setHeading("New FormPanel");
		
		this.chkFC = new CheckBox();
		this.chkFC.setTabIndex(1);
		this.frmCharUpdate.add(this.chkFC, new FormData("100%"));
		this.chkFC.setBoxLabel("Fleet Commander");
		this.chkFC.setHideLabel(true);
		
		this.chkScout = new CheckBox();
		this.chkScout.setTabIndex(2);
		this.frmCharUpdate.add(this.chkScout, new FormData("100%"));
		this.chkScout.setBoxLabel("Scout");
		this.chkScout.setHideLabel(true);
		
		this.chkCyno = new CheckBox();
		this.chkCyno.setTabIndex(3);
		this.frmCharUpdate.add(this.chkCyno, new FormData("100%"));
		this.chkCyno.setBoxLabel("Cyno");
		this.chkCyno.setHideLabel(true);
		
		this.chkTitan = new CheckBox();
		this.chkTitan.setTabIndex(4);
		this.frmCharUpdate.add(this.chkTitan, new FormData("100%"));
		this.chkTitan.setBoxLabel("Titan");
		this.chkTitan.setHideLabel(true);
		
		this.chkSuper = new CheckBox();
		this.chkSuper.setTabIndex(5);
		this.frmCharUpdate.add(this.chkSuper, new FormData("100%"));
		this.chkSuper.setBoxLabel("Super");
		this.chkSuper.setHideLabel(true);
		
		this.btnUpdate = new Button("Update");
		this.btnUpdate.setTabIndex(9);
		this.btnUpdate.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) 
			{				
				executeUpdate();
			}
		});
		this.frmCharUpdate.addButton(this.btnUpdate);
		this.btnUpdate.setWidth("61px");
		
		this.cboAssocWith = new ComboBox<BeanModel>();
		this.cboAssocWith.setMessageTarget("tooltip");
		this.cboAssocWith.setAllowBlank(false);
		this.cboAssocWith.setTriggerAction(TriggerAction.ALL);
		this.cboAssocWith.setSelectOnFocus(true);
		this.cboAssocWith.setTabIndex(6);
		this.cboAssocWith.setForceSelection(true);
		this.cboAssocWith.setTypeAheadDelay(100);
		this.cboAssocWith.setTypeAhead(true);
		this.cboAssocWith.setStore(new ListStore<BeanModel>());
		this.frmCharUpdate.add(this.cboAssocWith, new FormData("100%"));
		this.cboAssocWith.setFieldLabel("Assoc With");
		
		this.cboTimezone = new SimpleComboBox<String>();
		this.cboTimezone.setEditable(false);
		this.cboTimezone.setTriggerAction(TriggerAction.ALL);
		this.cboTimezone.setSelectOnFocus(true);
		this.cboTimezone.setTabIndex(7);
		this.cboTimezone.setTypeAhead(true);
		this.cboTimezone.setTypeAheadDelay(100);
		this.cboTimezone.setForceSelection(true);
		this.frmCharUpdate.add(this.cboTimezone, new FormData("100%"));
		this.cboTimezone.setFieldLabel("Timezone");
		
		this.cboAccess = new ComboBox<BeanModel>();
		this.cboAccess.setTriggerAction(TriggerAction.ALL);
		this.cboAccess.setEditable(false);
		this.cboAccess.setTabIndex(8);
		this.cboAccess.setTypeAhead(true);
		this.cboAccess.setTypeAheadDelay(100);
		this.cboAccess.setForceSelection(true);
		this.cboAccess.setStore(new ListStore<BeanModel>());
		this.frmCharUpdate.add(this.cboAccess, new FormData("100%"));
		this.cboAccess.setFieldLabel("Access");
		add(this.frmCharUpdate, new RowData(1.0, 1.0, new Margins(0, 0, 0, 0)));
	}
	
	/**
	 * Called when the update button is pressed.
	 */
	private void executeUpdate() 
	{
		// Don't hammer the form
		frmCharUpdate.setEnabled(false);
		
		// Retrieve selections from the combo boxes
		List<BeanModel> rawAccess = cboAccess.getSelection();
		List<BeanModel> rawAssocWith = cboAssocWith.getSelection();
		List<SimpleComboValue<String>> rawTimezone = cboTimezone.getSelection();
		
		// Ideally this shouldn't be necessary...
		if (rawAccess.size() == 1 && rawAssocWith.size() == 1 && rawTimezone.size() == 1)
		{
			// Update the char info object with user input
			charInfo.setNeededAccess(((AccessGroup)rawAccess.get(0).getBean()).getId());
			
			String timezoneValue = rawTimezone.get(0).getValue();
			if (timezoneValue != "None")
			{
				charInfo.setTimezone(timezoneValue);				
			}
			else
			{
				charInfo.setTimezone(null);
			}			
			
			// Handle the associations
			AllianceInfo assocWith = (AllianceInfo)rawAssocWith.get(0).getBean();
			long assocWithID = assocWith.getAllianceID();
			charInfo.setAssocWithAlliance(assocWithID);
			
			// Make sure assoc name gets set null if the user has
			// removed an assoc
			if (assocWithID != 0)
			{
				charInfo.setAssocWithAllianceName(assocWith.getAllianceName());			
			}
			else
			{
				charInfo.setAssocWithAllianceName(null);
			}
			
			// Set info bits
			charInfo.setIsFC(chkFC.getValue());
			charInfo.setIsScout(chkScout.getValue());
			charInfo.setIsCyno(chkCyno.getValue());
			charInfo.setIsTitan(chkTitan.getValue());
			charInfo.setIsSuper(chkSuper.getValue());
			
			// Perform the update
			fingerSvc.updateCharRecordUsrData(sessionID, charInfo, new AsyncCall<Boolean>()
					{					
						@Override
						public void onFailure(Throwable caught) 
						{
							// RPC error of some kind
							super.onFailure(caught);
							
							// Let the user try again
							frmCharUpdate.setEnabled(true);
						}
				
						@Override
						public void onSuccess(Boolean result) 
						{
							// Was the update successful?
							if (result)
							{
								// Create a new viewer								
								WinViewChar newViewer = new WinViewChar(charInfo);
								
								// Hide the old viewer
								viewer.hide();
								desk.removeWindow(viewer);
								
								// Hide this window
								hide();
								
								// Show the new viewer
								desk.addWindow(newViewer);
								newViewer.show();
							}
							else
							{
								// Unable to update the character
								updateError.show();
								frmCharUpdate.setEnabled(true);
							}
						}				
					});
		}
		else
		{
			// Really, how did you manage that?
			inputError.show();
			frmCharUpdate.setEnabled(true);
		}
	}
	
	/**
	 * Builds error dialogs used by this window.
	 */
	private static void buildDialogs()
	{
		// Build the input error dialog
		inputError.setHeading("Input Error!");
		inputError.addText("You have managed to have no selection in the combo boxes. That shouldn't be possible.");
		inputError.setBodyStyle("fontWeight:bold;padding:13px;");
		inputError.setSize(300, 120);
		inputError.setHideOnButtonClick(true);
		inputError.setButtons(Dialog.OK);	
		inputError.setClosable(false);
		inputError.setModal(true);
		
		// Build the update error dialog
		updateError.setHeading("Update Error!");
		updateError.addText("Update failed. Something went wrong server side, or your client is out of date. " +
							"Try reloading the application and trying again.");
		updateError.setBodyStyle("fontWeight:bold;padding:13px;");
		updateError.setSize(300, 120);
		updateError.setHideOnButtonClick(true);
		updateError.setButtons(Dialog.OK);	
		updateError.setClosable(false);
		updateError.setModal(true);		
	}
}
