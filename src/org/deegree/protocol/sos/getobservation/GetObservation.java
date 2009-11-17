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
package org.deegree.protocol.sos.getobservation;

import java.util.List;

import org.deegree.commons.utils.Pair;
import org.deegree.protocol.sos.filter.ProcedureFilter;
import org.deegree.protocol.sos.filter.PropertyFilter;
import org.deegree.protocol.sos.filter.ResultFilter;
import org.deegree.protocol.sos.filter.SpatialFilter;
import org.deegree.protocol.sos.filter.TimeFilter;

/**
 * 
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class GetObservation {

    private final String offering;

    private final List<PropertyFilter> properties;

    private final List<ProcedureFilter> procedures;

    private final String responseFormat;

    private final String resultModel;

    private final String responseMode;

    private final String srsName;

    private final List<TimeFilter> eventtime;

    private final Pair<List<String>, SpatialFilter> featureOfInterest;

    private final List<ResultFilter> resultFilter;

    /**
     * @param offering
     * @param procedures
     * @param properties
     * @param eventtime
     * @param featureOfInterest
     * @param resultFilter
     * @param resultModel
     * @param responseFormat
     * @param responseMode
     * @param srsName
     */
    GetObservation( String offering, List<ProcedureFilter> procedures, List<PropertyFilter> properties,
                    List<TimeFilter> eventtime, Pair<List<String>, SpatialFilter> featureOfInterest,
                    List<ResultFilter> resultFilter, String resultModel, String responseFormat, String responseMode,
                    String srsName ) {
        this.offering = offering == null ? "" : offering;
        this.procedures = procedures;
        this.properties = properties;
        this.eventtime = eventtime;
        if ( featureOfInterest == null ) {
            this.featureOfInterest = null;
        } else {
            this.featureOfInterest = featureOfInterest;
        }
        this.resultFilter = resultFilter;
        this.resultModel = resultModel == null ? "" : resultModel;
        // TODO the responseFormat appears to be mandatory (both in spec and schema). Shoudn't we raise an exception if
        // it is null???
        this.responseFormat = responseFormat == null ? "" : responseFormat;
        this.responseMode = responseMode == null ? "" : responseMode;
        this.srsName = srsName == null ? "" : srsName;
    }

    /**
     * @return the requested offering
     */
    public String getOffering() {
        return offering;
    }

    /**
     * @return one or more requested observedProperty
     */
    public List<PropertyFilter> getObservedProperties() {
        return properties;
    }

    /**
     * @return zero or more requested procedures
     */
    public List<ProcedureFilter> getProcedures() {
        return procedures;
    }

    /**
     * @return the requested responseFormat
     */
    public String getResponseFormat() {
        return responseFormat;
    }

    /**
     * The requested result mode.<br/>
     * 
     * one of: inline, out-of-band, attached, resultTemplate<br/>
     * resultTemplate is used in conjunction with the GetResult request (not in SOS core)
     * 
     * @return the requested resultModel
     */
    public String getResultModel() {
        return resultModel;
    }

    /**
     * @return the responseMode
     */
    public String getResponseMode() {
        return responseMode;
    }

    /**
     * @return the request SRSName
     */
    public String getSRSName() {
        return srsName;
    }

    /**
     * @return the event time filter
     */
    public List<TimeFilter> getEventTime() {
        return eventtime;
    }

    /**
     * @return the event time filter
     */
    public List<ResultFilter> getResultFilter() {
        return resultFilter;
    }

    /**
     * @return the featureOfInterest
     */
    public Pair<List<String>, SpatialFilter> getFeatureOfInterest() {
        return featureOfInterest;
    }

}
