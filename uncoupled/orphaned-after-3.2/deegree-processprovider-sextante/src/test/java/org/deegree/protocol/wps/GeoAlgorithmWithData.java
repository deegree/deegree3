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
package org.deegree.protocol.wps;

import java.util.LinkedList;

import org.deegree.services.wps.provider.sextante.ExampleData;
import org.deegree.services.wps.provider.sextante.OutputFormat;
import org.deegree.services.wps.provider.sextante.SextanteWPSProcess;

import es.unex.sextante.core.GeoAlgorithm;

/**
 * This class wraps a {@link GeoAlgorithm} with its test data.
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * 
 */
public class GeoAlgorithmWithData {

    // SEXTANTE algorithm
    private final GeoAlgorithm alg;

    // test data for the algorithm
    private final LinkedList<LinkedList<ExampleData>> data = new LinkedList<LinkedList<ExampleData>>();

    // output formats for the algorithm
    private final LinkedList<LinkedList<OutputFormat>> outputFormats = new LinkedList<LinkedList<OutputFormat>>();

    /**
     * Creates a {@link GeoAlgorithmWithData} without test data.
     * 
     * @param alg
     *            - SEXTANTE {@link GeoAlgorithm}.
     */
    public GeoAlgorithmWithData( GeoAlgorithm alg ) {
        this( alg, null, null );
    }

    /**
     * Creates a {@link GeoAlgorithmWithData} with test data.
     * 
     * @param alg
     *            SEXTANTE {@link GeoAlgorithm}.
     * @param data
     *            A list of a list of {@link ExampleData}. If the algorithm need only one input parameter, you need only
     *            one element of {@link ExampleData} in the list. If the algorithm need more than one input parameter,
     *            you need for every input parameter a element of {@link ExampleData} in the list. The sequence of the
     *            example data must be identical to the sequence of the input parameters. For more than one execution
     *            you need a list of these lists.
     */
    public GeoAlgorithmWithData( GeoAlgorithm alg, LinkedList<LinkedList<ExampleData>> data,
                                 LinkedList<LinkedList<OutputFormat>> format ) {
        this.alg = alg;

        if ( data != null )
            this.data.addAll( data );

        if ( format != null )
            this.outputFormats.addAll( format );
    }

    /**
     * Adds a list of test data for one execution.
     * 
     * @param data
     *            List of test data for one execution.
     */
    public void addInputData( LinkedList<ExampleData> data ) {
        this.data.add( data );
    }

    /**
     * Adds lists of test data for more than one execution.
     * 
     * @param data
     *            A list of a list of {@link ExampleData}. If the algorithm need only one input parameter, you need only
     *            one element of {@link ExampleData} in the list. If the algorithm need more than one input parameter,
     *            you need for every input parameter a element of {@link ExampleData} in the list. The sequence of the
     *            example data must be identical to the sequence of the input parameters. For more than one execution
     *            you need a list of these lists.
     */
    public void addAllInputData( LinkedList<LinkedList<ExampleData>> data ) {
        this.data.addAll( data );
    }

    /**
     * Adds a list of output formats for one execution.
     * 
     * @param format
     *            List of output formats for one execution.
     */
    public void addOutputFormats( LinkedList<OutputFormat> format ) {
        this.outputFormats.add( format );
    }

    /**
     * Adds lists of output formats for more than one execution.
     * 
     * @param format
     *            A list of a list of {@link OutputFormat}. If the algorithm need only one output parameter, you need
     *            only one element of {@link OutputFormat} in the list. If the algorithm need more than one output
     *            parameter, you need for every output parameter a element of {@link OutputFormat} in the list. The
     *            sequence of the example data must be identical to the sequence of the output parameters. For more than
     *            one execution you need a list of these lists.
     */
    public void addAllOutputFormats( LinkedList<LinkedList<OutputFormat>> format ) {
        this.outputFormats.addAll( format );
    }

    /**
     * Returns all test data of this algorithm.
     * 
     * @return All test data. Every list of {@link VectorExampleData} contains test data for one execution.
     */
    public LinkedList<LinkedList<ExampleData>> getAllInputData() {
        return data;
    }

    /**
     * Returns all output formats of this algorithm.
     * 
     * @return All output formats. Every list of {@link OutputFormat} contains output formats for one execution.
     */
    public LinkedList<LinkedList<OutputFormat>> getAllOutputFormats() {
        return outputFormats;
    }

    /**
     * Returns the SEXTANTE {@link GeoAlgorithm}.
     * 
     * @return SEXTANTE {@link GeoAlgorithm}.
     */
    public GeoAlgorithm getAlgorithm() {
        return alg;
    }

    /**
     * Returns the identifier of this process.
     * 
     * @return Identifier of this process.
     */
    public String getIdentifier() {
        return SextanteWPSProcess.createIdentifier( alg );
    }

    /**
     * Returns the command line name of the SEXTANTE {@link GeoAlgorithm}.
     * 
     * @return Command line name of the SEXTANTE {@link GeoAlgorithm}.
     */
    public String getCommandLineName() {
        return alg.getCommandLineName();
    }

    public String toString() {
        String s = GeoAlgorithmWithData.class.getSimpleName() + "(";

        s += alg.getCommandLineName() + ", ";

        // determine number of test data
        int size = 0;
        for ( LinkedList<ExampleData> list : data ) {
            size += list.size();
        }

        s += size + " test data";

        s += ")";
        return s;
    }

}
