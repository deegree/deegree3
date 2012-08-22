// $HeadURL:
// /cvsroot/deegree/src/org/deegree/ogcwebservices/csw/configuration/CatalogConfiguration.java,v
// 1.6 2004/07/12 12:23:21 tf Exp $
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

package org.deegree.ogcwebservices.csw.configuration;

import java.io.IOException;
import java.net.URL;

import org.deegree.framework.xml.InvalidConfigurationException;
import org.deegree.model.filterencoding.capabilities.FilterCapabilities;
import org.deegree.ogcwebservices.csw.capabilities.CatalogueCapabilities;
import org.deegree.ogcwebservices.csw.capabilities.EBRIMCapabilities;
import org.deegree.ogcwebservices.getcapabilities.Contents;
import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.getcapabilities.ServiceProvider;
import org.xml.sax.SAXException;

/**
 * Represents the configuration for a deegree CSW 2.0 instance.
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */
public class CatalogueConfiguration extends CatalogueCapabilities {

    private static final long serialVersionUID = 8811280848785864001L;

    private CatalogueDeegreeParams catalogDeegreeParams;

    private URL systemId;

    /**
     * Creates a catalog configuration from an URL
     *
     * @param url
     * @return the new instance
     * @throws IOException
     * @throws SAXException
     * @throws InvalidConfigurationException
     */
    public static CatalogueConfiguration createConfiguration( URL url )
                            throws IOException, SAXException, InvalidConfigurationException {
        CatalogueConfigurationDocument confDoc = new CatalogueConfigurationDocument();
        confDoc.load( url );
        CatalogueConfiguration configuration = confDoc.getConfiguration();
        return configuration;
    }

    /**
     * Generates a new CatalogConfiguration instance from the given parameters.
     *
     * @param version
     * @param updateSequence
     *            Optional value of service metadata document version, value is increased whenever
     *            any change is made in complete service metadata document. The value must be
     *            character string type, not empty.
     * @param serviceIdentification
     * @param serviceProvider
     * @param operationsMetadata
     * @param contents
     * @param filterCapabilities
     * @param catalogDeegreeParams
     * @param systemId
     */
    CatalogueConfiguration( String version, String updateSequence, ServiceIdentification serviceIdentification,
                            ServiceProvider serviceProvider, OperationsMetadata operationsMetadata, Contents contents,
                            FilterCapabilities filterCapabilities, CatalogueDeegreeParams catalogDeegreeParams,
                            URL systemId ) {
        super( version, updateSequence, serviceIdentification, serviceProvider, operationsMetadata, contents,
               filterCapabilities );
        this.catalogDeegreeParams = catalogDeegreeParams;

        this.systemId = systemId;

    }

    /**
     * Generates a new CatalogConfiguration instance from the given parameters.
     *
     * @param version
     * @param updateSequence
     *            Optional value of service metadata document version, value is increased whenever
     *            any change is made in complete service metadata document. The value must be
     *            character string type, not empty.
     * @param serviceIdentification
     * @param serviceProvider
     * @param operationsMetadata
     * @param contents
     * @param filterCapabilities
     * @param catalogDeegreeParams
     * @param systemId
     * @param ebrimCaps
     *            the specified ebrim extensions
     */
    CatalogueConfiguration( String version, String updateSequence, ServiceIdentification serviceIdentification,
                            ServiceProvider serviceProvider, OperationsMetadata operationsMetadata, Contents contents,
                            FilterCapabilities filterCapabilities, CatalogueDeegreeParams catalogDeegreeParams,
                            URL systemId, EBRIMCapabilities ebrimCaps ) {
        super( version, updateSequence, serviceIdentification, serviceProvider, operationsMetadata, contents,
               filterCapabilities, ebrimCaps );
        this.catalogDeegreeParams = catalogDeegreeParams;

        this.systemId = systemId;

    }

    /**
     * Returns the <code>deegreeParams</code> -section of the configuration.
     *
     * @return the <code>deegreeParams</code> -section of the configuration.
     */
    public CatalogueDeegreeParams getDeegreeParams() {
        return catalogDeegreeParams;
    }

    /**
     * Sets the <code>deegreeParams</code> -section of the configuration.
     *
     * @param deegreeParams
     */
    public void setDeegreeParams( CatalogueDeegreeParams deegreeParams ) {
        this.catalogDeegreeParams = deegreeParams;
    }

    /**
     * @return a most important systemId, needed for almost everything
     */
    public URL getSystemId() {
        return systemId;
    }

}
