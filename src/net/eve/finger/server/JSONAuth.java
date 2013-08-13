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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Auth provider for a JSON Auth API
 *
 */
public class JSONAuth implements AuthProvider 
{	
	/**
	 * Auths users with the TEST API
	 */
	HttpClient httpClient = null;
	
	public JSONAuth()
	{
		// Create a thread safe HTTPS client
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(
		         new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
		ClientConnectionManager cm = new ThreadSafeClientConnManager(schemeRegistry);
		httpClient = new DefaultHttpClient(cm);		
	}
	
	@Override
	public Boolean authUser(Connection conn, String username, String password) 
	{
		// Build the URL
		StringBuilder url = new StringBuilder();
		url.append("https://auth.example.com/api/login?user=");
		url.append(username);
		url.append("&pass=");
		url.append(password);
		
		// Create the request		
		HttpGet authReq = new HttpGet(url.toString());
		
		// DB var
		PreparedStatement stmt = null;
		
		try
		{
			// Make the request to the JSON API
			String response = EntityUtils.toString(httpClient.execute(authReq).getEntity());
			JSONObject authInfo = new JSONObject(response);
			
			// Was the auth successful?
			if (authInfo.getString("auth").compareToIgnoreCase("ok") == 0)
			{
				// Yes, get their groups
				JSONArray rawGroups = authInfo.getJSONArray("groups");
				
				// In a format that is useful to us
				Map<String, Boolean> groups = new HashMap<String, Boolean>();
				
				// Convert the groups
				JSONObject temp;
				int groupCount = rawGroups.length();
				for (int i = 0; i < groupCount; i++)
				{
					temp = rawGroups.getJSONObject(i);
					groups.put(temp.getString("name"), temp.getBoolean("admin"));					
				}
				
				// The group that will eventually be assigned to the user
				String groupToAssign;
				
				// Figure out what group to give them
				if (groups.containsKey("Alliance Director"))
				{
					groupToAssign = "Director";					
				}
				else if (groups.containsKey("FC") || 
					     groups.containsKey("Cap. FC") ||
					     groups.containsKey("Wing Cmdrs."))
				{
					groupToAssign = "Fleet Cmdrs.";
				}
				else if (groups.containsKey("Scouts") || 
						 groups.containsKey("Junior FCs"))
				{
					groupToAssign = "Scouts";					
				}
                else
                {
                    groupToAssign = "No Access";
                }
				
				// Insert/update the user				
				stmt = conn.prepareStatement("INSERT INTO tblUsers (username, accessGroup) " +
											 "VALUES (?, (SELECT id FROM tblAccessGroups WHERE name = ?)) " +
											 "ON DUPLICATE KEY UPDATE accessGroup = VALUES(accessGroup);");
				stmt.setString(1, username);
				stmt.setString(2, groupToAssign);
				stmt.execute();
				
				// Success, clean up
				Utils.closeStatement(stmt);
				return true;				
			}
			else
			{
				// No, go away
			}			
		} 
		catch (ClientProtocolException ex) 
		{
			// TODO Auto-generated catch block
			ex.printStackTrace();			
		} 
		catch (IOException ex)
		{
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} 
		catch (JSONException ex) 
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
		Utils.closeStatement(stmt);
		return false;
	}
}
