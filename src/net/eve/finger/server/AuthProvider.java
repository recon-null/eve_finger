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

import java.sql.Connection;

/**
 * Authenticator interface to allow for plugable
 * auth systems.
 */
public interface AuthProvider 
{
	/**
	 * Checks a user against an authentication source.
	 * 
	 * Note that the user MUST exist in the internal DB
	 * if this method returns true, so you may want to create
	 * their entry in this method if your auth system doesn't
	 * already handle creating it from external code.
	 * 
	 * You are expected to clean up any DB objects you create.
	 * That being said, the connection will be closed for you.
	 * 
	 * This method MUST be thread safe.
	 * 
	 * @param conn A connection to the internal DB.
	 * @param username The username to check. String guaranteed to exist and be within a sane length.
	 * @param password The password to check, SHA1 hashed and provided as a hex string (done client side).
	 * 				   String guaranteed to exist and be within a sane length.
	 * @return If authentication was successful. 
	 */
	public abstract Boolean authUser(Connection conn, String username, String password);
    
    /*
     * Example Algorithm:
     *      
     *      1. Lookup the user's information in auth source
     *         (the auth source could be a web service, forum database, etc).
     *      
     *      2. If the user was not found, clean up and return false.
     *
     *      3. The user was found, so determine which EVE Finger group they should be in
     *         based on information from the auth source.
     *  
     *      4. Create/update the user's record in the user's table using a statement
     *         similar to the one below. NOTE: You should default the assigned group
     *         to "No Access" so that when someone's access is removed in the auth source,
     *         it will be revoked in EVE Finger on their next login; in other words, if you
     *         look up the user and find no suitable group for them, your code should default
     *         to putting them in the "No Access" group.
     *
     *              INSERT INTO tblUsers (username, accessGroup)
     *              VALUES (?, (SELECT id FROM tblAccessGroups WHERE name = ?))
     *              ON DUPLICATE KEY UPDATE accessGroup = VALUES(accessGroup);
     *
     *      5. Clean up and return true.
     *
     */
}