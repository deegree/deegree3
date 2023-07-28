package org.deegree.client.sos.requesthandler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.deegree.client.sos.storage.StorageGetCapabilities;
import org.deegree.client.sos.storage.components.BoundedBy;
import org.deegree.client.sos.storage.components.Filter_Capabilities;
import org.deegree.client.sos.storage.components.ObservationOffering;
import org.deegree.client.sos.storage.components.Operation;
import org.deegree.client.sos.storage.components.Operator;
import org.deegree.client.sos.storage.components.Parameter;
import org.deegree.client.sos.storage.components.Time;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Requesthandler-class for GetCapabilities-request. Initializes a new StorageGetCapabilities-object with contents of
 * the GetCapabilities-response called by a http-post-method.
 * 
 * @author <a href="mailto:neumeister@lat-lon.de">Ulrich Neumeister</a>
 * 
 */
public class HandleGetCapabilities {

    private static final Logger LOG = LoggerFactory.getLogger( HandleGetCapabilities.class );

    private XMLAdapter xml;

    private StorageGetCapabilities storage = new StorageGetCapabilities();

    private static NamespaceBindings nsContext;

    private XPath xpath;

    static {
        nsContext = new NamespaceBindings();
        nsContext.addNamespace( "sos", "http://www.opengis.net/sos/1.0" );
        nsContext.addNamespace( "ows", "http://www.opengis.net/ows/1.1" );
        nsContext.addNamespace( "om", "http://www.opengis.net/om/1.0" );
        nsContext.addNamespace( "swe", "http://www.opengis.net/swe/1.0.1" );
        nsContext.addNamespace( "ogc", "http://www.opengis.net/ogc" );
        nsContext.addNamespace( "gml", "http://www.opengis.net/gml" );
    }

    /**
     * Public constructor initializes the XMLAdapter-object with the given request-String (http-get request).<br>
     * After that, the fillStorage-method is called.
     * 
     * @param request
     */
    public HandleGetCapabilities( String request ) {
        try {
            URL url = new URL( request );
            xml = new XMLAdapter( url );
        } catch ( MalformedURLException e ) {
            LOG.error( "Unexpected stack trace:", e.getMessage() );
        }
        fillStorage( request.split( "\\?" )[0] );
    }

    /**
     * Sets the contents of storage by parsing the XMLAdapter's rootElement.
     * 
     * @param host
     */
    private void fillStorage( String host ) {
        storage.setHost( host );
        storage.setServiceIdentification( parseServiceIdentification() );
        storage.setServiceProvider( parseServiceProvider() );
        storage.setOperationsMetadata( parseOperationsMetadata() );
        storage.setFilter_Capabilities( parseFilter_Capabilities() );
        storage.setContents( parseOfferings() );
    }

    /**
     * @return
     */
    private List<Pair<String, String>> parseServiceIdentification() {
        List<Pair<String, String>> result = new ArrayList<Pair<String, String>>();
        // xpath = new XPath( "/sos:Capabilities/ows:ServiceIdentification/descendant::*", nsContext );
        xpath = new XPath( "/sos:Capabilities/ows:ServiceIdentification/descendant::*", nsContext );
        List<OMElement> nodes = xml.getElements( xml.getRootElement(), xpath );
        for ( OMElement element : nodes ) {
            String name = element.getLocalName();
            String value = element.getText();
            if ( value != null && !value.trim().equals( "" ) ) {
                Pair<String, String> pair = new Pair();
                pair.first = name;
                pair.second = value;
                result.add( pair );
            }
        }
        return result;
    }

    /**
     * @return
     */
    private List<Pair<String, String>> parseServiceProvider() {
        List<Pair<String, String>> result = new ArrayList<Pair<String, String>>();
        xpath = new XPath( "//ows:ServiceProvider/descendant::*", nsContext );
        List<OMElement> nodes = xml.getElements( xml.getRootElement(), xpath );
        for ( OMElement element : nodes ) {
            String name = element.getLocalName();
            String value = element.getText();
            if ( value != null && !value.trim().equals( "" ) ) {
                Pair<String, String> pair = new Pair<String, String>();
                pair.first = name;
                pair.second = value;
                result.add( pair );
            } else if ( name.equals( "ProviderSite" ) || name.equals( "OnlineResource" ) ) {
                Pair<String, String> pair = new Pair<String, String>();
                pair.first = name;
                pair.second = element.getAttributeValue( new QName( CommonNamespaces.XLNNS, "href" ) );
                result.add( pair );
            }
        }
        return result;
    }

