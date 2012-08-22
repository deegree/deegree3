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

import java.util.ArrayList;
import java.util.List;

import javax.media.j3d.Locale;
import javax.media.j3d.PickShape;
import javax.media.j3d.SceneGraphPath;
import javax.media.j3d.Shape3D;
import javax.media.j3d.VirtualUniverse;

import org.deegree.datatypes.Types;
import org.deegree.framework.util.IDGenerator;
import org.deegree.i18n.Messages;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.OperationDefines;
import org.deegree.model.filterencoding.PropertyName;
import org.deegree.model.filterencoding.SpatialOperation;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.Position;
import org.deegree.model.spatialschema.Surface;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wfs.operation.FeatureResult;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;
import org.deegree.ogcwebservices.wfs.operation.Query;
import org.deegree.ogcwebservices.wfs.operation.GetFeature.RESULT_TYPE;
import org.deegree.ogcwebservices.wms.LayerNotDefinedException;
import org.deegree.ogcwebservices.wms.LayerNotQueryableException;
import org.deegree.ogcwebservices.wpvs.capabilities.Dataset;
import org.deegree.ogcwebservices.wpvs.configuration.AbstractDataSource;
import org.deegree.ogcwebservices.wpvs.configuration.LocalWFSDataSource;
import org.deegree.ogcwebservices.wpvs.configuration.RemoteWFSDataSource;
import org.deegree.ogcwebservices.wpvs.configuration.WPVSConfiguration;
import org.deegree.ogcwebservices.wpvs.operation.ConeRequest;
import org.deegree.ogcwebservices.wpvs.operation.Get3DFeatureInfo;
import org.deegree.ogcwebservices.wpvs.operation.Get3DFeatureInfoResponse;
import org.deegree.ogcwebservices.wpvs.operation.LineRequest;
import org.deegree.ogcwebservices.wpvs.operation.RequestGeometry;

import com.sun.j3d.utils.geometry.GeometryInfo;

/**
 * This class handles a WPVS Get3DFeatureInfo-Request.
 * 
 * 
 * @version $Revision$
 * @author <a href="mailto:cordes@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author$
 * 
 * @version 1.0. $Revision$, $Date$
 * 
 */
public class DefaultGet3DFeatureInfoHandler extends Get3DFeatureInfoHandler {

    private WPVSConfiguration configuration;

    private int featureCount;

    // collects all Datasets to query
    private ArrayList<Dataset> allDatasets;

    // collects the responses for each dataset
    private ArrayList<Feature> featCol;

    // stores the geometry of the request
    private RequestGeometry geom;

    private Get3DFeatureInfo request;

    /**
     * Create a new instance
     * 
     * @param owner
     *            the calling service instance.
     */
    public DefaultGet3DFeatureInfoHandler( WPVService owner ) {
        super( owner );
        this.configuration = owner.getConfiguration();
    }

    @Override
    /**
     * Handles the Get3DfeatureInfo request given by <code>Get3DFeatureInfoRequest</code>
     *
     * @param request
     *            the Get3DFeatureInfo-request
     * @return an instance of Get3DFeatureInfoResponse
     */
    public Get3DFeatureInfoResponse handleRequest( Get3DFeatureInfo request )
                            throws OGCWebServiceException {

        this.request = request;
        // Anzahl der zurueckzugebenden Features
        featureCount = request.getFeatureCount();

        // stores all datasets to query (even the child-datasets)
        allDatasets = new ArrayList<Dataset>();

        // validate QueryDatasets
        List<String> qDatasets = new ArrayList<String>( request.getQueryDatasets().size() );
        qDatasets = request.getQueryDatasets();
        for ( String s : qDatasets ) {
            Dataset dataset = configuration.findDataset( s );
            // test if dataset is configured
            if ( dataset == null ) {
                throw new LayerNotDefinedException( Messages.getMessage( "WPVS_UNDEFINED_DATASET", "(null)" ) );
            }
            // test if queryable
            if ( !dataset.getQueryable() ) {
                throw new LayerNotQueryableException( Messages.getMessage( "WPVS_NO_QUERYABLE_DATASET",
                                                                           dataset.getName() ) );
            }
            // test if dataset is given in GetViewRequestCopy
            List<String> ds = request.getGetViewRequestCopy().getDatasets();
            boolean given = false;

            for ( String dsString : ds ) {
                if ( dsString.equals( dataset.getName() ) ) {
                    given = true;
                }
            }
            if ( !given ) {
                throw new OGCWebServiceException( Messages.getMessage( "WPVS_INVISIBLE_DATASET", dataset.getName() ) );
            }
            // test for a valid CRS
            CoordinateSystem[] csArray = dataset.getCrs();
            boolean crs = false;
            for ( int i = 0; i < csArray.length; i++ ) {
                if ( csArray[i].equals( request.getGetViewRequestCopy().getCrs() ) ) {
                    crs = true;
                }
            }
            if ( !crs ) {
                throw new OGCWebServiceException( Messages.getMessage( "WPVS_INVALID_CRS", dataset.getName(),
                                                                       request.getGetViewRequestCopy().getCrs() ) );
            }

            allDatasets.add( dataset );
            addChildDataset( dataset.getDatasets() );
        }

        // initialize the request geometry
        try {
            if ( request.getApexAngle() != 0 ) {
                geom = new ConeRequest( request );
            } else {
                geom = new LineRequest( request );
                // }
                // }

            }
            // sets the geometry for WFS GetFeature-Request
            geom.setWfsReqGeom();
            // sets the 3d-geometry for final test of intersection with a feature
            geom.setPickshape();

        } catch ( GeometryException e ) {
            throw new OGCWebServiceException( Messages.getMessage( "WPVS_IMPOSSIBLE_CREATE_GEOMETRY" ) );
        }

        // query WFS
        featCol = new ArrayList<Feature>();
        handleGetFeatureRequest( allDatasets );
        Get3DFeatureInfoResponse response = createGet3DFeatureInfoResponse();
        return response;
    }

