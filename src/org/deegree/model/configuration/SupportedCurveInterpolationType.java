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

import org.deegree.model.configuration.types.CurveInterpolationType;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class SupportedCurveInterpolationType.
 * 
 * @version $Revision$ $Date$
 */
public class SupportedCurveInterpolationType implements java.io.Serializable {

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * 
     */
    private static final long serialVersionUID = -7963106572274759260L;

    /**
     * Field _curveInterpolationList.
     */
    private java.util.Vector _curveInterpolationList;

    // ----------------/
    // - Constructors -/
    // ----------------/

    public SupportedCurveInterpolationType() {
        super();
        this._curveInterpolationList = new java.util.Vector();
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     * 
     * 
     * @param vCurveInterpolation
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addCurveInterpolation(
                                       final org.deegree.model.configuration.types.CurveInterpolationType vCurveInterpolation )
                            throws java.lang.IndexOutOfBoundsException {
        this._curveInterpolationList.addElement( vCurveInterpolation );
    }

    /**
     * 
     * 
     * @param index
     * @param vCurveInterpolation
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addCurveInterpolation(
                                       final int index,
                                       final org.deegree.model.configuration.types.CurveInterpolationType vCurveInterpolation )
                            throws java.lang.IndexOutOfBoundsException {
        this._curveInterpolationList.add( index, vCurveInterpolation );
    }

    /**
     * Method enumerateCurveInterpolation.
     * 
     * @return an Enumeration over all org.deegree.model.configuration.types.CurveInterpolationType
     *         elements
     */
    public java.util.Enumeration enumerateCurveInterpolation() {
        return this._curveInterpolationList.elements();
    }

    /**
     * Method getCurveInterpolation.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     * @return the value of the org.deegree.model.configuration.types.CurveInterpolationType at the
     *         given index
     */
    public org.deegree.model.configuration.types.CurveInterpolationType getCurveInterpolation( final int index )
                            throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if ( index < 0 || index >= this._curveInterpolationList.size() )
            throw new IndexOutOfBoundsException( "getCurveInterpolation: Index value '" + index + "' not in range [0.."
                                                 + ( this._curveInterpolationList.size() - 1 ) + "]" );

        return (org.deegree.model.configuration.types.CurveInterpolationType) this._curveInterpolationList.get( index );
    }

    /**
     * Method getCurveInterpolation.Returns the contents of the collection in an Array.
     * <p>
     * Note: Just in case the collection contents are changing in another thread, we pass a 0-length
     * Array of the correct type into the API call. This way we <i>know</i> that the Array returned
     * is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public org.deegree.model.configuration.types.CurveInterpolationType[] getCurveInterpolation() {
        org.deegree.model.configuration.types.CurveInterpolationType[] array = new org.deegree.model.configuration.types.CurveInterpolationType[0];
        return (org.deegree.model.configuration.types.CurveInterpolationType[]) this._curveInterpolationList.toArray( array );
    }

    /**
     * Method getCurveInterpolationCount.
     * 
     * @return the size of this collection
     */
    public int getCurveInterpolationCount() {
        return this._curveInterpolationList.size();
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
    public void removeAllCurveInterpolation() {
        this._curveInterpolationList.clear();
    }

    /**
     * Method removeCurveInterpolation.
     * 
     * @param vCurveInterpolation
     * @return true if the object was removed from the collection.
     */
    public boolean removeCurveInterpolation(
                                             final org.deegree.model.configuration.types.CurveInterpolationType vCurveInterpolation ) {
        boolean removed = this._curveInterpolationList.remove( vCurveInterpolation );
        return removed;
    }

    /**
     * Method removeCurveInterpolationAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.deegree.model.configuration.types.CurveInterpolationType removeCurveInterpolationAt( final int index ) {
        java.lang.Object obj = this._curveInterpolationList.remove( index );
        return (org.deegree.model.configuration.types.CurveInterpolationType) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vCurveInterpolation
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void setCurveInterpolation(
                                       final int index,
                                       final org.deegree.model.configuration.types.CurveInterpolationType vCurveInterpolation )
                            throws java.lang.IndexOutOfBoundsException {
        // check bounds for index
        if ( index < 0 || index >= this._curveInterpolationList.size() )
            throw new IndexOutOfBoundsException( "setCurveInterpolation: Index value '" + index + "' not in range [0.."
                                                 + ( this._curveInterpolationList.size() - 1 ) + "]" );

        this._curveInterpolationList.set( index, vCurveInterpolation );
    }

    /**
     * 
     * 
     * @param vCurveInterpolationArray
     */
    public void setCurveInterpolation(
                                       final org.deegree.model.configuration.types.CurveInterpolationType[] vCurveInterpolationArray ) {
        // -- copy array
        this._curveInterpolationList.clear();

        for ( CurveInterpolationType element : vCurveInterpolationArray ) {
            this._curveInterpolationList.add( element );
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
     * @return the unmarshaled org.deegree.model.configuration.SupportedCurveInterpolationType
     */
    public static org.deegree.model.configuration.SupportedCurveInterpolationType unmarshal( final java.io.Reader reader )
                            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.deegree.model.configuration.SupportedCurveInterpolationType) Unmarshaller.unmarshal(
                                                                                                         org.deegree.model.configuration.SupportedCurveInterpolationType.class,
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
