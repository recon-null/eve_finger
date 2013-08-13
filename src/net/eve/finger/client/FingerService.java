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

import net.eve.finger.shared.*;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Allows users to maintain their session, and perform CRUD
 * operations.
 * 
 */
@RemoteServiceRelativePath("finger_remote")
public interface FingerService extends RemoteService
{
	/**
	 * Handles client login
	 * @param username The username.
	 * @param password The prehashed password.
	 * @return If the login was successful, static data, null otherwise.
	 */
	public LoginResult doLogin(String username, String password);
	
	/**
	 * Handles client logout
	 * @param sessionID The session ID to logout.
	 * @return If the session ID was successfully logged out.
	 */
	public Boolean doLogout(String sessionID);
	
	/**
	 * Checks to see if a given session ID is still valid.
	 * @param sessionID The session ID to check.
	 * @return Static data if the session is valid, null otherwise.
	 */
	public LoginResult isSessionValid(String sessionID);
	
	/**
	 * Gets the characters that are either associated with or
	 * members of the given alliance that match the given character type.
	 * @param sessionID The session ID to use.
	 * @param allianceID The alliance ID to select from.
	 * @param charType The types of characters to select.
	 * @return A list of characters that match the given parameters,
	 * 		   or null if a problem occurred.
	 */
	public List<CharInfo> getAllianceChars(String sessionID, long allianceID, CharType charType);
	
	/**
	 * Gets character info by name.
	 * @param sessionID The session ID.
	 * @param charName The pilot name.
	 * @return The character's info, or null if there is no such character.
	 */	
	public CharInfo getCharInfoByName(String sessionID, String charName);
	
	/**
	 * Updates user controlled data for an existing
	 * character record.
	 * @param sessionID The session ID to use.
	 * @param charInfo The character to update.
	 * @return If the update was successful.
	 */
	public Boolean updateCharRecordUsrData(String sessionID, CharInfo charInfo);
	
	/**
	 * Gets notes for the given character ID.
	 * @param sessionID The session ID to use.
	 * @param charID The character ID to get notes for.
	 * @return The notes for the given character ID.
	 * 		   Will return null if an error occurred,
	 *         or a zero length list if no notes were found.
	 */
	public List<CharNote> getCharNotes(String sessionID, long charID);	

	/**
	 * Updates the given note, or creates a new note when note.id == -1.
	 * @param sessionID The session ID to use.
	 * @param note The note to update/create.
	 * @return The note's ID if successful, -1 otherwise.
	 */
	public Integer updateCharNote(String sessionID, CharNote note);
	
	/**
	 * Deletes the given note.
	 * @param sessionID The session ID to use.
	 * @param note The note to delete.
	 * @return If the deletion was successful.
	 */
	public Boolean deleteCharNote(String sessionID, CharNote note);	
}
