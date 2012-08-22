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
package org.deegree.io.datastore.schema.content;

import java.util.HashMap;
import java.util.Map;

import org.deegree.i18n.Messages;
import org.deegree.ogcwebservices.wfs.operation.Query;

/**
 * Special content class that allows to refer to parameters of the {@link Query} or other properties
 * of the environment.
 * <p>
 * Currently defined variables:
 *
 * <table border="1">
 * <tr>
 * <th>Variable name</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>$QUERY.BBOX</td>
 * <td>Bounding box of the query (null if it is not present).</td>
 * </tr>
 * </table>
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class SpecialContent implements FunctionParam {

    /**
     * Known variables.
     */
    public static enum VARIABLE {

        /**
         * Bounding box of the query.
         */
        QUERY_BBOX
    }

    /**
     * Bounding box of the query (null if it is not present).
     */
    public final static String QUERY_BBOX_NAME = "$QUERY.BBOX";

    private static Map<String, VARIABLE> variableNameMap = new HashMap<String, VARIABLE>();

    static {
        variableNameMap.put( QUERY_BBOX_NAME, VARIABLE.QUERY_BBOX );
    }

    private VARIABLE variable;

    // backend-specific SRS (derived from other FunctionParams)
    private int internalSRS = -1;

    /**
     * Initializes a newly created <code>SpecialContent</code> object so that it represents the
     * given variable.
     *
     * @param variableName
     * @throws IllegalArgumentException
     *             if given variable is not known
     */
    public SpecialContent( String variableName ) {
        variable = variableNameMap.get( variableName );
        if ( variable == null ) {
            StringBuffer variableList = new StringBuffer();
            String[] knownVariables = getKnownVariables();
            for ( int i = 0; i < knownVariables.length; i++ ) {
                variableList.append( '\'' );
                variableList.append( knownVariables[i] );
                variableList.append( '\'' );
                if ( i != knownVariables.length - 1 ) {
                    variableList.append( ',' );
                }
            }
            String msg = Messages.getMessage( "DATASTORE_VARIABLE_UNKNOWN", variableName, variableList );
            throw new IllegalArgumentException( msg );
        }
    }

    /**
     * Returns the {@link VARIABLE}.
     *
     * @return the variable
     */
    public VARIABLE getVariable() {
        return this.variable;
    }

    /**
     * Returns all known variable names.
     *
     * @return all known variable names
     */
    public static String[] getKnownVariables() {
        return variableNameMap.keySet().toArray( new String[variableNameMap.keySet().size()] );
    }

    /**
     * Returns the vendor dependent descriptor for the SRS of the associated geometry.
     *
     * @return vendor dependent descriptor for the SRS, may be null
     */
    public int getSRS() {
        return this.internalSRS;
    }

    /**
     * Sets the vendor dependent descriptor for the SRS of the associated geometry.
     *
     * @param internalSRS
     *            the vendor dependent descriptor for the SRS
     */
    public void setSRS( int internalSRS ) {
        this.internalSRS = internalSRS;
    }
}
