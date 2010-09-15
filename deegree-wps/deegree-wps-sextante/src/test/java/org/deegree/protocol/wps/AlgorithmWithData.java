//$HeadURL: http://svn.wald.intevation.org/svn/deegree/base/trunk/resources/eclipse/files_template.xml $
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
import es.unex.sextante.core.GeoAlgorithm;

/**
 * This class wraps a {@link GeoAlgorithm} with his test data.
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * @author last edited by: $Author: pabel $
 * 
 * @version $Revision: $, $Date: $
 */
public class AlgorithmWithData {

    // SEXTANTE algorithm
    private final GeoAlgorithm alg;

    // test data for the algorithm
    private final LinkedList<LinkedList<ExampleData>> data = new LinkedList<LinkedList<ExampleData>>();

    /**
     * Creates a {@link AlgorithmWithData} without test data.
     * 
     * @param alg
     *            - SEXTANTE {@link GeoAlgorithm}.
     */
    public AlgorithmWithData( GeoAlgorithm alg ) {
        this( alg, null );
    }

    /**
     * Creates a {@link AlgorithmWithData} with test data.
     * 
     * @param alg
     *            - SEXTANTE {@link GeoAlgorithm}.
     * @param data
     *            - A list of a list of test data. If the algorithm need only one input parameter, you need only one
     *            list of test data in the list. If the algorithm need more than one input parameter, you need for every
     *            input parameter a list of test data in the list.
     */
    public AlgorithmWithData( GeoAlgorithm alg, LinkedList<LinkedList<ExampleData>> data ) {
        this.alg = alg;

        if ( data != null )
            this.data.addAll( data );

    }

    /**
     * Adds a list of test data for one input parameter.
     * 
     * @param data
     *            - List of test data for one input parameter.
     */
    public void addInputData( LinkedList<ExampleData> data ) {
        this.data.add( data );
    }

    /**
     * Adds lists of test data for more than one input parameters.
     * 
     * @param data
     *            - A list of a list of test data. If the algorithm need only one input parameter, you need only one
     *            list of test data in the list. If the algorithm need more than one input parameter, you need for every
     *            input parameter a list of test data in the list.
     */
    public void addAllInputData( LinkedList<LinkedList<ExampleData>> data ) {
        this.data.addAll( data );
    }

    /**
     * Returns all test data of this algorithm.
     * 
     * @return All test data. Every list of {@link GeometryExampleData} contains test data for one input parameter.
     */
    public LinkedList<LinkedList<ExampleData>> getAllInputData() {
        return data;
    }

    /**
     * Returns the SEXTANTE {@link GeoAlgorithm}.
     * 
     * @return SEXTANTE {@link GeoAlgorithm}.
     */
    public GeoAlgorithm getAlgorithm() {
        return alg;
    }

    public String toString() {
        String s = AlgorithmWithData.class.getSimpleName() + "(";

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
