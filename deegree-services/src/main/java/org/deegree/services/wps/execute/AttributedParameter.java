//$Header: /deegreerepository/deegree/resources/eclipse/svn_classfile_header_template.xml,v 1.2 2007/03/06 09:44:09 bezema Exp $
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

package org.deegree.services.wps.execute;

import java.util.HashMap;
import java.util.Map;

import org.deegree.commons.tom.ows.CodeType;

/**
 * Represents an input or output parameter from a KVP Execute request with optional value and attributes.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: padberg$
 *
 * @version $Revision: 15639 $, $Date: 08.05.2008 13:54:31$
 */
class AttributedParameter {

    private CodeType parameterId;

    private String value;

    private Map<String, String> attrNamesToValues;

    private Map<String, String> attrNamesUCToValues;

    /**
     * @param parameterId
     * @param value
     * @param attrNamesToValues
     */
    AttributedParameter( CodeType parameterId, String value, Map<String, String> attrNamesToValues ) {
        this.parameterId = parameterId;
        this.value = value;
        this.attrNamesToValues = attrNamesToValues;
        attrNamesUCToValues = new HashMap<String, String>();
        for ( String attrName : attrNamesToValues.keySet() ) {
            String attrValue = attrNamesToValues.get( attrName );
            attrNamesUCToValues.put( attrName.toUpperCase(), attrValue );
        }
    }

    /**
     * @return
     */
    public CodeType getParameterId() {
        return parameterId;
    }

    /**
     * @return
     */
    public String getValue() {
        return value;
    }

    /**
     * @return
     */
    public Map<String, String> getAttributes() {
        return attrNamesToValues;
    }

    /**
     * @return
     */
    public String getDataType() {
        return attrNamesUCToValues.get( "DATATYPE" );
    }

    /**
     * @return
     */
    public String getUom() {
        return attrNamesUCToValues.get( "UOM" );
    }

    /**
     * @return
     */
    public String getMimeType() {
        return attrNamesUCToValues.get( "MIMETYPE" );
    }

    /**
     * @return
     */
    public String getEncoding() {
        return attrNamesUCToValues.get( "ENCODING" );
    }

    /**
     * @return
     */
    public String getSchema() {
        return attrNamesUCToValues.get( "SCHEMA" );
    }

    /**
     * @return
     */
    public String getHref() {
        String href = attrNamesUCToValues.get( "HREF" );
        if (href == null ) {
            href = attrNamesUCToValues.get( "XLINK:HREF" );
        }
        return href;
    }

    public String getAsReference() {
        return attrNamesUCToValues.get( "ASREFERENCE" );
    }

    /**
     * Creates a new {@link AttributedParameter} from its string representation (as specified in section 10.2.2.1 of OGC
     * 05-007r7).
     *
     * @param s
     *            encoded input or output parameter
     * @return corresponding {@link AttributedParameter}
     * @throws IllegalArgumentException
     *             if the format is invalid
     */
    static AttributedParameter valueOf( String s )
                            throws IllegalArgumentException {

        String[] parts = s.split( "@" );
        if ( parts.length == 0 ) {
            String msg = "Encoded parameter '" + s + "' does not specify a parameter identifier.";
            throw new IllegalArgumentException( msg );
        }

        // extract parameter id and value
        String id = null;
        String value = null;

        String idAndValue = parts[0];
        int delimPos = idAndValue.indexOf( '=' );
        if ( delimPos != -1 ) {
            id = idAndValue.substring( 0, delimPos );
            if ( delimPos < idAndValue.length() - 1 ) {
                value = idAndValue.substring( delimPos + 1, idAndValue.length() );
            }
        } else {
            id = idAndValue;
        }

        // extract attribute names and values
        Map<String, String> attrNamesToValues = new HashMap<String, String>();
        for ( int i = 1; i < parts.length; i++ ) {
            String attrName = null;
            String attrValue = null;
            String attrNameAndValue = parts[i];
            delimPos = attrNameAndValue.indexOf( '=' );
            if ( delimPos != -1 ) {
                attrName = attrNameAndValue.substring( 0, delimPos );
                if ( delimPos < attrNameAndValue.length() - 1 ) {
                    attrValue = attrNameAndValue.substring( delimPos + 1, attrNameAndValue.length() );
                }
            } else {
                attrName = attrNameAndValue;
            }
            attrNamesToValues.put( attrName, attrValue );
        }

        return new AttributedParameter( new CodeType( id ), value, attrNamesToValues );
    }

    @Override
    public String toString() {
        String s = "{parameterId='" + parameterId + "',value='" + value + "'";
        for ( String attrName : attrNamesToValues.keySet() ) {
            String attrValue = attrNamesToValues.get( attrName );
            s += ",@" + attrName + "='" + attrValue + "'";
        }
        s += "}";
        return s;
    }
}
