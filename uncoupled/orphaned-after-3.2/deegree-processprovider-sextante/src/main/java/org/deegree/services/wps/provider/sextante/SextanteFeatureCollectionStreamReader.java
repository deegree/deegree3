/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.services.wps.provider.sextante;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.gml.feature.StreamFeatureCollection;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.parameters.Parameter;

/**
 * This class will be need to read {@link Feature} for {@link Feature} from {@link FeatureCollection} as Stream.
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * 
 */
public class SextanteFeatureCollectionStreamReader {

    private static final List<SextanteFeatureCollectionStreamReader> ALL_CONTAINERS = new LinkedList<SextanteFeatureCollectionStreamReader>();

    private final StreamFeatureCollection sfc;

    private final Parameter param;

    private boolean foundFeature = true;

    /**
     * Checks whether one of {@link SextanteFeatureCollectionStreamReader}s contains {@link Feature}s.
     * 
     * @return true = if one of {@link SextanteFeatureCollectionStreamReader}s contains {@link Feature}s.
     */
    public static boolean containOneOfAllReadersFeatures() {

        int counter = 0;

        // count no feature containers
        for ( SextanteFeatureCollectionStreamReader container : ALL_CONTAINERS ) {
            if ( !container.foundFeature ) {
                counter++;
            }
        }

        // check count with container size
        if ( counter == ALL_CONTAINERS.size() ) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Closes all {@link SextanteFeatureCollectionStreamReader}s.
     * 
     * @throws IOException
     */
    public static void closeAll()
                            throws IOException {
        for ( SextanteFeatureCollectionStreamReader container : ALL_CONTAINERS ) {
            container.close();
        }
    }

    public SextanteFeatureCollectionStreamReader( Parameter param, StreamFeatureCollection sfc ) {
        this.sfc = sfc;
        this.param = param;
        ALL_CONTAINERS.add( this );
    }

    /**
     * Returns the SEXTANTE {@link GeoAlgorithm} input parameter.
     * 
     * @return SEXTANTE {@link GeoAlgorithm} input parameter.
     */
    public Parameter getParameter() {
        return param;
    }

    /**
     * Returns the next {@link Feature} as {@link IVectorLayer}.
     * 
     * @return Next {@link Feature} as {@link IVectorLayer}, if no {@link Feature}s available returns an empty
     *         {@link IVectorLayer}.
     * @throws IOException
     */
    public IVectorLayer getNextFeatureAsVectorLayer()
                            throws IOException {
        Feature f = sfc.read();

        if ( f != null ) {
            foundFeature = true;
            return VectorLayerAdapter.createVectorLayer( f );
        } else {
            foundFeature = false;
            return new VectorLayerImpl( "Empty Vector Layer", "EPSG:4326" );
        }
    }

    /**
     * Returns, if {@link Feature} are found.
     * 
     * @return If {@link Feature} are found.
     */
    public boolean foundFeature() {
        return foundFeature;
    }

    /**
     * Closes the stream.
     * 
     * @throws IOException
     */
    public void close()
                            throws IOException {
        sfc.close();
    }
}
