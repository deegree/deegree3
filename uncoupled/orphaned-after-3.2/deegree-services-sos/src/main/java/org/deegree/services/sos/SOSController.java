/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.sos;

import static org.deegree.commons.ows.exception.OWSException.INVALID_DATE;
import static org.deegree.commons.ows.exception.OWSException.NO_APPLICABLE_CODE;
import static org.deegree.commons.ows.exception.OWSException.VERSION_NEGOTIATION_FAILED;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.commons.xml.CommonNamespaces.GML_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.XLINK_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.commons.xml.CommonNamespaces.XSI_PREFIX;
import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;
import static org.deegree.services.sos.SOSProvider.IMPLEMENTATION_METADATA;
import static org.deegree.services.sos.ServiceConfigurationXMLAdapter.SCHEMA;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.primitive.Point;
import org.deegree.observation.filter.FilterCollection;
import org.deegree.observation.filter.ProcedureFilter;
import org.deegree.observation.filter.SpatialFilter;
import org.deegree.observation.model.Observation;
import org.deegree.observation.model.Offering;
import org.deegree.observation.model.Procedure;
import org.deegree.observation.persistence.ObservationDatastoreException;
import org.deegree.observation.persistence.ObservationStoreManager;
import org.deegree.protocol.ows.getcapabilities.GetCapabilities;
import org.deegree.protocol.ows.getcapabilities.GetCapabilitiesKVPParser;
import org.deegree.protocol.ows.getcapabilities.GetCapabilitiesXMLParser;
import org.deegree.protocol.sos.SOSConstants.SOSRequestType;
import org.deegree.protocol.sos.describesensor.DescribeSensor;
import org.deegree.protocol.sos.describesensor.DescribeSensor100KVPAdapter;
import org.deegree.protocol.sos.describesensor.DescribeSensor100XMLAdapter;
import org.deegree.protocol.sos.getfeatureofinterest.GetFeatureOfInterest;
import org.deegree.protocol.sos.getfeatureofinterest.GetFeatureOfInterest100XMLAdapter;
import org.deegree.protocol.sos.getobservation.EventTime100XMLAdapter.EventTimeXMLParsingException;
import org.deegree.protocol.sos.getobservation.GetObservation;
import org.deegree.protocol.sos.getobservation.GetObservation100KVPAdapter;
import org.deegree.protocol.sos.getobservation.GetObservation100XMLAdapter;
import org.deegree.protocol.sos.getobservation.GetObservation100XMLAdapter.ResultFilterException;
import org.deegree.services.controller.AbstractOWS;
import org.deegree.services.controller.ImplementationMetadata;
import org.deegree.services.controller.exception.serializer.XMLExceptionSerializer;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.jaxb.controller.DeegreeServiceControllerType;
import org.deegree.services.jaxb.metadata.DeegreeServicesMetadataType;
import org.deegree.services.jaxb.metadata.ServiceIdentificationType;
import org.deegree.services.jaxb.sos.DeegreeSOS;
import org.deegree.services.jaxb.sos.PublishedInformation;
import org.deegree.services.sos.capabilities.Capabilities100XMLAdapter;
import org.deegree.services.sos.capabilities.Capabilities100XMLAdapter.Sections;
import org.deegree.services.sos.getobservation.Observation100XMLAdapter;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the <a href="http://www.opengeospatial.org/standards/sos">OpenGIS Sensor Observation Service</a>
 * server protocol.
 * 
 * <pre>
 * Note: The SOS 1.0.0 specification doesn't define KVP requests. It is an acknowledged flaw in the spec. Regardless of
 * that, some other SOS implemented KVP requests. The deegree SOS follows the KVP requests format of the following implementations.
 *  - http://www.oostethys.org/best-practices/best-practices-get    
 *  - http://mapserver.gis.umn.edu/docs/howto/sos_server/#getcapabilities-request
 * </pre>
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * 
 */
