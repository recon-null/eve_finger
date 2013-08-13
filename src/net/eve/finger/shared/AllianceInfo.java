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
 * DTO for alliance records.
 * 
 */
public class AllianceInfo implements IsSerializable
{
	/**
	 * Holds the DB ID of this record.
	 */
	private int id;
	
	/**
	 * Alliance ID
	 */
	private long allianceID;
	
	/**
	 * Alliance name.
	 */
	private String allianceName;
	
	/**
	 * Needed access to view this alliance record
	 */
	private int neededAccess;
	
	/**
	 * Notes on the alliance
	 */
	private String notes;
	
	/**
	 * Default constructor.
	 */
	public AllianceInfo()
	{
		
	}
	
	/**
	 * Inits fields.	
	 * @param id The DB ID to set.
	 * @param allianceID The alliance ID to set.
	 * @param allianceName The alliance name to set.
	 * @param neededAccess The needed access to set.
	 * @param notes THe notes to set.
	 */
	public AllianceInfo(int id, long allianceID, String allianceName, int neededAccess, String notes)
	{
		this.id = id;
		this.allianceID = allianceID;
		this.allianceName = allianceName;
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
	 * @return the allianceName
	 */
	public String getAllianceName()
	{
		return allianceName;
	}

	/**
	 * @param allianceName the allianceName to set
	 */
	public void setAllianceName(String allianceName)
	{
		this.allianceName = allianceName;
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
