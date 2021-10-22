/*
 * Copyright lat/lon GmbH 2011
 * All rights reserved.
 */
package org.deegree.services.wps.provider.fme;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copyLarge;
import static org.deegree.commons.utils.net.HttpUtils.STREAM;
import static org.deegree.commons.utils.net.HttpUtils.UTF8STRING;
import static org.deegree.commons.utils.net.HttpUtils.post;
import static org.deegree.commons.utils.net.HttpUtils.postFullResponse;
import static org.deegree.commons.xml.XMLAdapter.writeElement;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.deegree.commons.utils.Pair;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.input.LiteralInput;
import org.deegree.services.wps.input.ProcessletInput;
import org.deegree.services.wps.output.ComplexOutput;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: goerke $
 * 
 * @version $Revision: 84 $, $Date: 2011-05-30 10:31:58 +0200 (Mo, 30 Mai 2011)
 */
public class FMEProcesslet implements Processlet {

    private static final Logger LOG = getLogger(FMEProcesslet.class);

    private final String fmeBaseUrl;

    private final String fmeRepo;

    private final String fmeWorkspace;

    private final String tokenUrl;

    private final Map<String, String> tokenMap;

    public FMEProcesslet(String baseUrl, String tokenUrl, Map<String, String> tokenMap, String repo, String workspace,
            FMEInvocationStrategy invocationStrategy) {
        this.fmeBaseUrl = baseUrl;
        this.tokenUrl = tokenUrl;
        this.tokenMap = tokenMap;
        this.fmeRepo = repo;
        this.fmeWorkspace = workspace;
    }

    public void destroy() {
        // nothing to destroy
    }

    public void init() {
        // nothing to init
    }

    public void process(ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info)
            throws ProcessletException {
        try {
            Map<String, String> kvpMap = buildInputMap(in);

            processViaDatastreaming( out, kvpMap, ( in != null ? in.getQueryMap() : null ) );
        } catch (Exception e) {
            LOG.error( "Exception", e );
            throw new ProcessletException(e.getMessage());
        }
    }

    private void processViaDatastreaming( ProcessletOutputs out, Map<String, String> kvpMap,
                                          Map<String, String> kvpQueryMap )
                            throws MalformedURLException,
                            IOException,
                            XMLStreamException {
        String url = this.fmeBaseUrl + "/fmeserver/streaming/fmedatastreaming/" + fmeRepo + "/"
                     + encodeReportedFmeUrisHack( fmeWorkspace );
        if ( kvpQueryMap != null && kvpQueryMap.size() > 0 ) {
            url = url + "?" + kvpQueryMap.entrySet().stream() //
                                         .map( e -> e.getKey() + "=" + e.getValue() ) //
                                         .collect( Collectors.joining( "&" ) );
        }

        Map<String, String> headers = new HashMap<>();
        headers.put( "Authorization", "fmetoken token=" + getSecurityToken() );

        LOG.debug("Sending {}", url);
        Pair<InputStream, HttpResponse> p = postFullResponse( STREAM, url, kvpMap, headers, 0 );

        InputStream is = p.first;
        ComplexOutput output = (ComplexOutput) out.getParameter("FMEResponse");
        Header contentType = p.second.getEntity().getContentType();
        LOG.debug("Content type: {}", contentType);
        if ( contentType.getValue() != null
             && ( contentType.getValue().contains( "xml" ) || contentType.getValue().contains( "html" ) ) ) {
            copyXmlResponse(is, output);
        } else {
            copyBinaryResponse(is, output);
        }
    }

    private void copyXmlResponse( InputStream is, ComplexOutput output )
                            throws XMLStreamException {
        try {
            XMLStreamWriter writer = output.getXMLStreamWriter();
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(is);
            while (reader.getEventType() != START_ELEMENT) {
                reader.next();
            }
            writeElement(writer, reader);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private void copyBinaryResponse( InputStream is, ComplexOutput output )
                            throws IOException {
        try {
            OutputStream os = output.getBinaryOutputStream();
            copyLarge(is, os);
        } finally {
            closeQuietly(is);
        }
    }

    private String getSecurityToken()
                            throws IOException {
        LOG.debug("Sending {}", tokenUrl);
        return post(UTF8STRING, tokenUrl, tokenMap, null, 60000).trim();
    }

    private Map<String, String> buildInputMap(ProcessletInputs in) {
        Map<String, String> map = new HashMap<String, String>();
        if (in != null) {
            for (ProcessletInput input : in.getParameters()) {
                if (input instanceof LiteralInput) {
                    map.put(input.getIdentifier().getCode(), ((LiteralInput) input).getValue());
                }
            }
        }
        return map;
    }

    private String encodeReportedFmeUrisHack(String uri) {
        // cannot use URLEncoder here, as this would encode slashes as well...
        return uri.replace(" ", "+");
    }
}
