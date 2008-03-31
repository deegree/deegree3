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

import org.deegree.model.configuration.types.SurfaceInterpolationType;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class SupportedSurfaceInterpolationType.
 * 
 * @version $Revision$ $Date$
 */
public class SupportedSurfaceInterpolationType implements java.io.Serializable {

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * 
     */
    private static final long serialVersionUID = -6385265085294412645L;

    /**
     * Field _surfaceInterpolationList.
     */
    private java.util.Vector _surfaceInterpolationList;

    // ----------------/
    // - Constructors -/
    // ----------------/

    public SupportedSurfaceInterpolationType() {
        super();
        this._surfaceInterpolationList = new java.util.Vector();
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     * 
     * 
     * @param vSurfaceInterpolation
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addSurfaceInterpolation(
                                         final org.deegree.model.configuration.types.SurfaceInterpolationType vSurfaceInterpolation )
                            throws java.lang.IndexOutOfBoundsException {
        this._surfaceInterpolationList.addElement( vSurfaceInterpolation );
    }

    /**
     * 
     * 
     * @param index
     * @param vSurfaceInterpolation
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addSurfaceInterpolation(
                                         final int index,
                                         final org.deegree.model.configuration.types.SurfaceInterpolationType vSurfaceInterpolation )
                            throws java.lang.IndexOutOfBoundsException {
        this._surfaceInterpolationList.add( index, vSurfaceInterpolation );
    }

    /**
     * Method enumerateSurfaceInterpolation.
     * 
     * @return an Enumeration over all
     *         org.deegree.model.configuration.types.SurfaceInterpolationType elements
     */
    public java.util.Enumeration enumerateSurfaceInterpolation() {
        return this._surfaceInterpolationList.elements();
    }

    /**
     * Method getSurfaceInterpolation.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     * @return the value of the org.deegree.model.configuration.types.SurfaceInterpolationType at
     *         the given index
     */
    public org.deegree.model.configuration.types.SurfaceInterpolationType getSurfaceInterpolation( final int index )
                            throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if ( index < 0 || index >= this._surfaceInterpolationList.size() )
            throw new IndexOutOfBoundsException( "getSurfaceInterpolation: Index value '" + index
                                                 + "' not in range [0.." + ( this._surfaceInterpolationList.size() - 1 )
                                                 + "]" );

        return (org.deegree.model.configuration.types.SurfaceInterpolationType) this._surfaceInterpolationList.get( index );
    }

    /**
     * Method getSurfaceInterpolation.Returns the contents of the collection in an Array.
     * <p>
     * Note: Just in case the collection contents are changing in another thread, we pass a 0-length
     * Array of the correct type into the API call. This way we <i>know</i> that the Array returned
     * is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.deegree.model.configuration.types.SurfaceInterpolationType[] getSurfaceInterpolation() {
        org.deegree.model.configuration.types.SurfaceInterpolationType[] array = new org.deegree.model.configuration.types.SurfaceInterpolationType[0];
        return (org.deegree.model.configuration.types.SurfaceInterpolationType[]) this._surfaceInterpolationList.toArray( array );
    }

    /**
     * Method getSurfaceInterpolationCount.
     * 
     * @return the size of this collection
     */
    public int getSurfaceInterpolationCount() {
        return this._surfaceInterpolationList.size();
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
    public void removeAllSurfaceInterpolation() {
        this._surfaceInterpolationList.clear();
    }

    /**
     * Method removeSurfaceInterpolation.
     * 
     * @param vSurfaceInterpolation
     * @return true if the object was removed from the collection.
     */
    public boolean removeSurfaceInterpolation(
                                               final org.deegree.model.configuration.types.SurfaceInterpolationType vSurfaceInterpolation ) {
        boolean removed = this._surfaceInterpolationList.remove( vSurfaceInterpolation );
        return removed;
    }

    /**
     * Method removeSurfaceInterpolationAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.deegree.model.configuration.types.SurfaceInterpolationType removeSurfaceInterpolationAt( final int index ) {
        java.lang.Object obj = this._surfaceInterpolationList.remove( index );
        return (org.deegree.model.configuration.types.SurfaceInterpolationType) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vSurfaceInterpolation
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void setSurfaceInterpolation(
                                         final int index,
                                         final org.deegree.model.configuration.types.SurfaceInterpolationType vSurfaceInterpolation )
                            throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if ( index < 0 || index >= this._surfaceInterpolationList.size() )
            throw new IndexOutOfBoundsException( "setSurfaceInterpolation: Index value '" + index
                                                 + "' not in range [0.." + ( this._surfaceInterpolationList.size() - 1 )
                                                 + "]" );

        this._surfaceInterpolationList.set( index, vSurfaceInterpolation );
    }

    /**
     * 
     * 
     * @param vSurfaceInterpolationArray
     */
    public void setSurfaceInterpolation(
                                         final org.deegree.model.configuration.types.SurfaceInterpolationType[] vSurfaceInterpolationArray ) {
        // -- copy array
        this._surfaceInterpolationList.clear();

        for ( SurfaceInterpolationType element : vSurfaceInterpolationArray ) {
            this._surfaceInterpolationList.add( element );
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
     * @return the unmarshaled org.deegree.model.configuration.SupportedSurfaceInterpolationType
     */
    public static org.deegree.model.configuration.SupportedSurfaceInterpolationType unmarshal(
                                                                                               final java.io.Reader reader )
                            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.deegree.model.configuration.SupportedSurfaceInterpolationType) Unmarshaller.unmarshal(
                                                                                                           org.deegree.model.configuration.SupportedSurfaceInterpolationType.class,
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
