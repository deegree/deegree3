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

package org.deegree.ogcwebservices.csw.manager;

import java.util.List;
import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLException;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcwebservices.AbstractOGCWebServiceRequest;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.csw.CSWPropertiesAccess;
import org.w3c.dom.Element;

/**
 * A Transaction defines an atomic unit of work and is a container for one or more insert, update
 * and/or delete actions.
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class Transaction extends AbstractOGCWebServiceRequest {

    private static final long serialVersionUID = -4393029325052150570L;

    protected static final ILogger LOG = LoggerFactory.getLogger( Transaction.class );

    private static NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    private List<Operation> operations = null;

    private boolean verboseResponse = false;

    /**
     * creates a Transaction object from its XML representation defined in OGC CS-W 2.0.0
     * specification
     *
     * @param id
     *            uniquely identifies the given request
     *
     * @param root
     *            the root of the xml-encoded transaction request
     * @return a Transaction with the values taken form the XML-Document.
     * @throws OGCWebServiceException
     *             if the Transaction could not be created
     */
    public static final Transaction create( String id, Element root )
                            throws OGCWebServiceException {

        String version = null;
        try {
            // first try to read verdsion attribute which is optional for CSW 2.0.0 and 2.0.1
            version = XMLTools.getNodeAsString( root, "./@version", nsContext, null );
        } catch ( XMLParsingException e ) {
            // ignored
        }
        if ( version == null ) {
            // if no version attribute has been set try mapping namespace URI to a version;
            // this is not well defined for 2.0.0 and 2.0.1 which uses the same namespace.
            // in this case 2.0.0 will be returned!
            version = CSWPropertiesAccess.getString( root.getNamespaceURI() );
        }

        // read class for version depenging parsing of Transaction request from properties
        String className = CSWPropertiesAccess.getString( "Transaction" + version );
        Class<?> clzz = null;
        try {
            clzz = Class.forName( className );
        } catch ( ClassNotFoundException e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidParameterValueException( e.getMessage(), e );
        }
        TransactionDocument document = null;
        try {
            document = (TransactionDocument) clzz.newInstance();
        } catch ( InstantiationException e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidParameterValueException( e.getMessage(), e );
        } catch ( IllegalAccessException e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidParameterValueException( e.getMessage(), e );
        }

        document.setRootElement( root );

        try {
            return document.parse( id );
        } catch ( XMLException e ) {
            String msg = Messages.getMessage( "CSW_ERROR_WHILE_PARSING_TRANSACTION", e.getMessage() );
            LOG.logError( msg, e );
            throw new OGCWebServiceException( msg, ExceptionCode.INVALID_FORMAT );
        } catch ( XMLParsingException e ) {
            String msg = Messages.getMessage( "CSW_ERROR_WHILE_PARSING_TRANSACTION", e.getMessage() );
            LOG.logError( msg, e );
            throw new OGCWebServiceException( msg, ExceptionCode.INVALID_FORMAT );
        }
    }

    /**
     *
     * @param version
     * @param id
     * @param vendorSpecificParameter
     * @param operations
     * @param verboseResponse
     */
    public Transaction( String version, String id, Map<String, String> vendorSpecificParameter,
                        List<Operation> operations, boolean verboseResponse ) {
        super( version, id, vendorSpecificParameter );
        this.operations = operations;
        this.verboseResponse = verboseResponse;
    }

    /**
     * @return the name of the service; always CSW
     */
    public String getServiceName() {
        return "CSW";
    }

    /**
     * The verboseResponseattribute is a boolean that may be used by a client to indicate to a
     * server the amount of detail to generate in the rsponse. A value of FALSE means that a CSW
     * should generate a terse or brief transaction response. A value of TRUE, or the absence of the
     * attribute, means that the normal detailed transaction response should be generated.
     *
     * @return true if the response should be verbose
     */
    public boolean verboseResponse() {
        return verboseResponse;
    }

    /**
     * returns all operations being part of a transaction
     *
     * @return all operations being part of a transaction
     */
    public List<Operation> getOperations() {
        return operations;
    }
}
