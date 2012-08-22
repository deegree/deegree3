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

package org.deegree.ogcwebservices.wpvs;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.IDGenerator;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.FeatureFilter;
import org.deegree.model.filterencoding.FeatureId;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.spatialschema.GMLGeometryAdapter;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wfs.operation.FeatureResult;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;
import org.deegree.ogcwebservices.wpvs.configuration.AbstractDataSource;
import org.deegree.ogcwebservices.wpvs.configuration.LocalWFSDataSource;
import org.deegree.ogcwebservices.wpvs.j3d.DefaultSurface;
import org.deegree.ogcwebservices.wpvs.j3d.Object3DFactory;
import org.deegree.ogcwebservices.wpvs.j3d.PointsToPointListFactory;
import org.deegree.ogcwebservices.wpvs.j3d.TexturedSurface;
import org.deegree.ogcwebservices.wpvs.utils.ResolutionStripe;
import org.w3c.dom.Document;

/**
 * Invoker for a Web Feature Service.
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 *
 * $Revision$, $Date$
 *
 */
public class WFSInvoker extends GetViewServiceInvoker {

    private static final ILogger LOG = LoggerFactory.getLogger( WFSInvoker.class );

    /* whether the returned data is a 3D object or data for the elevation model */
    private final boolean isElevationModelRequest;

    private int id;

    /**
     * Creates a new instance of this class.
     *
     * @param owner
     *            the ResolutionStripe that calls this invoker
     * @param id
     * @param isElevationModelRequest
     */
    public WFSInvoker( ResolutionStripe owner, int id, boolean isElevationModelRequest ) {
        super( owner );
        this.id = id;
        this.isElevationModelRequest = isElevationModelRequest;
    }

    @Override
    public void invokeService( AbstractDataSource dataSource ) {
        if ( !( dataSource instanceof LocalWFSDataSource ) ) {
            LOG.logError( "The given AbstractDataSource is no WFSDataSource instance. It is needed for a WFSInvoker" );
            throw new RuntimeException( "DataSource should be a WFS-instance for a WFSInvoker" );
        }
        OGCWebService service = null;
        try {
            service = dataSource.getOGCWebService();
        } catch ( OGCWebServiceException ogcwe ) {
            LOG.logError( ogcwe.getMessage() );
        }
        Object response = null;
        try {
            GetFeature getFeature = createGetFeatureRequest( (LocalWFSDataSource) dataSource );
            if ( service != null ) {
                response = service.doService( getFeature );
            } else {
                LOG.logError( "The (WFS) service was null, therefore no response was given" );
            }
        } catch ( OGCWebServiceException ogcwse ) {
            if ( !Thread.currentThread().isInterrupted() ) {
                LOG.logError( "Exception while performing WFS-GetFeature: ", ogcwse );
            }
            return;
        }

        if ( response != null && response instanceof FeatureResult ) {
            FeatureCollection result = (FeatureCollection) ( (FeatureResult) response ).getResponse();
            if ( result != null ) {
                if ( isElevationModelRequest ) {
                    PointsToPointListFactory ptpFac = new PointsToPointListFactory();
                    resolutionStripe.setElevationModelFromMeassurePoints( ptpFac.createFromFeatureCollection( result ) );
                } else {
                    Object3DFactory o3DFactory = new Object3DFactory();
                    Map<String, TexturedSurface> textureMap = new HashMap<String, TexturedSurface>( result.size() * 10 );
                    for ( int i = 0; i < result.size(); ++i ) {
                        Feature feature = result.getFeature( i );
                        createSurfaces( o3DFactory, feature, textureMap );
                    }
                    if ( textureMap.size() > 0 ) {
                        Set<String> keys = textureMap.keySet();
                        int count = 0;
                        int total = textureMap.size();
                        for ( String key : keys ) {
                            if ( key != null ) {
                                TexturedSurface surf = textureMap.get( key );
                                if ( surf != null ) {
                                    if ( LOG.isDebug() ) {
                                        LOG.logDebug( "Adding textured surface (" + ( count++ ) + " of " + total
                                                      + "): " + key );
                                    }
                                    surf.compile();
                                    resolutionStripe.addFeature( id + "_" + surf.getDefaultSurfaceID(), surf );
                                }
                            }

                        }
                    }
                }

            }
        } else {
            LOG.logError( "ERROR while invoking wfs-datasource: " + dataSource.getName()
                          + " the result was no WFS-response or no FeatureResult instance" );
        }

    }

    /**
     * This method recursively constructs all the surfaces contained in the given feature. If the
     * Feature contains a PropertyType of {@link Types#FEATURE} this Feature will also be traversed,
     * if it contains a {@link Types#GEOMETRY} a {@link DefaultSurface} will be created.
     *
     * @param o3DFactory
     *            the Factory to create the defaultservice
     * @param feature
     *            the feature to traverse.
     */
    private void createSurfaces( Object3DFactory o3DFactory, Feature feature, Map<String, TexturedSurface> textureMap ) {

        FeatureType ft = feature.getFeatureType();
        PropertyType[] propertyTypes = ft.getProperties();
        for ( PropertyType pt : propertyTypes ) {
            if ( pt.getType() == Types.FEATURE ) {
                FeatureProperty[] fp = feature.getProperties( pt.getName() );
                if ( fp != null ) {
                    for ( int i = 0; i < fp.length; i++ ) {
                        createSurfaces( o3DFactory, (Feature) fp[i].getValue(), textureMap );
                    }
                }
            } else if ( pt.getType() == Types.GEOMETRY ) {
                DefaultSurface ds = o3DFactory.createSurface( feature, textureMap );
                if ( ds != null ) {
                    ds.compile();
                    resolutionStripe.addFeature( id + "_" + ds.getDefaultSurfaceID(), ds );
                }
            }
        }
    }

