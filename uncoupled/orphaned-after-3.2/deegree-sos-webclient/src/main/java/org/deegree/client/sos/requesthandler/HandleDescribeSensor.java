package org.deegree.client.sos.requesthandler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.client.sos.storage.StorageDescribeSensor;
import org.deegree.client.sos.storage.components.OWSException;
import org.deegree.commons.utils.net.HttpUtils;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Requesthandler-class for DescribeSensor-request. Initializes a new StorageDescribeSensor-object with contents of the
 * DescribeSensor-response.
 * 
 * @author <a href="mailto:neumeister@lat-lon.de">Ulrich Neumeister</a>
 * 
 */
public class HandleDescribeSensor {

    private static final Logger LOG = LoggerFactory.getLogger( HandleDescribeSensor.class );

    private StorageDescribeSensor storage = new StorageDescribeSensor();

    private OWSException exception;

    private XMLAdapter xml;

    private static NamespaceBindings nsContext;

    private XPath xpath;

    static {
        nsContext = CommonNamespaces.getNamespaceContext();
        nsContext.addNamespace( "sos", "http://www.opengis.net/sos/1.0" );
        nsContext.addNamespace( "ows", "http://www.opengis.net/ows/1.1" );
        nsContext.addNamespace( "om", "http://www.opengis.net/om/1.0" );
        nsContext.addNamespace( "swe", "http://www.opengis.net/swe/1.0.1" );
        nsContext.addNamespace( "sml", "http://www.opengis.net/sensorML/1.0.1" );
    }

    /**
     * Public constructor needs the hosts name and the two parameters "outputFormat" and "procedure" for the
     * DescribeSensor request. Also the path to the used template is needed for this.
     * 
     * @param path
     * @param outputFormat
     * @param procedure
     * @param host
     */
    public HandleDescribeSensor( String path, String outputFormat, String procedure, String host ) {

        String template = loadTemplate( path );

        String request = buildRequest( template, outputFormat, procedure );

        InputStream is = null;
        try {
            is = HttpUtils.post( HttpUtils.STREAM, host, new ByteArrayInputStream( request.getBytes( "UTF-8" ) ), null );
        } catch ( IOException e ) {
            LOG.error( "Unexpected stack trace:", e.getMessage() );
        }

        xml = new XMLAdapter( is );

        fillStorage();
    }

