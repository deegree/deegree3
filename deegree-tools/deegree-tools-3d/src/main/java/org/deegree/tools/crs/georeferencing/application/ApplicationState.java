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
package org.deegree.tools.crs.georeferencing.application;

import static java.awt.Cursor.getPredefinedCursor;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.vecmath.Point2d;

import org.deegree.commons.utils.Triple;
import org.deegree.cs.CRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.GeometryFactory;
import org.deegree.rendering.r3d.model.geometry.GeometryQualityModel;
import org.deegree.rendering.r3d.model.geometry.SimpleAccessGeometry;
import org.deegree.rendering.r3d.opengl.display.OpenGLEventHandler;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.WorldRenderableObject;
import org.deegree.services.wms.MapService;
import org.deegree.services.wms.utils.MapController;
import org.deegree.tools.crs.georeferencing.application.listeners.FootprintMouseListener;
import org.deegree.tools.crs.georeferencing.application.listeners.Scene2DMouseListener;
import org.deegree.tools.crs.georeferencing.application.listeners.Scene2DMouseMotionListener;
import org.deegree.tools.crs.georeferencing.application.listeners.Scene2DMouseWheelListener;
import org.deegree.tools.crs.georeferencing.application.transformation.AbstractTransformation;
import org.deegree.tools.crs.georeferencing.application.transformation.AffineTransformation;
import org.deegree.tools.crs.georeferencing.application.transformation.Helmert4Transform;
import org.deegree.tools.crs.georeferencing.application.transformation.Polynomial;
import org.deegree.tools.crs.georeferencing.communication.PointTableFrame;
import org.deegree.tools.crs.georeferencing.communication.checkboxlist.CheckboxListTransformation;
import org.deegree.tools.crs.georeferencing.communication.dialog.menuitem.OpenWMS;
import org.deegree.tools.crs.georeferencing.communication.dialog.menuitem.WMSParameterChooser;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.GenericSettingsPanel;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.NavigationPanel;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.OptionDialog;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.SettingsPanel;
import org.deegree.tools.crs.georeferencing.model.CheckBoxListModel;
import org.deegree.tools.crs.georeferencing.model.ControllerModel;
import org.deegree.tools.crs.georeferencing.model.Footprint;
import org.deegree.tools.crs.georeferencing.model.RowColumn;
import org.deegree.tools.crs.georeferencing.model.mouse.FootprintMouseModel;
import org.deegree.tools.crs.georeferencing.model.mouse.GeoReferencedMouseModel;
import org.deegree.tools.crs.georeferencing.model.points.Point4Values;
import org.deegree.tools.crs.georeferencing.model.points.PointResidual;
import org.deegree.tools.rendering.viewer.File3dImporter;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ApplicationState {

    public boolean start, isControlDown, isInitGeoref, isInitFoot, referencing, previewing, referencingLeft;

    public boolean zoomIn, zoomOut, pan;

    public Scene2DValues sceneValues;

    public PointTableFrame tablePanel;

    public ParameterStore store;

    public GeoReferencedMouseModel mouseGeoRef;

    public FootprintMouseModel mouseFootprint;

    public Point2d changePoint;

    public List<Triple<Point4Values, Point4Values, PointResidual>> mappedPoints;

    public ControllerModel conModel;

    public NavigationPanel optionNavPanel;

    public SettingsPanel optionSettPanel;

    public OptionDialog optionDialog;

    public OpenWMS wmsStartDialog;

    public WMSParameterChooser wmsParameter;

    public GenericSettingsPanel optionSettingPanel;

    public CheckboxListTransformation checkBoxListTransform;

    public CheckBoxListModel modelTransformation;

    public RowColumn rc;

    public String chosenFile;

    public CRS sourceCRS, targetCRS;

    public Footprint footPrint;

    public OpenGLEventHandler glHandler;

    private GeometryFactory geom = new GeometryFactory();

    public MapController mapController;

    public MapService service;

    /**
     * Removes sample points in panels and the table.
     * 
     * @param tableRows
     *            that should be removed, could be <Code>null</Code>
     */
    public void removeFromMappedPoints( int[] tableRows ) {
        for ( int i = tableRows.length - 1; i >= 0; i-- ) {
            mappedPoints.remove( tableRows[i] );
        }

    }

    /**
     * Initializes the georeferenced scene.
     */
    public void initGeoReferencingScene() {
        isInitGeoref = true;
        if ( isInitFoot ) {
            tablePanel.getSaveButton().setEnabled( true );
            tablePanel.getLoadButton().setEnabled( true );
        }

        mouseGeoRef = new GeoReferencedMouseModel();
        init();
        Controller.removeListeners( conModel.getPanel() );
        conModel.getPanel().addScene2DMouseListener( new Scene2DMouseListener( this ) );
        conModel.getPanel().addScene2DMouseMotionListener( new Scene2DMouseMotionListener( this ) );
        conModel.getPanel().addScene2DMouseWheelListener( new Scene2DMouseWheelListener( this ) );
    }

    /**
     * Initializes the computing and the painting of the maps.
     */
    void init() {
        if ( mapController != null ) {
            sceneValues.setGeorefDimension( new Rectangle( conModel.getPanel().getWidth(),
                                                           conModel.getPanel().getHeight() ) );
            conModel.getPanel().setImageDimension( sceneValues.getGeorefDimension() );
            conModel.getPanel().updatePoints( sceneValues );
            Component glassPane = ( (JFrame) conModel.getPanel().getTopLevelAncestor() ).getGlassPane();
            MouseAdapter mouseAdapter = new MouseAdapter() {
                // else the wait cursor will not appear
            };
            glassPane.addMouseListener( mouseAdapter );
            glassPane.setCursor( getPredefinedCursor( Cursor.WAIT_CURSOR ) );
            glassPane.setVisible( true );
            conModel.getPanel().repaint();
            glassPane.removeMouseListener( mouseAdapter );
            glassPane.setCursor( getPredefinedCursor( Cursor.DEFAULT_CURSOR ) );
            glassPane.setVisible( false );
        }
    }

    /**
     * Initializes the footprint scene.
     */
    public void initFootprintScene( String filePath ) {
        isInitFoot = true;
        if ( isInitGeoref ) {
            tablePanel.getSaveButton().setEnabled( true );
            tablePanel.getLoadButton().setEnabled( true );
        }

        this.footPrint = new Footprint( sceneValues, geom );
        Controller.removeListeners( conModel.getFootPanel() );
        conModel.getFootPanel().addScene2DMouseListener( new FootprintMouseListener( this ) );
        conModel.getFootPanel().addScene2DMouseMotionListener( new Scene2DMouseMotionListener( this ) );
        conModel.getFootPanel().addScene2DMouseWheelListener( new Scene2DMouseWheelListener( this ) );

        mouseFootprint = new FootprintMouseModel();
        List<WorldRenderableObject> rese = File3dImporter.open( conModel.getView(), filePath );
        sourceCRS = null;
        for ( WorldRenderableObject res : rese ) {
            sourceCRS = res.getBbox().getCoordinateSystem();
            glHandler.addDataObjectToScene( res );
        }
        List<float[]> geometryThatIsTaken = new ArrayList<float[]>();
        for ( GeometryQualityModel g : File3dImporter.gm ) {

            ArrayList<SimpleAccessGeometry> h = g.getQualityModelParts();
            boolean isfirstOccurrence = false;
            float minimalZ = 0;

            for ( SimpleAccessGeometry b : h ) {
                float[] a = b.getHorizontalGeometries( b.getGeometry() );
                if ( a != null ) {
                    if ( isfirstOccurrence == false ) {
                        minimalZ = a[2];
                        geometryThatIsTaken.add( a );
                        isfirstOccurrence = true;
                    } else {
                        if ( minimalZ < a[2] ) {
                            // nix
                        } else {
                            geometryThatIsTaken.remove( geometryThatIsTaken.size() - 1 );
                            minimalZ = a[2];
                            geometryThatIsTaken.add( a );
                        }
                    }

                }
            }

        }

        footPrint.generateFootprints( geometryThatIsTaken );

        sceneValues.setDimensionFootpanel( new Rectangle( conModel.getFootPanel().getBounds().width,
                                                          conModel.getFootPanel().getBounds().height ) );
        conModel.getFootPanel().updatePoints( sceneValues );

        conModel.getFootPanel().setPolygonList( footPrint.getWorldCoordinateRingList(), sceneValues );

        conModel.getFootPanel().repaint();

    }

    public void updateResidualsWithLastAbstractPoint() {
        if ( conModel.getFootPanel().getLastAbstractPoint() != null
             && conModel.getPanel().getLastAbstractPoint() != null ) {
            mappedPoints.add( new Triple<Point4Values, Point4Values, PointResidual>(
                                                                                     conModel.getFootPanel().getLastAbstractPoint(),
                                                                                     conModel.getPanel().getLastAbstractPoint(),
                                                                                     null ) );
            updateMappedPoints();
            updateResiduals( conModel.getTransformationType() );

            // remove the last element...should be the before inserted value
            mappedPoints.remove( mappedPoints.size() - 1 );
        } else {
            updateMappedPoints();
            updateResiduals( conModel.getTransformationType() );
        }
    }

    /**
     * Adds the <Code>AbstractPoint</Code>s to a map, if specified.
     * 
     * @param mappedPointKey
     * @param mappedPointValue
     */
    void addToMappedPoints( Point4Values mappedPointKey, Point4Values mappedPointValue, PointResidual residual ) {
        if ( mappedPointKey != null && mappedPointValue != null ) {
            this.mappedPoints.add( new Triple<Point4Values, Point4Values, PointResidual>( mappedPointKey,
                                                                                          mappedPointValue, residual ) );
        }

    }

    /**
     * Updates the rowNumber of the remained mappedPoints
     */
    private void updateMappedPoints() {

        List<Triple<Point4Values, Point4Values, PointResidual>> temp = new ArrayList<Triple<Point4Values, Point4Values, PointResidual>>();

        int counter = 0;
        for ( Triple<Point4Values, Point4Values, PointResidual> p : mappedPoints ) {
            System.out.println( "[Controller] before: " + p );
            Point4Values f = new Point4Values( p.first.getOldValue(), p.first.getInitialValue(), p.first.getNewValue(),
                                               p.first.getWorldCoords(), new RowColumn( counter,
                                                                                        p.first.getRc().getColumnX(),
                                                                                        p.first.getRc().getColumnY() ) );
            Point4Values s = new Point4Values( p.second.getOldValue(), p.second.getInitialValue(),
                                               p.second.getNewValue(), p.second.getWorldCoords(),
                                               new RowColumn( counter++, p.second.getRc().getColumnX(),
                                                              p.second.getRc().getColumnY() ) );
            if ( p.third != null ) {

                PointResidual r = new PointResidual( p.third.x, p.third.y );
                System.out.println( "\n[Controller] after: " + s );
                temp.add( new Triple<Point4Values, Point4Values, PointResidual>( f, s, r ) );
            } else {
                temp.add( new Triple<Point4Values, Point4Values, PointResidual>( f, s, null ) );
            }
        }
        mappedPoints.clear();
        mappedPoints.addAll( temp );
    }

    /**
     * Updates the model of the table to show the residuals of the already stored mappedPoints. It is based on the
     * Helmert transformation.
     * 
     * @param type
     * 
     */
    public void updateResiduals( AbstractTransformation.TransformationType type ) {

        try {
            AbstractTransformation t = determineTransformationType( type );
            PointResidual[] r = t.calculateResiduals();
            if ( r != null ) {
                Vector<Vector<? extends Double>> data = new Vector<Vector<? extends Double>>();
                int counter = 0;
                for ( Triple<Point4Values, Point4Values, PointResidual> point : mappedPoints ) {
                    Vector<Double> element = new Vector<Double>( 6 );
                    element.add( point.second.getWorldCoords().x );
                    element.add( point.second.getWorldCoords().y );
                    element.add( point.first.getWorldCoords().x );
                    element.add( point.first.getWorldCoords().y );
                    element.add( r[counter].x );
                    element.add( r[counter].y );
                    data.add( element );

                    point.third = r[counter++];

                }
                tablePanel.getModel().setDataVector( data, tablePanel.getColumnNamesAsVector() );
                tablePanel.getModel().fireTableDataChanged();
            }
        } catch ( UnknownCRSException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Removes everything after a complete deletion of the points.
     */
    public void removeAllFromMappedPoints() {
        mappedPoints = new ArrayList<Triple<Point4Values, Point4Values, PointResidual>>();
        tablePanel.removeAllRows();
        conModel.getPanel().removeAllFromSelectedPoints();
        conModel.getFootPanel().removeAllFromSelectedPoints();
        conModel.getFootPanel().setLastAbstractPoint( null, null, null );
        conModel.getPanel().setPolygonList( null, null );
        conModel.getPanel().setLastAbstractPoint( null, null, null );
        conModel.getPanel().repaint();
        conModel.getFootPanel().repaint();
        reset();

    }

    /**
     * Resets the focus of the panels and the startPanel.
     */
    public void reset() {
        conModel.getPanel().setFocus( false );
        conModel.getFootPanel().setFocus( false );
        start = false;

    }

    /**
     * Determines the transformationMethod by means of the type.
     * 
     * @param type
     *            of the transformationMethod, not <Code>null</Code>.
     * @return the transformationMethod to be used.
     * @throws UnknownCRSException
     */
    public AbstractTransformation determineTransformationType( AbstractTransformation.TransformationType type )
                            throws UnknownCRSException {
        AbstractTransformation t = null;
        if ( targetCRS == null ) {
            return null;
        }
        switch ( type ) {
        case Polynomial:
            t = new Polynomial( mappedPoints, footPrint, sceneValues, targetCRS, targetCRS, conModel.getOrder() );
            break;
        case Helmert_4:
            t = new Helmert4Transform( mappedPoints, footPrint, sceneValues, targetCRS, conModel.getOrder() );
            break;
        case Affine:
            t = new AffineTransformation( mappedPoints, footPrint, sceneValues, targetCRS, targetCRS,
                                          conModel.getOrder() );
            break;
        }

        return t;
    }

    /**
     * Updates the panels that are responsible for drawing the georeferenced points so that the once clicked points are
     * drawn into the right position.
     */
    public void updateDrawingPanels() {
        List<Point4Values> panelList = new ArrayList<Point4Values>();
        List<Point4Values> footPanelList = new ArrayList<Point4Values>();
        for ( Triple<Point4Values, Point4Values, PointResidual> p : mappedPoints ) {
            panelList.add( p.second );
            footPanelList.add( p.first );
        }

        conModel.getPanel().setSelectedPoints( panelList, sceneValues );
        conModel.getFootPanel().setSelectedPoints( footPanelList, sceneValues );

        conModel.getPanel().repaint();
        conModel.getFootPanel().repaint();

    }

    /**
     * Sets values to the JTableModel.
     */
    public void setValues() {
        conModel.getFootPanel().addToSelectedPoints( conModel.getFootPanel().getLastAbstractPoint() );
        conModel.getPanel().addToSelectedPoints( conModel.getPanel().getLastAbstractPoint() );
        if ( mappedPoints != null && mappedPoints.size() >= 1 ) {
            addToMappedPoints( conModel.getFootPanel().getLastAbstractPoint(),
                               conModel.getPanel().getLastAbstractPoint(), null );
            updateResiduals( conModel.getTransformationType() );
        } else {
            addToMappedPoints( conModel.getFootPanel().getLastAbstractPoint(),
                               conModel.getPanel().getLastAbstractPoint(), null );
        }
        conModel.getFootPanel().setLastAbstractPoint( null, null, null );
        conModel.getPanel().setLastAbstractPoint( null, null, null );

    }
}
