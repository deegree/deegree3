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

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class InitParamType.
 * 
 * @version $Revision$ $Date$
 */
public class InitParamType implements java.io.Serializable {

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * 
     */
    private static final long serialVersionUID = -872852246319244512L;

    /**
     * Field _name.
     */
    private java.lang.String _name;

    /**
     * Field _value.
     */
    private java.lang.String _value;

    // ----------------/
    // - Constructors -/
    // ----------------/

    public InitParamType() {
        super();
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     * Returns the value of field 'name'.
     * 
     * @return the value of field 'Name'.
     */
    public java.lang.String getName() {
        return this._name;
    }

    /**
     * Returns the value of field 'value'.
     * 
     * @return the value of field 'Value'.
     */
    public java.lang.String getValue() {
        return this._value;
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
     * Sets the value of field 'name'.
     * 
     * @param name
     *            the value of field 'name'.
     */
    public void setName( final java.lang.String name ) {
        this._name = name;
    }

    /**
     * Sets the value of field 'value'.
     * 
     * @param value
     *            the value of field 'value'.
     */
    public void setValue( final java.lang.String value ) {
        this._value = value;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException
     *             if object is null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException
     *             if this object is an invalid instance according to the schema
     * @return the unmarshaled org.deegree.model.configuration.InitParamType
     */
    public static org.deegree.model.configuration.InitParamType unmarshal( final java.io.Reader reader )
                            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.deegree.model.configuration.InitParamType) Unmarshaller.unmarshal(
                                                                                       org.deegree.model.configuration.InitParamType.class,
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
