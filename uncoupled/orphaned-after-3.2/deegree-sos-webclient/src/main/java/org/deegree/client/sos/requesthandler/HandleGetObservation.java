package org.deegree.client.sos.requesthandler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.deegree.client.sos.storage.StorageGetObservation;
import org.deegree.client.sos.storage.components.DataArray;
import org.deegree.client.sos.storage.components.Field;
import org.deegree.client.sos.storage.components.OWSException;
import org.deegree.client.sos.storage.components.Observation;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.net.HttpUtils;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Requesthandler-class for GetObservation-request. Initializes a new StorageGetObservation-object with contents of the
 * GetObservation-response called by a http-post-method.
 * 
 * @author <a href="mailto:neumeister@lat-lon.de">Ulrich Neumeister</a>
 * 
 */
public class HandleGetObservation {

    private StorageGetObservation storage = new StorageGetObservation();

    private OWSException exception;

    private XMLAdapter xml;

    private XPath xpath;

    private static final Logger LOG = LoggerFactory.getLogger( HandleGetObservation.class );

    private static NamespaceBindings nsContext;

    static {
        nsContext = CommonNamespaces.getNamespaceContext();
        nsContext.addNamespace( "sos", "http://www.opengis.net/sos/1.0" );
        nsContext.addNamespace( "ows", "http://www.opengis.net/ows/1.1" );
        nsContext.addNamespace( "om", "http://www.opengis.net/om/1.0" );
        nsContext.addNamespace( "swe", "http://www.opengis.net/swe/1.0.1" );
    }

    /**
     * Public constructor calls a the GetObservation response with a http-post-method. For this, the template has to be
     * filled with the given parameters by the methods loadTemplate and buildRequest. The InputStream from the
     * post-method's response is used to initialize the XMLAdapter. After that, the fillStorage-method is called.
     * 
     * @param parameters
     */
    public HandleGetObservation( Map<String, String> parameters ) {

        String template = loadTemplate( parameters.get( "path" ) );

        String request = buildRequest( template, parameters );

        InputStream is = null;
        try {
            is = HttpUtils.post( HttpUtils.STREAM, parameters.get( "host" ),
                                 new ByteArrayInputStream( request.getBytes( "UTF-8" ) ), null );
        } catch ( IOException e ) {
            LOG.error( "Unexpected error:", e.getLocalizedMessage() );
            LOG.debug( "Stack trace:", e );
        }

        xml = new XMLAdapter( is );

        fillStorage();
    }

    /**
     * Loads the hole XML template into the returned String.
     * 
     * @param path
     * @return
     */
    private String loadTemplate( String path ) {
        String result = "";
        File file = new File( path );
        try {
            FileInputStream fin = new FileInputStream( file );
            BufferedReader in = new BufferedReader( new InputStreamReader( fin, "UTF-8" ) );
            StringBuilder sb = new StringBuilder();
            while ( ( result = in.readLine() ) != null ) {
                sb.append( result );
            }
            result = sb.toString();
            in.close();
        } catch ( IOException e ) {
            LOG.error( "Unexpected stack trace:", e );
        }
        return result;
    }

