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
package org.deegree.services.sos.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.commons.utils.time.DateUtils;
import org.deegree.protocol.sos.time.TimeInstant;

/**
 * This {@link Measurement} shares the procedure, featureOfInterest and observedProperty with other measurements via a
 * common {@link MeasurementBase}.
 * 
 * <p>
 * A {@link SimpleMeasurement} object is immutable.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class SimpleMeasurement implements Measurement {
    private final MeasurementBase base;

    private final Date sampleDate;

    private final Procedure procedure;

    private final Map<Property, Result> results;

    /**
     * @param measurementBase
     * @param date
     * @param procedure
     * @param results
     */
    public SimpleMeasurement( MeasurementBase measurementBase, Date date, Procedure procedure, List<Result> results ) {
        this.base = measurementBase;
        this.sampleDate = date;
        this.procedure = procedure;
        this.results = new HashMap<Property, Result>();
        for ( Result result : results ) {
            this.results.put( result.getProperty(), result );
        }
    }

    public String getFeatureOfInterest() {
        return procedure.getFeatureRef();
    }

    public Procedure getProcedure() {
        return procedure;
    }

    public List<Property> getProperties() {
        return base.getProperties();
    }

    public Result getResult( Property property ) {
        return results.get( property );
    }

    public TimeInstant getSamplingTime() {
        return new TimeInstant( sampleDate );
    }

    public List<Result> getResults() {
        return new ArrayList<Result>( results.values() );
    }

    @Override
    public String toString() {
        return DateUtils.formatISO8601Date( getSamplingTime().getTime() ) + ": ["
               + getResults().get( 0 ).getResultAsString() + ",...]";
    }
}
