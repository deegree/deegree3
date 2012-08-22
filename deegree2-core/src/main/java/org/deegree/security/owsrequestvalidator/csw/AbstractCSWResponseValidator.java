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


package org.deegree.security.owsrequestvalidator.csw;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.deegree.framework.xml.XMLFragment;
import org.deegree.model.filterencoding.AbstractFilter;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.FilterConstructionException;
import org.deegree.model.filterencoding.Literal;
import org.deegree.model.filterencoding.LogicalOperation;
import org.deegree.model.filterencoding.Operation;
import org.deegree.model.filterencoding.OperationDefines;
import org.deegree.model.filterencoding.PropertyIsCOMPOperation;
import org.deegree.model.filterencoding.PropertyName;
import org.deegree.security.owsrequestvalidator.Policy;
import org.deegree.security.owsrequestvalidator.ResponseValidator;
import org.xml.sax.SAXException;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
abstract class AbstractCSWResponseValidator extends ResponseValidator {

    private static final String PROPERTY_INSTANCEFILTER = "instanceFilter";

    /**
     *
     * @param policy
     */
    public AbstractCSWResponseValidator( Policy policy ) {
        super( policy );
    }

    /**
     *
     * @param operation
     * @return the extracted filter
     * @throws IOException
     * @throws SAXException
     * @throws FilterConstructionException
     */
    protected ComplexFilter extractInstanceFilter( Operation operation, List<ComplexFilter> foundFilters )
                            throws SAXException, IOException, FilterConstructionException {
        ComplexFilter filter = null;
        if ( operation.getOperatorId() == OperationDefines.AND || operation.getOperatorId() == OperationDefines.OR ) {
            List<Operation> arguments = ( (LogicalOperation) operation ).getArguments();
            for ( int i = 0; i < arguments.size(); i++ ) {
                Operation op = arguments.get( i );
                filter = extractInstanceFilter( op, foundFilters );
                if ( filter != null ) {
                  //  foundFilters.add( filter );
                }
                /*
                 * if ( op.getOperatorId() == OperationDefines.PROPERTYISEQUALTO ) { PropertyName pn = (PropertyName) (
                 * (PropertyIsCOMPOperation) op ).getFirstExpression(); if ( PROPERTY_INSTANCEFILTER.equals(
                 * pn.getValue().getAsString() ) ) { Literal literal = (Literal) ( (PropertyIsCOMPOperation) op
                 * ).getSecondExpression(); StringReader sr = new StringReader( literal.getValue() ); XMLFragment xml =
                 * new XMLFragment( sr, XMLFragment.DEFAULT_URL ); filter = (ComplexFilter) AbstractFilter.buildFromDOM(
                 * xml.getRootElement() ); } }
                 */
            }
        } else {
            if ( operation.getOperatorId() == OperationDefines.PROPERTYISEQUALTO ) {
                PropertyName pn = (PropertyName) ( (PropertyIsCOMPOperation) operation ).getFirstExpression();
                if ( PROPERTY_INSTANCEFILTER.equals( pn.getValue().getAsString() ) ) {
                    Literal literal = (Literal) ( (PropertyIsCOMPOperation) operation ).getSecondExpression();
                    StringReader sr = new StringReader( literal.getValue() );
                    XMLFragment xml = new XMLFragment( sr, XMLFragment.DEFAULT_URL );
                    filter = (ComplexFilter) AbstractFilter.buildFromDOM( xml.getRootElement(), false );
                    if ( filter != null ) {
                        foundFilters.add( filter );
                    }
                }
            }
        }
        return filter;
    }

}
