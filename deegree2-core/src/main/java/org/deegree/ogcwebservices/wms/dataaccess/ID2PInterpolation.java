//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.ogcwebservices.wms.dataaccess;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.values.Interval;
import org.deegree.datatypes.values.TypedLiteral;
import org.deegree.datatypes.values.Values;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.FileUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.util.TimeTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.graphics.transformation.GeoTransform;
import org.deegree.graphics.transformation.WorldToScreenTransform;
import org.deegree.io.quadtree.IndexException;
import org.deegree.io.quadtree.MemPointQuadtree;
import org.deegree.io.quadtree.Quadtree;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.GMLFeatureCollectionDocument;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;
import org.deegree.ogcwebservices.wms.operation.GetFeatureInfo;
import org.deegree.ogcwebservices.wms.operation.GetFeatureInfoResult;
import org.deegree.ogcwebservices.wms.operation.GetLegendGraphic;
import org.deegree.ogcwebservices.wms.operation.GetMap;
import org.deegree.ogcwebservices.wms.operation.GetMapResult;
import org.deegree.processing.raster.interpolation.DataTuple;
import org.deegree.processing.raster.interpolation.InterpolationException;
import org.deegree.processing.raster.interpolation.InverseDistanceToPower;

/**
 * This class reads Point data from a WFS and interpolates them into a raster (image) using deegree's inverse distant to
 * power implementation. The class expects a confguration file that looks like this:<br>
 *
 * <pre>
 * #URL des WFS, der einen FeatureType mit Punktdaten liefert
 * url=http://bfs.lat-lon.de/deegree-wfs/services
 * #Name des Properties, das die zu interpolierenden Werte enhält
 * z_value={http://www.deegree.org/app}:value
 * # Farbtiefe des Ergebnisbildes (sollte nicht geändert werden)
 * image-type=32
 * # potenz der Entfernung mit der ein Wert bei der Interpolation gewichtet wird
 * interpolate-power=2
 * # minimale anzahl von Werten, die im Suchradius enthalten sein müssen
 * interpolate-min-data=2
 * # maximale Anzahl von Werten, die zur Berechung einer Rasterzelle
 * # heran gezogen werden
 * interpolate-max-data=40
 * # Default Werte, wenn für eine Rasterzelle keine Interpolation durchgeführt
 * # werden kann
 * interpolate-no-value=0
 * # Radius der Suchellipse in x-Richtung in Prozent der Breite der Boundingbox
 * interpolate-radius-x=30
 * # Radius der Suchellipse in y-Richtung in Prozent der Breite der Boundingbox
 * interpolate-radius-y=30
 * # Wert um die die suchellipse in x-Richtung vergössert wird in  Prozent der
 * # Breite der Boundingbox falls nicht genügend Werte enthalten sind
 * interpolate-radius-increase-x=5
 * # Wert um die die suchellipse in y-Richtung vergössert wird in  Prozent der
 * # Breite der Boundingbox falls nicht genügend Werte enthalten sind
 * interpolate-radius-increase-y=5
 * # Bereich von Werten, die bei der Interpolation ignoriert werden (Fehlwerte)
 * interpolate-ignore-range=-99999,-9999
 * # Grösse des Buffers um die Bounddingbox (in % der Boundingbox) mit dem Daten
 * # vom WFS angefragt werden
 * buffer=20
 * mindata=10
 * # properties file for mapping z_values to colors
 * # example:
 * # &lt;0=0x000000
 * # 0-80=0xedf8b1
 * # 80-110=0xc7e9b4
 * # 110-140=0x7fcdbb
 * # 140-170=0x41b6c4
 * # 170-200=0x1d91c0
 * # 200-400=0x225ea8
 * # 400-600=0xc2c84
 * # &gt;600=0x8b008b
 * colorMapFile=./color.properties
 * # GetFeature request template
 * # known wildcards are:
 * # $xmin$ $ymin$ $xmax$ $ymax$ (filled with BBOX value of GetMap request)
 * # $time$ (TIME value of GetMap request)
 * GetFeatureTemplate=./getfeature_template.xml
 * # time stamp to be used if no time parameter is set in GetMap request
 * defaultTime=2009-11-03T12:30:00
 * # native CRS of data requested from WFS
 * nativeCRS=EPSG:4326
 * </pre>
 *
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ID2PInterpolation implements ExternalRasterDataAccess {

    private static final ILogger LOG = LoggerFactory.getLogger( ID2PInterpolation.class );

    // parameters

    private boolean use32Bits = false;

    // data
    private Quadtree<DataTuple> quadtree;

    private BufferedImage image;

    // interpolating options
    private double interpolatePower = 2;

    private int interpolateMinData = 5;

    private int interpolateMaxData = 20;

    private double interpolateNoValue = 0;

    private double interpolateRadiusX = 2;

    private double interpolateRadiusY = 2;

    private double interpolateRadiusIncreaseX = 0;

    private double interpolateRadiusIncreaseY = 0;

    private Values ignoreValues = null;

    private GeoTransform gt;

    private double buffer = 10;

    private Properties prop;

    private static Map<double[], Color> colorMap;

    private GetMap getMap;

    private URL configFileURL;

    private String nativeCRS;

    private void parseColorMap()
                            throws IOException {
        XMLFragment dummy = new XMLFragment();
        dummy.setSystemId( configFileURL );
        URL url = dummy.resolve( prop.getProperty( "colorMapFile" ) );
        Properties p = new Properties();
        p.load( url.openStream() );
        Iterator<Object> iter = p.keySet().iterator();
        colorMap = new HashMap<double[], Color>();
        while ( iter.hasNext() ) {
            String s = (String) iter.next();
            double[] d = null;
            if ( s.startsWith( "<" ) ) {
                d = new double[2];
                d[0] = -9E9;
                d[1] = Double.parseDouble( s.substring( 1, s.length() ) );
            } else if ( s.startsWith( ">" ) ) {
                d = new double[2];
                d[0] = Double.parseDouble( s.substring( 1, s.length() ) );
                d[1] = 9E9;
            } else {
                d = StringTools.toArrayDouble( s, "-" );
            }
            colorMap.put( d, Color.decode( p.getProperty( s ) ) );
        }
    }

    private void parseProperties() {
        List<Interval> intervals = new ArrayList<Interval>();
        nativeCRS = prop.getProperty( "nativeCRS" );
        use32Bits = prop.getProperty( "image-type" ).equals( "32" );
        interpolatePower = Double.parseDouble( prop.getProperty( "interpolate-power" ) );
        interpolateMinData = Integer.parseInt( prop.getProperty( "interpolate-min-data" ) );
        interpolateMaxData = Integer.parseInt( prop.getProperty( "interpolate-max-data" ) );
        interpolateNoValue = Double.parseDouble( prop.getProperty( "interpolate-no-value" ) );
        interpolateRadiusX = Double.parseDouble( prop.getProperty( "interpolate-radius-x" ) );
        interpolateRadiusY = Double.parseDouble( prop.getProperty( "interpolate-radius-y" ) );
        interpolateRadiusIncreaseX = Double.parseDouble( prop.getProperty( "interpolate-radius-increase-x" ) );
        interpolateRadiusIncreaseY = Double.parseDouble( prop.getProperty( "interpolate-radius-increase-y" ) );
        String tmp = prop.getProperty( "interpolate-ignore-range" );
        String[] ig = StringTools.toArray( tmp, ",;", false );
        TypedLiteral min = new TypedLiteral( ig[0], null );
        TypedLiteral max = new TypedLiteral( ig[1], null );
        Interval interval = new Interval( min, max, null, null, null );
        intervals.add( interval );
        buffer = Double.parseDouble( prop.getProperty( "buffer" ) );
    }

    // creates the buffered image with the right size
    private void createImage() {

        ColorModel ccm;

        if ( use32Bits ) {
            image = new BufferedImage( getMap.getWidth(), getMap.getHeight(), BufferedImage.TYPE_INT_ARGB );
        } else {
            ccm = new ComponentColorModel( ColorSpace.getInstance( ColorSpace.CS_GRAY ), null, false, false,
                                           Transparency.OPAQUE, DataBuffer.TYPE_USHORT );
            WritableRaster wr = ccm.createCompatibleWritableRaster( getMap.getWidth(), getMap.getHeight() );
            image = new BufferedImage( ccm, wr, false, new Hashtable<Object, Object>() );
        }
    }

    // inserts all values into the image
    private void insertValue( int x, int y, double val ) {
        Iterator<double[]> keys = colorMap.keySet().iterator();
        double[] d = keys.next();
        Color color = null;
        while ( !( val >= d[0] && val < d[1] ) ) {
            d = keys.next();
        }
        color = colorMap.get( d );
        try {
            image.setRGB( x, y, color.getRGB() );
        } catch ( Exception e ) {
            System.out.println( x + " " + y );
        }
    }

    private int buildQuadtree( FeatureCollection fc )
                            throws IndexException {

        Iterator<Feature> iterator = fc.iterator();
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        String tmp = prop.getProperty( "z_value" );
        int count = 0;
        while ( iterator.hasNext() ) {
            Feature feat = iterator.next();
            Point point = (Point) feat.getDefaultGeometryPropertyValue();
            QualifiedName qn = new QualifiedName( tmp );
            Object o = feat.getDefaultProperty( qn ).getValue();
            if ( o != null ) {
                Double zValue = Double.parseDouble( o.toString() );
                point = GeometryFactory.createPoint( point.getX(), point.getY(), null );
                quadtree.insert( new DataTuple( point.getX(), point.getY(), zValue.doubleValue() ), point );
                if ( zValue < min ) {
                    min = zValue;
                }
                if ( zValue > max ) {
                    max = zValue;
                }
                count++;
            }
        }
        System.out.println( "min value : " + min );
        System.out.println( "max value : " + max );
        return count;
    }

    private FeatureCollection readData()
                            throws Exception {
        XMLFragment dummy = new XMLFragment();
        dummy.setSystemId( configFileURL );
        URL url = dummy.resolve( prop.getProperty( "GetFeatureTemplate" ) );

        String gf = FileUtils.readTextFile( url ).toString();
        Envelope temp = getMap.getBoundingBox().getBuffer( getMap.getBoundingBox().getWidth() / 100d * buffer );
        // transform GetMap BBOX into nativeCRS if required
        if ( !getMap.getSrs().equalsIgnoreCase( nativeCRS ) ) {
            GeoTransformer tr = new GeoTransformer( nativeCRS );
            temp = tr.transform( temp, getMap.getSrs(), true );
        }
        String singleValue = null;
        if ( getMap.getDimTime() != null && getMap.getDimTime().values.size() > 0 ) {
            singleValue = getMap.getDimTime().values.peek().value;
        } else {
            if ( prop.getProperty( "defaultDateTime" ) != null ) {
                singleValue = prop.getProperty( "defaultDateTime" );
            } else {
                singleValue = TimeTools.getISOFormattedTime();
            }
        }
        gf = StringTools.replace( gf, "$time$", singleValue, false );
        gf = StringTools.replace( gf, "$xmin$", Double.toString( temp.getMin().getX() ), false );
        gf = StringTools.replace( gf, "$ymin$", Double.toString( temp.getMin().getY() ), false );
        gf = StringTools.replace( gf, "$xmax$", Double.toString( temp.getMax().getX() ), false );
        gf = StringTools.replace( gf, "$ymax$", Double.toString( temp.getMax().getY() ), false );
        LOG.logDebug( "GetFeature Request: ", gf );
        HttpClient client = new HttpClient();
        PostMethod pm = new PostMethod( prop.getProperty( "url" ) );
        pm.setRequestEntity( new StringRequestEntity( gf ) );
        client.executeMethod( pm );
        GMLFeatureCollectionDocument doc = new GMLFeatureCollectionDocument();
        InputStream is = pm.getResponseBodyAsStream();
        doc.load( is, prop.getProperty( "url" ) );
        is.close();
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            LOG.logDebug( "GetFeature Response: ", doc.getAsPrettyString() );
        }
        FeatureCollection fc = doc.parse();
        // transform feature if required into CRS requested
        if ( !getMap.getSrs().equalsIgnoreCase( nativeCRS ) ) {
            GeoTransformer tr = new GeoTransformer( getMap.getSrs() );
            fc = tr.transform( fc );
        }
        return fc;
    }

    private void writeErrorMessage() {
        Graphics g = image.getGraphics();
        g.setColor( Color.WHITE );
        g.fillRect( 0, 0, getMap.getWidth(), getMap.getHeight() );
        g.setColor( Color.RED );
        g.drawString( "not enough values for interpolation available", 10, 50 );
        g.dispose();
    }

    private void interpolate()
                            throws InterpolationException {

        Envelope bbox = getMap.getBoundingBox();
        double scx = bbox.getWidth() * interpolateRadiusX / 100d;
        double scy = bbox.getHeight() * interpolateRadiusY / 100d;
        double iscx = bbox.getWidth() * interpolateRadiusIncreaseX / 100d;
        double iscy = bbox.getHeight() * interpolateRadiusIncreaseY / 100d;

        InverseDistanceToPower interpolator = new InverseDistanceToPower( quadtree, ignoreValues, scx, scy, 0,
                                                                          interpolateMinData, interpolateMaxData,
                                                                          interpolateNoValue, iscx, iscy,
                                                                          interpolatePower );

        int count = getMap.getWidth() * getMap.getHeight();

        int counter = 0;

        int interpolatedCounter = 0;

        for ( int xipos = 0; xipos < getMap.getWidth(); ++xipos ) {
            for ( int yipos = 0; yipos < getMap.getHeight(); ++yipos ) {
                double xpos = gt.getSourceX( xipos );
                double ypos = gt.getSourceY( yipos );
                double val = interpolator.calcInterpolatedValue( xpos, ypos, scx, scy );
                insertValue( xipos, yipos, val );
            }
        }

        System.out.println( counter + '/' + count + ", interpolated " + interpolatedCounter + " values" );
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.deegree.ogcwebservices.wms.dataaccess.ExternalRasterDataAccess#perform(org.deegree.ogcwebservices.wms.operation
     * .GetMap)
     */
    public GetMapResult perform( GetMap getMap )
                            throws Exception {
        this.getMap = getMap;
        Envelope bbox = getMap.getBoundingBox();

        gt = new WorldToScreenTransform( bbox.getMin().getX(), bbox.getMin().getY(), bbox.getMax().getX(),
                                         bbox.getMax().getY(), 0, 0, getMap.getWidth() - 1, getMap.getHeight() - 1 );

        FeatureCollection fc = readData();
        System.out.println( fc.getBoundedBy() );
        quadtree = new MemPointQuadtree<DataTuple>( fc.getBoundedBy() );

        int count = buildQuadtree( fc );

        createImage();

        if ( count >= Integer.parseInt( prop.getProperty( "mindata" ) ) ) {
            interpolate();
        } else {
            writeErrorMessage();
        }

        System.out.println( "Done." );

        return new GetMapResult( getMap, image );
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.deegree.ogcwebservices.wms.dataaccess.ExternalDataAccess#perform(org.deegree.ogcwebservices.wms.operation
     * .GetFeatureInfo)
     */
    public GetFeatureInfoResult perform( GetFeatureInfo gfi ) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.deegree.ogcwebservices.wms.dataaccess.ExternalDataAccess#perform(org.deegree.ogcwebservices.wms.operation
     * .GetLegendGraphic)
     */
    public BufferedImage perform( GetLegendGraphic glg ) {

        BufferedImage bi = new BufferedImage( 150, colorMap.size() * 25, BufferedImage.TYPE_4BYTE_ABGR );
        Iterator<double[]> iterator = colorMap.keySet().iterator();
        List<double[]> list = new ArrayList<double[]>( colorMap.size() );

        while ( iterator.hasNext() ) {
            double[] ds = iterator.next();
            list.add( ds );
        }

        for ( int i = list.size() - 1; 0 <= i; i-- ) {
            for ( int j = 0; j < i; j++ ) {
                if ( list.get( j + 1 )[0] < list.get( j )[0] ) {
                    double[] ds = list.get( j + 1 );
                    list.set( j + 1, list.get( j ) );
                    list.set( j, ds );
                }
            }
        }

        int i = 0;
        Graphics g = bi.getGraphics();
        for ( double[] ds : list ) {
            Color color = colorMap.get( ds );
            g.setColor( color );
            g.fillRect( 2, 2 + i * 25, 20, 20 );
            g.setColor( Color.BLACK );
            g.drawRect( 2, 2 + i * 25, 20, 20 );
            g.drawString( Double.toString( ds[0] ) + " - " + Double.toString( ds[1] ), 25, 17 + i * 25 );
            i++;
        }
        g.dispose();
        return bi;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.ogcwebservices.wms.dataaccess.ExternalDataAccess#setConfigurationFile(java.net.URL)
     */
    public void setConfigurationFile( URL url )
                            throws IOException {
        this.configFileURL = url;
        prop = new Properties();
        prop.load( url.openStream() );
        parseProperties();
        parseColorMap();
    }

}
