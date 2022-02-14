//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2022 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.services.wps.provider.fme;

import org.deegree.services.wps.provider.fme.jaxb.FMEServer;
import org.apache.axiom.om.OMElement;
import org.apache.http.HttpResponse;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.net.HttpUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.process.jaxb.java.ComplexFormatType;
import org.deegree.process.jaxb.java.ComplexOutputDefinition;
import org.deegree.process.jaxb.java.LanguageStringType;
import org.deegree.process.jaxb.java.LiteralInputDefinition;
import org.deegree.process.jaxb.java.ProcessDefinition;
import org.deegree.services.wps.provider.ProcessProvider;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.AbstractResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import static org.deegree.commons.utils.net.HttpUtils.UTF8STRING;
import static org.deegree.commons.utils.net.HttpUtils.postFullResponse;
import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;

/**
 * {@link ResourceBuilder} building a {@link FMEProcessProvider}
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FMEProcessProviderBuilder implements ResourceBuilder<ProcessProvider> {

    private static final Logger LOG = LoggerFactory.getLogger( FMEProcessProviderBuilder.class );

    private Workspace workspace;

    private ResourceLocation<ProcessProvider> location;

    private AbstractResourceProvider<ProcessProvider> provider;

    public FMEProcessProviderBuilder(
                            Workspace workspace, ResourceLocation<ProcessProvider> location,
                            AbstractResourceProvider<ProcessProvider> provider ) {
        this.workspace = workspace;
        this.location = location;
        this.provider = provider;
    }

    @Override
    public ProcessProvider build() {
        try {
            InputStream config = location.getAsStream();
            FMEServer server;
            server = (FMEServer) unmarshall( "org.deegree.services.wps.provider.fme.jaxb", provider.getSchema(), config,
                                             workspace );
            String user = "";
            String pass = "";
            if ( server.getUsername() != null ) {
                user = server.getUsername();
                pass = server.getPassword();
            }
            String base = server.getAddress();
            if ( base.endsWith( "/" ) ) {
                base = base.substring( 0, base.length() - 1 );
            }
            String resturl = base + "/fmerest/";
            String tokenurl = base + "/fmetoken/service/generate";

            HashSet<String> repositories = new HashSet<String>();
            List<String> list = server.getRepository();
            if ( list == null || list.isEmpty() ) {
                repositories.add( "wps" );
            } else {
                for ( String s : list ) {
                    repositories.add( s.trim().toLowerCase() );
                }
            }

            Map<String, String> map = new HashMap<String, String>();
            map.put( "user", user );
            map.put( "password", pass );
            map.put( "expiration", "1" );
            map.put( "timeunit", "hour" );
            LOG.debug( "Sending {}", tokenurl );
            Pair<String, HttpResponse> pair = postFullResponse( UTF8STRING, tokenurl, map, null, 0 );
            if ( pair.second.getStatusLine().getStatusCode() == 401 ) {
                throw new ResourceInitException( "Could not authenticate against token service. "
                                                 + "Check username/password in configuration." );
            }
            String token = pair.first.trim();
            String url = resturl + "repositories.xml?token=" + token;
            LOG.debug( "Sending {}", url );
            XMLAdapter xml = logAdapter( retrieve( url ) );
            Map<CodeType, FMEProcess> processes = new HashMap<CodeType, FMEProcess>();
            for ( String repo : xml.getNodesAsStrings( xml.getRootElement(), new XPath( "//name" ) ) ) {
                LOG.debug( "Found repository {}.", repo );
                if ( !repositories.contains( repo.trim().toLowerCase() ) ) {
                    LOG.debug( "Skipping repository {} because it was not configured.", repo );
                    continue;
                }
                String workspaces = resturl + "repositories/" + repo + ".xml?token=" + token;
                LOG.debug( "Sending {}", workspaces );
                XMLAdapter ws = logAdapter( retrieve( workspaces ) );
                XPath xpath = new XPath( "//workspace[isEnabled = 'true']" );
                for ( OMElement workspace : ws.getElements( ws.getRootElement(), xpath ) ) {
                    createProcesses( base, tokenurl, repositories, map, token, processes, repo, ws, workspace );
                }
            }
            FMEProcessMetadata metadata = new FMEProcessMetadata( workspace, location, provider );
            return new FMEProcessProvider( processes, metadata );
        } catch ( Exception e ) {
            if ( e instanceof ResourceInitException ) {
                throw (ResourceInitException) e;
            }
            throw new ResourceInitException(
                                    "Error creating FME processes from configuration '" + location.getIdentifier() +
                                    "': "
                                    + e.getMessage(), e );
        }
    }

    private void createProcesses( String base, String tokenurl, HashSet<String> repositories, Map<String, String> map,
                                  String token, Map<CodeType, FMEProcess> processes, String repo, XMLAdapter ws,
                                  OMElement workspace ) {
        try {
            FMEProcess process = createProcess( base, tokenurl, repositories, map, token, repo, ws,
                                                workspace );
            CodeType id = repositories.size() == 1 ?
                          new CodeType( process.getDescription().getIdentifier().getValue() )
                                                   :
                          new CodeType( process.getDescription().getIdentifier().getValue(), repo );
            LOG.debug( "Created FMEProcess: " + id );
            processes.put( id, process );
        } catch ( Exception e ) {
            LOG.error( "Unable to create FMEProcess from element '" + workspace + "': " + e.getMessage(), e );
        }
    }

    private FMEProcess createProcess( String base, String tokenurl, HashSet<String> repositories,
                                      Map<String, String> map, String token,
                                      String repo, XMLAdapter ws,
                                      OMElement workspace )
                            throws IOException, ResourceInitException {

        String name = ws.getRequiredNodeAsString( workspace, new XPath( "name" ) );
        String title = ws.getNodeAsString( workspace, new XPath( "title" ), null );
        String descr = ws.getRequiredNodeAsString( workspace, new XPath( "description" ) );
        String uri = ws.getRequiredNodeAsString( workspace, new XPath( "uri" ) );

        ProcessDefinition.InputParameters inputs;
        try {
            inputs = determineProcessInputs( base, uri, token );
        } catch ( Exception e ) {
            String msg = "Error determining process inputs: " + e.getMessage();
            throw new ResourceInitException( msg, e );
        }

        FMEInvocationStrategy invocationStrategy;
        try {
            invocationStrategy = determineInvocationStrategy( base, uri, token );
        } catch ( Exception e ) {
            String msg = "Error determining process outputs: " + e.getMessage();
            throw new ResourceInitException( msg, e );
        }

        return new FMEProcess( inputs, invocationStrategy, name, title, repo, descr, base, uri, tokenurl, token, map,
                               repositories.size() != 1 );
    }

    private ProcessDefinition.InputParameters determineProcessInputs( String base, String uri, String token )
                            throws IOException {

        String url = base + encodeReportedFmeUrisHack( uri ) + "/parameters.xml?token=" + token;
        LOG.debug( "Sending {}", url );
        XMLAdapter params = logAdapter( retrieve( url ) );

        ProcessDefinition.InputParameters inputs = new ProcessDefinition.InputParameters();
        for ( OMElement param : params.getElements( params.getRootElement(), new XPath( "//parameter" ) ) ) {
            String name = params.getRequiredNodeAsString( param, new XPath( "name" ) );
            String description = params.getRequiredNodeAsString( param, new XPath( "description" ) );
            LiteralInputDefinition input = new LiteralInputDefinition();
            org.deegree.process.jaxb.java.CodeType id = new org.deegree.process.jaxb.java.CodeType();
            id.setValue( name );
            input.setIdentifier( id );
            LanguageStringType title = new LanguageStringType();
            title.setValue( description );
            input.setTitle( title );
            JAXBElement<LiteralInputDefinition> inEl;
            inEl = new JAXBElement<LiteralInputDefinition>( new QName( "" ), LiteralInputDefinition.class, input );
            inEl.getValue().setMinOccurs( BigInteger.valueOf( 0 ) );
            inputs.getProcessInput().add( inEl );
        }
        return inputs;
    }

    private FMEInvocationStrategy determineInvocationStrategy( String base, String uri, String token )
                            throws IOException {

        FMEInvocationStrategy strategy = null;
        try {
            strategy = getStreamStrategy( base, uri, token );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        if ( strategy != null ) {
            LOG.info( "FME Invocation Strategy: Streaming Service" );
        } else {
            LOG.info( "FME Invocation Strategy: Job Submitter" );
            strategy = new FMEJobSubmitterInvocationStrategy();
        }
        return strategy;
    }

    private FMEStreamingServiceInvocationStrategy getStreamStrategy( String base, String uri, String token )
                            throws IOException {

        String fmeOutputWriters = determineOutputWriters( base, uri, token );
        if ( fmeOutputWriters == null || fmeOutputWriters.trim().isEmpty() ) {
            LOG.debug( "No FME output writers for '" + uri + "'." );
            return null;
        }

        ProcessDefinition.OutputParameters outputs = new ProcessDefinition.OutputParameters();
        StringTokenizer st = new StringTokenizer( fmeOutputWriters );
        String outputWriter = null;
        while ( st.hasMoreTokens() ) {
            outputWriter = st.nextToken();
            String url = base + encodeReportedFmeUrisHack( uri ) + "/writers/" + outputWriter + ".xml?token=" + token;
            LOG.debug( "Sending {}", url );
            XMLAdapter writer = logAdapter( retrieve( url ) );
            OMElement writerEl = writer.getElement( writer.getRootElement(), new XPath( "//writer[name='" + outputWriter
                                                                                        + "']" ) );
            if ( writerEl == null ) {
                continue;
            }
            String format = writer.getNodeAsString( writerEl, new XPath( "format" ), null );
            LOG.info( "FME writer format: " + format );
            ComplexOutputDefinition response = getComplexOutputDefinition( outputs, writerEl, format );
            JAXBElement<ComplexOutputDefinition> jaxbElement = new JAXBElement<ComplexOutputDefinition>(
                                    new QName( "" ),
                                    ComplexOutputDefinition.class, response );
            outputs.getProcessOutput().add( jaxbElement );
        }
        return new FMEStreamingServiceInvocationStrategy( outputs, outputWriter, fmeOutputWriters );
    }

    private ComplexOutputDefinition getComplexOutputDefinition( ProcessDefinition.OutputParameters outputs,
                                                                OMElement writerEl, String format ) {
        if ( "GML".equals( format ) || "GML2".equals( format ) ) {
            return getGmlOutputDefinition( outputs );
        } else if ( "TEXTLINE".equals( format ) ) {
            return getTextlineOutputDefinition( writerEl );
        } else {
            return getDefaultOutputDefinition();
        }
    }

    private ComplexOutputDefinition getTextlineOutputDefinition( OMElement writerEl ) {
        XMLAdapter adapter = new XMLAdapter( writerEl );
        ComplexOutputDefinition response = new ComplexOutputDefinition();
        org.deegree.process.jaxb.java.CodeType id = new org.deegree.process.jaxb.java.CodeType();
        id.setValue( "FMEResponse" );
        LanguageStringType title = new LanguageStringType();
        title.setValue( "Response from FME (Streaming Service)" );
        response.setTitle( title );
        ComplexFormatType fmtType = new ComplexFormatType();
        XPath xpath = new XPath( "properties/property[category='METAFILE_PARAMETER' and name='MIME_TYPE']/value" );
        String mimeType = adapter.getNodeAsString( writerEl, xpath, null );
        if ( mimeType == null ) {
            LOG.warn( "Unable to determine mime type from FME workspace description. Defaulting to 'application/octet-stream'." );
            mimeType = "application/octet-stream";
        }
        if ( !mimeType.startsWith( "text" ) ) {
            fmtType.setEncoding( "base64" );
        }
        fmtType.setMimeType( mimeType );
        response.setDefaultFormat( fmtType );
        response.setIdentifier( id );
        return response;
    }

    private ComplexOutputDefinition getDefaultOutputDefinition() {
        ComplexOutputDefinition response;
        response = new ComplexOutputDefinition();
        org.deegree.process.jaxb.java.CodeType id = new org.deegree.process.jaxb.java.CodeType();
        id.setValue( "FMEResponse" );
        LanguageStringType title = new LanguageStringType();
        title.setValue( "Response from FME (Streaming Service)" );
        response.setTitle( title );
        ComplexFormatType fmtType = new ComplexFormatType();
        fmtType.setEncoding( "base64" );
        fmtType.setMimeType( "application/octet-stream" );
        response.setDefaultFormat( fmtType );
        response.setIdentifier( id );
        return response;
    }

    private ComplexOutputDefinition getGmlOutputDefinition( ProcessDefinition.OutputParameters outputs ) {
        ComplexOutputDefinition response = new ComplexOutputDefinition();
        org.deegree.process.jaxb.java.CodeType id = new org.deegree.process.jaxb.java.CodeType();
        id.setValue( "GML" );
        LanguageStringType title = new LanguageStringType();
        title.setValue( "GML response from FME Server" );
        response.setTitle( title );
        ComplexFormatType fmtType = new ComplexFormatType();
        fmtType.setMimeType( "application/xml" );
        response.setDefaultFormat( fmtType );
        response.setIdentifier( id );
        outputs.getProcessOutput().add(
                                new JAXBElement<ComplexOutputDefinition>( new QName( "" ),
                                                                          ComplexOutputDefinition.class, response ) );
        response = new ComplexOutputDefinition();
        id = new org.deegree.process.jaxb.java.CodeType();
        id.setValue( "APPSCHEMA" );
        title = new LanguageStringType();
        title.setValue( "GML schema response from FME Server" );
        response.setTitle( title );
        fmtType = new ComplexFormatType();
        fmtType.setMimeType( "application/xml" );
        response.setDefaultFormat( fmtType );
        response.setIdentifier( id );
        return response;
    }

    // private FMEStreamInvocationStrategy getStreamStrategy(String base, String
    // uri, String token)
    // throws MalformedURLException, IOException {
    //
    // String fmeOutputWriters = determineOutputWriters(base, uri, token);
    // if (fmeOutputWriters == null || fmeOutputWriters.trim().isEmpty()) {
    // LOG.debug("No FME output writers for '" + uri + "'.");
    // return null;
    // }
    //
    // OutputParameters outputs = new OutputParameters();
    // StringTokenizer st = new StringTokenizer(fmeOutputWriters);
    // String outputFormat = null;
    // while (st.hasMoreTokens()) {
    // outputFormat = st.nextToken();
    // String url = base + uri + "/writers/" + outputFormat + ".xml?token=" +
    // token;
    // LOG.debug("Sending {}", url);
    // XMLAdapter writer = logAdapter(retrieve(XML, url));
    // String format = writer.getRequiredNodeAsString(writer.getRootElement(),
    // new XPath("//writer[name='"
    // + outputFormat + "']/format"));
    //
    // if (format.equals("GML") || format.equals("GML2")) {
    // ComplexOutputDefinition response = new ComplexOutputDefinition();
    // org.deegree.process.jaxb.java.CodeType id = new
    // org.deegree.process.jaxb.java.CodeType();
    // id.setValue(fmeOutputWriters + ".gml");
    // LanguageStringType title = new LanguageStringType();
    // title.setValue("GML response from FME Server");
    // response.setTitle(title);
    // ComplexFormatType fmtType = new ComplexFormatType();
    // fmtType.setMimeType("application/xml");
    // response.setDefaultFormat(fmtType);
    // response.setIdentifier(id);
    // outputs.getProcessOutput()
    // .add(new JAXBElement<ComplexOutputDefinition>(new QName(""),
    // ComplexOutputDefinition.class,
    // response));
    // response = new ComplexOutputDefinition();
    // id = new org.deegree.process.jaxb.java.CodeType();
    // id.setValue(fmeOutputWriters + ".xsd");
    // title = new LanguageStringType();
    // title.setValue("GML schema response from FME Server");
    // response.setTitle(title);
    // fmtType = new ComplexFormatType();
    // fmtType.setMimeType("application/xml");
    // response.setDefaultFormat(fmtType);
    // response.setIdentifier(id);
    // outputs.getProcessOutput()
    // .add(new JAXBElement<ComplexOutputDefinition>(new QName(""),
    // ComplexOutputDefinition.class,
    // response));
    // } else {
    // ComplexOutputDefinition response = new ComplexOutputDefinition();
    // org.deegree.process.jaxb.java.CodeType id = new
    // org.deegree.process.jaxb.java.CodeType();
    // id.setValue(fmeOutputWriters);
    // LanguageStringType title = new LanguageStringType();
    // title.setValue("Response from FME Server");
    // response.setTitle(title);
    // ComplexFormatType fmtType = new ComplexFormatType();
    // fmtType.setEncoding("base64");
    // fmtType.setMimeType("application/octet-stream");
    // response.setDefaultFormat(fmtType);
    // response.setIdentifier(id);
    // outputs.getProcessOutput()
    // .add(new JAXBElement<ComplexOutputDefinition>(new QName(""),
    // ComplexOutputDefinition.class,
    // response));
    // }
    // }
    // return new FMEStreamInvocationStrategy(outputs, outputFormat,
    // fmeOutputWriters);
    // }

    private String determineOutputWriters( String base, String uri, String token )
                            throws IOException {
        String url = base + encodeReportedFmeUrisHack( uri ) + "/fmedatastreaming/properties.xml?token=" + token;
        LOG.debug( "Sending {}", url );
        XMLAdapter props = logAdapter( retrieve( url ) );
        return props.getNodeAsString( props.getRootElement(), new XPath(
                                "//property[name='OUTPUT_WRITER']/value" ), null );
    }

    private static XMLAdapter logAdapter( XMLAdapter adapter ) {
        if ( LOG.isDebugEnabled() ) {
            try {
                StringWriter out = new StringWriter();
                XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter( out );
                writer = new IndentingXMLStreamWriter( writer );
                adapter.getRootElement().serialize( writer );
                LOG.debug( "Response was\n{}", out );
            } catch ( Throwable e ) {
                LOG.trace( "Stack trace while debugging: ", e );
            }
        }
        return adapter;
    }

    private String encodeReportedFmeUrisHack( String uri ) {
        // cannot use URLEncoder here, as this would encode slashes as well...
        return uri.replace( " ", "+" );
    }

    private XMLAdapter retrieve( String url )
                            throws IOException {
        InputStream retrieve = HttpUtils.retrieve( HttpUtils.STREAM, url );
        return new XMLAdapter( retrieve );
    }

}
