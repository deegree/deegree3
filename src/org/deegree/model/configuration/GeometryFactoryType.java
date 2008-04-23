/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.deegree.model.configuration;

// ---------------------------------/
// - Imported classes and packages -/
// ---------------------------------/

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class GeometryFactoryType.
 * 
 * @version $Revision$ $Date$
 */
public class GeometryFactoryType implements java.io.Serializable {

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * 
     */
    private static final long serialVersionUID = 2223722272817548986L;

    /**
     * Field _isDefault.
     */
    private boolean _isDefault = false;

    /**
     * keeps track of state for field: _isDefault
     */
    private boolean _has_isDefault;

    /**
     * Field _name.
     */
    private java.lang.String _name;

    /**
     * Field _className.
     */
    private java.lang.String _className;

    /**
     * Field _description.
     */
    private java.lang.String _description;

    /**
     * Field _initParamList.
     */
    private List<InitParam> _initParamList;

    /**
     * Field _supportedGeometries.
     */
    private SupportedGeometries _supportedGeometries;

    /**
     * Field _supportedCurveInterpolation.
     */
    private SupportedCurveInterpolation _supportedCurveInterpolation;

    /**
     * Field _supportedSurfaceInterpolation.
     */
    private SupportedSurfaceInterpolation _supportedSurfaceInterpolation;

    // ----------------/
    // - Constructors -/
    // ----------------/

    public GeometryFactoryType() {
        super();
        this._initParamList = new ArrayList<InitParam>();
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     * 
     * 
     * @param vInitParam
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addInitParam( final InitParam vInitParam )
                            throws java.lang.IndexOutOfBoundsException {
        this._initParamList.add( vInitParam );
    }

    /**
     * 
     * 
     * @param index
     * @param vInitParam
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addInitParam( final int index, final InitParam vInitParam )
                            throws java.lang.IndexOutOfBoundsException {
        this._initParamList.add( index, vInitParam );
    }

    /**
     */
    public void deleteIsDefault() {
        this._has_isDefault = false;
    }

    /**
     * Method enumerateInitParam.
     * 
     * @return an Enumeration over all InitParam elements
     */
    public Iterator enumerateInitParam() {
        return this._initParamList.iterator();
    }

    /**
     * Returns the value of field 'className'.
     * 
     * @return the value of field 'ClassName'.
     */
    public java.lang.String getClassName() {
        return this._className;
    }

    /**
     * Returns the value of field 'description'.
     * 
     * @return the value of field 'Description'.
     */
    public java.lang.String getDescription() {
        return this._description;
    }

    /**
     * Method getInitParam.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     * @return the value of the InitParam at the given index
     */
    public InitParam getInitParam( final int index )
                            throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if ( index < 0 || index >= this._initParamList.size() )
            throw new IndexOutOfBoundsException( "getInitParam: Index value '" + index + "' not in range [0.."
                                                 + ( this._initParamList.size() - 1 ) + "]" );

        return this._initParamList.get( index );
    }

    /**
     * Method getInitParam.Returns the contents of the collection in an Array.
     * <p>
     * Note: Just in case the collection contents are changing in another thread, we pass a 0-length
     * Array of the correct type into the API call. This way we <i>know</i> that the Array returned
     * is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public InitParam[] getInitParam() {
        InitParam[] array = new InitParam[0];
        return this._initParamList.toArray( array );
    }

    /**
     * Method getInitParamCount.
     * 
     * @return the size of this collection
     */
    public int getInitParamCount() {
        return this._initParamList.size();
    }

    /**
     * Returns the value of field 'isDefault'.
     * 
     * @return the value of field 'IsDefault'.
     */
    public boolean getIsDefault() {
        return this._isDefault;
    }

    /**
     * Returns the value of field 'name'.
     * 
     * @return the value of field 'Name'.
     */
    public java.lang.String getName() {
        return this._name;
    }

    /**
     * Returns the value of field 'supportedCurveInterpolation'.
     * 
     * @return the value of field 'SupportedCurveInterpolation'.
     */
    public SupportedCurveInterpolation getSupportedCurveInterpolation() {
        return this._supportedCurveInterpolation;
    }

    /**
     * Returns the value of field 'supportedGeometries'.
     * 
     * @return the value of field 'SupportedGeometries'.
     */
    public SupportedGeometries getSupportedGeometries() {
        return this._supportedGeometries;
    }

    /**
     * Returns the value of field 'supportedSurfaceInterpolation'.
     * 
     * @return the value of field 'SupportedSurfaceInterpolation'.
     */
    public SupportedSurfaceInterpolation getSupportedSurfaceInterpolation() {
        return this._supportedSurfaceInterpolation;
    }

    /**
     * Method hasIsDefault.
     * 
     * @return true if at least one IsDefault has been added
     */
    public boolean hasIsDefault() {
        return this._has_isDefault;
    }

    /**
     * Returns the value of field 'isDefault'.
     * 
     * @return the value of field 'IsDefault'.
     */
    public boolean isIsDefault() {
        return this._isDefault;
    }

