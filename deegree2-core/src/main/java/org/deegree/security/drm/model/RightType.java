// $HeadURL$
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
package org.deegree.security.drm.model;

/**
 * A <code>RightType</code> defines a certain type of right, e.g. an 'access' right. It
 * encapsulates a unique id and an also unique name.
 *
 *
 * @version $Revision$
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class RightType {

    // predefined right types
    // general
    public static final RightType ACCESS = new RightType( 1, "access" );

    public static final RightType QUERY = new RightType( 2, "query" );

    public static final RightType DELETE = new RightType( 3, "delete" );

    public static final RightType DELETE_RESPONSE = new RightType( 1003, "delete_Response" );

    public static final RightType INSERT = new RightType( 4, "insert" );

    public static final RightType INSERT_RESPONSE = new RightType( 1004, "insert_Response" );

    public static final RightType EXECUTE = new RightType( 5, "execute" );

    public static final RightType UPDATE = new RightType( 6, "update" );

    public static final RightType UPDATE_RESPONSE = new RightType( 1006, "update_Response" );

    public static final RightType VIEW = new RightType( 7, "view" );

    public static final RightType GRANT = new RightType( 8, "grant" );

    // WMS
    public static final RightType GETMAP = new RightType( 9, "GetMap" );

    public static final RightType GETMAP_RESPONSE = new RightType( 1009, "GetMap_Response" );

    public static final RightType GETFEATUREINFO = new RightType( 10, "GetFeatureInfo" );

    public static final RightType GETFEATUREINFO_RESPONSE = new RightType( 1010, "GetFeatureInfo_Response" );

    public static final RightType GETLEGENDGRAPHIC = new RightType( 11, "GetLegendGraphic" );

    public static final RightType GETLEGENDGRAPHIC_RESPONSE = new RightType( 1011, "GetLegendGraphic_Response" );

    // WFS
    public static final RightType GETFEATURE = new RightType( 13, "GetFeature" );

    public static final RightType GETFEATURE_RESPONSE = new RightType( 1013, "GetFeature_Response" );

    public static final RightType DESCRIBEFEATURETYPE = new RightType( 14, "DescribeFeatureType" );

    public static final RightType DESCRIBEFEATURETYPE_RESPONSE = new RightType( 1014, "DescribeFeatureType_Response" );

    // WCS
    public static final RightType GETCOVERAGE = new RightType( 15, "GetCoverage" );

    public static final RightType GETCOVERAGE_RESPONSE = new RightType( 1015, "GetCoverage_Response" );

    public static final RightType DESCRIBECOVERAGE = new RightType( 16, "DescribeCoverage" );

    public static final RightType DESCRIBECOVERAGE_RESPONSE = new RightType( 1016, "DescribeCoverage_Response" );

    // CSW
    public static final RightType GETRECORDS = new RightType( 17, "GetRecords" );

    public static final RightType GETRECORDS_RESPONSE = new RightType( 1017, "GetRecords_Response" );

    public static final RightType GETRECORDBYID = new RightType( 18, "GetRecordById" );

    public static final RightType GETRECORDBYID_RESPONSE = new RightType( 1018, "GetRecordById_Response" );

    public static final RightType DESCRIBERECORDTYPE = new RightType( 19, "DescribeRecordType" );

    public static final RightType DESCRIBERECORDTYPE_RESPONSE = new RightType( 1019, "DescribeRecordType_Response" );

    // ebrim CSW profile
    public static final RightType GETREPOSITORYITEM = new RightType( 20, "GetRepositoryItem" );

    public static final RightType GETREPOSITORYITEM_RESPONSE = new RightType( 1020, "GetRepositoryItem_Response" );

    private int id;

    private String name;

    /**
     * Creates a new <code>RightType</code>-instance.
     *
     * @param id
     * @param name
     */
    public RightType( int id, String name ) {
        this.id = id;
        this.name = name;
    }

    /**
     * Returns the unique identifier of this <code>RightType</code>.
     */
    public int getID() {
        return id;
    }

    /**
     * Returns the name of this <code>RightType</code>.
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Indicates whether some other <code>RightType</code> instance is "equal to" this one.
     *
     * @param that
     */
    public boolean equals( Object that ) {
        if ( that instanceof RightType ) {
            return ( ( (RightType) that ).getID() == getID() );
        }
        return false;
    }

    /**
     * Returns a hash code value for the object. This method is supported for the benefit of
     * hashtables such as those provided by java.util.Hashtable.
     */
    public int hashCode() {
        return id;
    }

    /**
     * Returns a <code>String</code> representation of this object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer( "Id: " ).append( id ).append( ", Name: " ).append( name );
        return sb.toString();
    }
}
