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
package org.deegree.commons.ows.metadata;

import org.deegree.commons.ows.metadata.party.ResponsibleParty;

/**
 * Encapsulates service provider metadata reported by an OGC web service.
 * <p>
 * Data model has been designed to capture the expressiveness of all OWS specifications
 * and versions and was verified against the following specifications:
 * <ul>
 * <li>OWS Common 2.0</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:wanhoff@lat-lon.de">Jeronimo Wanhoff</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 */
public class ServiceProvider {

	private String providerName;

	private String providerSite;

	private final ResponsibleParty serviceContact;

	/**
	 * Creates a new {@link ServiceProvider} instance.
	 * @param providerName
	 * @param providerSite
	 * @param serviceContact
	 */
	public ServiceProvider(String providerName, String providerSite, ResponsibleParty serviceContact) {
		this.providerName = providerName;
		this.providerSite = providerSite;
		this.serviceContact = serviceContact;
	}

	/**
	 * Returns the reported service provider name.
	 * <p>
	 * From OWS Common 2.0: <cite>A unique identifier for the service provider
	 * organization.</cite>
	 * </p>
	 * @return providerName, may be <code>null</code>
	 */
	public String getProviderName() {
		return providerName;
	}

	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}

	/**
	 * Returns the URL of the provider site.
	 * <p>
	 * From OWS Common 2.0: <cite>Reference to the most relevant web site of the service
	 * provider.</cite>
	 * </p>
	 * @return providerSite, may be <code>null</code>.
	 */
	public String getProviderSite() {
		return providerSite;
	}

	public void setProviderSite(String providerSite) {
		this.providerSite = providerSite;
	}

	/**
	 * Returns the information for contacting the service provider.
	 * <p>
	 * From OWS Common 2.0: <cite>Information for contacting the service provider. The
	 * OnlineResource element within this ServiceContact element should not be used to
	 * reference a web site of the service provider.</cite>
	 * </p>
	 * @return information for contacting the service provider, may be <code>null</code>
	 */
	public ResponsibleParty getServiceContact() {
		return serviceContact;
	}

}