public class SOSController extends AbstractOWS {

    private static final Logger LOG = LoggerFactory.getLogger( SOSController.class );

    private final Version SOS100 = Version.parseVersion( "1.0.0" );

    private final String SA_PREFIX = "sa";

    private final String SA_NS = "http://www.opengis.net/sampling/1.0";

    private SOService sosService;

    private int httpCodeForExceptions;

    private ServiceIdentificationType identification;

    private URL configUrl;

    public SOSController( URL configURL, ImplementationMetadata serviceInfo ) {
        super( configURL, serviceInfo );
        this.configUrl = configURL;
    }

    @Override
    public void init( DeegreeServicesMetadataType serviceMetadata, DeegreeServiceControllerType mainConf,
                      ImplementationMetadata<?> md, XMLAdapter controllerConf )
                            throws ResourceInitException {

        super.init( serviceMetadata, mainConf, IMPLEMENTATION_METADATA, controllerConf );

        NamespaceBindings nsContext = new NamespaceBindings();
        nsContext.addNamespace( "sos", "http://www.deegree.org/services/sos" );

        OMElement confElem = controllerConf.getRequiredElement( controllerConf.getRootElement(),
                                                                new XPath( "sos:ServiceConfiguration", nsContext ) );
        ServiceConfigurationXMLAdapter serviceConfigAdapter = new ServiceConfigurationXMLAdapter();
        serviceConfigAdapter.setRootElement( confElem );
        serviceConfigAdapter.setSystemId( controllerConf.getSystemId() );

        this.sosService = SOSBuilder.createService( workspace, configUrl );

        PublishedInformation pubInfo = null;
        try {
            DeegreeSOS sos = (DeegreeSOS) unmarshall( "org.deegree.services.jaxb.sos", SCHEMA, configUrl, workspace );
            pubInfo = sos.getPublishedInformation();
        } catch ( XMLParsingException e ) {
            throw new ResourceInitException( "TODO", e );
        } catch ( JAXBException e ) {
            throw new ResourceInitException( "TODO", e );
        }
        syncWithMainController();

        setConfiguredHTTPCodeForExceptions( pubInfo );
        validateAndSetOfferedVersions( pubInfo.getSupportedVersions().getVersion() );

        // check if all the sensors mentioned in the configuration are actually defined in their respective files
        for ( Offering offering : sosService.getAllOfferings() ) {
            for ( Procedure proc : offering.getProcedures() ) {
                URL description = proc.getSensorURL();
                if ( !checkProcURNinFile( proc.getProcedureHref(), description ) ) {
                    LOG.warn( "SOS configuration mentions sensor " + proc.getProcedureHref()
                              + " but the reference file " + description + " doesn't contain the sensor's definition." );
                }
            }
        }
    }

    private static boolean checkProcURNinFile( String procURN, URL sensorFile ) {
        NamespaceBindings nsContext = new NamespaceBindings();
        nsContext.addNamespace( "sml", "http://www.opengis.net/sensorML/1.0.1" );
        nsContext.addNamespace( "gml", "http://www.opengis.net/gml" );
        nsContext.addNamespace( "swe", "http://www.opengis.net/swe/1.0.1" );
        nsContext.addNamespace( "xlink", "http://www.w3.org/1999/xlink" );

        XMLAdapter sensorXML = new XMLAdapter( sensorFile );
        OMElement element = sensorXML.getElement( sensorXML.getRootElement(),
                                                  new XPath(
                                                             "/sml:SensorML/sml:identification/sml:IdentifierList/sml:identifier[@name=\"URN\"]/sml:Term/sml:value",
                                                             nsContext ) );
        if ( element.getText().equals( procURN ) ) {
            return true;
        }
        return false;
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub
    }

    private void setConfiguredHTTPCodeForExceptions( PublishedInformation pubInfo ) {
        Integer statusCode = pubInfo.getOWSOptions().getHTTPStatusCodeForExceptions();
        if ( statusCode == null ) {
            httpCodeForExceptions = 200;
        } else {
            httpCodeForExceptions = statusCode;
        }
    }

