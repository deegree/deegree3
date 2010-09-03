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

import static org.deegree.protocol.csw.CSWConstants.VERSION_202;
import static org.deegree.protocol.csw.CSWConstants.ConstraintLanguage.CQLTEXT;
import static org.deegree.protocol.csw.CSWConstants.ConstraintLanguage.FILTER;
import static org.deegree.protocol.csw.CSWConstants.ResultType.hits;
import static org.deegree.protocol.csw.CSWConstants.ResultType.results;
import static org.deegree.protocol.csw.CSWConstants.ResultType.validate;

import java.io.StringReader;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.filter.Filter;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.protocol.csw.CSWConstants.ConstraintLanguage;
import org.deegree.protocol.csw.CSWConstants.ResultType;
import org.deegree.protocol.csw.CSWConstants.SetOfReturnableElements;
import org.deegree.protocol.i18n.Messages;
import org.deegree.services.csw.AbstractCSWKVPAdapter;

/**
 * 
 * Encapsulates the method for parsing a {@Link GetRecords} KVP request via Http-GET.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetRecordsKVPAdapter extends AbstractCSWKVPAdapter {

    /**
     * Parses the {@link GetRecords} kvp request and decides which version has to parse because of the requested version
     * 
     * @param normalizedKVPParams
     *            that are requested as key to a value.
     * @return {@link GetRecords}
     */
    public static GetRecords parse( Map<String, String> normalizedKVPParams ) {
        Version version = Version.parseVersion( KVPUtils.getRequired( normalizedKVPParams, "VERSION" ) );
        GetRecords result = null;
        if ( VERSION_202.equals( version ) ) {
            result = parse202( VERSION_202, normalizedKVPParams );

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
     * @param normalizedKVPParams
     *            that are requested containing all mandatory and optional parts regarding CSW spec
     * @return {@link GetRecords}
     */
    private static GetRecords parse202( Version version, Map<String, String> normalizedKVPParams ) {

        // optional: 'NAMESPACE'
        Map<String, String> nsBindings = extractNamespaceBindings( normalizedKVPParams );
        if ( nsBindings == null ) {
            nsBindings = Collections.emptyMap();
        }

        NamespaceContext nsContext = new NamespaceContext();
        if ( nsBindings != null ) {
            for ( String key : nsBindings.keySet() ) {
                nsContext.addNamespace( key, nsBindings.get( key ) );
            }
        }

        // typeName (mandatory)
        QName[] typeNames = extractTypeNames( normalizedKVPParams, null );
        if ( typeNames == null ) {
            String msg = Messages.get( "no TYPENAME parameter specified" );

            throw new MissingParameterException( msg );
        }

        // outputFormat (optional)
        String outputFormat = KVPUtils.getDefault( normalizedKVPParams, "outputFormat", "application/xml" );

        // resultType (optional; default = hits)
        ResultType resultType = hits;
        if ( normalizedKVPParams.get( "RESULTTYPE" ) != null
             && !normalizedKVPParams.get( "RESULTTYPE" ).equalsIgnoreCase( hits.name() ) ) {
            if ( normalizedKVPParams.get( "RESULTTYPE" ).equalsIgnoreCase( results.name() ) ) {
                resultType = results;
            } else if ( normalizedKVPParams.get( "RESULTTYPE" ).equalsIgnoreCase( validate.name() ) ) {
                resultType = validate;
            }
        }

        // requestId (optional)
        String requestId = normalizedKVPParams.get( "REQUESTID" );

        // outputSchema String
        String outputSchemaString = KVPUtils.getDefault( normalizedKVPParams, "OUTPUTSCHEMA",
                                                         "http://www.opengis.net/cat/csw/2.0.2" );

        URI outputSchema = URI.create( outputSchemaString );

        // startPosition int 1..*
        int startPosition = KVPUtils.getInt( normalizedKVPParams, "STARTPOSITION", 1 );

        // maxRecords int 1..*
        int maxRecords = KVPUtils.getInt( normalizedKVPParams, "MAXRECORDS", 10 );

        // elementName List<String>
        List<String> elementName = KVPUtils.splitAll( normalizedKVPParams, "ELEMENTNAME" );

        /**
         * NOTE: Spec says nothing about the handling which properties should exported if there is just an ELEMENTNAME
         * provided. So, ELEMENTSETNAME is handled as a required attribute and is set to "summary" if there is nothing
         * specified in the request. This can be used in the "elementSet"-attribute in the <Code>SearchResult</Code>
         * parameter of the Response.
         */
        String elementSetNameString = KVPUtils.getDefault( normalizedKVPParams, "ELEMENTSETNAME",
                                                           SetOfReturnableElements.summary.name() );
        SetOfReturnableElements elementSetName = null;

        // Question if there is no mutually exclusiveness
        if ( ( elementName == null || elementName.size() == 0 )
             && elementSetNameString.equals( SetOfReturnableElements.summary.name() ) ) {
            // throw new InvalidParameterValueException(
            // "ElementName and ElementSetName are mutually exclusive and ONE has to be specified!" );
            elementSetName = SetOfReturnableElements.summary;
        } else {
            if ( elementSetNameString.equalsIgnoreCase( SetOfReturnableElements.brief.name() ) ) {
                elementSetName = SetOfReturnableElements.brief;
            } else if ( elementSetNameString.equalsIgnoreCase( SetOfReturnableElements.summary.name() ) ) {
                elementSetName = SetOfReturnableElements.summary;
            } else if ( elementSetNameString.equalsIgnoreCase( SetOfReturnableElements.full.name() ) ) {
                elementSetName = SetOfReturnableElements.full;
            }

        }

        String constraintLanguageString = normalizedKVPParams.get( "CONSTRAINTLAGNUAGE" );
        // ConstraintLanguage Enum Language is specified
        ConstraintLanguage constraintLanguage = null;
        // constraint String Languagequery is specified
        String constraintString = normalizedKVPParams.get( "CONSTRAINT" );

        // "Filterexpression" -> Filterexpression
        // TODO what if no begin and end tag?? -> <....>
        constraintString = constraintString.substring( 1, constraintString.length() - 1 );

        Filter constraint = null;

        XMLStreamReader xmlStream = null;

        try {
            // TODO remove usage of wrapper (necessary at the moment to work around problems
            // with AXIOM's

            xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( new StringReader( constraintString ) );
            // skip START_DOCUMENT
            xmlStream.nextTag();

            constraint = Filter110XMLDecoder.parse( xmlStream );

        } catch ( XMLStreamException e ) {
            e.printStackTrace();
            throw new XMLParsingException( xmlStream, e.getMessage() );
        }

        // If one is specified the other one has to be specified, as well.
        if ( constraint == null && constraintLanguageString == null ) {
            // TODO there is no filter expression available
        } else if ( constraint != null && constraintLanguageString == null ) {
            throw new MissingParameterException(
                                                 "If there is a Constraint denoted then there should be a ConstraintLanguage provided" );
        } else if ( constraint == null && constraintLanguageString != null ) {
            throw new MissingParameterException(
                                                 "If there is a ConstraintLanguage denoted then there should be a Constraint provided" );
        } else {
            if ( constraintLanguageString.equalsIgnoreCase( FILTER.name() ) ) {

                constraintLanguage = FILTER;
            } else if ( constraintLanguageString.equalsIgnoreCase( CQLTEXT.name() ) ) {
                constraintLanguage = CQLTEXT;
            }
        }

        // sortBy List<String>
        List<String> sortByStrList = KVPUtils.splitAll( normalizedKVPParams, "SORTBY" );
        List<SortProperty> sortBy = getSortBy( sortByStrList, nsContext );

        // distributedSearch (optional; default = false)
        boolean distributedSearch = KVPUtils.getBoolean( normalizedKVPParams, "DISTRIBUTEDSEARCH", false );
        // TODO wenn true, dann hopCount darf auch gesetzt werden Spec 156
        // hopCount (optional; default = 2)
        int hopCount = KVPUtils.getInt( normalizedKVPParams, "HOPCOUNT", 2 );

        // responseHandler String Spec 156
        // CSW processing synchron or asynchron
        // TODO
        String responseHandler = normalizedKVPParams.get( "RESPONSEHANDLER" );

        return new GetRecords( version, nsContext, typeNames, outputFormat, resultType, requestId, outputSchema,
                               startPosition, maxRecords, elementName, elementSetName, constraintLanguage, constraint,
                               sortBy, distributedSearch, hopCount, responseHandler, null );
    }

    /**
     * sorts a string list ascending or descending
     * 
     * @param sortByStrList
     * @param nsContext
     * @return
     */
    private static List<SortProperty> getSortBy( List<String> sortByStrList, NamespaceContext nsContext ) {
        List<SortProperty> result = null;
        if ( sortByStrList != null ) {

            result = new LinkedList<SortProperty>();
            for ( String s : sortByStrList ) {
                if ( s.endsWith( " D" ) ) {
                    String sortbyProp = s.substring( 0, s.indexOf( " " ) );
                    result.add( new SortProperty( new PropertyName( sortbyProp, nsContext ), false ) );

                } else {

                    if ( s.endsWith( " A" ) ) {
                        String sortbyProp = s.substring( 0, s.indexOf( " " ) );
                        result.add( new SortProperty( new PropertyName( sortbyProp, nsContext ), true ) );

                    } else {
                        result.add( new SortProperty( new PropertyName( s, nsContext ), true ) );
                    }
                }
            }
        }
        return result;
    }

}
