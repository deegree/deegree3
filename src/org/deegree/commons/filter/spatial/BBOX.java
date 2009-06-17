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
package org.deegree.commons.filter.spatial;

import static org.slf4j.LoggerFactory.getLogger;

import org.deegree.commons.filter.FilterEvaluationException;
import org.deegree.commons.filter.MatchableObject;
import org.deegree.commons.filter.expression.PropertyName;
import org.deegree.crs.exceptions.TransformationException;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.jaxen.JaxenException;
import org.slf4j.Logger;

/**
 * TODO add documentation here
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class BBOX extends SpatialOperator {

    private static final Logger LOG = getLogger( BBOX.class );

    private Envelope bbox;

    private PropertyName geometry;

    /**
     * @param bbox
     * @param geometry
     */
    public BBOX( Envelope bbox, PropertyName geometry ) {
        this.bbox = bbox;
        this.geometry = geometry;
    }

    public boolean evaluate( MatchableObject object )
                            throws FilterEvaluationException {
        try {
            Object o = object.getPropertyValue( geometry );
            if ( !( o instanceof Geometry ) ) {
                return false;
            }
            Geometry g = (Geometry) o;
            Envelope bbox = this.bbox;

            if ( !bbox.getCoordinateSystem().getWrappedCRS().equals( g.getCoordinateSystem().getWrappedCRS() ) ) {
                bbox = (Envelope) new GeometryTransformer( g.getCoordinateSystem().getWrappedCRS() ).transform( bbox );
            }
            return bbox.intersects( g );
        } catch ( JaxenException e ) {
            LOG.debug( "Stack trace", e );
            throw new FilterEvaluationException( "BBOX filter could not be evaluated, since "
                                                 + "the geometry value could not be extracted: "
                                                 + e.getLocalizedMessage() );
        } catch ( IllegalArgumentException e ) {
            LOG.error( "Unknown error", e );
        } catch ( TransformationException e ) {
            LOG.error( "Unknown error", e );
        } catch ( UnknownCRSException e ) {
            LOG.error( "Unknown error", e );
        }

        return false;
    }

    /**
     * @return the envelope
     */
    public Envelope getBoundingBox() {
        return bbox;
    }

    public String toString( String indent ) {
        return indent + bbox.toString();
    }
}
