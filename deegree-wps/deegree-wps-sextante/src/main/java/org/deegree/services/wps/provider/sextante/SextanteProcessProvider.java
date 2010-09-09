//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.services.exception.ServiceInitException;
import org.deegree.services.wps.WPSProcess;
import org.deegree.services.wps.provider.ProcessProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.IGeoAlgorithmFilter;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.outputs.Output;

/**
 * {@link ProcessProvider} that provides <a
 * href="http://www.osor.eu/studies/sextante-a-geographic-information-system-for-the-spanish-region-of-extremadura"
 * >Sextante</a> processes.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SextanteProcessProvider implements ProcessProvider {

    private static final Logger LOG = LoggerFactory.getLogger( SextanteProcessProvider.class );

    // SEXTANTE WPS processes
    private final Map<CodeType, WPSProcess> idToProcess = new HashMap<CodeType, WPSProcess>();

    /**
     * Returns an array of SEXTANTE {@link GeoAlgorithm}s, which processing vector data.
     * 
     * @return Array of SEXTANTE {@link GeoAlgorithm}s, which processing vector data.
     */
    public static GeoAlgorithm[] getVectorLayerAlgorithms() {
        Sextante.initialize();

        // filter for geoalgorithms
        IGeoAlgorithmFilter algFilter = new IGeoAlgorithmFilter() {

            @Override
            public boolean accept( GeoAlgorithm alg ) {

                boolean answer = true;

                // Raster Layers inclusive Multiple Input
                if ( alg.getNumberOfRasterLayers( true ) > 0 )
                    answer = false;

                // Table Fields
                if ( alg.getNumberOfTableFieldsParameters() > 0 )
                    answer = false;

                // Bands
                if ( alg.getNumberOfBandsParameters() > 0 )
                    answer = false;

                // Tables
                if ( alg.getNumberOfTables() > 0 )
                    answer = false;

                // No Date Parameters like String, Number, Boolean, etc.
                if ( alg.getNumberOfNoDataParameters() > 0 )
                    answer = false;

                // Multiple Input - Vector Layer
                if ( alg.getNumberOfVectorLayers( true ) - alg.getNumberOfVectorLayers( false ) > 0 )
                    answer = false;

                // Vector Layer (only 1)
                if ( alg.getNumberOfVectorLayers( false ) > 1 && answer ) {
                    // LOG.info( "MORE VECTOR LAYERS: " + alg.getCommandLineName() );
                    SextanteWPSProcess.logAlgorithm( alg );
                }

                // Output Paramerter Raster and Vector Layer
                OutputObjectsSet outputObjects = alg.getOutputObjects();

                // only 1 vector layer
                if ( outputObjects.getOutputObjectsCount() == 1 )

                    for ( int i = 0; i < outputObjects.getOutputObjectsCount(); i++ ) {
                        Output out = outputObjects.getOutput( i );
                        if ( out.getTypeDescription().equals( "raster" ) )
                            answer = false;
                        else if ( out.getTypeDescription().equals( "table" ) )
                            answer = false;
                        else if ( out.getTypeDescription().equals( "text" ) )
                            answer = false;
                        else if ( out.getTypeDescription().equals( "chart" ) )
                            answer = false;
                    }

                else
                    answer = false;

                return answer;
            }
        };

        // create a array of SEXTANTE-Algorithms
        HashMap<?, ?> algorithms = Sextante.getAlgorithms( algFilter );
        Set<?> keys = algorithms.keySet();
        Iterator<?> iterator = keys.iterator();
        GeoAlgorithm[] algs = new GeoAlgorithm[keys.size()];
        for ( int j = 0; j < algs.length; j++ ) {
            GeoAlgorithm alg = (GeoAlgorithm) algorithms.get( iterator.next() );
            algs[j] = alg;
        }

        return algs;
    }

    SextanteProcessProvider() {
    }

    @Override
    public void init()
                            throws ServiceInitException {
        // initialize SEXTANTE
        Sextante.initialize();
        LOG.info( "Sextante initialized" );

        // initialize WPS processes
        GeoAlgorithm[] algs = getVectorLayerAlgorithms();

        for ( int i = 0; i < algs.length; i++ ) {
            // SEXTANTE algorithm
            GeoAlgorithm alg = algs[i];

            // create WPS process code type
            CodeType codeType = new CodeType( alg.getCommandLineName() );

            // add and initialize process
            SextanteWPSProcess process = new SextanteWPSProcess( alg );
            process.getProcesslet().init();
            idToProcess.put( codeType, process );
        }
    }

    @Override
    public void destroy() {
        for ( WPSProcess process : idToProcess.values() ) {
            process.getProcesslet().destroy();
        }
    }

    @Override
    public WPSProcess getProcess( CodeType id ) {
        return idToProcess.get( id );
    }

    @Override
    public Map<CodeType, WPSProcess> getProcesses() {
        return idToProcess;
    }

}
