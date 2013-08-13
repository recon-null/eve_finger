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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.eve.finger.shared.AccessGroup;
import net.eve.finger.shared.AllianceInfo;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelLookup;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Text;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.i18n.client.DateTimeFormat;


/**
 * Utility functions.
 * 
 */
public class Utils
{
	/**
	 * General RPC error dialog.
	 */
	private static final Dialog rpcError = new Dialog();
	
	/**
	 * General error dialog for search failures.
	 */
	private static final Dialog searchError = new Dialog();	
	
	static
	{
		rpcError.setHeading("RPC Error!");
		rpcError.addText("RPC call failed. Try your request again, or contact tech support.");
		rpcError.setBodyStyle("fontWeight:bold;padding:13px;");
		rpcError.setSize(300, 120);
		rpcError.setHideOnButtonClick(true);
		rpcError.setButtons(Dialog.OK);	
		rpcError.setClosable(false);
		rpcError.setModal(true);
		
		searchError.setHeading("Search Error!");
		searchError.addText("Search failed. There may have been a technical problem, " +
							"or the results were above your pay grade.");
		searchError.setBodyStyle("fontWeight:bold;padding:13px;");
		searchError.setSize(300, 130);
		searchError.setHideOnButtonClick(true);
		searchError.setButtons(Dialog.OK);	
		searchError.setClosable(false);
		searchError.setModal(true);
	}
	
	/**
	 * Shows a generic RPC error dialog.
	 */
	public static void showRPCError()
	{
		rpcError.show();		
	}
	
	/**
	 * Shows a generic search error dialog.
	 */
	public static void showSearchError()
	{
		searchError.show();
	}
	
	
	/**
	 * Makes the Text widgets blink with the given style.
	 * @param txt The Text widgets to blink.
	 * @param style The style to blink.
	 * @param interval The interval between blinks.
	 * @param times The number of times to blink the widgets.
	 * 				This must be even or the widgets will end up 
	 * 				blinked on instead of off.
	 */
	public static void blinkText(final Text[] texts, final String style, final int interval, final int times)
	{	
		// We don't know if the UI exists yet, so we'll wait a bit
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() 
			{
				@Override
				public void execute() 
				{
					// Set up a blinking callback
					Scheduler.get().scheduleFixedPeriod(							
						new Scheduler.RepeatingCommand()
						{	
							// Counts the number of times
							// execute was called.
							int execs = 0;
							
							@Override
							public boolean execute()
							{
								// Blink the text
								for (Text txt : texts)
								{
									txt.el().toggleStyleName(style);									
								}
								return (++execs < times);
							}
						}, 
						interval);
				}
			});
	}
	
	/**
	 * Formats a date object into something readable to a human, 
	 * since most humans do not think about time in milliseconds
	 * since Unix Epoch
	 * @param date The date to format.
	 * @return The date in a human readable form.
	 */
	public static String formatFriendlyDateTime(Date date)
	{
		return DateTimeFormat.getFormat("yy.MM.dd HH:mm").format(date);
	}
	
	/**
	 * Converts the given access group list into a {@link ListStore},
	 * sorted by power, removing groups with power > the specified power.
	 * @param grpList The list to convert.
	 * @param power Remove groups with power > this power.
	 * @return The list as a {@link ListStore}, sorted by power.
	 */
	public static ListStore<BeanModel> getAccessGroupStoreFiltered(List<AccessGroup> grpList, int power)
	{
		List<AccessGroup> toRemove = new ArrayList<AccessGroup>(10);
		AccessGroup temp;		
		int grpCount = grpList.size();
		
		// Figure out what needs to be removed
		for (int i = 0; i < grpCount; i++)
		{
			temp = grpList.get(i);
			if (temp.getPower() > power)
			{
				toRemove.add(grpList.get(i));				
			}			
		}
		
		// And remove those groups
		grpList.removeAll(toRemove);		
		
		// Perform the conversion
		return getAccessGroupStore(grpList);		
	}
	
	/**
	 * Converts the given access group list into a {@link ListStore},
	 * sorted by power.
	 * @param grpList The list to convert.
	 * @return The list as a {@link ListStore}, sorted by power.
	 */
	private static ListStore<BeanModel> getAccessGroupStore(List<AccessGroup> grpList)
	{
		// Get the access groups
		ListStore<BeanModel> temp = new ListStore<BeanModel>();
		temp.add(BeanModelLookup.get().getFactory(AccessGroup.class).createModel(grpList));
		temp.sort("power", SortDir.ASC);		
		return temp;	
	}
	
	/**
	 * Converts the given alliance list into a {@link ListStore},
	 * sorted by name.
	 * @param allianceList The list to convert.
	 * @return The list as a {@link ListStore}, sorted by name.
	 */
	public static ListStore<BeanModel> getAllianceStore(List<AllianceInfo> allianceList)
	{		
		ListStore<BeanModel> temp = new ListStore<BeanModel>();
		temp.add(BeanModelLookup.get().getFactory(AllianceInfo.class).createModel(allianceList));
		temp.sort("allianceName", SortDir.ASC);
		return temp;
	}

	/**
	 * Calls the IGB add contact function for the
	 * given character ID.
	 * @param charID The character ID to add as a contact.
	 */
	public static native void IGB_addContact(int charID) 
/*-{
		// Bring up the add contact window in EVE for
		// the given character ID
		$wnd.CCPEVE.addContact(charID);
		
	}-*/;
	
	/**
	 * Uses JS to close the browser window.
	 * @throws Unknown Some kind of exception in GWT, but
	 * 		   doesn't effect the operation of the function.
	 */
	public static native void closeBrowserWindow() 
/*-{
	
		// Close the browser window
		$wnd.close();		
	  	
	}-*/;
	
	public static native String SHA1(String msg)
/*-{
		return $wnd.Crypto.util.bytesToHex($wnd.Crypto.SHA1(msg, { asBytes: true }));
	
	}-*/;
	
	/**
	 * Calls a JS implementation of SHA256.
	 * 
	 * The string is 
	 * 
	 * (GWT doesn't have the basic hashing the JRE provides
	 *  implemented on the client.)
	 *  
	 * @param rawPW The string to hash. 
	 * 				(Automatically converted to bytes 
	 * 				 using UTF-8 encoding.)
	 * @returns The hashed string as bytes in base64.
	 */
	public static native String SHA256(String msg)
 /*-{ 	
 	
  		return $wnd.Crypto.util.bytesToHex($wnd.Crypto.SHA256(msg, { asBytes: true }));
  			
    }-*/;

}
