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

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import net.eve.finger.shared.LoginResult;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Element;

/**
 * Basic login window with password hashing.
 * Requires 'fingersvc' to be set in the GXT registry.
 */
public class WinLogin extends Window 
{
	// Private class objects access to the form
	private TextField<String> txtUsername;
	private TextField<String> txtPassword;	
	
	/**
	 * Callback to alert the parent window we
	 * have information for them.
	 */
	private LoginCallback callback;
	
	/**
	 * RPC callback for FingerServiceAsync.doLogin
	 */
	private AsyncCall<LoginResult> _loginRPCCallback;
	
	/**
	 * Stores the user's name.
	 */
	private String username;
	
	private Button btnLogin;
	private FormPanel frmLogin;
	
	/**
	 * Access to the server
	 */
	private static FingerServiceAsync fingerSvc;
	
	static
	{
		// Get the service
		fingerSvc = (FingerServiceAsync)Registry.get("fingersvc");		
	}
	
	/**
	 * @wbp.parser.constructor
	 */
	public WinLogin() 
	{		
		// Create the GUI
		createContents();
		
		// Create the callback
		_loginRPCCallback =				
				new AsyncCall<LoginResult>()
				{
					@Override
					public void onFailure(Throwable caught) 
					{
						// Let the user know something went wrong
						super.onFailure(caught);
						
						// Let the user try again
						frmLogin.setEnabled(true);
					}

					@Override
					public void onSuccess(LoginResult result) 
					{						
						// Did the login work?
						if (result != null)
						{
							// Login successful, notify the parent
							callback.success(result);
						}
						else		
						{
							// Let the user try again
							frmLogin.setEnabled(true);
							
							// Invalid username/pw, notify the parent							
							callback.failure();							
						}						
					}
				};
		
		// Give the username textbox focus
		txtUsername.focus();		
	}
		
	/**
	 * Builds the GUI.
	 */
	private void createContents()
	{
		setModal(true);
		setBlinkModal(false);		
		this.setClosable(false);
		this.setSize("300px", "125px");
		this.setHeading("Login");
		this.setLayout(new RowLayout(Orientation.VERTICAL));
		
		frmLogin = new FormPanel();
		frmLogin.setBodyBorder(false);
		frmLogin.setHeaderVisible(false);
		frmLogin.setHeading("");
		
		this.txtUsername = new TextField<String>();
		this.txtUsername.setId("username");
		this.txtUsername.setSelectOnFocus(true);
		this.txtUsername.setTabIndex(1);
		this.txtUsername.setMaxLength(50);
		frmLogin.add(txtUsername, new FormData("100%"));
		this.txtUsername.setFieldLabel("Username");
		
		this.txtPassword = new TextField<String>();
		this.txtPassword.setId("passwd");
		this.txtPassword.addListener(Events.KeyPress, new Listener<FieldEvent>() {
			public void handleEvent(FieldEvent e) 
			{
				if (e.getKeyCode() == KeyCodes.KEY_ENTER)
				{
					executeLogin();
				}
			}
		});
		this.txtPassword.setMaxLength(50);
		this.txtPassword.setTabIndex(2);
		this.txtPassword.setSelectOnFocus(true);
		this.txtPassword.setPassword(true);
		frmLogin.add(txtPassword, new FormData("100%"));
		this.txtPassword.setFieldLabel("Password");

		btnLogin = new Button("Login");
		frmLogin.addButton(btnLogin);
		btnLogin.addSelectionListener(new SelectionListener<ButtonEvent>() 
			{
				public void componentSelected(ButtonEvent ce) 
				{					
					executeLogin();
				}
			});
		
		btnLogin.setTabIndex(3);		
		this.add(frmLogin, new RowData(1.0, 1.0, new Margins()));
	}	

	@Override
	protected void onRender(Element parent, int pos) 
	{
		super.onRender(parent, pos);
		
		// Make the username and password fields detectable
		// to browsers
		
		// We don't know if the UI exists yet, so we'll wait a bit
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() 
			{
				@Override
				public void execute() 
				{
					// Set up a blinking callback
					Scheduler.get().scheduleDeferred(							
						new Scheduler.ScheduledCommand() 
						{							
							@Override
							public void execute() 
							{
								txtUsername.el().firstChild().setId("username");
								txtPassword.el().firstChild().setId("passwd");								
							}
						});						
				}
			});		
	}
	
	/**
	 * @return The username.
	 */
	public String getUsername() 
	{
		return username;
	}

	/**
	 * Sets the callback.
	 * @param newCallback The {@link LoginCallback} to use.
	 */
	public void setCallback(LoginCallback newCallback)
	{
		callback = newCallback;
	}
	
	/**
	 * Sends the user's name, hashed password, and session ID to the server to 
	 * be validated. If successful, it fires the LoginSuccess event and closes the dialog.
	 */
	private void executeLogin()
	{
		// No messing with the form till we're done
		frmLogin.setEnabled(false);
		
		String rawPW = txtPassword.getValue();
		username = txtUsername.getValue();
		
		// Input sanity check
		if (username != null && username.length() > 3 && username.length() < 100
			&& rawPW != null && rawPW.length() > 3 && rawPW.length() < 150)
		{					
			// Hash the password so we don't send the pw in plain text
			// over the wire in any form
			String passwd = Utils.SHA1(rawPW);
			
			// Try to log the user in
			fingerSvc.doLogin(username, passwd, _loginRPCCallback);
		}
		else
		{			
			// Give the user another chance
			frmLogin.setEnabled(true);
		
			// You're doing it wrong.
			// A username AND password are REQUIRED.						
			callback.failure();		
		}
	}
}
