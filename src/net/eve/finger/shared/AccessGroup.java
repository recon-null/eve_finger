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
 * Access groups from the DB are cached in these objects.
 *
 */
public class AccessGroup implements IsSerializable, Comparable<AccessGroup>
{
	/**
	 * The access group ID.
	 */
	private int id;
	
	/**
	 * The access group power.
	 */
	private int power;
	
	/**
	 * The access group name.
	 */
	private String name;
	
	/**
	 * The access group's notes.
	 */
	private String notes;
	
	/**
	 * Default constructor.
	 */
	public AccessGroup()
	{
		
	}
	
	/**
	 * Inits fields.
	 * @param id The ID to set.
	 * @param name The group name to set.
	 * @param power The power to set.
	 * @param notes The notes to set.
	 */
	public AccessGroup(int id, String name, int power, String notes)
	{
		this.id = id;
		this.name = name;		
		this.power = power;	
		this.notes = notes;
	}
	
	/**
	 * Gets a string representation of the object.
	 * Used for debugging purposes.
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("id: ");
		sb.append(id);
		sb.append(", name: ");
		sb.append(name);
		sb.append(", power: ");
		sb.append(power);
		sb.append(", notes: ");
		sb.append(notes);
		return sb.toString();
	}

	/**
	 * @return The ID.
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * @param id The ID to set.
	 */
	public void setId(int id)
	{
		this.id = id;
	}

	/**
	 * @return The group's power.
	 */
	public int getPower() 
	{
		return power;
	}

	/**
	 * @param power The power to set.
	 */
	public void setPower(int power)
	{
		this.power = power;
	}

	/**
	 * @return the name
	 */
	public String getName() 
	{
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) 
	{
		this.name = name;
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

	@Override
	/**
	 * Compares by power.
	 */
	public int compareTo(AccessGroup arg0) 
	{
		int arg0Power = arg0.getPower();
		
		// Handle the comparison
		if (this.power < arg0Power)
		{
			return -1;			
		}
		else if (this.power == arg0Power)
		{
			return 0;
		}
		else
		{
			return 1;
		}
	}
}
