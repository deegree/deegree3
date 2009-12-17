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
import static org.deegree.gml.GMLVersion.GML_31;

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
import org.deegree.crs.CRS;
import org.deegree.crs.exceptions.TransformationException;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLReferenceResolver;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides methods for storing / retrieving single {@link Feature} instances in binary form.
 * 
 * TODO implement efficient binary format (instead of GML)
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FeatureCoder {

    private static final Logger LOG = LoggerFactory.getLogger( FeatureCoder.class );

    private static final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

    private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

    static {
        xmlOutputFactory.setProperty( IS_REPAIRING_NAMESPACES, TRUE );
    }

    /**
     * @param feature
     * @param os
     * @param crs
     * @throws FeatureStoreException
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws UnknownCRSException
     * @throws TransformationException
     * @throws IOException
     */
    public static void encode( Feature feature, OutputStream os, CRS crs )
                            throws FeatureStoreException, XMLStreamException, FactoryConfigurationError,
                            UnknownCRSException, TransformationException, IOException {

        long begin = System.currentTimeMillis();
        // GZIPOutputStream gos = new GZIPOutputStream( os );
        XMLStreamWriter xmlWriter = xmlOutputFactory.createXMLStreamWriter( os, "UTF-8" );
        xmlWriter.setPrefix( XMLConstants.DEFAULT_NS_PREFIX, feature.getName().getNamespaceURI() );
        GMLStreamWriter gmlWriter = GMLOutputFactory.createGMLStreamWriter( GML_31, xmlWriter );
        gmlWriter.setOutputCRS( crs );
        gmlWriter.setLocalXLinkTemplate( "#{}" );
        gmlWriter.setXLinkExpansion( 0 );
        xmlWriter.setPrefix( "", feature.getName().getNamespaceURI() );
        gmlWriter.write( feature );
        gmlWriter.close();
        // gos.close();
        long elapsed = System.currentTimeMillis() - begin;
        LOG.debug( "Encoding feature: " + elapsed + " [ms]" );
    }

    /**
     * @param is
     * @param schema
     * @param crs
     * @param idResolver
     * @return
     * @throws XMLParsingException
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    public static Feature decode( InputStream is, ApplicationSchema schema, CRS crs, GMLReferenceResolver idResolver )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException,
                            FactoryConfigurationError, IOException {

        long begin = System.currentTimeMillis();
        BufferedInputStream bis = new BufferedInputStream( is );
        XMLStreamReader xmlStream = xmlInputFactory.createXMLStreamReader( bis, "UTF-8" );
        GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_31, xmlStream );
        gmlReader.setResolver( idResolver );
        gmlReader.setApplicationSchema( schema );
        gmlReader.setDefaultCRS( crs );
        Feature feature = gmlReader.readFeature();
        long elapsed = System.currentTimeMillis() - begin;
        LOG.debug( "Decoding feature: " + elapsed + " [ms]" );
        return feature;
    }
}
