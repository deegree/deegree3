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

import org.deegree.datatypes.QualifiedName;

/**
 * SpatialCapabilitiesBean
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </A>
 *
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */
public class SpatialCapabilities {

    // keys are Strings (operator names), values are SpatialOperator-instances
    private Map<String, SpatialOperator> operators = new HashMap<String, SpatialOperator>();

    private QualifiedName[] geometryOperands;

    /**
     * Creates a new <code>SpatialCapabilities</code> instance that complies to the
     * <code>Filter Encoding Specification 1.0.0</code> (without <code>GeometryOperands</code>).
     *
     * @param spatialOperators
     */
    public SpatialCapabilities( SpatialOperator[] spatialOperators ) {
        setSpatialOperators( spatialOperators );
    }

    /**
     * Creates a new <code>SpatialCapabilities</code> instance that complies to the
     * <code>Filter Encoding Specification 1.1.0</code> (with <code>GeometryOperands</code>).
     *
     * @param spatialOperators
     * @param geometryOperands
     */
    public SpatialCapabilities( SpatialOperator[] spatialOperators, QualifiedName[] geometryOperands ) {
        setSpatialOperators( spatialOperators );
        this.geometryOperands = geometryOperands;
    }

    /**
     * @param operator
     */
    public void addSpatialOperator( SpatialOperator operator ) {
        this.operators.put( operator.getName(), operator );

    }

    /**
     * Returns if the given operator is supported.
     *
     * @param operatorName
     * @return if the given operator is supported.
     */
    public boolean hasOperator( String operatorName ) {
        return operators.get( operatorName ) != null ? true : false;
    }

    /**
     * @return the operators
     */
    public SpatialOperator[] getSpatialOperators() {
        return operators.values().toArray( new SpatialOperator[this.operators.size()] );
    }

    /**
     * @param operators
     */
    public void setSpatialOperators( SpatialOperator[] operators ) {
        this.operators.clear();
        for ( int i = 0; i < operators.length; i++ ) {
            this.addSpatialOperator( operators[i] );
        }
    }

    /**
     * @return Returns the geometryOperands.
     */
    public QualifiedName[] getGeometryOperands() {
        return geometryOperands;
    }

    /**
     * @param geometryOperands
     *            The geometryOperands to set.
     */
    public void setGeometryOperands( QualifiedName[] geometryOperands ) {
        this.geometryOperands = geometryOperands;
    }
}
