package org.deegree.services.controller.utils;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.featureinfo.FeatureInfoContext;

public class StandardFeatureInfoContext implements FeatureInfoContext {

    final HttpResponseBuffer response;

    OutputStream outputStream = null;

    XMLStreamWriter xmlWriter = null;

    public StandardFeatureInfoContext( HttpResponseBuffer response ) {
        this.response = response;
    }

    @Override
    public OutputStream getOutputStream()
                            throws IOException {

        if ( outputStream != null ) {
            return outputStream;
        }

        if ( xmlWriter != null ) {
            throw new IllegalStateException( "getXmlWriter() already called for FeatureInfoContext" );
        }

        return outputStream = response.getOutputStream();
    }

    @Override
    public XMLStreamWriter getXmlWriter()
                            throws IOException, XMLStreamException {

        if ( xmlWriter != null ) {
            return xmlWriter;
        }

        if ( outputStream != null ) {
            throw new IllegalStateException( "getOutputStream() already called for FeatureInfoContext" );
        }

        return xmlWriter = response.getXMLWriter();
    }
}
