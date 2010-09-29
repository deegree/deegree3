//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.services.wfs.format;

import java.util.Properties;

import javax.xml.namespace.QName;

import org.deegree.geometry.io.CoordinateFormatter;
import org.deegree.geometry.io.DecimalCoordinateFormatter;
import org.deegree.services.wfs.WFSController;

/**
 * {@link OutputFormatProvider} for the {@link GMLOutputFormat}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GMLOutputFormatProvider implements OutputFormatProvider {

    @Override
    public OutputFormat create( WFSController wfs, String mimeType, Properties props ) {
        boolean streamMode = "true".equals( props.getProperty( "streamMode" ) );
        CoordinateFormatter formatter = new DecimalCoordinateFormatter( 8 );
        String decimalPlaces = props.getProperty( "decimalPlaces" );
        if ( decimalPlaces != null ) {
            formatter = new DecimalCoordinateFormatter( Integer.parseInt( decimalPlaces ) );
        }
        String schemaLocation = props.getProperty( "schemaLocation" );
        QName responseContainerEl = null;
        if ( props.getProperty( "responseContainerLocalName" ) != null
             && props.getProperty( "responseContainerPrefix" ) != null
             && props.getProperty( "responseContainerNs" ) != null ) {
            responseContainerEl = new QName( props.getProperty( "responseContainerNs" ),
                                             props.getProperty( "responseContainerLocalName" ),
                                             props.getProperty( "responseContainerPrefix" ) );
        }
        return new GMLOutputFormat( wfs, streamMode, formatter, mimeType, responseContainerEl, schemaLocation );
    }

    @Override
    public String getWKN() {
        return "GENERIC_GML";
    }
}