    @Override
    public void doKVP( Map<String, String> param, HttpServletRequest request, HttpResponseBuffer response,
                       List<FileItem> multiParts )
                            throws ServletException, IOException {

        // see javadoc for this SOSController class for more information on KVP handling
        try {
            checkRequiredKeys( param );
            response.setContentType( "text/xml" );

            if ( param.containsKey( "REQUEST" ) ) {
                String requestName = param.get( "REQUEST" );
                switch ( IMPLEMENTATION_METADATA.getRequestTypeByName( requestName ) ) {
                case GetCapabilities:
                    GetCapabilities capabilities = GetCapabilitiesKVPParser.parse( param );
                    doGetCapabilities( capabilities, mainMetadataConf, response );
                    break;
                case DescribeSensor:
                    DescribeSensor sensor = DescribeSensor100KVPAdapter.parse( param );
                    doDescribeSensor( sensor, response );
                    break;
                case GetObservation:
                    GetObservation observation = GetObservation100KVPAdapter.parse( param );
                    doGetObservation( observation, response );
                    break;
                } // default handled by getRequestTypeByName
            }
        } catch ( ParseException e ) {
            sendServiceException( new OWSException( e.getLocalizedMessage(), INVALID_DATE ), response );
        } catch ( InvalidParameterValueException ex ) {
            sendServiceException( new OWSException( ex.getLocalizedMessage(), VERSION_NEGOTIATION_FAILED ), response );
        } catch ( OWSException ex ) {
            sendServiceException( ex, response );
        } catch ( ObservationDatastoreException e ) {
            sendServiceException( new OWSException( "an error occured while processing a request", "" ), response );
            LOG.error( "an error occured while processing a request", e );
        } catch ( Throwable e ) {
            sendServiceException( new OWSException( "an error occured while processing a request", NO_APPLICABLE_CODE ),
                                  response );
            LOG.error( "an error occured while processing a request", e );
        }
    }

    @Override
    public void doXML( XMLStreamReader xmlStream, HttpServletRequest request, HttpResponseBuffer response,
                       List<FileItem> multiParts )
                            throws ServletException, IOException {

        response.setContentType( "text/xml" );

        try {
            XMLAdapter requestDoc = new XMLAdapter( xmlStream );
            OMElement rootElement = requestDoc.getRootElement();
            String rootName = rootElement.getLocalName();

            switch ( IMPLEMENTATION_METADATA.getRequestTypeByName( rootName ) ) {
            case GetCapabilities:
                LOG.debug( "start handling GetCapabilities" );
                GetCapabilitiesXMLParser capabilitiesAdapter = new GetCapabilitiesXMLParser( rootElement );
                doGetCapabilities( capabilitiesAdapter.parse110(), mainMetadataConf, response );
                break;
            case GetObservation:
                LOG.debug( "start handling GetObservation" );
                GetObservation100XMLAdapter observation = new GetObservation100XMLAdapter( rootElement );
                doGetObservation( observation.parse(), response );
                break;
            case DescribeSensor:
                LOG.debug( "start handling DescribeSensor" );
                DescribeSensor100XMLAdapter describe = new DescribeSensor100XMLAdapter( rootElement );
                doDescribeSensor( describe.parse(), response );
                break;
            case GetFeatureOfInterest: // TODO still has to be added to doKVP
                LOG.debug( "start handling GetFeatureOfInterest" );
                GetFeatureOfInterest100XMLAdapter featureOfI = new GetFeatureOfInterest100XMLAdapter( rootElement );
                doGetFeatureOfInterest( featureOfI.parse(), response );
                break;
            }// default handled by getRequestTypeByName
        } catch ( OWSException ex ) {
            LOG.debug( "Stack trace:", ex );
            sendServiceException( ex, response );
        } catch ( ResultFilterException e ) {
            LOG.debug( "Stack trace:", e );
            sendServiceException( new OWSException( e.getLocalizedMessage(), OWSException.INVALID_PARAMETER_VALUE,
                                                    "result" ), response );
        } catch ( EventTimeXMLParsingException e ) {
            LOG.debug( "Stack trace:", e );
            sendServiceException( new OWSException( e.getLocalizedMessage(), OWSException.INVALID_PARAMETER_VALUE,
                                                    "eventTime" ), response );
        } catch ( ObservationDatastoreException e ) {
            sendServiceException( new OWSException( "an error occured while processing a request", "" ), response );
            LOG.error( "an error occured while processing a request", e );
        } catch ( Throwable e ) {
            sendServiceException( new OWSException( "an error occured while processing a request", NO_APPLICABLE_CODE ),
                                  response );
            LOG.error( "an error occured while processing a request", e );
        }
    }

