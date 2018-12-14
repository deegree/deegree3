//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.remoteows.wmts;

import java.net.URL;

import org.deegree.remoteows.RemoteOWS;
import org.deegree.remoteows.RemoteOWSProvider;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

/**
 * {@link RemoteOWSProvider} for {@link RemoteWMTS}.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class RemoteWMTSProvider extends RemoteOWSProvider {

    private static final URL CONFIG_SCHEMA = RemoteWMTSProvider.class.getResource( "/META-INF/schemas/remoteows/wmts/3.4.0/remotewmts.xsd" );

    private static final String CONFIG_NAMESPACE = "http://www.deegree.org/remoteows/wmts";

    @Override
    public String getNamespace() {
        return CONFIG_NAMESPACE;
    }

    @Override
    public ResourceMetadata<RemoteOWS> createFromLocation( Workspace workspace, ResourceLocation<RemoteOWS> location ) {
        return new RemoteWmtsMetadata( workspace, location, this );
    }

    @Override
    public URL getSchema() {
        return CONFIG_SCHEMA;
    }

}