    /**
     * @param path
     * @return The hole xml template for the DescribeSensor request in a String.
     */
    private String loadTemplate( String path ) {
        String result = "";
        File file = new File( path );
        try {
            FileInputStream fis = new FileInputStream( file );
            BufferedReader in = new BufferedReader( new InputStreamReader( fis, "UTF-8" ) );
            StringBuilder sb = new StringBuilder();
            while ( ( result = in.readLine() ) != null ) {
                sb.append( result );
            }
            result = sb.toString();
            in.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Builds a DescribeSensor request from the given template and the two parameters outputFormat and procedure.
     * 
     * @param template
     * @param outputFormat
     * @param procedure
     * @return The DescribeSensor request in a String.
     */
    private String buildRequest( String template, String outputFormat, String procedure ) {
        String result = template;
        outputFormat = outputFormat.replace( "\"", "&quot;" );
        result = result.replace( "_outputFormat", outputFormat );
        result = result.replace( "_procedure", procedure );
        return result;
    }

    /**
     * Checks, whether an exception has been sent from the SOS. In this case, only the parseException-method is called
     * to set the exception object. Otherwise every other method of the storage will be called with the parse-methods
     * from the RequestHandler.
     * 
     * 
     */
    private void fillStorage() {
        if ( xml.getRootElement().getLocalName().equals( "ExceptionReport" ) ) {
            exception = parseException();
            LOG.error( xml.getRootElement().toString() );
        } else {
            storage.setCapabilities( parseCapabilities() );
            storage.setCharacteristics( parseCharacteristics() );
            storage.setClassification( parseClassification() );
            storage.setContact( parseContact() );
            storage.setDocumentation( parseDocumentation() );
            storage.setHistory( parseHistory() );
            storage.setIdentification( parseIdentification() );
            storage.setKeywords( parseKeywords() );
            storage.setLegalConstraint( parseLegalConstraint() );
            storage.setMember( parseMember() );
            storage.setSecurityConstraint( parseSecurityConstraint() );
            storage.setValidTime( parseValidTime() );
        }
    }

    /**
     * @return the data from the DescribeSensor request.
     */
    public StorageDescribeSensor getStorage() {
        return storage;
    }

    /**
     * @return OWSException with containing exceptionCode, locator and exceptionText from the DescribeSensor response
     */
    private OWSException parseException() {
        OWSException result = new OWSException();
        String exceptionCode = "";
        String locator = "";
        String exceptionText = "";

        xpath = new XPath( "descendant::*", nsContext );
        List<OMElement> elements = xml.getElements( xml.getRootElement(), xpath );
        for ( OMElement element : elements ) {
            if ( element.getLocalName().equals( "Exception" ) ) {
                exceptionCode = element.getAttribute( new QName( "exceptionCode" ) ).getAttributeValue();
                locator = element.getAttribute( new QName( "locator" ) ).getAttributeValue();
                LOG.trace( "exceptionCode: " + exceptionCode );
                LOG.trace( "locator: " + locator );
            } else if ( element.getLocalName().equals( "ExceptionText" ) ) {
                exceptionText = element.getText();
                LOG.trace( "exceptionText: " + exceptionText );
            }
        }
        result.setExceptionCode( exceptionCode );
        result.setLocator( locator );
        result.setExceptionText( exceptionText );
        return result;
    }

    /**
     * @return
     */
    private List<OMElement> parseCapabilities() {
        List<OMElement> result = new ArrayList<OMElement>();
        xpath = new XPath( "//sml:capabilities/descendant::*", nsContext );
        result = xml.getElements( xml.getRootElement(), xpath );
        return result;
    }

    /**
     * @return
     */
    private List<OMElement> parseCharacteristics() {
        List<OMElement> result = new ArrayList<OMElement>();
        xpath = new XPath( "//sml:characteristics/descendant::*", nsContext );
        result = xml.getElements( xml.getRootElement(), xpath );
        return result;
    }

    /**
     * @return
     */
    private List<OMElement> parseClassification() {
        List<OMElement> result = new ArrayList<OMElement>();
        xpath = new XPath( "//sml:classification/descendant::*", nsContext );
        result = xml.getElements( xml.getRootElement(), xpath );
        return result;
    }

    /**
     * @return
     */
    private List<OMElement> parseContact() {
        List<OMElement> result = new ArrayList<OMElement>();
        xpath = new XPath( "//sml:contact/descendant::*", nsContext );
        result = xml.getElements( xml.getRootElement(), xpath );
        return result;
    }

    /**
     * @return
     */
    private List<OMElement> parseDocumentation() {
        List<OMElement> result = new ArrayList<OMElement>();
        xpath = new XPath( "//sml:documentation/descendant::*", nsContext );
        result = xml.getElements( xml.getRootElement(), xpath );
        return result;
    }

    /**
     * @return
     */
    private List<OMElement> parseHistory() {
        List<OMElement> result = new ArrayList<OMElement>();
        xpath = new XPath( "//sml:history/descendant::*", nsContext );
        result = xml.getElements( xml.getRootElement(), xpath );
        return result;
    }

    /**
     * @return
     */
    private List<OMElement> parseIdentification() {
        List<OMElement> result = new ArrayList<OMElement>();
        xpath = new XPath( "//sml:identification/descendant::*", nsContext );
        result = xml.getElements( xml.getRootElement(), xpath );
        return result;
    }

    /**
     * @return
     */
    private List<OMElement> parseKeywords() {
        List<OMElement> result = new ArrayList<OMElement>();
        xpath = new XPath( "//sml:keywords/descendant::*", nsContext );
        result = xml.getElements( xml.getRootElement(), xpath );
        return result;
    }

    /**
     * @return
     */
    private List<OMElement> parseLegalConstraint() {
        List<OMElement> result = new ArrayList<OMElement>();
        xpath = new XPath( "//sml:legalConstraint/descendant::*", nsContext );
        result = xml.getElements( xml.getRootElement(), xpath );
        return result;
    }

    /**
     * @return
     */
    private List<OMElement> parseMember() {
        List<OMElement> result = new ArrayList<OMElement>();
        xpath = new XPath( "//sml:member/descendant::*", nsContext );
        result = xml.getElements( xml.getRootElement(), xpath );
        return result;
    }

    /**
     * @return
     */
    private List<OMElement> parseSecurityConstraint() {
        List<OMElement> result = new ArrayList<OMElement>();
        xpath = new XPath( "//sml:securityConstraint/descendant::*", nsContext );
        result = xml.getElements( xml.getRootElement(), xpath );
        return result;
    }

    /**
     * @return
     */
    private List<OMElement> parseValidTime() {
        List<OMElement> result = new ArrayList<OMElement>();
        xpath = new XPath( "//sml:validTime/descendant::*", nsContext );
        result = xml.getElements( xml.getRootElement(), xpath );
        return result;
    }

    public OWSException getException() {
        return exception;
    }

}
