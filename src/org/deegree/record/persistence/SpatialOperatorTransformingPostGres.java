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
package org.deegree.record.persistence;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.spatial.BBOX;
import org.deegree.filter.spatial.Beyond;
import org.deegree.filter.spatial.DWithin;
import org.deegree.filter.spatial.Disjoint;
import org.deegree.filter.spatial.SpatialOperator;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry.GeometryType;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Point;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class SpatialOperatorTransformingPostGres {
    
    ExpressionFilterHandling expressionFilterHandling = new ExpressionFilterHandling();
    private ExpressionFilterObject expressObject;
    
    private Set<String> table;

    private Set<String> column;
    
    
    private String spatialOperation;
    private SpatialOperator spaOp;
    
    public SpatialOperatorTransformingPostGres(SpatialOperator spaOp){
        this.spaOp = spaOp;
        
        spatialOperation = doSpatialOperatorToPostGreSQL();
        
        
    }
    
    
    
    
    /**
     * @return the spatialOperation
     */
    public String getSpatialOperation() {
        return spatialOperation;
    }
    
    




    /**
     * @return the table
     */
    public Set<String> getTable() {
        return table;
    }




    /**
     * @return the column
     */
    public Set<String> getColumn() {
        return column;
    }




    private String doSpatialOperatorToPostGreSQL(){
        
        org.deegree.filter.spatial.SpatialOperator.SubType typeSpatial = spaOp.getSubType();
        String stringSpatial = "";
        
        table = new HashSet<String>();

        column = new HashSet<String>();

        switch ( typeSpatial ) {

        case BBOX:

            BBOX bboxOp = (BBOX) spaOp;
            Object[] paramsBBox = bboxOp.getParams();
            stringSpatial = "";

            for ( Object opParam : paramsBBox ) {

                if ( opParam != bboxOp.getBoundingBox() ) {
                    String exp = ( (PropertyName) opParam ).getPropertyName();
                    expressObject = expressionFilterHandling.expressionFilterHandling(
                                                                      ( (PropertyName) opParam ).getType(),
                                                                      new PropertyName(
                                                                                        exp,
                                                                                        ( (PropertyName) opParam ).getNsContext() ) );
                    table.addAll( expressObject.getTable());
                    column.addAll( expressObject.getColumn());
                    stringSpatial += expressObject.getExpression();
                    stringSpatial += " && SetSRID('BOX3D( ";
                } else {
                    double[] minArray = ( (Envelope) opParam ).getMin().getAsArray();
                    double[] maxArray = ( (Envelope) opParam ).getMax().getAsArray();
                    for ( double min : minArray ) {
                        stringSpatial += min + " ";
                    }
                    stringSpatial += ",";
                    for ( double max : maxArray ) {
                        stringSpatial += max + " ";
                    }
                    stringSpatial += " )'::box3d, 4326)";
                }
                System.out.println( stringSpatial );

            }

            return stringSpatial;

        case BEYOND:
            Beyond beyondOp = (Beyond) spaOp;
            Object[] paramsBeyond = beyondOp.getParams();
            stringSpatial += "DISTANCE(";
            
            int counter = 0;
            String stringSpatialPropertyName = "";
            String stringSpatialGeometry = "";
            List<String> stringSpatialGeom = new LinkedList<String>();
            //TODO into WKTAdapter?
            for ( Object opParam : paramsBeyond ) {

                if ( opParam == beyondOp.getPropName() ) {
                    counter++;
                    String exp = ( (PropertyName) opParam ).getPropertyName();

                    stringSpatialPropertyName += "GeomFromText(AsText(";
                    
                    expressObject = expressionFilterHandling.expressionFilterHandling(
                                                                      ( (PropertyName) opParam ).getType(),
                                                                      new PropertyName(
                                                                                        exp,
                                                                                        ( (PropertyName) opParam ).getNsContext() ) );
                    
                    stringSpatialPropertyName += expressObject.getExpression();
                    table.addAll( expressObject.getTable());
                    column.addAll( expressObject.getColumn());
                    stringSpatialPropertyName += "))";
                    stringSpatial += stringSpatialPropertyName;
                } else {
                    counter++;

                    GeometryType geom = beyondOp.getGeometry().getGeometryType();

                    switch ( geom ) {

                    case ENVELOPE:

                        double[] minArray = ( (Envelope) opParam ).getMin().getAsArray();
                        double[] maxArray = ( (Envelope) opParam ).getMax().getAsArray();
                        for ( double min : minArray ) {
                            stringSpatial += min + " ";
                        }
                        stringSpatial += ",";
                        for ( double max : maxArray ) {
                            stringSpatial += max + " ";
                        }
                        stringSpatial += " )'::box3d, 4326)";

                        return stringSpatial;

                    case PRIMITIVE_GEOMETRY:

                        GeometricPrimitive geomPrim = (GeometricPrimitive) opParam;
                        switch ( geomPrim.getPrimitiveType() ) {

                        case Point:
                            Point point = (Point) geomPrim;
                            stringSpatialGeometry += "GeomFromText('POINT(";
                            stringSpatialGeometry += point.get0();
                            stringSpatialGeometry += " ";
                            stringSpatialGeometry += point.get1();
                            if ( point.getCoordinateDimension() == 3 ) {
                                stringSpatialGeometry += " ";
                                stringSpatialGeometry += point.get2();
                            }
                            stringSpatialGeometry += ")')";

                            stringSpatial += stringSpatialGeometry;

                            break;
                        case Curve:
                            Curve curve = (Curve) geomPrim;
                            switch ( curve.getCurveType() ) {

                            case Curve:
                                break;

                            case LineString:
                                LineString lineString = (LineString) curve;
                                stringSpatialGeometry += "GeomFromText('LINESTRING(";
                                
                                
                                stringSpatialGeometry += lineString.getAsLineString();
                               
                                
                                stringSpatialGeometry += ")')";
                                stringSpatialGeom.add( stringSpatialGeometry );
                                stringSpatial += stringSpatialGeometry;
                                
                                break;

                            case OrientableCurve:

                                break;

                            case CompositeCurve:

                                break;

                            case Ring:

                                break;

                            }

                            break;
                        case Surface:

                            break;
                        case Solid:

                            break;
                        }

                        break;

                    case COMPOSITE_GEOMETRY:
                        

                        break;

                    case MULTI_GEOMETRY:

                        break;
                    }

                }
                if ( counter < paramsBeyond.length ) {
                    stringSpatial += ",";
                } else {
                    stringSpatial += ") <= " + beyondOp.getDistance().getValue().toString() + " AND ";
                }

            }
            stringSpatial += "DISJOINT(" + stringSpatialPropertyName + ",";
            //TODO counter if there are more stringSpatial
            for(String s : stringSpatialGeom){
                stringSpatial += s + ")";
            }
             
            System.out.println( stringSpatial );
            return stringSpatial;

        case CONTAINS:
            //TODO the same as Beyond -> receives an geometry as well
            return stringSpatial;

        case CROSSES:
          //TODO the same as Beyond -> receives an geometry as well
            return stringSpatial;

        case DISJOINT:
          //TODO the same as Beyond -> receives an geometry as well
            Disjoint disjointOp = (Disjoint) spaOp;
            Object[] paramsDisjoint = disjointOp.getParams();
            stringSpatial = "";

            for ( Object opParam : paramsDisjoint ) {

                if ( opParam != disjointOp.getParams() ) {
                    String exp = ( (PropertyName) opParam ).getPropertyName();
                    stringSpatial += expressionFilterHandling.expressionFilterHandling(
                                                               ( (PropertyName) opParam ).getType(),
                                                               new PropertyName(
                                                                                 exp,
                                                                                 ( (PropertyName) opParam ).getNsContext() ) );
                    stringSpatial += " && SetSRID('BOX3D( ";
                } else {
                    double[] minArray = ( (Envelope) opParam ).getMin().getAsArray();
                    double[] maxArray = ( (Envelope) opParam ).getMax().getAsArray();
                    for ( double min : minArray ) {
                        stringSpatial += min + " ";
                    }
                    stringSpatial += ",";
                    for ( double max : maxArray ) {
                        stringSpatial += max + " ";
                    }
                    stringSpatial += " )'::box3d, 4326)";
                }
                System.out.println( stringSpatial );

            }

            return stringSpatial;

        case DWITHIN:
          //TODO the same as Beyond -> receives an geometry as well
            DWithin dWithinOp = (DWithin) spaOp;
            Object[] paramsDWithin = dWithinOp.getParams();
            stringSpatial = "";

            for ( Object opParam : paramsDWithin ) {

                if ( opParam != dWithinOp.getParams() ) {
                    String exp = ( (PropertyName) opParam ).getPropertyName();
                    stringSpatial += expressionFilterHandling.expressionFilterHandling(
                                                               ( (PropertyName) opParam ).getType(),
                                                               new PropertyName(
                                                                                 exp,
                                                                                 ( (PropertyName) opParam ).getNsContext() ) );
                    stringSpatial += " && SetSRID('BOX3D( ";
                } else {
                    double[] minArray = ( (Envelope) opParam ).getMin().getAsArray();
                    double[] maxArray = ( (Envelope) opParam ).getMax().getAsArray();
                    for ( double min : minArray ) {
                        stringSpatial += min + " ";
                    }
                    stringSpatial += ",";
                    for ( double max : maxArray ) {
                        stringSpatial += max + " ";
                    }
                    stringSpatial += " )'::box3d, 4326)";
                }
                System.out.println( stringSpatial );

            }

            return stringSpatial;

        case EQUALS:
          //TODO the same as Beyond -> receives an geometry as well
            return stringSpatial;

        case INTERSECTS:
          //TODO the same as Beyond -> receives an geometry as well
            return stringSpatial;

        case OVERLAPS:
          //TODO the same as Beyond -> receives an geometry as well
            return stringSpatial;

        case TOUCHES:
          //TODO the same as Beyond -> receives an geometry as well
            return stringSpatial;

        case WITHIN:
          //TODO the same as Beyond -> receives an geometry as well
            return stringSpatial;

        }
        return stringSpatial;
        
        
    }
    
    
    
    

}
