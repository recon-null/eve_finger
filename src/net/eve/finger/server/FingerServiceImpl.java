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

import net.eve.finger.client.FingerService;
import net.eve.finger.shared.*;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.sql.DataSource;

import com.beimin.eveapi.core.ApiException;
import com.beimin.eveapi.eve.character.CharacterInfoParser;
import com.beimin.eveapi.eve.character.CharacterInfoResponse;

/**
 * Server code for FFE 
 *
 */
@SuppressWarnings("serial")
public class FingerServiceImpl extends RemoteServiceServlet
							   implements FingerService 
{	
	/**
	 * Stores all active sessions by session ID.
	 */
	private final Map<String, Session> activeSessions = new ConcurrentHashMap<String, Session>();
	
	/**
	 * Stores all active sessions by DB user ID.
	 * Used for updating permissions on the fly.
	 */
	private final Map<Integer, Session> userIDToSession = new ConcurrentHashMap<Integer, Session>();
	
	/**
	 * Cache of access groups for quick permission checks.
	 */
	private final Map<Integer, AccessGroup> accessGroupMap = new ConcurrentHashMap<Integer, AccessGroup>();
	
	/**
	 * Cache of visible access groups.
	 */
	private final Map<Integer, AccessGroup> visibleAccessGroupMap = new ConcurrentHashMap<Integer, AccessGroup>();
	
	/**
	 * Cache of alliances in the system.
	 */
	private final Map<Long, AllianceInfo> allianceMap = new ConcurrentHashMap<Long, AllianceInfo>();
	
	/**
	 * The active auth provider
	 */
	private final AuthProvider authProvider;
	
	/**
	 * Removes expired sessions
	 */
	private final Timer sessionPruneTimer = new Timer(true);
	
	/**
	 * A connection to our DB
	 */
	private DataSource dSource;
	
	/**
	 * Creates the service
	 */	
	public FingerServiceImpl()
	{
		// To avoid Date objects ending up with
		// some absurd local timezone and messing
		// up formatting, we'll set the default to GMT.
		//
		// (This is an application for players of EVE Online
		//  so GMT is naturally used for everything.)
		TimeZone.setDefault(TimeZone.getTimeZone("GMT"));	

		dSource = getDataSource();
		
		authProvider = getAuthProvider();

		// Prune tables, load data, etc
		performDBStartupTasks();
				
		// Start the session pruner timer (every 12 hrs, delete expired sessions)
		sessionPruneTimer.schedule(new TimerTask() 
			{
				@Override
				public void run() 
				{
					// We don't want sessions to get removed
					// by other threads (e.g. RPC calls) while we're pruning
					synchronized (activeSessions) 
					{	
						List<Session> sessionsToRemove = new LinkedList<Session>();
						Collection<Session> sessions = activeSessions.values();
						
						// Find all expired sessions
						for (Session s : sessions)
						{
							if (!s.isValid())
							{
								sessionsToRemove.add(s);
							}
						}				
	
						// Remove the expired sessions
						sessions.removeAll(sessionsToRemove);					
					}
				}
			}, 3600000 * 12, 3600000 * 12);		
	}

	/**
	 * Handles client login
	 * @param username The username.
	 * @param password The prehashed password. 
	 * @return If the login was successful, static data, null otherwise.
	 */
	@Override	
	public LoginResult doLogin(String username, String password) 
	{		
		// Make sure we actually got a username and
		// password
		if (username == null || password == null)
		{
			return null;
		}
		
		// Input sanity check. This stuff needs to be "reasonable."
		int usernameLength = username.length();
		int passwordLength = password.length();
		if (!(usernameLength > 3 && usernameLength < 60 && passwordLength > 3 && passwordLength < 100))
		{
			return null;
		}
		
		// DB vars
		Connection conn = null;
		PreparedStatement stmt = null;
		PreparedStatement setLastLogin = null;
		ResultSet rs = null;
		
		try
		{					
			// Get a DB connection
			conn = dSource.getConnection();
			
			// Auth the user
			if (!authProvider.authUser(conn, username, password))
			{
				// Username/pw didn't check out
				Utils.closeConnection(conn);
				return null;
			}
			
			// Get the user's info from the DB
			stmt = conn.prepareStatement(
					"SELECT id, accessGroup, lastLogin FROM tblUsers WHERE username = ?;");
			stmt.setString(1, username);			
			rs = stmt.executeQuery();
			
			// Does such a user exist?
			if (rs.next())
			{				
				// Yes, get their information
				int userID = rs.getInt(1);
				int accessGroup = rs.getInt(2);
				
				// FIXME: SECURITY: Do they already have a session? If so, they shouldn't be logging in again
				
				// Handle null last login dates
				Date lastLogin = null;
				Timestamp rawLastLogin = rs.getTimestamp(3);
				if (rawLastLogin != null)
				{
					lastLogin = new Date(rawLastLogin.getTime());
				}				
				
				// Set their last login date
				setLastLogin = conn.prepareStatement("UPDATE tblUsers SET lastLogin = ? WHERE id = ?;");
				setLastLogin.setString(1, Utils.getMySQLDT(new Date()));
				setLastLogin.setInt(2, userID);
				setLastLogin.execute();
				
				// Done with the DB
				Utils.closeResultSet(rs);
				Utils.closeStatement(stmt);
				Utils.closeStatement(setLastLogin);
				Utils.closeConnection(conn);
				
				// Generate a session ID
				String sessionID = UUID.randomUUID().toString();
				
				// Create a session for the user
				addSession(new Session(username, userID, sessionID, accessGroup, null));
				return (new LoginResult(userID, sessionID, visibleAccessGroupMap, allianceMap, accessGroup,
										accessGroupMap.get(accessGroup).getPower(), lastLogin));						
			}
			else
			{
				// Clean up
				Utils.closeResultSet(rs);
				Utils.closeStatement(stmt);
				Utils.closeConnection(conn);				
				
				// Invalid username or password
				return null;
			}
		}
		catch (SQLException ex)
		{
			// DB error of some kind
			ex.printStackTrace();			
		}				
		
		// Failure, clean up		
		Utils.closeResultSet(rs);
		Utils.closeStatement(stmt);
		Utils.closeStatement(setLastLogin);
		Utils.closeConnection(conn);
		return null;		
	}	
	

	/**
	 * Handles client logout
	 * @param sessionID The session ID to logout.
	 * @return If the session ID was successfully logged out.
	 */
	@Override
	public Boolean doLogout(String sessionID)
	{		
		// Get the session, if it's valid
		Session session = getSessionIfValid(sessionID);
		
		// Does the session exist?
		if (session != null)
		{
			// Yes, log it out
			removeSession(session);
			return true;			
		}
		else
		{
			// Session not logged in. This should never happen.
			return false;
		}		
	}
	
	
	/**
	 * Checks to see if a given session ID is still valid.
	 * @param sessionID The session ID to check.
	 * @return Static data if the session is valid, null otherwise.
	 */
	@Override
	public LoginResult isSessionValid(String sessionID) 
	{		
		// Try to get the session
		Session session = getSessionIfValid(sessionID);
		
		// Is there one?
		if (session != null)
		{
			// DB vars
			Connection conn = null;
			PreparedStatement getLastLogin = null;
			ResultSet rs = null;
			
			// Yes
			try 
			{
				// Get the user's last login date
				conn = dSource.getConnection();
				getLastLogin = conn.prepareStatement(
						"SELECT id, lastLogin FROM tblUsers WHERE id = ?;");
				getLastLogin.setInt(1, session.getUserID());			
				rs = getLastLogin.executeQuery();	
				
				// Does this user still exist in the DB?
				if (rs.next())
				{
					// Yes
					int userID = rs.getInt(1);
					Timestamp lastLogin = rs.getTimestamp(2);
					
					// Done with the DB
					Utils.closeResultSet(rs);
					Utils.closeStatement(getLastLogin);
					Utils.closeConnection(conn);
					
					// Get their access group
					int accessGroup = session.getAccessGroup();
					
					// Do they have a last login date?
					if (lastLogin != null)
					{
						// Yes
						return (new LoginResult(userID, sessionID, visibleAccessGroupMap, allianceMap,
												accessGroup, accessGroupMap.get(accessGroup).getPower(),
												new Date(lastLogin.getTime())));						
					}
					else
					{
						// No last login date
						return (new LoginResult(userID, sessionID, visibleAccessGroupMap, allianceMap,
												accessGroup, accessGroupMap.get(accessGroup).getPower(), null));	
					}
				}
				else
				{
					// They don't, forget this session
					removeSession(session);					
				}				
			} 
			catch (SQLException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();				
			}
			
			// Done with the DB
			Utils.closeResultSet(rs);
			Utils.closeStatement(getLastLogin);
			Utils.closeConnection(conn);
			
			// Session didn't exist or a query failed
			return null;
			
		}
		else
		{
			// No such valid session
			return null;
		}
	}	
	
	/**
	 * Gets the characters that are either associated with or
	 * members of the given alliance that match the given character type.
	 * @param sessionID The session ID to use.
	 * @param allianceID The alliance ID to select from.
	 * @param charType The types of characters to select.
	 * @return A list of characters that match the given parameters,
	 * 		   or null if a problem occurred.
	 */
	@Override
	public List<CharInfo> getAllianceChars(String sessionID, long allianceID,
										   CharType charType) 
	{
		// Try to get the session
		Session session = getSessionIfValid(sessionID);		
		if (session == null)
		{
			// If the user isn't logged in, we don't care
			// about this request
			return null;
		}
		
		// Create the basic query
		// NOTE: Normalized
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT chars.id AS id, charName, charID, corp.allianceID AS allianceID, " +
						  "alliance.allianceName AS allianceName, " +
						  "chars.corpID, corp.corpName, " +
						  "assocWithAlliance, assocA.allianceName AS assocWithAllianceName, " +
						  "timezone, chars.neededAccess, " +
						  "isSuper, isTitan, isFC, isScout, isCyno, cachedUntil " +
				   "FROM tblCharacters chars " +
				   "INNER JOIN tblAccessGroups ag ON chars.neededAccess = ag.id " +
				   "INNER JOIN tblCorps corp ON chars.corpID = corp.corpID " +
				   "INNER JOIN tblAlliances alliance ON corp.allianceID = alliance.allianceID " +
				   "LEFT JOIN tblAlliances assocA ON chars.assocWithAlliance = assocA.allianceID " +
				   "WHERE (corp.allianceID = ? OR chars.assocWithAlliance = ?) AND ag.power <= ? AND ");
		
		// Add the parameter from charType
		switch (charType)
		{
			case FC:
				sql.append("isFC = TRUE");
				break;
		
			case SCOUT:
				sql.append("isScout = TRUE");
				break;
				
			case CYNO:
				sql.append("isCyno = TRUE");
				break;
				
			case SUPER:
				sql.append("isSuper = TRUE");
				break;
				
			case TITAN:
				sql.append("isTitan = TRUE");
				break;		
		}
		sql.append(";");
		
		// DB vars
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			// Run the query			
			conn = dSource.getConnection();
			stmt = conn.prepareStatement(sql.toString());
			stmt.setLong(1, allianceID);
			stmt.setLong(2, allianceID);
			stmt.setInt(3, accessGroupMap.get(session.getAccessGroup()).getPower());
			rs = stmt.executeQuery();
			
			// Holds the characters once retrieved from the DB
			List<CharInfo> chars = new LinkedList<CharInfo>();
			
			// Temp var
			CharInfo charInfo;			
			
			while (rs.next())
			{	
				charInfo = new CharInfo();
				
				// Populate the info object
				charInfo.setCharName(rs.getString("charName"));
				charInfo.setCharID(rs.getLong("charID"));
				charInfo.setAllianceID(rs.getLong("allianceID"));
				charInfo.setAllianceName(rs.getString("allianceName"));
				charInfo.setAssocWithAlliance(rs.getLong("assocWithAlliance"));					
				charInfo.setAssocWithAllianceName(rs.getString("assocWithAllianceName"));					
				charInfo.setCorpID(rs.getLong("corpID"));
				charInfo.setCorpName(rs.getString("corpName"));
				charInfo.setId(rs.getInt("id"));
				charInfo.setIsCyno(rs.getBoolean("isCyno"));
				charInfo.setIsFC(rs.getBoolean("isFC"));
				charInfo.setIsScout(rs.getBoolean("isScout"));
				charInfo.setIsSuper(rs.getBoolean("isSuper"));
				charInfo.setIsTitan(rs.getBoolean("isTitan"));
				charInfo.setTimezone(rs.getString("timezone"));
				charInfo.setNeededAccess(rs.getInt("neededAccess"));
				charInfo.setCachedUntil(new Date(rs.getTimestamp("cachedUntil").getTime()));
				
				chars.add(charInfo);
			}
			
			// Clean up
			Utils.closeResultSet(rs);
			Utils.closeStatement(stmt);
			Utils.closeConnection(conn);
			
			// Return the list of characters
			return chars;			
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();						
		}
		
		// Failure, clean up
		Utils.closeResultSet(rs);
		Utils.closeStatement(stmt);
		Utils.closeConnection(conn);
		return null;		
	}
	
	
	/**
	 * Gets character info by name.
	 * @param sessionID The session ID.
	 * @param charName The pilot name.
	 * @return The character's info, or null if there is no such character.
	 */
	@Override
	public CharInfo getCharInfoByName(String sessionID, String charName)
	{
		// Try to get the session
		Session session = getSessionIfValid(sessionID);		
		if (session == null)
		{
			// If the user isn't logged in, we don't care
			// about this request
			return null;
		}
		
		// Make sure we actually got a character name
		if (charName != null)
		{
			// And it's in the valid range
			int charNameLen = charName.length();			
			if (!(charNameLen >= 4 && charNameLen <= 24))
			{
				return null;
			}			
		}
		else
		{
			return null;
		}
		
		// Get the char ID
		// TODO: Try to get this from the DB to avoid
		// 		 unnecessary API calls
		long charID = APIUtils.nameToID(charName);
		
		// Is there such a character?
		if (charID != -1)
		{
			// DB vars
			Connection conn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			
			try 
			{
				// Will be returned once populated
				CharInfo charInfo = new CharInfo();
				charInfo.setCharID(charID);	
				
				// Try to find the char in the DB
				// NOTE: Normalized
				conn = dSource.getConnection();
				stmt = conn.prepareStatement(							
						   "SELECT chars.id AS id, charName, charID, corp.allianceID AS allianceID, " +
								  "alliance.allianceName AS allianceName, " +
								  "chars.corpID, corp.corpName, " +
								  "assocWithAlliance, assocA.allianceName AS assocWithAllianceName, " +
								  "timezone, chars.neededAccess, " +
								  "isSuper, isTitan, isFC, isScout, isCyno, cachedUntil " +
						   "FROM tblCharacters chars " +
						   "INNER JOIN tblAccessGroups ag ON chars.neededAccess = ag.id " +
						   "INNER JOIN tblCorps corp ON chars.corpID = corp.corpID " +
						   "INNER JOIN tblAlliances alliance ON corp.allianceID = alliance.allianceID " +
						   "LEFT JOIN tblAlliances assocA ON chars.assocWithAlliance = assocA.allianceID " +
						   "WHERE chars.charID = ?");
				
				stmt.setLong(1, charID);
				rs = stmt.executeQuery();
				
				// Are they in the DB?
				if (rs.next())
				{
					// Is this user allowed to view this char record?
					int neededAccess = rs.getInt("neededAccess");					

					if (accessGroupMap.get(session.getAccessGroup()).getPower() 
							< accessGroupMap.get(neededAccess).getPower())
					{
						throw new Exception("User not allowed to view this char.");
					}
					
					// How long is the info valid for?
					Date cachedUntil = new Date(rs.getTimestamp("cachedUntil").getTime());
					
					// Populate the info object
					charInfo.setCharName(rs.getString("charName"));
					charInfo.setAllianceID(rs.getLong("allianceID"));
					charInfo.setAllianceName(rs.getString("allianceName"));
					charInfo.setAssocWithAlliance(rs.getLong("assocWithAlliance"));					
					charInfo.setAssocWithAllianceName(rs.getString("assocWithAllianceName"));					
					charInfo.setCorpID(rs.getLong("corpID"));
					charInfo.setCorpName(rs.getString("corpName"));
					charInfo.setId(rs.getInt("id"));
					charInfo.setIsCyno(rs.getBoolean("isCyno"));
					charInfo.setIsFC(rs.getBoolean("isFC"));
					charInfo.setIsScout(rs.getBoolean("isScout"));
					charInfo.setIsSuper(rs.getBoolean("isSuper"));
					charInfo.setIsTitan(rs.getBoolean("isTitan"));
					charInfo.setTimezone(rs.getString("timezone"));
					charInfo.setNeededAccess(neededAccess);
					charInfo.setCachedUntil(cachedUntil);
					
					// Clean up
					Utils.closeResultSet(rs);
					Utils.closeStatement(stmt);
					Utils.closeConnection(conn);
					
					// Does the info need to be refreshed?					
					if ((new Date()).after(cachedUntil))
					{
						// Yes, refresh it
						if (populateAPIInfo(charInfo) && updateCharRecord(charInfo))
						{
							return charInfo;
						}
						else
						{
							// API or DB error
							throw new Exception("API or DB error refreshing the character.");							
						}						
					}
					else
					{
						// Data is current, so send it to the client
						return charInfo;
					}
				}
				else
				{
					// Char is not in the DB yet					

					// Clean up the DB stuff
					Utils.closeResultSet(rs);
					Utils.closeStatement(stmt);
					Utils.closeConnection(conn);				
					
					// Populate the info object with default values
					charInfo.setAssocWithAlliance(0);
					charInfo.setAssocWithAllianceName(null);					
					charInfo.setIsCyno(false);
					charInfo.setIsFC(false);
					charInfo.setIsScout(false);
					charInfo.setIsSuper(false);
					charInfo.setIsTitan(false);
					charInfo.setTimezone(null);
					charInfo.setNeededAccess(session.getAccessGroup());
				
					// Populate API info for this char and put
					// it in the DB
					if (populateAPIInfo(charInfo) && updateCharRecord(charInfo))
					{
						return charInfo;
					}
					else
					{
						// API or DB error
						throw new Exception("API or DB error creating the character.");							
					}												
				}				
		
			} 
			catch (SQLException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();				
			}
			catch (Exception ex)
			{
				// TODO: Log this				
			}
			
			// Failure, clean up
			Utils.closeResultSet(rs);
			Utils.closeStatement(stmt);
			Utils.closeConnection(conn);
			return null;
		}
		else
		{
			// No such character
			return null;
		}
	}	
	
	/**
	 * Gets notes for the given character ID.
	 * @param sessionID The session ID to use.
	 * @param charID The character ID to get notes for.
	 * @return The notes for the given character ID.
	 * 		   Will return null if an error occurred,
	 *         or a zero length list if no notes were found.
	 */
	@Override
	public List<CharNote> getCharNotes(String sessionID, long charID) 
	{				
		// DB vars
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			// Try to get the session
			Session session = getSessionIfValid(sessionID);		
			if (session == null)
			{
				// If the user isn't logged in, we don't care
				// about this request
				throw new Exception("User not logged in.");
			}		
			
			// Stores the results
			List<CharNote> results = new LinkedList<CharNote>();
			
			conn = dSource.getConnection();

			// Only return notes that the user has permission to view
			stmt = conn.prepareStatement(
					"SELECT cn.id, cn.charID, cn.neededAccess, ag.power, cn.addedBy, users.username, cn.notes " +
					"FROM tblCharNotes cn " +
					"INNER JOIN tblAccessGroups ag ON cn.neededAccess = ag.id " +
					"INNER JOIN tblUsers users ON cn.addedBy = users.id " +
					"WHERE cn.charID = ? AND ag.power <= ?;");
			stmt.setLong(1, charID);
			stmt.setInt(2, SharedUtils.getGroupPowerByID(accessGroupMap, session.getAccessGroup()));
			
			// Run the query
			rs = stmt.executeQuery();
			
			// Create DTOs for the rows
			while (rs.next())
			{
				results.add(new CharNote(rs.getInt(1), rs.getLong(2), rs.getInt(3),
										 rs.getInt(5), rs.getString(6), rs.getString(7)));				
			}		
			
			// Clean up
			Utils.closeResultSet(rs);
			Utils.closeStatement(stmt);
			Utils.closeConnection(conn);
			
			// Return the DTOs
			return results;
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();						
		}
		catch (Exception ex)
		{
			// TODO: Log this
		}
		
		// Failure, clean up
		Utils.closeResultSet(rs);
		Utils.closeStatement(stmt);
		Utils.closeConnection(conn);
		return null;
	}
	
	/**
	 * Updates the given note, or creates a new note when note.id == -1.
	 * @param sessionID The session ID to use.
	 * @param note The note to update/create.
	 * @return The note's ID if successful, -1 otherwise.
	 */
	@Override
	public Integer updateCharNote(String sessionID, CharNote note)
	{
		// DB vars
		Connection conn = null;		
		PreparedStatement accessCheck = null;
		ResultSet rs = null;
		PreparedStatement updateNote = null;
		PreparedStatement createNote = null;
		PreparedStatement logEntry = null;
		
		try
		{			
			// Try to get the session
			Session session = getSessionIfValid(sessionID);		
			if (session == null)
			{
				// If the user isn't logged in, we don't care
				// about this request
				throw new Exception("User not logged in.");
			}
			
			// Make sure the note is not null
			if (note == null)
			{
				throw new Exception("note was null.");
			}
			
			// Get the note's ID
			int noteID = note.getId();
			
			// Get a connection to the DB
			conn = dSource.getConnection();
			
		
			// If we're updating the note
			if (noteID != -1)
			{
				// Is the user allowed to update this note?				
				accessCheck = conn.prepareStatement(
						"SELECT ag.power AS power FROM tblCharNotes notes " +
						"INNER JOIN tblAccessGroups ag ON notes.neededAccess = ag.id " +
						"WHERE notes.id = ? AND notes.addedBy = ?;");
				
				accessCheck.setInt(1, noteID);
				accessCheck.setInt(2, session.getUserID());
				rs = accessCheck.executeQuery();
				int neededUpdateAccess;
				
				// There better be a result here
				if (rs.next())
				{
					neededUpdateAccess = rs.getInt(1);					
				}
				else
				{
					// Either the note doesn't exist or the user doesn't own it
					// TODO: Log possible hack attempt					
					throw new Exception("Note doesn't exist or user doesn't own it.");
				}
				
				// If the user's power is less than the needed power, they aren't allowed
				// to edit this character (and shouldn't be able to view it anyway).
				if (SharedUtils.getGroupPowerByID(accessGroupMap, session.getAccessGroup()) < neededUpdateAccess)
				{
					// No
					throw new Exception("User not allowed to edit this note.");			
				}
				
				// Permission checks passed. Update the note.
				updateNote = conn.prepareStatement(
						"UPDATE tblCharNotes " +
						"SET neededAccess = ?, notes = ? " +
						"WHERE id = ?;");				
				updateNote.setInt(1, note.getNeededAccess());
				updateNote.setString(2, note.getNotes());
				updateNote.setInt(3, noteID);
				updateNote.execute();				
						
				// Update successful, log it
				logEntry = conn.prepareStatement("INSERT INTO tblLog (userID, created, type, message) " +
												"VALUES (?, ?, ?, ?);");
				logEntry.setInt(1, session.getUserID());
				logEntry.setString(2, Utils.getMySQLDT(new Date()));
				logEntry.setString(3, "INFO");
				logEntry.setString(4, String.format("User '%s' updated note with id '%d'. It now reads '%s'.",
							  					   session.getUsername(), noteID, note.getNotes()));
				logEntry.execute();
				
				// Clean up
				Utils.closeResultSet(rs);				
				Utils.closeStatement(updateNote);
				Utils.closeStatement(accessCheck);
				Utils.closeStatement(logEntry);
				Utils.closeConnection(conn);
				
				// Return the note's ID to indicate success
				return noteID;
			}
			else
			{
				// Creating a new note				
				
				// Does this user have permission to create a note for this character?
				long charID = note.getCharID();
				if (!canSessionUpdateChar(session, charID))
				{
					// Nope
					throw new Exception("User not allowed to create a note for this char.");			
				}
				
				// Build the note
				createNote = conn.prepareStatement(
						"INSERT INTO tblCharNotes (charID, neededAccess, addedBy, notes) " +
						"VALUES (?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
				createNote.setLong(1, charID);
				createNote.setInt(2, note.getNeededAccess());
				createNote.setInt(3, session.getUserID());
				createNote.setString(4, note.getNotes());
				
				// Insert the note
				createNote.execute();
				
				// Get the note's ID
				rs = createNote.getGeneratedKeys();
				if (rs.next())
				{
					noteID = rs.getInt(1);			
									
					// Note creation successful, log it
					logEntry = conn.prepareStatement("INSERT INTO tblLog (userID, created, type, message) " +
													"VALUES (?, ?, ?, ?);");
					logEntry.setInt(1, session.getUserID());
					logEntry.setString(2, Utils.getMySQLDT(new Date()));
					logEntry.setString(3, "INFO");
					logEntry.setString(4, String.format("User '%s' created note with id '%d' containing text '%s'.",
							  					   session.getUsername(), noteID, note.getNotes()));
					logEntry.execute();

					// Clean up
					Utils.closeResultSet(rs);
					Utils.closeStatement(createNote);
					Utils.closeStatement(logEntry);
					Utils.closeConnection(conn);
					
					// Return the note's ID for the client
					// to attach to the record
					return noteID;					
				}
				else
				{
					// Not sure what would cause this, but it's
					// a failure case					
					throw new Exception("Note insertion failed.");
				}		
			}			
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();			
		}
		catch (Exception ex)
		{
			// TODO: Log this			
		}
		
		// Failure, clean up
		Utils.closeResultSet(rs);
		Utils.closeStatement(createNote);
		Utils.closeStatement(updateNote);
		Utils.closeStatement(accessCheck);
		Utils.closeStatement(logEntry);
		Utils.closeConnection(conn);		
		return -1;
	}
	
	/**
	 * Deletes the given note.
	 * @param sessionID The session ID to use.
	 * @param note The note to delete.
	 * @return If the deletion was successful.
	 */
	@Override
	public Boolean deleteCharNote(String sessionID, CharNote note) 
	{
		// DB vars
		Connection conn = null;
		PreparedStatement accessCheck = null;
		ResultSet rs = null;
		PreparedStatement deleteNote = null;
		PreparedStatement logEntry = null;
		
		try
		{	
			// Get the session
			Session session = getSessionIfValid(sessionID);		
			if (session == null)
			{
				// Sorry, you can't do that without logging in
				throw new Exception("User not logged in.");
			}
			
			// Make sure the note is not null
			if (note == null)
			{
				throw new Exception("note was null.");
			}		
			
			int noteID = note.getId();
			
			// Is the user allowed to update this note?
			conn = dSource.getConnection();
			accessCheck = conn.prepareStatement
					("SELECT ag.power AS power FROM tblCharNotes notes " +
					 "INNER JOIN tblAccessGroups ag ON notes.neededAccess = ag.id " +
					 "WHERE notes.id = ?");		
			accessCheck.setInt(1, noteID);
			rs = accessCheck.executeQuery();
			int neededDeleteAccess;
			
			// There better be a result here
			if (rs.next())
			{
				neededDeleteAccess = rs.getInt(1);
				rs.close();
			}
			else
			{
				// Trying to delete a non existent note?
				// TODO: Log possible hack attempt
				throw new Exception("Trying to delete a non existant note.");
			}
			
			// If the user's power is less than the needed power, they aren't allowed
			// to delete this note (and shouldn't be able to view it anyway).
			if (SharedUtils.getGroupPowerByID(accessGroupMap, session.getAccessGroup()) < neededDeleteAccess)
			{
				// No
				throw new Exception("User not allowed to delete this note.");			
			}
			
			// The user has access to delete the note, 
			// so let's take care of it
			deleteNote = conn.prepareStatement("DELETE FROM tblCharNotes WHERE id = ?");
			deleteNote.setInt(1, noteID);
			deleteNote.execute();
			
			// And log it
			logEntry = conn.prepareStatement("INSERT INTO tblLog (userID, created, type, message) " +
											 "VALUES (?, ?, ?, ?);");
			logEntry.setInt(1, session.getUserID());
			logEntry.setString(2, Utils.getMySQLDT(new Date()));
			logEntry.setString(3, "INFO");
			logEntry.setString(4, String.format("User '%s' deleted note with id '%d'. The note contained '%s'.",
			  					   session.getUsername(), noteID, note.getNotes()));
			logEntry.execute();
			
			// Clean up			
			Utils.closeResultSet(rs);
			Utils.closeStatement(accessCheck);
			Utils.closeStatement(deleteNote);
			Utils.closeStatement(logEntry);
			Utils.closeConnection(conn);
			
			return true;
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();						
		}	
		catch (Exception ex)
		{
			// TODO: Log this
		}
		
		// Failure, clean up
		Utils.closeResultSet(rs);
		Utils.closeStatement(accessCheck);
		Utils.closeStatement(deleteNote);
		Utils.closeStatement(logEntry);
		Utils.closeConnection(conn);
		return false;
	}
	
	/**
	 * Updates user controlled data for an existing
	 * character record. Unlike updateCharRecord, this
	 * function will not update data fields populated by the API.
	 * @param sessionID The session ID to use.
	 * @param charInfo The character to update.
	 * @return If the update was successful.
	 */
	@Override
	public Boolean updateCharRecordUsrData(String sessionID, CharInfo charInfo)
	{		
		// DB vars
		Connection conn = null;
		PreparedStatement updateStmt = null;
		PreparedStatement logEntry = null;
		
		try
		{
			// Get the session
			Session session = getSessionIfValid(sessionID);		
			if (session == null)
			{
				// Sorry, you can't do that without logging in
				throw new Exception("User not logged in.");
			}
			
			// Make sure charInfo is not null
			if (charInfo == null)
			{
				throw new Exception("charInfo was null.");
			}
		
			// Get the char ID to update
			long charID = charInfo.getCharID();			
			
			// Is the user allowed to update this character?
			if (!canSessionUpdateChar(session, charID))
			{
				// No
				throw new Exception("User is not allowed to update this char.");	
			}
			
			// They have access, so let's perform the update
			// NOTE: Normalized
			conn = dSource.getConnection();
			updateStmt = conn.prepareStatement(
					"UPDATE tblCharacters " +
					"SET assocWithAlliance = ?, timezone = ?, " +
					"neededAccess = ?, isSuper = ?, isTitan = ?, isFC = ?, isScout = ?, isCyno = ? " +
					"WHERE charID = ?;");
			
			// Handle null associations
			long assocWithAlliance = charInfo.getAssocWithAlliance();
			if (assocWithAlliance != 0)
			{
				updateStmt.setLong(1, assocWithAlliance);				
			}
			else
			{
				updateStmt.setNull(1, java.sql.Types.BIGINT);
			}
			
			// Set values			
			updateStmt.setString(2, charInfo.getTimezone());
			updateStmt.setInt(3, charInfo.getNeededAccess());
			updateStmt.setBoolean(4, charInfo.getIsSuper());
			updateStmt.setBoolean(5, charInfo.getIsTitan());
			updateStmt.setBoolean(6, charInfo.getIsFC());
			updateStmt.setBoolean(7, charInfo.getIsScout());
			updateStmt.setBoolean(8, charInfo.getIsCyno());
			updateStmt.setLong(9, charID);
		
			updateStmt.execute();
			
			// Edit successful, log it
			logEntry = conn.prepareStatement("INSERT INTO tblLog (userID, created, type, message) " +
											 "VALUES (?, ?, ?, ?);");
			logEntry.setInt(1, session.getUserID());
			logEntry.setString(2, Utils.getMySQLDT(new Date()));
			logEntry.setString(3, "INFO");
			logEntry.setString(4, String.format("User '%s' edited character '%s' (charID: %d). It now contains: '%s'.",
			  					   				session.getUsername(), charInfo.getCharName(), charInfo.getCharID(),
			  					   				charInfo.toString()));
			logEntry.execute();

			// Clean up
			Utils.closeStatement(updateStmt);
			Utils.closeStatement(logEntry);
			Utils.closeConnection(conn);
			return true;			
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();		
		}
		catch (Exception ex)
		{
			// TODO: Log this
		}
		
		// Failure, clean up
		Utils.closeStatement(updateStmt);
		Utils.closeStatement(logEntry);
		Utils.closeConnection(conn);
		return false;
	}
	
	/**
	 * Determines if the given session can update the given character.
	 * @param session The session to check.
	 * @param charID The charID to check.
	 * @return If the given session can update the given character.
	 */
	private Boolean canSessionUpdateChar(Session session, long charID)
	{
		// DB vars
		Connection conn = null;
		PreparedStatement accessCheck = null;
		ResultSet rs = null;
		
		try
		{
			// Is the user allowed to update this character?
			conn = dSource.getConnection();
			accessCheck = conn.prepareStatement
					("SELECT ag.power AS power FROM tblCharacters chars " +
					 "INNER JOIN tblAccessGroups ag ON chars.neededAccess = ag.id " +
					 "WHERE chars.charID = ?;");			
			accessCheck.setLong(1, charID);
			rs = accessCheck.executeQuery();
			int neededUpdateAccess;
			
			// There better be a result here
			if (rs.next())
			{
				neededUpdateAccess = rs.getInt(1);
			}
			else
			{
				// Trying to update a non existent character?
				// TODO: Log possible hack attempt
				throw new Exception("Attempt to update non existant character.");
			}
			
			// Clean up
			Utils.closeResultSet(rs);
			Utils.closeStatement(accessCheck);
			Utils.closeConnection(conn);
			
			// If the user's power is less than the needed power, they aren't allowed
			// to edit this character (and shouldn't be able to view it anyway).
			return (SharedUtils.getGroupPowerByID(accessGroupMap, session.getAccessGroup()) >= neededUpdateAccess);			
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();			
		}
		catch (Exception ex)
		{
			// TODO: Log this
		}
		
		// Failure, clean up
		Utils.closeResultSet(rs);
		Utils.closeStatement(accessCheck);
		Utils.closeConnection(conn);
		return false;		
	}

	
	/**
	 * Updates the DB record for a given character.
	 * Will create if it doesn't exist.
	 * @param charInfo The character to update.
	 * @return If the update was successful.
	 */
	private Boolean updateCharRecord(CharInfo charInfo)
	{ 
		// DB vars
		Connection conn = null;
		PreparedStatement allianceCheck = null;
		ResultSet rsAllianceCheck = null;
		PreparedStatement corpCheck = null;
		ResultSet rsCorpCheck = null;
		PreparedStatement stmt = null;
		
		try
		{
			// Get the alliance ID
			long allianceID = charInfo.getAllianceID();
			long corpID = charInfo.getCorpID();
			
			// Is this char's alliance in our DB?
			conn = dSource.getConnection();
			allianceCheck = conn.prepareStatement(
					"SELECT allianceID FROM tblAlliances WHERE allianceID = ?;");
			allianceCheck.setLong(1, allianceID);
			rsAllianceCheck = allianceCheck.executeQuery();			
			
			// Well?
			if (!rsAllianceCheck.next())
			{
				// Nope, add the alliance
				AllianceInfo info = new AllianceInfo
						(-1, allianceID, charInfo.getAllianceName(), charInfo.getNeededAccess(), null);
				if (!updateAllianceRecord(info))
				{
					throw new Exception("Unable to update the alliance record.");
				}
			}
			
			// Is this char's corp in our DB?
			corpCheck = conn.prepareStatement(
					"SELECT corpID FROM tblCorps WHERE corpID = ?;");
			corpCheck.setLong(1, corpID);
			rsCorpCheck = corpCheck.executeQuery();
			
			// Well?
			if (!rsCorpCheck.next())
			{
				// Nope, add the corp
				CorpInfo info = new CorpInfo
						(-1, charInfo.getCorpID(), charInfo.getCorpName(), charInfo.getAllianceID(),
						 charInfo.getNeededAccess(), null);
				if (!updateCorpRecord(info))
				{
					throw new Exception("Unable to update the corp record.");
				}
			}
			
			
			// SQL to update/create a character record
			// NOTE: Normalized
			stmt = conn.prepareStatement(
					"INSERT INTO tblCharacters (charName, charID, allianceID, corpID, assocWithAlliance, " +
							   					"timezone, neededAccess, isSuper, isTitan, isFC, isScout, " +
							   					"isCyno, cachedUntil) " +							   
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" +
	
					"ON DUPLICATE KEY UPDATE allianceID = VALUES(allianceID)," +
	                     "corpID = VALUES(corpID), assocWithAlliance = VALUES(assocWithAlliance)," +
	                     "timezone = VALUES(timezone), neededAccess = VALUES(neededAccess)," +
	                     "isSuper = VALUES(isSuper), isTitan = VALUES(isTitan)," +
	                     "isFC = VALUES(isFC), isScout = VALUES(isScout)," +
	                     "isCyno = VALUES(isCyno), cachedUntil = VALUES(cachedUntil);");
			
			// Set values
			stmt.setString(1, charInfo.getCharName());
			stmt.setLong(2, charInfo.getCharID());
			stmt.setLong(3, allianceID);
			stmt.setLong(4, corpID);
			
			// Handle null associations
			long assocWithAlliance = charInfo.getAssocWithAlliance();
			if (assocWithAlliance != 0)
			{
				stmt.setLong(5, assocWithAlliance);				
			}
			else
			{
				stmt.setNull(5, java.sql.Types.BIGINT);
			}			
			
			// Set more values
			stmt.setString(6, charInfo.getTimezone());
			stmt.setInt(7, charInfo.getNeededAccess());
			stmt.setBoolean(8, charInfo.getIsSuper());
			stmt.setBoolean(9, charInfo.getIsTitan());
			stmt.setBoolean(10, charInfo.getIsFC());
			stmt.setBoolean(11, charInfo.getIsScout());
			stmt.setBoolean(12, charInfo.getIsCyno());
			stmt.setTimestamp(13, new java.sql.Timestamp(charInfo.getCachedUntil().getTime()));
			
			// Run the query
			stmt.execute();	
			
			// Clean up
			Utils.closeResultSet(rsAllianceCheck);
			Utils.closeResultSet(rsCorpCheck);
			Utils.closeStatement(allianceCheck);
			Utils.closeStatement(corpCheck);		
			Utils.closeStatement(stmt);	
			Utils.closeConnection(conn);
			return true;
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();			
		}
		catch (Exception ex)
		{
			// TODO: Log this			
		}
		
		// Failure, clean up
		Utils.closeResultSet(rsAllianceCheck);
		Utils.closeResultSet(rsCorpCheck);
		Utils.closeStatement(allianceCheck);
		Utils.closeStatement(corpCheck);		
		Utils.closeStatement(stmt);	
		Utils.closeConnection(conn);
		return false;		
	}
	
	/**
	 * Populates
	 * @param charInfo The character info to populate.
	 * 				   Must have charID set.
	 * @return If the operation was successful.
	 */
	private Boolean populateAPIInfo(CharInfo charInfo)
	{
		try
		{
			// Get the character's info
			CharacterInfoParser parser = CharacterInfoParser.getInstance();
	        CharacterInfoResponse response = parser.getResponse(charInfo.getCharID());
	        
	        // Populate the object
	        charInfo.setCharName(response.getCharacterName());
	        	                
	        // Are they in an alliance?
	        Long allianceID = response.getAllianceID();
	        if (allianceID != null)
	        {
	        	// Yes
	        	charInfo.setAllianceID(allianceID);
	        	charInfo.setAllianceName(response.getAlliance());
	        }
	        else
	        {
	        	// Not in an alliance
	        	charInfo.setAllianceID(0);
	        	charInfo.setAllianceName("None");
	        }
	        
	        charInfo.setCorpID(response.getCorporationID());
	        charInfo.setCorpName(response.getCorporation());
	        
	        // TODO: For some reason, the EVE API library isn't 
	        // 		 pulling the cachedUntil element of the XML doc.
	        // 		 We'll cache for six hours until that gets fixed.
	        Date cachedUntil = new Date();
	        cachedUntil.setTime(cachedUntil.getTime() + 3600000 * 6);	        
	        charInfo.setCachedUntil(cachedUntil);
	        
	        return true;
		}
		catch (ApiException ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Updates a corp record. Will create if it doesn't exist.
	 * @param corpInfo The record to update/create.
	 * @return If the record was successfully updated/created.
	 */
	private Boolean updateCorpRecord(CorpInfo corpInfo)
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try
		{
			// SQL to create/update a corp record
			conn = dSource.getConnection();
			stmt = conn.prepareStatement(
					"INSERT INTO tblCorps (corpID, corpName, allianceID, neededAccess, notes) " +
					"VALUES (?, ?, ?, ?, ?)" +
							
					"ON DUPLICATE KEY UPDATE neededAccess = VALUES(neededAccess), notes = VALUES(notes);");
			
			// Set values
			stmt.setLong(1, corpInfo.getCorpID());
			stmt.setString(2, corpInfo.getCorpName());
			stmt.setLong(3, corpInfo.getAllianceID());
			stmt.setInt(4, corpInfo.getNeededAccess());
			stmt.setString(5, corpInfo.getNotes());
			
			// Execute the query
			stmt.execute();
			
			// Clean up
			Utils.closeStatement(stmt);
			Utils.closeConnection(conn);
			return true;			
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();			
		}		
		
		// Failure, clean up
		Utils.closeStatement(stmt);
		Utils.closeConnection(conn);
		return false;
	}
	
	/**
	 * Updates an alliance record. Will create if it doesn't exist.
	 * @param allianceInfo The record to update/create.
	 * @return If the record was successfully updated/created.
	 */
	private Boolean updateAllianceRecord(AllianceInfo allianceInfo)
	{
		// DB vars
		Connection conn = null;
		PreparedStatement stmt = null;		
		
		try
		{
			// SQL to create/update an alliance record
			conn = dSource.getConnection();
			stmt = conn.prepareStatement(
					"INSERT INTO tblAlliances (allianceID, allianceName, neededAccess, notes) " +
					"VALUES (?, ?, ?, ?)" +
							
					"ON DUPLICATE KEY UPDATE neededAccess = VALUES(neededAccess), notes = VALUES(notes);");
			
			// Set values
			long allianceID = allianceInfo.getAllianceID();
			stmt.setLong(1, allianceID);
			stmt.setString(2, allianceInfo.getAllianceName());
			stmt.setInt(3, allianceInfo.getNeededAccess());
			stmt.setString(4, allianceInfo.getNotes());
			
			// Execute the query
			stmt.execute();
			
			// Update the cache
			if (allianceMap.containsKey(allianceID))
			{
				// Can't have duplicate entries
				allianceMap.remove(allianceID);				
			}
			allianceMap.put(allianceID, allianceInfo);
			
			// Clean up
			Utils.closeStatement(stmt);
			Utils.closeConnection(conn);
			
			// We're done
			return true;			
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();			
		}
		
		// Failure, clean up
		Utils.closeStatement(stmt);
		Utils.closeConnection(conn);
		return false;
	}
	
	
	/**
	 * Performs all app startup DB related tasks.
	 */
	private void performDBStartupTasks()
	{		
		// DB vars
		Connection conn = null;
		Statement stmt = null;
		
		try 
		{
			// Create a statement to run the startup tasks
			conn = dSource.getConnection();
			stmt = conn.createStatement();
			
			// Prune the sessions table
			pruneSessionsTable(stmt);
			
			// Load sessions
			loadSessionsTable(stmt);
			
			// Clear the sessions table
			clearSessionsTable(stmt);			
				
			// Load access groups
			loadAccessGroups(stmt);
			
			// Cache alliances
			loadAlliances(stmt);
		
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Free resources		
		Utils.closeStatement(stmt);
		Utils.closeConnection(conn);
	}
	
	/**
	 * Loads alliances from the DB into the local cache.
	 * @param stmt
	 */
	private void loadAlliances(Statement stmt) 
	{
		// DB var
		ResultSet rs = null;
		
		try
		{			
			// Get the alliances
			rs = stmt.executeQuery("SELECT id, allianceID, allianceName, neededAccess, notes FROM tblalliances;");
			
			// Build the cache
			while(rs.next())
			{
				long allianceID = rs.getLong(2);
				allianceMap.put(allianceID, new AllianceInfo(rs.getInt(1), allianceID,
								rs.getString(3), rs.getInt(4), rs.getString(5)));		
			}
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
		
		// Clean up
		Utils.closeResultSet(rs);
	}

	/**
	 * Loads access groups from the DB into the local cache.
	 * @param stmt The statement to use.
	 */
	private void loadAccessGroups(Statement stmt) 
	{
		// DB var
		ResultSet rs = null;
		
		try
		{
			// Get the access groups
			rs = stmt.executeQuery
					("SELECT id, name, power, notes, visible FROM tblAccessGroups " +					 
					 "ORDER BY power ASC;");			

			int id;
			AccessGroup grp;
			
			// Load the access groups
			while (rs.next())
			{				
				id = rs.getInt(1);
				grp = new AccessGroup(id, rs.getString(2), rs.getInt(3), rs.getString(4));
				accessGroupMap.put(id, grp);
				
				// Groups that are visible to clients
				if (rs.getBoolean(5))
				{
					visibleAccessGroupMap.put(id, grp);
				}
			}			
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}	
				
		// Clean up
		Utils.closeResultSet(rs);
	}

	
	/**
	 * Clears the session table.
	 * @param stmt
	 */
	private void clearSessionsTable(Statement stmt) 
	{
		try 
		{
			// Clear the table
			stmt.execute("DELETE FROM tblSessions;");
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	/**
	 * Saves all sessions in the activeSessions map
	 * to the DB.
	 */
	private void saveSessionsTable()
	{	
		// DB vars
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try 
		{			
			// Create the SQL statement
			conn = dSource.getConnection();
			stmt = conn.prepareStatement(					
					"INSERT INTO tblSessions (sessionID, userID, createdAt) VALUES (?, ?, ?);");	
		
			// Add each session to the batch of commands
			for (Session s : activeSessions.values())
			{
				// Don't save invalid sessions
				if (s.isValid())
				{
					stmt.setString(1, s.getSessionID());
					stmt.setInt(2, s.getUserID());					
					stmt.setString(3, Utils.getMySQLDT(s.getCreatedAt()));				
					stmt.addBatch();
				}
			}
			
			// Execute the SQL statements
			stmt.executeBatch();
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		// Free resources		
		Utils.closeStatement(stmt);
		Utils.closeConnection(conn);
	}

	
	/**
	 * Gets sessions from the DB and loads them into
	 * the activeSessions map.
	 * @param stmt The statement to use for queries.
	 */
	private void loadSessionsTable(Statement stmt) 
	{
		// DB var
		ResultSet rs = null;
		
		try 
		{
			// Get the sessions
			rs = stmt.executeQuery("SELECT username, userID, sessionID, createdAt, accessGroup FROM viewsessionlist;");
			
			// While we have rows to fetch
			while (rs.next())
			{
				// Get the data for this row
				String username = rs.getString(1);
				int userID = rs.getInt(2);
				String sessionID = rs.getString(3);
				Date createdAt = new Date(rs.getTimestamp(4).getTime());				
				int accessGroup = rs.getInt(5);
				
				// Add this session to the activeSessions map 
				addSession(new Session(username, userID, sessionID, accessGroup, createdAt));
			}	
		}
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Clean up
		Utils.closeResultSet(rs);
	}
	
	/**
	 * Prunes the sessions DB table.
	 * @param stmt The statement to use for the query.
	 */	
	private void pruneSessionsTable(Statement stmt)
	{		
		try 
		{
			// Get the max session age
			String maxAge = Utils.getMySQLDT(Session.getMaxAge().getTime());			
		
			// Prune expired sessions that were created before
			// the oldest allowed date
			stmt.execute("DELETE FROM tblSessions WHERE createdAt < '" + maxAge + "';");
		} 
		catch (SQLException e) 
		{
			// TODO: Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the session specified by the given session ID if
	 * it is valid.
	 * @param sessionID The session ID to get.
	 * @return The session, if valid. Null otherwise.
	 */
	private Session getSessionIfValid(String sessionID)
	{
		// Does the session exist?
		if(sessionID != null && sessionID.length() == 36 && activeSessions.containsKey(sessionID))
		{
			// Is it still valid?
			Session s = activeSessions.get(sessionID);
			if (s.isValid())
			{
				// Everything checks out
				return s;
			}
			else
			{
				// Nope, get rid of it
				removeSession(s);
				return null;
			}
		}
		else
		{
			// Session is either invalid (e.g. too long)
			// or doesn't exist in the system
			return null;
		}
	}
	
	/**
	 * Adds a session to the system.
	 * @param s The session to add.
	 */
	private void addSession(Session s)
	{
		activeSessions.put(s.getSessionID(), s);
		userIDToSession.put(s.getUserID(), s);
	}
	
	/**
	 * Removes a session from the system.
	 * @param s The session to remove.
	 */
	private void removeSession(Session s)
	{
		// We don't want to interfere with
		// pruning (e.g. remove a session that is about
		//			to be pruned).
		synchronized (activeSessions) 
		{
			activeSessions.remove(s.getSessionID());
		}
		
		userIDToSession.remove(s.getUserID());		
	}
	
	/**
	 * Gets the auth provider specified in
	 * the servlet's context
	 * @return
	 */
	private AuthProvider getAuthProvider()
	{
		try
		{			
			Context initContext = new InitialContext();
			Context envContext = (Context) initContext.lookup("java:/comp/env");
			return (AuthProvider) envContext.lookup("bean/AuthProviderFactory");			
		}
		catch (NamingException ex)
		{
			// TODO Auto-generated catch block
			ex.printStackTrace();			
		}		
		return null;		
	}
	
	/**
	 * Gets the DataSource for our DB.
	 */
	private DataSource getDataSource()
	{
		try
		{		
			Context initContext = new InitialContext();
			Context envContext  = (Context)initContext.lookup("java:/comp/env");
			return (DataSource)envContext.lookup("jdbc/fingerdb");
					
		}	
		catch (NamingException ex) 
		{
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		
		return null;
				
	}

	@Override
	public void destroy() 
	{
		// If the super class uses this method,
		// let it do so
		super.destroy();
		
		// Save activeSessions to the sessions table
		saveSessionsTable();
	}

}
