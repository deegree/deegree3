/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.commons.ows.metadata.party;

import org.deegree.commons.tom.ows.CodeType;

/**
 * Encapsulates information on the identification of, and the means of communication with
 * the person/party responsible for the server. <br/>
 * Data model has been carefully designed to capture the expressiveness of all OWS
 * specifications and versions and was verified for the following specifications:
 * <ul>
 * <li>OWS Common 2.0</li>
 * </ul>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 */
public class ResponsibleParty {

	private String individualName;

	private String positionName;

	private String organizationName;

	private ContactInfo contactInfo;

	private CodeType role;

	/**
	 * Returns the name of the responsible person.
	 * <p>
	 * From OWS Common 2.0: <cite>Name of the responsible person: surname, given name,
	 * title separated by a delimiter.</cite>
	 * </p>
	 * @return name of the responsible person, may be <code>null</code>
	 */
	public String getIndividualName() {
		return individualName;
	}

	/**
	 * @param individualName
	 */
	public void setIndividualName(String individualName) {
		this.individualName = individualName;
	}

	/**
	 * Returns the position of the responsible person.
	 * <p>
	 * From OWS Common 2.0: <cite>Role or position of the responsible person.</cite>
	 * </p>
	 * @return position of the responsible person, may be <code>null</code>
	 */
	public String getPositionName() {
		return positionName;
	}

	/**
	 * @param positionName
	 */
	public void setPositionName(String positionName) {
		this.positionName = positionName;
	}

	/**
	 * Returns the name of the responsible organization.
	 * <p>
	 * From OWS Common 2.0: <cite>Name of the responsible organization.</cite>
	 * </p>
	 * @return name of the responsible organization, may be <code>null</code>
	 */
	public String getOrganizationName() {
		return organizationName;
	}

	/**
	 * @param organizationName
	 */
	public void setOrganizationName(String organizationName) {
		this.organizationName = organizationName;
	}

	/**
	 * Returns the address of the responsible party.
	 * <p>
	 * From OWS Common 2.0: <cite>Address of the responsible party.</cite>
	 * </p>
	 * @return address of the responsible party, may be <code>null</code>
	 */
	public ContactInfo getContactInfo() {
		return contactInfo;
	}

	/**
	 * @param contactInfo
	 */
	public void setContactInfo(ContactInfo contactInfo) {
		this.contactInfo = contactInfo;
	}

	/**
	 * Returns the function of the responsible party.
	 * <p>
	 * From OWS Common 2.0: <cite>Function performed by the responsible party. Possible
	 * values of this Role shall include the values and the meanings listed in Subclause
	 * B.5.5 of ISO 19115:2003.</cite>
	 * </p>
	 * @return unction of the responsible party, may be <code>null</code>
	 */
	public CodeType getRole() {
		return role;
	}

	/**
	 * @param role
	 */
	public void setRole(CodeType role) {
		this.role = role;
	}

}
