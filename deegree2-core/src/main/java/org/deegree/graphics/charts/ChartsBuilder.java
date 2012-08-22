//$$Header: $$
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

package org.deegree.graphics.charts;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Iterator;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

/**
 * A Charts class with static methods. Its used to create de.latlon.charts from the given inputs.
 *
 * @author <a href="mailto:elmasry@lat-lon.de">Moataz Elmasry</a>
 * @author last edited by: $Author: elmasri$
 *
 * @version $Revision$, $Date: 27 Mar 2008 11:43:00$
 */
public class ChartsBuilder {

    private static final ILogger LOG = LoggerFactory.getLogger( ChartsBuilder.class );

    /**
     * To indicate a horizontal chart type
     */
    public static final int ORIENTATION_HORIZONTAL = 1001;

    /**
     * To indicate a vertical chart type
     */
    public static final int ORIENTATION_VERTICAL = 1002;

    /**
     * &CDU=34&SPD=36&Gruene=11
     */
    protected final static int VALUE_FORMAT_SIMPLE = 1101;

    /**
     * &CDU=23,25,21,26&SPD=42 23 33 36
     */
    protected final static int VALUE_FORMAT_SERIES = 1102;

    /**
     * &CDU=1970 23;1974,44;1978,43&SPD=1970,23;1974,44;1978,43
     */
    protected final static int VALUE_FORMAT_XYSERIES = 1103;

    /**
     * Unknown format
     */
    protected final static int VALUE_FORMAT_UNKNOWN = 1104;

    private ChartConfig chartConfigs = null;

    /**
     * @param chartConfigs
     */
    public ChartsBuilder( ChartConfig chartConfigs ) {
        this.chartConfigs = chartConfigs;
    }

    /**
     * Create a pie 2D/3D Pie Chart
     *
     * @param title
     *
     * @param keyedValues
     *            The key/value pairs used for the pie chart
     * @param width
     *            of the output image
     * @param height
     *            height of the output image
     * @param is3D
     *            is a 3D Chart
     * @param legend
     *            for the output chart
     * @param tooltips
     *            for the output chart
     * @param lblType
     *            Possible types are <i>Key</i>, <i>Value</i>, <i>KeyValue</i>
     * @param imageType
     *            of the output image
     * @param chartConfigs
     *            to configure the output chart, or null to use the default ChartConfig
     * @return BufferedImage representing the generated chart
     */
    public BufferedImage createPieChart( String title, QueuedMap<String, Double> keyedValues, int width, int height,
                                         boolean is3D, boolean legend, boolean tooltips, String lblType,
                                         String imageType, ChartConfig chartConfigs ) {

        DefaultPieDataset dataset = new DefaultPieDataset();
        Iterator<String> it = keyedValues.keySet().iterator();
        while ( it.hasNext() ) {
            String key = it.next();
            if ( "KeyValue".equals( lblType ) ) {
                dataset.setValue( StringTools.concat( 20, key, " ", keyedValues.get( key ) ), keyedValues.get( key ) );
            } else if ( "Value".equals( lblType ) ) {
                dataset.setValue( keyedValues.get( key ), keyedValues.get( key ) );
            } else {
                dataset.setValue( key, keyedValues.get( key ) );
            }
        }
        JFreeChart chart = null;
        if ( is3D ) {
            chart = ChartFactory.createPieChart3D( title, dataset, legend, tooltips, false );
        } else {
            chart = ChartFactory.createPieChart( title, dataset, legend, tooltips, true );
        }
        if ( chartConfigs == null ) {
            chartConfigs = this.chartConfigs;
        }
        return createBufferedImage( configPieChart( chart, chartConfigs ), width, height, imageType );
    }

