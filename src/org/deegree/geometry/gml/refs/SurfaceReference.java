//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.geometry.gml.refs;

import java.util.List;

import org.deegree.commons.uom.Measure;
import org.deegree.commons.uom.Unit;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.gml.GMLObjectResolver;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @param <T>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class SurfaceReference<T extends Surface> extends GeometricPrimitiveReference<T> implements Surface {

    /**
     * Creates a new {@link SurfaceReference} instance.
     * 
     * @param resolver
     *            used for resolving the reference, must not be <code>null</code>
     * @param uri
     *            the geometry's uri, must not be <code>null</code>
     * @param baseURL
     *            base URL for resolving the uri, may be <code>null</code> (no resolving of relative URLs)
     */
    public SurfaceReference( GMLObjectResolver resolver, String uri, String baseURL ) {
        super( resolver, uri, baseURL );
    }

    @Override
    public Measure getArea( Unit requestedBaseUnit ) {
        return getReferencedGeometry().getArea( requestedBaseUnit );
    }

    @Override
    public SurfaceType getSurfaceType() {
        return getReferencedGeometry().getSurfaceType();
    }

    @Override
    public Point getCentroid() {
        return getReferencedGeometry().getCentroid();
    }

    @Override
    public Points getExteriorRingCoordinates() {
        return getReferencedGeometry().getExteriorRingCoordinates();
    }

    @Override
    public List<Points> getInteriorRingsCoordinates() {
        return getReferencedGeometry().getInteriorRingsCoordinates();
    }

    @Override
    public List<? extends SurfacePatch> getPatches() {
        return getReferencedGeometry().getPatches();
    }

    @Override
    public Measure getPerimeter( Unit requestedUnit ) {
        return getReferencedGeometry().getPerimeter( requestedUnit );
    }
}
