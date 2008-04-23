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
 * Class GeometryFactoriesType.
 * 
 * @version $Revision$ $Date$
 */
public class GeometryFactoriesType implements java.io.Serializable {

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * 
     */
    private static final long serialVersionUID = -1153352325252831864L;

    /**
     * Field _geometryFactoryList.
     */
    private List<GeometryFactory> _geometryFactoryList;

    // ----------------/
    // - Constructors -/
    // ----------------/

    public GeometryFactoriesType() {
        super();
        this._geometryFactoryList = new ArrayList<GeometryFactory>();
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     * 
     * 
     * @param vGeometryFactory
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addGeometryFactory( final GeometryFactory vGeometryFactory )
                            throws java.lang.IndexOutOfBoundsException {
        this._geometryFactoryList.add( vGeometryFactory );
    }

    /**
     * 
     * 
     * @param index
     * @param vGeometryFactory
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addGeometryFactory( final int index, final GeometryFactory vGeometryFactory )
                            throws java.lang.IndexOutOfBoundsException {
        this._geometryFactoryList.add( index, vGeometryFactory );
    }

    /**
     * Method enumerateGeometryFactory.
     * 
     * @return an Enumeration over all GeometryFactory elements
     */
    public Iterator enumerateGeometryFactory() {
        return this._geometryFactoryList.iterator();
    }

    /**
     * Method getGeometryFactory.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     * @return the value of the GeometryFactory at the given index
     */
    public GeometryFactory getGeometryFactory( final int index )
                            throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if ( index < 0 || index >= this._geometryFactoryList.size() )
            throw new IndexOutOfBoundsException( "getGeometryFactory: Index value '" + index + "' not in range [0.."
                                                 + ( this._geometryFactoryList.size() - 1 ) + "]" );

        return this._geometryFactoryList.get( index );
    }

    /**
     * Method getGeometryFactory.Returns the contents of the collection in an Array.
     * <p>
     * Note: Just in case the collection contents are changing in another thread, we pass a 0-length
     * Array of the correct type into the API call. This way we <i>know</i> that the Array returned
     * is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public GeometryFactory[] getGeometryFactory() {
        GeometryFactory[] array = new GeometryFactory[0];
        return this._geometryFactoryList.toArray( array );
    }

    /**
     * Method getGeometryFactoryCount.
     * 
     * @return the size of this collection
     */
    public int getGeometryFactoryCount() {
        return this._geometryFactoryList.size();
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
    public void removeAllGeometryFactory() {
        this._geometryFactoryList.clear();
    }

    /**
     * Method removeGeometryFactory.
     * 
     * @param vGeometryFactory
     * @return true if the object was removed from the collection.
     */
    public boolean removeGeometryFactory( final GeometryFactory vGeometryFactory ) {
        boolean removed = this._geometryFactoryList.remove( vGeometryFactory );
        return removed;
    }

    /**
     * Method removeGeometryFactoryAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public GeometryFactory removeGeometryFactoryAt( final int index ) {
        java.lang.Object obj = this._geometryFactoryList.remove( index );
        return (GeometryFactory) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vGeometryFactory
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void setGeometryFactory( final int index, final GeometryFactory vGeometryFactory )
                            throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if ( index < 0 || index >= this._geometryFactoryList.size() )
            throw new IndexOutOfBoundsException( "setGeometryFactory: Index value '" + index + "' not in range [0.."
                                                 + ( this._geometryFactoryList.size() - 1 ) + "]" );

        this._geometryFactoryList.set( index, vGeometryFactory );
    }

    /**
     * 
     * 
     * @param vGeometryFactoryArray
     */
    public void setGeometryFactory( final GeometryFactory[] vGeometryFactoryArray ) {
        // -- copy array
        this._geometryFactoryList.clear();

        for ( GeometryFactory element : vGeometryFactoryArray ) {
            this._geometryFactoryList.add( element );
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
     * @return the unmarshaled GeometryFactoriesType
     */
    public static GeometryFactoriesType unmarshal( final java.io.Reader reader )
                            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (GeometryFactoriesType) Unmarshaller.unmarshal( GeometryFactoriesType.class, reader );
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
