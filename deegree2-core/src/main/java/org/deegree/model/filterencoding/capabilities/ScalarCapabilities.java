// $HeadURL:
// /cvsroot/deegree/src/org/deegree/ogcwebservices/getcapabilities/Contents.java,v
// 1.1 2004/06/23 11:55:40 mschneider Exp $
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
package org.deegree.model.filterencoding.capabilities;

import java.util.HashMap;
import java.util.Map;

/**
 * ScalarCapabilitiesBean
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ScalarCapabilities {

    private boolean supportsLogicalOperators;

    /**
     * keys are Strings (operator names), values are Operator-instances
     */
    private Map<String, Operator> comparisonOperators;

    /**
     * keys are Strings (operator names), values are Operator-instances
     */
    private Map<String, Operator> arithmeticOperators;

    /**
     * Creates a new <code>ScalarCapabilities</code> instance.
     *
     * @param supportsLogicalOperators
     * @param comparisonOperators
     *            may be null
     * @param arithmeticOperators
     *            may be null
     */
    public ScalarCapabilities( boolean supportsLogicalOperators, Operator[] comparisonOperators,
                               Operator[] arithmeticOperators ) {
        this.supportsLogicalOperators = supportsLogicalOperators;
        setComparisonOperators( comparisonOperators );
        setArithmeticOperators( arithmeticOperators );
    }

    /**
     *
     * @return true, if it supports them
     */
    public boolean hasLogicalOperatorsSupport() {
        return supportsLogicalOperators;
    }

    /**
     *
     * @param supportsLogicalOperators
     */
    public void setLogicalOperatorsSupport( boolean supportsLogicalOperators ) {
        this.supportsLogicalOperators = supportsLogicalOperators;
    }

    /**
     *
     * @param comparisonOperators
     */
    public void setComparisonOperators( Operator[] comparisonOperators ) {
        this.comparisonOperators = new HashMap<String, Operator>();
        if ( comparisonOperators != null ) {
            for ( int i = 0; i < comparisonOperators.length; i++ ) {
                this.comparisonOperators.put( comparisonOperators[i].getName(), comparisonOperators[i] );
            }
        }
    }

    /**
     *
     * @return Comparison Operators
     */
    public Operator[] getComparisonOperators() {
        return comparisonOperators.values().toArray( new Operator[comparisonOperators.values().size()] );
    }

    /**
     *
     * @param arithmeticOperators
     *            may be null
     */
    public void setArithmeticOperators( Operator[] arithmeticOperators ) {
        this.arithmeticOperators = new HashMap<String, Operator>();
        if ( arithmeticOperators != null ) {
            for ( int i = 0; i < arithmeticOperators.length; i++ ) {
                this.arithmeticOperators.put( arithmeticOperators[i].getName(), arithmeticOperators[i] );
            }
        }
    }

    /**
     *
     * @return Arithmetic Operators
     */
    public Operator[] getArithmeticOperators() {
        return arithmeticOperators.values().toArray( new Operator[arithmeticOperators.values().size()] );
    }

    /**
     * Returns if the given operator is supported.
     *
     * @param operatorName
     * @return if the given operator is supported.
     */
    public boolean hasComparisonOperator( String operatorName ) {

        // hack to cope with 1.0.0 simple comparisons (type doesn't map to a unique 1.1.0 operator)
        if (OperatorFactory100.OPERATOR_SIMPLE_COMPARISONS.equals( operatorName )) {
            // assume simple comparison operations are supported when equal to is supported
            operatorName = OperatorFactory110.EQUAL_TO;
        }

        return comparisonOperators.get( operatorName ) != null ? true : false;
    }

    /**
     * Returns if the given operator is supported.
     *
     * @param operatorName
     * @return if the given operator is supported.
     */
    public boolean hasArithmeticOperator( String operatorName ) {
        return arithmeticOperators.get( operatorName ) != null ? true : false;
    }
}
