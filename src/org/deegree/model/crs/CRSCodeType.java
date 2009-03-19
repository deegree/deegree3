//$HeadURL: svn+ssh://aionita@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.model.crs;

/**
 * The <code>CRSCodeType</code> class formalizes the access to CRSIdentifiable. 
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
    
    private String codeSpace;
    
    public CRSCodeType( String code, String codeSpace ) {
        if ( code == null )
            throw new IllegalArgumentException( "Code cannot be null!" );
        if ( code.trim().equals( "" ) )
            throw new IllegalArgumentException( "Code cannot be white space(s)!" );        
        
        this.codeSpace = codeSpace;
        this.code = code;
    }
    
    public CRSCodeType( String codeAsString ) {
        if ( codeAsString == null )
            throw new IllegalArgumentException( "Code string cannot be null!" );
        if ( codeAsString.trim().equals( "" ) )
            throw new IllegalArgumentException( "Code string cannot be white space(s)" );

        int n = codeAsString.length();
        String codenumber = "";
        for ( int i = n - 1; i >= 0; i-- ) {
            if ( !( codeAsString.charAt( i ) >= '0' && codeAsString.charAt( i ) <= '9' ) ) {
                // the first non-digit encoutered by reading the code from right to left
                break;
            } else
                codenumber = codeAsString.charAt( i ) + codenumber;
        }
        
        if ( codenumber.trim().equals( "" ) || ! codeAsString.toUpperCase().contains( "EPSG" ) ) {
            this.code = codeAsString;
            this.codeSpace = "";
        } else {
            this.code = codenumber;
            this.codeSpace = "EPSG";
        }
    }
        
    public String getCode() {
        return code;
    }
        
    public String getCodeSpace() {
        return codeSpace;
    }
    
    @Override
    public String toString() {
        return code + (codeSpace != null ? " (codeSpace=" + codeSpace + ")" : "");
    }
    
    @Override
    public int hashCode() {
        return codeSpace != null ? (codeSpace + code).hashCode() : code.hashCode();
    }

    @Override
    public boolean equals( Object o ) {
        if ( !( o instanceof CRSCodeType ) ) {
            return false;
        }
        CRSCodeType that = (CRSCodeType) o;
        if ( !code.equals( that.code ) ) {
            return false;
        }
        if ( codeSpace != null ) {
            return codeSpace.equals( that.codeSpace );
        }
        return that.codeSpace == null;
    }
    
    public static CRSCodeType valueOf( String codeAsString ) throws IllegalArgumentException {
        if ( codeAsString == null )
            throw new IllegalArgumentException( "Code string cannot be null!" );
        if ( codeAsString.trim().equals( "" ) )
            throw new IllegalArgumentException( "Code string cannot be white space(s)" );

        int n = codeAsString.length();
        String codenumber = "";
        for ( int i = n - 1; i >= 0; i-- ) {
            if ( !( codeAsString.charAt( i ) >= '0' && codeAsString.charAt( i ) <= '9' ) ) {
                // the first non-digit encoutered by reading the code from right to left
                break;
            } else
                codenumber = codeAsString.charAt( i ) + codenumber;
        }
        
        if ( codenumber.trim().equals( "" ) || ! codeAsString.toUpperCase().contains( "EPSG" ) )
            return new CRSCodeType( codeAsString, null );
        else
            return new CRSCodeType( codenumber, "EPSG" );
    }
    
    public String getEquivalentString() {
        if ( codeSpace != null )
            return codeSpace + ":" + code;
        else
            return code;
    }
}
