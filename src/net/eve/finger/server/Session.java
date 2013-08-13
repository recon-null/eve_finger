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

package net.eve.finger.server;

import java.util.Calendar;
import java.util.Date;

/**
 * Represents active sessions 
 *
 */
public class Session
{	
	/**
	 * Sets the maximum session age in days.
	 */
	private static final int MAX_SESSION_AGE = 1;
	
	/**
	 * Holds the username.
	 */
	private String username;
	
	/**
	 * Holds the user's database ID.
	 */
	private int userID;
	
	/**
	 * Holds the session ID.
	 */
	private String sessionID;
	
	/**
	 * Holds the user's access group
	 */
	private int accessGroup;
	
	/**
	 * Sets when the session was created.
	 * This is a public field for performance reasons as it is
	 * used in batch operations.
	 */
	public Date createdAt;
	
	public Session()
	{
		// Store when this was created, by default "now."
		createdAt = new Date();		
	}
	
	/**
	 * Inits fields.
	 * @param username The username to set.
	 * @param userID The user ID to set.
	 * @param sessionID The session ID to set.
	 * @param accessGroup The access group to set.
	 * @param createdAt The date the session was created, null for "now."
	 */
	public Session(String username, int userID, String sessionID, int accessGroup, Date createdAt)
	{
		this();
		this.username = username;
		this.userID = userID;
		this.sessionID = sessionID;
		this.accessGroup = accessGroup;
		
		if (createdAt != null)
		{
			this.createdAt = createdAt;
		}
	}
	
	@Override
	/**
	 * Gets a string representation of the object.
	 * Used for debugging purposes.
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("username: ");
		sb.append(username);
		sb.append(", userID: ");
		sb.append(userID);
		sb.append(", sessionID: ");
		sb.append(sessionID);
		sb.append(", accessGroup: ");
		sb.append(accessGroup);
		sb.append(", createdAt: ");
		sb.append(Utils.getMySQLDT(createdAt));
		return sb.toString();
	}
	
	/**
	 * Calculates the current max age of a session.
	 * @return The current max age of a session.
	 */
	public static Calendar getMaxAge()
	{
		// Calculate the current max age of a session
		Calendar maxAge = Calendar.getInstance();		
		maxAge.add(Calendar.DAY_OF_MONTH, -MAX_SESSION_AGE);		
		return maxAge;
	}
	
	/**
	 * Determines if the session has expired or not.
	 * @return If the session has expired.
	 */
	public Boolean isValid()
	{	
		// Does 'created at' fall after the max session age?
		return createdAt.after(getMaxAge().getTime());
	}

	/**
	 * @return The username.
	 */
	public String getUsername() 
	{
		return username;
	}

	/**
	 * @param username The username to set.
	 */
	public void setUsername(String username) 
	{
		this.username = username;
	}

	/**
	 * @return The date this object was created.
	 */
	public Date getCreatedAt() 
	{
		return createdAt;
	}

	/**
	 * @param createdAt The date this object was created.
	 */
	public void setCreatedAt(Date createdAt)
	{
		this.createdAt = createdAt;
	}	

	/**
	 * @return The sessionID.
	 */
	public String getSessionID() 
	{
		return sessionID;
	}

	/**
	 * @param sessionID The sessionID to set.
	 */
	public void setSessionID(String sessionID) 
	{
		this.sessionID = sessionID;
	}

	/**
	 * @return The user's access group.
	 */
	public int getAccessGroup() 
	{
		return accessGroup;
	}

	/**
	 * @param power The user's access group.
	 */
	public void setAccessGroup(int power) 
	{
		this.accessGroup = power;
	}

	/**
	 * @return The userID.
	 */
	public int getUserID() 
	{
		return userID;
	}

	/**
	 * @param userID The userID to set.
	 */
	public void setUserID(int userID) 
	{
		this.userID = userID;
	}

}
