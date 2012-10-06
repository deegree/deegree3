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
package org.deegree.protocol.wps.client.process.execute;

import java.util.LinkedHashMap;
import java.util.Map;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.protocol.wps.client.output.BBoxOutput;
import org.deegree.protocol.wps.client.output.ComplexOutput;
import org.deegree.protocol.wps.client.output.ExecutionOutput;
import org.deegree.protocol.wps.client.output.LiteralOutput;
import org.deegree.protocol.wps.client.process.ProcessExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to the outputs from a {@link ProcessExecution}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ExecutionOutputs {

    private static final Logger LOG = LoggerFactory.getLogger( ExecutionOutputs.class );

    private final Map<CodeType, ExecutionOutput> paramIdToOutput = new LinkedHashMap<CodeType, ExecutionOutput>();

    /**
     * Creates a new {@link ExecutionOutputs} instance.
     * 
     * @param outputs
     *            output values, never <code>null</code>
     */
    public ExecutionOutputs( ExecutionOutput[] outputs ) {
        for ( ExecutionOutput output : outputs ) {
            LOG.debug( "Output: " + output.getId() + ": " + output );
            paramIdToOutput.put( output.getId(), output );
        }
    }

    /**
     * Returns all outputs.
     * 
     * @return all outputs, never <code>null</code>
     */
    public ExecutionOutput[] getAll() {
        return paramIdToOutput.values().toArray( new ExecutionOutput[paramIdToOutput.size()] );
    }

    /**
     * Returns the output with the specified index.
     * 
     * @param i
     *            index of the output, starting with zero
     * @return the output with the specified index, never <code>null</code>
     * @throws IndexOutOfBoundsException
     *             if no such output exists
     */
    public ExecutionOutput get( int i ) {
        return getAll()[i];
    }

    /**
     * Returns the output with the specified identifier.
     * 
     * @param id
     *            output identifier, never <code>null</code>
     * @param idCodeSpace
     *            codespace of the output identifier, may be <code>null</code> (for identifiers that don't use a code
     *            space)
     * @return output with the specified identifier, may be <code>null</code> (if no such output exists)
     */
    public ExecutionOutput get( String id, String idCodeSpace ) {
        return paramIdToOutput.get( new CodeType( id, idCodeSpace ) );
    }

    /**
     * Returns the literal output with the specified identifier.
     * 
     * @param id
     *            output identifier, never <code>null</code>
     * @param idCodeSpace
     *            codespace of the output identifier, may be <code>null</code> (for identifiers that don't use a code
     *            space)
     * @return output with the specified identifier, may be <code>null</code> (if no such output exists)
     */
    public LiteralOutput getLiteral( String id, String idCodeSpace ) {
        return (LiteralOutput) paramIdToOutput.get( new CodeType( id, idCodeSpace ) );
    }

    /**
     * Returns the bounding box output with the specified identifier.
     * 
     * @param id
     *            output identifier, never <code>null</code>
     * @param idCodeSpace
     *            codespace of the output identifier, may be <code>null</code> (for identifiers that don't use a code
     *            space)
     * @return output with the specified identifier, may be <code>null</code> (if no such output exists)
     */
    public BBoxOutput getBoundingBox( String id, String idCodeSpace ) {
        return (BBoxOutput) paramIdToOutput.get( new CodeType( id, idCodeSpace ) );
    }

    /**
     * Returns the complex output with the specified identifier.
     * 
     * @param id
     *            output identifier, never <code>null</code>
     * @param idCodeSpace
     *            codespace of the output identifier, may be <code>null</code> (for identifiers that don't use a code
     *            space)
     * @return output with the specified identifier, may be <code>null</code> (if no such output exists)
     */
    public ComplexOutput getComplex( String id, String idCodeSpace ) {
        return (ComplexOutput) paramIdToOutput.get( new CodeType( id, idCodeSpace ) );
    }
}
