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
package org.deegree.services.csw.getrecords;

import static org.deegree.commons.xml.CommonNamespaces.OGCNS;
import static org.deegree.protocol.csw.CSWConstants.VERSION_202;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.filter.Filter;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.protocol.csw.CSWConstants;
import org.deegree.protocol.csw.CSWConstants.ConstraintLanguage;
import org.deegree.protocol.csw.CSWConstants.ResultType;
import org.deegree.protocol.csw.CSWConstants.SetOfReturnableElements;
import org.deegree.protocol.i18n.Messages;
import org.deegree.services.csw.AbstractCSWRequestXMLAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates the method for parsing a {@Link GetRecords} XML request via Http-POST.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetRecordsXMLAdapter extends AbstractCSWRequestXMLAdapter {

    private static final Logger LOG = LoggerFactory.getLogger( GetRecordsXMLAdapter.class );

    /**
     * Parses the {@link GetRecords} XML request by deciding which version has to be parsed because of the requested
     * version.
     * 
     * @param version
     * @return {@Link GetRecords}
     */
    public GetRecords parse( Version version ) {

        if ( version == null ) {
            version = Version.parseVersion( getRequiredNodeAsString( rootElement, new XPath( "@version", nsContext ) ) );
        }

        GetRecords result = null;

        if ( VERSION_202.equals( version ) ) {
            result = parse202();
        } else {
            String msg = Messages.get( "UNSUPPORTED_VERSION", version, Version.getVersionsString( VERSION_202 ) );
            throw new InvalidParameterValueException( msg );
        }

        return result;
    }

    /**
     * Parses the {@link GetRecords} request on basis of CSW version 2.0.2
     * 
     * @param version
     *            that is requested, 2.0.2
     * @return {@link GetRecords}
     */
    private GetRecords parse202() {

        String resultTypeStr = getNodeAsString( rootElement, new XPath( "@resultType", nsContext ),
                                                ResultType.hits.name() );

        OMElement holeRequest = getElement( rootElement, new XPath( ".", nsContext ) );

        ResultType resultType = null;
        if ( resultTypeStr.equalsIgnoreCase( ResultType.hits.name() ) ) {
            resultType = ResultType.hits;
        } else if ( resultTypeStr.equalsIgnoreCase( ResultType.results.name() ) ) {
            resultType = ResultType.results;
        } else if ( resultTypeStr.equalsIgnoreCase( ResultType.validate.name() ) ) {
            resultType = ResultType.validate;
        }

        int maxRecords = getNodeAsInt( rootElement, new XPath( "@maxRecords", nsContext ), 10 );

        int startPosition = getNodeAsInt( rootElement, new XPath( "@startPosition", nsContext ), 1 );

        String outputFormat = getNodeAsString( rootElement, new XPath( "@outputFormat", nsContext ), "application/xml" );

        String requestId = getNodeAsString( rootElement, new XPath( "@requestId", nsContext ), null );

        String outputSchemaString = getNodeAsString( rootElement, new XPath( "@outputSchema", nsContext ),
                                                     "http://www.opengis.net/cat/csw/2.0.2" );

        URI outputSchema = URI.create( outputSchemaString );

        List<OMElement> getRecordsChildElements = getRequiredElements( rootElement, new XPath( "*", nsContext ) );

        boolean distributedSearch = false;
        int hopCount = -1;
        String responseHandler = null;

        QName[] elementSetNameTypeNames = null;
        Set<QName> SetOfTypeNames = new HashSet<QName>();

        SetOfReturnableElements elementSetName = null;

        Filter constraint = null;
        ConstraintLanguage constraintLanguage = null;
        // String constraint = "";
        List<SortProperty> sortProps = null;

        // Question about the occurrence of the elements
        for ( OMElement omElement : getRecordsChildElements ) {

            if ( !new QName( CSWConstants.CSW_202_NS, "DistributedSearch" ).equals( omElement.getQName() )
                 && !new QName( CSWConstants.CSW_202_NS, "ResponseHandler" ).equals( omElement.getQName() )
                 && !new QName( CSWConstants.CSW_202_NS, "Query" ).equals( omElement.getQName() ) ) {
                String msg = "Child element '" + omElement.getQName() + "' is not allowed.";
                throw new XMLParsingException( this, omElement, msg );
            }
            // optional
            if ( new QName( CSWConstants.CSW_202_NS, "DistributedSearch" ).equals( omElement.getQName() ) ) {
                if ( omElement.getText().equals( "true" ) ) {
                    distributedSearch = true;
                } else {
                    distributedSearch = false;
                }
                hopCount = getNodeAsInt( omElement, new XPath( "@hopCount", nsContext ), 2 );
            }
            // optional
            if ( new QName( CSWConstants.CSW_202_NS, "ResponseHandler" ).equals( omElement.getQName() ) ) {

                responseHandler = omElement.getText();

            }
            // mandatory
            if ( new QName( CSWConstants.CSW_202_NS, "Query" ).equals( omElement.getQName() ) ) {

                List<OMElement> queryChildElements = getRequiredElements( omElement, new XPath( "*", nsContext ) );

                String type = getNodeAsString( omElement, new XPath( "./@typeNames", nsContext ), "" );

                if ( "".equals( type ) ) {
                    String msg = "ERROR in XML document: Required attribute \"typeNames\" in element \"Query\" is missing!";
                    throw new MissingParameterException( msg );
                }

                List<QName> listOfTypeNames = new ArrayList<QName>();
                String[] typeArray = type.split( "," );
                for ( int i = 0; i < typeArray.length; i++ ) {
                    listOfTypeNames.add( parseQName( typeArray[i], omElement ) );
                }
                QName[] queryTypeNames = new QName[listOfTypeNames.size()];
                listOfTypeNames.toArray( queryTypeNames );

                // QName[] queryTypeNames = getNodesAsQNames( omElement, new XPath("@typeNames", nsContext) );

                for ( OMElement omQueryElement : queryChildElements ) {

                    // TODO mandatory exclusiveness between ElementSetName vs. ElementName not implemented yet
                    if ( new QName( CSWConstants.CSW_202_NS, "ElementSetName" ).equals( omQueryElement.getQName() ) ) {
                        String elementSetNameString = omQueryElement.getText();

                        if ( elementSetNameString.equalsIgnoreCase( SetOfReturnableElements.brief.name() ) ) {
                            elementSetName = SetOfReturnableElements.brief;
                        } else if ( elementSetNameString.equalsIgnoreCase( SetOfReturnableElements.summary.name() ) ) {
                            elementSetName = SetOfReturnableElements.summary;
                        } else if ( elementSetNameString.equalsIgnoreCase( SetOfReturnableElements.full.name() ) ) {
                            elementSetName = SetOfReturnableElements.full;
                        } else {
                            elementSetName = SetOfReturnableElements.summary;
                        }
                        elementSetNameTypeNames = getNodesAsQNames( omQueryElement, new XPath( "@typeNames", nsContext ) );

                        /**
                         * checks if the attribute typename from ElementSetName is a subset of Query typename
                         */
                        int queryTypeNamesLength = queryTypeNames.length;
                        int elementSetNameTypeNamesLength = elementSetNameTypeNames.length;
                        if ( queryTypeNamesLength >= elementSetNameTypeNamesLength ) {
                            for ( QName queryTypeName : queryTypeNames ) {
                                if ( elementSetNameTypeNames.length != 0 ) {
                                    for ( QName elementSetNameTypeName : elementSetNameTypeNames ) {
                                        if ( queryTypeName.equals( elementSetNameTypeName )
                                             || elementSetNameTypeName.getLocalPart().equals( "" ) ) {
                                            SetOfTypeNames.add( queryTypeName );
                                        }
                                    }
                                } else {
                                    SetOfTypeNames.add( queryTypeName );
                                }
                            }
                        } else {
                            String msg = Messages.get( "TYPENAME OF ELEMENTSETNAME IS A PROPER SUBSET OF TYPENAME OF QUERY" );
                            throw new InvalidParameterValueException( msg );
                        }
                        if ( SetOfTypeNames.size() == 0 ) {
                            String msg = Messages.get( "NO HARMONY BETWEEN ELEMENTSETNAME TYPENAME AND QUERY TYPENAME" );
                            throw new InvalidParameterValueException( msg );
                        }

                    } else {
                        // TODO elementName

                    }

                    if ( new QName( CSWConstants.CSW_202_NS, "Constraint" ).equals( omQueryElement.getQName() ) ) {
                        Version versionConstraint = getRequiredNodeAsVersion( omQueryElement, new XPath( "@version",
                                                                                                         nsContext ) );

                        OMElement filterEl = omQueryElement.getFirstChildWithName( new QName( OGCNS, "Filter" ) );
                        OMElement cqlTextEl = omQueryElement.getFirstChildWithName( new QName( "", "CQLTEXT" ) );
                        if ( ( filterEl != null ) && ( cqlTextEl == null ) ) {

                            constraintLanguage = ConstraintLanguage.FILTER;
                            try {
                                // TODO remove usage of wrapper (necessary at the moment to work around problems
                                // with AXIOM's

                                XMLStreamReader xmlStream = new XMLStreamReaderWrapper(
                                                                                        filterEl.getXMLStreamReaderWithoutCaching(),
                                                                                        null );
                                // skip START_DOCUMENT
                                xmlStream.nextTag();

                                if ( versionConstraint.equals( new Version( 1, 1, 0 ) ) ) {

                                    constraint = Filter110XMLDecoder.parse( xmlStream );

                                } else {
                                    String msg = Messages.get( "FILTER_VERSION NOT SPECIFIED", versionConstraint,
                                                               Version.getVersionsString( new Version( 1, 1, 0 ) ) );
                                    throw new InvalidParameterValueException( msg );
                                }
                            } catch ( XMLStreamException e ) {
                                e.printStackTrace();
                                throw new XMLParsingException( this, filterEl, e.getMessage() );
                            }

                        } else if ( ( filterEl == null ) && ( cqlTextEl != null ) ) {
                            // TODO CQLParsing
                        } else {
                            String msg = Messages.get( "MANDATORY EXCLUSIVENESS! EITHER AN OGC FILTER- OR CQL-EXPRESSION MUST BE SPECIFIED." );
                            throw new InvalidParameterValueException( msg );
                        }

                    }
                    if ( new QName( OGCNS, "SortBy" ).equals( omQueryElement.getQName() ) ) {
                        sortProps = new ArrayList<SortProperty>();
                        List<OMElement> sortPropertyElements = getRequiredElements( omQueryElement,
                                                                                    new XPath( "ogc:SortProperty",
                                                                                               nsContext ) );

                        for ( OMElement sortPropertyEl : sortPropertyElements ) {
                            OMElement propNameEl = getRequiredElement( sortPropertyEl, new XPath( "ogc:PropertyName",
                                                                                                  nsContext ) );
                            String sortOrder = getNodeAsString( sortPropertyEl,
                                                                new XPath( "ogc:SortOrder", nsContext ), "ASC" );
                            SortProperty sortProp = new SortProperty(
                                                                      new PropertyName(
                                                                                        propNameEl.getText(),
                                                                                        getNamespaceContext( propNameEl ) ),
                                                                      sortOrder.equals( "ASC" ) );
                            sortProps.add( sortProp );
                        }
                    }

                }

            }

        }
        QName[] typeNames = new QName[SetOfTypeNames.size()];
        SetOfTypeNames.toArray( typeNames );

        if ( elementSetName == null ) {
            elementSetName = SetOfReturnableElements.summary;
        }

        // TODO ElementName
        return new GetRecords( VERSION_202, nsContext, typeNames, outputFormat, resultType, requestId, outputSchema,
                               startPosition, maxRecords, null, elementSetName, constraintLanguage, constraint,
                               sortProps, distributedSearch, hopCount, responseHandler, holeRequest );
    }

}
