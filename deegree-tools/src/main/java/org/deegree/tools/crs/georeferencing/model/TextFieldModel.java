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
package org.deegree.tools.crs.georeferencing.model;

import java.util.regex.Pattern;

/**
 * Model that holds the relevant information for textfields.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class TextFieldModel {

    private double xCoordinate;

    private double yCoordiante;

    /**
     * <Code>" "</Code> or <br>
     * ";" or <br>
     * "/" or
     * 
     */
    private final static String separator = "\\p{Space}*[ ;/]\\p{Space}*";

    private String textInput;

    public TextFieldModel( String textInput ) {
        this.textInput = textInput;
        String[] inputParameters = null;
        Pattern p = Pattern.compile( separator );
        inputParameters = p.split( textInput );

        for ( int i = 0; i < inputParameters.length; i += 2 ) {
            try {
                xCoordinate = Double.parseDouble( inputParameters[i] );

            } catch ( NumberFormatException e ) {
                xCoordinate = 0.0;

            }
            try {
                yCoordiante = Double.parseDouble( inputParameters[i + 1] );
            } catch ( NumberFormatException e ) {
                yCoordiante = 0.0;
            }
            System.out.println( "[TextFieldModel] inputParameters: " + xCoordinate + " " + yCoordiante );
        }
    }

    public double getxCoordinate() {
        return xCoordinate;
    }

    public double getyCoordiante() {
        return yCoordiante;
    }

    public static String getSeparator() {
        return separator;
    }

    public String getTextInput() {
        return textInput;
    }

}
