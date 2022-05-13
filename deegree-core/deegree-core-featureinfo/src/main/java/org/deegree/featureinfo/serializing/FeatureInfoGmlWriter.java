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

import static org.deegree.commons.xml.CommonNamespaces.GML_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.commons.xml.CommonNamespaces.XSI_PREFIX;
import static org.deegree.gml.GMLVersion.GML_2;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.featureinfo.FeatureInfoContext;
import org.deegree.featureinfo.FeatureInfoParams;
import org.deegree.geometry.Envelope;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.slf4j.Logger;

/**
 * Writes GML <code>GetFeatureInfo</code> responses.
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:reijer.copier@idgis.nl">Reijer Copier</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FeatureInfoGmlWriter implements FeatureInfoSerializer {

    private static final Logger LOG = getLogger( FeatureInfoGmlWriter.class );

    private static final String WFS_NS = "http://www.opengis.net/wfs";

    /* ; */

    /**
     * Creates a new {@link FeatureInfoGmlWriter} instance for the specified GML version.
     * 
     * @param version
     *            gml version, must not be <code>null</code>
     */
    /*
     * public FeatureInfoGmlWriter( GMLVersion version ) { gmlNs = version.getNamespace(); if ( !version.equals( GML_2 )
     * ) { fidAttr = new QName( gmlNs, "id" ); gmlNull = "Null"; } else { fidAttr = new QName( "", "fid" ); gmlNull =
     * "null"; } }
     */

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
    private void export( GMLVersion version, FeatureCollection fc, GMLStreamWriter gmlWriter,
                         String noNamespaceSchemaLocation, Map<String, String> bindings )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        QName fidAttr;
        String gmlNull;
        String gmlNs = version.getNamespace();
        if ( !version.equals( GML_2 ) ) {
            fidAttr = new QName( gmlNs, "id" );
            gmlNull = "Null";
        } else {
            fidAttr = new QName( "", "fid" );
            gmlNull = "null";
        }

        XMLStreamWriter writer = gmlWriter.getXMLStream();

        writer.setDefaultNamespace( WFS_NS );
        writer.writeStartElement( WFS_NS, "FeatureCollection" );
        writer.writeDefaultNamespace( WFS_NS );
        writer.writeNamespace( XSI_PREFIX, XSINS );
        writer.writeNamespace( GML_PREFIX, gmlNs );

        if ( fc.getId() != null ) {
            if ( fidAttr.getNamespaceURI() == "" ) {
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

        Envelope env = fc.getEnvelope();
        writer.writeStartElement( gmlNs, "boundedBy" );
        if ( env != null ) {
            gmlWriter.getGeometryWriter().exportEnvelope( env );
        } else {
            writer.writeStartElement( gmlNs, gmlNull );
            writer.writeCharacters( "missing" );
            writer.writeEndElement();
        }
        writer.writeEndElement();

        for ( Feature f : fc ) {
            writer.writeStartElement( gmlNs, "featureMember" );
            gmlWriter.write( f );
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private String determineNamespace( FeatureInfoParams params ) {
        String ns = params.getFeatureType() == null ? null : params.getFeatureType().getName().getNamespaceURI();
        if ( ns != null && ns.isEmpty() ) {
            ns = null;
        }
        return ns;
    }

    @Override
    public void serialize( FeatureInfoParams params, FeatureInfoContext context )
                            throws IOException, XMLStreamException {

        XMLStreamWriter xmlWriter = context.getXmlWriter();

        try {
            // for more than just quick 'hacky' schemaLocation attributes one should use a proper WFS
            HashMap<String, String> bindings = new HashMap<String, String>();
            String ns = determineNamespace( params );
            if ( ns != null ) {
                bindings.put( ns, params.getSchemaLocation() );
                if ( !params.getNsBindings().containsValue( ns ) ) {
                    params.getNsBindings().put( "app", ns );
                }
            }
            if ( !params.getNsBindings().containsKey( "app" ) ) {
                params.getNsBindings().put( "app", "http://www.deegree.org/app" );
            }
            bindings.put( "http://www.opengis.net/wfs", "http://schemas.opengis.net/wfs/1.0.0/WFS-basic.xsd" );
            String format = params.getFormat();

            GMLVersion gmlVersion = GMLVersion.GML_2;
            if ( format.endsWith( "3.0" ) || format.endsWith( "3.0.1" ) ) {
                gmlVersion = GMLVersion.GML_30;
            }
            if ( format.endsWith( "3.1" ) || format.endsWith( "3.1.1" ) ) {
                gmlVersion = GMLVersion.GML_31;
            }
            if ( format.endsWith( "3.2" ) || format.endsWith( "3.2.1" ) || format.endsWith( "3.2.2" ) ) {
                gmlVersion = GMLVersion.GML_32;
            }

            GMLStreamWriter gmlWriter = GMLOutputFactory.createGMLStreamWriter( gmlVersion, xmlWriter );
            gmlWriter.setOutputCrs( params.getCrs() );
            gmlWriter.setNamespaceBindings( params.getNsBindings() );
            gmlWriter.setExportGeometries( params.isWithGeometries() );
            export( gmlVersion, params.getFeatureCollection(), gmlWriter, ns == null ? params.getSchemaLocation()
                                                                                    : null, bindings );
        } catch ( Throwable e ) {
            LOG.warn( "Error when writing GetFeatureInfo GML response '{}'.", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        }
    }
}
