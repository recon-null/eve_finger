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

/**
 * Adds the following minor automation for handling RPC callbacks:
 * Automatically shows an error if the RPC call fails by default.
 * Does nothing by default if an RPC call succeeds.
 * @param <T>
 */
public abstract class AsyncCall<T> implements com.google.gwt.user.client.rpc.AsyncCallback<T> 
{
	@Override
	public void onFailure(Throwable caught) 
	{
		Utils.showRPCError();		
	}

	@Override
	public void onSuccess(T result) 
	{
		// By default, do nothing.		
	}
}
