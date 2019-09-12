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
package org.deegree.spring.bootstrap;

import java.net.URL;

import org.deegree.spring.ApplicationContextHolder;
import org.deegree.spring.ApplicationContextHolderProvider;

import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

/** 
 * BootstrapApplicationContextHolderProvider provides a
 * {@link org.deegree.spring.bootstrap.BootstrapApplicationContextHolderMetadata} object
 * which is subsequently used to construct a 
 * {@link org.deegree.spring.bootstrap.BootstrapApplicationContextHolderBuilder}.  
 * 
 * @author <a href="mailto:reijer.copier@idgis.nl">Reijer Copier</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class BootstrapApplicationContextHolderProvider extends ApplicationContextHolderProvider {

    private static final String CONFIG_NS = "http://www.deegree.org/spring/bootstrap";

    static final URL CONFIG_SCHEMA = BootstrapApplicationContextHolderProvider.class.getResource( "/META-INF/schemas/spring/3.4.0/bootstrap.xsd" );

    @Override
    public String getNamespace() {
        return CONFIG_NS;
    }

    @Override
    public ResourceMetadata<ApplicationContextHolder> createFromLocation( Workspace workspace,
                                                                          ResourceLocation<ApplicationContextHolder> location ) {
        return new BootstrapApplicationContextHolderMetadata( workspace, location, this );
    }

    @Override
    public URL getSchema() {
        return CONFIG_SCHEMA;
    }

}
