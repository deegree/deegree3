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

import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.vecmath.Point2d;

import org.deegree.commons.utils.Pair;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.cs.CRS;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.Ring;
import org.deegree.rendering.r3d.model.geometry.GeometryQualityModel;
import org.deegree.rendering.r3d.model.geometry.SimpleAccessGeometry;
import org.deegree.rendering.r3d.opengl.display.OpenGLEventHandler;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.WorldRenderableObject;
import org.deegree.tools.crs.georeferencing.application.transformation.Helmert4Transform;
import org.deegree.tools.crs.georeferencing.application.transformation.Polynomial;
import org.deegree.tools.crs.georeferencing.application.transformation.TransformationMethod;
import org.deegree.tools.crs.georeferencing.application.transformation.TransformationMethod.TransformationType;
import org.deegree.tools.crs.georeferencing.communication.GRViewerGUI;
import org.deegree.tools.crs.georeferencing.communication.GUIConstants;
import org.deegree.tools.crs.georeferencing.communication.PointTableFrame;
import org.deegree.tools.crs.georeferencing.communication.dialog.ButtonPanel;
import org.deegree.tools.crs.georeferencing.communication.dialog.coordinatejump.CoordinateJumperDialog;
import org.deegree.tools.crs.georeferencing.communication.dialog.error.ErrorDialog;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.GeneralPanel;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.GenericSettingsPanel;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.NavigationPanel;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.OptionDialog;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.SettingsPanel;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.ViewPanel;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.GenericSettingsPanel.PanelType;
import org.deegree.tools.crs.georeferencing.communication.navigationbar.NavigationBarPanelFootprint;
import org.deegree.tools.crs.georeferencing.communication.navigationbar.NavigationBarPanelGeoref;
import org.deegree.tools.crs.georeferencing.communication.panel2D.AbstractPanel2D;
import org.deegree.tools.crs.georeferencing.communication.panel2D.BuildingFootprintPanel;
import org.deegree.tools.crs.georeferencing.communication.panel2D.Scene2DPanel;
import org.deegree.tools.crs.georeferencing.model.Footprint;
import org.deegree.tools.crs.georeferencing.model.Scene2D;
import org.deegree.tools.crs.georeferencing.model.dialog.OptionDialogModel;
import org.deegree.tools.crs.georeferencing.model.exceptions.NumberException;
import org.deegree.tools.crs.georeferencing.model.mouse.FootprintMouseModel;
import org.deegree.tools.crs.georeferencing.model.mouse.GeoReferencedMouseModel;
import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint;
import org.deegree.tools.crs.georeferencing.model.points.FootprintPoint;
import org.deegree.tools.crs.georeferencing.model.points.GeoReferencedPoint;
import org.deegree.tools.crs.georeferencing.model.points.Point4Values;
import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint.PointType;
import org.deegree.tools.crs.georeferencing.model.textfield.AbstractTextfieldModel;
import org.deegree.tools.crs.georeferencing.model.textfield.TextFieldModel;
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

    private NavigationBarPanelGeoref navPanelGeoref;

    private NavigationBarPanelFootprint navPanelFoot;

    private PointTableFrame tablePanel;

    private RasterIOOptions options;

    private Footprint footPrint;

    private TextFieldModel textFieldModel;

    private OpenGLEventHandler glHandler;

    private GeoReferencedMouseModel mouseGeoRef;

    private FootprintMouseModel mouseFootprint;

    private Point2d changePoint;

    private boolean isHorizontalRef, start, isControlDown, selected;

    // private boolean isZoomIn, isZoomOut

    private GeometryFactory geom;

    private CRS sourceCRS, targetCRS;

    private List<Pair<Point4Values, Point4Values>> mappedPoints;

    private TransformationType transformationType;

    private TransformationMethod transform;

    private ParameterStore store;

    private NavigationPanel optionNavPanel;

    private OptionDialogModel dialogModel;

    public int order;

    private SettingsPanel optionSettPanel;

    private OptionDialog optionDialog;

    private CoordinateJumperDialog jumperDialog;

    private GenericSettingsPanel optionSettingPanel;

    private JToggleButton buttonPanGeoref, buttonPanFoot, buttonZoomInGeoref, buttonZoominFoot, buttonZoomoutGeoref,
                            buttonZoomoutFoot, buttonCoord;

    private ButtonModel buttonModel;

    public Controller( GRViewerGUI view, Scene2D model, ParameterStore store ) {

        geom = new GeometryFactory();
        options = new RasterOptions( store ).getOptions();
        sceneValues = new Scene2DValues( options, geom );
        this.view = view;
        this.model = model;
        this.panel = view.getScenePanel2D();
        this.footPanel = view.getFootprintPanel();
        this.navPanelGeoref = view.getNavigationPanelGeoref();
        this.navPanelFoot = view.getNaviPanelFoot();
        this.footPrint = new Footprint( sceneValues, geom );

        this.start = false;

        this.glHandler = view.getOpenGLEventListener();
        this.store = store;
        this.textFieldModel = new TextFieldModel();
        this.dialogModel = new OptionDialogModel();
        AbstractPanel2D.selectedPointSize = this.dialogModel.getSelectionPointSize().first;

        this.mappedPoints = new ArrayList<Pair<Point4Values, Point4Values>>();

        model.init( options, sceneValues );
        view.addListeners( new ButtonListener() );
        // view.addChangeListener( new ChangeActionListener() );
        view.addHoleWindowListener( new HoleWindowListener() );

        // init the scenePanel and the mouseinteraction of it
        initGeoReferencingScene();

        // init the footPanel and the mouseinteraction of it
        initFootprintScene();

        initToggleButtons();

        // init the transformation method
        this.tablePanel = new PointTableFrame();
        this.tablePanel.addListeners( new ButtonListener() );
        this.tablePanel.addHorizontalRefListener( new ButtonListener() );
        transform = null;
        if ( transformationType == null ) {
            order = 1;
            for ( JCheckBox box : tablePanel.getCheckbox().getList() ) {
                if ( ( box ).getText().startsWith( GUIConstants.MENUITEM_TRANS_POLYNOM_FIRST ) ) {
                    transformationType = TransformationType.PolynomialFirstOrder;
                    tablePanel.activateTransformationCheckbox( box );
                    break;
                }
            }

        }

        isHorizontalRef = true;

    }

    /**
     * Initializes the navigation buttons that are registered for each map.
     */
    private void initToggleButtons() {
        buttonPanGeoref = view.getNavigationPanelGeoref().getButtonPan();
        buttonPanFoot = view.getNaviPanelFoot().getButtonPan();
        buttonZoomInGeoref = view.getNavigationPanelGeoref().getButtonZoomIn();
        buttonZoominFoot = view.getNaviPanelFoot().getButtonZoomIn();
        buttonZoomoutGeoref = view.getNavigationPanelGeoref().getButtonZoomOut();
        buttonZoomoutFoot = view.getNaviPanelFoot().getButtonZoomOut();
        buttonCoord = view.getNavigationPanelGeoref().getButtonZoomCoord();
    }

    /**
     * Selects one navigation button and deselects the other so that the focus is just on this one button. The
     * georeferencing for the georeferenced map will be turned off in this case. <br>
     * If the button is selected already, that will be deselected and there is a horizontal referencing possible again.
     * 
     * @param t
     *            the toggleButton that should be selected/deselected, not <Code>null</Code>.
     */
    private void selectGeorefToggleButton( JToggleButton t ) {
        boolean checkSelected = false;
        buttonModel = t.getModel();
        selected = buttonModel.isSelected();
        if ( selected == false ) {
            isHorizontalRef = true;
        } else {
            checkSelected = true;
            buttonPanGeoref.setSelected( false );
            buttonZoomInGeoref.setSelected( false );
            buttonZoomoutGeoref.setSelected( false );
            buttonCoord.setSelected( false );
            isHorizontalRef = false;
        }
        if ( t == buttonPanGeoref ) {
            buttonPanGeoref.setSelected( checkSelected );
        } else if ( t == buttonZoomInGeoref ) {
            buttonZoomInGeoref.setSelected( checkSelected );
        } else if ( t == buttonZoomoutGeoref ) {
            buttonZoomoutGeoref.setSelected( checkSelected );
        } else if ( t == buttonCoord ) {
            buttonCoord.setSelected( checkSelected );
            if ( checkSelected == true ) {
                jumperDialog = new CoordinateJumperDialog();
                jumperDialog.getCoordinateJumper().setToolTipText( textFieldModel.getTooltipText() );
                jumperDialog.addListeners( new ButtonListener() );
                jumperDialog.setVisible( true );
            }
        }
    }

    /**
     * Selects one navigation button and deselects the other so that the focus is just on this one button. The
     * georeferencing for the footprint view will be turned off in this case. <br>
     * If the button is selected already, that will be deselected and there is a horizontal referencing possible again.
     * 
     * @param t
     *            the toggleButton that should be selected/deselected, not <Code>null</Code>.
     */
    private void selectFootprintToggleButton( JToggleButton t ) {
        buttonPanFoot.setSelected( false );
        buttonZoominFoot.setSelected( false );
        buttonZoomoutFoot.setSelected( false );
        isHorizontalRef = false;
        if ( t == buttonPanFoot ) {
            buttonPanFoot.setSelected( true );

        } else if ( t == buttonZoominFoot ) {
            buttonZoominFoot.setSelected( true );

        } else if ( t == buttonZoomoutFoot ) {
            buttonZoomoutFoot.setSelected( true );

        }

    }

    /**
     * Initializes the footprint scene.
     */
    private void initFootprintScene() {
        footPanel.addScene2DMouseListener( new Scene2DMouseListener() );
        footPanel.addScene2DMouseMotionListener( new Scene2DMouseMotionListener() );
        footPanel.addScene2DMouseWheelListener( new Scene2DMouseWheelListener() );
        // footPanel.addScene2DActionKeyListener( new Scene2DActionKeyListener() );
        // footPanel.addScene2DFocusListener( new Scene2DFocusListener() );

        sceneValues.setDimenstionFootpanel( footPanel.getBounds() );
        mouseFootprint = new FootprintMouseModel();

        List<WorldRenderableObject> rese = File3dImporter.open( view, store.getFilename() );
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

                }
            }

        }

        footPrint.generateFootprints( geometryThatIsTaken );

        footPanel.setPolygonList( footPrint.getWorldCoordinateRingList(), sceneValues );

        footPanel.repaint();

    }

    /**
     * Initializes the georeferenced scene.
     */
    private void initGeoReferencingScene() {
        mouseGeoRef = new GeoReferencedMouseModel();
        init();
        targetCRS = sceneValues.getCrs();
        panel.addScene2DMouseListener( new Scene2DMouseListener() );
        panel.addScene2DMouseMotionListener( new Scene2DMouseMotionListener() );
        panel.addScene2DMouseWheelListener( new Scene2DMouseWheelListener() );
        // panel.addScene2DActionKeyListener( new Scene2DActionKeyListener() );
        // panel.addScene2DFocusListener( new Scene2DFocusListener() );

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

        @Override
        public void actionPerformed( ActionEvent e ) {
            Object source = e.getSource();
            if ( source instanceof JCheckBox ) {
                JCheckBox selectedCheckbox = (JCheckBox) source;
                if ( ( selectedCheckbox ).getText().startsWith( NavigationBarPanelGeoref.HORIZONTAL_REFERENCING ) ) {
                    if ( isHorizontalRef == false ) {
                        isHorizontalRef = true;
                    } else {
                        isHorizontalRef = false;
                    }
                }
                if ( ( selectedCheckbox ).getText().startsWith( GeneralPanel.SNAPPING_TEXT ) ) {

                    boolean isSnappingOn = false;
                    if ( dialogModel.getSnappingOnOff().second == false ) {
                        isSnappingOn = true;

                    } else {
                        isSnappingOn = false;
                    }
                    dialogModel.setSnappingOnOff( isSnappingOn );
                }
                if ( ( selectedCheckbox ).getText().startsWith( GUIConstants.MENUITEM_TRANS_POLYNOM_FIRST ) ) {

                    transformationType = TransformationType.PolynomialFirstOrder;
                    tablePanel.activateTransformationCheckbox( selectedCheckbox );
                }
                if ( ( selectedCheckbox ).getText().startsWith( GUIConstants.MENUITEM_TRANS_HELMERT ) ) {

                    transformationType = TransformationType.Helmert_4;
                    tablePanel.activateTransformationCheckbox( selectedCheckbox );
                }

            }

            if ( source instanceof JToggleButton ) {
                JToggleButton tb = (JToggleButton) source;

                if ( tb.getName().startsWith( GUIConstants.JBUTTON_PAN ) ) {

                    if ( tb == buttonPanGeoref ) {
                        selectGeorefToggleButton( tb );
                        // TODO zoom functionality should be deactivated
                    } else {
                        selectFootprintToggleButton( tb );
                    }
                }
                if ( tb.getName().startsWith( GUIConstants.JBUTTON_ZOOM_COORD ) ) {

                    if ( tb == buttonCoord ) {
                        selectGeorefToggleButton( tb );
                    } else {
                        selectFootprintToggleButton( tb );
                    }
                }
                if ( tb.getName().startsWith( GUIConstants.JBUTTON_ZOOM_IN ) ) {

                    if ( tb == buttonZoomInGeoref ) {
                        selectGeorefToggleButton( tb );
                        // TODO pan should be deactivated
                    } else {
                        selectFootprintToggleButton( tb );
                    }
                }
                if ( tb.getName().startsWith( GUIConstants.JBUTTON_ZOOM_OUT ) ) {

                    if ( tb == buttonZoomoutGeoref ) {
                        selectGeorefToggleButton( tb );
                        // TODO pan functionality should be deactivated
                    } else {
                        selectFootprintToggleButton( tb );
                    }
                }
            }

            if ( source instanceof JButton ) {
                if ( ( (JButton) source ).getText().startsWith( PointTableFrame.BUTTON_DELETE_SELECTED ) ) {
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
                                panel.removeFromSelectedPoints( p.second );
                                footPanel.removeFromSelectedPoints( p.first );
                                break;
                            }
                        }
                        if ( contained == false ) {
                            footPanel.setLastAbstractPoint( null, null );
                            panel.setLastAbstractPoint( null, null );
                        }

                        tablePanel.removeRow( tableRow );

                    }

                    panel.repaint();
                    footPanel.repaint();
                }

                if ( ( (JButton) source ).getText().startsWith( PointTableFrame.BUTTON_DELETE_ALL ) ) {
                    removeAllFromMappedPoints();

                }
                if ( ( (JButton) source ).getText().startsWith( PointTableFrame.COMPUTE_BUTTON_NAME ) ) {
                    // swap the tempPoints into the map now
                    if ( footPanel.getLastAbstractPoint() != null && panel.getLastAbstractPoint() != null ) {
                        setValues();
                    }
                    // System.out.println( sourceCRS + " " + targetCRS );

                    switch ( transformationType ) {

                    case PolynomialFirstOrder:
                        // transform = new PolynomialFirstOrder( mappedPoints, footPrint, sceneValues, sourceCRS,
                        // targetCRS, 1 );
                        // transform = new LeastSquarePolynomial( mappedPoints, footPrint, sceneValues, sourceCRS,
                        // targetCRS, order );
                        order = 1;
                        transform = new Polynomial( mappedPoints, footPrint, sceneValues, sourceCRS, targetCRS, order );
                        break;
                    case Helmert_4:
                        order = 1;
                        transform = new Helmert4Transform( mappedPoints, footPrint, sceneValues, sourceCRS, targetCRS,
                                                           order );

                        break;
                    }
                    List<Ring> polygonRing = transform.computeRingList();

                    panel.setPolygonList( polygonRing, sceneValues );

                    panel.repaint();

                    reset();
                }
                if ( ( (JButton) source ).getText().startsWith( ButtonPanel.BUTTON_TEXT_CANCEL ) ) {
                    if ( optionDialog != null && optionDialog.isVisible() == true ) {
                        dialogModel.transferOldToNew();
                        AbstractPanel2D.selectedPointSize = dialogModel.getSelectionPointSize().first;
                        panel.repaint();
                        footPanel.repaint();
                        optionDialog.setVisible( false );
                    } else if ( jumperDialog != null && jumperDialog.isVisible() == true ) {
                        jumperDialog.setVisible( false );

                        selected = false;
                        buttonModel.setSelected( false );
                        isHorizontalRef = true;

                    }

                }
                if ( ( (JButton) source ).getText().startsWith( ButtonPanel.BUTTON_TEXT_OK ) ) {
                    if ( optionDialog != null && optionDialog.isVisible() == true ) {
                        boolean isRunIntoTrouble = false;
                        if ( optionSettingPanel != null ) {

                            if ( optionSettingPanel instanceof ViewPanel ) {
                                // if the custom radiobutton is selected and there is something inside the textField
                                if ( !( (ViewPanel) optionSettingPanel ).getTextFieldCustom().getText().equals( "" )
                                     && ( (ViewPanel) optionSettingPanel ).getRadioCustom().getSelectedObjects() != null ) {
                                    // here you have to check about the input for the custom textfield. Keylistener for
                                    // the textfield while typing in is problematic because you can workaround with
                                    // copy&paste...so this should be the way to go.

                                    String textInput = ( (ViewPanel) optionSettingPanel ).getTextFieldCustom().getText();
                                    if ( AbstractTextfieldModel.validateInt( textInput ) ) {
                                        dialogModel.setTextFieldKeyString( textInput );
                                        dialogModel.setSelectionPointSize( Integer.parseInt( dialogModel.getTextFieldKeyString().second ) );
                                        isRunIntoTrouble = false;
                                    } else {
                                        new ErrorDialog( optionDialog, JDialog.ERROR,
                                                         "Insert numbers only into the textField!" );
                                        isRunIntoTrouble = true;
                                    }

                                }
                            }
                        }
                        if ( isRunIntoTrouble == false ) {
                            dialogModel.transferNewToOld();
                            AbstractPanel2D.selectedPointSize = dialogModel.getSelectionPointSize().first;
                            panel.repaint();
                            footPanel.repaint();
                            optionDialog.setVisible( false );
                        }
                    } else if ( jumperDialog != null && jumperDialog.isVisible() == true ) {

                        try {
                            textFieldModel.setTextInput( jumperDialog.getCoordinateJumper().getText() );
                            if ( sceneValues.getTransformedBounds() != null ) {
                                System.out.println( textFieldModel.toString() );
                                if ( textFieldModel.getSpanX() != -1 && textFieldModel.getSpanY() != -1 ) {

                                    sceneValues.setCentroidWorldEnvelopePosition( textFieldModel.getxCoordinate(),
                                                                                  textFieldModel.getyCoordiante(),
                                                                                  textFieldModel.getSpanX(),
                                                                                  textFieldModel.getSpanY(),
                                                                                  PointType.GeoreferencedPoint );

                                } else {
                                    sceneValues.setCentroidWorldEnvelopePosition( textFieldModel.getxCoordinate(),
                                                                                  textFieldModel.getyCoordiante(),
                                                                                  PointType.GeoreferencedPoint );

                                }
                                jumperDialog.setVisible( false );
                                selected = false;
                                buttonModel.setSelected( false );
                                isHorizontalRef = true;
                                panel.setImageToDraw( model.generateSubImageFromRaster( sceneValues.getSubRaster() ) );
                                panel.updatePoints( sceneValues );
                                panel.repaint();

                            }
                        } catch ( NumberException e1 ) {

                            new ErrorDialog( view, JDialog.ERROR, e1.getMessage() );
                        }
                    }
                }
            }
            if ( source instanceof JMenuItem ) {

                if ( ( (JMenuItem) source ).getText().startsWith( GRViewerGUI.MENUITEM_EDIT_OPTIONS ) ) {
                    DefaultMutableTreeNode root = new DefaultMutableTreeNode( "Options" );

                    dialogModel.createNodes( root );
                    optionDialog = new OptionDialog( view, root );
                    optionDialog.getButtonPanel().addListeners( new ButtonListener() );
                    optionNavPanel = optionDialog.getNavigationPanel();
                    optionSettPanel = optionDialog.getSettingsPanel();

                    // add the listener to the navigation panel
                    optionNavPanel.addTreeListener( new NavigationTreeSelectionListener() );

                    optionDialog.setVisible( true );

                }

            }
            if ( source instanceof JRadioButton ) {
                if ( ( (JRadioButton) source ).getText().startsWith( ViewPanel.TWO ) ) {

                    dialogModel.setSelectionPointSize( 2 );
                }
                if ( ( (JRadioButton) source ).getText().startsWith( ViewPanel.THREE ) ) {

                    dialogModel.setSelectionPointSize( 3 );
                }
                if ( ( (JRadioButton) source ).getText().startsWith( ViewPanel.DEFAULT ) ) {

                    dialogModel.setSelectionPointSize( 5 );

                }
                if ( ( (JRadioButton) source ).getText().startsWith( ViewPanel.SEVEN ) ) {

                    dialogModel.setSelectionPointSize( 7 );
                }
                if ( ( (JRadioButton) source ).getText().startsWith( ViewPanel.TEN ) ) {

                    dialogModel.setSelectionPointSize( 10 );
                }
                if ( ( (JRadioButton) source ).getText().startsWith( ViewPanel.CUSTOM ) ) {
                    if ( !( (ViewPanel) optionSettingPanel ).getTextFieldCustom().getText().equals( "" ) ) {
                        dialogModel.setTextFieldKeyString( ( (ViewPanel) optionSettingPanel ).getTextFieldCustom().getText() );
                        int i;
                        try {
                            i = Integer.parseInt( dialogModel.getTextFieldKeyString().second );
                            dialogModel.setSelectionPointSize( i );
                        } catch ( NumberFormatException ex ) {
                            new ErrorDialog( optionDialog, JDialog.ERROR, "This is not a number" );
                        }

                    }
                }

            }

        }
    }

    class ChangeActionListener implements ChangeListener {
        @Override
        public void stateChanged( ChangeEvent changeEvent ) {
            // AbstractButton abstractButton = (AbstractButton) changeEvent.getSource();
            // ButtonModel buttonModel = abstractButton.getModel();
            // boolean armed = buttonModel.isArmed();
            // boolean pressed = buttonModel.isPressed();
            // boolean selected = buttonModel.isSelected();
            // if ( abstractButton.getName().startsWith( GUIConstants.JBUTTON_ZOOM_COORD ) ) {
            //
            // if ( pressed == true ) {
            // isHorizontalRef = false;
            // jumperDialog = new CoordinateJumperDialog();
            // jumperDialog.getCoordinateJumper().setToolTipText( textFieldModel.getTooltipText() );
            // jumperDialog.addListeners( new ButtonListener() );
            // jumperDialog.setVisible( true );
            // buttonModel.setSelected( true );
            // } else {
            // isHorizontalRef = true;
            // if ( jumperDialog != null ) {
            // jumperDialog.setVisible( false );
            // }
            // buttonModel.setSelected( false );
            // }
            //
            // }
            // if ( abstractButton.getName().startsWith( GUIConstants.JBUTTON_PAN ) ) {
            // System.out.println( "[Controller] jtogglebutton PAN " + buttonPan.getModel().isSelected() );
            // if ( buttonPan.getModel().isSelected() == true ) {
            // buttonPan.setSelected( false );
            // isHorizontalRef = true;
            // System.out.println( "[Controller] jtogglebutton PAN true" );
            // } else {
            // buttonPan.setSelected( true );
            // isHorizontalRef = false;
            // System.out.println( "[Controller] jtogglebutton PAN false" );
            // }
            // // System.out.println( "[Controller] jtogglebutton PAN" );
            // }
            // // AbstractButton abstractButton = (AbstractButton) changeEvent.getSource();
            // // ButtonModel buttonModel = abstractButton.getModel();
            //
            // System.out.println( "Changed: Armed: " + armed + "/Pressed: " + pressed + "/Selected: " + selected );
        }

    }

    /**
     * 
     * Provides functionality to handle user interaction within a JTree.
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    class NavigationTreeSelectionListener implements TreeSelectionListener {

        @Override
        public void valueChanged( TreeSelectionEvent e ) {
            Object source = e.getSource();
            if ( ( (JTree) source ).getName().equals( NavigationPanel.TREE_NAME ) ) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) optionNavPanel.getTree().getLastSelectedPathComponent();

                if ( node == null )
                    // Nothing is selected.
                    return;

                Object nodeInfo = node.getUserObject();
                if ( node.isLeaf() ) {
                    PanelType panelType = null;
                    if ( nodeInfo.equals( OptionDialogModel.GENERAL ) ) {
                        panelType = GenericSettingsPanel.PanelType.GeneralPanel;

                    } else if ( nodeInfo.equals( OptionDialogModel.VIEW ) ) {
                        panelType = GenericSettingsPanel.PanelType.ViewPanel;
                    }

                    switch ( panelType ) {
                    case GeneralPanel:
                        optionSettingPanel = new GeneralPanel( optionSettPanel );
                        ( (GeneralPanel) optionSettingPanel ).addCheckboxListener( new ButtonListener() );
                        ( (GeneralPanel) optionSettingPanel ).setSnappingOnOff( dialogModel.getSnappingOnOff().second );
                        break;
                    case ViewPanel:
                        optionSettingPanel = new ViewPanel();
                        ( (ViewPanel) optionSettingPanel ).setPointSize( dialogModel.getSelectionPointSize().second );
                        ( (ViewPanel) optionSettingPanel ).addRadioButtonListener( new ButtonListener() );

                        break;
                    }
                    optionSettPanel.setCurrentPanel( optionSettingPanel );
                    optionDialog.setSettingsPanel( optionSettPanel );

                } else {
                    optionSettPanel.reset();
                    optionDialog.reset();
                }

            }

        }
    }

    // class Scene2DActionKeyListener implements KeyListener {
    //
    // @Override
    // public void keyPressed( KeyEvent e ) {
    // if ( e.getKeyCode() == 17 ) {
    // char c = e.getKeyChar();
    // boolean isControl = e.isControlDown();
    // int control = KeyEvent.VK_CONTROL;
    //
    // System.out.println( "[Controller] " + c + "  " + isControl + " " + control );
    // }
    // if ( mouseGeoRef.isMouseInside() ) {
    // char c = e.getKeyChar();
    // boolean isControl = e.isControlDown();
    // int control = KeyEvent.VK_CONTROL;
    //
    // System.out.println( "[Controller] " + c + "  " + isControl + " " + control );
    // }
    // if ( mouseFootprint.isMouseInside() ) {
    // char c = e.getKeyChar();
    // boolean isControl = e.isControlDown();
    // int control = KeyEvent.VK_CONTROL;
    //
    // System.out.println( "[Controller] " + c + "  " + isControl + " " + control );
    // }
    // }
    //
    // @Override
    // public void keyReleased( KeyEvent e ) {
    // if ( e.getKeyCode() == 17 ) {
    // char c = e.getKeyChar();
    // boolean isControl = e.isControlDown();
    // int control = KeyEvent.VK_CONTROL;
    //
    // System.out.println( "[Controller] " + c + "  " + isControl + " " + control );
    // }
    // if ( mouseGeoRef.isMouseInside() ) {
    // char c = e.getKeyChar();
    // boolean isControl = e.isControlDown();
    // int control = KeyEvent.VK_CONTROL;
    //
    // System.out.println( "[Controller] " + c + "  " + isControl + " " + control );
    // }
    // if ( mouseFootprint.isMouseInside() ) {
    // char c = e.getKeyChar();
    // boolean isControl = e.isControlDown();
    // int control = KeyEvent.VK_CONTROL;
    //
    // System.out.println( "[Controller] " + c + "  " + isControl + " " + control );
    // }
    //
    // }
    //
    // @Override
    // public void keyTyped( KeyEvent e ) {
    // if ( e.getKeyCode() == 17 ) {
    // char c = e.getKeyChar();
    // boolean isControl = e.isControlDown();
    // int control = KeyEvent.VK_CONTROL;
    //
    // System.out.println( "[Controller] " + c + "  " + isControl + " " + control );
    // }
    // if ( mouseGeoRef.isMouseInside() ) {
    // char c = e.getKeyChar();
    // boolean isControl = e.isControlDown();
    // int control = KeyEvent.VK_CONTROL;
    //
    // System.out.println( "[Controller] " + c + "  " + isControl + " " + control );
    // }
    // if ( mouseFootprint.isMouseInside() ) {
    // char c = e.getKeyChar();
    // boolean isControl = e.isControlDown();
    // int control = KeyEvent.VK_CONTROL;
    //
    // System.out.println( "[Controller] " + c + "  " + isControl + " " + control );
    // }
    // }
    //
    // }

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
        public void mouseEntered( MouseEvent m ) {
            Object source = m.getSource();
            if ( source instanceof JPanel ) {
                // Scene2DPanel
                if ( ( (JPanel) source ).getName().equals( Scene2DPanel.SCENE2D_PANEL_NAME ) ) {

                    mouseGeoRef.setMouseInside( true );

                }
                if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {

                    mouseFootprint.setMouseInside( true );

                }
            }
        }

        @Override
        public void mouseExited( MouseEvent m ) {
            Object source = m.getSource();
            if ( source instanceof JPanel ) {
                // Scene2DPanel
                if ( ( (JPanel) source ).getName().equals( Scene2DPanel.SCENE2D_PANEL_NAME ) ) {
                    mouseGeoRef.setMouseInside( false );

                }
                if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {
                    mouseFootprint.setMouseInside( false );

                }
            }
        }

        @Override
        public void mousePressed( MouseEvent m ) {
            Object source = m.getSource();
            if ( source instanceof JPanel ) {
                // Scene2DPanel
                if ( ( (JPanel) source ).getName().equals( Scene2DPanel.SCENE2D_PANEL_NAME ) ) {
                    mouseGeoRef.setPointMousePressed( new Point2d( m.getX(), m.getY() ) );
                    isControlDown = m.isControlDown();
                    if ( isControlDown ) {
                        System.out.println( "[Controller] down" );
                    }
                }
                if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {
                    mouseFootprint.setPointMousePressed( new Point2d( m.getX(), m.getY() ) );
                    if ( isControlDown ) {
                        System.out.println( "[Controller] down" );
                    }
                }
            }

        }

        @Override
        public void mouseReleased( MouseEvent m ) {
            Object source = m.getSource();
            if ( source instanceof JPanel ) {
                // Scene2DPanel
                if ( ( (JPanel) source ).getName().equals( Scene2DPanel.SCENE2D_PANEL_NAME ) ) {
                    if ( isControlDown ) {
                        Point2d pointPressed = new Point2d( mouseGeoRef.getPointMousePressed().getX(),
                                                            mouseGeoRef.getPointMousePressed().getY() );
                        Point2d pointReleased = new Point2d( m.getX(), m.getY() );
                        Point2d minPoint;
                        Point2d maxPoint;
                        if ( pointPressed.getX() < pointReleased.getX() ) {
                            minPoint = pointPressed;
                            maxPoint = pointReleased;
                        } else {
                            minPoint = pointReleased;
                            maxPoint = pointPressed;
                        }
                        Rectangle r = new Rectangle(
                                                     new Double( minPoint.getX() ).intValue(),
                                                     new Double( minPoint.getY() ).intValue(),
                                                     Math.abs( new Double( maxPoint.getX() - minPoint.getX() ).intValue() ),
                                                     Math.abs( new Double( maxPoint.getY() - minPoint.getY() ).intValue() ) );
                        GeoReferencedPoint center = new GeoReferencedPoint( r.getCenterX(), r.getCenterY() );
                        GeoReferencedPoint dimension = new GeoReferencedPoint( r.getWidth(), r.getHeight() );
                        sceneValues.setCentroidRasterEnvelopePosition( center, dimension );

                        panel.setImageToDraw( model.generateSubImageFromRaster( sceneValues.getSubRaster() ) );
                        panel.updatePoints( sceneValues );
                        panel.setZoomRect( null );
                        panel.repaint();

                    }
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

                        double x = m.getX();
                        double y = m.getY();
                        GeoReferencedPoint geoReferencedPoint = new GeoReferencedPoint( x, y );
                        GeoReferencedPoint g = (GeoReferencedPoint) sceneValues.getWorldPoint( geoReferencedPoint );
                        panel.setLastAbstractPoint( geoReferencedPoint, g );
                        tablePanel.setCoords( panel.getLastAbstractPoint().getWorldCoords() );

                    } else {
                        mouseGeoRef.setMouseChanging( new GeoReferencedPoint(
                                                                              ( mouseGeoRef.getPointMousePressed().getX() - m.getX() ),
                                                                              ( mouseGeoRef.getPointMousePressed().getY() - m.getY() ) ) );

                        sceneValues.moveEnvelope( mouseGeoRef.getMouseChanging() );
                        panel.setImageToDraw( model.generateSubImageFromRaster( sceneValues.getSubRaster() ) );
                        panel.updatePoints( sceneValues );
                    }

                    panel.repaint();

                }
                // footprintPanel
                if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {

                    if ( isControlDown ) {
                        Point2d pointPressed = new Point2d( mouseFootprint.getPointMousePressed().getX(),
                                                            mouseFootprint.getPointMousePressed().getY() );
                        Point2d pointReleased = new Point2d( m.getX(), m.getY() );
                        Point2d minPoint;
                        Point2d maxPoint;
                        if ( pointPressed.getX() < pointReleased.getX() ) {
                            minPoint = pointPressed;
                            maxPoint = pointReleased;
                        } else {
                            minPoint = pointReleased;
                            maxPoint = pointPressed;
                        }
                        Rectangle r = new Rectangle(
                                                     new Double( minPoint.getX() ).intValue(),
                                                     new Double( minPoint.getY() ).intValue(),
                                                     Math.abs( new Double( maxPoint.getX() - minPoint.getX() ).intValue() ),
                                                     Math.abs( new Double( maxPoint.getY() - minPoint.getY() ).intValue() ) );
                        // System.out.println( "[Controller]" + r );
                        FootprintPoint center = new FootprintPoint( r.getCenterX(), r.getCenterY() );
                        FootprintPoint dimension = new FootprintPoint( r.getWidth(), r.getHeight() );
                        sceneValues.setCentroidRasterEnvelopePosition( center, dimension );

                        // footPanel.setImageToDraw( model.generateSubImageFromRaster( sceneValues.getSubRaster() ) );
                        footPanel.setZoomRect( null );
                        footPanel.updatePoints( sceneValues );
                        footPanel.repaint();

                    }
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
                        double x = m.getX();
                        double y = m.getY();
                        Pair<AbstractGRPoint, FootprintPoint> point = null;
                        if ( dialogModel.getSnappingOnOff().first ) {
                            point = footPanel.getClosestPoint( new FootprintPoint( x, y ) );
                        } else {
                            point = new Pair<AbstractGRPoint, FootprintPoint>(
                                                                               new FootprintPoint( x, y ),
                                                                               (FootprintPoint) sceneValues.getWorldPoint( new FootprintPoint(
                                                                                                                                               x,
                                                                                                                                               y ) ) );
                        }

                        footPanel.setLastAbstractPoint( point.first, point.second );
                        tablePanel.setCoords( footPanel.getLastAbstractPoint().getWorldCoords() );

                    } else {
                        mouseFootprint.setMouseChanging( new FootprintPoint(
                                                                             ( mouseFootprint.getPointMousePressed().getX() - m.getX() ),
                                                                             ( mouseFootprint.getPointMousePressed().getY() - m.getY() ) ) );

                        sceneValues.moveEnvelope( mouseFootprint.getMouseChanging() );
                        footPanel.updatePoints( sceneValues );
                    }
                    footPanel.repaint();

                }
            }
        }

    }

    /**
     * Sets values to the JTableModel.
     */
    private void setValues() {

        footPanel.addToSelectedPoints( footPanel.getLastAbstractPoint() );
        panel.addToSelectedPoints( panel.getLastAbstractPoint() );
        addToMappedPoints( footPanel.getLastAbstractPoint(), panel.getLastAbstractPoint() );
        footPanel.setLastAbstractPoint( null, null );
        panel.setLastAbstractPoint( null, null );

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
     * Registeres the mouseMotion in the component.
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    class Scene2DMouseMotionListener implements MouseMotionListener {

        @Override
        public void mouseDragged( MouseEvent m ) {

            Object source = m.getSource();

            if ( source instanceof JPanel ) {
                // Scene2DPanel
                if ( ( (JPanel) source ).getName().equals( Scene2DPanel.SCENE2D_PANEL_NAME ) ) {
                    if ( m.isControlDown() ) {
                        int x = new Double( mouseGeoRef.getPointMousePressed().getX() ).intValue();
                        int y = new Double( mouseGeoRef.getPointMousePressed().getY() ).intValue();
                        int width = new Double( m.getX() - mouseGeoRef.getPointMousePressed().getX() ).intValue();
                        int height = new Double( m.getY() - mouseGeoRef.getPointMousePressed().getY() ).intValue();
                        Rectangle rec = new Rectangle( x, y, width, height );
                        panel.setZoomRect( rec );
                        panel.repaint();
                    }
                }
                // footprintPanel
                if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {
                    if ( m.isControlDown() ) {
                        int x = new Double( mouseFootprint.getPointMousePressed().getX() ).intValue();
                        int y = new Double( mouseFootprint.getPointMousePressed().getY() ).intValue();
                        int width = new Double( m.getX() - mouseFootprint.getPointMousePressed().getX() ).intValue();
                        int height = new Double( m.getY() - mouseFootprint.getPointMousePressed().getY() ).intValue();
                        Rectangle rec = new Rectangle( x, y, width, height );
                        footPanel.setZoomRect( rec );
                        footPanel.repaint();
                    }
                }
            }

        }

        @Override
        public void mouseMoved( MouseEvent m ) {

            Object source = m.getSource();

            if ( source instanceof JPanel ) {
                // Scene2DPanel
                if ( ( (JPanel) source ).getName().equals( Scene2DPanel.SCENE2D_PANEL_NAME ) ) {
                    // System.out.println( m.getPoint() );
                    if ( mouseGeoRef != null ) {
                        mouseGeoRef.setMouseMoved( new GeoReferencedPoint( m.getX(), m.getY() ) );
                    }
                }
                // footprintPanel
                if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {
                    // System.out.println( m.getPoint() );
                    if ( mouseFootprint != null ) {
                        mouseFootprint.setMouseMoved( new FootprintPoint( m.getX(), m.getY() ) );
                    }
                }
            }
        }

    }

    /**
     * 
     * Represents the zoom function.
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    class Scene2DMouseWheelListener implements MouseWheelListener {
        private boolean zoomIn = false;

        private float resizing;

        private AbstractGRPoint mouseOver;

        @Override
        public void mouseWheelMoved( MouseWheelEvent m ) {

            Object source = m.getSource();

            if ( source instanceof JPanel ) {
                // Scene2DPanel
                if ( ( (JPanel) source ).getName().equals( Scene2DPanel.SCENE2D_PANEL_NAME ) ) {
                    mouseOver = mouseGeoRef.getMouseMoved();
                    resizing = .05f;
                    if ( m.getWheelRotation() < 0 ) {
                        zoomIn = true;
                    } else {
                        zoomIn = false;
                    }
                    sceneValues.computeZoomedEnvelope( zoomIn, resizing, mouseOver );
                    panel.setImageToDraw( model.generateSubImageFromRaster( sceneValues.getSubRaster() ) );
                    panel.updatePoints( sceneValues );
                    panel.repaint();
                }
                // footprintPanel
                if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {

                    resizing = .1f;
                    mouseOver = mouseFootprint.getMouseMoved();
                    if ( m.getWheelRotation() < 0 ) {
                        zoomIn = true;
                    } else {
                        zoomIn = false;
                    }
                    sceneValues.computeZoomedEnvelope( zoomIn, resizing, mouseOver );
                    footPanel.updatePoints( sceneValues );
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
            Object source = c.getSource();

            if ( source instanceof JFrame ) {
                if ( model.getGeneratedImage() != null ) {
                    init();

                }
            }

        }

        @Override
        public void componentShown( ComponentEvent arg0 ) {
            // TODO Auto-generated method stub

        }

    }

    /**
     * Initializes the computing and the painting of the georeferenced map.
     */
    private void init() {

        sceneValues.setImageDimension( new Rectangle( panel.getBounds().width, panel.getBounds().height ) );
        sceneValues.setDimenstionFootpanel( new Rectangle( footPanel.getBounds().width, footPanel.getBounds().height ) );
        panel.setImageDimension( sceneValues.getImageDimension() );
        footPanel.updatePoints( sceneValues );
        // TODO make a modularization in Scene2DValues because there is a strict sequence in this case. If there is the
        // update of the points before the new envelope is computed the polygons will be drawn on the wrong position
        panel.setImageToDraw( model.generateSubImage( sceneValues.getImageDimension() ) );
        panel.updatePoints( sceneValues );
        panel.repaint();
        footPanel.repaint();
    }

    /**
     * Adds the <Code>AbstractPoint</Code>s to a map, if specified.
     * 
     * @param mappedPointKey
     * @param mappedPointValue
     */
    private void addToMappedPoints( Point4Values mappedPointKey, Point4Values mappedPointValue ) {
        if ( mappedPointKey != null && mappedPointValue != null ) {
            this.mappedPoints.add( new Pair<Point4Values, Point4Values>( mappedPointKey, mappedPointValue ) );
        }

    }

    /**
     * Removes sample points in panels and the table.
     * 
     * @param pointFromTable
     *            that should be removed, could be <Code>null</Code>
     */
    private void removeFromMappedPoints( Pair<Point4Values, Point4Values> pointFromTable ) {
        if ( pointFromTable != null ) {
            mappedPoints.remove( pointFromTable );
        }
    }

    /**
     * Removes everything after a complete deletion of the points.
     */
    private void removeAllFromMappedPoints() {
        mappedPoints = new ArrayList<Pair<Point4Values, Point4Values>>();
        tablePanel.removeAllRows();
        panel.removeAllFromSelectedPoints();
        footPanel.removeAllFromSelectedPoints();
        footPanel.setLastAbstractPoint( null, null );
        panel.setPolygonList( null, null );
        panel.setLastAbstractPoint( null, null );
        panel.repaint();
        footPanel.repaint();
        reset();

    }

    /**
     * Resets the focus of the panels and the startPanel.
     */
    private void reset() {
        panel.setFocus( false );
        footPanel.setFocus( false );
        start = false;

    }

}
