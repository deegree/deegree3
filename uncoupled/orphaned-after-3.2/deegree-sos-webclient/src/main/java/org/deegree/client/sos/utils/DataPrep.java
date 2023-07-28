package org.deegree.client.sos.utils;

import static org.deegree.commons.tom.datetime.ISO8601Converter.parseDateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.client.sos.storage.ChartInput;
import org.deegree.client.sos.storage.StorageGetObservation;
import org.deegree.client.sos.storage.components.Field;
import org.deegree.client.sos.storage.components.Observation;
import org.deegree.commons.utils.Pair;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Month;
import org.jfree.data.time.Second;
import org.jfree.data.time.Year;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The DataPrep converts the data from the StorageGetObservation into a ChartInput.
 * 
 * @author <a href="mailto:neumeister@lat-lon.de">Ulrich Neumeister</a>
 * 
 */
public class DataPrep {

    private static final Logger LOG = LoggerFactory.getLogger( DataPrep.class );

    private ChartInput chartInput = new ChartInput();

    /**
     * Public constructor for the DataPrep.
     * 
     * @param storage
     *            the unformatted values from the xml response
     * @param ID
     *            the ID of the data that shall be shown in the chart
     */
    public DataPrep( StorageGetObservation storage, String procedure ) {
        createChartInput( storage, procedure );
    }

    /**
     * Transforms the data from the DataStorage into a ChartInput.
     */
    private void createChartInput( StorageGetObservation storage, String procedure ) {
        List<Observation> observationCollection = storage.getObservationCollection();
        for ( Observation observation : observationCollection ) {
            if ( observation.getProcedure().equals( procedure ) ) {

                List<String> names = new ArrayList<String>();

                String rawvalues = observation.getDataArray().getValues();

                String tokenSeparator = "";
                String blockSeparator = "";
                for ( Pair<String, String> separator : observation.getDataArray().getSeparators() ) {
                    if ( separator.first.equals( "tokenSeparator" ) ) {
                        tokenSeparator = separator.second;
                    } else if ( separator.first.equals( "blockSeparator" ) ) {
                        blockSeparator = separator.second;
                    }
                }

                String[] rawblocks = rawvalues.split( blockSeparator );
                int countValues = rawblocks.length;

                Date[] dates = new Date[countValues];
                Map<String, double[]> data = new HashMap<String, double[]>( countValues );

                double[] converted;

                int countDiff = 0;
                try {
                    for ( int i = 0; i < countValues; i++ ) {
                        String[] contents = rawblocks[i].split( tokenSeparator );
                        dates[i] = parseDateTime( contents[0] ).getDate();
                        // Time occurs ever(!) first in the values!

                        countDiff = contents.length;
                    }

                    for ( int i = 1; i < countDiff; i++ ) {
                        converted = new double[countValues];
                        for ( int j = 0; j < countValues; j++ ) {
                            String[] contents = rawblocks[j].split( tokenSeparator );
                            converted[j] = Double.parseDouble( contents[i] );
                        }
                        String columnName = "";
                        List<Field> fields = observation.getDataArray().getElementTypes();
                        for ( Field field : fields ) {
                            if ( field.getIndex() == i ) {
                                columnName = field.getName();
                                names.add( columnName );
                            }
                        }
                        data.put( columnName, converted );
                    }
                    chartInput.setDates( dates );
                    chartInput.setData( data );
                    chartInput.setTimeResolution( determineTimeResolution( dates ) );
                } catch ( IllegalArgumentException e ) {
                    e.printStackTrace();
                    LOG.error( "error occured while parsing the dates" );
                }
                chartInput.setNames( names );
                chartInput.setChartTitle( procedure );
            }
        }
    }

    /**
     * Sub-method for determining the resolution of the requested time. This is needed while generating the chart and
     * setting the labels of the axis.
     */
    private Class determineTimeResolution( Date[] dates ) {
        Class result;
        double l = 0;
        l = dates[1].getTime() - dates[0].getTime();
        if ( l < 1000d ) {
            result = Millisecond.class;
        } else if ( l < 1000d * 60d ) {
            result = Second.class;
        } else if ( l < 1000d * 60d * 60d ) {
            result = Minute.class;
        } else if ( l < 1000d * 60d * 60d * 24d ) {
            result = Hour.class;
        } else if ( l < 1000d * 60d * 60d * 24d * 31d ) {
            result = Day.class;
        } else if ( l < 1000d * 60d * 60d * 24d * 365d ) {
            result = Month.class;
        } else {
            result = Year.class;
        }
        return result;
    }

    /**
     * @return The input for the chart. It's needed for ChartProcessing.
     */
    public ChartInput getChartInput() {
        return chartInput;
    }

}
