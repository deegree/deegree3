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
package org.deegree.gml.geometry;

import javax.xml.stream.XMLStreamException;

import org.deegree.crs.exceptions.TransformationException;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.composite.CompositeCurve;
import org.deegree.geometry.composite.CompositeGeometry;
import org.deegree.geometry.composite.CompositeSolid;
import org.deegree.geometry.composite.CompositeSurface;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Solid;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.Tin;
import org.deegree.geometry.primitive.TriangulatedSurface;
import org.deegree.gml.geometry.refs.GeometryReference;

/**
 * Interface that makes the usage of Geometry Encoders simpler, not depending on their version anymore. Any new version
 * of a GML encoder should implement this interface.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public interface GMLGeometryWriter {

    /**
     * Exports a general geometry. This is the method to call when there is no information about the geometry (the case
     * switching is done here).
     * 
     * @param geometry
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void export( Geometry geometry )
                            throws XMLStreamException, UnknownCRSException, TransformationException;

    /**
     * @param compositeCurve
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void exportCompositeCurve( CompositeCurve compositeCurve )
                            throws XMLStreamException, UnknownCRSException, TransformationException;

    /**
     * @param geometryComplex
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void exportCompositeGeometry( CompositeGeometry<GeometricPrimitive> geometryComplex )
                            throws XMLStreamException, UnknownCRSException, TransformationException;

    /**
     * @param compositeSolid
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void exportCompositeSolid( CompositeSolid compositeSolid )
                            throws XMLStreamException, UnknownCRSException, TransformationException;

    /**
     * @param compositeSurface
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void exportCompositeSurface( CompositeSurface compositeSurface )
                            throws XMLStreamException, UnknownCRSException, TransformationException;

    /**
     * @param curve
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void exportCurve( Curve curve )
                            throws XMLStreamException, UnknownCRSException, TransformationException;

    /**
     * @param envelope
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void exportEnvelope( Envelope envelope )
                            throws XMLStreamException, UnknownCRSException, TransformationException;

    /**
     * @param geometry
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void exportMultiGeometry( MultiGeometry<? extends Geometry> geometry )
                            throws XMLStreamException, UnknownCRSException, TransformationException;

    /**
     * @param point
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void exportPoint( Point point )
                            throws XMLStreamException, UnknownCRSException, TransformationException;

    /**
     * @param geometryRef
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void exportReference( GeometryReference<Geometry> geometryRef )
                            throws XMLStreamException, UnknownCRSException, TransformationException;

    /**
     * @param ring
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void exportRing( Ring ring )
                            throws XMLStreamException, UnknownCRSException, TransformationException;

    /**
     * @param solid
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void exportSolid( Solid solid )
                            throws XMLStreamException, UnknownCRSException, TransformationException;

    /**
     * @param surface
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void exportSurface( Surface surface )
                            throws XMLStreamException, UnknownCRSException, TransformationException;

    /**
     * @param tin
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void exportTin( Tin tin )
                            throws XMLStreamException, UnknownCRSException, TransformationException;

    /**
     * @param triangSurface
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void exportTriangulatedSurface( TriangulatedSurface triangSurface )
                            throws XMLStreamException, UnknownCRSException, TransformationException;
}
