package org.deegree.client.sos.utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.deegree.client.sos.storage.ChartInput;
import org.deegree.client.sos.storage.StorageGetObservation;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Year;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

/**
 * RequestHandler class for generating the chart out of the given StorageGetObservation object and the procedure String.
 * First, a ChartInput object has to be created from the StorageGetObservation object by the DataPrep class. This is
 * needed to create the XYDataset for the chart. In the end, a BufferedImage will be created from the chart.
 * 
 * @author <a href="mailto:neumeister@lat-lon.de">Ulrich Neumeister</a>
 * 
 */
public class ChartProcessing {

    private BufferedImage image;

    private static Map<Class, String> dateFormat = new HashMap<Class, String>();
    static {
        dateFormat.put( Year.class, "yyyy" );
        dateFormat.put( Month.class, "yyyy-MM" );
        dateFormat.put( Day.class, "yyyy-MM-dd" );
        dateFormat.put( Hour.class, "yyyy-MM-dd'T'HH" );
        dateFormat.put( Minute.class, "yyyy-MM-dd'T'HH:mm" );
        dateFormat.put( Second.class, "HH:mm:ss" );
        dateFormat.put( Millisecond.class, "HH:mm:ss.SSS" );
    }

    /**
     * Public constructor. Sets the ChartInput given to it on its own ChartInput. Then generates a XYDataset by calling
     * createXYDataset(). At least createChart() is called.
     * 
     */
    public ChartProcessing( StorageGetObservation observationStorage, String procedure ) {
        
        ChartInput chartInput = new DataPrep( observationStorage, procedure ).getChartInput();

        XYDataset dataset = createXYDataset( chartInput );
        String chartTitle = chartInput.getChartTitle();
        Class timeResolution = chartInput.getTimeResolution();

        JFreeChart chart = createChart( dataset, chartTitle, timeResolution );
        
        createImage( chart );
    }

    /**
     * Generates the XYDataset for the chart from ChartInput.
     * 
     * @return the Dataset
     */
    @SuppressWarnings("unchecked")
    private XYDataset createXYDataset( ChartInput chartInput ) {

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        try {
            Constructor constructor = chartInput.getTimeResolution().getConstructor( new Class[] { Date.class } );

            List<String> names = chartInput.getNames();
            for ( String name : names ) {
                TimeSeries ts = new TimeSeries( name );
                Date[] dates = chartInput.getDates();
                for ( int i = 0; i < chartInput.getDates().length; i++ ) {
                    Date dateVal = dates[i];
                    RegularTimePeriod tp = (RegularTimePeriod) constructor.newInstance( new Object[] { dateVal } );
                    ts.add( tp, chartInput.getData().get( name )[i] );
                }
                dataset.addSeries( ts );
            }
        } catch ( Exception e ) {
            // to do: exceptionhandling from timeResolution.getConstructor()
        }
        return dataset;
    }

    /**
     * @param dataset
     *            : the XYDataset from which the chart shall be build
     * @return the generated JFreeChart
     */
    private JFreeChart createChart( XYDataset dataset, String chartTitle, Class timeResolution ) {

        TimeZone.setDefault( TimeZone.getTimeZone( "GMT" ) );

        JFreeChart result = ChartFactory.createTimeSeriesChart( chartTitle, "", "", dataset, true, true, true );

        XYPlot plot = (XYPlot) result.getPlot();
        plot.setBackgroundPaint( Color.lightGray );
        plot.setDomainGridlinePaint( Color.white );
        plot.setRangeGridlinePaint( Color.white );
        plot.setAxisOffset( new RectangleInsets( 5.0, 5.0, 5.0, 5.0 ) );
        plot.setDomainCrosshairVisible( true );
        plot.setRangeCrosshairVisible( true );

        XYItemRenderer r = plot.getRenderer();
        if ( r instanceof XYLineAndShapeRenderer ) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible( true );
            renderer.setBaseShapesFilled( true );
        }
        final DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride( new SimpleDateFormat( dateFormat.get( timeResolution ) ) );

        return result;
    }

    private void createImage( JFreeChart chart ) {
        image = new BufferedImage( 600, 400, BufferedImage.TYPE_INT_RGB );
        Graphics2D g2 = image.createGraphics();
        chart.draw( g2, new Rectangle( new Dimension( 600, 400 ) ) );
    }

    public BufferedImage getImage() {
        return image;
    }

}
