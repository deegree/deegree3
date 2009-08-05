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

/**
 * The <code>CRSCodeType</code> class formalizes the access to CRSIdentifiable, replacing the old "identifiers". Any
 * prefix patterns that are noticed as for example
 * <ul>
 * <li>EPSG:****</li>
 * <li>URN:OGC:DEF:CRS:EPSG::****</li>
 * <li>HTTP://WWW.OPENGIS.NET/GML/SRS/EPSG.XML#****</li>
 * <li>URN:OPENGIS:DEF:CRS:EPSG::****</li>
 * </ul>
 * are encapsulated in codespace( in this case "EPSG") and code( the respective value). If any new codetypes are
 * noticed, please add them to the constructor and valueOf method.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author: ionita $
 * 
 * @version $Revision: $, $Date: $
 * 
 */
public class CRSCodeType {

    private String code;

    private String codeVersion;

    private String codeSpace;

    private String original;

    /**
     * Create a CRSCodeType from the code and codeSpace. This constructor should be used when the "$codeSpace:$code"
     * string is an actual id (and not just a compressed version).
     * 
     * @param code
     * @param codeSpace
     */
    public CRSCodeType( String code, String codeSpace ) {
        if ( code == null )
            throw new IllegalArgumentException( "Code cannot be null!" );
        if ( code.trim().equals( "" ) )
            throw new IllegalArgumentException( "Code cannot be white space(s)!" );

        this.codeSpace = codeSpace;
        this.code = code;
        this.codeVersion = "";
        this.original = codeSpace + ":" + code;
    }

    /**
     * Create a CRSCodeType from a string. The string is parsed into code, codeSpace and codeVersion. When it cannot,
     * only the original field is non-empty (it will be equal to codeAsString).
     * 
     * @param codeAsString
     */
    public CRSCodeType( String codeAsString ) {
        if ( codeAsString == null )
            throw new IllegalArgumentException( "Code string cannot be null!" );
        if ( codeAsString.trim().equals( "" ) )
            throw new IllegalArgumentException( "Code string cannot be white space(s)" );

        int n = codeAsString.length();
        String codenumber = "";
        boolean numberFinished = false;
        String codeversion = "";
        boolean versionFinished = false;
        for ( int i = n - 1; i >= 0; i-- ) {
            if ( codeAsString.charAt( i ) >= '0' && codeAsString.charAt( i ) <= '9' && !numberFinished ) {
                codenumber = codeAsString.charAt( i ) + codenumber;
            } else if ( !versionFinished
                        && ( ( codeAsString.charAt( i ) >= '0' && codeAsString.charAt( i ) <= '9' ) || codeAsString.charAt( i ) == '.' ) ) {
                codeversion = codeAsString.charAt( i ) + codeversion;
            } else if ( codeAsString.charAt( i ) == ':' && !numberFinished ) {
                numberFinished = true;
            } else if ( codeAsString.charAt( i ) == ':' && !versionFinished ) {
                versionFinished = true;
            } else if ( codeAsString.charAt( i ) == '#' ) {
                numberFinished = true;
                versionFinished = true;
            }
        }

        this.original = codeAsString;
        if ( codenumber.trim().equals( "" ) || !codeAsString.toUpperCase().contains( "EPSG" ) ) {
            this.code = "";
            this.codeSpace = "";
            this.codeVersion = "";
        } else if ( codenumber.length() != 0 && codeversion.length() != 0 ) {
            this.code = codenumber;
            this.codeVersion = codeversion;
            this.codeSpace = "EPSG";
        } else {
            this.code = codenumber;
            this.codeSpace = "EPSG";
            this.codeVersion = "";
        }

    }

    /**
     * Returns the CRSCodeType that can be constructed from the string parameter.
     * 
     * @param codeAsString
     * @return the parsed string as a code type
     * @throws IllegalArgumentException
     */
    public static CRSCodeType valueOf( String codeAsString )
                            throws IllegalArgumentException {
        return new CRSCodeType( codeAsString );
    }

    /**
     * 
     * @return the code number from the code (e.g. "4326" ). Can be an empty string if the id could not be parsed. To
     *         get the id use {@link #getOriginal()}
     */
    public String getCode() {
        return code;
    }

    /**
     * 
     * @return the code space from the code (e.g. "EPSG" ). Can be an empty string if the id could not be parsed.
     */
    public String getCodeSpace() {
        return codeSpace;
    }

    /**
     * 
     * @return the version of the code. If it does not exist, an empty string is returned.
     */
    public String getCodeVersion() {
        return codeVersion;
    }

    /**
     * 
     * @return the code id before parsing.
     */
    public String getOriginal() {
        return original;
    }

    /**
     * @return The code used when a {@link CRSIdentifiable} has no id. The code used is "NOT PROVIDED".
     */
    public static CRSCodeType getUndefined() {
        return new CRSCodeType( "NOT PROVIDED" );
    }

    @Override
    public int hashCode() {
        return original.hashCode();
    }

    @Override
    public boolean equals( Object o ) {
        if ( !( o instanceof CRSCodeType ) ) {
            return false;
        }
        CRSCodeType that = (CRSCodeType) o;

        return original.equals( that.getOriginal() );
    }

    /**
     * Returns the codespace codeversion and code, or just the original string
     * if it could not be parsed. Recommended for presentation purposes only.
     * @return  the code in the form $codespace::$codeversion::$code 
     */
    @Override
    public String toString() {
        if ( !code.equals( "" ) ) {
            if ( !codeVersion.equals( "" ) ) {
                return codeSpace + "::" + codeVersion + "::" + code;
            }
            return codeSpace + "::" + code;
        }

        return original;
    }
}
