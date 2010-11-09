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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.services.jaxb.wps.BoundingBoxOutputDefinition;
import org.deegree.services.jaxb.wps.ComplexOutputDefinition;
import org.deegree.services.jaxb.wps.LiteralOutputDefinition;
import org.deegree.services.jaxb.wps.ProcessDefinition;
import org.deegree.services.jaxb.wps.ProcessletOutputDefinition;
import org.deegree.services.wps.output.BoundingBoxOutputImpl;
import org.deegree.services.wps.output.ComplexOutputImpl;
import org.deegree.services.wps.output.LiteralOutputImpl;
import org.deegree.services.wps.output.ProcessletOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates the output parameters for the execution of a {@link Processlet}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public class ProcessletOutputs {

    private static final Logger LOG = LoggerFactory.getLogger( ProcessletOutputs.class );

    private Map<CodeType, ProcessletOutput> idToOutput = new HashMap<CodeType, ProcessletOutput>();

    /**
     * @param processDefinition
     * @param requestedOutputs
     */
    public ProcessletOutputs( ProcessDefinition processDefinition,
                              Collection<? extends ProcessletOutput> requestedOutputs ) {
        for ( ProcessletOutput processOutput : requestedOutputs ) {
            idToOutput.put( processOutput.getIdentifier(), processOutput );
        }

        // create dummy output sinks for unrequested output parameters
        for ( JAXBElement<? extends ProcessletOutputDefinition> outputElement : processDefinition.getOutputParameters().getProcessOutput() ) {
            ProcessletOutputDefinition outputDef = outputElement.getValue();
            CodeType outputId = new CodeType( outputDef.getIdentifier().getValue(),
                                              outputDef.getIdentifier().getCodeSpace() );

            ProcessletOutput output = idToOutput.get( outputId );
            if ( output == null ) {
                LOG.debug( "Output '" + outputId + "' is not requested, providing a dummy output parameter object." );
                if ( outputDef instanceof LiteralOutputDefinition ) {
                    LiteralOutputDefinition literalDef = (LiteralOutputDefinition) outputDef;
                    String uom = literalDef.getDefaultUOM() != null ? literalDef.getDefaultUOM().getValue() : null;
                    output = new LiteralOutputImpl( literalDef, uom, false );
                } else if ( outputDef instanceof BoundingBoxOutputDefinition ) {
                    output = new BoundingBoxOutputImpl( (BoundingBoxOutputDefinition) outputDef, false );
                } else if ( outputDef instanceof ComplexOutputDefinition ) {
                    output = new ComplexOutputImpl( (ComplexOutputDefinition) outputDef, new OutputStream() {
                        @Override
                        public void write( int b )
                                                throws IOException {
                            // nothing to do, goes to nil
                        }
                    }, false, null, null, null );
                }
                idToOutput.put( outputId, output );
            }
        }
    }

    /**
     * Returns all output parameters.
     *
     * @return all output parameters
     */
    public Collection<ProcessletOutput> getParameters() {
        return idToOutput.values();
    }

    /**
     * Returns the output parameters with the given identifier (convenience method for identifiers with a
     * <code>null</code> codeSpace).
     *
     * @param parameterId
     *            identifier of the output parameters to be looked up
     * @return the output parameters with the given identifier
     */
    public ProcessletOutput getParameter( String parameterId ) {
        return idToOutput.get( new CodeType( parameterId ) );
    }

    /**
     * Returns the output parameters with the given identifier.
     *
     * @param parameterId
     *            identifier of the output parameters to be looked up
     * @param codeSpace
     *            codeSpace of the parameter identifier
     * @return the output parameters with the given identifier
     */
    public ProcessletOutput getParameter( String parameterId, String codeSpace ) {
        return idToOutput.get( new CodeType( parameterId, codeSpace ) );
    }

    /**
     * Returns the output parameter with the given identifier.
     *
     * @param parameterId
     *            identifier of the output parameter to be looked up
     * @return the output parameter with the given identifier
     */
    public ProcessletOutput getParameter( CodeType parameterId ) {
        return idToOutput.get( parameterId );
    }
}
