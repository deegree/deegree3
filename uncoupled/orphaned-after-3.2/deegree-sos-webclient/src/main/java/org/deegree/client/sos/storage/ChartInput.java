package org.deegree.client.sos.storage;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Keeps the data needed for creating the chart by ChartProcessing.
 * 
 * @author <a href="mailto:neumeister@lat-lon.de">Ulrich Neumeister</a>
 * 
 */
public class ChartInput {

    private String chartTitle;
    
    private Map<String, double[]> data = new HashMap<String, double[]>();

    private Date[] dates;

    private List<String> names;
    
    private Class timeResolution;

    /**
     * Public constructor
     */
    public ChartInput() {

    }

    /**
     * @return the data from the getObservation-request stored in a Map
     */
    public Map<String, double[]> getData() {
        return data;
    }

    /**
     * @return the dates from the getObservation-request stored in a Date[]
     */
    public Date[] getDates() {
        return dates;
    }

    public Class getTimeResolution() {
        return timeResolution;
    }
    
    public String getChartTitle(){
        return chartTitle;
    }
    
    public List<String> getNames(){
        return names;
    }

    /**
     * replaces the data with the given values
     * 
     * @param values
     *            : the given values stored in a Map
     */
    public void setData( Map<String, double[]> values ) {
        data = values;
    }

    /**
     * replaces the stored dates with the given dates
     * 
     * @param time
     *            : the given Date-values stored in a Date[]
     */
    public void setDates( Date[] time ) {
        dates = time;
    }

    public void setTimeResolution( Class tr ) {
        timeResolution = tr;
    }
    
    public void setChartTitle(String title){
        chartTitle = title;
    }
    
    public void setNames(List<String> that){
        names = that;
    }

}