    /**
     * @return
     */
    private List<Operation> parseOperationsMetadata() {
        List<Operation> result = new ArrayList<Operation>();
        xpath = new XPath( "//ows:Operation", nsContext );
        List<OMElement> operations = xml.getElements( xml.getRootElement(), xpath );
        for ( OMElement operationsElement : operations ) {
            Operation operation = new Operation();
            String operationName = operationsElement.getAttribute( new QName( "name" ) ).getAttributeValue();
            operation.setName( operationName );
            List<Pair<String, String>> http = new ArrayList<Pair<String, String>>();
            xpath = new XPath( "ows:DCP/ows:HTTP/child::*", nsContext );
            List<OMElement> nodes = xml.getElements( operationsElement, xpath );
            for ( OMElement metadataElement : nodes ) {
                Pair<String, String> pair = new Pair<String, String>();
                if ( metadataElement.getLocalName().equals( "Get" ) ) {
                    pair.first = "Get";
                    pair.second = metadataElement.getAttributeValue( new QName( CommonNamespaces.XLNNS, "href" ) );
                    http.add( pair );
                } else if ( metadataElement.getLocalName().equals( "Post" ) ) {
                    pair.first = "Post";
                    pair.second = metadataElement.getAttributeValue( new QName( CommonNamespaces.XLNNS, "href" ) );
                    http.add( pair );
                }
            }
            operation.setHttp( http );
            xpath = new XPath( "ows:Parameter", nsContext );
            nodes = xml.getElements( operationsElement, xpath );
            List<Parameter> parameters = new ArrayList<Parameter>();
            for ( OMElement parameterElement : nodes ) {
                Parameter parameter = new Parameter();
                String parameterName = parameterElement.getAttribute( new QName( "name" ) ).getAttributeValue();
                parameter.setName( parameterName );
                xpath = new XPath( "ows:AllowedValues/descendant::*", nsContext );
                List<OMElement> owsValues = xml.getElements( parameterElement, xpath );
                List<String> parameterValues = new ArrayList<String>();
                for ( OMElement owsValue : owsValues ) {
                    if ( owsValue.getText() != null && !owsValue.getText().trim().equals( "" ) ) {
                        parameterValues.add( owsValue.getText() );
                    }
                }
                parameter.setAllowedValues( parameterValues );
                parameters.add( parameter );
            }
            operation.setParameters( parameters );
            result.add( operation );
        }
        return result;
    }

