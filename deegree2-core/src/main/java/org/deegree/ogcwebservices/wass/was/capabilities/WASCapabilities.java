//$HeadURL$
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

package org.deegree.ogcwebservices.wass.was.capabilities;

import java.util.ArrayList;

import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.getcapabilities.ServiceProvider;
import org.deegree.ogcwebservices.wass.common.OWSCapabilitiesBaseType_1_0;
import org.deegree.ogcwebservices.wass.common.OperationsMetadata_1_0;
import org.deegree.ogcwebservices.wass.common.SupportedAuthenticationMethod;

/**
 * Encapsulates: GDI NRW WAS capabilities according to V1.0
 *
 * Namespace: http://www.gdi-nrw.org/was
 *
 * This class does not really contain any special data and is only used for consistent interface
 * reasons.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */
public class WASCapabilities extends OWSCapabilitiesBaseType_1_0 {

    private static final long serialVersionUID = 4049719938261335584L;

    /**
     * Constructs new one from given values.
     *
     * @param version
     * @param updateSequence
     * @param serviceIdentification
     * @param serviceProvider
     * @param operationsMetadata
     * @param supportedAuthenticationMethods
     */
    public WASCapabilities( String version, String updateSequence,
                           ServiceIdentification serviceIdentification,
                           ServiceProvider serviceProvider, OperationsMetadata_1_0 operationsMetadata,
                           ArrayList<SupportedAuthenticationMethod> supportedAuthenticationMethods ) {
        super( version, updateSequence, serviceIdentification, serviceProvider, operationsMetadata,
               supportedAuthenticationMethods );
    }

}
