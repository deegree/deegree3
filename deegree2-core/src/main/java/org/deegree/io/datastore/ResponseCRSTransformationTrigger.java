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
package org.deegree.io.datastore;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.trigger.Trigger;
import org.deegree.framework.trigger.TriggerException;
import org.deegree.model.crs.CRSTransformationException;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.FeatureCollection;

/**
 * Trigger implementation for transforming the geometries being part of a resposne of a Datastore
 * into the desired target CRS.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ResponseCRSTransformationTrigger implements Trigger {

    private static final ILogger LOG = LoggerFactory.getLogger( ResponseCRSTransformationTrigger.class );

    private String name;

    /**
     * @param caller
     *            calling instance
     * @param values
     *            values passed from the calling method
     */
    public Object[] doTrigger( Object caller, Object... values ) {
        FeatureCollection fc = (FeatureCollection) values[0];
        String targetCRS = (String) values[1];

        if ( targetCRS != null && fc != null ) {
            try {
                GeoTransformer gt = new GeoTransformer( targetCRS );
                fc = gt.transform( fc );
            } catch ( CRSTransformationException e ) {
                LOG.logError( e.getMessage(), e );
                throw new TriggerException( e );
            } catch ( UnknownCRSException e ) {
                LOG.logError( e.getMessage(), e );
                throw new TriggerException( e );
            }
        }
        return values;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        return "Trigger name: " + name;
    }
}
