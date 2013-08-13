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

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * DTO for corp records.
 */
public class CorpInfo implements IsSerializable
{
	/**
	 * Holds the DB ID
	 */
	private int id;
	
	/**
	 * Corp ID
	 */
	private long corpID;
	
	/**
	 * Corp name
	 */
	private String corpName;
	
	/**
	 * Member of alliance
	 */
	private long allianceID;
	
	/**
	 * Needed access
	 */
	private int neededAccess;
	
	/**
	 * Corp notes.
	 */
	private String notes;
	
	/**
	 * Default constructor
	 */
	public CorpInfo()
	{
		
	}
	
	/**
	 * Inits fields.
	 * @param id The ID to set.
	 * @param corpID The corp ID to set.
	 * @param corpName The corp name to set.
	 * @param allianceID The corp's alliance to set.
	 * @param neededAccess The needed access to set.
	 * @param notes The notes to set.
	 */
	public CorpInfo(int id, long corpID, String corpName, long allianceID, int neededAccess, String notes)
	{
		this.id = id;
		this.corpID = corpID;
		this.corpName = corpName;
		this.allianceID = allianceID;
		this.neededAccess = neededAccess;
		this.notes = notes;
	}

	/**
	 * @return the id
	 */
	public int getId() 
	{
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) 
	{
		this.id = id;
	}

	/**
	 * @return the corpID
	 */
	public long getCorpID() 
	{
		return corpID;
	}

	/**
	 * @param corpID the corpID to set
	 */
	public void setCorpID(long corpID) 
	{
		this.corpID = corpID;
	}

	/**
	 * @return the corpName
	 */
	public String getCorpName() 
	{
		return corpName;
	}

	/**
	 * @param corpName the corpName to set
	 */
	public void setCorpName(String corpName) 
	{
		this.corpName = corpName;
	}

	/**
	 * @return the allianceID
	 */
	public long getAllianceID() 
	{
		return allianceID;
	}

	/**
	 * @param allianceID the allianceID to set
	 */
	public void setAllianceID(long allianceID)
	{
		this.allianceID = allianceID;
	}

	/**
	 * @return the neededAccess
	 */
	public int getNeededAccess()
	{
		return neededAccess;
	}

	/**
	 * @param neededAccess the neededAccess to set
	 */
	public void setNeededAccess(int neededAccess)
	{
		this.neededAccess = neededAccess;
	}

	/**
	 * @return the notes
	 */
	public String getNotes() 
	{
		return notes;
	}

	/**
	 * @param notes the notes to set
	 */
	public void setNotes(String notes)
	{
		this.notes = notes;
	}

}
