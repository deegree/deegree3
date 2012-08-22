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
package org.deegree.portal.cataloguemanager.control;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.deegree.datatypes.QualifiedName;
import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.Pair;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.filterencoding.Literal;
import org.deegree.model.filterencoding.LogicalOperation;
import org.deegree.model.filterencoding.Operation;
import org.deegree.model.filterencoding.OperationDefines;
import org.deegree.model.filterencoding.PropertyIsBetweenOperation;
import org.deegree.model.filterencoding.PropertyIsLikeOperation;
import org.deegree.model.filterencoding.PropertyIsNullOperation;
import org.deegree.model.filterencoding.PropertyName;
import org.deegree.model.filterencoding.SpatialOperation;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.SortProperty;
import org.deegree.ogcwebservices.csw.discovery.GetRecords;
import org.deegree.ogcwebservices.csw.discovery.Query;
import org.deegree.ogcwebservices.csw.discovery.GetRecords.RESULT_TYPE;
import org.deegree.portal.cataloguemanager.model.ExceptionBean;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class MetadataSearchListener extends AbstractSearchListener {

    private static final ILogger LOG = LoggerFactory.getLogger( MetadataSearchListener.class );

    // private String[] ignoredChars = new String[] { ",", ";", "\\.", "\\+", "\\(", "\\)" };

    // private String[] cqp = new String[] { "{http://www.opengis.net/cat/csw/apiso/1.0}:Title" };

    // private String[] timecqp = new String[] { "{http://www.opengis.net/cat/csw/apiso/1.0}:date" };

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.enterprise.control.ajax.AbstractListener#actionPerformed(org.deegree.enterprise.control.ajax.WebEvent
     * , org.deegree.enterprise.control.ajax.ResponseHandler)
     */
    @SuppressWarnings("unchecked")
    public void actionPerformed( WebEvent event, ResponseHandler resp )
                            throws IOException {

        config = getCatalogueManagerConfiguration( event );
        Map<String, String> param = event.getParameter();
        String query = param.get( "freeText" );
        String startDate = param.get( "from" );
        String endDate = param.get( "to" );
        String geographicIdentifier = param.get( "geogrID" );
        List<String> queryStrings = parseQuery( config, query );
        String bbox = getBBOX( geographicIdentifier );
        GetRecords getRecords;
        try {
            getRecords = createGetRecords( queryStrings, startDate, endDate, bbox );
        } catch ( Exception e ) {
            LOG.logError( e );
            ExceptionBean bean = new ExceptionBean( this.getClass().getName(), e.getMessage() );
            resp.writeAndClose( true, bean );
            return;
        }
        List<Pair<String, XMLFragment>> result;
        try {
            result = performQuery( getRecords );
        } catch ( Throwable e ) {
            LOG.logError( e );
            ExceptionBean bean = new ExceptionBean( this.getClass().getName(), e.getMessage() );
            resp.writeAndClose( true, bean );
            return;
        }
        List<SearchResultBean> formattedResult = null;
        try {
            formattedResult = formatResult( result );
        } catch ( Exception e ) {
            LOG.logError( e );
            ExceptionBean bean = new ExceptionBean( this.getClass().getName(), e.getMessage() );
            resp.writeAndClose( true, bean );
            return;
        }

        // store search meta informations to be used for pageing
        event.getSession().setAttribute( SEARCHRESULTINFO, searchResultInfos );

        int c = 0;
        for ( SearchResultInfo sri : searchResultInfos ) {
            c += sri.getNumberOfRecordsMatched();
        }

        event.getSession().setAttribute( CURRENTSEARCH, getRecords );
        event.getSession().setAttribute( COUNTER, 0 );

        String charEnc = Charset.defaultCharset().displayName();
        resp.setContentType( "application/json; charset=" + charEnc );
        resp.writeAndClose( false, new Object[] { formattedResult, new Integer( c ), new Integer( 0 ) } );

    }

    /**
     * @param queryStrings
     * @param startDate
     * @param endDate
     * @param bbox
     * @return GetRecord request
     * @throws Exception
     */
    private GetRecords createGetRecords( List<String> queryStrings, String startDate, String endDate, String bbox )
                            throws Exception {
        List<Operation> opList = new ArrayList<Operation>( queryStrings.size() + 3 );
        List<Operation> notOpList = new ArrayList<Operation>( queryStrings.size() + 3 );
        for ( String searchString : queryStrings ) {
            for ( String property : config.getSearchableProperties() ) {
                PropertyName propertyName = new PropertyName( new QualifiedName( property ) );
                Operation operation = null;
                if ( searchString.startsWith( "-" ) ) {
                    Literal literal = new Literal( '*' + searchString.substring( 1 ) + '*' );
                    operation = new PropertyIsLikeOperation( propertyName, literal, '*', '?', '/', false );
                    List<Operation> tmp = new ArrayList<Operation>();
                    tmp.add( operation );
                    operation = new LogicalOperation( OperationDefines.NOT, tmp );
                    Operation notNull = new PropertyIsNullOperation( propertyName );
                    tmp = new ArrayList<Operation>();
                    tmp.add( operation );
                    tmp.add( notNull );
                    notOpList.add( new LogicalOperation( OperationDefines.OR, tmp ) );
                } else {
                    Literal literal = new Literal( '*' + searchString + '*' );
                    operation = new PropertyIsLikeOperation( propertyName, literal, '*', '?', '/', false );
                    opList.add( operation );
                }

            }
        }
        LogicalOperation compare = null;
        if ( opList.size() > 1 && notOpList.size() > 0 ) {
            compare = new LogicalOperation( OperationDefines.OR, opList );
            notOpList.add( compare );
            compare = new LogicalOperation( OperationDefines.AND, notOpList );
        } else if ( opList.size() > 1 && notOpList.size() == 0 ) {
            compare = new LogicalOperation( OperationDefines.OR, opList );
        } else if ( opList.size() == 0 && notOpList.size() > 1 ) {
            compare = new LogicalOperation( OperationDefines.AND, notOpList );
        } else if ( opList.size() == 0 && notOpList.size() == 0 ) {
            compare = new LogicalOperation( OperationDefines.AND, notOpList );
        }

        List<Operation> opTimeList = new ArrayList<Operation>( queryStrings.size() + 3 );
        for ( String property : config.getDateProperties() ) {
            PropertyName propertyName = new PropertyName( new QualifiedName( property ) );
            Literal start = new Literal( startDate );
            Literal end = new Literal( endDate );
            Operation operation = new PropertyIsBetweenOperation( propertyName, start, end );
            opTimeList.add( operation );
        }

        List<Operation> allOps = new ArrayList<Operation>();
        if ( opList.size() > 1 || notOpList.size() > 1 ) {
            allOps.add( compare );
        } else if ( opList.size() == 1 && notOpList.size() == 1 ) {
            allOps.add( opList.get( 0 ) );
            allOps.add( notOpList.get( 0 ) );
        } else if ( opList.size() == 0 && notOpList.size() == 1 ) {
            allOps.add( notOpList.get( 0 ) );
        } else if ( opList.size() == 1 && notOpList.size() == 0 ) {
            allOps.add( opList.get( 0 ) );
        }

        LogicalOperation time = new LogicalOperation( OperationDefines.OR, opTimeList );
        allOps.add( time );

        if ( bbox != null ) {
            QualifiedName qn = new QualifiedName( "{http://www.opengis.net/cat/csw/apiso/1.0}:BoundingBox" );
            PropertyName propertyName = new PropertyName( qn );
            CoordinateSystem crs = CRSFactory.create( "EPSG:4326" );
            Geometry geom = GeometryFactory.createSurface( GeometryFactory.createEnvelope( bbox, crs ), crs );
            SpatialOperation sp = new SpatialOperation( OperationDefines.BBOX, propertyName, geom );
            allOps.add( sp );
        }

        Filter filter = null;
        if ( allOps.size() > 1 ) {
            filter = new ComplexFilter( new LogicalOperation( OperationDefines.AND, allOps ) );
        } else {
            filter = new ComplexFilter( allOps.get( 0 ) );
        }
        List<QualifiedName> typeNames = new ArrayList<QualifiedName>();
        typeNames.add( new QualifiedName( "{http://www.isotc211.org/2005/gmd}:MD_Metadata" ) );
        SortProperty[] sortProperties = SortProperty.create( "apiso:Title",
                                                             CommonNamespaces.getNamespaceContext().getNamespaceMap() );
        Query query = new Query( "brief", new ArrayList<QualifiedName>(), new HashMap<String, QualifiedName>(),
                                 new ArrayList<PropertyPath>(), filter, sortProperties, typeNames,
                                 new HashMap<String, QualifiedName>() );
        return new GetRecords( UUID.randomUUID().toString(), "2.0.2", null, null, RESULT_TYPE.RESULTS,
                               "application/xml", "http://www.isotc211.org/2005/gmd", 1, config.getStepSize(), -1,
                               null, query );
    }

    /**
     * @param geographicIdentifier
     * @return
     */
    private String getBBOX( String geographicIdentifier ) {
        if ( geographicIdentifier != null ) {
            List<SpatialExtent> extents = config.getSpatialExtents();
            for ( SpatialExtent spatialExtent : extents ) {
                if ( spatialExtent.getId().equalsIgnoreCase( geographicIdentifier ) ) {
                    return spatialExtent.getBbox();
                }
            }
        }
        return null;
    }

    /**
     * 
     * @param config
     * @param query
     * @return
     */
    private List<String> parseQuery( CatalogueManagerConfiguration config, String query ) {
        char[] ignoredChars = config.getIgnoreCharacters();
        for ( int i = 0; i < ignoredChars.length; i++ ) {
            query = query.replace( ignoredChars[i], ' ' ).trim();
        }
        List<String> list = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        boolean open = false;
        for ( int i = 0; i < query.length(); i++ ) {
            if ( query.charAt( i ) == ' ' && !open && sb.length() > 0 ) {
                list.add( sb.toString() );
                sb.delete( 0, sb.length() );
            } else if ( ( query.charAt( i ) == '"' || query.charAt( i ) == '\'' ) && !open ) {
                open = true;
            } else if ( ( query.charAt( i ) == '"' || query.charAt( i ) == '\'' ) && open ) {
                open = false;
            } else if ( query.charAt( i ) != ' ' || open ) {
                sb.append( query.charAt( i ) );
            }
        }
        if ( sb.length() > 0 && sb.charAt( sb.length() - 1 ) != '"' && sb.charAt( sb.length() - 1 ) != '\'' ) {
            list.add( sb.toString() );
        }
        return list;
    }

}
