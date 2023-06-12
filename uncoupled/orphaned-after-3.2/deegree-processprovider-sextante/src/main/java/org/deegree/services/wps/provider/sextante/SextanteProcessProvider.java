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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.services.wps.WPSProcess;
import org.deegree.services.wps.provider.ProcessProvider;
import org.deegree.services.wps.provider.sextante.jaxb.SextanteProcesses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.IGeoAlgorithmFilter;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.parameters.Parameter;

/**
 * {@link ProcessProvider} that provides <a
 * href="http://www.osor.eu/studies/sextante-a-geographic-information-system-for-the-spanish-region-of-extremadura"
 * >Sextante</a> processes.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * 
 */
public class SextanteProcessProvider implements ProcessProvider {

    private static final Logger LOG = LoggerFactory.getLogger( SextanteProcessProvider.class );

    // SEXTANTE WPS processes
    private final Map<CodeType, WPSProcess> idToProcess = new HashMap<CodeType, WPSProcess>();

    // configurations of configuration file
    private final SextanteProcesses config;

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

                // allowed input parameters
                ParametersSet paramSet = alg.getParameters();
                for ( int i = 0; i < paramSet.getNumberOfParameters(); i++ ) {
                    Parameter param = paramSet.getParameter( i );

                    if ( !param.getParameterTypeName().equals( SextanteWPSProcess.VECTOR_LAYER_INPUT )
                    // && !param.getParameterTypeName().equals( SextanteWPSProcess.NUMERICAL_VALUE_INPUT )
                    // && !param.getParameterTypeName().equals( SextanteWPSProcess.BOOLEAN_INPUT )
                    // && !param.getParameterTypeName().equals( SextanteWPSProcess.SELECTION_INPUT )
                    ) {
                        answer = false;
                        break;
                    }

                }

                // allowed output parameters
                OutputObjectsSet outputSet = alg.getOutputObjects();
                for ( int i = 0; i < outputSet.getOutputDataObjectsCount(); i++ ) {
                    Output output = outputSet.getOutput( i );

                    if ( !output.getTypeDescription().equals( SextanteWPSProcess.VECTOR_LAYER_OUTPUT )
                    // && !output.getTypeDescription().equals( SextanteWPSProcess.VECTOR_LAYER_OUTPUT )
                    ) {
                        answer = false;
                        break;
                    }

                }

                if ( answer )
                    SextanteWPSProcess.logAlgorithm( alg );

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

    /**
     * This method returns all supported SEXTANTE {@link GeoAlgorithm}s, these are called in the configuration file.
     * 
     * @param config
     *            Data of the configuration file.
     * @return All supported SEXTANTE {@link GeoAlgorithm}s.
     */
    public static GeoAlgorithm[] getSupportedAlgorithms( SextanteProcesses config ) {

        // initialize Sextante
        Sextante.initialize();

        // list for notice algorithms
        LinkedList<GeoAlgorithm> algs = new LinkedList<GeoAlgorithm>();

        if ( config != null ) {

            // list of supported algorithms
            List<org.deegree.services.wps.provider.sextante.jaxb.SextanteProcesses.Process> processes = config.getProcess();

            for ( org.deegree.services.wps.provider.sextante.jaxb.SextanteProcesses.Process p : processes ) {

                // get algorithm
                GeoAlgorithm alg = Sextante.getAlgorithmFromCommandLineName( p.getId() );

                if ( alg != null ) {// found
                    algs.add( alg );
                } else {// not found
                    String message = "Algorithm with the id '" + p.getId() + "' is not found.";
                    LOG.error( message );
                }
            }

        } else {
            String message = "Configuration file can not be found.";
            LOG.error( message );
        }

        return algs.toArray( new GeoAlgorithm[algs.size()] );
    }

    SextanteProcessProvider( SextanteProcesses config ) {
        this.config = config;
    }

    @Override
    public void init( DeegreeWorkspace workspace )
                            throws ResourceInitException {

        // initialize SEXTANTE
        Sextante.initialize();
        LOG.info( "Sextante initialized" );

        // initialize WPS processes
        // GeoAlgorithm[] algs = getVectorLayerAlgorithms();
        GeoAlgorithm[] algs = getSupportedAlgorithms( config );

        for ( int i = 0; i < algs.length; i++ ) {
            // SEXTANTE algorithm
            GeoAlgorithm alg = algs[i];

            // create WPS process code type
            CodeType codeType = new CodeType( SextanteWPSProcess.createIdentifier( alg ) );

            // add and initialize process
            // SextanteWPSProcess process = new SextanteWPSProcess( alg, null );
            SextanteWPSProcess process = new SextanteWPSProcess( alg, config );
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
