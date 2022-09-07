package org.deegree.style.styling.wkn.shape;

import static java.awt.geom.Path2D.WIND_EVEN_ODD;

import java.awt.Shape;
import java.awt.geom.GeneralPath;

import org.deegree.geometry.Geometry;
import org.deegree.geometry.composite.CompositeGeometry;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.patches.SurfacePatch;

public abstract class AbstractShapeConverter {

    public Shape convert( Geometry geometry ) {
        GeneralPath path = new GeneralPath( WIND_EVEN_ODD );
        toShape( path, geometry );
        return path;
    }

    protected abstract void toShape( GeneralPath path, Curve geometry );

    private void toShape( GeneralPath path, Geometry geometry ) {
        switch ( geometry.getGeometryType() ) {
        case ENVELOPE:
            // will be ignored
            break;
        case COMPOSITE_GEOMETRY:
            @SuppressWarnings("unchecked")
            CompositeGeometry<? extends GeometricPrimitive> comp = (CompositeGeometry<? extends GeometricPrimitive>) geometry;
            toShape( path, comp );
        case MULTI_GEOMETRY:
            @SuppressWarnings("unchecked")
            MultiGeometry<? extends Geometry> multi = (MultiGeometry<? extends Geometry>) geometry;
            toShape( path, multi );
            break;
        case PRIMITIVE_GEOMETRY:
            switch ( ( (GeometricPrimitive) geometry ).getPrimitiveType() ) {
            case Curve:
                toShape( path, (Curve) geometry );
                break;
            case Point:
            case Solid:
                // will be ignored
                break;
            case Surface:
                toShape( path, (Surface) geometry );
                break;
            }
            break;
        }
    }

    private void toShape( GeneralPath path, CompositeGeometry<? extends GeometricPrimitive> geometry ) {
        for ( Geometry geom : geometry ) {
            toShape( path, geom );
        }
    }

    private void toShape( GeneralPath path, MultiGeometry<? extends Geometry> geometry ) {
        for ( Geometry geom : geometry ) {
            toShape( path, geom );
        }
    }

    private void toShape( GeneralPath path, Surface surface ) {
        for ( SurfacePatch patch : surface.getPatches() ) {
            if ( patch instanceof PolygonPatch ) {
                PolygonPatch polygonPatch = (PolygonPatch) patch;
                for ( Curve curve : polygonPatch.getBoundaryRings() ) {
                    toShape( path, curve );
                }
            } else {
                throw new IllegalArgumentException( "Cannot render non-planar surfaces." );
            }
        }
    }
}
