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

package net.eve.finger.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Container class for returning basic application and user
 * information to clients when they login. By packaging this
 * information together, we avoid unnecessary RPC calls that
 * every client would make after they logged in.
 *   
 */
public class LoginResult implements IsSerializable
{
	
	/**
	 * This user's access group.
	 * (Not used for security of any kind.)
	 */
	private int accessGroup;
	
	/**
	 * This user's last login date.
	 */
	private Date lastLogin;
	
	/**
	 * This user's session ID.
	 */
	private String sessionID;
	
	/**
	 * Currently available access groups, sorted
	 * by power.
	 */
	private List<AccessGroup> accessGroupList;

	/**
	 * All alliances in the system when the user logged in.
	 */
	private List<AllianceInfo> allianceList;
	
	/**
	 * The user's DB ID
	 */
	private int userID;
	
	/**
	 * The user's power as defined by their access group.
	 */
	private int accessGroupPower;	

	/**
	 * Default constructor.
	 */
	public LoginResult()
	{
		
	}	
	
	/**
	 * Inits/converts fields.
	 * @param accessGroupMap The access group map to set 						
	 * @param accessGroup The access group to set.
	 * @param lastLogin The last login date to set.
	 * @param accessGroupPower The power the user has from their access group. 
	 */
	public LoginResult(int id, String sessionID, Map<Integer, AccessGroup> accessGroupMap, Map<Long, AllianceInfo> allianceMap, int accessGroup, int accessGroupPower, Date lastLogin)
	{
		this.userID = id;
		this.sessionID = sessionID;
		this.accessGroupPower = accessGroupPower;
		this.allianceList = new ArrayList<AllianceInfo>(allianceMap.values());
		this.accessGroupList = new ArrayList<AccessGroup>(accessGroupMap.values());
		Collections.sort(accessGroupList);
		this.accessGroup = accessGroup;
		this.lastLogin = lastLogin;
	}

	/**
	 * @return The access group list.
	 */
	public List<AccessGroup> getAccessGroupList() 
	{
		return accessGroupList;
	}

	/**
	 * @param accessGroupList The access group list to set.
	 */
	public void setAccessGroupList(List<AccessGroup> accessGroupList) 
	{
		this.accessGroupList = accessGroupList;
	}


	/**
	 * @return The access group.
	 */
	public int getAccessGroup() 
	{
		return accessGroup;
	}
	

	/**
	 * @param accessGroup The access group to set.
	 */
	public void setAccessGroup(int accessGroup) 
	{
		this.accessGroup = accessGroup;
	}

	
	/**
	 * @return The last login date.
	 */
	public Date getLastLogin() 
	{
		return lastLogin;
	}
	

	/**
	 * @param lastLogin The last login date to set.
	 */
	public void setLastLogin(Date lastLogin) 
	{
		this.lastLogin = lastLogin;
	}


	/**
	 * @return The session ID.
	 */
	public String getSessionID() 
	{
		return sessionID;
	}


	/**
	 * @param sessionID The session ID to set.
	 */
	public void setSessionID(String sessionID) 
	{
		this.sessionID = sessionID;
	}


	/**
	 * @return The alliance list.
	 */
	public List<AllianceInfo> getAllianceList() 
	{
		return allianceList;
	}


	/**
	 * @param allianceList The alliance list to set.
	 */
	public void setAllianceList(List<AllianceInfo> allianceList)
	{
		this.allianceList = allianceList;
	}

	/**
	 * @return The userID.
	 */
	public int getUserID() 
	{
		return userID;
	}


	/**
	 * @param userID The user ID to set.
	 */
	public void setUserID(int userID) 
	{
		this.userID = userID;
	}
	
	/**
	 * @return The user's power as defined by their access group.
	 */
	public int getAccessGroupPower() {
		return accessGroupPower;
	}

	/**
	 * @param accessGroupPower Sets the user's power as defined by their access group.
	 */
	public void setAccessGroupPower(int accessGroupPower) 
	{
		this.accessGroupPower = accessGroupPower;
	}
}
