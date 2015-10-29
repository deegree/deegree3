//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
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
package org.deegree.protocol.wfs.getfeature.kvp;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import org.deegree.protocol.wfs.WFSVersion;
import org.deegree.protocol.wfs.getfeature.GetFeature;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class GetFeature200KVPEncoder {

    public Map<String, String> export( GetFeature getFeature ) {
        if ( !WFSVersion.WFS_200.getOGCVersion().equals( getFeature.getVersion() ) ) {
            String failure = "Serialization of other versions than 2.0.0 are currently not supported by this encoder!";
            throw new IllegalArgumentException( failure );
        }
        Map<String, String> kvp = new LinkedHashMap<String, String>();
        exportBaseParams( kvp );
        return kvp;
    }

    private void exportBaseParams( Map<String, String> kvp ) {
        kvp.put( "SERVICE", "WFS" );
        kvp.put( "VERSION", "2.0.0" );
        kvp.put( "REQUEST", "GetFeature" );
    }

}