    private void doGetFeatureOfInterest( GetFeatureOfInterest foi, HttpResponseBuffer response )
                            throws IOException, XMLStreamException {
        XMLStreamWriter xmlWriter = response.getXMLWriter();

        List<String> foiIDs = Arrays.asList( foi.getFoiID() );

        xmlWriter.writeStartElement( SA_PREFIX, "SamplingFeatureCollection", SA_NS );
        xmlWriter.writeNamespace( SA_PREFIX, SA_NS );
        xmlWriter.writeNamespace( XSI_PREFIX, XSINS );
        xmlWriter.writeNamespace( XLINK_PREFIX, XLNNS );
        xmlWriter.writeNamespace( GML_PREFIX, GMLNS );

        xmlWriter.writeAttribute( XSI_PREFIX, XSINS, "schemaLocation",
                                  "http://www.opengis.net/sampling/1.0 http://schemas.opengis.net/sampling/1.0.0/sampling.xsd" );

        // TODO a url should be specified in the xlink:href of sampledFeature
        xmlWriter.writeEmptyElement( SA_PREFIX, "sampledFeature", SA_NS );

        for ( Offering offering : sosService.getAllOfferings() ) {
            for ( Procedure procedure : offering.getProcedures() ) {
                if ( foiIDs.contains( procedure.getFeatureOfInterestHref() ) ) {
                    Geometry procGeometry = procedure.getLocation();
                    if ( procGeometry instanceof Point ) { // TODO check if the procedure can have some other geometries
                        // and if so,
                        // handle them

                        xmlWriter.writeStartElement( SA_PREFIX, "member", SA_NS );

                        xmlWriter.writeStartElement( SA_PREFIX, "SamplingPoint", SA_NS );
                        xmlWriter.writeStartElement( GML_PREFIX, "name", GMLNS );
                        xmlWriter.writeCharacters( procedure.getFeatureOfInterestHref() );
                        // TODO if the GetFeatureOfInterest does not provide a foi but a location instead, search
                        // for all
                        // sensors
                        // inside that BBOX
                        xmlWriter.writeEndElement();

                        // TODO a url should be specified in the xlink:href of sampledFeature
                        xmlWriter.writeEmptyElement( SA_PREFIX, "sampledFeature", SA_NS );

                        xmlWriter.writeStartElement( SA_PREFIX, "position", SA_NS );
                        // exporting a gml:Point TODO use GML encoder
                        xmlWriter.writeStartElement( GML_PREFIX, "Point", GMLNS );
                        // have the last part of the foiID as the Point id attribute
                        String[] foiParts = procedure.getFeatureOfInterestHref().split( ":" );
                        xmlWriter.writeAttribute( GML_PREFIX, GMLNS, "id", foiParts[foiParts.length - 1] );

                        xmlWriter.writeStartElement( GML_PREFIX, "pos", GMLNS );
                        ICRS foiCRS = null;
                        foiCRS = procGeometry.getCoordinateSystem();
                        xmlWriter.writeAttribute( "srsName", foiCRS.getCode().toString() );

                        Point p = (Point) procGeometry;
                        xmlWriter.writeCharacters( p.get0() + " " + p.get1() );
                        xmlWriter.writeEndElement(); // gml:pos
                        xmlWriter.writeEndElement(); // gml:Point
                        xmlWriter.writeEndElement(); // gml:position
                        xmlWriter.writeEndElement(); // sa:SamplingPoint
                        xmlWriter.writeEndElement(); // sa:member
                    }
                }
            }
        }

        xmlWriter.writeEndElement(); // sa:SamplingFeatureCollection
        xmlWriter.writeEndDocument();
        xmlWriter.flush();
    }

