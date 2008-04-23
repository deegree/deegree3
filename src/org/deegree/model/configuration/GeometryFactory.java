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
 * Class GeometryFactory.
 * 
 * @version $Revision$ $Date$
 */
public class GeometryFactory extends GeometryFactoryType implements java.io.Serializable {

    // ----------------/
    // - Constructors -/
    // ----------------/

    /**
     * 
     */
    private static final long serialVersionUID = 1816634977972096094L;

    public GeometryFactory() {
        super();
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     * Method isValid.
     * 
     * @return true if this object is valid according to the schema
     */
    @Override
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
    @Override
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
    @Override
    public void marshal( final org.xml.sax.ContentHandler handler )
                            throws java.io.IOException, org.exolab.castor.xml.MarshalException,
                            org.exolab.castor.xml.ValidationException {
        Marshaller.marshal( this, handler );
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException
     *             if object is null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException
     *             if this object is an invalid instance according to the schema
     * @return the unmarshaled org.deegree.model.configuration.GeometryFactoryType
     */
    public static org.deegree.model.configuration.GeometryFactoryType unmarshal( final java.io.Reader reader )
                            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.deegree.model.configuration.GeometryFactoryType) Unmarshaller.unmarshal(
                                                                                             org.deegree.model.configuration.GeometryFactory.class,
                                                                                             reader );
    }

    /**
     * 
     * 
     * @throws org.exolab.castor.xml.ValidationException
     *             if this object is an invalid instance according to the schema
     */
    @Override
    public void validate()
                            throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate( this );
    }

}
