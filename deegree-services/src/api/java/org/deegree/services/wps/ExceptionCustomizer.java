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

import java.util.List;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.utils.Pair;
import org.deegree.services.controller.ows.OWSException;

/**
 * The <code>ExceptionCustomizer</code> can be implemented to let a {@link Processlet} create an appropriate
 * {@link Exception} for an invalid (validation) status.
 * 
 * @see ExceptionAwareProcesslet
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface ExceptionCustomizer {

    public OWSException missingParameter( String parameter );

    public OWSException missingParameters( String... parameters );

    public OWSException mutualExclusive( String parameter, String excludes );

    public OWSException inputMutualExclusive( CodeType inputParameterId, String parameter, String excludes );

    public OWSException inputNoSuchParameter( CodeType identifier );

    public OWSException inputInvalidDatatype( CodeType inputParameterId, String foundDatatype, String definedDataType );

    public OWSException inputInvalidParameter( CodeType inputParameterId, Pair<String, String> kvp );

    public OWSException inputMissingParameter( CodeType inputParameterId, String parameter );

    public OWSException inputMissingParameters( CodeType inputParameterId, String... parameters );

    public OWSException inputInvalidBBoxCoordinates( CodeType inputParameterId, String[] suppliedCoordinates );

    public OWSException inputEvalutationNotSupported( CodeType inputParameterId, Pair<String, String> kvp,
                                                      String explanation );

    public OWSException inputInvalidCombination( CodeType inputParameterId, List<Pair<String, String>> conflictingKVPs );

    public OWSException inputInvalidOccurence( CodeType inputParameterId, int minOccurs, int maxOccurs, int actualOccurs );

    public OWSException outputNoSuchParameter( CodeType outputParameterId );

    public OWSException outputInvalidParameter( CodeType outputParameterId, Pair<String, String> kvp );

    public OWSException outputInvalidCombination( CodeType outputParameterId, List<Pair<String, String>> conflictingKVPs );

    public OWSException invalidAttributedParameter( Pair<String, String> kvp );

}
