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
import static org.deegree.commons.xml.stax.XMLStreamUtils.copy;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipStartDocument;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.net.HttpUtils;
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

    private final FMEInvocationStrategy invocationStrategy;

    public FMEProcesslet(String baseUrl, String tokenUrl, Map<String, String> tokenMap, String repo, String workspace,
            FMEInvocationStrategy invocationStrategy) {
        this.fmeBaseUrl = baseUrl;
        this.tokenUrl = tokenUrl;
        this.tokenMap = tokenMap;
        this.fmeRepo = repo;
        this.fmeWorkspace = workspace;
        this.invocationStrategy = invocationStrategy;
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
            String token = getSecurityToken();
            kvpMap.put("token", token);

            if (invocationStrategy instanceof FMEStreamingServiceInvocationStrategy) {
                processViaStreamingService(out, kvpMap, (FMEStreamingServiceInvocationStrategy) invocationStrategy);
            } else {
                processViaJobSubmitter(out, kvpMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ProcessletException(e.getMessage());
        }
    }

    private void processViaJobSubmitter(ProcessletOutputs out, Map<String, String> kvpMap)
            throws MalformedURLException, IOException, XMLStreamException {
        String url = this.fmeBaseUrl + "/fmerest/repositories/" + fmeRepo + "/"
                + encodeReportedFmeUrisHack(fmeWorkspace) + "/run.xml";
        LOG.debug("Sending {}", url);
        Pair<InputStream, HttpResponse> p = postFullResponse(STREAM, url, kvpMap, null, 0);
        InputStream is = p.first;
        ComplexOutput output = (ComplexOutput) out.getParameter("FMEResponse");
        Header contentType = p.second.getEntity().getContentType();
        LOG.debug("Content type: {}", contentType);
        if (contentType.getValue() != null && contentType.getValue().contains("xml")) {
            copyXmlResponse(is, output);
        } else {
            copyBinaryResponse(is, output);
        }
    }

    private void copyXmlResponse(InputStream is, ComplexOutput output) throws XMLStreamException {
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

    private void copyBinaryResponse(InputStream is, ComplexOutput output) throws IOException {
        try {
            OutputStream os = output.getBinaryOutputStream();
            copyLarge(is, os);
        } finally {
            closeQuietly(is);
        }
    }

    private String getSecurityToken() throws IOException {
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

    private void processViaStreamingService(ProcessletOutputs out, Map<String, String> map,
            FMEStreamingServiceInvocationStrategy invocationStrategy) {

        map.put("opt_responseformat", "xml");
        String url = this.fmeBaseUrl + "/fmedatastreaming/" + fmeRepo + "/" + encodeReportedFmeUrisHack(fmeWorkspace);
        LOG.debug("Sending {}", url);
        String format = invocationStrategy.getOutputFormat();
        try {
            ComplexOutput singleOutput = (ComplexOutput) out.getParameter("FMEResponse");
            if (singleOutput != null) {
                InputStream is = null;
                try {
                    is = HttpUtils.post(HttpUtils.STREAM, url, map, null, 0);
                    String mimeType = singleOutput.getRequestedMimeType();
                    if (mimeType != null && mimeType.contains("xml")) {
                        XMLStreamWriter xmlWriter = null;
                        XMLStreamReader xmlReader = null;
                        try {
                            xmlWriter = singleOutput.getXMLStreamWriter();
                            xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(is);
                            skipStartDocument(xmlReader);
                            copy(xmlWriter, xmlReader);
                        } finally {
                            if (xmlWriter != null) {
                                xmlWriter.close();
                            }
                            if (xmlReader != null) {
                                xmlReader.close();
                            }
                        }
                    } else {
                        OutputStream os = null;
                        try {
                            IOUtils.copy(is, os = singleOutput.getBinaryOutputStream());
                        } finally {
                            closeQuietly(os);
                        }
                    }
                } finally {
                    closeQuietly(is);
                }
            } else if (format.equals("GML") || format.equals("GML2")) {
                copyGmlResponse(out, map, url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyGmlResponse(ProcessletOutputs out, Map<String, String> map, String url) throws IOException {
        ComplexOutput gmlOutput = (ComplexOutput) out.getParameter("GML");
        ComplexOutput schemaOutput = (ComplexOutput) out.getParameter("APPSCHEMA");
        InputStream is = null;
        OutputStream osGml = gmlOutput.getBinaryOutputStream();
        OutputStream osXsd = schemaOutput.getBinaryOutputStream();
        try {
            Pair<InputStream, HttpResponse> p = HttpUtils.postFullResponse(HttpUtils.STREAM, url, map, null, 0);
            is = p.first;
            Header contentType = p.second.getEntity().getContentType();
            LOG.debug("Content type: {}", contentType);
            if (contentType != null && !contentType.getValue().toLowerCase().startsWith("application/zip")) {
                IOUtils.copy(is, osGml);
            } else {
                ZipInputStream zin = new ZipInputStream(is);
                ZipEntry entry = zin.getNextEntry();
                if (entry == null) {
                    IOUtils.copy(is, osGml);
                } else {
                    while (entry.isDirectory()) {
                        zin.closeEntry();
                        entry = zin.getNextEntry();
                    }
                    if (entry.getName().toLowerCase().endsWith(".gml")) {
                        IOUtils.copy(zin, osGml);
                    } else {
                        IOUtils.copy(zin, osXsd);
                    }
                    zin.closeEntry();
                    entry = zin.getNextEntry();
                    if (entry.getName().toLowerCase().endsWith(".gml")) {
                        IOUtils.copy(zin, osGml);
                    } else {
                        IOUtils.copy(zin, osXsd);
                    }
                }
            }
        } finally {
            closeQuietly(is);
            closeQuietly(osGml);
            closeQuietly(osXsd);
        }
    }

    private String encodeReportedFmeUrisHack(String uri) {
        // cannot use URLEncoder here, as this would encode slashes as well...
        return uri.replace(" ", "+");
    }
}
