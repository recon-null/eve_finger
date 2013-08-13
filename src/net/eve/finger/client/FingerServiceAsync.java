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

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface FingerServiceAsync {

	void doLogin(String username, String password,
				 AsyncCallback<LoginResult> callback);	

	void doLogout(String sessionID, AsyncCallback<Boolean> callback);

	void isSessionValid(String sessionID, AsyncCallback<LoginResult> callback);

	void getCharInfoByName(String sessionID, String charName,
			AsyncCallback<CharInfo> callback);

	void updateCharRecordUsrData(String sessionID, CharInfo charInfo,
			AsyncCallback<Boolean> callback);

	void getCharNotes(String sessionID, long charID,
			AsyncCallback<List<CharNote>> callback);

	void updateCharNote(String sessionID, CharNote note,
			AsyncCallback<Integer> callback);

	void deleteCharNote(String sessionID, CharNote note,
			AsyncCallback<Boolean> callback);

	void getAllianceChars(String sessionID, long allianceID, CharType charType,
			AsyncCallback<List<CharInfo>> callback);

}
