/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2013 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.remoteows.wms;

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;

import org.deegree.remoteows.RemoteOWS;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.AbstractResourceMetadata;
import org.deegree.workspace.standard.AbstractResourceProvider;

/**
 * Metadata implementation for remote WMS.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * 
 * @since 3.4
 */
public class RemoteWmsMetadata extends AbstractResourceMetadata<RemoteOWS> {

    private static final String CONFIG_JAXB_PACKAGE = "org.deegree.remoteows.wms_new.jaxb";

    public RemoteWmsMetadata( Workspace workspace, ResourceLocation<RemoteOWS> location,
                              AbstractResourceProvider<RemoteOWS> provider ) {
        super( workspace, location, provider );
    }

    @Override
    public ResourceBuilder<RemoteOWS> prepare() {
        try {
            org.deegree.remoteows.wms_new.jaxb.RemoteWMS cfg;
            cfg = (org.deegree.remoteows.wms_new.jaxb.RemoteWMS) unmarshall( CONFIG_JAXB_PACKAGE, provider.getSchema(),
                                                                             location.getAsStream(), workspace );
            return new RemoteWmsBuilder( cfg, this );
        } catch ( Exception e ) {
            throw new ResourceInitException( "Unable to prepare " + location.getIdentifier() + ": "
                                             + e.getLocalizedMessage(), e );
        }
    }

}
