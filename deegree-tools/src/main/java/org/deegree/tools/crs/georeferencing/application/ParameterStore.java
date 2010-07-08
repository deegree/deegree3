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
package org.deegree.tools.crs.georeferencing.application;

import java.util.regex.Pattern;

/**
 * Helper class to store the parameters from the commandLine.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ParameterStore {

    private final String filename;

    private final String mapURL;

    private final String CRS;

    private final String format;

    private final String layers;

    private final String bbox;

    private String LEFT_LOWER_X;

    private String LEFT_LOWER_Y;

    private String RIGHT_UPPER_X;

    private String RIGHT_UPPER_Y;

    private final static String separator = "\\p{Space}*[ ;/]\\p{Space}*";

    public ParameterStore( String mapURL, String CRS, String format, String layers, String bbox, String filename ) {

        this.mapURL = mapURL;
        this.CRS = CRS;
        this.format = format;
        this.layers = layers;
        this.bbox = bbox;
        this.filename = filename;

        String[] inputParameters = null;
        Pattern p = Pattern.compile( separator );
        inputParameters = p.split( bbox );

        for ( int i = 0; i < inputParameters.length; i += 4 ) {

            LEFT_LOWER_X = inputParameters[i];

            LEFT_LOWER_Y = inputParameters[i + 1];

            RIGHT_UPPER_X = inputParameters[i + 2];

            RIGHT_UPPER_Y = inputParameters[i + 3];
        }
        System.out.println( "[ParameterStore]" + LEFT_LOWER_X + " " + LEFT_LOWER_Y + " " + RIGHT_UPPER_X + " "
                            + RIGHT_UPPER_Y );

    }

    public String getFilename() {
        return filename;
    }

    public String getMapURL() {
        return mapURL;
    }

    public String getCRS() {
        return CRS;
    }

    public String getFormat() {
        return format;
    }

    public String getLayers() {
        return layers;
    }

    public String getBbox() {
        return bbox;
    }

    public String getLEFT_LOWER_X() {
        return LEFT_LOWER_X;
    }

    public String getLEFT_LOWER_Y() {
        return LEFT_LOWER_Y;
    }

    public String getRIGHT_UPPER_X() {
        return RIGHT_UPPER_X;
    }

    public String getRIGHT_UPPER_Y() {
        return RIGHT_UPPER_Y;
    }

}