    /**
     * Builds the request String for the http-post-method from the given parameters and the given template.
     * 
     * @param request
     * @param parameters
     * @return
     */
    private String buildRequest( String request, Map<String, String> parameters ) {

        String beginTime = parameters.get( "beginTime" );
        String endTime = parameters.get( "endTime" );
        String observedProperty = parameters.get( "observedProperty" );
        String offering = parameters.get( "offering" );
        String procedure = parameters.get( "procedure" );
        String responseFormat = parameters.get( "responseFormat" );
        String responseMode = parameters.get( "responseMode" );
        String resultModel = parameters.get( "resultModel" );

        request = request.replace( "_offering", offering );
        request = request.replace( "_begin", beginTime );
        request = request.replace( "_end", endTime );
        if ( !procedure.trim().equals( "" ) ) {
            String replacement = "";
            String[] procedures = procedure.split( "@@" );
            for ( int i = 0; i < procedures.length; i++ ) {
                replacement += "<procedure>" + procedures[i] + "</procedure>";
            }
            request = request.replace( "_procedure", replacement );
        } else {
            request = request.replace( "_procedure", "" );
        }
        if ( !observedProperty.trim().equals( "" ) ) {
            String replacement = "";
            String[] obsP = observedProperty.split( "@@" );
            for ( int i = 0; i < obsP.length; i++ ) {
                replacement += "<observedProperty>" + obsP[i] + "</observedProperty>";
            }
            request = request.replace( "_observedProperty", replacement );
        } else {
            request = request.replace( "_observedProperty", "" );
        }
        request = request.replace( "_responseFormat", responseFormat );
        if ( !responseMode.trim().equals( "" ) ) {
            String replacement = "<responseMode>" + responseMode + "</responseMode>";
            request = request.replace( "_responseMode", replacement );
        } else {
            request = request.replace( "_responseMode", "" );
        }
        if ( !resultModel.trim().equals( "" ) ) {
            String replacement = "<resultModel>" + resultModel + "</resultModel>";
            request = request.replace( "_resultModel", replacement );
        } else {
            request = request.replace( "_resultModel", "" );
        }
        return request;
    }

    /**
     * Checks, if the response from the GetObservation request is an exception report and in this case calls the
     * parseException-method for setting the contents of the exception object. In other cases the
     * setObservationCollection-method from the storage is called with the parseObservationCollection-method.
     * 
     */
    private void fillStorage() {
        if ( xml.getRootElement().getLocalName().equals( "ExceptionReport" ) ) {
            exception = parseException();
            LOG.error( xml.getRootElement().toString() );
        } else {
            storage.setObservationCollection( parseObservationCollection() );
        }
    }

    /**
     * @return
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
            } else if ( element.getLocalName().equals( "ExceptionText" ) ) {
                exceptionText = element.getText();
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
    private List<Observation> parseObservationCollection() {
        List<Observation> result = new ArrayList<Observation>();
        xpath = new XPath( "om:member/child::*", nsContext );
        List<OMElement> observationCollection = xml.getElements( xml.getRootElement(), xpath );
        for ( OMElement observationElement : observationCollection ) {
            Observation observation = new Observation();
            xpath = new XPath( "child::*", nsContext );
            List<OMElement> childreen = xml.getElements( observationElement, xpath );
            for ( OMElement child : childreen ) {
                if ( child.getLocalName().equals( "result" ) ) {
                    observation.setDataArray( parseDataArray( child ) );
                } else if ( child.getLocalName().equals( "featureOfInterest" ) ) {
                    observation.setFeatureOfInterest( parseFeatureOfInterest( child ) );
                } else if ( child.getLocalName().equals( "observedProperty" ) ) {
                    observation.setObservedProperty( parseObservedProperty( child ) );
                } else if ( child.getLocalName().equals( "samplingTime" ) ) {
                    observation.setSamplingTime( parseSamplingTime( child ) );
                } else if ( child.getLocalName().equals( "procedure" ) ) {
                    observation.setProcedure( parseProcedure( child ) );
                }
            }
            result.add( observation );
        }
        return result;
    }

    /**
     * @param context
     * @return
     */
    private DataArray parseDataArray( OMElement context ) {
        DataArray result = new DataArray();
        xpath = new XPath( "descendant::*", nsContext );
        List<OMElement> childreen = xml.getElements( context, xpath );
        for ( OMElement child : childreen ) {
            if ( child.getLocalName().equals( "value" ) ) {
                result.setCount( child.getText() );
            } else if ( child.getLocalName().equals( "elementType" ) ) {
                int count = 0;
                List<Field> elementTypes = new ArrayList<Field>();
                List<OMElement> fieldElements = xml.getElements( child, xpath );
                for ( OMElement fieldElement : fieldElements ) {
                    if ( fieldElement.getLocalName().equals( "field" ) ) {
                        Field field = new Field();
                        field.setIndex( count );
                        field.setName( fieldElement.getAttributeValue( new QName( "name" ) ) );
                        field.setDefinition( fieldElement.getFirstElement().getAttributeValue( new QName( "definition" ) ) );
                        OMElement type = fieldElement.getFirstChildWithName( new QName( "uom" ) );
                        if ( type != null ) {
                            field.setType( type.getAttributeValue( new QName( "code" ) ) );
                        } else {
                            field.setType( "Time" );
                        }
                        elementTypes.add( field );
                        count++;
                    }
                }
                result.setElementTypes( elementTypes );
            } else if ( child.getLocalName().equals( "TextBlock" ) ) {
                List<Pair<String, String>> separators = new ArrayList<Pair<String, String>>();
                Iterator<OMAttribute> attributes;
                for ( attributes = child.getAllAttributes(); attributes.hasNext(); ) {
                    OMAttribute attribute = attributes.next();
                    Pair<String, String> pair = new Pair<String, String>();
                    pair.first = attribute.getLocalName();
                    pair.second = attribute.getAttributeValue();
                    separators.add( pair );
                }
                result.setSeparators( separators );
            } else if ( child.getLocalName().equals( "values" ) ) {
                result.setValues( child.getText() );
            }
        }
        return result;
    }

