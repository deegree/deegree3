//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.feature.persistence;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.Feature;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.SimpleGeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks feature type envelope information during a {@link FeatureStoreTransaction}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class BBoxTracker {

    private static Logger LOG = LoggerFactory.getLogger( BBoxTracker.class );

    // feature types that require envelope recalculation
    private final Set<QName> recalcFts = new HashSet<QName>();

    private final Map<QName, Envelope> increaseBBoxes = new HashMap<QName, Envelope>();

    /**
     * An insert event for the specified feature instance.
     *
     * @param f
     *            feature instance to be inserted, must not be <code>null</code>
     * @param storageSrs
     *            srs of stored envelope, must not be <code>null</code>
     */
    public void insert( Feature f, ICRS storageSrs ) {
        if ( !recalcFts.contains( f.getName() ) ) {
            Envelope bbox = null;
            try {
                bbox = f.getEnvelope();
            } catch ( Exception e ) {
                LOG.warn( "Unable to determine bbox of feature with id " + f.getId() + ": " + e.getMessage() );
            }
            if ( bbox != null ) {
                try {
                    if ( bbox.getCoordinateSystem() == null ){
                        bbox.setCoordinateSystem( storageSrs );
                    }
                    if ( bbox.getCoordinateSystem() != null && !bbox.getCoordinateSystem().equals( storageSrs ) ) {
                        GeometryTransformer transformer = new GeometryTransformer( storageSrs );
                        bbox = transformer.transform( bbox );
                    }
                    Envelope oldBBox = increaseBBoxes.get( f.getName() );
                    if ( oldBBox != null ) {
                        bbox = oldBBox.merge( bbox );
                    }
                    increaseBBoxes.put( f.getName(), bbox );
                } catch ( Throwable t ) {
                    LOG.error( "Tracking bbox increase failed. Falling back to full recalculation. Error: "
                               + t.getMessage() );
                    recalcFts.add( f.getName() );
                }
            }
        }
    }

    /**
     * An update event for the specified feature type.
     *
     * @param ft
     *            feature type to be updated, must not be <code>null</code>
     */
    public void update( QName ft ) {
        LOG.debug( "Update on feature type '" + ft + "'. Full bbox recalculation required on commit." );
        recalcFts.add( ft );
    }

    /**
     * A delete event for the specified feature type.
     *
     * @param ft
     *            feature type to be deleted, must not be <code>null</code>
     */
    public void delete( QName ft ) {
        LOG.debug( "Delete on feature type '" + ft + "'. Full bbox recalculation required on commit." );
        recalcFts.add( ft );
    }

    /**
     * Returns feature type name to {@link Envelope} mappings for all envelopes to be increased.
     *
     * @return name to envelope mappings, never <code>null</code>
     */
    public Map<QName, Envelope> getIncreaseBBoxes() {
        return increaseBBoxes;
    }

    /**
     * Returns the names of all feature types that require a bbox recalculation.
     *
     * @return names of feature types, never <code>null</code>
     */
    public Set<QName> getRecalcFeatureTypes() {
        return recalcFts;
    }
}
