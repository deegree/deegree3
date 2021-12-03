//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2013 by:

 IDgis bv

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

 IDgis bv
 Boomkamp 16
 7461 AX Rijssen
 The Netherlands
 http://idgis.nl/ 

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
package org.deegree.spring.services;

import java.net.URL;

import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.services.metadata.provider.OWSMetadataProviderProvider;
import org.deegree.spring.GenericSpringResourceMetadata;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

/** 
 * SpringOWSMetadataProviderProvider enables a bean implementing
 * {@link org.deegree.services.metadata.OWSMetadataProvider}
 * to be used within a deegree workspace.
 * 
 * @author <a href="mailto:reijer.copier@idgis.nl">Reijer Copier</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SpringOWSMetadataProviderProvider extends OWSMetadataProviderProvider {

    private static final String CONFIG_NS = "http://www.deegree.org/spring/metadata";

    private static final URL CONFIG_SCHEMA = SpringOWSMetadataProviderProvider.class.getResource( "/META-INF/schemas/spring/3.4.0/metadata.xsd" );

    private static final String CONFIG_JAXB_PACKAGE = "org.deegree.spring.metadata.jaxb";

    @Override
    public String getNamespace() {
        return CONFIG_NS;
    }

    @Override
    public ResourceMetadata<OWSMetadataProvider> createFromLocation( Workspace workspace,
                                                                     ResourceLocation<OWSMetadataProvider> location ) {

        return new GenericSpringResourceMetadata<OWSMetadataProvider>( workspace, location, this, CONFIG_JAXB_PACKAGE, OWSMetadataProvider.class );
    }

    @Override
    public URL getSchema() {
        return CONFIG_SCHEMA;
    }
}
