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
package org.deegree.tools.crs.georeferencing.application;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.vecmath.Point2d;

import org.deegree.commons.utils.Pair;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.rendering.r3d.model.geometry.GeometryQualityModel;
import org.deegree.rendering.r3d.model.geometry.SimpleAccessGeometry;
import org.deegree.rendering.r3d.opengl.display.OpenGLEventHandler;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.WorldRenderableObject;
import org.deegree.tools.crs.georeferencing.communication.BuildingFootprintPanel;
import org.deegree.tools.crs.georeferencing.communication.GRViewerGUI;
import org.deegree.tools.crs.georeferencing.communication.NavigationBarPanel;
import org.deegree.tools.crs.georeferencing.communication.PointTablePanel;
import org.deegree.tools.crs.georeferencing.communication.Scene2DPanel;
import org.deegree.tools.crs.georeferencing.model.Footprint;
import org.deegree.tools.crs.georeferencing.model.MouseModel;
import org.deegree.tools.crs.georeferencing.model.Scene2D;
import org.deegree.tools.crs.georeferencing.model.Scene2DValues;
import org.deegree.tools.crs.georeferencing.model.points.FootprintPoint;
import org.deegree.tools.crs.georeferencing.model.points.GeoReferencedPoint;
import org.deegree.tools.crs.georeferencing.model.points.Point3Values;
import org.deegree.tools.rendering.viewer.File3dImporter;

