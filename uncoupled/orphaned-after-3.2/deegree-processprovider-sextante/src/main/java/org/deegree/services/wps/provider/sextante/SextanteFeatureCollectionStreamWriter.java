/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.services.wps.provider.sextante;

import java.util.HashMap;
import java.util.Set;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.output.ComplexOutput;

/**
 * This class can be use to write a {@link FeatureCollection}, {@link Feature} for {@link Feature} on a Stream.
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * 
 */
public class SextanteFeatureCollectionStreamWriter {

    private static int activeWriterCounter; // number of active writers

    private int writerId; // id of this writer

    private int featureCount; // number of written features of this writer

    private final XMLStreamWriter sw;

    private final GMLStreamWriter gmlWriter;

    private final GMLVersion gmlVersion;

    private boolean firstFeature = true; // true if first feature isn't written

    private String gmlNSPrefix = "gml"; // default gml namespace prefix

    private String gmlNS = "http://www.opengis.net/gml"; // default gml namespace

    public SextanteFeatureCollectionStreamWriter( String identifier, ProcessletOutputs out ) throws XMLStreamException {
        activeWriterCounter++;
        writerId = activeWriterCounter;

        ComplexOutput gmlOutput = (ComplexOutput) out.getParameter( identifier );
        this.sw = gmlOutput.getXMLStreamWriter();
        this.gmlVersion = FormatHelper.determineGMLVersion( gmlOutput );
        this.gmlWriter = GMLOutputFactory.createGMLStreamWriter( gmlVersion, sw );
    }

    /**
     * Writes the start element of the feature collection.
     * 
     * @param f
     *            {@link Feature}.
     * @throws XMLStreamException
     */
    private void writeStartElement( Feature f )
                            throws XMLStreamException {

        // determine and set namespaces
        HashMap<String, String> namespaces = SextanteProcesslet.determinePropertyNamespaces( f );
        Set<String> namespaceURIs = namespaces.keySet();
        for ( String uri : namespaceURIs ) {
            String prefix = namespaces.get( uri );
            if ( prefix.equals( gmlNSPrefix ) )
                gmlNS = uri;
            sw.setPrefix( prefix, uri );
        }

        // write start element of FeatureCollection
        sw.writeStartElement( gmlNSPrefix, "FeatureCollection", gmlNS );

        // set the correct id for feature collection
        if ( gmlVersion.equals( GMLVersion.GML_2 ) ) {
            sw.writeAttribute( "fid", "SextanteFeatureCollection" + writerId );
        } else {
            sw.writeAttribute( "gml:id", "SextanteFeatureCollection" + writerId );
        }

    }

    /**
     * Writes a {@link Feature} into a {@link FeatureCollection} on stream.
     * 
     * @param f
     *            {@link Feature}.
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void writeFeature( Feature f )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        if ( f != null ) {

            // LOG.info( "WRITE FEATURE" );

            // write feature collection start element
            if ( firstFeature ) {
                writeStartElement( f );
                firstFeature = false;
            }

            // write start element of featureMember
            sw.writeStartElement( gmlNSPrefix, "featureMember", gmlNS );

            // write Feature
            featureCount++;
            f.setId( "SextanteFeature" + writerId + featureCount );
            gmlWriter.write( f );

            // write end element of featureMember
            sw.writeEndElement();

        }

    }

    /**
     * Closes the feature collection.
     * 
     * @throws XMLStreamException
     */
    public void close()
                            throws XMLStreamException {

        // write end element of FeatureCollection
        sw.writeEndElement();

        // Remove active writer
        activeWriterCounter--;
    }
}