    private void doGetObservation( GetObservation observationReq, HttpResponseBuffer response )
                            throws XMLStreamException, IOException, OWSException {
        XMLStreamWriter xmlWriter = response.getXMLWriter();
        LOG.debug( "offering: {}", observationReq.getOffering() );
        if ( sosService.hasOffering( observationReq.getOffering() ) ) {
            validateGetObservation( observationReq );

            Offering offering = sosService.getOffering( observationReq.getOffering() );
            Observation observation = getObservationResult( offering, observationReq );

            writeObservationResult( xmlWriter, observation, observationReq );

        } else {
            throw new OWSException( "the offering " + observationReq.getOffering() + " is invalid",
                                    OWSException.INVALID_PARAMETER_VALUE, "offering" );
        }
        xmlWriter.flush();
    }

    private static Observation getObservationResult( Offering offering, GetObservation observationReq )
                            throws OWSException {
        FilterCollection filter = createFilterFromRequest( observationReq );
        try {
            return offering.getObservation( filter );
        } catch ( ObservationDatastoreException e ) {
            throw new OWSException( e.getMessage(), OWSException.INVALID_PARAMETER_VALUE, "observedProperty" );
        }
    }

    private static FilterCollection createFilterFromRequest( GetObservation observationReq ) {
        FilterCollection filter = new FilterCollection();
        filter.add( observationReq.getEventTime() );
        filter.add( observationReq.getObservedProperties() );
        filter.add( observationReq.getProcedures() );
        filter.add( observationReq.getResultFilter() );
        if ( observationReq.getFeatureOfInterest() != null ) {
            filter.add( observationReq.getFeatureOfInterest().second );
        }
        return filter;
    }

    private static void writeObservationResult( XMLStreamWriter xmlWriter, Observation observation, GetObservation req )
                            throws XMLStreamException, OWSException {
        String model = req.getResultModel();
        if ( model.equals( "" ) || model.endsWith( "Observation" ) ) {
            Observation100XMLAdapter.exportOMObservation( xmlWriter, observation );
        } else if ( model.endsWith( "Measurement" ) ) {
            Observation100XMLAdapter.exportOMMeasurement( xmlWriter, observation );
        } else {
            throw new OWSException( "the resultModel " + model + " is invalid", OWSException.INVALID_PARAMETER_VALUE,
                                    "resultModel" );
        }
    }

