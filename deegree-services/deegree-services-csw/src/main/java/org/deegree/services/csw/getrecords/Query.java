//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;
import static org.deegree.protocol.csw.CSWConstants.CSW_PREFIX;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.StringUtils;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.filter.Filter;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.xml.Filter100XMLDecoder;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.protocol.csw.CSWConstants;
import org.deegree.protocol.csw.CSWConstants.ConstraintLanguage;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;
import org.deegree.protocol.i18n.Messages;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@deegree.org">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class Query {

    private static final Logger LOG = getLogger( Query.class );

    private static final NamespaceBindings nsContext = CommonNamespaces.getNamespaceContext();

    static {
        nsContext.addNamespace( CSW_PREFIX, CSW_202_NS );
    }

    private ReturnableElement elementSetName;

    private String[] elementName;

    private Filter constraint;

    private ConstraintLanguage constraintLanguage;

    private SortProperty[] sortProps;

    private QName[] queryTypeNames;

    private QName[] returnTypeNames;

    public Query( ReturnableElement elementSetName, String[] elementName, Filter constraint,
                  ConstraintLanguage constraintLanguage, SortProperty[] sortProps, QName[] queryTypeNames,
                  QName[] returnTypeNames ) {
        this.elementSetName = elementSetName;
        this.elementName = elementName;
        this.constraint = constraint;
        this.constraintLanguage = constraintLanguage;
        this.sortProps = sortProps;
        this.queryTypeNames = queryTypeNames;
        this.returnTypeNames = returnTypeNames;
    }

    public QName[] getQueryTypeNames() {
        if ( queryTypeNames == null )
            return new QName[0];
        return queryTypeNames;
    }

    public QName[] getReturnTypeNames() {
        if ( returnTypeNames == null )
            return new QName[0];
        return returnTypeNames;
    }

    public ReturnableElement getElementSetName() {
        return elementSetName;
    }

    public String[] getElementName() {
        if ( elementName == null )
            return new String[0];
        return elementName;
    }

    public Filter getConstraint() {
        return constraint;
    }

    public ConstraintLanguage getConstraintLanguage() {
        return constraintLanguage;
    }

    public SortProperty[] getSortProps() {
        if ( sortProps == null )
            return new SortProperty[0];
        return sortProps;
    }

    public static Query getQuery( OMElement omElement ) {
        if ( new QName( CSWConstants.CSW_202_NS, "Query" ).equals( omElement.getQName() ) ) {
            XMLAdapter adapter = new XMLAdapter( omElement );
            SortProperty[] sortProps = null;
            Filter constraint = null;
            ReturnableElement elementSetName = null;
            String[] elementName = null;
            ConstraintLanguage constraintLanguage = null;

            List<OMElement> queryChildElements = adapter.getRequiredElements( omElement, new XPath( "*", nsContext ) );

            String typeQuery = adapter.getNodeAsString( omElement, new XPath( "./@typeNames", nsContext ), "" );

            if ( "".equals( typeQuery ) ) {
                String msg = "ERROR in XML document: Required attribute \"typeNames\" in element \"Query\" is missing!";
                throw new MissingParameterException( msg );
            }

            String[] queryTypeNamesString = StringUtils.split( typeQuery, " " );
            QName[] queryTypeNames = new QName[queryTypeNamesString.length];
            int counterQName = 0;
            for ( String s : queryTypeNamesString ) {
                LOG.debug( "Parsing typeName '" + s + "' of Query as QName. " );
                QName qname = adapter.parseQName( s, adapter.getRootElement() );
                queryTypeNames[counterQName++] = qname;
            }
            elementName = adapter.getNodesAsStrings( omElement, new XPath( "./csw:ElementName", nsContext ) );
            QName[] returnTypeNames = null;
            for ( OMElement omQueryElement : queryChildElements ) {

                // TODO mandatory exclusiveness between ElementSetName vs. ElementName not implemented yet
                if ( new QName( CSWConstants.CSW_202_NS, "ElementSetName" ).equals( omQueryElement.getQName() ) ) {
                    String elementSetNameString = omQueryElement.getText();
                    elementSetName = ReturnableElement.determineReturnableElement( elementSetNameString );

                    // elementSetNameTypeNames = getNodesAsQNames( omQueryElement, new XPath( "@typeNames",
                    // nsContext ) );
                    String typeElementSetName = adapter.getNodeAsString( omQueryElement,
                                                                         new XPath( "./@typeNames", nsContext ), "" ).trim();
                    String[] elementSetNameTypeNamesString = StringUtils.split( typeElementSetName, " " );
                    returnTypeNames = new QName[elementSetNameTypeNamesString.length];
                    for ( int i = 0; i < elementSetNameTypeNamesString.length; i++ ) {
                        returnTypeNames[i] = adapter.parseQName( elementSetNameTypeNamesString[i], omElement );
                    }
                }
                Pair<Filter, ConstraintLanguage> parsedConstraint = parseConstraint( adapter, omQueryElement );
                if ( parsedConstraint != null ) {
                    constraintLanguage = parsedConstraint.second;
                    constraint = parsedConstraint.first;
                }

                if ( new QName( OGCNS, "SortBy" ).equals( omQueryElement.getQName() ) ) {

                    List<OMElement> sortPropertyElements = adapter.getRequiredElements( omQueryElement,
                                                                                        new XPath( "ogc:SortProperty",
                                                                                                   nsContext ) );
                    sortProps = new SortProperty[sortPropertyElements.size()];
                    int counter = 0;
                    for ( OMElement sortPropertyEl : sortPropertyElements ) {
                        OMElement propNameEl = adapter.getRequiredElement( sortPropertyEl,
                                                                           new XPath( "ogc:PropertyName", nsContext ) );
                        String sortOrder = adapter.getNodeAsString( sortPropertyEl, new XPath( "following-sibling::ogc:SortOrder",
                                                                                              nsContext ), "ASC" );
                        SortProperty sortProp = new SortProperty(
                                                                  new ValueReference(
                                                                                      propNameEl.getText(),
                                                                                      adapter.getNamespaceContext( propNameEl ) ),
                                                                  sortOrder.equals( "ASC" ) );
                        sortProps[counter++] = sortProp;
                    }
                }
            }
            return new Query( elementSetName, elementName, constraint, constraintLanguage, sortProps, queryTypeNames,
                              returnTypeNames );
        }
        return null;
    }

    protected static Pair<Filter, ConstraintLanguage> parseConstraint( XMLAdapter adapter, OMElement omQueryElement ) {
        Pair<Filter, ConstraintLanguage> pc = null;
        if ( omQueryElement != null && new QName( CSWConstants.CSW_202_NS, "Constraint" ).equals( omQueryElement.getQName() ) ) {
            Version versionConstraint = adapter.getRequiredNodeAsVersion( omQueryElement, new XPath( "@version",
                                                                                                     nsContext ) );

            OMElement filterEl = omQueryElement.getFirstChildWithName( new QName( OGCNS, "Filter" ) );
            OMElement cqlTextEl = omQueryElement.getFirstChildWithName( new QName( "", "CQLTEXT" ) );
            if ( ( filterEl != null ) && ( cqlTextEl == null ) ) {

                ConstraintLanguage constraintLanguage = ConstraintLanguage.FILTER;
                Filter constraint;
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

                    } else if ( versionConstraint.equals( new Version( 1, 0, 0 ) ) ) {
                        constraint = Filter100XMLDecoder.parse( xmlStream );
                    } else {
                        String msg = Messages.get( "CSW_FILTER_VERSION_NOT_SPECIFIED", versionConstraint,
                                                   Version.getVersionsString( new Version( 1, 1, 0 ) ),
                                                   Version.getVersionsString( new Version( 1, 0, 0 ) ) );
                        LOG.info( msg );
                        throw new InvalidParameterValueException( msg );
                    }
                } catch ( XMLStreamException e ) {
                    String msg = "FilterParsingException: There went something wrong while parsing the filter expression, so please check this!";
                    LOG.debug( msg );
                    throw new XMLParsingException( adapter, filterEl, e.getMessage() );
                }
                pc = new Pair<Filter, CSWConstants.ConstraintLanguage>( constraint, constraintLanguage );
            } else if ( ( filterEl == null ) && ( cqlTextEl != null ) ) {
                String msg = Messages.get( "CSW_UNSUPPORTED_CQL_FILTER" );
                LOG.info( msg );
                throw new InvalidParameterValueException( msg );
            } else {
                String msg = Messages.get( "CSW_MISSING_FILTER_OR_CQL" );
                LOG.debug( msg );
                throw new InvalidParameterValueException( msg );
            }
        }
        return pc;
    }
}