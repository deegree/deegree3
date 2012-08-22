//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.portal.standard.routing.control;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.deegree.framework.util.StringTools;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CRSTransformationException;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class RouteBean {
    
    private static NumberFormat nf = new DecimalFormat( "#.##" );

    private String coordinates;

    private String description;

    private String distance;

    private double[] bbox = new double[] { 9E99, 9E99, -9E99, -9E99 };

    private String start;

    private String end;

    private int numberOfNodes;

    /**
     * Initializes a response bean by assigning according values and transforming route coordinates into the coordinate
     * reference system of the current map model
     * 
     * @param modelCrs
     * @param coords
     * @param description
     * @param distance
     * @param start
     *            description/name of the start point
     * @param end
     *            description/name of the end point
     * @throws IllegalArgumentException
     * @throws CRSTransformationException
     * @throws GeometryException
     */
    public RouteBean( CoordinateSystem modelCrs, String coords, String description, double distance, String start,
                      String end ) throws IllegalArgumentException, CRSTransformationException, GeometryException {
        // coordinates returned from YOURS routing service must be transformed into
        // coordinate reference system of current map model
        GeoTransformer gt = new GeoTransformer( modelCrs );
        String[] tmp = StringTools.toArray( coords, " \n\t", false );
        StringBuilder sb = new StringBuilder( 10000 );
        sb.append( "LINESTRING(" );
        for ( int i = 0; i < tmp.length; i++ ) {
            double[] d = StringTools.toArrayDouble( tmp[i], "," );
            Point p = GeometryFactory.createPoint( d[0], d[1], CRSFactory.EPSG_4326 );
            p = (Point) gt.transform( p );
            if ( p.getX() < bbox[0] ) {
                bbox[0] = p.getX();
            }
            if ( p.getX() > bbox[2] ) {
                bbox[2] = p.getX();
            }
            if ( p.getY() < bbox[1] ) {
                bbox[1] = p.getY();
            }
            if ( p.getY() > bbox[3] ) {
                bbox[3] = p.getY();
            }
            sb.append( p.getX() ).append( ' ' ).append( p.getY() );
            if ( i < tmp.length - 1 ) {
                sb.append( ',' );
            }
        }
        sb.append( ")" );
        this.coordinates = sb.toString();
        
        this.numberOfNodes = tmp.length;
        this.description = StringTools.replace( description, "<br>", "\n", true );
        this.description = StringTools.replace( this.description, "</br>", "\n", true );
        this.distance = nf.format( distance );
        this.start = start;
        this.end = end;
    }

    /**
     * @return the coordinates
     */
    public String getCoordinates() {
        return coordinates;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the distance
     */
    public String getDistance() {
        return distance;
    }

    /**
     * @return the bbox
     */
    public double[] getBbox() {
        return bbox;
    }

    /**
     * @return the start
     */
    public String getStart() {
        return start;
    }

    /**
     * @return the end
     */
    public String getEnd() {
        return end;
    }
    /**
     * @return the numberOfNodes
     */
    public int getNumberOfNodes() {
        return numberOfNodes;
    }

}
