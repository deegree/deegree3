/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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
package org.deegree.services.wcs;

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;

import javax.xml.bind.JAXBException;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLProcessingException;
import org.deegree.services.OWS;
import org.deegree.services.OWSProvider;
import org.deegree.services.jaxb.wcs.DeegreeWCS;
import org.deegree.services.jaxb.wcs.ServiceConfiguration;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

/**
 * This is an xml adapter for the deegree WCS ServiceConfiguration.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 */
public class ServiceConfigurationXMLAdapter extends XMLAdapter {

	/**
	 * @return the parsed ServiceConfiguration
	 * @throws XMLProcessingException
	 */
	public static ServiceConfiguration parse(Workspace workspace, ResourceMetadata<OWS> metadata)
			throws XMLProcessingException {
		try {
			DeegreeWCS wcsConf = (DeegreeWCS) unmarshall("org.deegree.services.jaxb.wcs",
					((OWSProvider) metadata.getProvider()).getSchema(), metadata.getLocation().getAsStream(),
					workspace);
			return wcsConf.getServiceConfiguration();
		}
		catch (JAXBException e) {
			throw new XMLProcessingException(e.getMessage(), e);
		}
	}

}
