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

import java.net.URL;

/**
 * Encapsulates information on the identification of, and the means of communication with
 * the person/party responsible for the server.
 * <p>
 * Data model has been carefully designed to capture the expressiveness of all OWS
 * specifications and versions and was verified for the following specifications:
 * <ul>
 * <li>OWS Common 2.0</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 */
public class ContactInfo {

	private Telephone phone;

	private Address address;

	private URL onlineResource;

	private String hoursOfService;

	private String contactInstructions;

	/**
	 * Returns the telephone numbers.
	 * <p>
	 * From OWS Common 2.0: <cite>Telephone numbers at which the organization or
	 * individual may be contacted.</cite>
	 * </p>
	 * @return telephone numbers, may be <code>null</code>
	 */
	public Telephone getPhone() {
		return phone;
	}

	/**
	 * @param phone
	 */
	public void setPhone(Telephone phone) {
		this.phone = phone;
	}

	/**
	 * Returns the physical and email address information.
	 * <p>
	 * From OWS Common 2.0: <cite>Physical and email address at which the organization or
	 * individual may be contacted.</cite>
	 * </p>
	 * @return physical and email address information, may be <code>null</code>
	 */
	public Address getAddress() {
		return address;
	}

	/**
	 * @param address
	 */
	public void setAddress(Address address) {
		this.address = address;
	}

	/**
	 * Returns the online resource.
	 * <p>
	 * From OWS Common 2.0: <cite>Reference to on-line resource from which data can be
	 * obtained.</cite>
	 * </p>
	 * @return online resource, may be <code>null</code>
	 */
	public URL getOnlineResource() {
		return onlineResource;
	}

	/**
	 * @param onlineResource
	 */
	public void setOnlineResource(URL onlineResource) {
		this.onlineResource = onlineResource;
	}

	/**
	 * Returns the hours of service.
	 * <p>
	 * From OWS Common 2.0: <cite>Time period (including time zone) when individuals can
	 * contact the organization or individual.</cite>
	 * </p>
	 * @return hours of service, may be <code>null</code>
	 */
	public String getHoursOfService() {
		return hoursOfService;
	}

	/**
	 * @param hoursOfService
	 */
	public void setHoursOfService(String hoursOfService) {
		this.hoursOfService = hoursOfService;
	}

	/**
	 * Returns the contact instructions.
	 * <p>
	 * From OWS Common 2.0: <cite>Supplemental instructions on how or when to contact the
	 * individual or organization.</cite>
	 * </p>
	 * @return contact instructions, may be <code>null</code>
	 */
	public String getContactInstruction() {
		return contactInstructions;
	}

	/**
	 * @param contactInstructions
	 */
	public void setContactInstructions(String contactInstructions) {
		this.contactInstructions = contactInstructions;
	}

}
