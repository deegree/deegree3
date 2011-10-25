//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.metadata.provider;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.protocol.ows.metadata.ServiceIdentification;
import org.deegree.protocol.ows.metadata.ServiceProvider;
import org.deegree.services.metadata.OWSMetadataProvider;
import org.w3c.dom.Element;

/**
 * {@link OWSMetadataProvider} implementation that is a simple bean that stores the metadata.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */
public class DefaultOWSMetadataProvider implements OWSMetadataProvider {

    private ServiceIdentification serviceIdentification;

    private ServiceProvider serviceProvider;

    private Map<String, List<Element>> extendedCapabilities;

    private final Map<QName, String> dataMetadataUrls;

    public DefaultOWSMetadataProvider( ServiceIdentification si, ServiceProvider sp,
                                       Map<String, List<Element>> extendedCapabilities,
                                       Map<QName, String> dataMetadataUrls ) {
        this.serviceIdentification = si;
        this.serviceProvider = sp;
        this.extendedCapabilities = extendedCapabilities;
        this.dataMetadataUrls = dataMetadataUrls;
    }

    @Override
    public void init( DeegreeWorkspace workspace )
                            throws ResourceInitException {
        // nothing to init
    }

    @Override
    public void destroy() {
        // nothing to release
    }

    @Override
    public ServiceIdentification getServiceIdentification() {
        return serviceIdentification;
    }

    @Override
    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    @Override
    public Map<String, List<Element>> getExtendedCapabilities() {
        return extendedCapabilities;
    }

    @Override
    public String getDataMetadataUrl( QName name ) {
        return dataMetadataUrls.get( name );
    }

}