    private void validateGetObservation( GetObservation observationReq )
                            throws OWSException {
        if ( observationReq.getSRSName() != null && !observationReq.getSRSName().trim().equals( "" ) ) {
            try {
                CRSManager.lookup( observationReq.getSRSName() );
            } catch ( UnknownCRSException e ) {
                throw new OWSException( "Invalid SRS name given: " + observationReq.getSRSName(),
                                        OWSException.INVALID_PARAMETER_VALUE, "srsName" );
            }
        }
        validateParameterValue( "resultModel", observationReq.getResultModel(), "", "Observation", "om:Observation",
                                "Measurement", "om:Measurement" );
        validateParameterValue( "responseFormat", observationReq.getResponseFormat(), "",
                                "text/xml;subtype=\"om/1.0.0\"", "text/xml; subtype=\"om/1.0.0\"" );
        validateParameterValue( "responseMode", observationReq.getResponseMode(), "", "inline" );

        Offering offering = sosService.getOffering( observationReq.getOffering() );
        // List<Property> props = offering.getProperties();
        // String[] allProps = new String[props.size()];
        // for ( int i = 0; i < props.size(); i++ ) {
        // allProps[i] = props.get( i ).getHref();
        // }
        // for ( PropertyFilter prop : observationReq.getObservedProperties() ) {
        // validateParameterValue( "observedProperty", prop.getPropertyName(), allProps );
        // }

        String[] procs = new String[offering.getProcedures().size()];
        for ( int i = 0; i < procs.length; ++i ) {
            procs[i] = offering.getProcedures().get( i ).getProcedureHref();
        }
        for ( ProcedureFilter proc : observationReq.getProcedures() ) {
            validateParameterValue( "procedure", proc.getProcedureName(), procs );
        }

        List<String> featsList = new LinkedList<String>();
        for ( Procedure proc : offering.getProcedures() ) {
            featsList.add( proc.getFeatureOfInterestHref() ); // TODO this could be more than just one
        }
        String[] feats = featsList.toArray( new String[featsList.size()] );
        Pair<List<String>, SpatialFilter> p = observationReq.getFeatureOfInterest();
        if ( p != null ) {
            for ( String s : p.first ) {
                validateParameterValue( "featureOfInterest", s, feats );
            }
        }
    }

    private static void validateParameterValue( String locator, String value, String... validValues )
                            throws OWSException {
        if ( value == null ) {
            throw new OWSException( "the " + locator + " parameter is missing", OWSException.MISSING_PARAMETER_VALUE,
                                    locator );
        }
        boolean isValid = false;
        for ( String valid : validValues ) {
            if ( value.equals( valid ) ) {
                isValid = true;
                break;
            }
        }
        if ( !isValid ) {
            throw new OWSException( "the " + locator + " " + value + " is invalid",
                                    OWSException.INVALID_PARAMETER_VALUE, locator );
        }
    }

    private static void validateDescribeSensor( DescribeSensor req )
                            throws OWSException {
        validateParameterValue( "outputFormat", req.getOutputFormat(), "text/xml;subtype=\"sensorML/1.0.1\"" );
        if ( req.getProcedure() == null ) {
            throw new OWSException( "The procedure parameter is missing.", OWSException.MISSING_PARAMETER_VALUE,
                                    "procedure" );
        }
    }

