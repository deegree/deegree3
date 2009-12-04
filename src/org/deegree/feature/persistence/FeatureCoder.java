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

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.crs.CRS;
import org.deegree.crs.exceptions.TransformationException;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLObjectResolver;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.GML3FeatureEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FeatureCoder {

    private static final Logger LOG = LoggerFactory.getLogger( FeatureCoder.class );

    private static final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

    static {
        xmlOutputFactory.setProperty( IS_REPAIRING_NAMESPACES, TRUE );
    }

    public static void encode( Feature feature, OutputStream os, Logger log )
                            throws FeatureStoreException, XMLStreamException, FactoryConfigurationError,
                            UnknownCRSException, TransformationException, IOException {

        xmlOutputFactory.setProperty( IS_REPAIRING_NAMESPACES, TRUE );
        XMLStreamWriter xmlWriter = xmlOutputFactory.createXMLStreamWriter( os, "UTF-8" );
        // TODO
        xmlWriter.setPrefix( "gml", CommonNamespaces.GMLNS );
        xmlWriter.setPrefix( "xlink", CommonNamespaces.XLNNS );
        xmlWriter.setPrefix( "xplan", feature.getName().getNamespaceURI() );
        GML3FeatureEncoder encoder = new GML3FeatureEncoder( GMLVersion.GML_31, xmlWriter, new CRS( "EPSG:31466" ),
                                                             "#{}", null, 0, -1, false );
        long begin = System.currentTimeMillis();
        encoder.export( feature );
        xmlWriter.close();
        long elapsed = System.currentTimeMillis() - begin;
        log.debug( "Encoding to XML: " + elapsed );

        // if (log.isDebugEnabled()) {
        // File tmpFile = File.createTempFile( "encoded_feature", ".xml" );
        // LOG.debug ("Writing encoded feature to '" + tmpFile + "'.");
        // xmlWriter = new FormattingXMLStreamWriter( xmlOutputFactory.createXMLStreamWriter( new FileWriter( tmpFile )
        // ) );
        // xmlWriter.setPrefix( "gml", CommonNamespaces.GMLNS );
        // xmlWriter.setPrefix( "xlink", CommonNamespaces.XLNNS );
        // xmlWriter.setPrefix( "xplan", feature.getName().getNamespaceURI() );
        // encoder = new GML311FeatureEncoder( xmlWriter, new CRS( "EPSG:31466" ), "#{}", null, 1, -1, false );
        // encoder.export( feature );
        // xmlWriter.close();
        // }
    }

    public static Feature decode( InputStream is, ApplicationSchema schema, CRS crs,
                                  GMLObjectResolver featureStoreGMLIdResolver )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException {

        BufferedInputStream bis = new BufferedInputStream( is );
        XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( bis, "UTF-8" );
        GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_31, xmlStream );
        gmlReader.setApplicationSchema( schema );
        gmlReader.setDefaultCRS( crs );
        return gmlReader.readFeature();
    }
}
