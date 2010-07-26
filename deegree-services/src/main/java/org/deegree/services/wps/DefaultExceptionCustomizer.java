//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.services.wps;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.utils.Pair;
import org.deegree.services.controller.exception.ControllerException;
import org.deegree.services.controller.ows.OWSException;

/**
 * The <code>ExceptionCustomizerImpl</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class DefaultExceptionCustomizer implements ExceptionCustomizer {

    private final String processMsg;

    /**
     * The default exception customizer
     * 
     * @param processId
     *            of the process this exception customizer is used for.
     */
    public DefaultExceptionCustomizer( CodeType processId ) {
        if ( processId != null ) {
            this.processMsg = "Process (" + processId + ") ";
        } else {
            this.processMsg = "";
        }

    }

    // @Override
    // public OWSException inputInvalidParameter( Pair<String, String> input ) {
    // return new OWSException( processMsg + ", the input " + input + " is invalid.",
    // OWSException.INVALID_PARAMETER_VALUE );
    // }

    @Override
    public OWSException missingParameter( String parameter ) {
        return new OWSException( processMsg + " the required parameter: " + parameter + " was missing.",
                                 ControllerException.NO_APPLICABLE_CODE );
    }

    @Override
    public OWSException missingParameters( String... parameters ) {
        return new OWSException( processMsg + " At least one of following parameters is required but missing: "
                                 + Arrays.toString( parameters ) + ".", ControllerException.NO_APPLICABLE_CODE );
    }

    @Override
    public OWSException mutualExclusive( String parameter, String excludes ) {
        return new OWSException( processMsg + ": given parameters '" + parameter + "' and '" + excludes
                                 + "' are mutually exclusive.", OWSException.INVALID_PARAMETER_VALUE );
    }

    @Override
    public OWSException inputMutualExclusive( CodeType identifier, String parameter, String excludes ) {
        return new OWSException( processMsg + "Input parameter" + identifier + " defines '" + parameter + "' and '"
                                 + excludes + "' which are mutually exclusive.", OWSException.INVALID_PARAMETER_VALUE );
    }

    @Override
    public OWSException inputNoSuchParameter( CodeType identifier ) {
        String msg = processMsg + " has no input parameter with identifier '" + identifier + "'.";
        return new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, identifier.getCode() );
    }

    @Override
    public OWSException inputInvalidDatatype( CodeType parameterId, String foundDatatype, String definedDataType ) {
        String msg = processMsg + ", the value of datatype attribute (='" + foundDatatype + "') for input parameter '"
                     + parameterId + "' does not match the datatype from the corresponding parameter definition (='"
                     + definedDataType + "')";
        return new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, parameterId.toString() );
    }

    @Override
    public OWSException inputInvalidParameter( CodeType inputParameterId, Pair<String, String> kvp ) {
        String msg = processMsg + "Given value: " + kvp + " of input parameter '" + inputParameterId
                     + "' is not supported according the corresponding parameter definition.";
        return new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, inputParameterId.toString() );
    }

    @Override
    public OWSException inputInvalidBBoxCoordinates( CodeType inputParameterId, String[] suppliedCoordinates ) {
        String msg = processMsg + ", values for bounding box input parameter '" + inputParameterId + "' (='"
                     + Arrays.toString( suppliedCoordinates ) + "') does not specify enough coordinates.";
        return new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, inputParameterId.toString() );
    }

    @Override
    public OWSException inputEvalutationNotSupported( CodeType inputParameterId, Pair<String, String> kvp,
                                                      String explanation ) {
        String msg = "Evaluating " + kvp + " for input parameter '" + inputParameterId
                     + "' is not supported, because: " + explanation;
        return new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, inputParameterId.toString() );

    }

    @Override
    public OWSException inputInvalidCombination( CodeType inputParameterId, List<Pair<String, String>> conflictingKVPs ) {
        StringBuilder sb = new StringBuilder( processMsg );
        sb.append( "The combination of" );
        Iterator<Pair<String, String>> it = conflictingKVPs.iterator();
        while ( it.hasNext() ) {
            sb.append( it.next() );
            if ( it.hasNext() ) {
                sb.append( " and " );
            }
        }
        sb.append( " is not supported for input parameter '" ).append( inputParameterId );
        sb.append( "' according to the corresponding parameter definition." );
        return new OWSException( sb.toString(), OWSException.INVALID_PARAMETER_VALUE, inputParameterId.toString() );

    }

    @Override
    public OWSException inputInvalidOccurence( CodeType inputParameterId, int minOccurs, int maxOccurs, int actualOccurs ) {
        StringBuilder msg = new StringBuilder( processMsg );
        msg.append( "Input parameter '" ).append( inputParameterId ).append( "' is present " ).append( actualOccurs ).append(
                                                                                                                              " times." );
        if ( minOccurs < actualOccurs ) {
            msg.append( "but at least " ).append( minOccurs );
            msg.append( " occurrence(s) is/are required" );
        } else {
            msg.append( "but only " ).append( maxOccurs );
            msg.append( " occurrence(s) is/are allowed" );
        }
        msg.append( "according to the corresponding parameter definition." );

        return new OWSException( msg.toString(), OWSException.INVALID_PARAMETER_VALUE, inputParameterId.toString() );

    }

    @Override
    public OWSException outputNoSuchParameter( CodeType outputParameterId ) {
        String msg = processMsg + " has no output parameter with identifier '" + outputParameterId + "'.";
        return new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, outputParameterId.getCode() );
    }

    @Override
    public OWSException outputInvalidParameter( CodeType outputParameterId, Pair<String, String> kvp ) {
        String msg = processMsg + "Given value: " + kvp + " of outputparameter '" + outputParameterId
                     + "' is not supported according the corresponding parameter definition.";
        return new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, outputParameterId.toString() );
    }

    @Override
    public OWSException outputInvalidCombination( CodeType outputParameterId, List<Pair<String, String>> conflictingKVPs ) {
        StringBuilder sb = new StringBuilder( processMsg );
        sb.append( "The combination of" );
        Iterator<Pair<String, String>> it = conflictingKVPs.iterator();
        while ( it.hasNext() ) {
            sb.append( it.next() );
            if ( it.hasNext() ) {
                sb.append( " and " );
            }
        }
        sb.append( " is not supported for output parameter '" ).append( outputParameterId );
        sb.append( "' according to the corresponding parameter definition." );
        return new OWSException( sb.toString(), OWSException.INVALID_PARAMETER_VALUE, outputParameterId.toString() );
    }

    @Override
    public OWSException invalidAttributedParameter( Pair<String, String> kvp ) {
        return new OWSException( processMsg + ", the parameter" + kvp.first + " has an invalid value: " + kvp.second,
                                 OWSException.INVALID_PARAMETER_VALUE );
    }

    @Override
    public OWSException inputMissingParameter( CodeType inputParameterId, String parameter ) {
        return new OWSException( processMsg + " the required parameter: " + parameter + " of inputparameter: "
                                 + inputParameterId + " was missing.", OWSException.MISSING_PARAMETER_VALUE );
    }

    @Override
    public OWSException inputMissingParameters( CodeType inputParameterId, String... parameters ) {
        return new OWSException( processMsg + " missing parameters for input" + inputParameterId
                                 + ". At least one of following parameters is required but missing: "
                                 + Arrays.toString( parameters ) + ".", ControllerException.NO_APPLICABLE_CODE );
    }
}