    /**
     * @return
     */
    private Filter_Capabilities parseFilter_Capabilities() {
        Filter_Capabilities result = new Filter_Capabilities();
        List<String> operands;
        List<Operator> operators;
        List<OMElement> nodes;
        xpath = new XPath( "//ogc:Spatial_Capabilities/child::*", nsContext );
        nodes = xml.getElements( xml.getRootElement(), xpath );
        for ( OMElement spatial_Capability : nodes ) {
            if ( spatial_Capability.getLocalName().equals( "GeometryOperands" ) ) {
                xpath = new XPath( "child::*", nsContext );
                List<OMElement> geometryOperands = xml.getElements( spatial_Capability, xpath );
                operands = new ArrayList<String>();
                for ( OMElement geometryOperand : geometryOperands ) {
                    if ( geometryOperand.getLocalName().equals( "GeometryOperand" ) ) {
                        operands.add( geometryOperand.getText() );
                    }
                }
                result.setGeometryOperands( operands );
            } else if ( spatial_Capability.getLocalName().equals( "SpatialOperators" ) ) {
                xpath = new XPath( "child::*", nsContext );
                List<OMElement> spatialOperators = xml.getElements( spatial_Capability, xpath );
                operators = new ArrayList<Operator>();
                for ( OMElement spatialOperatorElement : spatialOperators ) {
                    Iterator<OMAttribute> attributes;
                    for ( attributes = spatialOperatorElement.getAllAttributes(); attributes.hasNext(); ) {
                        OMAttribute attribute = attributes.next();
                        if ( attribute.getLocalName().equals( "name" ) ) {
                            Operator spatialOperator = new Operator();
                            spatialOperator.setName( attribute.getAttributeValue() );
                            xpath = new XPath( "child::*/child::*", nsContext );
                            List<OMElement> geometryOperands = xml.getElements( spatialOperatorElement, xpath );
                            if ( geometryOperands.size() > 0 ) {
                                List<String> values = new ArrayList<String>();
                                for ( OMElement geometryOperand : geometryOperands ) {
                                    values.add( geometryOperand.getText() );
                                }
                                spatialOperator.setOperands( values );
                            }
                            operators.add( spatialOperator );
                        }
                    }
                }
                result.setSpatialOperators( operators );
            }
        }
        xpath = new XPath( "//ogc:Temporal_Capabilities/child::*", nsContext );
        nodes = xml.getElements( xml.getRootElement(), xpath );
        for ( OMElement temporal_Capability : nodes ) {
            if ( temporal_Capability.getLocalName().equals( "TemporalOperands" ) ) {
                xpath = new XPath( "child::*", nsContext );
                List<OMElement> temporalOperands = xml.getElements( temporal_Capability, xpath );
                operands = new ArrayList<String>();
                for ( OMElement temporalOperand : temporalOperands ) {
                    if ( temporalOperand.getLocalName().equals( "TemporalOperand" ) ) {
                        operands.add( temporalOperand.getText() );
                    }
                }
                result.setTemporalOperands( operands );
            } else if ( temporal_Capability.getLocalName().equals( "TemporalOperators" ) ) {
                xpath = new XPath( "child::*", nsContext );
                List<OMElement> temporalOperators = xml.getElements( temporal_Capability, xpath );
                operators = new ArrayList<Operator>();
                for ( OMElement temporalOperatorElement : temporalOperators ) {
                    Iterator<OMAttribute> attributes;
                    for ( attributes = temporalOperatorElement.getAllAttributes(); attributes.hasNext(); ) {
                        OMAttribute attribute = attributes.next();
                        if ( attribute.getLocalName().equals( "name" ) ) {
                            Operator temporalOperator = new Operator();
                            temporalOperator.setName( attribute.getAttributeValue() );
                            xpath = new XPath( "child::*/child::*", nsContext );
                            List<OMElement> temporalOperands = xml.getElements( temporalOperatorElement, xpath );
                            if ( temporalOperands.size() > 0 ) {
                                List<String> values = new ArrayList<String>();
                                for ( OMElement temporalOperand : temporalOperands ) {
                                    values.add( temporalOperand.getText() );
                                }
                                temporalOperator.setOperands( values );
                            }
                            operators.add( temporalOperator );
                        }
                    }
                }
                result.setTemporalOperators( operators );
            }
        }
        xpath = new XPath( "//ogc:Scalar_Capabilities/child::*", nsContext );
        nodes = xml.getElements( xml.getRootElement(), xpath );
        for ( OMElement scalar_Capability : nodes ) {
            if ( scalar_Capability.getLocalName().equals( "LogicalOperators" ) ) {
                result.setLogicalOperators( scalar_Capability );
            } else if ( scalar_Capability.getLocalName().equals( "ComparisonOperators" ) ) {
                List<String> comparisonOperators = new ArrayList<String>();
                xpath = new XPath( "child::*", nsContext );
                List<OMElement> comparisonOperatorsElements = xml.getElements( scalar_Capability, xpath );
                for ( OMElement comparisonOperator : comparisonOperatorsElements ) {
                    comparisonOperators.add( comparisonOperator.getText() );
                }
                result.setComparisonOperators( comparisonOperators );
            } else if ( scalar_Capability.getLocalName().equals( "ArithmeticOperators" ) ) {
                result.setArithmeticOperators( scalar_Capability );
            }
        }
        xpath = new XPath( "//ogc:Id_Capabilities/child::*", nsContext );
        result.setId_Capabilities( xml.getElement( xml.getRootElement(), xpath ) );
        return result;
    }

