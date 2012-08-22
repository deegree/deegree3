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
package org.deegree.security.owsrequestvalidator.wfs;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.i18n.Messages;
import org.deegree.model.filterencoding.AbstractFilter;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.FilterConstructionException;
import org.deegree.model.filterencoding.Literal;
import org.deegree.model.filterencoding.LogicalOperation;
import org.deegree.model.filterencoding.Operation;
import org.deegree.model.filterencoding.OperationDefines;
import org.deegree.model.filterencoding.PropertyIsCOMPOperation;
import org.deegree.model.filterencoding.PropertyName;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.security.owsproxy.Condition;
import org.deegree.security.owsproxy.OperationParameter;
import org.deegree.security.owsrequestvalidator.Policy;
import org.deegree.security.owsrequestvalidator.RequestValidator;
import org.xml.sax.SAXException;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
abstract class AbstractWFSRequestValidator extends RequestValidator {

    private static final ILogger LOG = LoggerFactory.getLogger( AbstractWFSRequestValidator.class );

    // known condition parameter
    private static final String FEATURETYPES = "featureTypes";

    private static final String PROPERTY_INSTANCEFILTER = "instanceFilter";

    /**
     * @param policy
     */
    public AbstractWFSRequestValidator( Policy policy ) {
        super( policy );
    }

    /**
     * validates if the requested info featuretypes are valid against the policy/condition. If the
     * passed user <> null this is checked against the user- and rights-management system/repository
     *
     * @param condition
     * @param featureTypes
     * @throws InvalidParameterValueException
     */
    protected void validateFeatureTypes( Condition condition, String[] featureTypes )
                            throws InvalidParameterValueException {

        OperationParameter op = condition.getOperationParameter( FEATURETYPES );

        if ( op == null ) {
            LOG.logWarning( "Did you forget to add a featureTypes parameter to the precondition?" );
            return;
        }

        // version is valid because no restrictions are made
        if ( op.isAny() )
            return;

        List<String> validLayers = op.getValues();
        if ( op.isUserCoupled() ) {
            userCoupled = true;
        } else {
            for ( int i = 0; i < featureTypes.length; i++ ) {
                LOG.logDebug( "validating feature type: ", featureTypes[i] );
                if ( !validLayers.contains( featureTypes[i] ) ) {
                    String s = Messages.getMessage( "OWSPROXY_NOT_ALLOWED_FEATURETYPE", "access", featureTypes[i] );
                    throw new InvalidParameterValueException( s );
                }
            }
        }
    }

    /**
     *
     * @param operation
     * @return the filter defined for the given operation or <code>null</code> if no such filter
     *         is defined.
     * @throws IOException
     * @throws SAXException
     * @throws FilterConstructionException
     */
    protected ComplexFilter extractInstanceFilter( Operation operation )
                            throws SAXException, IOException, FilterConstructionException {
        ComplexFilter filter = null;
        if ( operation.getOperatorId() == OperationDefines.AND ) {
            List<Operation> arguments = ( (LogicalOperation) operation ).getArguments();
            for ( int i = 0; i < arguments.size(); i++ ) {
                Operation op = arguments.get( i );
                if ( op.getOperatorId() == OperationDefines.PROPERTYISEQUALTO ) {
                    PropertyName pn = (PropertyName) ( (PropertyIsCOMPOperation) op ).getFirstExpression();
                    if ( PROPERTY_INSTANCEFILTER.equals( pn.getValue().getAsString() ) ) {
                        Literal literal = (Literal) ( (PropertyIsCOMPOperation) op ).getSecondExpression();
                        StringReader sr = new StringReader( literal.getValue() );
                        XMLFragment xml = new XMLFragment( sr, XMLFragment.DEFAULT_URL );
                        filter = (ComplexFilter) AbstractFilter.buildFromDOM( xml.getRootElement(), false );
                    }
                }
            }
        } else if ( operation.getOperatorId() == OperationDefines.PROPERTYISEQUALTO ) {
            PropertyName pn = (PropertyName) ( (PropertyIsCOMPOperation) operation ).getFirstExpression();
            if ( PROPERTY_INSTANCEFILTER.equals( pn.getValue().getAsString() ) ) {
                Literal literal = (Literal) ( (PropertyIsCOMPOperation) operation ).getSecondExpression();
                StringReader sr = new StringReader( literal.getValue() );
                XMLFragment xml = new XMLFragment( sr, XMLFragment.DEFAULT_URL );
                filter = (ComplexFilter) AbstractFilter.buildFromDOM( xml.getRootElement(), false );
            }
        }
        return filter;
    }
}