    /**
     * @param context
     * @return
     */
    private Pair<String, String> parseSamplingTime( OMElement context ) {
        Pair<String, String> result = new Pair<String, String>();
        xpath = new XPath( "child::*", nsContext );
        List<OMElement> time = xml.getElements( xml.getRootElement(), xpath );
        for ( OMElement timeElement : time ) {
            if ( timeElement.getLocalName().equals( "beginPosition" ) ) {
                result.first = timeElement.getText();
            } else if ( timeElement.getLocalName().equals( "endPosition" ) ) {
                result.second = timeElement.getText();
            }
        }
        return result;
    }

    /**
     * @param context
     * @return
     */
    private String parseFeatureOfInterest( OMElement context ) {
        String result = "";
        Iterator<OMAttribute> attributes;
        for ( attributes = context.getAllAttributes(); attributes.hasNext(); ) {
            OMAttribute attribute = attributes.next();
            result += attribute.getAttributeValue();
        }
        return result;
    }

    /**
     * @param context
     * @return
     */
    private List<String> parseObservedProperty( OMElement context ) {
        List<String> result = new ArrayList<String>();
        Iterator<OMAttribute> attributes;
        for ( attributes = context.getAllAttributes(); attributes.hasNext(); ) {
            OMAttribute attribute = attributes.next();
            result.add( attribute.getAttributeValue() );
        }
        xpath = new XPath( "child::*", nsContext );
        List<OMElement> elements = xml.getElements( context, xpath );
        for ( OMElement element : elements ) {
            if ( element.getLocalName().equals( "observedProperty" ) ) {
                List<OMElement> contents = xml.getElements( element, xpath );
                for ( OMElement content : contents ) {
                    if ( content.getLocalName().equals( "CompositePhenomenon" ) ) {
                        List<OMElement> components = xml.getElements( content, xpath );
                        for ( OMElement component : components ) {
                            for ( attributes = component.getAllAttributes(); attributes.hasNext(); ) {
                                OMAttribute attribute = attributes.next();
                                result.add( attribute.getAttributeValue() );
                            }
                        }
                    } else {
                        result.add( content.toString() );
                    }
                }
            }
        }
        return result;
    }

    /**
     * @param context
     * @return
     */
    private String parseProcedure( OMElement context ) {
        String result = "";
        Iterator<OMAttribute> attributes;
        for ( attributes = context.getAllAttributes(); attributes.hasNext(); ) {
            OMAttribute attribute = attributes.next();
            result += attribute.getAttributeValue();
        }
        return result;
    }

    /**
     * @return
     */
    public StorageGetObservation getStorage() {
        return storage;
    }

    /**
     * @return
     */
    public OWSException getException() {
        return exception;
    }

}
