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
package org.deegree.portal.standard.digitizer.control;

import static java.util.Collections.singletonList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.datatypes.UnknownTypeException;
import org.deegree.enterprise.control.ajax.AbstractListener;
import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.FileUtils;
import org.deegree.framework.util.HttpUtils;
import org.deegree.framework.util.TimeTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.filterencoding.FeatureFilter;
import org.deegree.model.filterencoding.FeatureId;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.WKTAdapter;
import org.deegree.ogcwebservices.wfs.XMLFactory;
import org.deegree.ogcwebservices.wfs.operation.transaction.Delete;
import org.deegree.ogcwebservices.wfs.operation.transaction.Insert;
import org.deegree.ogcwebservices.wfs.operation.transaction.Insert.ID_GEN;
import org.deegree.ogcwebservices.wfs.operation.transaction.Transaction;
import org.deegree.ogcwebservices.wfs.operation.transaction.TransactionOperation;
import org.deegree.portal.Constants;
import org.deegree.portal.context.ViewContext;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SaveFeatureListener extends AbstractListener {

    private static final ILogger LOG = LoggerFactory.getLogger( SaveFeatureListener.class );

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.enterprise.control.ajax.AbstractListener#actionPerformed(org.deegree.enterprise.control.ajax.WebEvent
     * , org.deegree.enterprise.control.ajax.ResponseHandler)
     */
    @Override
    public void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {
        HttpSession session = event.getSession();
        ViewContext vc = (ViewContext) session.getAttribute( Constants.CURRENTMAPCONTEXT );

        List<Map<String, Object>> list = (List<Map<String, Object>>) event.getParameter().get( "featureMap" );
        for ( Map<String, Object> map : list ) {
            Map<String, Object> attributes = (Map<String, Object>) ( (List<Object>) map.get( "attributes" ) ).get( 0 );
            LOG.logDebug( "attributes: ", attributes );
            Map<String, Object> featureTypeMap = (Map<String, Object>) attributes.get( "$FEATURETYPE$" );
            LOG.logDebug( "featureType: ", featureTypeMap );
            FeatureType featureType;
            try {
                featureType = createFeatureType( featureTypeMap );
            } catch ( Exception e ) {
                handleException( responseHandler, e );
                return;
            }
            LOG.logDebug( "geometry: ", map.get( "geometry" ) );
            Geometry geometry;
            try {
                geometry = createGeometry( (String) map.get( "geometry" ), vc );
            } catch ( Exception e ) {
                handleException( responseHandler, e );
                return;
            }
            Feature feature = null;
            try {
                feature = createFeature( attributes, geometry, featureType );
            } catch ( Exception e ) {
                handleException( responseHandler, e );
                return;
            }
            FeatureCollection fc = FeatureFactory.createFeatureCollection( "UUID" + UUID.randomUUID().toString(),
                                                                           new Feature[] { feature } );

            URL url = new URL( (String) featureTypeMap.get( "wfsURL" ) );
            if ( "INSERT".equalsIgnoreCase( (String) attributes.get( "$ACTION$" ) ) ) {
                handleInsert( responseHandler, url, fc );
            }
            if ( "DELETE".equalsIgnoreCase( (String) attributes.get( "$ACTION$" ) ) ) {
                handleDelete( responseHandler, url, fc );
            }
        }

    }

    private void handleInsert( ResponseHandler responseHandler, URL url, FeatureCollection fc )
                            throws IOException {
        Insert insert = new Insert( UUID.randomUUID().toString(), ID_GEN.GENERATE_NEW, null, fc );
        List<TransactionOperation> tmp = new ArrayList<TransactionOperation>();
        tmp.add( insert );
        try {
            performTransaction( url, tmp, null, null );
        } catch ( Exception e ) {
            handleException( responseHandler, e );
            return;
        }
    }

    private void handleDelete( ResponseHandler responseHandler, URL url, FeatureCollection fc )
                            throws IOException {
        FeatureId id = new FeatureId( fc.getFeature( 0 ).getId() );
        ArrayList<FeatureId> fids = new ArrayList<FeatureId>( singletonList( id ) );
        Delete delete = new Delete( UUID.randomUUID().toString(), fc.getFeature( 0 ).getFeatureType().getName(),
                                    new FeatureFilter( fids ) );
        List<TransactionOperation> tmp = new ArrayList<TransactionOperation>();
        tmp.add( delete );
        try {
            performTransaction( url, tmp, null, null );
        } catch ( Exception e ) {
            handleException( responseHandler, e );
            return;
        }
    }

    private static XMLFragment performTransaction( URL wfsURL, List<TransactionOperation> list, String user,
                                                   String password )
                            throws Exception {

        Transaction transaction = new Transaction( null, null, null, null, list, true, null );
        XMLFragment xml = XMLFactory.export( transaction );
        // HttpUtils.addAuthenticationForXML( xml, appCont.getUser(), appCont.getPassword(), //
        // appCont.getCertificate( wfsURL.toURI().toASCIIString() ) );
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            LOG.logDebug( "WFS Transaction: ", xml.getAsString() );
        }
        InputStream is = HttpUtils.performHttpPost( wfsURL.toURI().toASCIIString(), xml, timeout, user, password, null ).getResponseBodyAsStream();
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            String st = FileUtils.readTextFile( is ).toString();
            is = new ByteArrayInputStream( st.getBytes() );
            LOG.logDebug( "WFS transaction result: ", st );
        }
        xml = new XMLFragment();
        xml.load( is, wfsURL.toExternalForm() );
        if ( "ExceptionReport".equalsIgnoreCase( xml.getRootElement().getLocalName() ) ) {
            LOG.logError( "Transaction on: " + xml.getAsString() + " failed" );
            // TODO // extract exception message
            throw new Exception( xml.getAsString() );
        }
        return xml;
    }

    /**
     * @param wkt
     * @return
     * @throws GeometryException
     */
    private static Geometry createGeometry( String wkt, ViewContext vc )
                            throws GeometryException {
        return WKTAdapter.wrap( wkt, vc.getGeneral().getBoundingBox()[0].getCoordinateSystem() );
    }

    /**
     * @param attributes
     * @param geometry
     * @param featureType
     * @return
     */
    private static Feature createFeature( Map<String, Object> attributes, Geometry geometry, FeatureType featureType ) {

        PropertyType[] pts = featureType.getProperties();
        FeatureProperty[] fps = new FeatureProperty[pts.length];
        for ( int i = 0; i < fps.length; i++ ) {
            if ( pts[i].getType() == Types.GEOMETRY ) {
                fps[i] = FeatureFactory.createFeatureProperty( pts[i].getName(), geometry );
                continue;
            }
            if ( attributes.get( pts[i].getName().getFormattedString() ) == null ) {
                fps[i] = FeatureFactory.createFeatureProperty( pts[i].getName(), null );
                continue;
            }
            String value = attributes.get( pts[i].getName().getFormattedString() ).toString();
            switch ( pts[i].getType() ) {
            case Types.VARCHAR: {
                fps[i] = FeatureFactory.createFeatureProperty( pts[i].getName(), value );
                break;
            }
            case Types.FLOAT:
            case Types.DOUBLE: {
                fps[i] = FeatureFactory.createFeatureProperty( pts[i].getName(), Float.parseFloat( value ) );
                break;
            }
            case Types.BIGINT:
            case Types.TINYINT:
            case Types.INTEGER:
            case Types.SMALLINT: {
                fps[i] = FeatureFactory.createFeatureProperty( pts[i].getName(), Integer.parseInt( value ) );
                break;
            }
            case Types.BOOLEAN: {
                fps[i] = FeatureFactory.createFeatureProperty( pts[i].getName(), "true".equalsIgnoreCase( value ) );
                break;
            }
            case Types.TIMESTAMP:
            case Types.DATE: {
                Date date = TimeTools.createDate( value );
                fps[i] = FeatureFactory.createFeatureProperty( pts[i].getName(), date );
                break;
            }
            }

        }
        return FeatureFactory.createFeature( "UUID_" + UUID.randomUUID().toString(), featureType, fps );
    }

    private static FeatureType createFeatureType( Map<String, Object> featureType )
                            throws UnknownTypeException {
        String featureTypeName = (String) featureType.get( "name" );
        String featureTypeNamespace = (String) featureType.get( "namespace" );
        String geoPropName = (String) featureType.get( "geomPropertyName" );
        String geoPropNamespace = (String) featureType.get( "geomPropertyNamespace" );

        List<PropertyType> propertyTypes = new ArrayList<PropertyType>();
        QualifiedName qn = null;

        List<Map<String, Object>> properties = (List<Map<String, Object>>) featureType.get( "properties" );
        for ( Map<String, Object> map : properties ) {
            int type = Types.getTypeCodeForSQLType( (String) map.get( "type" ) );
            String name = (String) map.get( "name" );
            URI nsp = URI.create( (String) map.get( "namespace" ) );
            qn = new QualifiedName( name, nsp );
            propertyTypes.add( FeatureFactory.createSimplePropertyType( qn, type, false ) );
        }
        qn = new QualifiedName( geoPropName, URI.create( geoPropNamespace ) );
        propertyTypes.add( FeatureFactory.createSimplePropertyType( qn, Types.GEOMETRY, false ) );
        qn = new QualifiedName( featureTypeName, URI.create( featureTypeNamespace ) );
        return FeatureFactory.createFeatureType( qn, false,
                                                 propertyTypes.toArray( new PropertyType[propertyTypes.size()] ) );
    }

}
