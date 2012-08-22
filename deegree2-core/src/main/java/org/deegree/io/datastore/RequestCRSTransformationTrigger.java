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

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.trigger.Trigger;
import org.deegree.framework.trigger.TriggerException;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.model.crs.CRSTransformationException;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.filterencoding.FilterTools;
import org.deegree.model.filterencoding.SpatialOperation;
import org.deegree.ogcwebservices.wfs.operation.Query;

/**
 * Trigger implementation for transformation of geometries being part of a WFS request into the CRS of the comparsion
 * geometries at the physical datasource. <br>
 * At th moment just support for GetFeature is implemented TODO support for update and delete requests
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class RequestCRSTransformationTrigger implements Trigger {

    private static final ILogger LOG = LoggerFactory.getLogger( RequestCRSTransformationTrigger.class );

    private String name;

    /**
     * @param caller
     *            calling instance
     * @param values
     *            parameter values passed from the caller. Because RequestCRSTransformationTrigger is intented to be
     *            used as a preTrigger this will be all parameters passed to the calling method
     */
    public Object[] doTrigger( Object caller, Object... values ) {

        Query query = (Query) values[0];

        Filter filter = query.getFilter();

        if ( filter instanceof ComplexFilter ) {
            // transformations are just required if a filter is complex
            // because FeatureID filters does not contain spatial operations
            ComplexFilter cFilter = (ComplexFilter) filter;
            SpatialOperation[] so = FilterTools.extractSpatialFilter( cFilter );
            QualifiedName[] qns = query.getTypeNames();
            GeoTransformer gt = null;
            Datastore ds = (Datastore) caller;
            for ( int i = 0; i < qns.length; i++ ) {
                MappedFeatureType mft = ds.getFeatureType( qns[i] );
                CoordinateSystem targetCRS = mft.getGMLSchema().getDefaultCS();
                for ( int j = 0; j < so.length; j++ ) {
                    CoordinateSystem sourceCRS = so[i].getGeometry().getCoordinateSystem();
                    if ( sourceCRS != null && !targetCRS.equals( sourceCRS )
                         && !ds.canTransformTo( targetCRS.getIdentifier() ) ) {
                        try {
                            if ( gt == null ) {
                                gt = new GeoTransformer( targetCRS );
                            }
                            so[j].setGeometry( gt.transform( so[i].getGeometry() ) );
                        } catch ( CRSTransformationException e ) {
                            LOG.logError( e.getMessage(), e );
                            throw new TriggerException( e );
                        }
                    }
                }
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
