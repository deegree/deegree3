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
 * Class GeometryTypeType.
 * 
 * @version $Revision$ $Date$
 */
public class GeometryTypeType implements java.io.Serializable {

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * The Curve type
     */
    public static final int CURVE_TYPE = 0;

    /**
     * The instance of the Curve type
     */
    public static final GeometryTypeType CURVE = new GeometryTypeType( CURVE_TYPE, "Curve" );

    /**
     * The MultiCurve type
     */
    public static final int MULTICURVE_TYPE = 1;

    /**
     * The instance of the MultiCurve type
     */
    public static final GeometryTypeType MULTICURVE = new GeometryTypeType( MULTICURVE_TYPE, "MultiCurve" );

    /**
     * The MultiGeometry type
     */
    public static final int MULTIGEOMETRY_TYPE = 2;

    /**
     * The instance of the MultiGeometry type
     */
    public static final GeometryTypeType MULTIGEOMETRY = new GeometryTypeType( MULTIGEOMETRY_TYPE, "MultiGeometry" );

    /**
     * The MultiPoint type
     */
    public static final int MULTIPOINT_TYPE = 3;

    /**
     * The instance of the MultiPoint type
     */
    public static final GeometryTypeType MULTIPOINT = new GeometryTypeType( MULTIPOINT_TYPE, "MultiPoint" );

    /**
     * The MultiSurface type
     */
    public static final int MULTISURFACE_TYPE = 4;

    /**
     * The instance of the MultiSurface type
     */
    public static final GeometryTypeType MULTISURFACE = new GeometryTypeType( MULTISURFACE_TYPE, "MultiSurface" );

    /**
     * The Point type
     */
    public static final int POINT_TYPE = 5;

    /**
     * The instance of the Point type
     */
    public static final GeometryTypeType POINT = new GeometryTypeType( POINT_TYPE, "Point" );

    /**
     * The Solid type
     */
    public static final int SOLID_TYPE = 6;

    /**
     * The instance of the Solid type
     */
    public static final GeometryTypeType SOLID = new GeometryTypeType( SOLID_TYPE, "Solid" );

    /**
     * The Surface type
     */
    public static final int SURFACE_TYPE = 7;

    /**
     * The instance of the Surface type
     */
    public static final GeometryTypeType SURFACE = new GeometryTypeType( SURFACE_TYPE, "Surface" );

    /**
     * The ComplexCurve type
     */
    public static final int COMPLEXCURVE_TYPE = 8;

    /**
     * The instance of the ComplexCurve type
     */
    public static final GeometryTypeType COMPLEXCURVE = new GeometryTypeType( COMPLEXCURVE_TYPE, "ComplexCurve" );

    /**
     * The ComplexSolid type
     */
    public static final int COMPLEXSOLID_TYPE = 9;

    /**
     * The instance of the ComplexSolid type
     */
    public static final GeometryTypeType COMPLEXSOLID = new GeometryTypeType( COMPLEXSOLID_TYPE, "ComplexSolid" );

    /**
     * The ComplexSurface type
     */
    public static final int COMPLEXSURFACE_TYPE = 10;

    /**
     * The instance of the ComplexSurface type
     */
    public static final GeometryTypeType COMPLEXSURFACE = new GeometryTypeType( COMPLEXSURFACE_TYPE, "ComplexSurface" );

    /**
     * The GeometricComplex type
     */
    public static final int GEOMETRICCOMPLEX_TYPE = 11;

    /**
     * The instance of the GeometricComplex type
     */
    public static final GeometryTypeType GEOMETRICCOMPLEX = new GeometryTypeType( GEOMETRICCOMPLEX_TYPE,
                                                                                  "GeometricComplex" );

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

    private GeometryTypeType( final int type, final java.lang.String value ) {
        super();
        this.type = type;
        this.stringValue = value;
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     * Method enumerate.Returns an enumeration of all possible instances of GeometryTypeType
     * 
     * @return an Enumeration over all possible instances of GeometryTypeType
     */
    public static java.util.Enumeration enumerate() {
        return _memberTable.elements();
    }

    /**
     * Method getType.Returns the type of this GeometryTypeType
     * 
     * @return the type of this GeometryTypeType
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
        Hashtable<String, GeometryTypeType> members = new Hashtable<String, GeometryTypeType>();
        members.put( "Curve", CURVE );
        members.put( "MultiCurve", MULTICURVE );
        members.put( "MultiGeometry", MULTIGEOMETRY );
        members.put( "MultiPoint", MULTIPOINT );
        members.put( "MultiSurface", MULTISURFACE );
        members.put( "Point", POINT );
        members.put( "Solid", SOLID );
        members.put( "Surface", SURFACE );
        members.put( "ComplexCurve", COMPLEXCURVE );
        members.put( "ComplexSolid", COMPLEXSOLID );
        members.put( "ComplexSurface", COMPLEXSURFACE );
        members.put( "GeometricComplex", GEOMETRICCOMPLEX );
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
     * Method toString.Returns the String representation of this GeometryTypeType
     * 
     * @return the String representation of this GeometryTypeType
     */
    public java.lang.String toString() {
        return this.stringValue;
    }

    /**
     * Method valueOf.Returns a new GeometryTypeType based on the given String value.
     * 
     * @param string
     * @return the GeometryTypeType value of parameter 'string'
     */
    public static org.deegree.model.configuration.types.GeometryTypeType valueOf( final java.lang.String string ) {
        java.lang.Object obj = null;
        if ( string != null ) {
            obj = _memberTable.get( string );
        }
        if ( obj == null ) {
            String err = "" + string + " is not a valid GeometryTypeType";
            throw new IllegalArgumentException( err );
        }
        return (GeometryTypeType) obj;
    }

}
