//$HeadURL: http://svn.wald.intevation.org/svn/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.services.wps.provider;

import org.deegree.gml.GMLVersion;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.output.ComplexOutput;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * @author last edited by: $Author: pabel $
 * 
 * @version $Revision: $, $Date: $
 */
public class FormatHelper {

    public enum GML_PAYLOAD {
        GEOMETRY, FEATURE, FEATURE_COLLECTION
    }

    public static String getApplicationSchema( ComplexInput input ) {
        // TODO
        return null;
    }

    public static GML_PAYLOAD determinePayload( ComplexInput input ) {
        // TODO
        return GML_PAYLOAD.GEOMETRY;
    }

    public static GMLVersion determineGMLVersion( ComplexInput input ) {
        GMLVersion gmlVersion = null;
        String schema = input.getSchema();

        // TODO CORRECT THE REDUNDANZ
        if ( schema.equals( "http://schemas.opengis.net/gml/2.1.2/geometry.xsd" ) )
            gmlVersion = GMLVersion.GML_2;
        else if ( schema.equals( "http://schemas.opengis.net/gml/3.0.1/base/geometryComplexes.xsd" ) )
            gmlVersion = GMLVersion.GML_30;
        else if ( schema.equals( "http://schemas.opengis.net/gml/3.1.1/base/geometryComplexes.xsd" ) )
            gmlVersion = GMLVersion.GML_31;
        else if ( schema.equals( "http://schemas.opengis.net/gml/3.2.1/geometryComplexes.xsd" ) )
            gmlVersion = GMLVersion.GML_32;
        else if ( schema.equals( "http://schemas.opengis.net/gml/3.1.1/base/feature.xsd" ) )
            gmlVersion = GMLVersion.GML_31;
        return gmlVersion;
    }

    public static String getApplicationSchema( ComplexOutput output ) {
        // TODO
        return null;
    }

    public static GML_PAYLOAD determinePayload( ComplexOutput output ) {
        // TODO
        return GML_PAYLOAD.GEOMETRY;
    }

    public static GMLVersion determineGMLVersion( ComplexOutput output ) {
        GMLVersion gmlVersion = null;
        String schema = output.getRequestedSchema();

        // TODO CORRECT THE REDUNDANZ
        if ( schema.equals( "http://schemas.opengis.net/gml/2.1.2/geometry.xsd" ) )
            gmlVersion = GMLVersion.GML_2;
        else if ( schema.equals( "http://schemas.opengis.net/gml/3.0.1/base/geometryComplexes.xsd" ) )
            gmlVersion = GMLVersion.GML_30;
        else if ( schema.equals( "http://schemas.opengis.net/gml/3.1.1/base/geometryComplexes.xsd" ) )
            gmlVersion = GMLVersion.GML_31;
        else if ( schema.equals( "http://schemas.opengis.net/gml/3.2.1/geometryComplexes.xsd" ) )
            gmlVersion = GMLVersion.GML_32;
        else if ( schema.equals( "http://schemas.opengis.net/gml/3.1.1/base/feature.xsd" ) )
            gmlVersion = GMLVersion.GML_31;
        return gmlVersion;
    }

}
