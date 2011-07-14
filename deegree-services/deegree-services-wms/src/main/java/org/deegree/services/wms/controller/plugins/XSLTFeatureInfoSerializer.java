package org.deegree.services.wms.controller.plugins;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.URL;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.slf4j.Logger;

public class XSLTFeatureInfoSerializer implements FeatureInfoSerializer {

    private static final Logger LOG = getLogger( FeatureInfoSerializer.class );

    private final GMLVersion gmlVersion;

    private final URL xslt;

    private final DeegreeWorkspace workspace;

    public XSLTFeatureInfoSerializer( GMLVersion version, URL xslt, DeegreeWorkspace workspace ) {
        this.gmlVersion = version;
        this.xslt = xslt;
        this.workspace = workspace;
    }

    @Override
    public void serialize( ApplicationSchema schema, FeatureCollection col, OutputStream outputStream ) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( workspace.getModuleClassLoader() );
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            XMLStreamWriter out = XMLOutputFactory.newInstance().createXMLStreamWriter( bos );
            if ( LOG.isDebugEnabled() ) {
                out = new IndentingXMLStreamWriter( out );
            }
            GMLStreamWriter writer = GMLOutputFactory.createGMLStreamWriter( gmlVersion, out );
            writer.setNamespaceBindings( schema.getNamespaceBindings() );
            writer.write( col );
            writer.close();
            bos.flush();
            bos.close();
            if ( LOG.isDebugEnabled() ) {
                LOG.debug( "GML before XSLT:\n{}", new String( bos.toByteArray(), "UTF-8" ) );
            }
            Source source = new StreamSource( new ByteArrayInputStream( bos.toByteArray() ) );
            Source xslt = new StreamSource( new File( this.xslt.toURI() ) );
            TransformerFactory fac = TransformerFactory.newInstance();
            Transformer t = fac.newTransformer( xslt );
            Result result = new StreamResult( outputStream );
            t.transform( source, result );
        } catch ( Throwable e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            Thread.currentThread().setContextClassLoader( loader );
        }
    }

}