    /**
     * Method isValid.
     * 
     * @return true if this object is valid according to the schema
     */
    public boolean isValid() {
        try {
            validate();
        } catch ( org.exolab.castor.xml.ValidationException vex ) {
            return false;
        }
        return true;
    }

    /**
     * 
     * 
     * @param out
     * @throws org.exolab.castor.xml.MarshalException
     *             if object is null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException
     *             if this object is an invalid instance according to the schema
     */
    public void marshal( final java.io.Writer out )
                            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        Marshaller.marshal( this, out );
    }

    /**
     * 
     * 
     * @param handler
     * @throws java.io.IOException
     *             if an IOException occurs during marshaling
     * @throws org.exolab.castor.xml.ValidationException
     *             if this object is an invalid instance according to the schema
     * @throws org.exolab.castor.xml.MarshalException
     *             if object is null or if any SAXException is thrown during marshaling
     */
    public void marshal( final org.xml.sax.ContentHandler handler )
                            throws java.io.IOException, org.exolab.castor.xml.MarshalException,
                            org.exolab.castor.xml.ValidationException {
        Marshaller.marshal( this, handler );
    }

    /**
     */
    public void removeAllInitParam() {
        this._initParamList.clear();
    }

    /**
     * Method removeInitParam.
     * 
     * @param vInitParam
     * @return true if the object was removed from the collection.
     */
    public boolean removeInitParam( final InitParam vInitParam ) {
        boolean removed = this._initParamList.remove( vInitParam );
        return removed;
    }

    /**
     * Method removeInitParamAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public InitParam removeInitParamAt( final int index ) {
        java.lang.Object obj = this._initParamList.remove( index );
        return (InitParam) obj;
    }

    /**
     * Sets the value of field 'className'.
     * 
     * @param className
     *            the value of field 'className'.
     */
    public void setClassName( final java.lang.String className ) {
        this._className = className;
    }

    /**
     * Sets the value of field 'description'.
     * 
     * @param description
     *            the value of field 'description'.
     */
    public void setDescription( final java.lang.String description ) {
        this._description = description;
    }

    /**
     * 
     * 
     * @param index
     * @param vInitParam
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void setInitParam( final int index, final InitParam vInitParam )
                            throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if ( index < 0 || index >= this._initParamList.size() )
            throw new IndexOutOfBoundsException( "setInitParam: Index value '" + index + "' not in range [0.."
                                                 + ( this._initParamList.size() - 1 ) + "]" );

        this._initParamList.set( index, vInitParam );
    }

    /**
     * 
     * 
     * @param vInitParamArray
     */
    public void setInitParam( final InitParam[] vInitParamArray ) {
        // -- copy array
        this._initParamList.clear();

        for ( InitParam element : vInitParamArray ) {
            this._initParamList.add( element );
        }
    }

    /**
     * Sets the value of field 'isDefault'.
     * 
     * @param isDefault
     *            the value of field 'isDefault'.
     */
    public void setIsDefault( final boolean isDefault ) {
        this._isDefault = isDefault;
        this._has_isDefault = true;
    }

    /**
     * Sets the value of field 'name'.
     * 
     * @param name
     *            the value of field 'name'.
     */
    public void setName( final java.lang.String name ) {
        this._name = name;
    }

    /**
     * Sets the value of field 'supportedCurveInterpolation'.
     * 
     * @param supportedCurveInterpolation
     *            the value of field 'supportedCurveInterpolation'.
     */
    public void setSupportedCurveInterpolation( final SupportedCurveInterpolation supportedCurveInterpolation ) {
        this._supportedCurveInterpolation = supportedCurveInterpolation;
    }

    /**
     * Sets the value of field 'supportedGeometries'.
     * 
     * @param supportedGeometries
     *            the value of field 'supportedGeometries'.
     */
    public void setSupportedGeometries( final SupportedGeometries supportedGeometries ) {
        this._supportedGeometries = supportedGeometries;
    }

    /**
     * Sets the value of field 'supportedSurfaceInterpolation'.
     * 
     * @param supportedSurfaceInterpolation
     *            the value of field 'supportedSurfaceInterpolation'.
     */
    public void setSupportedSurfaceInterpolation( final SupportedSurfaceInterpolation supportedSurfaceInterpolation ) {
        this._supportedSurfaceInterpolation = supportedSurfaceInterpolation;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException
     *             if object is null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException
     *             if this object is an invalid instance according to the schema
     * @return the unmarshaled GeometryFactoryType
     */
    public static GeometryFactoryType unmarshal( final java.io.Reader reader )
                            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (GeometryFactoryType) Unmarshaller.unmarshal( GeometryFactoryType.class, reader );
    }

    /**
     * 
     * 
     * @throws org.exolab.castor.xml.ValidationException
     *             if this object is an invalid instance according to the schema
     */
    public void validate()
                            throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate( this );
    }

}
