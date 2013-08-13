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
 * DTO for character notes.
 *
 */
public class CharNote implements IsSerializable
{
	/**
	 * Holds the DB ID
	 */
	private int id;
	
	/**
	 * Character this note is associated with
	 */
	private long charID;
	
	/**
	 * The access needed to perform CRUD on this note.
	 */
	private int neededAccess;
	
	/**
	 * Who added the note (DB user ID)
	 */
	private int addedBy;
	
	/**
	 * Who added the note (name)
	 */
	private String addedByName;
	
	/**
	 * The note itself
	 */
	private String notes;
	
	/**
	 * Default constructor.
	 */
	public CharNote()
	{
		this.id = -1;		
	}
	
	/**
	 * Inits fields.
	 * @param id The DB ID to set.
	 * @param charID The character ID to set.
	 * @param neededAccess The access ID to set.
	 * @param addedBy The owner's DB ID to set.
	 * @param addedByName The owner's name to set.
	 * @param notes The note itself to set.
	 */
	public CharNote(int id, long charID, int neededAccess, int addedBy,
					String addedByName, String notes) 
	{
		this.id = id;
		this.charID = charID;
		this.neededAccess = neededAccess;
		this.addedBy = addedBy;
		this.addedByName = addedByName;
		this.notes = notes;
	}
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the charID
	 */
	public long getCharID() {
		return charID;
	}
	/**
	 * @param charID the charID to set
	 */
	public void setCharID(long charID) {
		this.charID = charID;
	}
	/**
	 * @return the neededAccess
	 */
	public int getNeededAccess() {
		return neededAccess;
	}
	/**
	 * @param neededAccess the neededAccess to set
	 */
	public void setNeededAccess(int neededAccess) {
		this.neededAccess = neededAccess;
	}
	/**
	 * @return the addedBy
	 */
	public int getAddedBy() {
		return addedBy;
	}
	/**
	 * @param addedBy the addedBy to set
	 */
	public void setAddedBy(int addedBy) {
		this.addedBy = addedBy;
	}
	/**
	 * @return the addedByName
	 */
	public String getAddedByName() {
		return addedByName;
	}
	/**
	 * @param addedByName the addedByName to set
	 */
	public void setAddedByName(String addedByName) {
		this.addedByName = addedByName;
	}
	/**
	 * @return the notes
	 */
	public String getNotes() {
		return notes;
	}
	/**
	 * @param notes the notes to set
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}
}
