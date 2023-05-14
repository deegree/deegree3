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

import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.Pair;

/**
 * A {@link Description} that encapsulates general server-specific metadata reported by an
 * OGC web service.
 * <p>
 * Data model has been designed to capture the expressiveness of all OWS specifications
 * and versions and was verified against the following specifications:
 * <ul>
 * <li>OWS Common 2.0</li>
 * </ul>
 * </p>
 * <p>
 * From OWS Common 2.0: <cite>General metadata for this specific server. This XML Schema
 * of this section shall be the same for all OWS.</cite>
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 */
public class ServiceIdentification extends Description {

	private CodeType serviceType;

	private List<Version> serviceTypeVersion;

	private List<String> profiles;

	private String fees;

	private List<String> accessConstraints;

	/**
	 * Creates a new {@link ServiceIdentification} instance.
	 * @param name
	 * @param titles
	 * @param abstracts
	 * @param keywords
	 * @param serviceType
	 * @param serviceTypeVersion
	 * @param profiles
	 * @param fees
	 * @param accessConstraints
	 */
	public ServiceIdentification(String name, List<LanguageString> titles, List<LanguageString> abstracts,
			List<Pair<List<LanguageString>, CodeType>> keywords, CodeType serviceType, List<Version> serviceTypeVersion,
			List<String> profiles, String fees, List<String> accessConstraints) {
		super(name, titles, abstracts, keywords);
		this.serviceType = serviceType;
		this.serviceTypeVersion = serviceTypeVersion;
		this.profiles = profiles;
		this.fees = fees;
		this.accessConstraints = accessConstraints;
	}

	/**
	 * Returns the reported service type.
	 * <p>
	 * From OWS Common 2.0: <cite>A service type name from a registry of services. For
	 * example, the values of the codeSpace URI and name and code string may be "OGC" and
	 * "catalogue." This type name is normally used for machine-to-machine
	 * communication.</cite>
	 * </p>
	 * @return reported service type, may be <code>null</code>
	 */
	public CodeType getServiceType() {
		return serviceType;
	}

	/**
	 * @param serviceType
	 */
	public void setServiceType(CodeType serviceType) {
		this.serviceType = serviceType;
	}

	/**
	 * Returns the reported service type versions.
	 * <p>
	 * From OWS Common 2.0: <cite>Unordered list of one or more versions of this service
	 * type implemented by this server. This information is not adequate for version
	 * negotiation, and shall not be used for that purpose.</cite>
	 * </p>
	 * @return reported service type versions, may be empty, but never <code>null</code>
	 */
	public List<Version> getServiceTypeVersion() {
		if (serviceTypeVersion == null) {
			serviceTypeVersion = new ArrayList<Version>();
		}
		return serviceTypeVersion;
	}

	/**
	 * @param serviceTypeVersion the serviceTypeVersion to set
	 */
	public void setServiceTypeVersion(List<Version> serviceTypeVersion) {
		this.serviceTypeVersion = serviceTypeVersion;
	}

	/**
	 * Returns the identifiers of implemented application profiles.
	 * <p>
	 * From OWS Common 2.0: <cite>Unordered list of identifiers of Application Profiles
	 * that are implemented by this server. This element should be included for each
	 * specified application profile implemented by this server. The identifier value
	 * should be specified by each Application Profile. If this element is omitted, no
	 * meaning is implied.</cite>
	 * </p>
	 * @return identifiers of implemented application profiles, may be empty but never
	 * <code>null</code>.
	 */
	public List<String> getProfiles() {
		if (profiles == null) {
			profiles = new ArrayList<String>();
		}
		return profiles;
	}

	/**
	 * @param profiles the profiles to set
	 */
	public void setProfiles(List<String> profiles) {
		this.profiles = profiles;
	}

	/**
	 * @return the fees
	 */
	public String getFees() {
		return fees;
	}

	/**
	 * @param fees
	 */
	public void setFees(String fees) {
		this.fees = fees;
	}

	/**
	 * @return accessConstraints, may be <code>null</code>.
	 */
	public List<String> getAccessConstraints() {
		if (accessConstraints == null) {
			accessConstraints = new ArrayList<String>();
		}
		return accessConstraints;
	}

	/**
	 * @param accessConstraints the accessConstraints to set
	 */
	public void setAccessConstraints(List<String> accessConstraints) {
		this.accessConstraints = accessConstraints;
	}

}
