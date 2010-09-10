//$HeadURL$
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

package org.deegree.services.wcs;

import static javax.xml.stream.XMLOutputFactory.IS_REPAIRING_NAMESPACES;
import static org.deegree.protocol.wcs.WCSConstants.VERSION_100;
import static org.deegree.protocol.wcs.WCSConstants.VERSION_110;
import static org.deegree.protocol.wcs.WCSConstants.WCS_100_NS;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.commons.fileupload.FileItem;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.FormattingXMLStreamWriter;
import org.deegree.coverage.rangeset.AxisSubset;
import org.deegree.coverage.rangeset.RangeSet;
import org.deegree.coverage.raster.interpolation.InterpolationType;
import org.deegree.cs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.ows.capabilities.GetCapabilities;
import org.deegree.protocol.wcs.WCSConstants;
import org.deegree.protocol.wcs.WCSConstants.WCSRequestType;
import org.deegree.protocol.wcs.capabilities.GetCapabilities100KVPAdapter;
import org.deegree.services.controller.AbstractOGCServiceController;
import org.deegree.services.controller.ImplementationMetadata;
import org.deegree.services.controller.exception.ControllerException;
import org.deegree.services.controller.exception.ControllerInitException;
import org.deegree.services.controller.exception.serializer.XMLExceptionSerializer;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.jaxb.main.DeegreeServiceControllerType;
import org.deegree.services.jaxb.main.DeegreeServicesMetadataType;
import org.deegree.services.jaxb.main.ServiceIdentificationType;
import org.deegree.services.jaxb.main.ServiceProviderType;
import org.deegree.services.jaxb.wcs.PublishedInformation;
import org.deegree.services.jaxb.wcs.PublishedInformation.AllowedOperations;
import org.deegree.services.wcs.capabilities.Capabilities100XMLAdapter;
import org.deegree.services.wcs.capabilities.GetCapabilities100XMLAdapter;
import org.deegree.services.wcs.capabilities.Capabilities100XMLAdapter.Sections;
import org.deegree.services.wcs.coverages.WCSCoverage;
import org.deegree.services.wcs.describecoverage.CoverageDescription100XMLAdapter;
import org.deegree.services.wcs.describecoverage.DescribeCoverage;
import org.deegree.services.wcs.describecoverage.DescribeCoverage100KVPAdapter;
import org.deegree.services.wcs.describecoverage.DescribeCoverage100XMLAdapter;
import org.deegree.services.wcs.getcoverage.GetCoverage;
import org.deegree.services.wcs.getcoverage.GetCoverage100KVPAdapter;
import org.deegree.services.wcs.getcoverage.GetCoverage100XMLAdapter;
import org.deegree.services.wcs.model.CoverageOptions;
import org.deegree.services.wcs.model.CoverageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the <a href="http://www.opengeospatial.org/standards/wcs">OpenGIS Web Coverage Service</a> server
 * protocol.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WCSController extends AbstractOGCServiceController {

    private static int UPDATE_SEQUENCE = -1;

    private static final String COVERAGE_NOT_DEFINED = "CoverageNotDefined";

    private static final Logger LOG = LoggerFactory.getLogger( WCSController.class );

    private WCService wcsService;

    private List<String> allowedOperations = new LinkedList<String>();

    private ServiceIdentificationType identification;

    private ServiceProviderType provider;

    private static final String CONFIG_PRE = "dwcs";

    private static final String CONFIG_NS = "http://www.deegree.org/services/wcs";

    private final static String PUBLISHED_SCHEMA_FILE = "/META-INF/schemas/wcs/0.5.0/wcs_published_information.xsd";

    private static final ImplementationMetadata<WCSRequestType> IMPLEMENTATION_METADATA = new ImplementationMetadata<WCSRequestType>() {
        {
            supportedVersions = new Version[] { VERSION_100, VERSION_110 };
            handledNamespaces = new String[] { WCS_100_NS };
            handledRequests = WCSRequestType.class;
            supportedConfigVersions = new Version[] { Version.parseVersion( "0.5.0" ) };
        }
    };

    @Override
    public void init( XMLAdapter controllerConf, DeegreeServicesMetadataType serviceMetadata,
                      DeegreeServiceControllerType mainConf )
                            throws ControllerInitException {
        UPDATE_SEQUENCE++;
        init( serviceMetadata, mainConf, IMPLEMENTATION_METADATA, controllerConf );

        NamespaceContext nsContext = new NamespaceContext();
        nsContext.addNamespace( WCSConstants.WCS_100_PRE, WCS_100_NS );
        nsContext.addNamespace( WCSConstants.WCS_110_PRE, WCSConstants.WCS_110_NS );
        nsContext.addNamespace( CONFIG_PRE, CONFIG_NS );

        ServiceConfigurationXMLAdapter serviceConfigAdapter = parseServiceConfiguration( controllerConf, nsContext );

        this.wcsService = new WCServiceBuilder( serviceConfigAdapter ).buildService();

        PublishedInformation publishedInformation = parsePublishedInformation( controllerConf, nsContext );
        syncWithMainController( publishedInformation );

        validateAndSetOfferedVersions( publishedInformation.getSupportedVersions().getVersion() );
    }

    /**
     * sets the identification to the main controller or it will be synchronized with the maincontroller. sets the
     * provider to the provider of the configured main controller or it will be synchronized with it's values.
     * 
     * @param publishedInformation
     */
    private void syncWithMainController( PublishedInformation publishedInformation ) {
        if ( identification == null ) {
            if ( publishedInformation == null || publishedInformation.getServiceIdentification() == null ) {
                LOG.info( "Using global service identification because no WCS specific service identification was defined." );
                identification = mainMetadataConf.getServiceIdentification();
            } else {
                identification = synchronizeServiceIdentificationWithMainController( publishedInformation.getServiceIdentification() );
            }
        }
        if ( provider == null ) {
            if ( publishedInformation == null || publishedInformation.getServiceProvider() == null ) {
                LOG.info( "Using global serviceProvider because no WCS specific service provider was defined." );
                provider = mainMetadataConf.getServiceProvider();
            } else {
                provider = synchronizeServiceProviderWithMainControllerConf( publishedInformation.getServiceProvider() );
            }
        }
    }

    @Override
    public void destroy() {
        // *Kaaboooom!*
    }

    private ServiceConfigurationXMLAdapter parseServiceConfiguration( XMLAdapter controllerConf,
                                                                      NamespaceContext nsContext ) {
        OMElement confElem = controllerConf.getRequiredElement( controllerConf.getRootElement(),
                                                                new XPath( CONFIG_PRE + ":ServiceConfiguration",
                                                                           nsContext ) );
        ServiceConfigurationXMLAdapter serviceConfigAdapter = new ServiceConfigurationXMLAdapter();
        serviceConfigAdapter.setRootElement( confElem );
        serviceConfigAdapter.setSystemId( controllerConf.getSystemId() );
        return serviceConfigAdapter;
    }

    private PublishedInformation parsePublishedInformation( XMLAdapter controllerConf, NamespaceContext nsContext )
                            throws ControllerInitException {

        PublishedInformation pubInf = null;
        try {
            Unmarshaller u = getUnmarshaller( "org.deegree.services.jaxb.wcs", PUBLISHED_SCHEMA_FILE );
            XPath xp = new XPath( CONFIG_PRE + ":PublishedInformation", nsContext );
            OMElement elem = controllerConf.getElement( controllerConf.getRootElement(), xp );
            if ( elem != null ) {
                pubInf = (PublishedInformation) u.unmarshal( elem.getXMLStreamReaderWithoutCaching() );
                if ( pubInf != null ) {
                    // mandatory
                    allowedOperations.add( WCSRequestType.GetCapabilities.name() );
                    AllowedOperations configuredOperations = pubInf.getAllowedOperations();
                    if ( configuredOperations != null ) {
                        // if ( configuredOperations.getDescribeCoverage() != null ) {
                        // if
                        // }
                        LOG.info( "WCS specification implies support for all three Operations." );
                    }
                    allowedOperations.add( WCSRequestType.DescribeCoverage.name() );
                    allowedOperations.add( WCSRequestType.GetCoverage.name() );
                }
            }
        } catch ( JAXBException e ) {
            throw new ControllerInitException(
                                               "Error while unmarshalling the published information from the configuration file: "
                                                                       + e.getLocalizedMessage(), e );
        }
        return pubInf;
    }

    @Override
    public void doKVP( Map<String, String> param, HttpServletRequest request, HttpResponseBuffer response,
                       List<FileItem> multiParts )
                            throws ServletException, IOException {
        try {
            checkRequiredKeys( param );
            WCSRequestType requestType = getRequestType( param );
            LOG.debug( "Handling {} request: {}", requestType, param );
            switch ( requestType ) {
            case GetCoverage:
                GetCoverage coverageReq = GetCoverage100KVPAdapter.parse( param );
                doGetCoverage( coverageReq, response );
                break;
            case GetCapabilities:
                GetCapabilities capabilitiesReq = GetCapabilities100KVPAdapter.parse( param );
                doGetCapabilities( capabilitiesReq, request, response );
                break;
            case DescribeCoverage:
                DescribeCoverage describeReq = DescribeCoverage100KVPAdapter.parse( param );
                doDescribeCoverage( describeReq, response );
                break;
            }

        } catch ( MissingParameterException e ) {
            sendServiceException( new OWSException( e.getLocalizedMessage(), OWSException.MISSING_PARAMETER_VALUE ),
                                  response );
        } catch ( OWSException ex ) {
            sendServiceException( ex, response );
        } catch ( XMLStreamException e ) {
            sendServiceException( new OWSException( "an error occured while processing a request",
                                                    ControllerException.NO_APPLICABLE_CODE ), response );
            LOG.error( "an error occured while processing a request", e );
        }
    }

    @Override
    public void doXML( XMLStreamReader xmlStream, HttpServletRequest request, HttpResponseBuffer response,
                       List<FileItem> multiParts )
                            throws ServletException, IOException {

        try {
            XMLAdapter requestDoc = new XMLAdapter( xmlStream );
            OMElement rootElement = requestDoc.getRootElement();
            String rootName = rootElement.getLocalName();

            switch ( IMPLEMENTATION_METADATA.getRequestTypeByName( rootName ) ) {
            case GetCapabilities:
                GetCapabilities100XMLAdapter capa = new GetCapabilities100XMLAdapter( rootElement );
                doGetCapabilities( capa.parse(), request, response );
                break;
            case DescribeCoverage:
                DescribeCoverage100XMLAdapter describe = new DescribeCoverage100XMLAdapter( rootElement );
                doDescribeCoverage( describe.parse(), response );
                break;
            case GetCoverage:
                GetCoverage100XMLAdapter getCoverage = new GetCoverage100XMLAdapter( rootElement );
                doGetCoverage( getCoverage.parse(), response );
                break;
            }
        } catch ( OWSException ex ) {
            sendServiceException( ex, response );
        } catch ( XMLStreamException e ) {
            sendServiceException( new OWSException( "An error occured while processing a request",
                                                    ControllerException.NO_APPLICABLE_CODE ), response );
            LOG.error( "an error occured while processing a request", e );
        }
    }

    private void doGetCoverage( GetCoverage coverageReq, HttpResponseBuffer response )
                            throws IOException, OWSException {
        if ( wcsService.hasCoverage( coverageReq.getCoverage() ) ) {
            WCSCoverage coverage = wcsService.getCoverage( coverageReq.getCoverage() );
            if ( coverageReq.getVersion().equals( WCSConstants.VERSION_100 ) ) {
                // do wcs 1.0.0 specific request checking.
                if ( coverageReq.getRangeSet() != null && coverage.getRangeSet() != null ) {
                    checkRangeSet( coverage.getRangeSet(), coverageReq.getRangeSet() );
                }
            }
            testIntersectingBBox( coverage, coverageReq );
            checkOutputOptions( coverageReq, coverage.getCoverageOptions() );
            response.setContentType( "image/" + coverageReq.getOutputFormat() );

            CoverageResult result;
            try {
                result = coverage.getCoverageResult( coverageReq.getRequestEnvelope(), coverageReq.getOutputGrid(),
                                                     coverageReq.getOutputFormat(), coverageReq.getInterpolation(),
                                                     coverageReq.getRangeSet() );
            } catch ( WCServiceException e ) {
                throw new OWSException( "An error occured while creating the coverage result: " + e.getMessage(),
                                        ControllerException.NO_APPLICABLE_CODE );
            }
            result.write( response.getOutputStream() );

        } else {
            throw new OWSException( "The coverage " + coverageReq.getCoverage() + " is invalid", COVERAGE_NOT_DEFINED,
                                    "offering" );
        }
    }

    /**
     * Tests if the requested bbox intersects with the bbox of the coverage, if not, an exception must be thrown.
     * 
     * @param coverage
     *            from the {@link WCService}
     * @param coverageReq
     *            requested.
     * @throws OWSException
     */
    private void testIntersectingBBox( WCSCoverage coverage, GetCoverage coverageReq )
                            throws OWSException {
        Envelope rEnv = coverageReq.getRequestEnvelope();
        if ( rEnv != null ) {
            CRS crs = rEnv.getCoordinateSystem();
            boolean intersects = true;
            if ( crs == null ) {
                // test against the default crs.
                intersects = rEnv.intersects( coverage.getEnvelope() );
            } else {
                Iterator<Envelope> it = coverage.responseEnvelopes.iterator();
                Envelope defEnv = null;
                while ( it.hasNext() && defEnv == null ) {
                    Envelope e = it.next();
                    if ( e != null ) {
                        CRS eCRS = e.getCoordinateSystem();
                        if ( crs.equals( eCRS ) ) {
                            defEnv = e;
                        }
                    }
                }
                if ( defEnv == null ) {
                    defEnv = coverage.getEnvelope();
                }
                intersects = rEnv.intersects( defEnv );
            }
            if ( !intersects ) {
                throw new OWSException( "Given is outside the bbox of the coverage.",
                                        OWSException.INVALID_PARAMETER_VALUE );
            }
        }

    }

    private void checkRangeSet( RangeSet configuredRangeSet, RangeSet requestedRangeSet )
                            throws OWSException {
        List<AxisSubset> reqAxis = requestedRangeSet.getAxisDescriptions();
        for ( AxisSubset ras : reqAxis ) {
            if ( ras.getName() != null ) {
                boolean hasMatch = false;
                Iterator<AxisSubset> it = configuredRangeSet.getAxisDescriptions().iterator();

                while ( it.hasNext() && !hasMatch ) {
                    AxisSubset as = it.next();
                    if ( as.getName().equalsIgnoreCase( ras.getName() ) ) {
                        boolean match = false;
                        try {
                            match = ras.match( as, true );
                        } catch ( NumberFormatException e ) {
                            throw new OWSException(
                                                    "Following rangeset: "
                                                                            + ras.getName()
                                                                            + " has an AxisDescriptions requesting a value which is not valid for the requested coverage",
                                                    OWSException.INVALID_PARAMETER_VALUE );
                        }
                        if ( !match ) {
                            throw new OWSException(
                                                    "Following rangeset: "
                                                                            + ras.getName()
                                                                            + " has an AxisDescriptions requesting a value which is not valid for the requested coverage",
                                                    OWSException.INVALID_PARAMETER_VALUE );
                        }
                    }
                }
            }
        }
    }

    private void checkOutputOptions( GetCoverage request, CoverageOptions options )
                            throws OWSException {
        boolean supported;
        String outputFormat = request.getOutputFormat();
        supported = options.getOutputFormats().contains( outputFormat );

        if ( !supported ) {
            // check for geotiff
            if ( outputFormat == null || !"geotiff".equals( outputFormat.toLowerCase() ) ) {
                throw new OWSException( "Unsupported output format (" + outputFormat + ")",
                                        OWSException.INVALID_PARAMETER_VALUE, "FORMAT" );
            }
        }
        String interpolation = request.getInterpolation();
        try {

            supported = options.getInterpolations().contains( InterpolationType.fromString( interpolation ) );
        } catch ( Exception e ) {
            throw new OWSException( "Unsupported interpolation (" + interpolation + ")",
                                    OWSException.INVALID_PARAMETER_VALUE, "INTERPOLATION" );
        }
        String crs = request.getOutputCRS();
        supported = options.getCRSs().contains( crs );
        if ( !supported ) {
            throw new OWSException( "unsupported response crs (" + crs + ")", OWSException.INVALID_PARAMETER_VALUE,
                                    "RESPONSE CRS" );
        }
    }

    private void doDescribeCoverage( DescribeCoverage describeReq, HttpResponseBuffer response )
                            throws IOException, XMLStreamException, OWSException {
        response.setContentType( "text/xml" );
        XMLStreamWriter xmlWriter = getXMLStreamWriter( response.getWriter() );
        List<WCSCoverage> coverages = new LinkedList<WCSCoverage>();
        if ( describeReq.getCoverages().size() == 0 ) { // return all
            coverages = wcsService.getAllCoverages();
        } else {
            for ( String reqCoverage : describeReq.getCoverages() ) {
                if ( wcsService.hasCoverage( reqCoverage ) ) {
                    coverages.add( wcsService.getCoverage( reqCoverage ) );
                } else {
                    throw new OWSException( "Unknown coverage " + reqCoverage, COVERAGE_NOT_DEFINED, "coverage" );
                }
            }
        }
        CoverageDescription100XMLAdapter.export( xmlWriter, coverages, UPDATE_SEQUENCE );
        xmlWriter.flush();
    }

    private void doGetCapabilities( GetCapabilities request, HttpServletRequest requestWrapper,
                                    HttpResponseBuffer response )
                            throws IOException, XMLStreamException, OWSException {

        Set<Sections> sections = getSections( request );

        checkOrCreateDCPGetURL( requestWrapper );
        checkOrCreateDCPPostURL( requestWrapper );

        Version negotiateVersion = negotiateVersion( request );
        // if update sequence is given and matches the given update sequence an error should occur
        // http://cite.opengeospatial.org/OGCTestData/wcs/1.0.0/specs/03-065r6.html#7.2.1_Key-value_pair_encoding
        if ( negotiateVersion.equals( VERSION_100 ) ) {
            String updateSeq = request.getUpdateSequence();
            int requestedUS = UPDATE_SEQUENCE - 1;
            try {
                requestedUS = Integer.parseInt( updateSeq );
            } catch ( NumberFormatException e ) {
                // nothing to do, just ignore it.
            }
            if ( requestedUS == UPDATE_SEQUENCE ) {
                throw new OWSException( "Update sequence may not be equal than server's current update sequence.",
                                        WCSConstants.ExeptionCode_1_0_0.CurrentUpdateSequence.name() );
            } else if ( requestedUS > UPDATE_SEQUENCE ) {
                throw new OWSException( "Update sequence may not be higher than server's current update sequence.",
                                        WCSConstants.ExeptionCode_1_0_0.InvalidUpdateSequence.name() );
            }
        }

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        factory.setProperty( IS_REPAIRING_NAMESPACES, true );

        response.setContentType( "text/xml" );
        XMLStreamWriter xmlWriter = getXMLStreamWriter( response.getWriter() );
        if ( negotiateVersion.equals( VERSION_100 ) ) {
            Capabilities100XMLAdapter.export( xmlWriter, request, identification, provider, allowedOperations,
                                              sections, wcsService.getAllCoverages(), mainMetadataConf,
                                              mainControllerConf, xmlWriter, UPDATE_SEQUENCE );
        } else {
            // the 1.1.0
        }
        xmlWriter.writeEndDocument();
        xmlWriter.flush();
    }

    private static Set<Sections> getSections( GetCapabilities capabilitiesReq ) {
        Set<String> sections = capabilitiesReq.getSections();
        Set<Sections> result = new HashSet<Sections>();
        if ( !( sections.isEmpty() || sections.contains( "/" ) ) ) {
            final int length = "/WCS_Capabilities/".length();
            for ( String section : sections ) {
                if ( section.startsWith( "/WCS_Capabilities/" ) ) {
                    section = section.substring( length );
                }
                try {
                    result.add( Sections.valueOf( section ) );
                } catch ( IllegalArgumentException ex ) {
                    // unknown section name
                    // the spec does not say what to do, so we ignore it
                }
            }
        }
        return result;
    }

    private void sendServiceException( OWSException ex, HttpResponseBuffer response )
                            throws ServletException {
        sendException( "application/vnd.ogc.se_xml", null, null, 200, new ServiceException120XMLAdapter(), ex, response );
    }

    private void checkRequiredKeys( Map<String, String> param )
                            throws OWSException {
        try {
            String service = KVPUtils.getRequired( param, "SERVICE" );
            if ( !"WCS".equalsIgnoreCase( service ) ) {
                throw new OWSException( "SERVICE " + service + " is not supported",
                                        OWSException.INVALID_PARAMETER_VALUE, "SERVICE" );
            }
            String request = KVPUtils.getRequired( param, "REQUEST" );
            if ( !getHandledRequests().contains( request ) ) {
                throw new OWSException( "REQUEST " + request + " is not supported",
                                        OWSException.OPERATION_NOT_SUPPORTED, "REQUEST" );
            }
            String version;
            if ( IMPLEMENTATION_METADATA.getRequestTypeByName( request ) != WCSRequestType.GetCapabilities ) {
                // no version required
                version = KVPUtils.getRequired( param, "VERSION" );
                if ( version != null && !offeredVersions.contains( Version.parseVersion( version ) ) ) {
                    throw new OWSException( "VERSION " + version + " is not supported",
                                            OWSException.VERSION_NEGOTIATION_FAILED, "VERSION" );
                }
            }
        } catch ( MissingParameterException e ) {
            throw new OWSException( e.getMessage(), OWSException.MISSING_PARAMETER_VALUE );
        }
    }

    private WCSRequestType getRequestType( Map<String, String> param )
                            throws OWSException {
        try {
            String requestName = KVPUtils.getRequired( param, "REQUEST" );
            return IMPLEMENTATION_METADATA.getRequestTypeByName( requestName );
        } catch ( MissingParameterException e ) {
            throw new OWSException( e.getMessage(), OWSException.MISSING_PARAMETER_VALUE );
        }
    }

    private static XMLStreamWriter getXMLStreamWriter( Writer writer )
                            throws XMLStreamException {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        factory.setProperty( XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE );
        return new FormattingXMLStreamWriter( factory.createXMLStreamWriter( writer ) );
    }

    @Override
    public Pair<XMLExceptionSerializer<OWSException>, String> getExceptionSerializer( Version requestVersion ) {
        return new Pair<XMLExceptionSerializer<OWSException>, String>( new ServiceException120XMLAdapter(),
                                                                       "application/vnd.ogc.se_xml" );
    }

}
