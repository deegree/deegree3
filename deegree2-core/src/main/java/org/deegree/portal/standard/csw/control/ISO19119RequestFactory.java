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

package org.deegree.portal.standard.csw.control;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.deegree.datatypes.QualifiedName;
import org.deegree.enterprise.control.RPCStruct;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.model.filterencoding.Literal;
import org.deegree.model.filterencoding.Operation;
import org.deegree.model.filterencoding.OperationDefines;
import org.deegree.model.filterencoding.PropertyIsCOMPOperation;
import org.deegree.model.filterencoding.PropertyName;

/**
 * A <code>${type_name}</code> class.<br/> TODO class description
 *
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ISO19119RequestFactory extends CSWRequestFactory {

    private static final ILogger LOG = LoggerFactory.getLogger( ISO19119RequestFactory.class );

    // private static final char WILDCARD = '*';
    // private static final String OUTPUTSCHEMA = "csw:profile";
    private static final String OUTPUTSCHEMA = "http://www.isotc211.org/2005/gmd";

    private RPCStruct struct = null;

    private Properties requestElementsProps = new Properties();

    /**
     *
     *
     */
    public ISO19119RequestFactory() {
        try {
            InputStream is = ISO19119RequestFactory.class.getResourceAsStream( "ISO19119requestElements.properties" );
            this.requestElementsProps.load( is );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
        }
    }

    /**
     * creates a GetRecord request that is conform to the OGC Stateless Web Service Catalog Profil and GDI NRW catalog
     * specifications from a RPC struct.
     *
     * @param struct
     *            RPC structure containing the request parameter
     * @param resultType
     * @return GetFeature request as a string
     */
    @Override
    public String createRequest( RPCStruct struct, String resultType ) {

        this.struct = struct;

        InputStream is = null;
        InputStreamReader ireader = null;
        BufferedReader br = null;
        StringBuffer sb = null;
        String request = null;

        is = ISO19119RequestFactory.class.getResourceAsStream( "CSWGetRecordsTemplate.xml" );
        // is = ISO19119RequestFactory.class.getResourceAsStream( "CSWGetRecordByIdTemplate.xml" );

        try {
            ireader = new InputStreamReader( is );
            br = new BufferedReader( ireader );
            sb = new StringBuffer( 50000 );

            while ( ( request = br.readLine() ) != null ) {
                sb.append( request );
            }
            request = sb.toString();
            br.close();

        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
        }

        request = replaceVarsInSearchRequest( request, resultType );
        // request = replaceVarsInOverviewRequest( request );

        return request;
    }

    private String replaceVarsInSearchRequest( String request, String resultType ) {

        String filter = createFilterEncoding();
        LOG.logDebug( "created filter: ", filter );
        request = request.replaceFirst( "\\$FILTER", filter );

        request = request.replaceFirst( "\\$OUTPUTSCHEMA", OUTPUTSCHEMA );

        request = request.replaceFirst( "\\$RESULTTYPE", resultType );

        String startPos = "1"; // default is 1 according to OGC CSW-spec
        if ( struct.getMember( RPC_STARTPOSITION ) != null ) {
            startPos = (String) struct.getMember( RPC_STARTPOSITION ).getValue();
        }
        request = request.replaceFirst( "\\$STARTPOSITION", startPos );

        String maxRecords = Integer.toString( config.getMaxRecords() ); // default is 10 (according to spec)
        request = request.replaceFirst( "\\$MAXRECORDS", maxRecords );

        // For 2.0.0
        // String queryType = "csw:service"; // dataset, dataseries, application
        // // FOR BOTH CSW2.0.0 AND CSW2.0.2 !
        String queryType = "csw:service"; // dataset, dataseries, service, application
        if ( struct.getMember( RPC_TYPENAME ) != null ) {
            queryType = (String) struct.getMember( RPC_TYPENAME ).getValue();
        }
        /*
         * if ( struct.getMember( RPC_TYPENAME ) != null ) { queryType = (String) struct.getMember( RPC_TYPENAME
         * ).getValue(); if ( !"gmd:MD_Metadata".equals( queryType ) && !"csw:Record".equals( queryType )) {
         * LOG.logError( StringTools.concat( 100, "typename ", queryType, " is not a supported type.", "Suuported types
         * are: ", "gmd:MD_Metadata", "csw:Record" ) ); queryType = "gmd:MD_Metadata"; } }
         */
        request = request.replaceFirst( "\\$TYPENAME", queryType );

        String elementSet = "brief"; // brief, summary, full
        if ( struct.getMember( RPC_ELEMENTSETNAME ) != null ) {
            elementSet = (String) struct.getMember( RPC_ELEMENTSETNAME ).getValue();
        }
        request = request.replaceFirst( "\\$ELEMENTSETNAME", elementSet );

        return request;
    }

    /**
     * Creates a filter text according to the given parameters of the rpc request to be added to the GetRecords request
     *
     * @return the filter xml fragment
     */
    private String createFilterEncoding() {

        StringBuffer sb = new StringBuffer( 2000 );
        int expCounter = 0;

        sb.append( "<csw:Constraint version='1.0.0'><ogc:Filter>" );

        // build filter encoding structure, handle all known fields sequentially
        String s = null;

        s = handleServiceSearch();
        if ( ( s != null ) && ( s.length() > 0 ) ) {
            expCounter++;
            sb.append( s );
        }

        // NOTE: if some of the methods below are needed,
        // copy them from ISO19115RequestFactory and adapt them where needed.

        // s = handleFileIdentifier();
        // if ( ( s != null ) && ( s.length() > 0 ) ) {
        // expCounter++;
        // sb.append( s );
        // }

        // s = handleParentIdentifier();
        // if ( ( s != null ) && ( s.length() > 0 ) ) {
        // expCounter++;
        // sb.append( s );
        // }

        // s = handleKeywords();
        // if ( ( s != null ) && ( s.length() > 0 ) ) {
        // expCounter++;
        // sb.append( s );
        // }

        // s = handleDate();
        // if ( ( s != null ) && ( s.length() > 0 ) ) {
        // expCounter++;
        // sb.append( s );
        // }

        // s = handleBbox();
        // if ( ( s != null ) && ( s.length() > 0 ) ) {
        // expCounter++;
        // sb.append( s );
        // }

        if ( expCounter > 1 ) {
            sb.insert( "<csw:Constraint version='1.0.0'><ogc:Filter>".length(), "<ogc:And>" );
            sb.append( "</ogc:And>" );
        }

        sb.append( "</ogc:Filter></csw:Constraint>" );

        return sb.toString();
    }

    /**
     * @return Returns a string containing the service search part of the filter condition.
     */
    private String handleServiceSearch() {

        StringBuffer sb = new StringBuffer( 2000 );

        String[] t = null;
        if ( struct.getMember( Constants.RPC_SERVICESEARCH ) != null ) {
            String s = (String) struct.getMember( Constants.RPC_SERVICESEARCH ).getValue();
            t = StringTools.toArray( s, "|", true );
        }

        if ( ( t != null ) && ( t.length > 0 ) ) {
            // sb.append( "<ogc:Or>" );
            for ( int i = 0; i < t.length; i++ ) {
                if ( ( t[i] != null ) && ( t[i].length() > 0 ) ) {
                    // replace invalid chars
                    // t[i] = StringExtend.replace( t[i], "'", " ", true );
                    // t[i] = StringExtend.replace( t[i], "\"", " ", true );

                    // determine the way to build FilterEncoding part
                    String cf_props = requestElementsProps.getProperty( Constants.CONF_SERVICESEARCH );
                    String[] cf = cf_props.split( ";" );

                    for ( int k = 0; k < cf.length; k++ ) {
                        String strOp = t[i];
                        if ( ( strOp != null ) && ( strOp.length() > 0 ) ) {
                            Operation op = createOperation( OperationDefines.PROPERTYISEQUALTO, cf[k], strOp );
                            sb.append( op.to110XML() );
                        }
                    }
                }
            }
            // sb.append( "</ogc:Or>" );
        }

        return sb.toString();
    }

    /**
     * Creates an operation in the csw GetRecords request
     *
     * @param opId
     *            the operationID, ex: PROPERTYISEQUALTO, PROPERTYISELIKE
     * @param property
     *            what property to compare to, ex: apiso:title
     * @param value
     *            to compare to
     *
     * @return The created operation
     */
    private Operation createOperation( int opId, String property, Object value ) {

        Operation op = null;

        switch ( opId ) {
        case OperationDefines.PROPERTYISEQUALTO:
            op = new PropertyIsCOMPOperation( OperationDefines.PROPERTYISEQUALTO,
                                              new PropertyName( new QualifiedName( property ) ),
                                              new Literal( (String) value ) );
            break;

        // case OperationDefines.PROPERTYISLIKE:
        // char wildCard = WILDCARD;
        // char singleChar = '?';
        // char escapeChar = '/';
        // String lit = wildCard + (String)value + wildCard;
        // op = new PropertyIsLikeOperation( new PropertyName( new QualifiedName( property ) ),
        // new Literal( lit ), wildCard, singleChar, escapeChar );
        // break;
        // case OperationDefines.PROPERTYISLESSTHANOREQUALTO:
        // op = new PropertyIsCOMPOperation( OperationDefines.PROPERTYISLESSTHANOREQUALTO,
        // new PropertyName( new QualifiedName( property ) ),
        // new Literal( (String)value ) );
        // break;
        // case OperationDefines.PROPERTYISGREATERTHANOREQUALTO:
        // op = new PropertyIsCOMPOperation( OperationDefines.PROPERTYISGREATERTHANOREQUALTO,
        // new PropertyName( new QualifiedName( property ) ),
        // new Literal( (String)value ) );
        // break;
        // case OperationDefines.BBOX:
        // op = new SpatialOperation( OperationDefines.BBOX,
        // new PropertyName( new QualifiedName( property ) ),
        // (Geometry)value );
        // break;
        // case OperationDefines.PROPERTYISNULL:
        // op = new PropertyIsNullOperation( new PropertyName( new QualifiedName( property ) ) );
        // break;

        default:
            op = new PropertyIsCOMPOperation( OperationDefines.PROPERTYISEQUALTO,
                                              new PropertyName( new QualifiedName( property ) ),
                                              new Literal( (String) value ) );
        }

        return op;
    }

}
