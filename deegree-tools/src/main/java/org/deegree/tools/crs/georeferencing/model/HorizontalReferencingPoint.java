package org.deegree.tools.crs.georeferencing.model;

import javax.vecmath.Point2d;

import org.deegree.commons.utils.Pair;

public class HorizontalReferencingPoint extends Pair<Point2d, Point2d> {

    private Pair<Point2d, Point2d> pair1;

    private Pair<Point2d, Point2d> pair2;

    private Pair<Point2d, Point2d> pair3;

    private Pair<Point2d, Point2d>[] pairArray;

    public HorizontalReferencingPoint( Pair<Point2d, Point2d> pair1, Pair<Point2d, Point2d> pair2,
                                       Pair<Point2d, Point2d> pair3 ) {
        this.pair1 = pair1;
        this.pair2 = pair2;
        this.pair3 = pair3;
        this.pairArray = new Pair[] { pair1, pair2, pair3 };

    }

    public HorizontalReferencingPoint( Pair<Point2d, Point2d> pair1, Pair<Point2d, Point2d> pair2 ) {
        this( pair1, pair2, null );
    }

    public HorizontalReferencingPoint( Pair<Point2d, Point2d> pair1 ) {
        this( pair1, null );
    }

    public HorizontalReferencingPoint() {
        this.pairArray = new Pair[] {};
    }

    public Pair<Point2d, Point2d> getPair1() {
        return pair1;
    }

    public void setPair1( Pair<Point2d, Point2d> pair1 ) {
        this.pair1 = pair1;
        this.pairArray[0] = pair1;
    }

    public Pair<Point2d, Point2d> getPair2() {
        return pair2;
    }

    public void setPair2( Pair<Point2d, Point2d> pair2 ) {
        this.pair2 = pair2;
        this.pairArray[1] = pair2;
    }

    public Pair<Point2d, Point2d> getPair3() {
        return pair3;
    }

    public void setPair3( Pair<Point2d, Point2d> pair3 ) {
        this.pair3 = pair3;
        this.pairArray[2] = pair3;
    }

    public Pair<Point2d, Point2d>[] getPairArray() {
        return pairArray;
    }

}
