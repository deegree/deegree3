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
package org.deegree.ogcwebservices.wms.operation;

import java.util.Map;

import org.deegree.i18n.Messages;
import org.deegree.ogcwebservices.InconsistentRequestException;

/**
 * <code>DescribeLayer</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class DescribeLayer extends WMSRequestBase {

    private static final long serialVersionUID = 3600055196281010553L;

    private String[] layers;

    /**
     * Creates a new DescribeLayer object.
     *
     * @param version
     * @param id
     * @param vendorSpecificParameter
     */
    private DescribeLayer( String version, String id, Map<String, String> vendorSpecificParameter, String[] layers ) {
        super( version, id, vendorSpecificParameter );
        this.layers = layers;
    }

    /**
     * @param map
     * @return the new describe layer request
     * @throws InconsistentRequestException
     */
    public static DescribeLayer create( Map<String, String> map )
                            throws InconsistentRequestException {
        String id = map.get( "ID" );
        map.remove( "ID" );
        String version = map.get( "VERSION" );
        map.remove( "VERSION" );

        if ( version == null ) {
            throw new InconsistentRequestException( Messages.getMessage( "WMS_PARAMETER_MUST_BE_SET", "VERSION" ) );
        }

        String ls = map.get( "LAYERS" );
        if ( ls == null ) {
            throw new InconsistentRequestException( Messages.getMessage( "WMS_PARAMETER_MUST_BE_SET", "LAYERS" ) );
        }

        String[] layers = ls.split( "," );
        if ( layers.length == 0 ) {
            throw new InconsistentRequestException( Messages.getMessage( "WMS_PARAMETER_MUST_BE_SET", "LAYERS" ) );
        }

        return new DescribeLayer( version, id, map, layers );
    }

    /**
     * @return the list of requested layers
     */
    public String[] getLayers() {
        return layers;
    }

}
