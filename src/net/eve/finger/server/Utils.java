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
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility functions.
 * 
 */
public class Utils 
{
	/**
	 * Creates a MySQL DateTime string from the given Java date. 
	 * @param date The date to convert.
	 * @return The date as a MySQL DateTime string.
	 */
	public static String getMySQLDT(Date date)
	{
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");		
		return fmt.format(date);
	}
	
	/**
	 * Closes a statement, suppressing any
	 * exceptions, and handling null values.
	 * @param rs The statement to close.
	 */
	public static void closeStatement(Statement stmt)
	{
		if (stmt != null)
		{
			try
			{
				stmt.close();				
			}
			catch (SQLException ex)
			{
				// Do nothing
			}
		}		
	}
	
	/**
	 * Closes a result set, suppressing any
	 * exceptions, and handling null values.
	 * @param rs The result set to close.
	 */
	public static void closeResultSet(ResultSet rs)
	{
		if (rs != null)
		{
			try
			{
				rs.close();				
			}
			catch (SQLException ex)
			{
				// Do nothing
			}
		}		
	}
	
	/**
	 * Closes a DB connection, suppressing any
	 * exceptions, and handling null values.
	 * @param conn The connection to close.
	 */
	public static void closeConnection(Connection conn)
	{
		if (conn != null)
		{
			try
			{
				conn.close();				
			}
			catch (SQLException ex)
			{
				// Do nothing
			}
		}
	}
	
}
