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
 * Class SurfaceInterpolationType.
 * 
 * @version $Revision$ $Date$
 */
public class SurfaceInterpolationType implements java.io.Serializable {

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * The none type
     */
    public static final int NONE_TYPE = 0;

    /**
     * The instance of the none type
     */
    public static final SurfaceInterpolationType NONE = new SurfaceInterpolationType( NONE_TYPE, "none" );

    /**
     * The planar type
     */
    public static final int PLANAR_TYPE = 1;

    /**
     * The instance of the planar type
     */
    public static final SurfaceInterpolationType PLANAR = new SurfaceInterpolationType( PLANAR_TYPE, "planar" );

    /**
     * The spherical type
     */
    public static final int SPHERICAL_TYPE = 2;

    /**
     * The instance of the spherical type
     */
    public static final SurfaceInterpolationType SPHERICAL = new SurfaceInterpolationType( SPHERICAL_TYPE, "spherical" );

    /**
     * The elliptical type
     */
    public static final int ELLIPTICAL_TYPE = 3;

    /**
     * The instance of the elliptical type
     */
    public static final SurfaceInterpolationType ELLIPTICAL = new SurfaceInterpolationType( ELLIPTICAL_TYPE,
                                                                                            "elliptical" );

    /**
     * The conic type
     */
    public static final int CONIC_TYPE = 4;

    /**
     * The instance of the conic type
     */
    public static final SurfaceInterpolationType CONIC = new SurfaceInterpolationType( CONIC_TYPE, "conic" );

    /**
     * The tin type
     */
    public static final int TIN_TYPE = 5;

    /**
     * The instance of the tin type
     */
    public static final SurfaceInterpolationType TIN = new SurfaceInterpolationType( TIN_TYPE, "tin" );

    /**
     * The bilinear type
     */
    public static final int BILINEAR_TYPE = 6;

    /**
     * The instance of the bilinear type
     */
    public static final SurfaceInterpolationType BILINEAR = new SurfaceInterpolationType( BILINEAR_TYPE, "bilinear" );

    /**
     * The biquadratic type
     */
    public static final int BIQUADRATIC_TYPE = 7;

    /**
     * The instance of the biquadratic type
     */
    public static final SurfaceInterpolationType BIQUADRATIC = new SurfaceInterpolationType( BIQUADRATIC_TYPE,
                                                                                             "biquadratic" );

    /**
     * The bicubic type
     */
    public static final int BICUBIC_TYPE = 8;

    /**
     * The instance of the bicubic type
     */
    public static final SurfaceInterpolationType BICUBIC = new SurfaceInterpolationType( BICUBIC_TYPE, "bicubic" );

    /**
     * The polynomialSpline type
     */
    public static final int POLYNOMIALSPLINE_TYPE = 9;

    /**
     * The instance of the polynomialSpline type
     */
    public static final SurfaceInterpolationType POLYNOMIALSPLINE = new SurfaceInterpolationType(
                                                                                                  POLYNOMIALSPLINE_TYPE,
                                                                                                  "polynomialSpline" );

    /**
     * The rationalSpline type
     */
    public static final int RATIONALSPLINE_TYPE = 10;

    /**
     * The instance of the rationalSpline type
     */
    public static final SurfaceInterpolationType RATIONALSPLINE = new SurfaceInterpolationType( RATIONALSPLINE_TYPE,
                                                                                                "rationalSpline" );

    /**
     * The triangulatedSpline type
     */
    public static final int TRIANGULATEDSPLINE_TYPE = 11;

    /**
     * The instance of the triangulatedSpline type
     */
    public static final SurfaceInterpolationType TRIANGULATEDSPLINE = new SurfaceInterpolationType(
                                                                                                    TRIANGULATEDSPLINE_TYPE,
                                                                                                    "triangulatedSpline" );

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

    private SurfaceInterpolationType( final int type, final java.lang.String value ) {
        super();
        this.type = type;
        this.stringValue = value;
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     * Method enumerate.Returns an enumeration of all possible instances of SurfaceInterpolationType
     * 
     * @return an Enumeration over all possible instances of SurfaceInterpolationType
     */
    public static java.util.Enumeration enumerate() {
        return _memberTable.elements();
    }

    /**
     * Method getType.Returns the type of this SurfaceInterpolationType
     * 
     * @return the type of this SurfaceInterpolationType
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
        Hashtable<String, SurfaceInterpolationType> members = new Hashtable<String, SurfaceInterpolationType>();
        members.put( "none", NONE );
        members.put( "planar", PLANAR );
        members.put( "spherical", SPHERICAL );
        members.put( "elliptical", ELLIPTICAL );
        members.put( "conic", CONIC );
        members.put( "tin", TIN );
        members.put( "bilinear", BILINEAR );
        members.put( "biquadratic", BIQUADRATIC );
        members.put( "bicubic", BICUBIC );
        members.put( "polynomialSpline", POLYNOMIALSPLINE );
        members.put( "rationalSpline", RATIONALSPLINE );
        members.put( "triangulatedSpline", TRIANGULATEDSPLINE );
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
     * Method toString.Returns the String representation of this SurfaceInterpolationType
     * 
     * @return the String representation of this SurfaceInterpolationType
     */
    public java.lang.String toString() {
        return this.stringValue;
    }

    /**
     * Method valueOf.Returns a new SurfaceInterpolationType based on the given String value.
     * 
     * @param string
     * @return the SurfaceInterpolationType value of parameter 'string'
     */
    public static org.deegree.model.configuration.types.SurfaceInterpolationType valueOf( final java.lang.String string ) {
        java.lang.Object obj = null;
        if ( string != null ) {
            obj = _memberTable.get( string );
        }
        if ( obj == null ) {
            String err = "" + string + " is not a valid SurfaceInterpolationType";
            throw new IllegalArgumentException( err );
        }
        return (SurfaceInterpolationType) obj;
    }

}
