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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.XMLConstants;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

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

/**
 * Provides methods for storing / retrieving {@link GMLObject} instances in binary form, e.g. in BLOBs.
 * 
 * TODO implement efficient binary format (instead of GML)
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FeatureCodec {

    private static final Logger LOG = LoggerFactory.getLogger( FeatureCodec.class );

    private static final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

    private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

    static {
        xmlOutputFactory.setProperty( IS_REPAIRING_NAMESPACES, TRUE );
    }

    private GMLVersion gmlVersion;

    private Compression compression;

    public enum Compression {
        NONE, GZIP, FAST_INFOSET
    }

    public FeatureCodec( GMLVersion gmlVersion, Compression compression ) {
        this.gmlVersion = gmlVersion;
        this.compression = compression;
    }

    /**
     * Encodes the given {@link Feature} to the specified output stream.
     * 
     * @param feature
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
    public void encode( Feature feature, OutputStream os, CRS crs )
                            throws FeatureStoreException, XMLStreamException, FactoryConfigurationError,
                            UnknownCRSException, TransformationException, IOException {

        long begin = System.currentTimeMillis();
        // GZIPOutputStream gos = new GZIPOutputStream( os );
        XMLStreamWriter xmlWriter = xmlOutputFactory.createXMLStreamWriter( os, "UTF-8" );
        xmlWriter.setPrefix( XMLConstants.DEFAULT_NS_PREFIX, feature.getName().getNamespaceURI() );
        GMLStreamWriter gmlWriter = GMLOutputFactory.createGMLStreamWriter( gmlVersion, xmlWriter );
        gmlWriter.setOutputCRS( crs );
        gmlWriter.setLocalXLinkTemplate( "#{}" );
        gmlWriter.setXLinkDepth( 0 );
        xmlWriter.setPrefix( "", feature.getName().getNamespaceURI() );
        gmlWriter.write( feature );
        gmlWriter.close();
        // gos.close();
        LOG.debug( "Encoding feature took {} [ms]", System.currentTimeMillis() - begin );
    }

    /**
     * Decodes the given {@link Feature} from the specified input stream.
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
    public Feature decode( InputStream is, ApplicationSchema schema, CRS crs, GMLReferenceResolver idResolver )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException,
                            FactoryConfigurationError, IOException {

        long begin = System.currentTimeMillis();
        BufferedInputStream bis = new BufferedInputStream( is );
        XMLStreamReader xmlStream = xmlInputFactory.createXMLStreamReader( bis, "UTF-8" );
        GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( gmlVersion, xmlStream );
        gmlReader.setResolver( idResolver );
        gmlReader.setApplicationSchema( schema );
        gmlReader.setDefaultCRS( crs );
        Feature feature = gmlReader.readFeature();
        LOG.debug( "Decoding feature took {} [ms]", System.currentTimeMillis() - begin );
        return feature;
    }
}
