//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.services.wps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.services.wps.input.ProcessletInput;

/**
 * Encapsulates the input parameters for the execution of a {@link Processlet}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public final class ProcessletInputs {

    private List<ProcessletInput> inputs;

    private Map<CodeType, List<ProcessletInput>> idsToInputs = new HashMap<CodeType, List<ProcessletInput>>();

    /**
     * Creates a new {@link ProcessletInputs} instance that consists of the given input parameters.
     *
     * @param inputs
     *            input parameters
     */
    public ProcessletInputs( List<ProcessletInput> inputs ) {
        for ( ProcessletInput input : inputs ) {
            CodeType id = input.getIdentifier();
            List<ProcessletInput> inputsForId = idsToInputs.get( id );
            if ( inputsForId == null ) {
                inputsForId = new ArrayList<ProcessletInput>();
                idsToInputs.put( id, inputsForId );
            }
            inputsForId.add( input );
        }
        this.inputs = inputs;
    }

    /**
     * Returns all input parameters.
     *
     * @return all input parameters
     */
    public List<ProcessletInput> getParameters() {
        return inputs;
    }

    /**
     * Returns the input parameters with the given identifier (convenience method for identifiers with a
     * <code>null</code> codeSpace).
     *
     * @param parameterId
     *            identifier of the input parameters to be looked up
     * @return the input parameters with the given identifier
     */
    public List<ProcessletInput> getParameters( String parameterId ) {
        return idsToInputs.get( new CodeType( parameterId ) );
    }

    /**
     * Returns the input parameters with the given identifier.
     *
     * @param parameterId
     *            identifier of the input parameters to be looked up
     * @return the input parameters with the given identifier
     */
    public List<ProcessletInput> getParameters( CodeType parameterId ) {
        return idsToInputs.get( parameterId );
    }

    /**
     * Returns the input parameters with the given identifier.
     *
     * @param parameterId
     *            identifier of the input parameters to be looked up
     * @param codeSpace
     *            codeSpace of the parameter identifier
     * @return the input parameters with the given identifier
     */
    public List<ProcessletInput> getParameters( String parameterId, String codeSpace ) {
        return idsToInputs.get( new CodeType( parameterId, codeSpace ) );
    }

    /**
     * Returns the single input parameter with the given identifier (convenience method for the common case when only a
     * single parameter with a certain identifier is allowed and the identifier has a <code>null</code> codeSpace).
     *
     * @param parameterId
     *            identifier of the input parameters to be looked up
     * @return the input parameters with the given identifier, or null if no such parameter exists
     * @throws RuntimeException
     *             when more than one input parameter with the specified identifier is present
     */
    public ProcessletInput getParameter( String parameterId ) {
        List<ProcessletInput> inputs = getParameters( new CodeType( parameterId ) );
        if ( inputs == null || inputs.size() == 0 ) {
            return null;
        }
        if ( inputs.size() > 1 ) {
            String msg = "Unexpected input. More than one input parameter with identifier '" + parameterId + "'.";
            throw new RuntimeException( msg );
        }
        return inputs.get( 0 );
    }

    /**
     * Returns the single input parameter with the given identifier (convenience method for the common case when only a
     * single parameter with a certain identifier is allowed).
     *
     * @param parameterId
     *            identifier of the input parameters to be looked up
     * @return the input parameters with the given identifier, or null if no such parameter exists
     * @throws RuntimeException
     *             when more than one input parameter with the specified identifier is present
     */
    public ProcessletInput getParameter( CodeType parameterId ) {
        List<ProcessletInput> inputs = getParameters( parameterId );
        if ( inputs == null || inputs.size() == 0 ) {
            return null;
        }
        if ( inputs.size() > 1 ) {
            String msg = "Unexpected input. More than one input parameter with identifier '" + parameterId + "'.";
            throw new RuntimeException( msg );
        }
        return inputs.get( 0 );
    }

    /**
     * Returns the single input parameter with the given identifier (convenience method for the common case when only a
     * single parameter with a certain identifier is allowed).
     *
     * @param parameterId
     *            identifier of the input parameters to be looked up
     * @param codeSpace
     *            codeSpace of the parameter identifier
     * @return the input parameters with the given identifier, or null if no such parameter exists
     * @throws RuntimeException
     *             when more than one input parameter with the specified identifier is present
     */
    public ProcessletInput getParameter( String parameterId, String codeSpace ) {
        CodeType code = new CodeType( parameterId, codeSpace );
        List<ProcessletInput> inputs = getParameters( code );
        if ( inputs == null || inputs.size() == 0 ) {
            return null;
        }
        if ( inputs.size() > 1 ) {
            String msg = "Unexpected input. More than one input parameter with identifier '" + code + "'.";
            throw new RuntimeException( msg );
        }
        return inputs.get( 0 );
    }
}
