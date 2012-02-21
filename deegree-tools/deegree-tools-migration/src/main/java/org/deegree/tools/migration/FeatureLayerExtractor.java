//$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.tools.migration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.stream.StreamSource;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceState;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.layer.persistence.LayerStoreManager;
import org.deegree.services.controller.WebServicesConfiguration;
import org.deegree.services.wms.controller.WMSController;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class FeatureLayerExtractor {

    private DeegreeWorkspace workspace;

    public FeatureLayerExtractor( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    public void extract()
                            throws XMLStreamException {
        Pattern stylepattern = Pattern.compile( "\\.\\./styles/([A-Za-z_0-9]+)\\.xml" );
        LayerStoreManager lmgr = workspace.getSubsystemManager( LayerStoreManager.class );
        WebServicesConfiguration mgr = workspace.getSubsystemManager( WebServicesConfiguration.class );
        ResourceState<?>[] states = mgr.getStates();

        XMLInputFactory infac = XMLInputFactory.newInstance();
        XMLOutputFactory outfac = XMLOutputFactory.newInstance();

        Map<String, List<FeatureLayer>> map = new HashMap<String, List<FeatureLayer>>();

        int logicalLayerCount = 0;

        String crs = null;

        for ( ResourceState<?> s : states ) {
            if ( s.getResource() instanceof WMSController ) {
                XMLStreamReader reader = infac.createXMLStreamReader( new StreamSource( s.getConfigLocation() ) );
                reader.next();

                while ( reader.hasNext() ) {
                    if ( crs == null && reader.isStartElement() && reader.getLocalName().equals( "CRS" ) ) {
                        crs = reader.getElementText();
                    }

                    if ( reader.isStartElement()
                         && ( reader.getLocalName().equals( "RequestableLayer" ) || reader.getLocalName().equals( "LogicalLayer" ) ) ) {

                        FeatureLayer l = new FeatureLayer();

                        if ( reader.getLocalName().equals( "RequestableLayer" ) ) {
                            reader.nextTag();
                            l.name = reader.getElementText();
                            reader.nextTag();
                            l.title = reader.getElementText();
                        } else {
                            l.name = "LogicalLayer_" + ++logicalLayerCount;
                            l.title = l.name;
                        }

                        reader.nextTag();
                        if ( reader.getLocalName().equals( "ScaleDenominators" ) ) {
                            if ( reader.getAttributeValue( null, "min" ) != null ) {
                                l.minscale = Double.parseDouble( reader.getAttributeValue( null, "min" ) );
                            }
                            if ( reader.getAttributeValue( null, "max" ) != null ) {
                                l.maxscale = Double.parseDouble( reader.getAttributeValue( null, "max" ) );
                            }
                            reader.nextTag();
                            reader.nextTag();
                        }

                        String ftid = null;
                        if ( reader.getLocalName().equals( "FeatureStoreId" ) ) {
                            ftid = reader.getElementText();
                            reader.nextTag();
                        }

                        if ( reader.getLocalName().equals( "DirectStyle" ) ) {
                            reader.nextTag();
                            String text = reader.getElementText();
                            Matcher m = stylepattern.matcher( text );
                            if ( m.find() ) {
                                l.style = m.group( 1 );
                            } else {
                                l.style = text;
                            }
                        }
                        if ( ftid == null ) {
                            continue;
                        }
                        List<FeatureLayer> list = map.get( ftid );
                        if ( list == null ) {
                            list = new ArrayList<FeatureLayer>();
                            map.put( ftid, list );
                        }
                        list.add( l );
                    }
                    reader.next();
                }
            }
        }

        for ( Entry<String, List<FeatureLayer>> e : map.entrySet() ) {
            String id = e.getKey();
            List<FeatureLayer> list = e.getValue();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            String flns = "http://www.deegree.org/layers/feature";
            String lns = "http://www.deegree.org/layers/base";
            String dns = "http://www.deegree.org/metadata/description";
            String gns = "http://www.deegree.org/metadata/spatial";

            XMLStreamWriter writer = new IndentingXMLStreamWriter( outfac.createXMLStreamWriter( bos ) );
            writer.writeStartDocument();
            writer.setDefaultNamespace( flns );
            writer.writeStartElement( flns, "FeatureLayers" );
            writer.writeDefaultNamespace( flns );
            writer.writeNamespace( "l", lns );
            writer.writeNamespace( "d", dns );
            writer.writeNamespace( "g", gns );
            writer.writeAttribute( "configVersion", "3.2.0" );

            XMLAdapter.writeElement( writer, flns, "FeatureStoreId", id );

            for ( FeatureLayer l : list ) {
                writer.writeStartElement( flns, "FeatureLayer" );
                XMLAdapter.writeElement( writer, lns, "Name", l.name );
                XMLAdapter.writeElement( writer, dns, "Title", l.title );
                XMLAdapter.writeElement( writer, gns, "CRS", crs );
                if ( !( Double.isInfinite( l.minscale ) && Double.isInfinite( l.maxscale ) ) ) {
                    writer.writeStartElement( lns, "ScaleDenominators" );
                    if ( !Double.isInfinite( l.minscale ) ) {
                        writer.writeAttribute( "min", Double.toString( l.minscale ) );
                    }
                    if ( !Double.isInfinite( l.maxscale ) ) {
                        writer.writeAttribute( "max", Double.toString( l.maxscale ) );
                    }
                    writer.writeEndElement();
                }
                if ( l.style != null ) {
                    writer.writeStartElement( lns, "StyleRef" );
                    XMLAdapter.writeElement( writer, lns, "StyleStoreId", l.style );
                    writer.writeEndElement();
                }

                writer.writeEndElement();
            }

            writer.writeEndElement();
            writer.close();

            lmgr.createResource( id, new ByteArrayInputStream( bos.toByteArray() ) );
            lmgr.activate( id );
        }
    }

    static class FeatureLayer {
        String name;

        String title;

        String style;

        double minscale = Double.NEGATIVE_INFINITY, maxscale = Double.POSITIVE_INFINITY;
    }

}
