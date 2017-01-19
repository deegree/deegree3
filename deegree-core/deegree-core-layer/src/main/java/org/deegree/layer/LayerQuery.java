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
package org.deegree.layer;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import static org.deegree.commons.utils.MapUtils.DEFAULT_PIXEL_SIZE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.deegree.commons.utils.Pair;
import org.deegree.filter.OperatorFilter;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.rendering.r2d.RenderHelper;
import org.deegree.rendering.r2d.context.MapOptionsMaps;
import org.deegree.style.StyleRef;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class LayerQuery {

    public static final String FILTERPROPERTY = "FILTERPROPERTY";

    public static final String FILTERVALUE = "FILTERVALUE";

    public static final String RADIUS = "RADIUS";

    private final Envelope envelope;

    private final int width, height;

    private final Map<String, String> parameters;

    private int x, y, featureCount;

    private final StyleRef style;

    private final OperatorFilter filter;

    private double scale;

    private final Map<String, List<?>> dimensions;

    private double resolution;

    private final MapOptionsMaps options;

    private Envelope queryBox;

    private int layerRadius;

    /**
     * @param envelope
     * @param width
     * @param height
     * @param style
     * @param filters
     * @param parameters
     * @param dimensions
     * @param pixelSize
     *            must be in meter, not mm
     * @param options
     * @param layerRadius
     */
    public LayerQuery( Envelope envelope, int width, int height, StyleRef style, OperatorFilter filter,
                       Map<String, String> parameters, Map<String, List<?>> dimensions, double pixelSize,
                       MapOptionsMaps options, Envelope queryBox ) {
        this.envelope = envelope;
        this.width = width;
        this.height = height;
        this.style = style;
        this.filter = filter;
        this.parameters = parameters;
        this.dimensions = dimensions;
        this.options = options;
        this.queryBox = queryBox;
        this.scale = RenderHelper.calcScaleWMS130( width, height, envelope, envelope.getCoordinateSystem(), pixelSize );
        this.resolution = Utils.calcResolution( envelope, width, height );
    }

    public LayerQuery( Envelope envelope, int width, int height, int x, int y, int featureCount, OperatorFilter filter,
                       StyleRef style, Map<String, String> parameters, Map<String, List<?>> dimensions,
                       MapOptionsMaps options, Envelope queryBox, int layerRadius ) {
        this.envelope = envelope;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.featureCount = featureCount;
        this.filter = filter;
        this.style = style;
        this.parameters = parameters;
        this.dimensions = dimensions;
        this.options = options;
        this.queryBox = queryBox;
        this.layerRadius = layerRadius;
        this.scale = RenderHelper.calcScaleWMS130( width, height, envelope, envelope.getCoordinateSystem(),
                                                   DEFAULT_PIXEL_SIZE );
        this.resolution = Utils.calcResolution( envelope, width, height );
    }

    public Envelope getEnvelope() {
        return envelope;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public OperatorFilter getFilter() {
        return filter;
    }

    public StyleRef getStyle() {
        return style;
    }

    public Map<String, List<?>> getDimensions() {
        return dimensions;
    }

    public int getFeatureCount() {
        return featureCount;
    }

    public double getScale() {
        return scale;
    }

    public double getResolution() {
        return resolution;
    }

    public Envelope getQueryBox() {
        return queryBox;
    }

    public MapOptionsMaps getRenderingOptions() {
        return options;
    }

    public int getLayerRadius() {
        return layerRadius;
    }

    public Envelope calcClickBox( int radius ) {
        radius = parameters.get( RADIUS ) == null ? radius : parseInt( parameters.get( RADIUS ) );
        GeometryFactory fac = new GeometryFactory();
        double dw = envelope.getSpan0() / width;
        double dh = envelope.getSpan1() / height;
        int r2 = radius / 2;
        r2 = r2 <= 0 ? 1 : r2;
        return fac.createEnvelope( new double[] { envelope.getMin().get0() + ( x - r2 ) * dw,
                                                 envelope.getMax().get1() - ( y + r2 ) * dh },
                                   new double[] { envelope.getMin().get0() + ( x + r2 ) * dw,
                                                 envelope.getMax().get1() - ( y - r2 ) * dh },
                                   envelope.getCoordinateSystem() );
    }

    /**
     * Returns the additional request parameters used for filtering.
     *
     * @return the property (first) and the values (second) to filter for, <code>null</code> if at least one parameter is <code>null</code> or empty
     */
    public Pair<String, List<String>> requestFilter() {
        String filterProperty = parameters.get( FILTERPROPERTY );
        String filterValue = parameters.get( FILTERVALUE );
        if ( filterProperty == null || filterProperty.isEmpty() || filterValue == null || filterValue.isEmpty() )
            return null;
        List<String> filterValues = parseFilterValues( filterValue );
        return new Pair<String, List<String>>( filterProperty, filterValues );
    }

    private List<String> parseFilterValues( String filterValue ) {
        List<String> filterValues = new ArrayList<String>();
        String[] splittedFilterValue = filterValue.split( "," );
        for ( String value : splittedFilterValue ) {
            String trimmedValue = value.trim();
            if ( !trimmedValue.isEmpty() )
                filterValues.add( trimmedValue );
        }
        return filterValues;
    }

}