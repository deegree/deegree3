//$HeadURL$
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
package org.deegree.security.owsrequestvalidator.wms;

import java.util.List;

import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.security.owsproxy.Condition;
import org.deegree.security.owsproxy.OperationParameter;
import org.deegree.security.owsrequestvalidator.Messages;
import org.deegree.security.owsrequestvalidator.Policy;
import org.deegree.security.owsrequestvalidator.RequestValidator;

/**
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
abstract class AbstractWMSRequestValidator extends RequestValidator {

    // known condition parameter
    private static final String FORMAT = "format";

    private static final String MAXWIDTH = "maxWidth";

    private static final String MAXHEIGHT = "maxHeight";

    private static final String INVALIDFORMAT = Messages.getString( "AbstractWMSRequestValidator.INVALIDFORMAT" );

    private static final String INVALIDWIDTH1 = Messages.getString( "AbstractWMSRequestValidator.INVALIDWIDTH1" );

    private static final String INVALIDWIDTH2 = Messages.getString( "AbstractWMSRequestValidator.INVALIDWIDTH2" );

    private static final String INVALIDHEIGHT1 = Messages.getString( "AbstractWMSRequestValidator.INVALIDHEIGHT1" );

    private static final String INVALIDHEIGHT2 = Messages.getString( "AbstractWMSRequestValidator.INVALIDHEIGHT2" );

    /**
     * @param policy
     */
    public AbstractWMSRequestValidator( Policy policy ) {
        super( policy );
    }

    /**
     * checks if the passed format is valid against the formats defined in the policy. If
     * <tt>user</ff> != <tt>null</tt> the valid
     * formats will be read from the user/rights repository
     * @param condition condition containing the definition of the valid format
     * @param format
     * @throws InvalidParameterValueException
     */
    protected void validateFormat( Condition condition, String format )
                            throws InvalidParameterValueException {

        OperationParameter op = condition.getOperationParameter( FORMAT );

        // version is valid because no restrictions are made
        if ( op.isAny() ) {
            return;
        }

        List<String> list = op.getValues();

        if ( !list.contains( format ) ) {
            if ( !op.isUserCoupled() ) {
                throw new InvalidParameterValueException( INVALIDFORMAT + format );
            }
            userCoupled = true;
        }

    }

    /**
     * checks if the passed width is > 0 and if it's valid against the maxWidth defined in the
     * policy. If <tt>user</ff> != <tt>null</tt> the valid
     * width will be read from the user/rights repository
     * @param condition condition containing the definition of the valid width
     * @param width
     * @throws InvalidParameterValueException
     */
    protected void validateMaxWidth( Condition condition, int width )
                            throws InvalidParameterValueException {

        if ( width < 1 ) {
            throw new InvalidParameterValueException( INVALIDWIDTH1 + width );
        }

        OperationParameter op = condition.getOperationParameter( MAXWIDTH );

        // version is valid because no restrictions are made
        if ( op.isAny() ) {
            return;
        }

        if ( width > op.getFirstAsInt() ) {
            if ( !op.isUserCoupled() ) {
                throw new InvalidParameterValueException( INVALIDWIDTH2 + width );
            }
            userCoupled = true;
        }

    }

    /**
     * checks if the passed height is > 0 and if it's valid against the maxHeight defined in the
     * policy. If <tt>user</ff> != <tt>null</tt> the valid
     * height will be read from the user/rights repository
     * @param condition condition containing the definition of the valid height
     * @param height
     * @throws InvalidParameterValueException
     */
    protected void validateMaxHeight( Condition condition, int height )
                            throws InvalidParameterValueException {

        if ( height < 1 ) {
            throw new InvalidParameterValueException( INVALIDHEIGHT1 + height );
        }

        OperationParameter op = condition.getOperationParameter( MAXHEIGHT );

        // version is valid because no restrictions are made
        if ( op.isAny() ) {
            return;
        }

        if ( height > op.getFirstAsInt() ) {
            if ( !op.isUserCoupled() ) {
                throw new InvalidParameterValueException( INVALIDHEIGHT2 + height );
            }
            userCoupled = true;
        }
    }
}
