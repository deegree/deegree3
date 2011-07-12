package org.deegree.services.wms.controller.plugins;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;

public class XSLTFeatureInfoSerializer implements FeatureInfoSerializer {

    private final GMLVersion gmlVersion;

    private final String xslt;

    public XSLTFeatureInfoSerializer( GMLVersion version, String xslt ) {
        this.gmlVersion = version;
        this.xslt = xslt;
    }

    @Override
    public void serialize( ApplicationSchema schema, FeatureCollection col, OutputStream outputStream ) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            XMLStreamWriter out = XMLOutputFactory.newInstance().createXMLStreamWriter( bos );
            GMLStreamWriter writer = GMLOutputFactory.createGMLStreamWriter( gmlVersion, out );
            writer.setNamespaceBindings( schema.getNamespaceBindings() );
            writer.write( col );
            bos.close();
            Source source = new StreamSource( new ByteArrayInputStream( bos.toByteArray() ) );
            Source xslt = new StreamSource( this.xslt );
            Transformer t = TransformerFactory.newInstance().newTransformer( xslt );
            Result result = new StreamResult( outputStream );
            t.transform( source, result );
        } catch ( Throwable e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
