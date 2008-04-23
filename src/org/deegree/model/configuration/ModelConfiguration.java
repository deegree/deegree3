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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;

/**
 * Class ModelConfiguration.
 * 
 * @version $Revision$ $Date$
 */
public class ModelConfiguration implements java.io.Serializable {

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * 
     */
    private static final long serialVersionUID = -7234894657846966675L;

    /**
     * Field _geometryFactories.
     */
    private GeometryFactories _geometryFactories;

    // ----------------/
    // - Constructors -/
    // ----------------/

    public ModelConfiguration() {
        super();
    }
    
    /**
     * 
     * @return
     * @throws MarshalException
     * @throws ValidationException
     */
    public static ModelConfiguration readConfiguration() throws MarshalException, ValidationException {
        InputStream is = ModelConfiguration.class.getResourceAsStream( "modelconfiguration.xml" );
        Reader reader = new InputStreamReader( is );
        return ModelConfiguration.unmarshal( reader );
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     * Returns the value of field 'geometryFactories'.
     * 
     * @return the value of field 'GeometryFactories'.
     */
    public GeometryFactories getGeometryFactories() {
        return this._geometryFactories;
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
     * Sets the value of field 'geometryFactories'.
     * 
     * @param geometryFactories
     *            the value of field 'geometryFactories'.
     */
    public void setGeometryFactories( final GeometryFactories geometryFactories ) {
        this._geometryFactories = geometryFactories;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException
     *             if object is null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException
     *             if this object is an invalid instance according to the schema
     * @return the unmarshaled ModelConfiguration
     */
    public static ModelConfiguration unmarshal( final java.io.Reader reader )
                            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (ModelConfiguration) Unmarshaller.unmarshal( ModelConfiguration.class, reader );
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
