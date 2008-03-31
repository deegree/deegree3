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

import org.deegree.model.configuration.types.GeometryTypeType;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class SupportedGeometriesType.
 * 
 * @version $Revision$ $Date$
 */
public class SupportedGeometriesType implements java.io.Serializable {

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * 
     */
    private static final long serialVersionUID = 483206754934543749L;

    /**
     * Field _geometryTypeList.
     */
    private java.util.Vector _geometryTypeList;

    // ----------------/
    // - Constructors -/
    // ----------------/

    public SupportedGeometriesType() {
        super();
        this._geometryTypeList = new java.util.Vector();
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     * 
     * 
     * @param vGeometryType
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addGeometryType( final org.deegree.model.configuration.types.GeometryTypeType vGeometryType )
                            throws java.lang.IndexOutOfBoundsException {
        this._geometryTypeList.addElement( vGeometryType );
    }

    /**
     * 
     * 
     * @param index
     * @param vGeometryType
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addGeometryType( final int index,
                                 final org.deegree.model.configuration.types.GeometryTypeType vGeometryType )
                            throws java.lang.IndexOutOfBoundsException {
        this._geometryTypeList.add( index, vGeometryType );
    }

    /**
     * Method enumerateGeometryType.
     * 
     * @return an Enumeration over all org.deegree.model.configuration.types.GeometryTypeType
     *         elements
     */
    public java.util.Enumeration enumerateGeometryType() {
        return this._geometryTypeList.elements();
    }

    /**
     * Method getGeometryType.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     * @return the value of the org.deegree.model.configuration.types.GeometryTypeType at the given
     *         index
     */
    public org.deegree.model.configuration.types.GeometryTypeType getGeometryType( final int index )
                            throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if ( index < 0 || index >= this._geometryTypeList.size() )
            throw new IndexOutOfBoundsException( "getGeometryType: Index value '" + index + "' not in range [0.."
                                                 + ( this._geometryTypeList.size() - 1 ) + "]" );

        return (org.deegree.model.configuration.types.GeometryTypeType) this._geometryTypeList.get( index );
    }

    /**
     * Method getGeometryType.Returns the contents of the collection in an Array.
     * <p>
     * Note: Just in case the collection contents are changing in another thread, we pass a 0-length
     * Array of the correct type into the API call. This way we <i>know</i> that the Array returned
     * is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.deegree.model.configuration.types.GeometryTypeType[] getGeometryType() {
        org.deegree.model.configuration.types.GeometryTypeType[] array = new org.deegree.model.configuration.types.GeometryTypeType[0];
        return (org.deegree.model.configuration.types.GeometryTypeType[]) this._geometryTypeList.toArray( array );
    }

    /**
     * Method getGeometryTypeCount.
     * 
     * @return the size of this collection
     */
    public int getGeometryTypeCount() {
        return this._geometryTypeList.size();
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
    public void removeAllGeometryType() {
        this._geometryTypeList.clear();
    }

    /**
     * Method removeGeometryType.
     * 
     * @param vGeometryType
     * @return true if the object was removed from the collection.
     */
    public boolean removeGeometryType( final org.deegree.model.configuration.types.GeometryTypeType vGeometryType ) {
        boolean removed = this._geometryTypeList.remove( vGeometryType );
        return removed;
    }

    /**
     * Method removeGeometryTypeAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.deegree.model.configuration.types.GeometryTypeType removeGeometryTypeAt( final int index ) {
        java.lang.Object obj = this._geometryTypeList.remove( index );
        return (org.deegree.model.configuration.types.GeometryTypeType) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vGeometryType
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void setGeometryType( final int index,
                                 final org.deegree.model.configuration.types.GeometryTypeType vGeometryType )
                            throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if ( index < 0 || index >= this._geometryTypeList.size() )
            throw new IndexOutOfBoundsException( "setGeometryType: Index value '" + index + "' not in range [0.."
                                                 + ( this._geometryTypeList.size() - 1 ) + "]" );

        this._geometryTypeList.set( index, vGeometryType );
    }

    /**
     * 
     * 
     * @param vGeometryTypeArray
     */
    public void setGeometryType( final org.deegree.model.configuration.types.GeometryTypeType[] vGeometryTypeArray ) {
        // -- copy array
        this._geometryTypeList.clear();

        for ( GeometryTypeType element : vGeometryTypeArray ) {
            this._geometryTypeList.add( element );
        }
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException
     *             if object is null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException
     *             if this object is an invalid instance according to the schema
     * @return the unmarshaled org.deegree.model.configuration.SupportedGeometriesType
     */
    public static org.deegree.model.configuration.SupportedGeometriesType unmarshal( final java.io.Reader reader )
                            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.deegree.model.configuration.SupportedGeometriesType) Unmarshaller.unmarshal(
                                                                                                 org.deegree.model.configuration.SupportedGeometriesType.class,
                                                                                                 reader );
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