/**
 * The <Code>Controller</Code> is responsible to bind the view with the model.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Controller {

    private GRViewerGUI view;

    private Scene2D model;

    private Scene2DPanel panel;

    private Scene2DValues sceneValues;

    private BuildingFootprintPanel footPanel;

    private NavigationBarPanel navPanel;

    private PointTablePanel tablePanel;

    private RasterIOOptions options;

    private Footprint footPrint;

    private OpenGLEventHandler glHandler;

    private MouseModel mouseGeoRef;

    private MouseModel mouseFootprint;

    private Point2d changePoint;

    private boolean isHorizontalRef, start;

    private GeoReferencedPoint lastGeoReferencedPoint;

    private List<Pair<FootprintPoint, GeoReferencedPoint>> mappedPoints;

    public Controller( GRViewerGUI view, Scene2D model ) {
        this.view = view;
        this.model = model;
        this.panel = view.getScenePanel2D();
        this.footPanel = view.getFootprintPanel();
        this.navPanel = view.getNavigationPanel();
        this.footPrint = new Footprint( 4.0f );
        this.tablePanel = view.getPointTablePanel();
        this.start = false;
        this.glHandler = view.getOpenGLEventListener();

        this.mappedPoints = new ArrayList<Pair<FootprintPoint, GeoReferencedPoint>>();
        this.footPrint.setOffset( 10 );

        options = new RasterOptions( view ).getOptions();
        sceneValues = new Scene2DValues( options );
        model.init( options, sceneValues );
        view.addMenuItemListener( new ButtonListener() );
        view.addHoleWindowListener( new HoleWindowListener() );
        navPanel.addHorizontalRefListener( new ButtonListener() );
        footPanel.addScene2DMouseListener( new Scene2DMouseListener() );
        footPanel.addScene2DMouseMotionListener( new Scene2DMouseMotionListener() );
        footPanel.addScene2DMouseWheelListener( new Scene2DMouseWheelListener() );
        tablePanel.addHorizontalRefListener( new ButtonListener() );
        tablePanel.addTableModelListener( new TableListener() );

        isHorizontalRef = false;

    }

    /**
     * 
     * Controls the ActionListener
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    class ButtonListener implements ActionListener {

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed( ActionEvent e ) {
            Object source = e.getSource();
            if ( source instanceof JCheckBox ) {
                if ( ( (JCheckBox) source ).getText().startsWith( NavigationBarPanel.HORIZONTAL_REFERENCING ) ) {
                    if ( isHorizontalRef == false ) {
                        isHorizontalRef = true;
                    } else {
                        isHorizontalRef = false;
                    }
                }

            }
            if ( source instanceof JButton ) {
                if ( ( (JButton) source ).getText().startsWith( PointTablePanel.BUTTON_DELETE_SELECTED ) ) {
                    System.out.println( "you clicked on delete selected" );
                    int[] tableRows = tablePanel.getTable().getSelectedRows();

                    for ( int tableRow : tableRows ) {
                        FootprintPoint pointFromTable = new FootprintPoint(
                                                                            (Double) tablePanel.getModel().getValueAt(
                                                                                                                       tableRow,
                                                                                                                       2 ),
                                                                            (Double) tablePanel.getModel().getValueAt(
                                                                                                                       tableRow,

                                                                                                                       3 ) );
                        boolean contained = false;
                        for ( Pair<FootprintPoint, GeoReferencedPoint> p : mappedPoints ) {
                            double x = p.first.getX();
                            double y = p.first.getY();
                            if ( pointFromTable.getX() == x && pointFromTable.getY() == y ) {
                                removeFromMappedPoints( p );
                                contained = true;
                                break;
                            }
                        }
                        if ( contained == false ) {
                            footPrint.setLastFootprintPoint( null );
                            lastGeoReferencedPoint = null;
                        }

                        tablePanel.removeRow( tableRow );

                    }

                    panel.addPoint( mappedPoints, lastGeoReferencedPoint );
                    footPanel.addPoint( mappedPoints, footPrint.getLastFootprintPoint().getNewValue() );
                    panel.repaint();
                    footPanel.repaint();
                }
                if ( ( (JButton) source ).getText().startsWith( PointTablePanel.BUTTON_DELETE_ALL ) ) {
                    System.out.println( "you clicked on delete all" );
                    removeAllFromMappedPoints();

                }
                if ( ( (JButton) source ).getText().startsWith( NavigationBarPanel.COMPUTE_BUTTON_NAME ) ) {
                    System.out.println( "you clicked on computation" );

                    // swap the tempPoints into the map now
                    // if ( footPrint.getLastFootprintPoint() != null && lastGeoReferencedPoint != null ) {
                    // mappedPoints.put( footPrint.getLastFootprintPoint(), lastGeoReferencedPoint );
                    // footPrint.setLastFootprintPoint( null );
                    // lastGeoReferencedPoint = null;
                    // }

                    // int arraySize = mappedPoints.size() * 2;
                    // if ( arraySize > 0 ) {
                    //
                    // CRSCodeType[] s = sourceCRS.getCodes();
                    // CRSCodeType[] t = targetCRS.getCodes();
                    // int size = s.length + t.length;
                    // int countT = 0;
                    // CRSCodeType[] codeTypes = new CRSCodeType[size];
                    // for ( int i = 0; i < s.length; i++ ) {
                    // codeTypes[i] = s[i];
                    // }
                    // for ( int i = s.length; i < size; i++ ) {
                    // codeTypes[i] = t[countT];
                    // countT++;
                    // }
                    //
                    // final Helmert wgs_info = new Helmert( sourceCRS, targetCRS, codeTypes );
                    //
                    // double[] ordinatesSrc = new double[arraySize];
                    // double[] ordinatesDst = new double[arraySize];
                    // int counterSrc = 0;
                    // int counterDst = 0;
                    // for ( FootprintPoint p : mappedPoints.keySet() ) {
                    // double x = p.getX();
                    // double y = p.getY();
                    // ordinatesSrc[counterSrc] = x;
                    // ordinatesSrc[++counterSrc] = y;
                    // counterSrc++;
                    // GeoReferencedPoint pValue = mappedPoints.get( p );
                    // x = pValue.getX();
                    // y = pValue.getY();
                    // ordinatesDst[counterDst] = x;
                    // ordinatesDst[++counterDst] = y;
                    // counterDst++;
                    //
                    // }
                    //
                    // try {
                    // wgs_info.doTransform( ordinatesDst, 0, ordinatesSrc, 0, ordinatesSrc.length );
                    // } catch ( TransformationException e1 ) {
                    // e1.printStackTrace();
                    // }
                    // for ( double d : ordinatesDst ) {
                    // System.out.println( "WGS_INFO: " + d );
                    // }
                    //                
                    // } else {
                    // try {
                    // throw new Exception( "You must specify coordinates to transform" );
                    // } catch ( Exception e1 ) {
                    // e1.printStackTrace();
                    // }
                    // }
                }
            }
            if ( source instanceof JMenuItem ) {
                if ( ( (JMenuItem) source ).getText().startsWith( GRViewerGUI.MENUITEM_GETMAP ) ) {
                    mouseGeoRef = new MouseModel();
                    init();
                    // sourceCRS = sceneValues.getCrs();
                    panel.addScene2DMouseListener( new Scene2DMouseListener() );
                    // panel.addScene2DMouseMotionListener( new Scene2DMouseMotionListener() );
                    panel.addScene2DMouseWheelListener( new Scene2DMouseWheelListener() );

                }
                if ( ( (JMenuItem) source ).getText().startsWith( GRViewerGUI.MENUITEM_GET_3DOBJECT ) ) {
                    mouseFootprint = new MouseModel();
                    // TODO at the moment the file which is used is static in the GRViewerGUI!!!
                    List<WorldRenderableObject> rese = File3dImporter.open( view, view.fileName() );
                    // targetCRS = null;
                    for ( WorldRenderableObject res : rese ) {
                        // try {
                        // targetCRS = res.getBbox().getCoordinateSystem().getWrappedCRS();
                        // } catch ( UnknownCRSException e1 ) {
                        // e1.printStackTrace();
                        // }
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

                                    } else {
                                        geometryThatIsTaken.remove( geometryThatIsTaken.size() - 1 );
                                        minimalZ = a[2];
                                        geometryThatIsTaken.add( a );
                                    }
                                }
                                System.out.println( a );
                            }
                        }

                    }

                    footPrint.generateFootprints( geometryThatIsTaken );

                    footPanel.setPolygonList( footPrint.getPixelCoordinatePolygonList() );

                    footPanel.repaint();

                }

            }

        }
    }

    class TableListener implements TableModelListener {

        @Override
        public void tableChanged( TableModelEvent e ) {

        }

    }

    /**
     * 
     * Controls the MouseListener
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    class Scene2DMouseListener implements MouseListener {

        @Override
        public void mouseClicked( MouseEvent arg0 ) {

        }

        @Override
        public void mouseEntered( MouseEvent arg0 ) {

        }

        @Override
        public void mouseExited( MouseEvent arg0 ) {

        }

        @Override
        public void mousePressed( MouseEvent m ) {
            Object source = m.getSource();
            if ( source instanceof JPanel ) {
                // Scene2DPanel
                if ( ( (JPanel) source ).getName().equals( Scene2DPanel.SCENE2D_PANEL_NAME ) ) {
                    mouseGeoRef.setPointMousePressed( new Point2d( m.getX(), m.getY() ) );
                }
                if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {
                    mouseFootprint.setPointMousePressed( new Point2d( m.getX(), m.getY() ) );
                }
            }

        }

        @Override
        public void mouseReleased( MouseEvent m ) {
            Object source = m.getSource();
            if ( source instanceof JPanel ) {
                // Scene2DPanel
                if ( ( (JPanel) source ).getName().equals( Scene2DPanel.SCENE2D_PANEL_NAME ) ) {

                    if ( isHorizontalRef == true ) {
                        if ( start == false ) {
                            start = true;
                            footPanel.setFocus( false );
                            panel.setFocus( true );
                        }
                        if ( footPrint.getLastFootprintPoint() != null && lastGeoReferencedPoint != null
                             && panel.getFocus() == true ) {
                            setValues();
                        }
                        int x = m.getX();
                        int y = m.getY();
                        GeoReferencedPoint geoReferencedPoint = new GeoReferencedPoint( x, y );
                        lastGeoReferencedPoint = geoReferencedPoint;
                        panel.addPoint( mappedPoints, geoReferencedPoint );
                        // panel.setTranslated( isHorizontalRef );
                        GeoReferencedPoint point = (GeoReferencedPoint) sceneValues.getWorldPoint( geoReferencedPoint );
                        tablePanel.setCoords( point );

                    } else {

                        mouseGeoRef.setMouseChanging( new Point2d(
                                                                   ( mouseGeoRef.getPointMousePressed().getX() - m.getX() ),
                                                                   ( mouseGeoRef.getPointMousePressed().getY() - m.getY() ) ) );
                        System.out.println( "MouseChanging: " + mouseGeoRef.getMouseChanging() );

                        // Prediction pred = new Prediction( mouse.getMouseChanging() );
                        // pred.start();

                        mouseGeoRef.setCumulatedMouseChanging( new Point2d(
                                                                            mouseGeoRef.getCumulatedMouseChanging().getX()
                                                                                                    + mouseGeoRef.getMouseChanging().getX(),
                                                                            mouseGeoRef.getCumulatedMouseChanging().getY()
                                                                                                    + mouseGeoRef.getMouseChanging().getY() ) );
                        mouseGeoRef.setPersistentCumulatedMouseChanging( new Point2d(
                                                                                      mouseGeoRef.getPersistentCumulatedMouseChanging().getX()
                                                                                                              + mouseGeoRef.getMouseChanging().getX(),
                                                                                      mouseGeoRef.getPersistentCumulatedMouseChanging().getY()
                                                                                                              + mouseGeoRef.getMouseChanging().getY() ) );
                        panel.setTranslationPoint( mouseGeoRef.getPersistentCumulatedMouseChanging() );
                        System.out.println( "persCum: " + mouseGeoRef.getPersistentCumulatedMouseChanging() );
                        panel.setBeginDrawImageAtPosition( new Point2d(
                                                                        panel.getBeginDrawImageAtPosition().getX()
                                                                                                - mouseGeoRef.getMouseChanging().getX(),
                                                                        panel.getBeginDrawImageAtPosition().getY()
                                                                                                - mouseGeoRef.getMouseChanging().getY() ) );
                        sceneValues.setStartRasterEnvelopePosition( mouseGeoRef.getMouseChanging() );
                        // 
                        // if the user went into any critical region
                        if ( mouseGeoRef.getCumulatedMouseChanging().getX() >= sceneValues.getImageMargin().getX()
                             || mouseGeoRef.getCumulatedMouseChanging().getX() <= -sceneValues.getImageMargin().getX()
                             || mouseGeoRef.getCumulatedMouseChanging().getY() >= sceneValues.getImageMargin().getY()
                             || mouseGeoRef.getCumulatedMouseChanging().getY() <= -sceneValues.getImageMargin().getY() ) {

                            Point2d updateDrawImageAtPosition = new Point2d(
                                                                             mouseGeoRef.getCumulatedMouseChanging().getX(),
                                                                             mouseGeoRef.getCumulatedMouseChanging().getY() );
                            System.out.println( "updatePos: " + updateDrawImageAtPosition );

                            // panel.setImageToDraw( model.getGeneratedImage() );
                            // sceneValues.setStartRasterEnvelopePosition( updateDrawImageAtPosition );
                            sceneValues.setMinPointPixel( null );
                            // panel.setTranslated( isHorizontalRef );
                            panel.setImageToDraw( model.generateSubImage( sceneValues.getImageDimension() ) );
                            mouseGeoRef.reset();
                            panel.setBeginDrawImageAtPosition( sceneValues.getImageStartPosition() );

                        }

                    }
                    panel.repaint();
                }
                // footprintPanel
                if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {
                    if ( isHorizontalRef == true ) {

                        if ( start == false ) {
                            start = true;
                            footPanel.setFocus( true );
                            panel.setFocus( false );
                        }
                        if ( footPrint.getLastFootprintPoint() != null && lastGeoReferencedPoint != null
                             && footPanel.getFocus() == true ) {
                            setValues();
                        }
                        int x = m.getX();
                        int y = m.getY();
                        footPrint.setLastFootprintPoint( footPrint.getClosestPoint( new FootprintPoint( x, y ) ) );
                        tablePanel.setCoords( footPrint.getLastFootprintPoint().getNewValue() );
                        footPanel.addPoint( mappedPoints, footPrint.getLastFootprintPoint().getNewValue() );

                    } else {
                        mouseFootprint.setMouseChanging( new Point2d(
                                                                      ( mouseFootprint.getPointMousePressed().getX() - m.getX() ),
                                                                      ( mouseFootprint.getPointMousePressed().getY() - m.getY() ) ) );

                        mouseFootprint.setCumulatedMouseChanging( new Point2d(
                                                                               mouseFootprint.getCumulatedMouseChanging().getX()
                                                                                                       + mouseFootprint.getMouseChanging().getX(),
                                                                               mouseFootprint.getCumulatedMouseChanging().getY()
                                                                                                       + mouseFootprint.getMouseChanging().getY() ) );

                        footPanel.setCumTranslationPoint( mouseFootprint.getCumulatedMouseChanging() );
                        footPrint.updatePoints( mouseFootprint.getMouseChanging() );
                        footPanel.setTranslated( isHorizontalRef );
                        System.out.println( isHorizontalRef );

                    }
                    footPanel.repaint();
                }
            }
        }

        /**
         * Sets values to the JTableModel and adds a new row to it.
         */
        private void setValues() {

            // footPrint.addPointToSelectedPointsList( lastFootprintPoint );
            footPrint.addToSelectedPoints( footPrint.getLastFootprintPoint() );
            addToMappedPoints( (FootprintPoint) footPrint.getLastFootprintPoint().getNewValue(), lastGeoReferencedPoint );
            footPrint.setLastFootprintPoint( null );
            lastGeoReferencedPoint = null;
            tablePanel.addRow();

        }
    }

    /**
     * 
     * The <Code>Prediction</Code> class should handle the getImage in the background.
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    class Prediction extends Thread {

        Point2d changing;

        public Prediction( Point2d changing ) {
            this.changing = changing;

        }

        public void run() {

            changePoint = new Point2d( changing.getX(), changing.getY() );
            System.out.println( "Threadchange: " + changePoint );

            // model.generatePredictedImage( changing );
            // model.generateImage( changing );

        }

    }

    /**
     * 
     * TODO add class documentation here
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    class Scene2DMouseMotionListener implements MouseMotionListener {

        @Override
        public void mouseDragged( MouseEvent arg0 ) {
            // TODO Auto-generated method stub

        }

        @Override
        public void mouseMoved( MouseEvent m ) {

            Object source = m.getSource();

            if ( source instanceof JPanel ) {
                // Scene2DPanel
                if ( ( (JPanel) source ).getName().equals( Scene2DPanel.SCENE2D_PANEL_NAME ) ) {

                }
                // footprintPanel
                if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {
                    // System.out.println( m.getPoint() );
                    if ( mouseFootprint != null ) {
                        mouseFootprint.setMouseMoved( new Point2d( m.getX(), m.getY() ) );
                    }
                }
            }
        }

    }

    /**
     * 
     * TODO add class documentation here
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    class Scene2DMouseWheelListener implements MouseWheelListener {

        @Override
        public void mouseWheelMoved( MouseWheelEvent m ) {

            Object source = m.getSource();

            if ( source instanceof JPanel ) {
                // Scene2DPanel
                if ( ( (JPanel) source ).getName().equals( Scene2DPanel.SCENE2D_PANEL_NAME ) ) {
                    if ( m.getWheelRotation() < 0 ) {
                        sceneValues.setSize( sceneValues.getSize() - .05f );
                    } else {
                        sceneValues.setSize( sceneValues.getSize() + .05f );
                    }
                    init();
                    panel.repaint();
                }
                // footprintPanel
                if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {
                    float newSize = 1.0f;
                    if ( m.getWheelRotation() < 0 ) {
                        newSize = footPrint.getSize() + .1f;
                    } else {
                        newSize = footPrint.getSize() - .1f;
                    }
                    footPrint.updatePoints( newSize );
                    updateMappedPoints();
                    footPanel.setPolygonList( footPrint.getPixelCoordinatePolygonList() );
                    footPanel.addPoint( mappedPoints, footPrint.getLastFootprintPoint().getNewValue() );
                    footPanel.repaint();
                }
            }

        }

    }

    /**
     * 
     * Controls the ComponentListener
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    class HoleWindowListener implements ComponentListener {

        @Override
        public void componentHidden( ComponentEvent arg0 ) {
            // TODO Auto-generated method stub

        }

        @Override
        public void componentMoved( ComponentEvent arg0 ) {
            // TODO Auto-generated method stub

        }

        @Override
        public void componentResized( ComponentEvent c ) {
            if ( model.getGeneratedImage() != null ) {
                init();

            }

        }

        @Override
        public void componentShown( ComponentEvent arg0 ) {
            // TODO Auto-generated method stub

        }

    }

    private void init() {
        // model.reset();
        sceneValues.setImageMargin( new Point2d( panel.getBounds().width * 0.1, panel.getBounds().height * 0.1 ) );
        sceneValues.setImageDimension( new Rectangle(
                                                      (int) ( panel.getBounds().width + 2 * sceneValues.getImageMargin().x ),
                                                      (int) ( panel.getBounds().height + 2 * sceneValues.getImageMargin().y ) ) );
        sceneValues.setImageStartPosition( new Point2d( -sceneValues.getImageMargin().x,
                                                        -sceneValues.getImageMargin().y ) );

        panel.setBeginDrawImageAtPosition( sceneValues.getImageStartPosition() );
        panel.setImageDimension( sceneValues.getImageDimension() );

        panel.setImageToDraw( model.generateSubImage( sceneValues.getImageDimension() ) );

        panel.repaint();
    }

    /**
     * Adds the <Code>AbstractPoint</Code>s to a map
     * 
     * @param mappedPointKey
     * @param mappedPointValue
     */
    private void addToMappedPoints( FootprintPoint mappedPointKey, GeoReferencedPoint mappedPointValue ) {
        if ( mappedPointKey != null && mappedPointValue != null ) {
            this.mappedPoints.add( new Pair<FootprintPoint, GeoReferencedPoint>( mappedPointKey, mappedPointValue ) );
        }

    }

    private void updateMappedPoints() {
        for ( Pair<FootprintPoint, GeoReferencedPoint> pair : mappedPoints ) {
            List<Point3Values> points = footPrint.getSelectedPoints();
            for ( Point3Values p : points ) {
                if ( p.getOldValue() == pair.first ) {
                    pair.first = (FootprintPoint) p.getNewValue();
                    break;
                }
            }

        }
    }

    private void removeFromMappedPoints( Pair<FootprintPoint, GeoReferencedPoint> pointFromTable ) {
        if ( pointFromTable != null ) {
            mappedPoints.remove( pointFromTable );
        }
    }

    private void removeAllFromMappedPoints() {
        mappedPoints = new ArrayList<Pair<FootprintPoint, GeoReferencedPoint>>();
        tablePanel.removeAllRows();
        panel.addPoint( null, null );
        footPanel.addPoint( null, null );
        footPrint.setLastFootprintPoint( null );
        lastGeoReferencedPoint = null;
        panel.repaint();
        footPanel.repaint();
        panel.setFocus( false );
        footPanel.setFocus( false );
        start = false;

    }

}
