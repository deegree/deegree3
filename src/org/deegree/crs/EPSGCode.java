//$HeadURL: svn+ssh://aionita@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.crs;

import org.deegree.commons.types.ows.CodeType;

/**
 * The <code>EPSGCode</code> class formalizes the CRSIdentifiables object codes that were issued by EPSG.
 * An instance of this class will represent all the EPSG codes variants that denote the same object.
 *
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 *
 * @author last edited by: $Author: ionita $
 *
 * @version $Revision: $, $Date: $
 *
 */
public class EPSGCode extends CRSCodeType {

    private int codeNo;

    public EPSGCode (int codeNo) {
        super ("" + codeNo, "EPSG" );
        this.codeNo = codeNo;
    }

    public int getCodeNo() {
        return codeNo;
    }

//    public boolean isEPSGCode( String code ) {
//        if ( code == null )
//            return false;
//
//        boolean caught = false;
//        int suffix = 0;
//        if ( code.startsWith( "EPSG" ) || code.startsWith( "URN:OGC:DEF:CRS:EPSG" ) ||
//                                code.startsWith( "URN:OPENGIS:DEF:CRS:EPSG" ) ||
//                                code.startsWith( "URN:X-OGC:DEF:CRS:EPSG" ) ) {
//            try {
//                Integer.parseInt( code.substring( code.lastIndexOf( ':' ) + 1 ) );
//            } catch ( NumberFormatException e ) {
//                System.out.println( "Invalid EPSG code: " + code );
//                e.printStackTrace();
//                caught = true;
//            }
//            if ( ! caught )
//                return true;
//            else
//                return false;
//        }
//        else if ( code.startsWith( "HTTP://WWW.OPENGIS.NET/GML/SRS/EPSG.XML" ) ) {
//            try {
//            suffix = Integer.parseInt( epsgCodeAsString.substring( epsgCodeAsString.lastIndexOf( '#' ) + 1 ) );
//            } catch ( NumberFormatException e ) {
//                System.out.println( "Invalid EPSG code " + epsgCodeAsString );
//                e.printStackTrace();
//                caught = true;
//            }
//            if ( ! caught )
//                return new EPSGCode( suffix );
//            else
//                throw new IllegalArgumentException();
//        } else if ( epsgCodeAsString.contains( "EPSG" ) ) {
//           int lastChPos = epsgCodeAsString.length();
//           int noOfDigits = 1;
//           int finalNoOfDigits = 0;
//           while ( true ) {
//               String stringSuffix = epsgCodeAsString.substring( lastChPos - noOfDigits, lastChPos );
//            try {
//                Integer.parseInt( stringSuffix );
//            } catch ( NumberFormatException e ) {
//                finalNoOfDigits = noOfDigits - 1;
//                break;
//            }
//            noOfDigits++;
//           }
//           if ( finalNoOfDigits > 0 )
//               return new EPSGCode( Integer.parseInt(
//                                    epsgCodeAsString.substring( lastChPos - finalNoOfDigits, lastChPos ) ) );
//           else
//               throw new IllegalArgumentException();
//        } else
//            throw new IllegalArgumentException();
//
//    }
//
//    public static EPSGCode valueOf (String epsgCodeAsString) throws IllegalArgumentException {
//        if ( epsgCodeAsString == null )
//            throw new IllegalArgumentException();
//
//        boolean caught = false;
//        int suffix = 0;
//        if ( epsgCodeAsString.startsWith( "EPSG" ) || epsgCodeAsString.startsWith( "URN:OGC:DEF:CRS:EPSG" ) ||
//                                epsgCodeAsString.startsWith( "URN:OPENGIS:DEF:CRS:EPSG" ) ||
//                                epsgCodeAsString.startsWith( "URN:X-OGC:DEF:CRS:EPSG" ) ) {
//            try {
//                suffix = Integer.parseInt( epsgCodeAsString.substring( epsgCodeAsString.lastIndexOf( ':' ) + 1 ) );
//            } catch ( NumberFormatException e ) {
//                System.out.println( "Invalid EPSG code " + epsgCodeAsString );
//                e.printStackTrace();
//                caught = true;
//            }
//            if ( ! caught )
//                return new EPSGCode( suffix );
//            else
//                throw new IllegalArgumentException();
//        }
//        else if ( epsgCodeAsString.startsWith( "HTTP://WWW.OPENGIS.NET/GML/SRS/EPSG.XML" ) ) {
//            try {
//            suffix = Integer.parseInt( epsgCodeAsString.substring( epsgCodeAsString.lastIndexOf( '#' ) + 1 ) );
//            } catch ( NumberFormatException e ) {
//                System.out.println( "Invalid EPSG code " + epsgCodeAsString );
//                e.printStackTrace();
//                caught = true;
//            }
//            if ( ! caught )
//                return new EPSGCode( suffix );
//            else
//                throw new IllegalArgumentException();
//        } else if ( epsgCodeAsString.contains( "EPSG" ) ) {
//           int lastChPos = epsgCodeAsString.length();
//           int noOfDigits = 1;
//           int finalNoOfDigits = 0;
//           while ( true ) {
//               String stringSuffix = epsgCodeAsString.substring( lastChPos - noOfDigits, lastChPos );
//            try {
//                Integer.parseInt( stringSuffix );
//            } catch ( NumberFormatException e ) {
//                finalNoOfDigits = noOfDigits - 1;
//                break;
//            }
//            noOfDigits++;
//           }
//           if ( finalNoOfDigits > 0 )
//               return new EPSGCode( Integer.parseInt(
//                                    epsgCodeAsString.substring( lastChPos - finalNoOfDigits, lastChPos ) ) );
//           else
//               throw new IllegalArgumentException();
//        } else
//            throw new IllegalArgumentException();
//    }

}
