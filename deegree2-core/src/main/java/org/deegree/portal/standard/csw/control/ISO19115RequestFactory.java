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
import org.deegree.i18n.Messages;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.filterencoding.Literal;
import org.deegree.model.filterencoding.Operation;
import org.deegree.model.filterencoding.OperationDefines;
import org.deegree.model.filterencoding.PropertyIsCOMPOperation;
import org.deegree.model.filterencoding.PropertyIsLikeOperation;
import org.deegree.model.filterencoding.PropertyIsNullOperation;
import org.deegree.model.filterencoding.PropertyName;
import org.deegree.model.filterencoding.SpatialOperation;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.portal.standard.csw.CatalogClientException;

/**
 * A <code>${type_name}</code> class.<br/>
 *
 * class for creating a get GetRecord Request against a catalog based on OGC Stateless Web Service Catalog Profil and
 * GDI NRW catalog specifications to access data metadata (ISO 19115).
 * <p>
 * The only public method of the class receives a 'model' represented by a <tt>HashMap</tt> that contains the request
 * parameters as name-value-pairs. The names corresponds to the form-field-names. For common this will be the fields of
 * a HTML-form but it can be any other form (e.g. swing-application)
 * </p>
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ISO19115RequestFactory extends CSWRequestFactory {

    private static final ILogger LOG = LoggerFactory.getLogger( ISO19115RequestFactory.class );

    static final String RPC_SIMPLESEARCH = "RPC_SIMPLESEARCH";

    private static final char WILDCARD = '*';

    // private static final String OUTPUTSCHEMA = "csw:profile";
    private static final String OUTPUTSCHEMA = "http://www.isotc211.org/2005/gmd";

    private RPCStruct struct = null;

    private Properties requestElementsProps = new Properties();

    /**
     *
     */
    public ISO19115RequestFactory() {
        try {
            InputStream is = ISO19115RequestFactory.class.getResourceAsStream( "ISO19115requestElements.properties" );
            this.requestElementsProps.load( is );
        } catch ( Exception e ) {
            LOG.logError( e.getLocalizedMessage(), e );
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
     * @throws CatalogClientException
     */
    @Override
    public String createRequest( RPCStruct struct, String resultType )
                            throws CatalogClientException {

        this.struct = struct;
        boolean isSearchRequest = false;
        boolean isOverviewRequest = false;

        if ( "HITS".equals( resultType ) || "RESULTS".equals( resultType ) ) {
            isSearchRequest = true;
            LOG.logDebug( "request is searchRequest" );
        } else if ( resultType == null ) {
            LOG.logDebug( "request is overviewRequest" );
            isOverviewRequest = true;
        }

        InputStream is = null;
        InputStreamReader ireader = null;
        BufferedReader br = null;
        StringBuffer sb = null;
        String request = null;

        if ( isSearchRequest ) {
            is = ISO19115RequestFactory.class.getResourceAsStream( "CSWGetRecordsTemplate.xml" );
        } else if ( isOverviewRequest ) {
            is = ISO19115RequestFactory.class.getResourceAsStream( "CSWGetRecordByIdTemplate.xml" );
        }

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

        if ( isSearchRequest ) {
            try {
                request = replaceVarsInSearchRequest( request, resultType );
            } catch ( UnknownCRSException e ) {
                throw new CatalogClientException( e.getMessage(), e );
            }
        } else if ( isOverviewRequest ) {
            request = replaceVarsInOverviewRequest( request );
        }

        return request;
    }

    /**
     * @param request
     * @param resultType
     * @return Returns the request, where all variables are replaced by values.
     * @throws CatalogClientException
     * @throws UnknownCRSException
     */
    private String replaceVarsInSearchRequest( String request, String resultType )
                            throws CatalogClientException, UnknownCRSException {

        // replace variables from template

        String filter = createFilterEncoding();
        request = request.replaceFirst( "\\$FILTER", filter );

        request = request.replaceFirst( "\\$OUTPUTSCHEMA", OUTPUTSCHEMA );

        request = request.replaceFirst( "\\$RESULTTYPE", resultType );

        // According to OGC CSW-spec default is 1
        String startPos = "1";
        if ( struct.getMember( RPC_STARTPOSITION ) != null ) {
            startPos = (String) struct.getMember( RPC_STARTPOSITION ).getValue();
        }
        request = request.replaceFirst( "\\$STARTPOSITION", startPos );

        // According to OGC CSW-spec default is 10
        String maxRecords = Integer.toString( config.getMaxRecords() );
        request = request.replaceFirst( "\\$MAXRECORDS", maxRecords );

        // For 2.0.0
        // String queryType = "csw:dataset"; // dataset, dataseries, application
        // For 2.0.2
        String queryType = "gmd:MD_Metadata"; // possible values are: csw:Records, gmd:MD_Metadata, csw:service
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
     * @param request
     * @return Returns the request, where all variables are replaced by values.
     * @throws CatalogClientException
     */
    private String replaceVarsInOverviewRequest( String request )
                            throws CatalogClientException {

        String id = null;
        if ( struct.getMember( Constants.RPC_IDENTIFIER ) != null ) {
            id = (String) struct.getMember( Constants.RPC_IDENTIFIER ).getValue();
        } else {
            throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_ERROR_ID_NOT_SET" ) );
        }
        request = request.replaceFirst( "\\$IDENTIFIER", id );

        request = request.replaceFirst( "\\$OUTPUTSCHEMA", OUTPUTSCHEMA );

        String elementSet = "full"; // brief, summary, full
        if ( struct.getMember( RPC_ELEMENTSETNAME ) != null ) {
            elementSet = (String) struct.getMember( RPC_ELEMENTSETNAME ).getValue();
        }
        request = request.replaceFirst( "\\$ELEMENTSETNAME", elementSet );

        return request;
    }

    /**
     * takes RequestModel and builds a String result out of it. The result should be OGC FilterEncoding conformant.
     *
     * @return Returns the fragment for filter encoding.
     * @throws CatalogClientException
     * @throws UnknownCRSException
     */
    private String createFilterEncoding()
                            throws CatalogClientException, UnknownCRSException {

        StringBuffer sb = new StringBuffer( 2000 );
        int expCounter = 0;
        LOG.logDebug( "in method createFilterEncoding" );
        sb.append( "<csw:Constraint version='1.0.0'><ogc:Filter>" );

        // build filter encoding structure, handle all known fields sequentially
        String s = handleFileIdentifier();
        LOG.logDebug( "file identifier=", s );
        if ( ( s != null ) && ( s.length() > 0 ) ) {
            expCounter++;
            sb.append( s );
        }

        s = handleParentIdentifier();
        LOG.logDebug( "parent identifier=", s );
        if ( ( s != null ) && ( s.length() > 0 ) ) {
            expCounter++;
            sb.append( s );
        }

        s = handleSimpleSearch();
        LOG.logDebug( "simple search=", s );
        if ( ( s != null ) && ( s.length() > 0 ) ) {
            expCounter++;
            sb.append( s );
        }

        s = handleTopiccategory();
        LOG.logDebug( "topic category=", s );
        if ( ( s != null ) && ( s.length() > 0 ) ) {
            expCounter++;
            sb.append( s );
        }

        s = handleKeywords();
        LOG.logDebug( "handle keywords=", s );
        if ( ( s != null ) && ( s.length() > 0 ) ) {
            expCounter++;
            sb.append( s );
        }

        s = handleDate();
        LOG.logDebug( "date=", s );
        if ( ( s != null ) && ( s.length() > 0 ) ) {
            expCounter++;
            sb.append( s );
        }

        s = handleBbox();
        LOG.logDebug( "box=", s );
        if ( ( s != null ) && ( s.length() > 0 ) ) {
            expCounter++;
            sb.append( s );
        }

        s = handleMetadataType();
        LOG.logDebug( "MetadataType=", s );
        if ( ( s != null ) && ( s.length() > 0 ) ) {
            expCounter++;
            sb.append( s );
        }

        if ( expCounter > 1 ) {
            sb.insert( "<csw:Constraint version='1.0.0'><ogc:Filter>".length(), "<ogc:And>" );
            sb.append( "</ogc:And>" );
        }

        sb.append( "</ogc:Filter></csw:Constraint>" );

        return sb.toString();
    }

    /**
     * Build OGC Filterencoding fragment: use <code>CSWRequestmodel</code> field <b>fileIdentifier</b> to create
     * Comparison Operation.
     *
     * @return Returns the fragment for fileIdentifier. May be empty.
     */
    private String handleFileIdentifier() {

        StringBuffer sb = new StringBuffer( 1000 );

        String id = null;
        if ( struct.getMember( Constants.RPC_IDENTIFIER ) != null ) {
            id = (String) struct.getMember( Constants.RPC_IDENTIFIER ).getValue();
        }

        if ( ( id != null ) && ( id.trim().length() > 0 ) ) {
            String cf_props = requestElementsProps.getProperty( Constants.CONF_IDENTIFIER );
            String[] cf = cf_props.split( ";" );

            sb = new StringBuffer( 1000 );
            Operation op1 = createOperation( OperationDefines.PROPERTYISEQUALTO, cf[0], id );
            sb.append( op1.to110XML() );
        }

        return sb.toString();
    }

    /**
     * Build OGC Filterencoding fragment: use <code>CSWRequestmodel</code> field <b>parentIdentifier</b> to create
     * Comparison Operation.
     *
     * @return Returns the fragment for apiiso:type property. May be empty
     */
    private String handleMetadataType() {

        StringBuffer sb = new StringBuffer( 50 );

        if ( ( struct.getMember( RPC_MDTYPE ) ) != null ) {
            String rpc_mdType = (String) struct.getMember( RPC_MDTYPE ).getValue();
            String[] mdTypes = null;
            if ( rpc_mdType != null ) {
                mdTypes = rpc_mdType.split( "," );
            }

            String property = requestElementsProps.getProperty( Constants.CONF_MDTYPE );
            if ( mdTypes != null && mdTypes.length > 0 ) {
                if ( mdTypes.length > 1 && mdTypes.length <= 3 ) {
                    sb.append( "<ogc:Or>" );
                    for ( int i = 0; i < mdTypes.length; i++ ) {

                        Operation op = createOperation( OperationDefines.PROPERTYISEQUALTO, property, mdTypes[i] );
                        sb.append( op.to110XML() );
                    }
                    sb.append( "</ogc:Or>" );
                } else if ( mdTypes.length == 1 ) {
                    Operation op = createOperation( OperationDefines.PROPERTYISEQUALTO, property, mdTypes[0] );
                    sb.append( op.to110XML() );
                } // else {
                // This block does nothing, I just added it for clarity.
                // This is the case, where the three metadata types are searched and in this case, no type filter
                // shall be added at all
                // Update: since no MD of type service shall be returned, we use the first if condition of this block
                /*
                 * Operation op = createOperation( OperationDefines.PROPERTYISLIKE, property, "*" ); sb.append(
                 * op.to110XML() );
                 */
                // }
            }
        }
        return sb.toString();
    }

    /**
     * Build OGC Filterencoding fragment: use <code>CSWRequestmodel</code> field <b>parentIdentifier</b> to create
     * Comparison Operation.
     *
     * @return Returns the fragment for parentIdentifier. May be empty.
     */
    private String handleParentIdentifier() {

        StringBuffer sb = new StringBuffer( 1000 );
        String seriesIdentifier = null;
        if ( struct.getMember( Constants.RPC_DATASERIES ) != null ) {
            seriesIdentifier = (String) struct.getMember( Constants.RPC_DATASERIES ).getValue();
        }

        if ( ( seriesIdentifier != null ) && ( seriesIdentifier.trim().length() > 0 ) ) {
            String cf_props = requestElementsProps.getProperty( Constants.CONF_DATASERIES );
            String[] cf = cf_props.split( ";" );

            sb = new StringBuffer( 1000 );
            Operation op1 = createOperation( OperationDefines.PROPERTYISEQUALTO, cf[0], seriesIdentifier );
            sb.append( op1.to110XML() );
        }

        return sb.toString();
    }

    /**
     * Spread <code>CSWRequestmodel</code> field <b>terms</b> to several Comparison Operations with pre-defined
     * Property names.
     *
     * @return Returns the fragment for the search string. May be empty.
     */
    private String handleSimpleSearch() {

        StringBuffer sb = new StringBuffer( 2000 );

        String[] keywords = null;
        if ( struct.getMember( RPC_SIMPLESEARCH ) != null ) {
            String rpcVal = (String) struct.getMember( RPC_SIMPLESEARCH ).getValue();
            keywords = StringTools.toArray( rpcVal, ",;|", true );
        }

        if ( ( keywords != null ) && ( keywords.length > 0 ) ) {
            sb.append( "<ogc:Or>" );

            for ( int i = 0; i < keywords.length; i++ ) {
                // replace invalid chars
                if ( ( keywords[i] != null ) && ( keywords[i].length() > 0 ) ) {
                    keywords[i] = StringTools.replace( keywords[i], "'", " ", true );
                    keywords[i] = StringTools.replace( keywords[i], "\"", " ", true );

                    // determine the way to build FilterEncoding part
                    String cf_props = requestElementsProps.getProperty( Constants.CONF_SIMPLESEARCH );
                    String[] cf = cf_props.split( ";" );
                    for ( int k = 0; k < cf.length; k++ ) {
                        String strOp = keywords[i];

                        if ( ( strOp != null ) && ( strOp.length() > 0 ) ) {
                            // LOWERCASE SECTION
                            strOp = strOp.substring( 0, 1 ).toLowerCase() + strOp.substring( 1 );
                            Operation op = createOperation( OperationDefines.PROPERTYISLIKE, cf[k], strOp );
                            sb.append( op.to110XML() );

                            // FIRST LETTER UPPERCASE SECTION
                            strOp = strOp.substring( 0, 1 ).toUpperCase() + strOp.substring( 1 );
                            op = createOperation( OperationDefines.PROPERTYISLIKE, cf[k], strOp );
                            sb.append( op.to110XML() );
                        }
                    }
                }
            }
            sb.append( "</ogc:Or>" );
        }

        return sb.toString();
    }

    /**
     * Builds OGC Filterencoding fragment: for <code>CSWRequestmodel</code> field <b>topiccategory</b>.
     *
     * @return Returns the fragment for topiccategory. May be null, if no topiccategory is specified.
     */
    private String handleTopiccategory() {

        String rpcVal = null;
        if ( struct.getMember( RPC_TOPICCATEGORY ) != null ) {
            rpcVal = (String) struct.getMember( RPC_TOPICCATEGORY ).getValue();
        }

        if ( rpcVal != null && !rpcVal.startsWith( "..." ) && rpcVal.length() > 0 ) {
            String cf_props = requestElementsProps.getProperty( Constants.CONF_TOPICCATEGORY );
            String[] cf = cf_props.split( ";" );

            Operation op1 = createOperation( OperationDefines.PROPERTYISEQUALTO, cf[0], rpcVal );
            rpcVal = op1.to110XML().toString();
        } else {
            rpcVal = null;
        }

        return rpcVal;
    }

    /**
     * Build OGC Filterencoding fragment: Split <code>CSWRequestmodel</code> field <b>keywords</b> to one Comparison
     * Operation for each keyword.
     *
     * @return Returns the fragment for keywords. May be empty, if no keywords are specified.
     */
    private String handleKeywords() {

        StringBuffer sb = new StringBuffer( 1000 );
        String[] keywords = null;
        if ( struct.getMember( RPC_KEYWORDS ) != null ) {
            String s = (String) struct.getMember( RPC_KEYWORDS ).getValue();
            keywords = StringTools.toArray( s, ",;", true );
        }

        if ( ( keywords != null ) && ( keywords.length > 0 ) ) {
            String cf_props = requestElementsProps.getProperty( Constants.CONF_KEYWORDS );
            String[] cf = cf_props.split( ";" );

            sb = new StringBuffer( 1000 );
            int i = 0;

            for ( i = 0; i < keywords.length; i++ ) {
                if ( keywords[i].trim().length() > 0 ) {
                    Operation op1 = createOperation( OperationDefines.PROPERTYISEQUALTO, cf[0], keywords[i] );
                    sb.append( op1.to110XML() );
                }
            }

            if ( i > 1 ) {
                sb.insert( 0, "<ogc:Or>" );
                sb.append( "</ogc:Or>" );
            }
        }

        return sb.toString();
    }

    /**
     * Build OGC Filterencoding fragment: use <code>dateFrom</code> and <code>dateTo</code> to create Comparison
     * Operations.
     *
     * @return Returns the fragment for dates specified in the <code>RPCStruct</code>. May be null, if no dates are
     *         specified.
     */
    private String handleDate() {

        String s = null;

        if ( struct.getMember( Constants.RPC_DATEFROM ) == null && struct.getMember( Constants.RPC_DATETO ) == null ) {
            return s;
        }

        // RPC_DATEFROM
        String fromYear = null;
        String fromMonth = null;
        String fromDay = null;

        if ( struct.getMember( Constants.RPC_DATEFROM ) != null ) {
            RPCStruct st = (RPCStruct) struct.getMember( Constants.RPC_DATEFROM ).getValue();
            if ( st.getMember( Constants.RPC_YEAR ) != null ) {
                fromYear = st.getMember( Constants.RPC_YEAR ).getValue().toString();
            }
            if ( st.getMember( Constants.RPC_MONTH ) != null ) {
                fromMonth = st.getMember( Constants.RPC_MONTH ).getValue().toString();
            }
            if ( st.getMember( Constants.RPC_DAY ) != null ) {
                fromDay = st.getMember( Constants.RPC_DAY ).getValue().toString();
            }
        }

        if ( fromYear == null ) {
            fromYear = "0000";
        }
        if ( fromMonth == null ) {
            fromMonth = "1";
        }
        if ( Integer.parseInt( fromMonth ) < 10 ) {
            fromMonth = "0" + Integer.parseInt( fromMonth );
        }
        if ( fromDay == null ) {
            fromDay = "1";
        }
        if ( Integer.parseInt( fromDay ) < 10 ) {
            fromDay = "0" + Integer.parseInt( fromDay );
        }
        String dateFrom = fromYear + "-" + fromMonth + "-" + fromDay;

        // RPC_DATETO
        String toYear = null;
        String toMonth = null;
        String toDay = null;

        if ( struct.getMember( Constants.RPC_DATETO ) != null ) {
            RPCStruct st = (RPCStruct) struct.getMember( Constants.RPC_DATETO ).getValue();
            if ( st.getMember( Constants.RPC_YEAR ) != null ) {
                toYear = st.getMember( Constants.RPC_YEAR ).getValue().toString();
            }
            if ( st.getMember( Constants.RPC_MONTH ) != null ) {
                toMonth = st.getMember( Constants.RPC_MONTH ).getValue().toString();
            }
            if ( st.getMember( Constants.RPC_DAY ) != null ) {
                toDay = st.getMember( Constants.RPC_DAY ).getValue().toString();
            }
        }

        if ( toYear == null ) {
            toYear = "9999";
        }
        if ( toMonth == null ) {
            toMonth = "12";
        }
        if ( Integer.parseInt( toMonth ) < 10 ) {
            toMonth = "0" + Integer.parseInt( toMonth );
        }
        if ( toDay == null ) {
            toDay = "31";
        }
        if ( Integer.parseInt( toDay ) < 10 ) {
            toDay = "0" + Integer.parseInt( toDay );
        }
        String dateTo = toYear + "-" + toMonth + "-" + toDay;

        String date_props = requestElementsProps.getProperty( Constants.CONF_DATE );
        String[] conf_date = date_props.split( ";" );

        if ( ( toYear != null ) && ( toYear.length() > 0 ) ) {
            StringBuffer sb = new StringBuffer( "<ogc:And>" );

            Operation op1 = null;
            op1 = createOperation( OperationDefines.PROPERTYISGREATERTHANOREQUALTO, conf_date[0], dateFrom );
            sb.append( op1.to110XML() );
            op1 = createOperation( OperationDefines.PROPERTYISLESSTHANOREQUALTO, conf_date[0], dateTo );
            sb.append( op1.to110XML() );

            sb.append( "</ogc:And>" );
            s = sb.toString();
        }

        return s;
    }

    /**
     * Build OGC Filterencoding fragment: use <code>CSWRequestmodel</code> field <b>geographicBox</b> to create
     * Comparison Operation.
     *
     * @return Returns the fragment for the geographic bounding box. May be empty, if no bounding box is specified.
     * @throws CatalogClientException
     * @throws UnknownCRSException
     */
    private String handleBbox()
                            throws CatalogClientException, UnknownCRSException {

        StringBuffer sb = new StringBuffer( 1000 );
        if ( struct.getMember( Constants.RPC_BBOX ) != null ) {
            RPCStruct bboxStruct = (RPCStruct) struct.getMember( Constants.RPC_BBOX ).getValue();

            Double minx = (Double) bboxStruct.getMember( Constants.RPC_BBOXMINX ).getValue();
            Double miny = (Double) bboxStruct.getMember( Constants.RPC_BBOXMINY ).getValue();
            Double maxx = (Double) bboxStruct.getMember( Constants.RPC_BBOXMAXX ).getValue();
            Double maxy = (Double) bboxStruct.getMember( Constants.RPC_BBOXMAXY ).getValue();

            // FIXME check if srs is correct
            CoordinateSystem srs = CRSFactory.create( config.getSrs() );
            Envelope bbox = GeometryFactory.createEnvelope( minx.doubleValue(), miny.doubleValue(), maxx.doubleValue(),
                                                            maxy.doubleValue(), srs );
            try {
                // transform request boundingbox to EPSG:4326 because a ISO 19115
                // compliant catalog must store the bbox of an entry like this
                GeoTransformer geoTransformer = new GeoTransformer( "EPSG:4326" );
                bbox = geoTransformer.transform( bbox, config.getSrs() );

            } catch ( Exception e ) {
                throw new CatalogClientException( e.toString() );
            }

            Geometry boxGeom = null;
            try {
                boxGeom = GeometryFactory.createSurface( bbox, srs );
            } catch ( GeometryException e ) {
                throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_ERROR_CREATE_SURFACE",
                                                                       e.getMessage() ) );
            }

            String property = requestElementsProps.getProperty( Constants.CONF_GEOGRAPHICBOX );
            String[] re = property.split( ";" );

            if ( boxGeom != null ) {
                Operation op1 = createOperation( OperationDefines.BBOX, re[0], boxGeom );
                sb.append( op1.to110XML() );
            }
        }

        return sb.toString();
    }

    // /**
    // * @param bbox The bounding box to be used as filter condition.
    // * @return Returns the GML bounding box snippet.
    // */
    // private String createGMLBox( Envelope bbox ) {
    // StringBuffer sb = new StringBuffer( 1000 );
    //
    // sb.append( "<gml:Box xmlns:gml=\"http://www.opengis.net/gml\" >" );
    // sb.append( "<gml:coord><gml:X>" );
    // sb.append( "" + bbox.getMin().getX() );
    // sb.append( "</gml:X><gml:Y>" );
    // sb.append( "" + bbox.getMin().getY() );
    // sb.append( "</gml:Y></gml:coord><gml:coord><gml:X>" );
    // sb.append( "" + bbox.getMax().getX() );
    // sb.append( "</gml:X><gml:Y>" );
    // sb.append( "" + bbox.getMax().getY() );
    // sb.append( "</gml:Y></gml:coord></gml:Box>" );
    //
    // return sb.toString();
    // }

    /**
     * @param opId
     * @param property
     * @param value
     * @return Returns the operation to create.
     */
    private Operation createOperation( int opId, String property, Object value ) {

        Operation op = null;

        switch ( opId ) {
        case OperationDefines.PROPERTYISEQUALTO:
            op = new PropertyIsCOMPOperation( OperationDefines.PROPERTYISEQUALTO,
                                              new PropertyName( new QualifiedName( property ) ),
                                              new Literal( (String) value ) );
            break;
        case OperationDefines.PROPERTYISLIKE:

            char wildCard = WILDCARD;
            char singleChar = '?';
            char escapeChar = '/';
            String lit = wildCard + (String) value + wildCard;
            op = new PropertyIsLikeOperation( new PropertyName( new QualifiedName( property ) ), new Literal( lit ),
                                              wildCard, singleChar, escapeChar );
            break;
        case OperationDefines.PROPERTYISLESSTHANOREQUALTO:
            op = new PropertyIsCOMPOperation( OperationDefines.PROPERTYISLESSTHANOREQUALTO,
                                              new PropertyName( new QualifiedName( property ) ),
                                              new Literal( (String) value ) );
            break;
        case OperationDefines.PROPERTYISGREATERTHANOREQUALTO:
            op = new PropertyIsCOMPOperation( OperationDefines.PROPERTYISGREATERTHANOREQUALTO,
                                              new PropertyName( new QualifiedName( property ) ),
                                              new Literal( (String) value ) );
            break;
        case OperationDefines.BBOX:
            op = new SpatialOperation( OperationDefines.BBOX, new PropertyName( new QualifiedName( property ) ),
                                       (Geometry) value );
            break;
        case OperationDefines.PROPERTYISNULL:
            op = new PropertyIsNullOperation( new PropertyName( new QualifiedName( property ) ) );
            break;
        default:
            op = new PropertyIsCOMPOperation( OperationDefines.PROPERTYISEQUALTO,
                                              new PropertyName( new QualifiedName( property ) ),
                                              new Literal( (String) value ) );
        }

        return op;
    }

}
