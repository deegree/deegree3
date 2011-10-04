//$HeadURL$
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
package org.deegree.tools.crs.georeferencing.application.listeners;

import static java.lang.Double.isNaN;
import static java.util.Collections.singletonList;
import static org.deegree.gml.GMLVersion.GML_31;
import static org.deegree.protocol.wfs.transaction.IDGenMode.GENERATE_NEW;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;

import javax.vecmath.Point2d;

import org.deegree.feature.Feature;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.property.Property;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsNull;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.logical.Not;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.deegree.tools.crs.georeferencing.application.ApplicationState;
import org.deegree.tools.crs.georeferencing.model.points.GeoReferencedPoint;

/**
 * 
 * Controls the MouseListener
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Scene2DMouseListener extends MouseAdapter {

    private ApplicationState state;

    public Scene2DMouseListener( ApplicationState state ) {
        this.state = state;
    }

    @Override
    public void mouseEntered( MouseEvent m ) {
        this.state.mouseGeoRef.setMouseInside( true );
    }

    @Override
    public void mouseExited( MouseEvent m ) {
        this.state.mouseGeoRef.setMouseInside( false );
    }

    @Override
    public void mousePressed( MouseEvent m ) {
        if ( this.state.mapController != null ) {
            if ( this.state.zoomIn ) {
                this.state.mapController.setZoomRectStart( m.getX(), m.getY() );
            }
            if ( this.state.pan ) {
                this.state.mapController.startPanning( m.getX(), m.getY() );
                this.state.previewing = true;
            }
        }
        this.state.mouseGeoRef.setPointMousePressed( new Point2d( m.getX(), m.getY() ) );
        this.state.isControlDown = m.isControlDown();
    }

    @Override
    public void mouseReleased( MouseEvent m ) {
        boolean isFirstNumber = false;
        if ( this.state.mapController != null ) {
            if ( this.state.isControlDown || this.state.zoomIn || this.state.zoomOut ) {
                if ( this.state.zoomIn ) {
                    if ( !this.state.mapController.finishZoomin( m.getX(), m.getY() ) ) {
                        this.state.mapController.zoom( 1 - this.state.conModel.getDialogModel().getResizeValue().second,
                                                       m.getX(), m.getY() );
                    }
                } else if ( this.state.zoomOut ) {
                    this.state.mapController.zoom( 1 / ( 1 - this.state.conModel.getDialogModel().getResizeValue().second ),
                                                   m.getX(), m.getY() );
                }

                updateTransformation( this.state );
                this.state.conModel.getPanel().updatePoints( this.state.sceneValues );
                this.state.conModel.getPanel().repaint();
            }

            else {
                if ( this.state.referencing && this.state.referencingLeft ) {
                    if ( this.state.start == false ) {
                        this.state.start = true;
                        this.state.conModel.getFootPanel().setFocus( false );
                        this.state.conModel.getPanel().setFocus( true );
                    }
                    if ( this.state.conModel.getFootPanel().getLastAbstractPoint() != null
                         && this.state.conModel.getPanel().getLastAbstractPoint() != null
                         && this.state.conModel.getPanel().getFocus() == true ) {
                        this.state.setValues();
                    }
                    if ( this.state.conModel.getFootPanel().getLastAbstractPoint() == null
                         && this.state.conModel.getPanel().getLastAbstractPoint() == null
                         && this.state.conModel.getPanel().getFocus() == true ) {
                        this.state.tablePanel.addRow();
                        isFirstNumber = true;
                    }

                    double x = m.getX();
                    double y = m.getY();
                    this.state.sceneValues.setEnvelopeGeoref( this.state.mapController.getCurrentEnvelope() );
                    GeoReferencedPoint geoReferencedPoint = new GeoReferencedPoint( x, y );
                    GeoReferencedPoint g = (GeoReferencedPoint) this.state.sceneValues.getWorldPoint( geoReferencedPoint );
                    Property p = new GenericProperty( state.pointGeometryType,
                                                      new DefaultPoint( null, state.targetCRS, null,
                                                                        new double[] { g.getX(), g.getY() } ) );
                    Feature f = state.featureType.newFeature( "test", Collections.singletonList( p ), null, GML_31 );
                    try {
                        FeatureStoreTransaction ta = state.featureStore.acquireTransaction();
                        GenericFeatureCollection col = new GenericFeatureCollection();
                        col.add( f );
                        ta.performInsert( col, GENERATE_NEW );
                        ta.commit();
                        state.mapController.forceRepaint();
                        this.state.conModel.getPanel().repaint();
                        this.state.conModel.getFootPanel().repaint();
                    } catch ( Throwable e ) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    this.state.rc = this.state.tablePanel.setCoords( g );
                    this.state.conModel.getPanel().setLastAbstractPoint( geoReferencedPoint, g, this.state.rc );
                    if ( isFirstNumber == false ) {
                        this.state.updateResidualsWithLastAbstractPoint();
                    }
                    this.state.referencingLeft = false;

                    updateTransformation( this.state );

                } else if ( this.state.pan ) {
                    this.state.previewing = false;
                    this.state.mapController.endPanning();
                    this.state.conModel.getPanel().updatePoints( this.state.sceneValues );
                }

                this.state.conModel.getPanel().repaint();
            }

        }
    }

    public static void updateTransformation( ApplicationState state ) {
        // swap the tempPoints into the map now
        if ( state.conModel.getFootPanel().getLastAbstractPoint() != null
             && state.conModel.getPanel().getLastAbstractPoint() != null ) {
            state.setValues();
        } else {
            return;
        }

        state.conModel.setTransform( state.determineTransformationType( state.conModel.getTransformationType() ) );
        if ( state.conModel.getTransform() == null ) {
            return;
        }
        List<Ring> polygonRing = state.conModel.getTransform().computeRingList();

        GeometryFactory fac = new GeometryFactory();

        PropertyIsNull op = new PropertyIsNull( new ValueReference( state.buildingGeometryType.getName() ), null );
        OperatorFilter f = new OperatorFilter( new Not( op ) );
        try {
            FeatureStoreTransaction ta = state.featureStore.acquireTransaction();
            ta.performDelete( state.featureType.getName(), f, null );
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
                Property prop = new GenericProperty( state.buildingGeometryType, p );
                Feature feat = state.featureType.newFeature( "test" + i++, singletonList( prop ), null, GML_31 );
                col.add( feat );
            }
            ta = state.featureStore.acquireTransaction();
            ta.performInsert( col, GENERATE_NEW );
            ta.commit();
        } catch ( Throwable e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        state.updateResiduals( state.conModel.getTransformationType() );

        state.conModel.getPanel().repaint();

        state.reset();

        if ( state.transformationListener != null )
            state.transformationListener.actionPerformed( new ActionEvent( state, 0, "transformationupdated" ) );
    }

}
