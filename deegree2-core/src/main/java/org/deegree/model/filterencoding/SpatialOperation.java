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
package org.deegree.model.filterencoding;

import static org.deegree.model.filterencoding.OperationDefines.BBOX;

import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.feature.Feature;
import org.deegree.model.spatialschema.GMLGeometryAdapter;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.Surface;
import org.w3c.dom.Element;

/**
 * Encapsulates the information of a spatial_ops entity (as defined in the Filter DTD).
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:luigimarinucci@yahoo.com">Luigi Marinucci<a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class SpatialOperation extends AbstractOperation {

    private Geometry geometry;

    private PropertyName propertyName;

    // calvin added on 10/21/2003
    private double distance = -1;

    /**
     * Constructs a new SpatialOperation.
     *
     * @param operatorId
     *            The {@link OperationDefines} id
     * @param propertyName
     *            name of the property to do the spatial compare to.
     * @param geometry
     *            the geometry to do the spatial compare to.
     *
     * @see OperationDefines
     */
    public SpatialOperation( int operatorId, PropertyName propertyName, Geometry geometry ) {
        super( operatorId );
        this.propertyName = propertyName;
        this.geometry = geometry;
    }

    /**
     * Constructs a new SpatialOperation.
     *
     * @param operatorId
     *            The {@link OperationDefines} id
     * @param propertyName
     *            name of the property to do the spatial compare to.
     * @param geometry
     *            the geometry to do the spatial compare to.
     * @param d
     *            the distance (a buffer)
     *
     * @see OperationDefines Calvin added on 10/21/2003
     *
     */
    public SpatialOperation( int operatorId, PropertyName propertyName, Geometry geometry, double d ) {
        super( operatorId );
        this.propertyName = propertyName;
        this.geometry = geometry;
        this.distance = d;
    }

    /**
     * returns the distance for geo spatial comparsions such as DWithin or Beyond
     *
     * @return the distance for geo spatial comparsions such as DWithin or Beyond
     */
    public double getDistance() {
        return distance;
    }


    /**
     * Given a DOM-fragment, a corresponding Operation-object is built. This method recursively calls other buildFromDOM
     * () - methods to validate the structure of the DOM-fragment.
     *
     * @throws FilterConstructionException
     *             if the structure of the DOM-fragment is invalid
     */
    public static Operation buildFromDOM( Element element, boolean useVersion_1_0_0 )
                            throws FilterConstructionException {

        // check if root element's name is a spatial operator
        String name = element.getLocalName();
        int operatorId = OperationDefines.getIdByName( name );

        // every spatial operation must have exactly 2 elements (except BEYOND + DWITHIN)
        ElementList children = XMLTools.getChildElements( element );

        if ( operatorId == OperationDefines.DWITHIN || operatorId == OperationDefines.BEYOND ) {
            if ( children.getLength() != 3 ) {
                throw new FilterConstructionException( "'" + name + "' operator requires exactly 3 elements!" );
            }
        } else {
            if ( children.getLength() != 2 && operatorId != BBOX ) {
                throw new FilterConstructionException( "'" + name + "' operator requires exactly 2 elements!" );
            }
        }

        // first element must be a PropertyName-element
        Element child1 = children.item( 0 );
        if ( !child1.getLocalName().toLowerCase().equals( "propertyname" ) && operatorId != BBOX ) {
            throw new FilterConstructionException( "First element of every '" + name
                                                   + "'-operation (except BBOX) must be a 'PropertyName'-element!" );
        }

        PropertyName propertyName = null;
        if ( child1.getLocalName().toLowerCase().equals( "propertyname" ) && !child1.getTextContent().equals( "" ) ) {
            propertyName = (PropertyName) PropertyName.buildFromDOM( child1 );
        } else {
            if ( operatorId != BBOX ) {
                throw new FilterConstructionException( "First element of every '" + name
                                                       + "'-operation (except BBOX) must be a 'PropertyName'-element!" );
            }
        }

        // second element must be a GML Geometry-element
        Geometry geometry = null;
        Element child2 = children.item( 1 );

        // this is really not a good way of parsing...
        if ( !child1.getLocalName().toLowerCase().equals( "propertyname" ) ) {
            child2 = child1;
        }

        try {
            geometry = GMLGeometryAdapter.wrap( child2, null );
        } catch ( Exception e ) {
            throw new FilterConstructionException( "GML Geometry definition in '" + name + "'-operation is erroneous: "
                                                   + e.getMessage() );
        }
        if ( geometry == null ) {
            throw new FilterConstructionException( "Unable to parse GMLGeometry definition in '" + name
                                                   + "'-operation!" );
        }

        double dist = -1;

        // BEYOND + DWITHIN have an additional Distance-element
        if ( operatorId == OperationDefines.DWITHIN || operatorId == OperationDefines.BEYOND ) {
            Element child3 = children.item( 2 );

            if ( !child3.getLocalName().toLowerCase().equals( "distance" ) ) {
                throw new FilterConstructionException( "Name of element does not equal 'Distance'!" );
            }

            String distanceString = XMLTools.getStringValue( child3 );

            try {
                dist = Double.parseDouble( distanceString );
            } catch ( NumberFormatException e ) {
                throw new FilterConstructionException( "Distance value is not a number: " + distanceString );
            }
            if ( dist < 0 ) {
                throw new FilterConstructionException( "Distance value can't be negative: " + distanceString );
            }
        }

        switch ( operatorId ) {
        case OperationDefines.CROSSES:
        case OperationDefines.BEYOND:
        case OperationDefines.EQUALS:
        case OperationDefines.OVERLAPS:
        case OperationDefines.TOUCHES:
        case OperationDefines.DISJOINT:
        case OperationDefines.INTERSECTS:
        case OperationDefines.WITHIN:
        case OperationDefines.CONTAINS:
        case OperationDefines.DWITHIN:
            // every GMLGeometry is allowed as Literal-argument here
            break;
        case OperationDefines.BBOX: {
            if ( !( geometry instanceof Surface ) ) {
                String msg = "'" + name + "'operator  can only be used with 'Envelope'-geometries!";
                throw new FilterConstructionException( msg );
            }

            break;
        }
        default:
            throw new FilterConstructionException( "'" + name + "' is not a known spatial operator!" );
        }

        return new SpatialOperation( operatorId, propertyName, geometry, dist );
    }

    /**
     * Returns the geometry literal used in the operation.
     *
     * @return the literal as a <tt>GMLGeometry</tt>-object.
     */
    public Geometry getGeometry() {
        return this.geometry;
    }

    /**
     * sets a geometry for a spatial operation
     *
     * @param geometry
     */
    public void setGeometry( Geometry geometry ) {
        this.geometry = geometry;
    }

    /**
     * @return the name of the (spatial) property that shall be use for geo spatial comparsions
     */
    public PropertyName getPropertyName() {
        return this.propertyName;
    }

    public StringBuffer toXML() {
        return to110XML();
    }

    public StringBuffer to100XML() {
        StringBuffer sb = new StringBuffer( 2000 );
        sb.append( "<ogc:" ).append( getOperatorName() );
        sb.append( " xmlns:gml='http://www.opengis.net/gml' " ).append( ">" );
        if ( propertyName != null ) {
            sb.append( propertyName.toXML() );
        }
        try {
            if ( getOperatorName().equals( "BBOX" ) && geometry instanceof Surface ) {
                sb.append( GMLGeometryAdapter.exportAsBox( geometry.getEnvelope() ) );
            } else {
                sb.append( GMLGeometryAdapter.export( geometry ) );
            }
        } catch ( GeometryException e ) {
            e.printStackTrace();
        }
        if ( distance > 0 ) {
            sb.append( "<ogc:Distance units=\"m\">" ).append( distance ).append( "</ogc:Distance>" );
        }
        sb.append( "</ogc:" ).append( getOperatorName() ).append( ">" );
        return sb;
    }

    public StringBuffer to110XML() {
        StringBuffer sb = new StringBuffer( 2000 );
        sb.append( "<ogc:" ).append( getOperatorName() );
        sb.append( " xmlns:gml='http://www.opengis.net/gml' " ).append( ">" );
        if ( propertyName != null ) {
            sb.append( propertyName.toXML() );
        }
        try {
            if ( getOperatorName().equals( "BBOX" ) && geometry instanceof Surface ) {
                sb.append( GMLGeometryAdapter.exportAsEnvelope( geometry.getEnvelope() ) );
            } else {
                sb.append( GMLGeometryAdapter.export( geometry ) );
            }
        } catch ( GeometryException e ) {
            e.printStackTrace();
        }
        if ( distance > 0 ) {
            sb.append( "<ogc:Distance units=\"m\">" ).append( distance ).append( "</ogc:Distance>" );
        }
        sb.append( "</ogc:" ).append( getOperatorName() ).append( ">" );
        return sb;
    }

    /**
     * Calculates the <tt>SpatialOperation</tt>'s logical value based on the property values of the given
     * <tt>Feature</tt>.
     * <p>
     * TODO: Implement operations: CROSSES, BEYOND, OVERLAPS AND TOUCHES.
     * <p>
     *
     * @param feature
     *            that determines the property values
     * @return true, if the <tt>SpatialOperation</tt> evaluates to true, else false
     * @throws FilterEvaluationException
     *             if the evaluation fails
     */
    public boolean evaluate( Feature feature )
                            throws FilterEvaluationException {
        boolean value = false;

        Geometry geom = getGeometry( feature );
        if ( geom == null ) {
            return false;
        }

        switch ( operatorId ) {
        case OperationDefines.EQUALS:
            value = getGeometry( feature ).equals( getGeometry() );
        case OperationDefines.DISJOINT: {
            value = !getGeometry( feature ).intersects( getGeometry() );
            break;
        }
        case OperationDefines.WITHIN: {
            value = getGeometry().contains( getGeometry( feature ) );
            break;
        }
        case OperationDefines.CONTAINS: {
            value = getGeometry( feature ).contains( getGeometry() );
            break;
        }
        case OperationDefines.INTERSECTS:
        case OperationDefines.BBOX: {
            value = getGeometry( feature ).intersects( getGeometry() );
            break;
        }
            // calvin added on 10/21/2003
        case OperationDefines.DWITHIN: {
            value = getGeometry( feature ).isWithinDistance( getGeometry(), distance );
            break;
        }
        case OperationDefines.CROSSES:
        case OperationDefines.BEYOND:
        case OperationDefines.OVERLAPS:
        case OperationDefines.TOUCHES:
            throw new FilterEvaluationException( "Evaluation for spatial operation '"
                                                 + OperationDefines.getNameById( operatorId )
                                                 + "' is not implemented yet!" );
        default:
            throw new FilterEvaluationException( "Encountered unexpected operatorId: " + operatorId
                                                 + " in SpatialOperation.evaluate ()!" );
        }

        return value;
    }

    /**
     * Returns the value of the targeted geometry property.
     *
     * @param feature
     * @return the property value
     * @throws FilterEvaluationException
     *             if the PropertyName does not denote a Geometry
     */
    private Geometry getGeometry( Feature feature )
                            throws FilterEvaluationException {

        Geometry geom = null;

        if ( this.propertyName != null ) {
            Object propertyValue = this.propertyName.evaluate( feature );
            if ( !( propertyValue instanceof Geometry ) ) {
                String msg = "Cannot evaluate spatial operation: property '" + this.propertyName
                             + "' does not denote a geometry property.";
                throw new FilterEvaluationException( msg );
            }
            geom = (Geometry) propertyValue;
        } else {
            Geometry[] geoms = feature.getGeometryPropertyValues();
            if ( geoms == null || geoms.length < 1 ) {
                String msg = "Cannot evaluate spatial operation: feature has no geometry property.";
                throw new FilterEvaluationException( msg );
            }
            geom = geoms[0];
        }
        return geom;
    }

    /**
     * This is necessary for the BBOX operator, where the property name can/must be determined later.
     *
     * @param name
     */
    public void setPropertyName( PropertyName name ) {
        propertyName = name;
    }

}