    /**
     * Creates a Bar chart
     *
     * @param title
     * @param keyedValues
     *            key is the category name, value is a series tupels as follows for instance key1 = (arg1,4);(arg2,6)
     *            key2 = (arg1,8); (arg2,11)
     * @param width
     *            of the output image
     * @param height
     *            height of the output image
     * @param is3D
     *            is a 3D Chart
     * @param legend
     *            for the output chart
     * @param tooltips
     *            for the output de.latlon.charts
     * @param orientation
     *            Horiyontal or vertical chart
     * @param imageType
     *            of the output image
     * @param horizontalAxisName
     *            Name of the Horizontal Axis
     * @param verticalAxisName
     *            Name of the vertical Axis
     * @param chartConfigs
     *            to configure the output chart, or null to use the default ChartConfig
     * @return BufferedImage representing the generated chart
     * @throws IncorrectFormatException
     */
    public BufferedImage createBarChart( String title, QueuedMap<String, String> keyedValues, int width, int height,
                                         boolean is3D, boolean legend, boolean tooltips, int orientation,
                                         String imageType, String horizontalAxisName, String verticalAxisName,
                                         ChartConfig chartConfigs )
                            throws IncorrectFormatException {

        CategoryDataset dataset = convertMapToCategoryDataSet( keyedValues );
        JFreeChart chart = null;
        if ( is3D ) {
            chart = ChartFactory.createBarChart3D( title, horizontalAxisName, verticalAxisName, dataset,
                                                   translateToPlotOrientation( orientation ), legend, tooltips, false );
        } else {
            chart = ChartFactory.createBarChart( title, horizontalAxisName, verticalAxisName, dataset,
                                                 translateToPlotOrientation( orientation ), legend, tooltips, false );
        }
        if ( chartConfigs == null ) {
            chartConfigs = this.chartConfigs;
        }
        return createBufferedImage( configChart( chart, chartConfigs ), width, height, imageType );
    }

    /**
     * Creates a Line chart
     *
     * @param title
     * @param keyedValues
     *            key is the category name, value is a series tupels as follows for instance key1 = (arg1,4);(arg2,6)
     *            key2 = (arg1,8); (arg2,11)
     * @param width
     *            of the output image
     * @param height
     *            height of the output image
     * @param is3D
     *            is a 3D Chart
     * @param legend
     *            for the output chart
     * @param tooltips
     *            for the output de.latlon.charts
     * @param orientation
     *            Horiyontal or vertical chart
     * @param imageType
     *            of the output image
     * @param horizontalAxisName
     *            Name of the Horizontal Axis
     * @param verticalAxisName
     *            Name of the vertical Axis
     * @param chartConfigs
     *            to configure the output chart, or null to use the default ChartConfig
     * @return BufferedImage representing the generated chart
     * @throws IncorrectFormatException
     */
    public BufferedImage createLineChart( String title, QueuedMap<String, String> keyedValues, int width, int height,
                                          boolean is3D, boolean legend, boolean tooltips, int orientation,
                                          String imageType, String horizontalAxisName, String verticalAxisName,
                                          ChartConfig chartConfigs )
                            throws IncorrectFormatException {

        CategoryDataset dataset = convertMapToCategoryDataSet( keyedValues );

        JFreeChart chart = null;
        if ( is3D ) {
            chart = ChartFactory.createLineChart3D( title, horizontalAxisName, verticalAxisName, dataset,
                                                    translateToPlotOrientation( orientation ), legend, tooltips, false );
        } else {
            chart = ChartFactory.createLineChart( title, horizontalAxisName, verticalAxisName, dataset,
                                                  translateToPlotOrientation( orientation ), legend, tooltips, false );
        }
        if ( chartConfigs == null ) {
            chartConfigs = this.chartConfigs;
        }
        return createBufferedImage( configChart( chart, chartConfigs ), width, height, imageType );
    }

    /**
     * Creates an XY Line chart
     *
     * @param title
     * @param keyedValues
     *            key is the category name, value is a series tupels Format: key = x1,y1;x2,y2;x3,y3 Example row1 =
     *            2,3;4,10 Note that x and y have to be numbers
     * @param width
     *            of the output image
     * @param height
     *            height of the output image
     * @param legend
     *            for the output chart
     * @param tooltips
     *            for the output de.latlon.charts
     * @param orientation
     *            Horiyontal or vertical chart
     * @param imageType
     *            of the output image
     * @param horizontalAxisName
     *            Name of the Horizontal Axis
     * @param verticalAxisName
     *            Name of the vertical Axis
     * @param chartConfigs
     *            to configure the output chart, or null to use the default ChartConfig
     * @return BufferedImage representing the generated chart
     * @throws IncorrectFormatException
     */
    public BufferedImage createXYLineChart( String title, QueuedMap<String, String> keyedValues, int width, int height,
                                            boolean legend, boolean tooltips, int orientation, String imageType,
                                            String horizontalAxisName, String verticalAxisName, ChartConfig chartConfigs )
                            throws IncorrectFormatException {
        XYDataset dataset = convertMapToXYSeriesDataSet( keyedValues );

        JFreeChart chart = null;
        chart = ChartFactory.createXYLineChart( title, horizontalAxisName, verticalAxisName, dataset,
                                                translateToPlotOrientation( orientation ), legend, tooltips, false );

        XYSplineRenderer renderer = new XYSplineRenderer();
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setRenderer( renderer );
        if ( chartConfigs == null ) {
            chartConfigs = this.chartConfigs;
        }
        return createBufferedImage( configLineChart( chart, chartConfigs ), width, height, imageType );
    }

