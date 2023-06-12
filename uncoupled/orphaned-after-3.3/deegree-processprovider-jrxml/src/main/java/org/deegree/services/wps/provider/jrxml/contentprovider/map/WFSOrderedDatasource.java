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
package org.deegree.services.wps.provider.jrxml.contentprovider.map;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.dom.DOMSource;

import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.Triple;
import org.deegree.feature.Feature;
import org.deegree.feature.xpath.TypedObjectNodeXPathEvaluator;
import org.deegree.filter.Filter;
import org.deegree.filter.XPathEvaluator;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.protocol.wfs.client.GetFeatureResponse;
import org.deegree.protocol.wfs.client.WFSClient;
import org.deegree.protocol.wfs.client.WFSFeatureCollection;
import org.deegree.rendering.r2d.Java2DRenderer;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.provider.jrxml.jaxb.map.WFSDatasource;
import org.deegree.style.styling.Styling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * 
 */
class WFSOrderedDatasource extends OrderedDatasource<WFSDatasource> {

    private static final Logger LOG = LoggerFactory.getLogger( WFSOrderedDatasource.class );

    public WFSOrderedDatasource( WFSDatasource datasource ) {
        super( datasource );
    }

    public WFSOrderedDatasource( WFSDatasource datasource, int pos ) {
        super( datasource, pos, pos );
    }

    @SuppressWarnings("rawtypes")
    @Override
    BufferedImage getImage( int width, int height, Envelope bbox )
                            throws ProcessletException {
        LOG.debug( "create map image for WFS datasource '{}'", datasource.getName() );
        GetFeatureResponse<Feature> response = null;
        BufferedImage image = null;
        XMLStreamReader reader = null;
        try {
            String capURL = datasource.getUrl() + "?service=WFS&request=GetCapabilities&version="
                            + datasource.getVersion();
            WFSClient wfsClient = new WFSClient( new URL( capURL ) );

            Filter filter = null;
            final Element filterFromRequest = datasource.getFilter();
            if ( filterFromRequest != null ) {
                XMLInputFactory fac = XMLInputFactory.newInstance();
                reader = fac.createXMLStreamReader( new DOMSource( filterFromRequest ) );
                while ( reader.getEventType() != XMLStreamReader.START_ELEMENT
                        || ( reader.isStartElement() && !"http://www.opengis.net/ogc".equals( reader.getNamespaceURI() ) ) ) {
                    reader.nextTag();
                }
                filter = Filter110XMLDecoder.parse( reader );
            }

            response = wfsClient.getFeatures( new QName( datasource.getFeatureType().getValue(),
                                                         datasource.getFeatureType().getValue() ), filter );
            image = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
            Graphics2D g = image.createGraphics();
            Java2DRenderer renderer = new Java2DRenderer( g, width, height, bbox, bbox.getCoordinateDimension() /* pixelSize */);

            org.deegree.style.se.unevaluated.Style style = getStyle( datasource.getStyle() );
            if ( style != null && response != null ) {
                WFSFeatureCollection wfsFc = response.getAsWFSFeatureCollection();
                @SuppressWarnings("unchecked")
                Iterator<Feature> iter = wfsFc.getMembers();
                while ( iter.hasNext() ) {
                    Feature feature = iter.next();
                    XPathEvaluator<?> evaluator = new TypedObjectNodeXPathEvaluator( );
                    LinkedList<Triple<Styling, LinkedList<Geometry>, String>> evaluate = style.evaluate( feature,
                                                                                                         (XPathEvaluator<Feature>) evaluator );
                    for ( Triple<Styling, LinkedList<Geometry>, String> triple : evaluate ) {
                        for ( Geometry geom : triple.second ) {
                            renderer.render( triple.first, geom );
                        }
                    }
                }
            }
            g.dispose();
            return image;
        } catch ( Exception e ) {
            String msg = "Could nor create image from wfs datasource " + datasource.getName() + ":  " + e.getMessage();
            LOG.error( msg, e );
            throw new ProcessletException( msg );
        } finally {
            try {
                if ( reader != null )
                    reader.close();
                if ( response != null )
                    response.close();
            } catch ( Exception e ) {
                LOG.info( "Reader/Response could not be closed: " + e.getMessage() );
            }
        }
    }

    @Override
    Map<String, List<String>> getLayerList() {
        HashMap<String, List<String>> layerList = new HashMap<String, List<String>>();
        layerList.put( datasource.getName(), new ArrayList<String>() );
        return layerList;
    }

    @Override
    public List<Pair<String, BufferedImage>> getLegends( int width ) {
        ArrayList<Pair<String, BufferedImage>> legends = new ArrayList<Pair<String, BufferedImage>>();

        BufferedImage legend = null;
        String name = datasource.getName() != null ? datasource.getName() : datasource.getUrl();
        try {
            org.deegree.style.se.unevaluated.Style style = getStyle( datasource.getStyle() );
            if ( style != null ) {
                legend = getLegendImg( style, width );
            }
        } catch ( Exception e ) {
            LOG.debug( "Could not create legend for wfs datasource '{}': {}", name, e.getMessage() );
        }
        legends.add( new Pair<String, BufferedImage>( name, legend ) );
        return legends;
    }

}