    /**
     * Adds the subdatasets to the collection of all datasets to query.
     * 
     * @param ds
     *            an array of subdatasets to add to the Collection of all Datasets
     * 
     */
    private void addChildDataset( Dataset[] ds ) {
        for ( int i = 0; i < ds.length; i++ ) {

            // test for a valid CRS
            CoordinateSystem[] csArray = ds[i].getCrs();
            boolean crs = false;
            for ( int j = 0; j < csArray.length; j++ ) {
                if ( csArray[j].equals( request.getGetViewRequestCopy().getCrs() ) ) {
                    crs = true;
                }
            }
            // test if dataset is queryable
            if ( ds[i].getQueryable() && crs == true ) {
                allDatasets.add( ds[i] );
            }
            if ( ds[i].getDatasets().length != 0 ) {
                addChildDataset( ds[i].getDatasets() );
            }
        }
    }

    /**
     * Tests the parameter feat for an intersection with the Ray of view and calculates the distance between ViewPoint
     * and feature.
     * 
     * @param feat
     *            the Feature to test for intersection
     * @return distance between ViewPoint and feature
     */
    private double distance( Feature feat ) {
        PickShape pick = geom.getPickshape();

        double[] dist = new double[1];
        Geometry[] geoms = feat.getGeometryPropertyValues();
        for ( Geometry geom : geoms ) {
            float[] coords = toCoords( geom ); // interior rings???
            if ( coords != null ) {
                GeometryInfo geomInfo = new GeometryInfo( GeometryInfo.POLYGON_ARRAY );
                geomInfo.setCoordinates( coords );
                geomInfo.setStripCounts( new int[] { coords.length / 3 } );
                Shape3D shape = new Shape3D( geomInfo.getGeometryArray() );
                Locale l = new Locale( new VirtualUniverse() );
                SceneGraphPath sgp = new SceneGraphPath( l, shape );
                boolean erg = shape.intersect( sgp, pick, dist );
                if ( !erg ) {
                    dist[0] = Double.NaN;
                }
            }
        }
        return dist[0];
    }

    /**
     * Converts the coordinates of a geometry to an array of floats.
     * 
     * @param geom
     *            the Geometry to convert
     * @return the coords
     */
    private float[] toCoords( Geometry geom ) {
        float[] coords = null;
        if ( geom instanceof Surface ) {
            Surface p = (Surface) geom;
            Position[] positions = p.getSurfaceBoundary().getExteriorRing().getPositions();
            coords = new float[3 * ( positions.length - 1 )];
            for ( int i = 0; i < positions.length - 1; i++ ) {
                int ix = 3 * i;
                coords[ix] = (float) positions[i].getX();
                coords[ix + 1] = (float) positions[i].getY();
                coords[ix + 2] = (float) positions[i].getZ();
            }
        }
        return coords;
    }

