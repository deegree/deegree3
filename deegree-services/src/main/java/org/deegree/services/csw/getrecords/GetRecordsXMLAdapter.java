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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import jj2000.j2k.NotImplementedError;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.ArrayUtils;
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
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;
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
        ResultType resultType = ResultType.determineResultType( resultTypeStr );

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

        Set<QName> SetOfTypeNames = new HashSet<QName>();

        ReturnableElement elementSetName = null;
        String[] elementName = null;

        Filter constraint = null;
        ConstraintLanguage constraintLanguage = null;
        // String constraint = "";
        SortProperty[] sortProps = null;

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

                String typeQuery = getNodeAsString( omElement, new XPath( "./@typeNames", nsContext ), "" );

                if ( "".equals( typeQuery ) ) {
                    String msg = "ERROR in XML document: Required attribute \"typeNames\" in element \"Query\" is missing!";
                    throw new MissingParameterException( msg );
                }

                String[] queryTypeNamesString = ArrayUtils.toArray( typeQuery, ",", true );
                QName[] queryTypeNames = new QName[queryTypeNamesString.length];
                int counterQName = 0;
                for ( String s : queryTypeNamesString ) {
                    LOG.debug( "Parsing typeName '" + s + "' of Query as QName. " );
                    QName qname = parseQName( s, rootElement );
                    queryTypeNames[counterQName++] = qname;
                }

                for ( OMElement omQueryElement : queryChildElements ) {

                    // TODO mandatory exclusiveness between ElementSetName vs. ElementName not implemented yet
                    if ( new QName( CSWConstants.CSW_202_NS, "ElementSetName" ).equals( omQueryElement.getQName() ) ) {
                        String elementSetNameString = omQueryElement.getText();
                        elementSetName = ReturnableElement.determineReturnableElement( elementSetNameString );

                        // elementSetNameTypeNames = getNodesAsQNames( omQueryElement, new XPath( "@typeNames",
                        // nsContext ) );
                        String typeElementSetName = getNodeAsString( omQueryElement, new XPath( "./@typeNames",
                                                                                                nsContext ), "" );
                        if ( "".equals( typeElementSetName ) ) {
                            LOG.info( "TypeName of element 'ElementSetName' is not specified, so there are the typeNames taken from element 'Query'. " );
                            SetOfTypeNames.addAll( Arrays.asList( queryTypeNames ) );
                        } else {

                            String[] elementSetNameTypeNamesString = ArrayUtils.toArray( typeElementSetName, ",", true );
                            boolean isSubset = false;
                            for ( String s : elementSetNameTypeNamesString ) {
                                LOG.debug( "Parsing typeName '" + s + "' of ElementSetName as QName. " );
                                QName qname = parseQName( s, omElement );
                                LOG.debug( "Parsing correct, so check for containing of the typeName '" + s + "'. " );
                                isSubset = ArrayUtils.contains( queryTypeNamesString, s, true, true );
                                if ( !isSubset ) {
                                    String msg = "QName '"
                                                 + s
                                                 + "' is not containing in typeNames of element Query => typeName of element ElementSetName is not a subset of typeName of element Query!";
                                    LOG.debug( msg );
                                    throw new InvalidParameterValueException( msg );
                                }
                                SetOfTypeNames.add( qname );
                            }
                        }

                    }
                    if ( new QName( CSWConstants.CSW_202_NS, "ElementName" ).equals( omQueryElement.getQName() ) ) {
                        String msg = "ElementName is not implmeneted yet, use ElementSetName, instead. ";
                        LOG.info( msg );
                        throw new NotImplementedError( msg );
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
                                    LOG.info( msg );
                                    throw new InvalidParameterValueException( msg );
                                }
                            } catch ( XMLStreamException e ) {
                                String msg = "FilterParsingException: There went something wrong while parsing the filter expression, so please check this!";
                                LOG.debug( msg );
                                throw new XMLParsingException( this, filterEl, e.getMessage() );
                            }

                        } else if ( ( filterEl == null ) && ( cqlTextEl != null ) ) {
                            String msg = "CQL-Filter is not implemented yet. Please use the OGC Filter expression, instead. ";
                            LOG.info( msg );
                            throw new NotImplementedError( msg );
                        } else {
                            String msg = Messages.get( "MANDATORY EXCLUSIVENESS! EITHER AN OGC FILTER- OR CQL-EXPRESSION MUST BE SPECIFIED." );
                            LOG.debug( msg );
                            throw new InvalidParameterValueException( msg );
                        }

                    }
                    if ( new QName( OGCNS, "SortBy" ).equals( omQueryElement.getQName() ) ) {

                        List<OMElement> sortPropertyElements = getRequiredElements( omQueryElement,
                                                                                    new XPath( "ogc:SortProperty",
                                                                                               nsContext ) );
                        sortProps = new SortProperty[sortPropertyElements.size()];
                        int counter = 0;
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
                            sortProps[counter++] = sortProp;
                        }
                    }

                }

            }

        }
        QName[] typeNames = new QName[SetOfTypeNames.size()];
        SetOfTypeNames.toArray( typeNames );

        if ( elementName == null && elementSetName == null ) {
            elementSetName = ReturnableElement.summary;
        }

        return new GetRecords( VERSION_202, nsContext, typeNames, outputFormat, resultType, requestId, outputSchema,
                               startPosition, maxRecords, elementName, elementSetName, constraintLanguage, constraint,
                               sortProps, distributedSearch, hopCount, responseHandler, holeRequest );
    }
}
