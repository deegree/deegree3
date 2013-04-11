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
package org.deegree.observation.model;

import java.net.URL;

import org.deegree.geometry.primitive.Point;

/**
 * This class encapsulates a observation and measurement process.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class Procedure {

    private final String procedureHref;

    private final Point location;

    private final String featureOfInterestHref;

    private final String sensorId;

    private final String sensorName;

    private final URL sensorURL;

    /**
     * 
     * @param procedureHref
     * @param location
     * @param featureOfInterestHref
     * @param sensorId
     * @param sensorName
     * @param sensorURL
     */
    public Procedure( String procedureHref, Point location, String featureOfInterestHref, String sensorId,
                      String sensorName, URL sensorURL ) {
        this.procedureHref = procedureHref;
        this.location = location;
        this.featureOfInterestHref = featureOfInterestHref;
        this.sensorId = sensorId;
        this.sensorName = sensorName;
        this.sensorURL = sensorURL;
    }

    /**
     * @return
     */
    public String getProcedureHref() {
        return procedureHref;
    }

    /**
     * 
     * @return
     */
    public Point getLocation() {
        return location;
    }

    /**
     * @return
     */
    public String getFeatureOfInterestHref() {
        return featureOfInterestHref;
    }

    /**
     * @return
     */
    public String getSensorId() {
        return sensorId;
    }

    /**
     * 
     * @return
     */
    public String getSensorName() {
        return sensorName;
    }

    /**
     * @return
     */
    public URL getSensorURL() {
        return sensorURL;
    }

    @Override
    public String toString() {
        return procedureHref;
    }

}