    /**
     * It takes in a map a QueuedMap and converts it to a XYDataSet. Format: key =
     * RowName,Value;RowName,Value;RowName,Value Example row1 = col1,3;col2,10
     *
     * @param keyedValues
     * @return CategoryDataSet
     * @throws IncorrectFormatException
     */
    protected CategoryDataset convertMapToCategoryDataSet( QueuedMap<String, String> keyedValues )
                            throws IncorrectFormatException {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for ( String key : keyedValues.keySet() ) {
            String value = keyedValues.get( key );
            ValueFormatsParser parser = new ValueFormatsParser( value );
            if ( parser.isFormatUnknown() ) {
                continue;
            }
            if ( !parser.isFormatSeries() && !parser.isFormatSeriesXY() ) {
                throw new IncorrectFormatException( Messages.getMessage( "GRA_CHART_BAD_FORMAT_SERIES", value ) );
            }
            int counter = 0;
            while ( parser.hasNext() ) {
                String tupel = parser.nextTupel();

                String colName = "Col" + ++counter;
                String colValue = tupel;
                dataset.addValue( Double.parseDouble( colValue ), key, colName );
            }
        }

        return dataset;
    }

    /**
     * It takes in a map a QueuedMap and converts it to a XYDataSet.The two tokens of each tupel have to be numbers
     * Format: key = x1,y1;x2,y2;x3,y3; Example row1 = 2,3;4,10;
     *
     * @param keyedValues
     * @return CategoryDataSet
     * @throws IncorrectFormatException
     */
    protected XYDataset convertMapToXYSeriesDataSet( QueuedMap<String, String> keyedValues )
                            throws IncorrectFormatException {

        XYSeriesCollection dataset = new XYSeriesCollection();

        for ( String key : keyedValues.keySet() ) {
            String value = keyedValues.get( key );
            ValueFormatsParser parser = new ValueFormatsParser( value );

            if ( !parser.isFormatSeriesXY() && !parser.isFormatUnknown() ) {
                throw new IncorrectFormatException( Messages.getMessage( "GRA_CHART_BAD_FORMAT_KEY", key ) );
            }
            if ( parser.isFormatUnknown() ) {
                continue;
            } else if ( !parser.isFormatSeriesXY() ) {
                throw new IncorrectFormatException( Messages.getMessage( "GRA_CHART_BAD_FORMAT_SERIESXY", value ) );
            }

            XYSeries series = new XYSeries( key );
            while ( parser.hasNext() ) {
                String tupel = parser.getNext();
                int separatorIndex = tupel.indexOf( "," );
                if ( separatorIndex == -1 ) {
                    separatorIndex = tupel.indexOf( " " );
                }
                if ( separatorIndex == -1 ) {
                    throw new IncorrectFormatException( Messages.getMessage( "GRA_CHART_MISSING_SEPARATOR", tupel,
                                                                             value ) );
                }

                String xValue = tupel.substring( 0, separatorIndex );
                String yValue = tupel.substring( separatorIndex + 1, tupel.length() );
                try {
                    series.add( Double.parseDouble( xValue ), Double.parseDouble( yValue ) );
                } catch ( Exception e ) {
                    throw new IncorrectFormatException( Messages.getMessage( "GRA_CHART_INVALID_TUPEL", tupel ) );
                }
            }
            dataset.addSeries( series );
        }
        return dataset;
    }

    /**
     * Translates an integer that represents the chart orientation to a plot orientation instance
     *
     * @param orientation
     * @return Horizontal plot orientation if orientation is Horizontal else Vertical
     */
    protected PlotOrientation translateToPlotOrientation( int orientation ) {

        if ( orientation == ORIENTATION_HORIZONTAL ) {
            return PlotOrientation.HORIZONTAL;
        }
        return PlotOrientation.VERTICAL;
    }

    /**
     * Creates a BufferedImage instance from a given chart, according to the given additional parameters
     *
     * @param chart
     * @param width
     *            of the generated image
     * @param height
     *            of the generated image
     * @param imageType
     *            ex image/png, image/jpg
     * @return BufferedImage
     */
    protected BufferedImage createBufferedImage( JFreeChart chart, int width, int height, String imageType ) {

        chart.setTextAntiAlias( true );
        chart.setAntiAlias( true );
        BufferedImage image = new BufferedImage( width, height, mapImageformat( imageType ) );
        Graphics2D g2 = image.createGraphics();
        chart.draw( g2, new Rectangle( new Dimension( width, height ) ) );
        return image;
    }

