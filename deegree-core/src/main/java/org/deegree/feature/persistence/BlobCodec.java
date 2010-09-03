//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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

package org.deegree.feature.persistence;

import static java.lang.Boolean.TRUE;
import static javax.xml.stream.XMLOutputFactory.IS_REPAIRING_NAMESPACES;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.cs.CRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLObject;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLReferenceResolver;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xml.fastinfoset.stax.StAXDocumentParser;
import com.sun.xml.fastinfoset.stax.StAXDocumentSerializer;

/**
 * Provides methods for storing / retrieving {@link GMLObject} instances in binary form, e.g. in BLOBs.
 * 
 * TODO improve namespace handling (should not be done for every single blob) TODO get FAST_INFOSET to work TODO
 * FAST_INFOSET with external vocabulary
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class BlobCodec {

    private static final Logger LOG = LoggerFactory.getLogger( BlobCodec.class );

    private static final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

    private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

    static {
        xmlOutputFactory.setProperty( IS_REPAIRING_NAMESPACES, TRUE );
    }

    private final GMLVersion gmlVersion;

    private final Compression compression;

    public enum Compression {
        NONE, GZIP, FAST_INFOSET
    }

    /**
     * Creates a new {@link BlobCodec} instance.
     * 
     * @param gmlVersion
     *            gml version to use, must not be <code>null</code>
     * @param compression
     *            compression method to use, must not be <code>null</code>
     */
    public BlobCodec( GMLVersion gmlVersion, Compression compression ) {
        this.gmlVersion = gmlVersion;
        this.compression = compression;
    }

    /**
     * Encodes the given {@link GMLObject} to the specified output stream.
     * 
     * @param object
     *            object to be encoded, must not be <code>null</code>
     * @param os
     *            output stream to write to, must not be <code>null</code>
     * @param crs
     * @throws FeatureStoreException
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws UnknownCRSException
     * @throws TransformationException
     * @throws IOException
     */
    public void encode( GMLObject object, OutputStream os, CRS crs )
                            throws FeatureStoreException, XMLStreamException, FactoryConfigurationError,
                            UnknownCRSException, TransformationException, IOException {

        long begin = System.currentTimeMillis();
        XMLStreamWriter xmlWriter = getXMLWriter( os );
        xmlWriter.setPrefix( CommonNamespaces.XSI_PREFIX, CommonNamespaces.XSINS );
        GMLStreamWriter gmlWriter = GMLOutputFactory.createGMLStreamWriter( gmlVersion, xmlWriter );
        gmlWriter.setOutputCRS( crs );
        gmlWriter.setLocalXLinkTemplate( "#{}" );
        gmlWriter.setXLinkDepth( 0 );
        gmlWriter.write( object );
        gmlWriter.close();
        if ( LOG.isDebugEnabled() ) {
            File file = File.createTempFile( "encoded-feature", ".tmp" );
            os = new FileOutputStream( file );
            xmlWriter = getXMLWriter( os );
            gmlWriter = GMLOutputFactory.createGMLStreamWriter( gmlVersion, xmlWriter );
            gmlWriter.setOutputCRS( crs );
            gmlWriter.setLocalXLinkTemplate( "#{}" );
            gmlWriter.setXLinkDepth( 0 );
            gmlWriter.write( object );
            gmlWriter.close();
            LOG.debug( "Wrote encoded feature to '" + file.getAbsolutePath() + "'" );
        }
        LOG.debug( "Encoding feature (compression: {}) took {} [ms]", compression, System.currentTimeMillis() - begin );
    }

    private XMLStreamWriter getXMLWriter( OutputStream os )
                            throws XMLStreamException, IOException {
        XMLStreamWriter writer = null;
        switch ( compression ) {
        case FAST_INFOSET: {
            StAXDocumentSerializer staxDocumentSerializer = new StAXDocumentSerializer();
            staxDocumentSerializer.setOutputStream( os );
            writer = staxDocumentSerializer;
            break;
        }
        case GZIP: {
            GZIPOutputStream gos = new GZIPOutputStream( os );
            writer = xmlOutputFactory.createXMLStreamWriter( gos, "UTF-8" );
            writer = new XMLStreamWriterWrapper( writer, gos );
            break;
        }
        case NONE: {
            writer = xmlOutputFactory.createXMLStreamWriter( os, "UTF-8" );
            break;
        }
        }
        return writer;
    }

    /**
     * Decodes the given {@link GMLObject} from the specified input stream.
     * 
     * @param is
     *            input stream to read from, must not be <code>null</code>
     * @param schema
     *            application schema, must not be <code>null</code>
     * @param crs
     * @param idResolver
     * @return
     * @throws XMLParsingException
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    public GMLObject decode( InputStream is, ApplicationSchema schema, CRS crs, GMLReferenceResolver idResolver )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException,
                            FactoryConfigurationError, IOException {

        long begin = System.currentTimeMillis();
        BufferedInputStream bis = new BufferedInputStream( is );
        XMLStreamReader xmlStream = getXMLReader( bis );
        GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( gmlVersion, xmlStream );
        gmlReader.setResolver( idResolver );
        gmlReader.setApplicationSchema( schema );
        gmlReader.setDefaultCRS( crs );
        Feature feature = gmlReader.readFeature();
        LOG.debug( "Decoding feature (compression: {}) took {} [ms]", compression, System.currentTimeMillis() - begin );
        return feature;
    }

    private XMLStreamReader getXMLReader( InputStream is )
                            throws XMLStreamException, IOException {
        XMLStreamReader reader = null;
        switch ( compression ) {
        case FAST_INFOSET: {
            reader = new StAXDocumentParser( is );
            break;
        }
        case GZIP: {
            GZIPInputStream gis = new GZIPInputStream( is );
            reader = xmlInputFactory.createXMLStreamReader( gis, "UTF-8" );
            break;
        }
        case NONE: {
            reader = xmlInputFactory.createXMLStreamReader( is, "UTF-8" );
            break;
        }
        }
        return reader;
    }
}

class XMLStreamWriterWrapper implements XMLStreamWriter {

    private final XMLStreamWriter xmlStreamWriter;

    private final OutputStream os;

    XMLStreamWriterWrapper( XMLStreamWriter xmlStream, OutputStream os ) {
        this.xmlStreamWriter = xmlStream;
        this.os = os;
    }

    /**
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#close()
     */
    public void close()
                            throws XMLStreamException {
        xmlStreamWriter.close();
        try {
            os.close();
        } catch ( IOException e ) {
            throw new XMLStreamException( e.getMessage() );
        }
    }

    /**
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#flush()
     */
    public void flush()
                            throws XMLStreamException {
        xmlStreamWriter.flush();
        try {
            os.flush();
        } catch ( IOException e ) {
            throw new XMLStreamException( e.getMessage() );
        }
    }

    /**
     * @return
     * @see javax.xml.stream.XMLStreamWriter#getNamespaceContext()
     */
    public NamespaceContext getNamespaceContext() {
        return xmlStreamWriter.getNamespaceContext();
    }

    /**
     * @param arg0
     * @return
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#getPrefix(java.lang.String)
     */
    public String getPrefix( String arg0 )
                            throws XMLStreamException {
        return xmlStreamWriter.getPrefix( arg0 );
    }

    /**
     * @param arg0
     * @return
     * @throws IllegalArgumentException
     * @see javax.xml.stream.XMLStreamWriter#getProperty(java.lang.String)
     */
    public Object getProperty( String arg0 )
                            throws IllegalArgumentException {
        return xmlStreamWriter.getProperty( arg0 );
    }

    /**
     * @param arg0
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#setDefaultNamespace(java.lang.String)
     */
    public void setDefaultNamespace( String arg0 )
                            throws XMLStreamException {
        xmlStreamWriter.setDefaultNamespace( arg0 );
    }

    /**
     * @param arg0
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#setNamespaceContext(javax.xml.namespace.NamespaceContext)
     */
    public void setNamespaceContext( NamespaceContext arg0 )
                            throws XMLStreamException {
        xmlStreamWriter.setNamespaceContext( arg0 );
    }

    /**
     * @param arg0
     * @param arg1
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#setPrefix(java.lang.String, java.lang.String)
     */
    public void setPrefix( String arg0, String arg1 )
                            throws XMLStreamException {
        xmlStreamWriter.setPrefix( arg0, arg1 );
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @param arg3
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#writeAttribute(java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    public void writeAttribute( String arg0, String arg1, String arg2, String arg3 )
                            throws XMLStreamException {
        xmlStreamWriter.writeAttribute( arg0, arg1, arg2, arg3 );
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#writeAttribute(java.lang.String, java.lang.String, java.lang.String)
     */
    public void writeAttribute( String arg0, String arg1, String arg2 )
                            throws XMLStreamException {
        xmlStreamWriter.writeAttribute( arg0, arg1, arg2 );
    }

    /**
     * @param arg0
     * @param arg1
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#writeAttribute(java.lang.String, java.lang.String)
     */
    public void writeAttribute( String arg0, String arg1 )
                            throws XMLStreamException {
        xmlStreamWriter.writeAttribute( arg0, arg1 );
    }

    /**
     * @param arg0
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#writeCData(java.lang.String)
     */
    public void writeCData( String arg0 )
                            throws XMLStreamException {
        xmlStreamWriter.writeCData( arg0 );
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#writeCharacters(char[], int, int)
     */
    public void writeCharacters( char[] arg0, int arg1, int arg2 )
                            throws XMLStreamException {
        xmlStreamWriter.writeCharacters( arg0, arg1, arg2 );
    }

    /**
     * @param arg0
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#writeCharacters(java.lang.String)
     */
    public void writeCharacters( String arg0 )
                            throws XMLStreamException {
        xmlStreamWriter.writeCharacters( arg0 );
    }

    /**
     * @param arg0
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#writeComment(java.lang.String)
     */
    public void writeComment( String arg0 )
                            throws XMLStreamException {
        xmlStreamWriter.writeComment( arg0 );
    }

    /**
     * @param arg0
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#writeDTD(java.lang.String)
     */
    public void writeDTD( String arg0 )
                            throws XMLStreamException {
        xmlStreamWriter.writeDTD( arg0 );
    }

    /**
     * @param arg0
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#writeDefaultNamespace(java.lang.String)
     */
    public void writeDefaultNamespace( String arg0 )
                            throws XMLStreamException {
        xmlStreamWriter.writeDefaultNamespace( arg0 );
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#writeEmptyElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void writeEmptyElement( String arg0, String arg1, String arg2 )
                            throws XMLStreamException {
        xmlStreamWriter.writeEmptyElement( arg0, arg1, arg2 );
    }

    /**
     * @param arg0
     * @param arg1
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#writeEmptyElement(java.lang.String, java.lang.String)
     */
    public void writeEmptyElement( String arg0, String arg1 )
                            throws XMLStreamException {
        xmlStreamWriter.writeEmptyElement( arg0, arg1 );
    }

    /**
     * @param arg0
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#writeEmptyElement(java.lang.String)
     */
    public void writeEmptyElement( String arg0 )
                            throws XMLStreamException {
        xmlStreamWriter.writeEmptyElement( arg0 );
    }

    /**
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#writeEndDocument()
     */
    public void writeEndDocument()
                            throws XMLStreamException {
        xmlStreamWriter.writeEndDocument();
    }

    /**
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#writeEndElement()
     */
    public void writeEndElement()
                            throws XMLStreamException {
        xmlStreamWriter.writeEndElement();
    }

    /**
     * @param arg0
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#writeEntityRef(java.lang.String)
     */
    public void writeEntityRef( String arg0 )
                            throws XMLStreamException {
        xmlStreamWriter.writeEntityRef( arg0 );
    }

    /**
     * @param arg0
     * @param arg1
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#writeNamespace(java.lang.String, java.lang.String)
     */
    public void writeNamespace( String arg0, String arg1 )
                            throws XMLStreamException {
        xmlStreamWriter.writeNamespace( arg0, arg1 );
    }

    /**
     * @param arg0
     * @param arg1
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#writeProcessingInstruction(java.lang.String, java.lang.String)
     */
    public void writeProcessingInstruction( String arg0, String arg1 )
                            throws XMLStreamException {
        xmlStreamWriter.writeProcessingInstruction( arg0, arg1 );
    }

    /**
     * @param arg0
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#writeProcessingInstruction(java.lang.String)
     */
    public void writeProcessingInstruction( String arg0 )
                            throws XMLStreamException {
        xmlStreamWriter.writeProcessingInstruction( arg0 );
    }

    /**
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#writeStartDocument()
     */
    public void writeStartDocument()
                            throws XMLStreamException {
        xmlStreamWriter.writeStartDocument();
    }

    /**
     * @param arg0
     * @param arg1
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#writeStartDocument(java.lang.String, java.lang.String)
     */
    public void writeStartDocument( String arg0, String arg1 )
                            throws XMLStreamException {
        xmlStreamWriter.writeStartDocument( arg0, arg1 );
    }

    /**
     * @param arg0
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#writeStartDocument(java.lang.String)
     */
    public void writeStartDocument( String arg0 )
                            throws XMLStreamException {
        xmlStreamWriter.writeStartDocument( arg0 );
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#writeStartElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void writeStartElement( String arg0, String arg1, String arg2 )
                            throws XMLStreamException {
        xmlStreamWriter.writeStartElement( arg0, arg1, arg2 );
    }

    /**
     * @param arg0
     * @param arg1
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#writeStartElement(java.lang.String, java.lang.String)
     */
    public void writeStartElement( String arg0, String arg1 )
                            throws XMLStreamException {
        xmlStreamWriter.writeStartElement( arg0, arg1 );
    }

    /**
     * @param arg0
     * @throws XMLStreamException
     * @see javax.xml.stream.XMLStreamWriter#writeStartElement(java.lang.String)
     */
    public void writeStartElement( String arg0 )
                            throws XMLStreamException {
        xmlStreamWriter.writeStartElement( arg0 );
    }
}
