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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deegree.io.datastore.schema.MappedSimplePropertyType;

/**
 * Content class for {@link MappedSimplePropertyType}s that describes a call to a function provided
 * by an SQL database.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class SQLFunctionCall extends FunctionCall {

    private String callString;

    private int typeCode;

    private int[] usedVarNumbers;

    /**
     * Initializes a newly created <code>SQLFunctionCall</code> with the given call string and
     * {@link FunctionParam}s.
     *
     * @param callString
     *            call string with placeholders ($1,$2,...,$n)
     * @param typeCode
     *            SQL type code of the return value
     * @param params
     *            parameters to be used for the placeholders
     */
    public SQLFunctionCall( String callString, int typeCode, List<FunctionParam> params ) {
        super( params );
        this.callString = callString;
        this.typeCode = typeCode;
        this.usedVarNumbers = extractUsedVars( callString );
    }

    /**
     * Initializes a newly created <code>SQLFunctionCall</code> with the given call string and
     * {@link FunctionParam}s.
     *
     * @param callString
     *            call string with placeholders ($1,$2,...,$n)
     * @param typeCode
     *            SQL type code of the return value
     * @param params
     *            parameters to be used for the placeholders
     */
    public SQLFunctionCall( String callString, int typeCode, FunctionParam... params ) {
        super( params );
        this.callString = callString;
        this.typeCode = typeCode;
        this.usedVarNumbers = extractUsedVars( callString );
    }

    /**
     * Extracts all variable numbers ('$i') used in the given call string.
     *
     * @param callString
     * @return all variable numbers used in the given call string
     */
    private int[] extractUsedVars( String callString ) {

        Set<Integer> usedVars = new HashSet<Integer>();

        int foundAt = callString.indexOf( '$' );
        while ( foundAt != -1 ) {
            foundAt++;
            String varNumberString = "";
            while ( foundAt < callString.length() ) {
                char numberChar = callString.charAt( foundAt++ );
                if ( numberChar >= '0' && numberChar <= '9' ) {
                    varNumberString += numberChar;
                } else {
                    break;
                }
            }

            assert varNumberString.length() != 0;

            try {
                int varNo = Integer.parseInt( varNumberString );
                assert varNo != 0;
                usedVars.add( varNo );
            } catch ( NumberFormatException e ) {
                assert false;
            }

            // find next '$' symbol
            foundAt = callString.indexOf( '$', foundAt );
        }

        int[] usedVarInts = new int[usedVars.size()];
        int i = 0;
        for ( Integer pos : usedVars ) {
            usedVarInts[i++] = pos;
        }

        return usedVarInts;
    }

    /**
     * Returns true, because the result of an SQL function call is (in general) suitable as a sort
     * criterion.
     *
     * @return true, because the result of an SQL function call is (in general) suitable as a sort
     *         criterion
     */
    public boolean isSortable() {
        return true;
    }

    /**
     * Returns the call string with placeholders ($1,$2,...,$n) for the {@link FunctionParam}s.
     *
     * @return the call string with placeholders ($1,$2,...,$n)
     */
    public String getCall() {
        return this.callString;
    }

    /**
     * Returns the SQL type code of the function call's return value.
     *
     * @return the SQL type code of the function call's return value.
     */
    public int getTypeCode() {
        return this.typeCode;
    }

    /**
     * Returns all variable numbers used in the call string.
     *
     * @return all variable numbers used in the call string
     */
    public int[] getUsedVars() {
        return this.usedVarNumbers;
    }

    @Override
    public String toString() {
        String s = this.callString;
        int[] usedVars = this.getUsedVars();
        List<FunctionParam> params = this.getParams();
        for ( int j = 0; j < usedVars.length; j++ ) {
            int varNo = usedVars[j];
            String varString = "\\$" + varNo;
            FunctionParam param = params.get( varNo - 1 );
            if ( param instanceof FieldContent ) {
                String replace = ( (FieldContent) param ).getField().getTable() + "."
                                 + ( (FieldContent) param ).getField().getField();
                s = s.replaceAll( varString, replace );
            } else if ( param instanceof ConstantContent ) {
                String replace = ( (ConstantContent) param ).getValue();
                s = s.replaceAll( varString, replace );
            } else if ( param instanceof SpecialContent ) {
//                appendSpecialContentValue( query, (SpecialContent) param );
//                s = s.replaceFirst( varString, "?" );
            } else {
                assert false;
            }
        }
        return s;
    }
}
