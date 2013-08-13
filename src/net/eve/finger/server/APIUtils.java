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

import java.util.Collection;

import com.beimin.eveapi.core.ApiException;
import com.beimin.eveapi.eve.character.ApiCharacterLookup;
import com.beimin.eveapi.eve.character.CharacterLookupParser;
import com.beimin.eveapi.eve.character.CharacterLookupResponse;

public class APIUtils 
{
	/**
	 * Converts a name to char ID using the EVE API.
	 * @param name The name to convert.
	 * @return The char ID, or -1 if there is no single ID for the given name.
	 */
	public static long nameToID(String name)
	{
		try
		{
			// Get pilot char ID from the EVE API
			CharacterLookupParser parser = CharacterLookupParser.getName2IdInstance();
		    CharacterLookupResponse response = parser.getResponse(name);
		    Collection<ApiCharacterLookup> chars = response.getAll();
		    
		    // We should only get one
		    if (chars.size() == 1)
		    {
		    	ApiCharacterLookup charInfo = chars.iterator().next();		    	
		    	long charID = charInfo.getCharacterID();
		    	
		    	// Valid character?
		    	if (charID != 0)
		    	{
		    		return charInfo.getCharacterID();
		    	}
		    	else
		    	{
		    		return -1;
		    	}
		    }
		    else
		    {
		    	return -1;
		    }
		}
		catch (ApiException ex)
		{
			return -1;			
		}
	}

}