    /**
     * Creates a new <code>GetFeature</code> object from an "XML-String" not nice.
     *
     * @param dataSource
     *            the datasource containing service data
     * @return a new GetFeature request
     */
    private GetFeature createGetFeatureRequest( LocalWFSDataSource dataSource /*
                                                                                 * String id,
                                                                                 * Surface[] boxes
                                                                                 */)
                            throws OGCWebServiceException {

        QualifiedName qn = dataSource.getName();

        StringBuilder sb = new StringBuilder( 5000 );
        sb.append( "<?xml version='1.0' encoding='" + CharsetUtils.getSystemCharset() + "'?>" );
        sb.append( "<wfs:GetFeature xmlns:wfs='http://www.opengis.net/wfs' " );
        sb.append( "xmlns:ogc='http://www.opengis.net/ogc' " );
        sb.append( "xmlns:gml='http://www.opengis.net/gml' " );
        sb.append( "xmlns:" ).append( qn.getPrefix() ).append( '=' );
        sb.append( "'" ).append( qn.getNamespace() ).append( "' " );
        if ( dataSource.getMaxFeatures() > 0 ) {
            sb.append( "maxFeatures='" ).append( dataSource.getMaxFeatures() ).append( "' " );
        }

        if ( dataSource.getServiceType() == AbstractDataSource.LOCAL_WFS ) {
            sb.append( "outputFormat='FEATURECOLLECTION'>" );
        } else {
            sb.append( "outputFormat='text/xml; subtype=gml/3.1.1'>" );
        }

        /**
         * To make things a little clearer compare with this SQL-Statement: SELECT ( !texture )?
         * geoProperty : * FROM qn.getLocalName() WHERE geoPoperty intersects with
         * resolutionStripe.getSurface() AND FilterConditions.
         */
        PropertyPath geoProperty = dataSource.getGeometryProperty();

        // FROM
        sb.append( "<wfs:Query typeName='" ).append( qn.getPrefix() ).append( ":" );
        sb.append( qn.getLocalName() ).append( "'>" );

        // SELECT
        /*
         * if ( !isElevationModelRequest ) { sb.append( "<wfs:PropertyName>" ); sb.append(
         * geoProperty.getAsString() ); sb.append( "</wfs:PropertyName>" ); }
         */

        StringBuffer sbArea = GMLGeometryAdapter.exportAsEnvelope( resolutionStripe.getSurface().getEnvelope() );

        // WHERE
        sb.append( "<ogc:Filter>" );

        // AND
        Filter filter = dataSource.getFilter();
        if ( filter != null ) {
            if ( filter instanceof ComplexFilter ) {
                sb.append( "<ogc:And>" );
                sb.append( "<ogc:Intersects>" );
                sb.append( "<wfs:PropertyName>" );
                sb.append( geoProperty.getAsString() );
                sb.append( "</wfs:PropertyName>" );
                sb.append( sbArea );
                sb.append( "</ogc:Intersects>" );
                // add filter as defined in the layers datasource description
                // to the filter expression
                org.deegree.model.filterencoding.Operation op = ( (ComplexFilter) filter ).getOperation();
                sb.append( op.to110XML() ).append( "</ogc:And>" );
            } else {
                if ( filter instanceof FeatureFilter ) {
                    ArrayList<FeatureId> featureIds = ( (FeatureFilter) filter ).getFeatureIds();
                    if ( featureIds.size() != 0 )
                        sb.append( "<ogc:And>" );
                    for ( FeatureId fid : featureIds ) {
                        sb.append( fid.toXML() );
                    }
                    if ( featureIds.size() != 0 )
                        sb.append( "</ogc:And>" );
                }
            }
        } else {
            sb.append( "<ogc:Intersects>" );
            sb.append( "<wfs:PropertyName>" );
            sb.append( geoProperty.getAsString() );
            sb.append( "</wfs:PropertyName>" );
            sb.append( sbArea );
            sb.append( "</ogc:Intersects>" );
        }

        sb.append( "</ogc:Filter></wfs:Query></wfs:GetFeature>" );

        Document doc;
        try {
            doc = XMLTools.parse( new StringReader( sb.toString() ) );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( e.getMessage() );
        }

        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            System.out.println( sb.toString() );
            XMLFragment d = new XMLFragment( doc.getDocumentElement() );
            LOG.logDebug( d.getAsPrettyString() );
        }

        IDGenerator idg = IDGenerator.getInstance();
        return GetFeature.create( String.valueOf( idg.generateUniqueID() ), doc.getDocumentElement() );
    }

}
