//$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.tools.crs.georeferencing.application;

import static java.lang.Double.isNaN;
import static java.util.Collections.singletonList;
import static org.deegree.protocol.wfs.transaction.action.IDGenMode.GENERATE_NEW;
import static org.deegree.services.wms.MapService.fillInheritedInformation;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.utils.Triple;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.Feature;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.NewFeatureStoreProvider;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.IdFilter;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsNull;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.logical.Not;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.deegree.services.jaxb.wms.RequestableLayer;
import org.deegree.services.jaxb.wms.ScaleDenominatorsType;
import org.deegree.services.wms.model.layers.FeatureLayer;
import org.deegree.services.wms.model.layers.Layer;
import org.deegree.tools.crs.georeferencing.application.handler.FileInputHandler;
import org.deegree.tools.crs.georeferencing.application.transformation.AbstractTransformation;
import org.deegree.tools.crs.georeferencing.model.RowColumn;
import org.deegree.tools.crs.georeferencing.model.datatransformer.VectorTransformer;
import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint;
import org.deegree.tools.crs.georeferencing.model.points.FootprintPoint;
import org.deegree.tools.crs.georeferencing.model.points.GeoReferencedPoint;
import org.deegree.tools.crs.georeferencing.model.points.Point4Values;
import org.deegree.tools.crs.georeferencing.model.points.PointResidual;
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.standard.DefaultResourceIdentifier;
import org.deegree.workspace.standard.IncorporealResourceLocation;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class TransformationPoints {

    private ArrayList<Triple<Point4Values, Point4Values, PointResidual>> mappedPoints = new ArrayList<Triple<Point4Values, Point4Values, PointResidual>>();

    private HashMap<Triple<Point4Values, Point4Values, PointResidual>, String> featureIds = new HashMap<Triple<Point4Values, Point4Values, PointResidual>, String>();

    private FeatureStore featureStore;

    private FeatureType featureType;

    private PropertyType pointGeometryType, buildingGeometryType;

    private ApplicationState state;

    public TransformationPoints( ApplicationState state ) {
        this.state = state;
        setupFeatureStore();
    }

    private void setupFeatureStore() {
        try {
            URL schemaUrl = ApplicationState.class.getResource( "/org/deegree/tools/crs/georeferencing/memoryschema.xsd" );
            String cfg = "<MemoryFeatureStore configVersion=\"3.0.0\" xmlns=\"http://www.deegree.org/datasource/feature/memory\">"
                         + "  <StorageCRS>"
                         + state.targetCRS.getAlias()
                         + "</StorageCRS>"
                         + "  <NamespaceHint namespaceURI=\"app\" prefix=\"http://www.deegree.org/georef\" />"
                         + "  <GMLSchema version=\"GML_31\">"
                         + schemaUrl.toExternalForm()
                         + "</GMLSchema></MemoryFeatureStore>";
            IncorporealResourceLocation<FeatureStore> loc;
            ResourceIdentifier<FeatureStore> id = new DefaultResourceIdentifier<FeatureStore>(
                                                                                               NewFeatureStoreProvider.class,
                                                                                               "pointsstore" );
            loc = new IncorporealResourceLocation<FeatureStore>( cfg.getBytes( "UTF-8" ), id );
            state.workspace.getNewWorkspace().add( loc );
            state.workspace.getNewWorkspace().prepare( id );
            featureStore = state.workspace.getNewWorkspace().init( id, null );

            featureType = featureStore.getSchema().getFeatureTypes()[0];
            pointGeometryType = featureType.getPropertyDeclarations().get( 0 );
            buildingGeometryType = featureType.getPropertyDeclarations().get( 1 );
            if ( state.service != null ) {
                RequestableLayer lcfg = new RequestableLayer();
                lcfg.setName( "points" );
                lcfg.setTitle( "Points Layer" );
                lcfg.setFeatureStoreId( "pointsstore" );
                lcfg.setScaleDenominators( new ScaleDenominatorsType() );
                try {
                    FeatureLayer lay = new FeatureLayer( state.service, lcfg, state.service.getRootLayer(),
                                                         state.workspace );
                    Layer root = state.service.getRootLayer();
                    root.addOrReplace( lay );
                    state.service.layers.put( "points", lay );
                    fillInheritedInformation( root, new LinkedList<ICRS>( root.getSrs() ) );
                    state.mapController.setLayers( new LinkedList<Layer>( state.service.getRootLayer().getChildren() ) );
                } catch ( Throwable e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch ( Throwable e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void removeAll() {
        try {
            FeatureStoreTransaction ta = featureStore.acquireTransaction();
            ta.performDelete( new IdFilter( featureIds.values() ), null );
            ta.commit();
            mappedPoints.clear();
            featureIds.clear();
            updateTransformation();
            state.conModel.getFootPanel().setLastAbstractPoint( null, null, null );
            state.updateDrawingPanels();
            state.mapController.forceRepaint();
            this.state.conModel.getPanel().repaint();
            this.state.conModel.getFootPanel().repaint();
        } catch ( Throwable e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void load() {
        FileInputHandler in = new FileInputHandler( this.state.tablePanel );
        if ( in.getData() != null ) {
            removeAll();

            this.state.sceneValues.setEnvelopeGeoref( this.state.mapController.getCurrentEnvelope() );
            state.init();
            VectorTransformer vt = new VectorTransformer( in.getData(), this.state.sceneValues );

            for ( Triple<Point4Values, Point4Values, PointResidual> t : vt.getMappedPoints() ) {
                add( null, t.first.getInitialValue() );
                updateTransformation();
                add( t.second.getInitialValue(), null );
                updateTransformation();
            }

            updateTransformation();
            this.state.updateDrawingPanels();
            state.mapController.forceRepaint();
            this.state.conModel.getPanel().repaint();
            this.state.conModel.getFootPanel().repaint();
        }
    }

    public void delete( int[] indexes ) {
        state.tablePanel.removeRow( indexes );
        HashSet<String> ids = new HashSet<String>();
        HashSet<Triple<Point4Values, Point4Values, PointResidual>> pts = new HashSet<Triple<Point4Values, Point4Values, PointResidual>>();
        for ( int i : indexes ) {
            Triple<Point4Values, Point4Values, PointResidual> t = mappedPoints.get( i );
            pts.add( t );
            ids.add( featureIds.get( t ) );
            featureIds.remove( t );
        }
        mappedPoints.removeAll( pts );

        try {
            FeatureStoreTransaction ta = featureStore.acquireTransaction();
            ta.performDelete( new IdFilter( ids ), null );
            ta.commit();

            this.state.conModel.getFootPanel().setLastAbstractPoint( null, null, null );
            this.state.conModel.getPanel().setLastAbstractPoint( null, null, null );
            updateTransformation();
            this.state.updateDrawingPanels();
            state.mapController.forceRepaint();
            this.state.conModel.getPanel().repaint();
            this.state.conModel.getFootPanel().repaint();
        } catch ( Throwable t ) {
            t.printStackTrace();
        }
    }

    public void add( AbstractGRPoint left, AbstractGRPoint right ) {
        this.state.sceneValues.setEnvelopeGeoref( this.state.mapController.getCurrentEnvelope() );
        Point4Values leftp4 = null, rightp4 = null;
        String fid = null;
        if ( left != null ) {
            GeoReferencedPoint g = (GeoReferencedPoint) this.state.sceneValues.getWorldPoint( left );
            leftp4 = new Point4Values( left, g, null );
            Property p = new GenericProperty( pointGeometryType, new DefaultPoint( null, state.targetCRS, null,
                                                                                   new double[] { g.getX(), g.getY() } ) );
            Feature f = featureType.newFeature( "test", Collections.singletonList( p ), null );
            try {
                FeatureStoreTransaction ta = featureStore.acquireTransaction();
                GenericFeatureCollection col = new GenericFeatureCollection();
                col.add( f );
                fid = ta.performInsert( col, GENERATE_NEW ).get( 0 );
                ta.commit();
                state.mapController.forceRepaint();
                this.state.conModel.getPanel().repaint();
                this.state.conModel.getFootPanel().repaint();
            } catch ( Throwable e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            this.state.rc = this.state.tablePanel.setCoords( g );
            this.state.conModel.getPanel().setLastAbstractPoint( left, g, this.state.rc );
        }
        if ( right != null ) {
            rightp4 = new Point4Values( right, state.sceneValues.getWorldPoint( right ), null );
        }
        this.state.referencingLeft = false;

        Triple<Point4Values, Point4Values, PointResidual> last = null;
        if ( !mappedPoints.isEmpty() ) {
            last = mappedPoints.get( mappedPoints.size() - 1 );
        }

        if ( last != null && last.second == null && leftp4 != null ) {
            last.second = leftp4;
            if ( fid != null ) {
                featureIds.put( last, fid );
            }
        } else if ( last != null && last.first == null && rightp4 != null ) {
            last.first = rightp4;
            if ( fid != null ) {
                featureIds.put( last, fid );
            }
        } else {
            Triple<Point4Values, Point4Values, PointResidual> t;
            t = new Triple<Point4Values, Point4Values, PointResidual>( rightp4, leftp4, null );
            mappedPoints.add( t );
            featureIds.put( t, fid );
        }

        updateTransformation();
    }

    public void updateTransformation() {
        // if ( state.conModel.getFootPanel().getLastAbstractPoint() == null
        // || state.conModel.getPanel().getLastAbstractPoint() == null ) {
        // return;
        // }

        state.conModel.setTransform( state.determineTransformationType( state.conModel.getTransformationType() ) );
        if ( state.conModel.getTransform() == null ) {
            return;
        }
        List<Ring> polygonRing = state.conModel.getTransform().computeRingList();

        GeometryFactory fac = new GeometryFactory();

        PropertyIsNull op = new PropertyIsNull( new ValueReference( buildingGeometryType.getName() ), null );
        OperatorFilter f = new OperatorFilter( new Not( op ) );
        try {
            FeatureStoreTransaction ta = featureStore.acquireTransaction();
            ta.performDelete( featureType.getName(), f, null );
            ta.commit();

            GenericFeatureCollection col = new GenericFeatureCollection();
            int i = 0;
            for ( Ring r : polygonRing ) {
                if ( r.getControlPoints().size() < 4 ) {
                    continue;
                }
                if ( isNaN( r.getControlPoints().get( 0 ).get0() ) ) {
                    continue;
                }
                Polygon p = fac.createPolygon( null, state.targetCRS, r, null );
                Property prop = new GenericProperty( buildingGeometryType, p );
                Feature feat = featureType.newFeature( "test" + i++, singletonList( prop ), null );
                col.add( feat );
            }
            ta = featureStore.acquireTransaction();
            ta.performInsert( col, GENERATE_NEW );
            ta.commit();
        } catch ( Throwable e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        updateResiduals( state.conModel.getTransformationType() );

        state.conModel.getPanel().repaint();

        state.reset();

        if ( state.transformationListener != null )
            state.transformationListener.actionPerformed( new ActionEvent( state, 0, "transformationupdated" ) );
    }

    /**
     * Updates the model of the table to show the residuals of the already stored mappedPoints. It is based on the
     * Helmert transformation.
     * 
     * @param type
     * 
     */
    public void updateResiduals( AbstractTransformation.TransformationType type ) {
        AbstractTransformation t = state.determineTransformationType( type );
        PointResidual[] r = t.calculateResiduals();
        if ( r != null ) {
            Vector<Vector<? extends Double>> data = new Vector<Vector<? extends Double>>();
            int counter = 0;
            for ( Triple<Point4Values, Point4Values, PointResidual> point : this.mappedPoints ) {
                // hashcode of triple will change if values are changed
                String id = featureIds.remove( point );
                if ( point.second == null ) {
                    continue;
                }
                Vector<Double> element = new Vector<Double>( 6 );
                element.add( point.second.getWorldCoords().x );
                element.add( point.second.getWorldCoords().y );
                element.add( point.first.getWorldCoords().x );
                element.add( point.first.getWorldCoords().y );
                element.add( r[counter].x );
                element.add( r[counter].y );
                data.add( element );
                point.third = r[counter++];
                featureIds.put( point, id );
            }
            state.tablePanel.getModel().setDataVector( data, state.tablePanel.getColumnNamesAsVector() );
            state.tablePanel.getModel().fireTableDataChanged();
        }
    }

    public int getNumPoints() {
        return mappedPoints.size();
    }

    public boolean isEmpty() {
        return mappedPoints.isEmpty();
    }

    public List<Triple<Point4Values, Point4Values, PointResidual>> getMappedPoints() {
        return new ArrayList<Triple<Point4Values, Point4Values, PointResidual>>( mappedPoints );
    }

    public void updateFootprintPoints( Scene2DValues sceneValues ) {

        for ( Triple<Point4Values, Point4Values, PointResidual> t : mappedPoints ) {
            String id = featureIds.remove( t );
            Point4Values p = t.first;
            int[] pValues = sceneValues.getPixelCoord( p.getWorldCoords() );
            double x = pValues[0];
            double y = pValues[1];
            FootprintPoint pi = new FootprintPoint( x, y );
            t.first = new Point4Values( p.getNewValue(), p.getInitialValue(), pi, p.getWorldCoords(), p.getRc() );
            featureIds.put( t, id );
        }

        Point4Values lastAbstractPoint = state.conModel.getFootPanel().getLastAbstractPoint();
        if ( lastAbstractPoint != null ) {
            AbstractGRPoint worldCoords = lastAbstractPoint.getWorldCoords();
            RowColumn rc = lastAbstractPoint.getRc();
            int[] pValues = sceneValues.getPixelCoord( worldCoords );
            double x = pValues[0];
            double y = pValues[1];

            FootprintPoint pi = new FootprintPoint( x, y );
            state.conModel.getFootPanel().setLastAbstractPoint( pi, worldCoords, rc );
        }
    }

}
