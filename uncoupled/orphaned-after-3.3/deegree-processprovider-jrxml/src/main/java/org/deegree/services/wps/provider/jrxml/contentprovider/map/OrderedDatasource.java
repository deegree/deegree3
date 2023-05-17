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

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.dom.DOMSource;

import org.deegree.commons.utils.Pair;
import org.deegree.geometry.Envelope;
import org.deegree.rendering.r2d.legends.Legends;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.provider.jrxml.jaxb.map.AbstractDatasourceType;
import org.deegree.services.wps.provider.jrxml.jaxb.map.Style;
import org.deegree.style.se.parser.SymbologyParser;

abstract class OrderedDatasource<T extends AbstractDatasourceType> {
    // OrderedDatasource
    // abstract String getRequest( int width, int height, String bbox, String crs );

    int min = Integer.MIN_VALUE;

    int max = Integer.MIN_VALUE;

    final T datasource;

    public OrderedDatasource( T datasource ) {
        this.datasource = datasource;
    }

    public abstract List<Pair<String, BufferedImage>> getLegends( int width )
                            throws ProcessletException;

    public OrderedDatasource( T datasource, int min, int max ) {
        this.datasource = datasource;
        this.min = min;
        this.max = max;
    }

    abstract BufferedImage getImage( int width, int height, Envelope bbox )
                            throws ProcessletException;

    abstract Map<String, List<String>> getLayerList();

    protected org.deegree.style.se.unevaluated.Style getStyle( Style dsStyle )
                            throws MalformedURLException, FactoryConfigurationError, XMLStreamException,
                            IOException {
        XMLStreamReader reader = null;
        if ( dsStyle == null || dsStyle.getNamedStyle() != null ) {
            return null;
        } else if ( dsStyle.getExternalStyle() != null ) {
            URL ex = new URL( dsStyle.getExternalStyle() );
            XMLInputFactory fac = XMLInputFactory.newInstance();
            reader = fac.createXMLStreamReader( ex.openStream() );
        } else if ( dsStyle.getEmbeddedStyle() != null ) {
            XMLInputFactory fac = XMLInputFactory.newInstance();
            reader = fac.createXMLStreamReader( new DOMSource( dsStyle.getEmbeddedStyle() ) );
            nextElement( reader );
            nextElement( reader );
        }

        SymbologyParser symbologyParser = SymbologyParser.INSTANCE;
        return symbologyParser.parse( reader );
    }

    protected BufferedImage getLegendImg( org.deegree.style.se.unevaluated.Style style, int width ) {
        Legends legends = new Legends();
        Pair<Integer, Integer> legendSize = legends.getLegendSize( style );
        if ( legendSize.first < width )
            width = legendSize.first;
        BufferedImage img = new BufferedImage( width, legendSize.second, BufferedImage.TYPE_INT_ARGB );
        Graphics2D g = img.createGraphics();
        g.setRenderingHint( KEY_ANTIALIASING, VALUE_ANTIALIAS_ON );
        g.setRenderingHint( KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON );
        legends.paintLegend( style, width, legendSize.second, g );
        g.dispose();
        return img;
    }
}