package org.deegree.style.styling.wkn.shape;

import static org.deegree.commons.utils.math.MathUtils.isZero;

import java.awt.geom.GeneralPath;
import java.util.Iterator;

import org.deegree.geometry.linearization.GeometryLinearizer;
import org.deegree.geometry.linearization.LinearizationCriterion;
import org.deegree.geometry.linearization.NumPointsCriterion;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;

public class ShapeConverterLinearize extends AbstractShapeConverter {

    private static final GeometryLinearizer linearizer = new GeometryLinearizer();

    private final LinearizationCriterion crit;

    private final boolean close;

    public ShapeConverterLinearize( boolean close, int pointsPerArc ) {
        this.close = close;
        this.crit = new NumPointsCriterion( pointsPerArc );
    }

    @Override
    protected void toShape( GeneralPath path, Curve geometry ) {
        geometry = linearizer.linearize( geometry, crit );

        Points points = geometry.getControlPoints();
        Iterator<Point> iter = points.iterator();
        Point p = iter.next();
        double x = p.get0(), y = p.get1();
        path.moveTo( x, y );
        while ( iter.hasNext() ) {
            p = iter.next();
            if ( iter.hasNext() ) {
                path.lineTo( p.get0(), p.get1() );
            } else {
                if ( close && isZero( x - p.get0() ) && isZero( y - p.get1() ) ) {
                    path.closePath();
                } else {
                    path.lineTo( p.get0(), p.get1() );
                }
            }
        }
    }
}
