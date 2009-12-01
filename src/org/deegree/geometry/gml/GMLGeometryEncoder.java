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
package org.deegree.geometry.gml;

import javax.xml.stream.XMLStreamException;

import org.deegree.crs.exceptions.TransformationException;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.composite.CompositeCurve;
import org.deegree.geometry.composite.CompositeGeometry;
import org.deegree.geometry.composite.CompositeSolid;
import org.deegree.geometry.composite.CompositeSurface;
import org.deegree.geometry.gml.refs.GeometryReference;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Solid;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.Tin;
import org.deegree.geometry.primitive.TriangulatedSurface;

/**
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public interface GMLGeometryEncoder {

    /**
     * @param geometry
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void export( Geometry geometry )
                            throws XMLStreamException, UnknownCRSException, TransformationException;

    /**
     * @param compositeCurve
     * @throws Exception
     */
    public void exportCompositeCurve( CompositeCurve compositeCurve )
                            throws Exception;

    /**
     * @param geometryComplex
     * @throws Exception
     */
    public void exportCompositeGeometry( CompositeGeometry<GeometricPrimitive> geometryComplex )
                            throws Exception;

    /**
     * @param compositeSolid
     * @throws Exception
     */
    public void exportCompositeSolid( CompositeSolid compositeSolid )
                            throws Exception;

    /**
     * @param compositeSurface
     * @throws Exception
     */
    public void exportCompositeSurface( CompositeSurface compositeSurface )
                            throws Exception;

    /**
     * @param curve
     * @throws Exception
     */
    public void exportCurve( Curve curve )
                            throws Exception;

    /**
     * @param envelope
     * @throws Exception
     */
    public void exportEnvelope( Envelope envelope )
                            throws Exception;

    /**
     * @param geometry
     * @throws Exception
     */
    public void exportMultiGeometry( MultiGeometry<? extends Geometry> geometry )
                            throws Exception;

    /**
     * @param point
     * @throws Exception
     */
    public void exportPoint( Point point )
                            throws Exception;

    /**
     * @param geometryRef
     * @throws Exception
     */
    public void exportReference( GeometryReference<Geometry> geometryRef )
                            throws Exception;

    /**
     * @param ring
     * @throws Exception
     */
    public void exportRing( Ring ring )
                            throws Exception;

    /**
     * @param solid
     * @throws Exception
     */
    public void exportSolid( Solid solid )
                            throws Exception;

    /**
     * @param surface
     * @throws Exception
     */
    public void exportSurface( Surface surface )
                            throws Exception;

    /**
     * @param tin
     * @throws Exception
     */
    public void exportTin( Tin tin )
                            throws Exception;

    /**
     * @param triangSurface
     * @throws Exception
     */
    public void exportTriangulatedSurface( TriangulatedSurface triangSurface )
                            throws Exception;
}
