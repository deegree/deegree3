// $HeadURL:
// /deegreerepository/deegree/src/org/deegree/model/filterencoding/capabilities/OperatorFactory.java,v
// 1.1 2005/03/04 16:33:07 mschneider Exp $
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

import org.deegree.framework.util.StringTools;
import org.deegree.ogcwebservices.getcapabilities.UnknownOperatorNameException;

/**
 *
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class OperatorFactory110 {

    // comparison operators as defined in filterCapabilities.xsd (1.1.0)

    /**
     *
     */
    public final static String LESS_THAN = "LessThan";

    /**
     *
     */
    public final static String GREATER_THAN = "GreaterThan";

    /**
     *
     */
    public final static String LESS_THAN_EQUAL_TO = "LessThanEqualTo";

    /**
     *
     */
    public final static String GREATER_THAN_EQUAL_TO = "GreaterThanEqualTo";

    /**
     *
     */
    public final static String EQUAL_TO = "EqualTo";

    /**
     *
     */
    public final static String NOT_EQUAL_TO = "NotEqualTo";

    /**
     *
     */
    public final static String LIKE = "Like";

    /**
     *
     */
    public final static String BETWEEN = "Between";

    /**
     *
     */
    public final static String NULL_CHECK = "NullCheck";

    // spatial operators as defined in filterCapabilities.xsd (1.1.0)

    /**
     *
     */
    public final static String BBOX = "BBOX";

    /**
     *
     */
    public final static String EQUALS = "Equals";

    /**
     *
     */
    public final static String DISJOINT = "Disjoint";

    /**
     *
     */
    public final static String INTERSECTS = "Intersects";

    /**
     *
     */
    public final static String TOUCHES = "Touches";

    /**
     *
     */
    public final static String CROSSES = "Crosses";

    /**
     *
     */
    public final static String WITHIN = "Within";

    /**
     *
     */
    public final static String CONTAINS = "Contains";

    /**
     *
     */
    public final static String OVERLAPS = "Overlaps";

    /**
     *
     */
    public final static String BEYOND = "Beyond";

    /**
     *
     */
    public final static String DWITHIN = "DWithin";

    /**
     *
     * @param name
     * @return SpatialOperator for name
     * @throws UnknownOperatorNameException
     */
    public static SpatialOperator createSpatialOperator( String name )
                            throws UnknownOperatorNameException {

        if ( name.equals( BBOX ) || name.equals( EQUALS ) || name.equals( DISJOINT ) || name.equals( INTERSECTS )
             || name.equals( TOUCHES ) || name.equals( CROSSES ) || name.equals( WITHIN ) || name.equals( CONTAINS )
             || name.equals( OVERLAPS ) || name.equals( BEYOND ) || name.equals( DWITHIN ) ) {
            return new SpatialOperator( name, null );
        }
        String msg = StringTools.concat( 200, "'", name, "' is no valid spatial operator (according to filter ",
                                         "encoding specification 1.1.0.)." );
        throw new UnknownOperatorNameException( msg );
    }

    /**
     *
     * @param name
     * @return Operator for name
     * @throws UnknownOperatorNameException
     */
    public static Operator createComparisonOperator( String name )
                            throws UnknownOperatorNameException {

        if ( name.equals( LESS_THAN ) || name.equals( GREATER_THAN ) || name.equals( LESS_THAN_EQUAL_TO )
             || name.equals( EQUAL_TO ) || name.equals( LESS_THAN_EQUAL_TO ) || name.equals( GREATER_THAN_EQUAL_TO )
             || name.equals( EQUAL_TO ) || name.equals( NOT_EQUAL_TO ) || name.equals( LIKE ) || name.equals( BETWEEN )
             || name.equals( NULL_CHECK ) ) {
            return new Operator( name );
        }
        String msg = StringTools.concat( 200, "'", name, "' is no valid comparison operator (according tofilter",
                                         " encoding specification 1.1.0.)." );
        throw new UnknownOperatorNameException( msg );
    }
}
