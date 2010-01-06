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
package org.deegree.record.publication;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.protocol.csw.CSWConstants.TransactionType;
import org.deegree.record.publication.TransactionOperation;

/**
 * Represents a CSW <code>Insert</code> operation (part of a {@link Transaction} request).
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class InsertTransaction extends TransactionOperation {

    private List<OMElement> element;

    private QName typeName;

    /**
     * @param handle
     */
    public InsertTransaction( List<OMElement> transChildElementInsertAPISORecordList, QName typeName, String handle ) {
        super( handle );
        this.element = transChildElementInsertAPISORecordList;
        this.typeName = typeName;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.services.controller.csw.transaction.TransactionOperation#getType()
     */
    @Override
    public TransactionType getType() {

        return TransactionType.INSERT;
    }

    /**
     * @return the element
     */
    public List<OMElement> getElement() {
        return element;
    }

    /**
     * @param element
     *            the element to set
     */
    public void setElement( List<OMElement> element ) {
        this.element = element;
    }

    /**
     * @return the typeName
     */
    public QName getTypeName() {
        return typeName;
    }

    /**
     * @param typeName
     *            the typeName to set
     */
    public void setTypeName( QName typeName ) {
        this.typeName = typeName;
    }

}
