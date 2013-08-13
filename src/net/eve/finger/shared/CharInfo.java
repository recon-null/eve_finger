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

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * DTO for character data.
 *
 */
public class CharInfo implements IsSerializable 
{
	private int id;
	private String charName;
	private long charID;
	private long allianceID;
	private String allianceName;
	private Date cachedUntil;
	private long corpID;
	private String corpName;
	private long assocWithAlliance;
	private String assocWithAllianceName;
	private String timezone;
	private int neededAccess;
	private Boolean isSuper;
	private Boolean isTitan;
	private Boolean isFC;
	private Boolean isScout;
	private Boolean isCyno;
	
	/**
	 * Default constructor.
	 */
	public CharInfo()
	{
		
	}

	/**
	 * Gets a string representation of the object.
	 */
	@Override
	public String toString() 
	{
		StringBuilder sb = new StringBuilder();
		sb.append("id: ");
		sb.append(id);
		sb.append(", charName: ");
		sb.append(charName);
		sb.append(", charID: ");
		sb.append(charID);
		sb.append(", assocWithAllianceName: ");
		sb.append(assocWithAllianceName);
		sb.append(", assocWithAlliance: ");
		sb.append(assocWithAlliance);
		sb.append(", timezone: ");
		sb.append(timezone);
		sb.append(", neededAccess: ");
		sb.append(neededAccess);
		sb.append(", isSuper: ");
		sb.append(isSuper);
		sb.append(", isTitan: ");
		sb.append(isTitan);
		sb.append(", isFC: ");
		sb.append(isFC);
		sb.append(", isScout: ");
		sb.append(isScout);
		sb.append(", isCyno: ");
		sb.append(isCyno);
		return sb.toString();		
	}
	
	/** 
	 * ****************************************************
	 * Auto-generated getters and setters below this line. 
	 * **************************************************** 
	 */
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the charName
	 */
	public String getCharName() {
		return charName;
	}
	/**
	 * @param charName the charName to set
	 */
	public void setCharName(String charName) {
		this.charName = charName;
	}
	/**
	 * @return the charID
	 */
	public long getCharID() {
		return charID;
	}
	/**
	 * @param charID the charID to set
	 */
	public void setCharID(long charID) {
		this.charID = charID;
	}
	/**
	 * @return the allianceID
	 */
	public long getAllianceID() {
		return allianceID;
	}
	/**
	 * @param allianceID the allianceID to set
	 */
	public void setAllianceID(long allianceID) {
		this.allianceID = allianceID;
	}
	/**
	 * @return the allianceName
	 */
	public String getAllianceName() {
		return allianceName;
	}
	/**
	 * @param allianceName the allianceName to set
	 */
	public void setAllianceName(String allianceName) {
		this.allianceName = allianceName;
	}
	/**
	 * @return the corpID
	 */
	public long getCorpID() {
		return corpID;
	}
	/**
	 * @param corpID the corpID to set
	 */
	public void setCorpID(long corpID) {
		this.corpID = corpID;
	}
	/**
	 * @return the corpName
	 */
	public String getCorpName() {
		return corpName;
	}
	/**
	 * @param corpName the corpName to set
	 */
	public void setCorpName(String corpName) {
		this.corpName = corpName;
	}
	/**
	 * @return the assocWithAlliance
	 */
	public long getAssocWithAlliance() {
		return assocWithAlliance;
	}
	/**
	 * @param assocWithAlliance the assocWithAlliance to set
	 */
	public void setAssocWithAlliance(long assocWithAlliance) {
		this.assocWithAlliance = assocWithAlliance;
	}
	/**
	 * @return the assocWithAllianceName
	 */
	public String getAssocWithAllianceName() {
		return assocWithAllianceName;
	}
	/**
	 * @param assocWithAllianceName the assocWithAllianceName to set
	 */
	public void setAssocWithAllianceName(String assocWithAllianceName) {
		this.assocWithAllianceName = assocWithAllianceName;
	}
	/**
	 * @return the timezone
	 */
	public String getTimezone() {
		return timezone;
	}
	/**
	 * @param timezone the timezone to set
	 */
	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}
	/**
	 * @return the neededAccess
	 */
	public int getNeededAccess() {
		return neededAccess;
	}
	/**
	 * @param neededAccess the neededAccess to set
	 */
	public void setNeededAccess(int neededAccess) {
		this.neededAccess = neededAccess;
	}
	/**
	 * @return the isSuper
	 */
	public Boolean getIsSuper() {
		return isSuper;
	}
	/**
	 * @param isSuper the isSuper to set
	 */
	public void setIsSuper(Boolean isSuper) {
		this.isSuper = isSuper;
	}
	/**
	 * @return the isTitan
	 */
	public Boolean getIsTitan() {
		return isTitan;
	}
	/**
	 * @param isTitan the isTitan to set
	 */
	public void setIsTitan(Boolean isTitan) {
		this.isTitan = isTitan;
	}
	/**
	 * @return the isFC
	 */
	public Boolean getIsFC() {
		return isFC;
	}
	/**
	 * @param isFC the isFC to set
	 */
	public void setIsFC(Boolean isFC) {
		this.isFC = isFC;
	}
	/**
	 * @return the isScout
	 */
	public Boolean getIsScout() {
		return isScout;
	}
	/**
	 * @param isScout the isScout to set
	 */
	public void setIsScout(Boolean isScout) {
		this.isScout = isScout;
	}
	/**
	 * @return the isCyno
	 */
	public Boolean getIsCyno() {
		return isCyno;
	}
	/**
	 * @param isCyno the isCyno to set
	 */
	public void setIsCyno(Boolean isCyno) {
		this.isCyno = isCyno;
	}


	/**
	 * @return the cachedUntil
	 */
	public Date getCachedUntil() {
		return cachedUntil;
	}


	/**
	 * @param cachedUntil the cachedUntil to set
	 */
	public void setCachedUntil(Date cachedUntil) {
		this.cachedUntil = cachedUntil;
	}	

}
