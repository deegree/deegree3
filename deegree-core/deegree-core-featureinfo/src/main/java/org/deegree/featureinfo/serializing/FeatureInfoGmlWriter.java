//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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

package org.deegree.featureinfo.serializing;

import static javax.xml.XMLConstants.NULL_NS_URI;
import static org.deegree.commons.xml.CommonNamespaces.GML_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.commons.xml.CommonNamespaces.XSI_PREFIX;
import static org.deegree.gml.GMLVersion.GML_2;

import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.geometry.Envelope;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;

/**
 * Writes GML <code>GetFeatureInfo</code> responses. TODO: adapt this to the serializer interface.
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FeatureInfoGmlWriter {

    private static final String WFS_NS = "http://www.opengis.net/wfs";

    private final String gmlNull;

    private final String gmlNs;

    private final QName fidAttr;

    /**
     * Creates a new {@link FeatureInfoGmlWriter} instance for the specified GML version.
     * 
     * @param version
     *            gml version, must not be <code>null</code>
     */
    public FeatureInfoGmlWriter( GMLVersion version ) {
        gmlNs = version.getNamespace();
        if ( !version.equals( GML_2 ) ) {
            fidAttr = new QName( gmlNs, "id" );
            gmlNull = "Null";
        } else {
            fidAttr = new QName( NULL_NS_URI, "fid" );
            gmlNull = "null";
        }
    }

    /**
     * Writes the given feature collection as a <code>wfs:FeatureCollection</code> element.
     * 
     * @param fc
     *            feature collection, must not be <code>null</code>
     * @param gmlWriter
     *            gml writer to use for exporting features, must not be <code>null</code>
     * @param noNamespaceSchemaLocation
     * @param bindings
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void export( FeatureCollection fc, GMLStreamWriter gmlWriter, String noNamespaceSchemaLocation,
                 Map<String, String> bindings )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        XMLStreamWriter writer = gmlWriter.getXMLStream();

        writer.setDefaultNamespace( WFS_NS );
        writer.writeStartElement( WFS_NS, "FeatureCollection" );
        writer.writeDefaultNamespace( WFS_NS );
        writer.writeNamespace( XSI_PREFIX, XSINS );
        writer.writeNamespace( GML_PREFIX, gmlNs );

        if ( fc.getId() != null ) {
            if ( fidAttr.getNamespaceURI() == NULL_NS_URI ) {
                writer.writeAttribute( fidAttr.getLocalPart(), fc.getId() );
            } else {
                writer.writeAttribute( fidAttr.getNamespaceURI(), fidAttr.getLocalPart(), fc.getId() );
            }
        }

        if ( noNamespaceSchemaLocation != null ) {
            writer.writeAttribute( XSINS, "noNamespaceSchemaLocation", noNamespaceSchemaLocation );
        }
        if ( bindings != null && !bindings.isEmpty() ) {
            String locs = null;
            for ( Entry<String, String> e : bindings.entrySet() ) {
                if ( locs == null ) {
                    locs = "";
                } else {
                    locs += " ";
                }
                locs += e.getKey() + " " + e.getValue();
            }
            writer.writeAttribute( XSINS, "schemaLocation", locs );
        }

        exportBoundedBy( gmlWriter, writer, fc.getEnvelope(), true );

        for ( Feature f : fc ) {
            writer.writeStartElement( gmlNs, "featureMember" );
            gmlWriter.write( f );
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private void exportBoundedBy( GMLStreamWriter gmlWriter, XMLStreamWriter writer, Envelope env,
                                  boolean indicateMissing )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        if ( env != null || indicateMissing ) {
            writer.writeStartElement( gmlNs, "boundedBy" );
            if ( env != null ) {
                gmlWriter.getGeometryWriter().exportEnvelope( env );
            } else {
                writer.writeStartElement( gmlNs, gmlNull );
                writer.writeCharacters( "missing" );
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
    }

}
