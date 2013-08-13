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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.CharEncoding;

/**
 * Reference implementation of an auth provider.
 * 
 * If you would like to use this provider, the password
 * checking process is:
 * 
 *   1. Client side: SHA1 hash -> hex string
 *   2. Server side: hex string -> SHA256 hash -> hex string
 *   3. String equality test on the pwHash column in tblUsers
 *
 */
public class InternalDBAuth implements AuthProvider 
{
	/**
	 * Password hasher.
	 * Not thread safe.
	 */
	private MessageDigest SHA256;
	
	/**
	 * Provides hex <---> string services.
	 * Thread safe.
	 */
	private Hex hexManager = new Hex(CharEncoding.US_ASCII);
	
	/**
	 * Default constructor.
	 */
	public InternalDBAuth()
	{
		// Setup the password hasher
		try
		{
			SHA256 = MessageDigest.getInstance("SHA-256");
		}
		catch (NoSuchAlgorithmException ex)
		{
			SHA256 = null;
			ex.printStackTrace();
		}	
	}
	
	/**
	 * Checks a user against the built in DB.
	 * 	
	 * Note that non thread safe fields are used from
	 * synchronized blocks.
	 */
	@SuppressWarnings("static-access")
	@Override
	public Boolean authUser(Connection conn, String username, String password)
	{
		// DB vars
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			// Hash the user's PW
			byte[] pwHashBytes = null;
			synchronized (SHA256) 
			{
				pwHashBytes = SHA256.digest(hexManager.decodeHex(password.toCharArray()));				
			}
			
			// Since the hex manager methods are static, and given
			// that I've read the source, this is thread safe.
			// (The class instance only changes a final variable in the
			//  constructor.)
			String pwHash = new String(hexManager.encodeHex(pwHashBytes));
			
			// Check the user's password						
			stmt = conn.prepareStatement(
					"SELECT id, accessGroup, lastLogin FROM tblUsers WHERE username = ? AND pwHash = ?;");
			stmt.setString(1, username);
			stmt.setString(2, pwHash);
			rs = stmt.executeQuery();
			
			// Is their password valid?
			if (rs.next())
			{
				// Yes, clean up
				Utils.closeResultSet(rs);
				Utils.closeStatement(stmt);
				return true;
			}
		}
		catch (DecoderException ex) 
		{
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} 
		catch (SQLException ex) 
		{
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}		
		
		// Failure, clean up
		Utils.closeResultSet(rs);
		Utils.closeStatement(stmt);
		return false;
	}

}