    /**
     * @return
     */
    private List<ObservationOffering> parseOfferings() {
        List<ObservationOffering> result = new ArrayList<ObservationOffering>();
        List<OMElement> nodes;
        xpath = new XPath( "//sos:ObservationOffering", nsContext );
        nodes = xml.getElements( xml.getRootElement(), xpath );
        for ( OMElement observationOffering : nodes ) {
            ObservationOffering offering = new ObservationOffering();
            offering.setId( observationOffering.getAttributeValue( new QName( CommonNamespaces.GMLNS, "id" ) ) );
            List<OMElement> metadata = new ArrayList<OMElement>();
            BoundedBy boundedBy = new BoundedBy();
            List<OMElement> intendedApplication = new ArrayList<OMElement>();
            Time time = new Time();
            List<String> procedures = new ArrayList<String>();
            List<String> observedProperties = new ArrayList<String>();
            List<OMElement> featuresOfInterest = new ArrayList<OMElement>();
            List<OMElement> responseFormats = new ArrayList<OMElement>();
            List<OMElement> responseModes = new ArrayList<OMElement>();
            List<OMElement> resultModels = new ArrayList<OMElement>();
            xpath = new XPath( "child::*", nsContext );
            List<OMElement> children = xml.getElements( observationOffering, xpath );
            for ( OMElement child : children ) {
                String childName = child.getLocalName();
                if ( childName.equals( "metaDataProperty" ) || childName.equals( "description" )
                     || childName.equals( "name" ) ) {
                    metadata.add( child );
                } else if ( childName.equals( "boundedBy" ) ) {
                    OMElement element = xml.getElement( child, xpath );
                    boundedBy.setType( element.getLocalName() );
                    boundedBy.setText( element.getText() );
                    List<OMAttribute> attributes = new ArrayList<OMAttribute>();
                    Iterator<OMAttribute> attributeIterator;
                    for ( attributeIterator = element.getAllAttributes(); attributeIterator.hasNext(); ) {
                        OMAttribute attribute = attributeIterator.next();
                        attributes.add( attribute );
                    }
                    boundedBy.setAttributes( attributes );
                    boundedBy.setElements( xml.getElements( element, xpath ) );
                } else if ( childName.equals( "intendedApplication" ) ) {
                    intendedApplication.add( child );
                } else if ( childName.equals( "time" ) ) {
                    Iterator<OMAttribute> attributesIterator;
                    List<OMAttribute> attributes = new ArrayList<OMAttribute>();
                    for ( attributesIterator = child.getAllAttributes(); attributesIterator.hasNext(); ) {
                        OMAttribute attribute = attributesIterator.next();
                        attributes.add( attribute );
                    }
                    time.setAttributesOfTime( attributes );
                    OMElement element = xml.getElement( child, xpath );
                    if ( element == null ) {
                        time.setIsNull( true );
                    } else {
                        time.setIsNull( false );
                        attributes = new ArrayList<OMAttribute>();
                        for ( attributesIterator = child.getAllAttributes(); attributesIterator.hasNext(); ) {
                            OMAttribute attribute = attributesIterator.next();
                            attributes.add( attribute );
                        }
                        time.setAttributesOfChild( attributes );
                        time.setElements( xml.getElements( element, xpath ) );
                    }

                } else if ( childName.equals( "procedure" ) ) {
                    Iterator<OMAttribute> attributes;
                    for ( attributes = child.getAllAttributes(); attributes.hasNext(); ) {
                        OMAttribute attribute = attributes.next();
                        procedures.add( attribute.getAttributeValue() );
                    }
                } else if ( childName.equals( "observedProperty" ) ) {
                    Iterator<OMAttribute> attributes;
                    for ( attributes = child.getAllAttributes(); attributes.hasNext(); ) {
                        OMAttribute attribute = attributes.next();
                        observedProperties.add( attribute.getAttributeValue() );
                    }
                } else if ( childName.equals( "featureOfInterest" ) ) {
                    featuresOfInterest.add( child );
                } else if ( childName.equals( "responseFormat" ) ) {
                    responseFormats.add( child );
                } else if ( childName.equals( "responseMode" ) ) {
                    responseModes.add( child );
                } else if ( childName.equals( "resultModel" ) ) {
                    resultModels.add( child );
                }
            }
            offering.setMetadata( metadata );
            offering.setBoundedBy( boundedBy );
            offering.setIntendedApplications( intendedApplication );
            offering.setTime( time );
            offering.setProcedures( procedures );
            offering.setObservedProperties( observedProperties );
            offering.setFeaturesOfInterest( featuresOfInterest );
            offering.setResponseFormats( responseFormats );
            offering.setResponseModes( responseModes );
            offering.setResultModels( resultModels );
            result.add( offering );
        }
        return result;
    }

    /**
     * @return
     */
    public StorageGetCapabilities getStorage() {
        return storage;
    }

}