    /**
     * Configures the pie chart according to the stored configurations file
     *
     * @param chart
     * @param chartConfigs
     *            to configure the output chart
     * @return configured JFreeChart
     */
    protected JFreeChart configPieChart( JFreeChart chart, ChartConfig chartConfigs ) {

        chart = configChart( chart, chartConfigs );

        ( (PiePlot) chart.getPlot() ).setLabelFont( new Font( chartConfigs.getGenFontFamily(),
                                                              findFontType( chartConfigs.getGenFontType() ),
                                                              (int) chartConfigs.getGenFontSize() ) );
        ( (PiePlot) chart.getPlot() ).setInteriorGap( chartConfigs.getPieInteriorGap() );
        ( (PiePlot) chart.getPlot() ).setLabelGap( chartConfigs.getPieLabelGap() );
        ( (PiePlot) chart.getPlot() ).setCircular( chartConfigs.isPieCircular() );
        ( (PiePlot) chart.getPlot() ).setBaseSectionPaint( chartConfigs.getPieBaseSectionColor() );
        ( (PiePlot) chart.getPlot() ).setShadowPaint( chartConfigs.getPieShadowColor() );
        return chart;
    }

    /**
     * Configures the pie chart according to the stored configurations file
     *
     * @param chart
     * @param chartConfigs
     *            to configure the output chart
     * @return configured JFreeChart
     */
    protected JFreeChart configLineChart( JFreeChart chart, ChartConfig chartConfigs ) {

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer( chartConfigs.isLineRenderLines(),
                                                                      chartConfigs.isLineRenderShapes() );

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setRenderer( renderer );
        return chart;
    }

    /**
     * Initializes the chart with the values from the properties file config.properties
     *
     * @param chart
     * @param chartConfigs
     *            to configure the output chart
     * @return initialized chart
     */
    protected JFreeChart configChart( JFreeChart chart, ChartConfig chartConfigs ) {

        chart.setAntiAlias( chartConfigs.isGenAntiAliasing() );

        chart.setBorderVisible( chartConfigs.isGenBorderVisible() );

        String rectanglkeInsets = chartConfigs.getGenRectangleInsets();
        if ( !rectanglkeInsets.startsWith( "!" ) ) {
            String[] insets = rectanglkeInsets.split( "," );
            if ( insets.length == 4 ) {
                try {
                    double top = Double.parseDouble( insets[0] );
                    double left = Double.parseDouble( insets[1] );
                    double buttom = Double.parseDouble( insets[2] );
                    double right = Double.parseDouble( insets[3] );
                    RectangleInsets rectInsets = new RectangleInsets( top, left, buttom, right );
                    chart.setPadding( rectInsets );
                } catch ( Exception e ) {
                    LOG.logError( Messages.getMessage( "GRA_CHART_BAD_FORMAT_INSETS" ) );
                }
            } else {
                LOG.logError( Messages.getMessage( "GRA_CHART_BAD_FORMAT_INSETS" ) );
            }
        }

        chart.setTextAntiAlias( chartConfigs.isGenTextAntiAlias() );
        chart.setBackgroundPaint( chartConfigs.getGenBackgroundColor() );

        chart.getPlot().setOutlineVisible( chartConfigs.isPlotOutlineVisible() );
        chart.getPlot().setForegroundAlpha( (float) chartConfigs.getPlotForegroundOpacity() );
        chart.getPlot().setBackgroundPaint( chartConfigs.getPlotBackgroundColor() );
        return chart;
    }

    /**
     * Maps the image format to an appropriate type, either RGB or RGBA (allow opacity). There are image types that
     * allow opacity like png, while others don't, like jpg
     *
     * @param imgFormat
     * @return BufferedImage Type INT_ARGB if the mime type is image/png or image/gif INT_RGB else.
     */
    protected int mapImageformat( String imgFormat ) {
        if ( ( "image/png" ).equals( imgFormat ) || ( "image/gif" ).equals( imgFormat ) ) {
            return BufferedImage.TYPE_INT_ARGB;
        }
        return BufferedImage.TYPE_INT_RGB;
    }

    /**
     * Finds the appropriate integer that represents either one of the following "PLAIN","BOLD" or "ITALIC"
     *
     * @param fontType
     * @return font type
     */
    private int findFontType( String fontType ) {

        if ( fontType.toUpperCase().equals( "ITALIC" ) ) {
            return Font.ITALIC;
        } else if ( fontType.toUpperCase().equals( "BOLD" ) ) {
            return Font.BOLD;
        }
        return Font.PLAIN;
    }
}
