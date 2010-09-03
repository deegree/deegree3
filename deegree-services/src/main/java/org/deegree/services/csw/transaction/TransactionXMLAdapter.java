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
package org.deegree.services.csw.transaction;

import static org.deegree.commons.xml.CommonNamespaces.OGCNS;
import static org.deegree.protocol.csw.CSWConstants.VERSION_202;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.filter.Filter;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.protocol.csw.CSWConstants.TransactionType;
import org.deegree.protocol.i18n.Messages;
import org.deegree.record.publication.DeleteTransaction;
import org.deegree.record.publication.InsertTransaction;
import org.deegree.record.publication.RecordProperty;
import org.deegree.record.publication.TransactionOperation;
import org.deegree.record.publication.UpdateTransaction;
import org.deegree.services.csw.AbstractCSWRequestXMLAdapter;

/**
 * Adapter between XML encoded <code>Transaction</code> requests and {@link Transaction} objects.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class TransactionXMLAdapter extends AbstractCSWRequestXMLAdapter {

    /**
     * Parses the {@link Transaction} XML request by deciding which version has to be parsed because of the requested
     * version.
     * 
     * @param version
     * @return {@link Transaction}
     */
    public Transaction parse( Version version ) {

        if ( version == null ) {
            version = Version.parseVersion( getRequiredNodeAsString( rootElement, new XPath( "@version", nsContext ) ) );
        }

        Transaction result = null;

        if ( VERSION_202.equals( version ) ) {
            result = parse202();
        } else {
            String msg = Messages.get( "UNSUPPORTED_VERSION", version, Version.getVersionsString( VERSION_202 ) );
            throw new InvalidParameterValueException( msg );
        }

        return result;
    }

    /**
     * Parses the {@link Transaction} request on basis of CSW version 2.0.2
     * 
     * @return {@link Transaction}
     */
    private Transaction parse202() {

        String requestId = getNodeAsString( rootElement, new XPath( "@requestId", nsContext ), null );

        boolean verboseRequest = getNodeAsBoolean( rootElement, new XPath( "@verboseRequest", nsContext ), false );

        List<OMElement> transChildElements = getRequiredElements( rootElement, new XPath( "*", nsContext ) );

        List<TransactionOperation> operations = new ArrayList<TransactionOperation>();

        for ( OMElement transChildElement : transChildElements ) {
            TransactionType type = null;
            QName typeName = null;
            String handle = null;
            String transType = transChildElement.getLocalName();

            if ( transType.equalsIgnoreCase( TransactionType.INSERT.name() ) ) {
                type = TransactionType.INSERT;
            } else if ( transType.equalsIgnoreCase( TransactionType.DELETE.name() ) ) {
                type = TransactionType.DELETE;
            } else if ( transType.equalsIgnoreCase( TransactionType.UPDATE.name() ) ) {
                type = TransactionType.UPDATE;
            }

            switch ( type ) {

            case INSERT:

                List<OMElement> transChildElementInsert = getRequiredElements( transChildElement, new XPath( "*",
                                                                                                             nsContext ) );
                typeName = getNodeAsQName( transChildElement, new XPath( "@typeName", nsContext ), null );
                handle = getNodeAsString( transChildElement, new XPath( "@handle", nsContext ), null );

                operations.add( new InsertTransaction( transChildElementInsert, typeName, handle ) );
                break;

            case DELETE:
                typeName = getNodeAsQName( transChildElement, new XPath( "@typeName", nsContext ), null );
                handle = getNodeAsString( transChildElement, new XPath( "@handle", nsContext ), null );

                Filter constraintDelete = null;

                OMElement transChildElementDelete = getRequiredElement( transChildElement, new XPath( "*", nsContext ) );

                Version versionConstraint = getRequiredNodeAsVersion( transChildElementDelete, new XPath( "@version",
                                                                                                          nsContext ) );

                OMElement filterEl = transChildElementDelete.getFirstChildWithName( new QName( OGCNS, "Filter" ) );

                // at the moment there is no cql parsing support
                OMElement cqlTextEl = transChildElementDelete.getFirstChildWithName( new QName( "", "CQLTEXT" ) );

                try {
                    // TODO remove usage of wrapper (necessary at the moment to work around problems
                    // with AXIOM's

                    XMLStreamReader xmlStream = new XMLStreamReaderWrapper(
                                                                            filterEl.getXMLStreamReaderWithoutCaching(),
                                                                            null );
                    // skip START_DOCUMENT
                    xmlStream.nextTag();

                    if ( versionConstraint.equals( new Version( 1, 1, 0 ) ) ) {

                        constraintDelete = Filter110XMLDecoder.parse( xmlStream );
                        System.out.println( constraintDelete );

                    } else {
                        String msg = Messages.get( "FILTER_VERSION NOT SPECIFIED", versionConstraint,
                                                   Version.getVersionsString( new Version( 1, 1, 0 ) ) );
                        throw new InvalidParameterValueException( msg );
                    }
                } catch ( XMLStreamException e ) {
                    e.printStackTrace();
                    throw new XMLParsingException( this, filterEl, e.getMessage() );
                }

                operations.add( new DeleteTransaction( handle, typeName, constraintDelete ) );
                break;

            case UPDATE:

                OMElement transChildElementUpdate = null;
                List<RecordProperty> recordProperties = null;
                Filter constraintUpdate = null;
                List<OMElement> recordPropertyElements = getElements( transChildElement,
                                                                      new XPath( "//csw:RecordProperty", nsContext ) );

                if ( recordPropertyElements.size() != 0 ) {

                    RecordProperty recordProperty = null;
                    recordProperties = new ArrayList<RecordProperty>();
                    for ( OMElement recordPropertyElement : recordPropertyElements ) {
                        QName name = getRequiredNodeAsQName( recordPropertyElement, new XPath( "Name", nsContext ) );
                        // String name = getRequiredNodeAsString( recordPropertyElement, new XPath( "Name", nsContext )
                        // );
                        String value = getNodeAsString( recordPropertyElement, new XPath( "Value", nsContext ), null );

                        recordProperty = new RecordProperty( new PropertyName( name ), new Literal( value ) );
                        recordProperties.add( recordProperty );
                    }

                    Version versionConstraintUpdate = getRequiredNodeAsVersion( transChildElement,
                                                                                new XPath( "//csw:Constraint/@version",
                                                                                           nsContext ) );

                    OMElement filterElUpdate = (OMElement) getNode(
                                                                    transChildElement,
                                                                    new XPath( "//csw:Constraint/ogc:Filter", nsContext ) );
                    // OMElement cqlTextElUpdate = transChildElement.getFirstChildWithName( new QName( "", "CQLTEXT" )
                    // );

                    try {
                        // TODO remove usage of wrapper (necessary at the moment to work around problems
                        // with AXIOM's

                        XMLStreamReader xmlStream = new XMLStreamReaderWrapper(
                                                                                filterElUpdate.getXMLStreamReaderWithoutCaching(),
                                                                                null );
                        // skip START_DOCUMENT
                        xmlStream.nextTag();

                        if ( versionConstraintUpdate.equals( new Version( 1, 1, 0 ) ) ) {

                            constraintUpdate = Filter110XMLDecoder.parse( xmlStream );
                            System.out.println( constraintUpdate );

                        } else {
                            String msg = Messages.get( "FILTER_VERSION NOT SPECIFIED", versionConstraintUpdate,
                                                       Version.getVersionsString( new Version( 1, 1, 0 ) ) );
                            throw new InvalidParameterValueException( msg );
                        }
                    } catch ( XMLStreamException e ) {
                        e.printStackTrace();
                        throw new XMLParsingException( this, filterElUpdate, e.getMessage() );
                    }
                } else {
                    handle = getNodeAsString( transChildElement, new XPath( "@handle", nsContext ), null );
                    // typeName = getNodeAsQName( transChildElement, new XPath( "@typeName", nsContext ), null );

                    transChildElementUpdate = getRequiredElement( transChildElement, new XPath( "*", nsContext ) );
                }

                operations.add( new UpdateTransaction( handle, transChildElementUpdate, typeName, constraintUpdate,
                                                       recordProperties ) );
                break;

            }
        }

        return new Transaction( new Version( 2, 0, 2 ), operations, requestId, verboseRequest );
    }
}