    /**
     * Handles the WFS GetFeature-Request for all datasets. Collects the Responses in the FeatureCollection of all
     * valide Results.
     * 
     * @param datasets
     *            list of all valide datasets to query
     * @throws OGCWebServiceException
     */
    private void handleGetFeatureRequest( List<Dataset> datasets )
                            throws OGCWebServiceException {
        Feature tmpFeat = null;
        double tmpDist = 0;
        for ( Dataset dataset : datasets ) {
            AbstractDataSource[] datasource = dataset.getDataSources();
            for ( int i = 0; i < datasource.length; i++ ) {

                GetFeature WFSrequest = null;
                if ( datasource[i].getServiceType() == AbstractDataSource.LOCAL_WFS ) {
                    WFSrequest = createGetFeatureRequest( (LocalWFSDataSource) datasource[i] );
                } else if ( datasource[i].getServiceType() == AbstractDataSource.REMOTE_WFS ) {
                    WFSrequest = createGetFeatureRequest( (RemoteWFSDataSource) datasource[i] );
                } else {
                    throw new OGCWebServiceException( Messages.getMessage( "WPVS_INVALID_DATASOURCE",
                                                                           datasource[i].getName() ) );
                }
                OGCWebService webservice = datasource[i].getOGCWebService();
                FeatureResult fr = (FeatureResult) webservice.doService( WFSrequest );
                Object testFC = fr.getResponse();
                if ( testFC != null && testFC instanceof FeatureCollection ) {
                    FeatureCollection fc = (FeatureCollection) testFC;
                    Feature[] features = fc.toArray();
                    // all Features must be checked for intersection with the geometry of the
                    // request
                    for ( Feature feat : features ) {
                        double dist = distance( feat );
                        if ( !Double.isNaN( dist ) ) {
                            // filter the nearest object, if wanted (FEATURE_COUNT=1)
                            if ( featureCount != 1 && featCol.size() < featureCount ) {
                                featCol.add( feat );
                            } else if ( featureCount == 1 ) {
                                if ( tmpFeat == null ) {
                                    tmpFeat = feat;
                                    tmpDist = dist;
                                } else {
                                    if ( tmpDist > dist ) {
                                        tmpFeat = feat;
                                        tmpDist = dist;
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
        // adds the nearest Object as result to the FeatureCollection
        if ( tmpFeat != null ) {
            featCol.add( tmpFeat );
        }
    }

    /**
     * Creates the WFS GetFeature-Request for a WFS-DataSource.
     * 
     * @param ds
     *            the LocalWFSDataSource to request
     * @return the GetFeature request bean.
     */
    private GetFeature createGetFeatureRequest( LocalWFSDataSource ds ) {
        Geometry queryGeom = geom.getWfsReqGeom();
        SpatialOperation spatialOp = new SpatialOperation( OperationDefines.INTERSECTS,
                                                           new PropertyName( ds.getGeometryProperty() ), queryGeom );
        ComplexFilter comp = new ComplexFilter( spatialOp );
        Query query = Query.create( ds.getName(), comp );
        IDGenerator idg = IDGenerator.getInstance();
        GetFeature gf = GetFeature.create( "1.1.0", String.valueOf( idg.generateUniqueID() ), RESULT_TYPE.RESULTS,
                                           null, null, 9999999, 0, -1, -1, new Query[] { query } );

        return gf;
    }

    /**
     * Creates the Response of a WPVS Get3DFeatureInfo-Request.
     * 
     * @return the Response
     */
    private Get3DFeatureInfoResponse createGet3DFeatureInfoResponse() {
        StringBuffer sb = new StringBuffer( 2000 );
        sb.append( "<ll:FeatureCollection numberOfFeatures='" ).append( featCol.size() ).append(
                                                                                                 "' xmlns:gml='http://www.opengis.net/gml'" ).append(
                                                                                                                                                      " xmlns:ll='http://www.lat-lon.de'>" );
        for ( Feature feat : featCol ) {
            FeatureType ft = feat.getFeatureType();
            PropertyType[] pt = ft.getProperties();
            sb.append( "<gml:featureMember>" ).append( "<ll:" + ft.getName().getLocalName() ).append( " fid='" ).append(
                                                                                                                         feat.getId() ).append(
                                                                                                                                                "'>" );
            for ( int i = 0; i < pt.length; i++ ) {
                if ( pt[i].getType() != Types.GEOMETRY && pt[i].getType() != Types.POINT
                     && pt[i].getType() != Types.CURVE && pt[i].getType() != Types.SURFACE
                     && pt[i].getType() != Types.MULTIPOINT && pt[i].getType() != Types.MULTICURVE
                     && pt[i].getType() != Types.MULTISURFACE ) {

                    sb.append( "<ll:" ).append( pt[i].getName().getLocalName() ).append( ">" ).append(
                                                                                                       feat.getDefaultProperty(
                                                                                                                                pt[i].getName() ).getValue() ).append(
                                                                                                                                                                       "</ll:"
                                                                                                                                                                                               + pt[i].getName().getLocalName()
                                                                                                                                                                                               + ">" );
                }
            }
            sb.append( "</ll:" ).append( ft.getName().getLocalName() ).append( ">" ).append( "</gml:featureMember>" );

        }
        sb.append( "</ll:FeatureCollection>" );

        Get3DFeatureInfoResponse response = new Get3DFeatureInfoResponse( sb.toString() );

        return response;
    }

}
