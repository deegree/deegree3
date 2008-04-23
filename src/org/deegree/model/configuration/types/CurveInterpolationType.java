/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.deegree.model.configuration.types;

// ---------------------------------/
// - Imported classes and packages -/
// ---------------------------------/

import java.util.Hashtable;

/**
 * Class CurveInterpolationType.
 * 
 * @version $Revision$ $Date$
 */
public class CurveInterpolationType implements java.io.Serializable {

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * The linear type
     */
    public static final int LINEAR_TYPE = 0;

    /**
     * The instance of the linear type
     */
    public static final CurveInterpolationType LINEAR = new CurveInterpolationType( LINEAR_TYPE, "linear" );

    /**
     * The geodesic type
     */
    public static final int GEODESIC_TYPE = 1;

    /**
     * The instance of the geodesic type
     */
    public static final CurveInterpolationType GEODESIC = new CurveInterpolationType( GEODESIC_TYPE, "geodesic" );

    /**
     * The circularArc3Points type
     */
    public static final int CIRCULARARC3POINTS_TYPE = 2;

    /**
     * The instance of the circularArc3Points type
     */
    public static final CurveInterpolationType CIRCULARARC3POINTS = new CurveInterpolationType(
                                                                                                CIRCULARARC3POINTS_TYPE,
                                                                                                "circularArc3Points" );

    /**
     * The circularArc2PointWithBulge type
     */
    public static final int CIRCULARARC2POINTWITHBULGE_TYPE = 3;

    /**
     * The instance of the circularArc2PointWithBulge type
     */
    public static final CurveInterpolationType CIRCULARARC2POINTWITHBULGE = new CurveInterpolationType(
                                                                                                        CIRCULARARC2POINTWITHBULGE_TYPE,
                                                                                                        "circularArc2PointWithBulge" );

    /**
     * The elliptical type
     */
    public static final int ELLIPTICAL_TYPE = 4;

    /**
     * The instance of the elliptical type
     */
    public static final CurveInterpolationType ELLIPTICAL = new CurveInterpolationType( ELLIPTICAL_TYPE, "elliptical" );

    /**
     * The conic type
     */
    public static final int CONIC_TYPE = 5;

    /**
     * The instance of the conic type
     */
    public static final CurveInterpolationType CONIC = new CurveInterpolationType( CONIC_TYPE, "conic" );

    /**
     * The cubicSpline type
     */
    public static final int CUBICSPLINE_TYPE = 6;

    /**
     * The instance of the cubicSpline type
     */
    public static final CurveInterpolationType CUBICSPLINE = new CurveInterpolationType( CUBICSPLINE_TYPE,
                                                                                         "cubicSpline" );

    /**
     * The polynomialSpline type
     */
    public static final int POLYNOMIALSPLINE_TYPE = 7;

    /**
     * The instance of the polynomialSpline type
     */
    public static final CurveInterpolationType POLYNOMIALSPLINE = new CurveInterpolationType( POLYNOMIALSPLINE_TYPE,
                                                                                              "polynomialSpline" );

    /**
     * The rationalSpline type
     */
    public static final int RATIONALSPLINE_TYPE = 8;

    /**
     * The instance of the rationalSpline type
     */
    public static final CurveInterpolationType RATIONALSPLINE = new CurveInterpolationType( RATIONALSPLINE_TYPE,
                                                                                            "rationalSpline" );

    /**
     * Field _memberTable.
     */
    private static java.util.Hashtable _memberTable = init();

    /**
     * Field type.
     */
    private final int type;

    /**
     * Field stringValue.
     */
    private java.lang.String stringValue = null;

    // ----------------/
    // - Constructors -/
    // ----------------/

    private CurveInterpolationType( final int type, final java.lang.String value ) {
        super();
        this.type = type;
        this.stringValue = value;
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     * Method enumerate.Returns an enumeration of all possible instances of CurveInterpolationType
     * 
     * @return an Enumeration over all possible instances of CurveInterpolationType
     */
    public static java.util.Enumeration enumerate() {
        return _memberTable.elements();
    }

    /**
     * Method getType.Returns the type of this CurveInterpolationType
     * 
     * @return the type of this CurveInterpolationType
     */
    public int getType() {
        return this.type;
    }

    /**
     * Method init.
     * 
     * @return the initialized Hashtable for the member table
     */
    private static java.util.Hashtable init() {
        Hashtable<String, CurveInterpolationType> members = new Hashtable<String, CurveInterpolationType>();
        members.put( "linear", LINEAR );
        members.put( "geodesic", GEODESIC );
        members.put( "circularArc3Points", CIRCULARARC3POINTS );
        members.put( "circularArc2PointWithBulge", CIRCULARARC2POINTWITHBULGE );
        members.put( "elliptical", ELLIPTICAL );
        members.put( "conic", CONIC );
        members.put( "cubicSpline", CUBICSPLINE );
        members.put( "polynomialSpline", POLYNOMIALSPLINE );
        members.put( "rationalSpline", RATIONALSPLINE );
        return members;
    }

    /**
     * Method readResolve. will be called during deserialization to replace the deserialized object
     * with the correct constant instance.
     * 
     * @return this deserialized object
     */
    private java.lang.Object readResolve() {
        return valueOf( this.stringValue );
    }

    /**
     * Method toString.Returns the String representation of this CurveInterpolationType
     * 
     * @return the String representation of this CurveInterpolationType
     */
    public java.lang.String toString() {
        return this.stringValue;
    }

    /**
     * Method valueOf.Returns a new CurveInterpolationType based on the given String value.
     * 
     * @param string
     * @return the CurveInterpolationType value of parameter 'string
     */
    public static org.deegree.model.configuration.types.CurveInterpolationType valueOf( final java.lang.String string ) {
        java.lang.Object obj = null;
        if ( string != null ) {
            obj = _memberTable.get( string );
        }
        if ( obj == null ) {
            String err = "" + string + " is not a valid CurveInterpolationType";
            throw new IllegalArgumentException( err );
        }
        return (CurveInterpolationType) obj;
    }

}
