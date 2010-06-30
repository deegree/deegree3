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

import java.awt.Polygon;
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
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.media.jai.WarpPolynomial;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import org.deegree.commons.utils.Pair;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.cs.CRS;
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.CoordinateTransformer;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.transformations.helmert.Helmert;
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
import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint;
import org.deegree.tools.crs.georeferencing.model.points.FootprintPoint;
import org.deegree.tools.crs.georeferencing.model.points.GeoReferencedPoint;
import org.deegree.tools.crs.georeferencing.model.points.Point4Values;
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

    private CRS sourceCRS;

    private CRS targetCRS;

    private List<Pair<Point4Values, Point4Values>> mappedPoints;

    public Controller( GRViewerGUI view, Scene2D model ) {
        this.view = view;
        this.model = model;
        this.panel = view.getScenePanel2D();
        this.footPanel = view.getFootprintPanel();
        this.navPanel = view.getNavigationPanel();
        this.footPrint = new Footprint();
        this.tablePanel = view.getPointTablePanel();
        this.start = false;
        this.glHandler = view.getOpenGLEventListener();

        this.mappedPoints = new ArrayList<Pair<Point4Values, Point4Values>>();
        this.footPanel.setOffset( 10 );

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
                        for ( Pair<Point4Values, Point4Values> p : mappedPoints ) {
                            double x = p.first.getWorldCoords().getX();
                            double y = p.first.getWorldCoords().getY();
                            if ( pointFromTable.getX() == x && pointFromTable.getY() == y ) {
                                removeFromMappedPoints( p );
                                contained = true;
                                break;
                            }
                        }
                        if ( contained == false ) {
                            footPanel.setLastAbstractPoint( null, null );
                            panel.setLastAbstractPoint( null, null );
                        }

                        tablePanel.removeRow( tableRow );

                    }

                    panel.addPoint( mappedPoints, panel.getLastAbstractPoint() );
                    footPanel.addPoint( mappedPoints, footPanel.getLastAbstractPoint() );
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
                    if ( footPanel.getLastAbstractPoint() != null && panel.getLastAbstractPoint() != null ) {
                        addToMappedPoints( footPanel.getLastAbstractPoint(), panel.getLastAbstractPoint() );
                        footPanel.setLastAbstractPoint( null, null );
                        panel.setLastAbstractPoint( null, null );
                    }

                    int arraySize = mappedPoints.size() * 2;
                    if ( arraySize > 0 ) {

                        CRSCodeType[] s = null;
                        CRSCodeType[] t = null;
                        try {
                            s = sourceCRS.getWrappedCRS().getCodes();
                            t = targetCRS.getWrappedCRS().getCodes();
                        } catch ( UnknownCRSException e1 ) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }

                        int size = s.length + t.length;
                        int countT = 0;
                        CRSCodeType[] codeTypes = new CRSCodeType[size];
                        for ( int i = 0; i < s.length; i++ ) {
                            codeTypes[i] = s[i];
                        }
                        for ( int i = s.length; i < size; i++ ) {
                            codeTypes[i] = t[countT];
                            countT++;
                        }
                        CRSIdentifiable identifiable = new CRSIdentifiable( codeTypes );

                        // final Helmert wgs_info = new Helmert( sourceCRS, targetCRS, codeTypes );

                        int arrSize = footPrint.getWorldCoordinates().length;
                        // double[] ordinatesSrc = new double[arraySize];
                        // double[] ordinatesDst = new double[arraySize];
                        float[] ordinatesSrc = new float[arraySize];
                        float[] ordinatesDst = new float[arraySize];
                        int counterSrc = 0;
                        int counterDst = 0;
                        List<double[]> coordinateList = new LinkedList<double[]>();
                        CoordinateTransformer ct;
                        try {
                            ct = new CoordinateTransformer( targetCRS.getWrappedCRS() );

                            for ( Pair<Point4Values, Point4Values> p : mappedPoints ) {

                                // double[] from = new double[3];
                                double x = p.first.getWorldCoords().getX();
                                double y = p.first.getWorldCoords().getY();

                                // from[0] = x;
                                // from[1] = y;
                                // from[2] = 0;
                                // coordinateList.add( from );
                                // System.out.println( "Before transform: " + x + " " + y );

                                ordinatesDst[counterSrc] = (float) x;
                                ordinatesDst[++counterSrc] = (float) y;
                                // ordinatesSrc[counterSrc] = x;
                                // ordinatesSrc[++counterSrc] = y;
                                counterSrc++;
                                Point4Values pValue = p.second;
                                x = pValue.getWorldCoords().getX();
                                y = pValue.getWorldCoords().getY();
                                ordinatesSrc[counterDst] = (float) x;
                                ordinatesSrc[++counterDst] = (float) y;
                                // ordinatesDst[counterDst] = x;
                                // ordinatesDst[++counterDst] = y;
                                counterDst++;

                            }
                            double x;
                            double y;
                            for ( double[] c : coordinateList ) {
                                try {
                                    double[] out = ct.transform( sourceCRS.getWrappedCRS(), c, new double[3] );

                                    // for ( FootprintPoint d : footPrint.getWorldCoordinatePoints() ) {
                                    // System.out.println( "newX: " + d.getX() * out[0] / c[0] );
                                    // System.out.println( "newY: " + d.getY() * out[1] / c[1] );
                                    // }
                                    // double newX = 25.0 * out[0] / c[0];
                                    // System.out.println( "After transform: " + out[0] + " " + out[1] );

                                } catch ( IllegalArgumentException e1 ) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                                } catch ( TransformationException e1 ) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                                } catch ( UnknownCRSException e1 ) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                                }
                            }
                        } catch ( IllegalArgumentException e2 ) {
                            // TODO Auto-generated catch block
                            e2.printStackTrace();
                        } catch ( UnknownCRSException e2 ) {
                            // TODO Auto-generated catch block
                            e2.printStackTrace();
                        }

                        Helmert wgs_info = null;
                        try {
                            wgs_info = new Helmert( sourceCRS.getWrappedCRS(), targetCRS.getWrappedCRS(), codeTypes );
                        } catch ( UnknownCRSException e1 ) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                        System.out.println( "\n\n coordinates" );
                        for ( int i = 0; i < ordinatesDst.length; i += 2 ) {
                            System.out.println( ordinatesSrc[i] + "/" + ordinatesSrc[i + 1] + " -- " + ordinatesDst[i]
                                                + "/" + ordinatesDst[i + 1] );
                        }
                        WarpPolynomial warp = WarpPolynomial.createWarp( ordinatesSrc, 0, ordinatesDst, 0,
                                                                         ordinatesSrc.length, 1f, 1f, 1f, 1f, 1 );
                        // for ( float p : warp.getXCoeffs() ) {
                        // System.out.println( "warp: " + p );
                        // }
                        System.out.println( "coeff:" );
                        float[] x = warp.getXCoeffs();
                        float[] y = warp.getYCoeffs();
                        for ( int i = 0; i < y.length; i++ ) {
                            System.out.println( i + " " + x[i] + " " + y[i] );
                        }

                        List<Point3d> result = new ArrayList<Point3d>();
                        System.out.println();
                        System.out.println( "resid" );
                        double rx = 0;
                        double ry = 0;
                        // int[] tz = sceneValues.getPixelCoordinate( new Point2D.Float( 0.03f, 6.0f ) );

                        List<Polygon> transformedPolygonList = new ArrayList<Polygon>();
                        for ( Polygon po : footPrint.getWorldCoordinatePolygonList() ) {

                            int[] x2 = new int[po.npoints];
                            int[] y2 = new int[po.npoints];
                            for ( int i = 0; i < po.npoints; i++ ) {

                                Point2D p = warp.mapDestPoint( new Point2D.Float( po.xpoints[i], po.ypoints[i] ) );
                                AbstractGRPoint convertPoint = new GeoReferencedPoint( p.getX(), p.getY() );
                                int[] value = sceneValues.getPixelCoordinate( convertPoint );
                                x2[i] = value[0];
                                y2[i] = value[1];

                            }

                            Polygon p = new Polygon( x2, y2, po.npoints );
                            transformedPolygonList.add( p );
                        }
                        // for ( int i = 0; i < ordinatesDst.length; i += 2 ) {
                        // Point2D p = warp.mapDestPoint( new Point2D.Float( ordinatesDst[i], ordinatesDst[i + 1] ) );
                        // // System.out.println( "p: " + p + " : " + p.getX() + " - " + ordinatesSrc[i] );
                        // rx += ( p.getX() - ordinatesSrc[i] );
                        // ry += ( p.getY() - ordinatesSrc[i + 1] );
                        // System.out.println( ( i / 2 ) + " -> " + ( p.getX() - ordinatesSrc[i] ) + "/"
                        // + ( p.getY() - ordinatesSrc[i + 1] ) );
                        // result.add( new Point3d( p.getX(), p.getY(), 0 ) );
                        //
                        // }
                        System.out.println();
                        System.out.println( "mean resid" );
                        rx /= ( ordinatesSrc.length / 2 );
                        ry /= ( ordinatesSrc.length / 2 );
                        System.out.println( rx + " " + ry );

                        for ( Point3d p : result ) {
                            System.out.println( p.getX() + " " + p.getY() );
                        }
                        // Matrix4d m = wgs_info.getAsAffineTransform();
                        // try {
                        // wgs_info.doTransform( ordinatesSrc, 0, ordinatesDst, 0, ordinatesSrc.length );
                        // m = wgs_info.getAsAffineTransform();
                        // } catch ( TransformationException e1 ) {
                        // e1.printStackTrace();
                        // }

                        // for ( double d : ordinatesDst ) {
                        // System.out.println( "WGS_INFO: " + m );
                        // }
                        panel.setPolygonList( transformedPolygonList, mappedPoints );

                        panel.repaint();

                    } else {
                        try {
                            throw new Exception( "You must specify coordinates to transform" );
                        } catch ( Exception e1 ) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
            if ( source instanceof JMenuItem ) {
                if ( ( (JMenuItem) source ).getText().startsWith( GRViewerGUI.MENUITEM_GETMAP ) ) {
                    mouseGeoRef = new MouseModel();
                    init();
                    panel.setinitialResolution( sceneValues.getSize() );
                    targetCRS = sceneValues.getCrs();
                    panel.addScene2DMouseListener( new Scene2DMouseListener() );
                    // panel.addScene2DMouseMotionListener( new Scene2DMouseMotionListener() );
                    panel.addScene2DMouseWheelListener( new Scene2DMouseWheelListener() );

                }
                if ( ( (JMenuItem) source ).getText().startsWith( GRViewerGUI.MENUITEM_GET_3DOBJECT ) ) {
                    mouseFootprint = new MouseModel();
                    // TODO at the moment the file which is used is static in the GRViewerGUI!!!
                    List<WorldRenderableObject> rese = File3dImporter.open( view, view.fileName() );
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

                    footPanel.setPolygonList( footPrint.getWorldCoordinatePolygonList() );

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
                        if ( footPanel.getLastAbstractPoint() != null && panel.getLastAbstractPoint() != null
                             && panel.getFocus() == true ) {
                            setValues();
                        }
                        if ( footPanel.getLastAbstractPoint() == null && panel.getLastAbstractPoint() == null
                             && panel.getFocus() == true ) {
                            tablePanel.addRow();
                        }
                        int x = m.getX();
                        int y = m.getY();
                        GeoReferencedPoint geoReferencedPoint = new GeoReferencedPoint( x, y );
                        GeoReferencedPoint g = (GeoReferencedPoint) sceneValues.getWorldPoint( geoReferencedPoint );
                        panel.setLastAbstractPoint( geoReferencedPoint,
                                                    (GeoReferencedPoint) sceneValues.getWorldPoint( geoReferencedPoint ) );
                        System.out.println( geoReferencedPoint + " -> "
                                            + (GeoReferencedPoint) sceneValues.getWorldPoint( geoReferencedPoint )
                                            + " -> " + sceneValues.getPixelCoordinate( g )[0] + ", "
                                            + sceneValues.getPixelCoordinate( g )[1] );
                        panel.addPoint( mappedPoints, panel.getLastAbstractPoint() );
                        // panel.setTranslated( isHorizontalRef );

                        tablePanel.setCoords( panel.getLastAbstractPoint().getWorldCoords() );

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
                        if ( footPanel.getLastAbstractPoint() != null && panel.getLastAbstractPoint() != null
                             && footPanel.getFocus() == true ) {
                            setValues();
                        }
                        if ( footPanel.getLastAbstractPoint() == null && panel.getLastAbstractPoint() == null
                             && footPanel.getFocus() == true ) {
                            tablePanel.addRow();
                        }
                        int x = m.getX();
                        int y = m.getY();
                        System.out.println( "[CONTROLLER] " + x + ", " + y );
                        footPanel.setTranslated( isHorizontalRef );
                        Pair<AbstractGRPoint, FootprintPoint> point = footPanel.getClosestPoint( new FootprintPoint( x,
                                                                                                                     y ) );
                        footPanel.setLastAbstractPoint( point.first, point.second );
                        tablePanel.setCoords( footPanel.getLastAbstractPoint().getWorldCoords() );
                        footPanel.addPoint( mappedPoints, footPanel.getLastAbstractPoint() );

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
                        // footPrint.updatePoints( mouseFootprint.getMouseChanging() );
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

            footPanel.addToSelectedPoints( footPanel.getLastAbstractPoint() );
            panel.addToSelectedPoints( panel.getLastAbstractPoint() );
            addToMappedPoints( footPanel.getLastAbstractPoint(), panel.getLastAbstractPoint() );
            footPanel.setLastAbstractPoint( null, null );
            panel.setLastAbstractPoint( null, null );

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
                    float newSize = 1.0f;
                    if ( m.getWheelRotation() < 0 ) {
                        newSize = sceneValues.getSize() - .05f;
                    } else {
                        newSize = sceneValues.getSize() + .05f;
                    }
                    // TODO sceneValues options size ändern
                    sceneValues.setSize( newSize );
                    init();
                    panel.updatePoints( newSize );
                    panel.repaint();
                }
                // footprintPanel
                if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {
                    float newSize = 1.0f;
                    if ( m.getWheelRotation() < 0 ) {
                        newSize = footPanel.getResolution() + .1f;
                    } else {
                        newSize = footPanel.getResolution() - .1f;
                    }
                    footPanel.updatePoints( newSize );
                    updateMappedPoints();
                    // footPanel.setTranslated( false );
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
    private void addToMappedPoints( Point4Values mappedPointKey, Point4Values mappedPointValue ) {
        if ( mappedPointKey != null && mappedPointValue != null ) {
            this.mappedPoints.add( new Pair<Point4Values, Point4Values>( mappedPointKey, mappedPointValue ) );
        }

    }

    private void updateMappedPoints() {
        for ( Pair<Point4Values, Point4Values> pair : mappedPoints ) {
            List<Point4Values> points = footPanel.getSelectedPoints();
            for ( Point4Values p : points ) {
                if ( p.getOldValue() == pair.first.getOldValue() ) {
                    pair.first = new Point4Values( p.getOldValue(), p.getInitialValue(), p.getNewValue(),
                                                   p.getWorldCoords() );
                    break;
                }
            }

        }
    }

    private void removeFromMappedPoints( Pair<Point4Values, Point4Values> pointFromTable ) {
        if ( pointFromTable != null ) {
            mappedPoints.remove( pointFromTable );
        }
    }

    private void removeAllFromMappedPoints() {
        mappedPoints = new ArrayList<Pair<Point4Values, Point4Values>>();
        tablePanel.removeAllRows();
        panel.addPoint( null, null );
        footPanel.addPoint( null, null );
        footPanel.setLastAbstractPoint( null, null );
        panel.setLastAbstractPoint( null, null );
        panel.repaint();
        footPanel.repaint();
        panel.setFocus( false );
        footPanel.setFocus( false );
        start = false;

    }

}
