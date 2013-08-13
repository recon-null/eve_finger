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

import java.util.Date;
import java.util.List;

import net.eve.finger.shared.LoginResult;

import com.extjs.gxt.desktop.client.Desktop;
import com.extjs.gxt.desktop.client.StartMenu;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.event.WindowListener;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Net_eve_finger implements EntryPoint 
{
	/**
	 * Cookie name for storing the sessionID.
	 */
	private final String COOKIE_SESSION = "net_eve_finger_session";
	
	/**
	 * Cookie name for storing the username.
	 */
	private final String COOKIE_NAME = "net_eve_finger_name";
	
	/**
	 * Sets how long the cookie expiration date is.
	 */
	private final long COOKIE_EXPIRE_DAYS = 1;
	
	/**
	 * Async RPC access to the server.
	 */
	private final FingerServiceAsync fingerSvc = 
			GWT.create(FingerService.class);
	
	/**
	 * Manages the desktop interface.
	 */
	private final DesktopMgr deskMgr = new DesktopMgr();
	
	/**
	 * The desktop
	 */
	private Desktop desktop;
	
	/**
	 * Stores static data returned by logging in or
	 * resuming a session.
	 */
	private LoginResult staticLoginData;
	
	/**
	 * Holds the current authed session ID
	 */
	private String sessionID = null;
	
	/**
	 * Holds the current user's name.
	 * Used for display only.
	 */
	private String username = null;

	/**
	 * This is the entry point method.
	 */	
	public void onModuleLoad()
	{		
		// Setup the desktop interface		
		deskMgr.createDesktop();
		desktop = deskMgr.getDesktop();
		desktop.getTaskBar().setEnabled(false);
		
		// Make app level objects (globals) available
		// to all windows
		Registry.register("desktop", desktop);
		Registry.register("fingersvc", fingerSvc);
		
		// If there is an old session stored, retrieve it
		final String cookieSessionID = Cookies.getCookie(COOKIE_SESSION);	
		
		// Do they have a stored session ID?
		if (cookieSessionID != null)
		{
			// Is it valid?
			fingerSvc.isSessionValid(cookieSessionID, new AsyncCall<LoginResult>()
					{
						@Override
						public void onSuccess(LoginResult result)
						{
							// Valid session?
							if (result != null)
							{
								// Save it
								sessionID = cookieSessionID;								
								
								// Get their username
								username = Cookies.getCookie(COOKIE_NAME);
								
								// Save the other data
								staticLoginData = result;
								
								// Load the UI
								userAuthed();
							}
							else
							{
								// They'll have to login
								showLoginForm();
							}
						}				
					});				
		}
		else
		{
			// No, ask them to login
			showLoginForm();
		}
	}
	
	/**
	 * Builds the main user interface and glue code.
	 * Do not call more than once. 
	 */
	private void userAuthed()
	{
		// Make static data available to the entire
		// application
		Registry.register("staticlogindata", staticLoginData);

		// Make the username available
		Registry.register("username", username);
		
		// Make the session ID available
		Registry.register("sessionid", sessionID);
		
		// Set the user's name
		StartMenu menu = desktop.getStartMenu();		
		menu.setHeading(username);
		menu.setIconStyle("user");
		
		// Build the UI / glue
		createLastLoginDateItem(menu);
		createLogoutItem(menu);
		WinSearchChar searchWindow = new WinSearchChar();
		deskMgr.addStartMenuEntry
			("Character Search", searchWindow);
		deskMgr.addStartMenuEntry("Alliance Search",
				new WinSearchAlliance());
		
		// Enable the taskbar		
		desktop.getTaskBar().setEnabled(true);
		
		// Open the character search window by default
		desktop.addWindow(searchWindow);
		searchWindow.show();
	}
		
	/**
	 * Handles the entire login process
	 */
	private void showLoginForm()
	{
		// Login error dialog
		final Dialog loginError = new Dialog();
		loginError.setHeading("Login Failed!");
		loginError.addText("Username or password incorrect.");
		loginError.setBodyStyle("fontWeight:bold;padding:13px;");
		loginError.setSize(300, 100);
		loginError.setHideOnButtonClick(true);
		loginError.setClosable(false);
		loginError.setButtons(Dialog.OK);
		loginError.setModal(true);
		
		// Create the login window
		final WinLogin loginWindow = new WinLogin();

		// Set the callbacks
		loginWindow.setCallback(				
				new LoginCallback()
				{
					@Override
					public void success(LoginResult result) 
					{
						// Save the session ID
						sessionID = result.getSessionID();						
						
						// Save the other data
						staticLoginData = result;
						
						// Save the username
						username = loginWindow.getUsername();
						
						// Calculate the expiration date of the cookies
						// (This class is deprecated for a reason...
						//  Google, implement the Calendar class...)
						Date expOn = new Date();						
						expOn.setTime(expOn.getTime() + 3600000 * 24 * COOKIE_EXPIRE_DAYS);						
						
						// Remember this user for later
						Cookies.setCookie(COOKIE_SESSION, sessionID, expOn);
						Cookies.setCookie(COOKIE_NAME, loginWindow.getUsername(), expOn);
						
						// Don't need the login window anymore						
						desktop.removeWindow(loginWindow);
						loginWindow.hide();
						
						// Load the UI
						userAuthed();
					}
					
					@Override
					public void failure() 
					{
						// Show an error 
						loginError.show();
					}		
				});	
		
		// Display the login window
		desktop.addWindow(loginWindow);
		loginWindow.show();
	}	
	
	/**
	 * Creates the logout menu item on the start menu
	 * and contains all handler code.
	 * @param startMenu The start menu to add the item to.
	 */
	private void createLogoutItem(StartMenu startMenu)
	{
		// Create the item
		MenuItem menuItem = new MenuItem("Logout");
		menuItem.setIcon(IconHelper.createStyle("logout"));
		
		// Set up a click handler 
		menuItem.addSelectionListener(new SelectionListener<MenuEvent>() 
				{
					@Override
					public void componentSelected(MenuEvent ce) 
					{
						// User wants to logout
						fingerSvc.doLogout(sessionID, new AsyncCall<Boolean>() 
						{
							@Override
							public void onSuccess(Boolean result)
							{
								// Clear the cookies
								Cookies.removeCookie(COOKIE_SESSION);
								Cookies.removeCookie(COOKIE_NAME);
								
								// Build the logout message
								Dialog logoutMsg = new Dialog();
								logoutMsg.setHeading("Logged out.");								
								logoutMsg.setButtons(Dialog.CLOSE);
								logoutMsg.setClosable(false);
								logoutMsg.setHideOnButtonClick(true);
								logoutMsg.setModal(true);
								logoutMsg.addText("You have successfully logged out.");
								logoutMsg.setBodyStyle("fontWeight:bold;padding:13px;");
								logoutMsg.setSize(300, 100);
								
								// Add a listener to close the application
								logoutMsg.addWindowListener(new WindowListener()
								{
									@Override
									public void windowHide(WindowEvent we)
									{	
										// Actually hide the dialog
										super.windowHide(we);										
										
										// TODO: This seems to cause an exception,
										//       but it still works.
										// 		 Find another method of closing
										//		  the browser window
										Utils.closeBrowserWindow();
									}										
								}); // Window listener
								
								// Show the logout message
								logoutMsg.show();								
								
							} // onSuccess
							
						});	// RPC call
					
						// Disable the taskbar
						desktop.getTaskBar().setEnabled(false);
						
						// *********************************
						//
						// Hide and remove all windows
						//
						// *********************************
						List<Window> wl = desktop.getWindows();
						Window w;
						for(int i = 0; i < wl.size(); i++)
						{
							w = wl.get(i);
							w.hide();
							desktop.removeWindow(w);											
						}
											
					} // Click method
					
		}); // Click handler
		
		// Add the logout item to the right side
		// of the start menu
		startMenu.addTool(menuItem);
	}
	
	
	/**
	 * Creates the last login info entry on the start menu.
	 * @param startMenu The start menu to add the entry to.
	 */
	private void createLastLoginDateItem(StartMenu startMenu)
	{
		// Show their last login date in the start menu,
		// if they have one
		Date lastLogin = staticLoginData.getLastLogin();		
		if (lastLogin != null)
		{
			// Build the menu item
			MenuItem menuItem = new MenuItem();
			menuItem.setStyleAttribute("fontWeight", "bold");
			menuItem.setText("Last login: " 
							+ Utils.formatFriendlyDateTime
							(lastLogin));
			
			// The user shouldn't actually be clicking
			// on this
			menuItem.setEnabled(false);							
			startMenu.add(menuItem);			
		}		
	}
}