    private void doDescribeSensor( DescribeSensor describeReq, HttpResponseBuffer response )
                            throws IOException, OWSException {
        validateDescribeSensor( describeReq );

        boolean found = false;
        PrintWriter writer = response.getWriter();
        String requestedProcedure = describeReq.getProcedure();
        for ( Offering offering : sosService.getAllOfferings() ) {
            for ( Procedure proc : offering.getProcedures() ) {
                String procedure = proc.getProcedureHref();
                if ( requestedProcedure.equals( procedure ) ) {
                    found = true;
                    URL description = proc.getSensorURL();
                    LOG.debug( "trying to read {}", description );
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader( new InputStreamReader( description.openStream() ) );
                        String line;
                        while ( ( line = reader.readLine() ) != null ) {
                            writer.write( line );
                            writer.write( "\n" );
                        }
                    } catch ( FileNotFoundException e ) {
                        LOG.debug( "couldn't find SensorML file for {} at {}", requestedProcedure, description );
                        throw new OWSException( "an internal error occured while creating the response",
                                                NO_APPLICABLE_CODE );
                    } finally {
                        IOUtils.closeQuietly( reader );
                    }
                    break;
                }
            }
            if ( found ) {
                break;
            }
        }
        if ( !found ) {
            throw new OWSException( "the procedure " + requestedProcedure + " is invalid",
                                    OWSException.INVALID_PARAMETER_VALUE, "procedure" );
        }
        writer.flush();
    }

    private void doGetCapabilities( GetCapabilities capabilitiesReq, DeegreeServicesMetadataType serviceMetadata,
                                    HttpResponseBuffer response )
                            throws XMLStreamException, IOException, OWSException, ObservationDatastoreException {
        negotiateVersion( capabilitiesReq ); // throws OWS Exception, if version is not supported
        XMLStreamWriter xmlWriter = response.getXMLWriter();
        Set<Sections> sections = getSections( capabilitiesReq );
        ObservationStoreManager storeMgr = workspace.getSubsystemManager( ObservationStoreManager.class );
        Capabilities100XMLAdapter.export( sections, sosService.getAllOfferings(), serviceMetadata, identification,
                                          xmlWriter, storeMgr );
        xmlWriter.flush();
    }

    private static Set<Sections> getSections( GetCapabilities capabilitiesReq ) {
        Set<String> sections = capabilitiesReq.getSections();
        Set<Sections> result = new HashSet<Sections>();
        if ( sections.isEmpty() || sections.contains( "All" ) ) { // we don't have sections that "could be listed",
            // or?
            result.add( Sections.ServiceIdentification );
            result.add( Sections.ServiceProvider );
            result.add( Sections.OperationsMetadata );
            result.add( Sections.FilterCapabilities );
            result.add( Sections.Contents );
        } else {
            for ( String section : sections ) {
                try {
                    result.add( Sections.valueOf( section ) );
                } catch ( IllegalArgumentException ex ) {
                    // the spec does not say what to do, so we ignore it
                }
            }
        }
        return result;
    }

    private void sendServiceException( OWSException ex, HttpResponseBuffer response )
                            throws ServletException {
        sendException( null, getExceptionSerializer( SOS100 ), ex, response );
    }

    /**
     * @param param
     * @throws OWSException
     */
    private void checkRequiredKeys( Map<String, String> param )
                            throws OWSException {
        try {
            String service = KVPUtils.getRequired( param, "SERVICE" );
            if ( !"SOS".equalsIgnoreCase( service ) ) {
                throw new OWSException( "SERVICE " + service + " is not supported",
                                        OWSException.INVALID_PARAMETER_VALUE, "SERVICE" );
            }
            String request = KVPUtils.getRequired( param, "REQUEST" );
            if ( !getHandledRequests().contains( request ) ) {
                throw new OWSException( "REQUEST " + request + " is not supported",
                                        OWSException.INVALID_PARAMETER_VALUE, "REQUEST" );
            }
            String version;
            if ( IMPLEMENTATION_METADATA.getRequestTypeByName( request ) != SOSRequestType.GetCapabilities ) { // no
                // version
                // required
                version = KVPUtils.getRequired( param, "VERSION" );
                if ( version != null && !offeredVersions.contains( Version.parseVersion( version ) ) ) {
                    throw new OWSException( "VERSION " + version + " is not supported", VERSION_NEGOTIATION_FAILED,
                                            "VERSION" );
                }
            }
        } catch ( MissingParameterException e ) {
            throw new OWSException( e.getMessage(), OWSException.MISSING_PARAMETER_VALUE );
        }
    }

    /**
     * sets the identification to the main controller or it will be synchronized with the maincontroller. sets the
     * provider to the provider of the configured main controller or it will be synchronized with it's values.
     */
    private void syncWithMainController() {
        identification = mainMetadataConf.getServiceIdentification();
    }

    @Override
    public XMLExceptionSerializer getExceptionSerializer( Version requestVersion ) {
        return new SOS100ExceptionReportSerializer( httpCodeForExceptions );
    }

    /* (non-Javadoc)
     * @see org.deegree.workspace.Resource#getMetadata()
     */
    @Override
    public ResourceMetadata<? extends Resource> getMetadata() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.deegree.workspace.Resource#init()
     */
    @Override
    public void init() {
        // TODO Auto-generated method stub
        
    }
}
