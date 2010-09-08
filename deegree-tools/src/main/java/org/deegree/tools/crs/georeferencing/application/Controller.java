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

import static java.lang.Math.max;
import static org.deegree.tools.crs.georeferencing.communication.GUIConstants.JTEXTFIELD_COORDINATE_JUMPER;
import static org.deegree.tools.crs.georeferencing.communication.GUIConstants.MENUITEM_TRANS_HELMERT;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.vecmath.Point2d;

import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.Triple;
import org.deegree.cs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.Ring;
import org.deegree.rendering.r3d.model.geometry.GeometryQualityModel;
import org.deegree.rendering.r3d.model.geometry.SimpleAccessGeometry;
import org.deegree.rendering.r3d.opengl.display.OpenGLEventHandler;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.WorldRenderableObject;
import org.deegree.tools.crs.georeferencing.application.handler.FileInputHandler;
import org.deegree.tools.crs.georeferencing.application.handler.FileOutputHandler;
import org.deegree.tools.crs.georeferencing.application.handler.JCheckboxHandler;
import org.deegree.tools.crs.georeferencing.application.transformation.AffineTransformation;
import org.deegree.tools.crs.georeferencing.application.transformation.Helmert4Transform;
import org.deegree.tools.crs.georeferencing.application.transformation.Polynomial;
import org.deegree.tools.crs.georeferencing.application.transformation.TransformationMethod;
import org.deegree.tools.crs.georeferencing.application.transformation.TransformationMethod.TransformationType;
import org.deegree.tools.crs.georeferencing.communication.FileChooser;
import org.deegree.tools.crs.georeferencing.communication.GRViewerGUI;
import org.deegree.tools.crs.georeferencing.communication.GUIConstants;
import org.deegree.tools.crs.georeferencing.communication.PointTableFrame;
import org.deegree.tools.crs.georeferencing.communication.checkboxlist.CheckboxListTransformation;
import org.deegree.tools.crs.georeferencing.communication.dialog.ButtonPanel;
import org.deegree.tools.crs.georeferencing.communication.dialog.coordinatejump.CoordinateJumperTextfieldDialog;
import org.deegree.tools.crs.georeferencing.communication.dialog.error.ErrorDialog;
import org.deegree.tools.crs.georeferencing.communication.dialog.menuitem.OpenWMS;
import org.deegree.tools.crs.georeferencing.communication.dialog.menuitem.WMSParameterChooser;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.GeneralPanel;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.GenericSettingsPanel;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.NavigationPanel;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.OptionDialog;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.SettingsPanel;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.ViewPanel;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.GenericSettingsPanel.PanelType;
import org.deegree.tools.crs.georeferencing.communication.panel2D.AbstractPanel2D;
import org.deegree.tools.crs.georeferencing.communication.panel2D.BuildingFootprintPanel;
import org.deegree.tools.crs.georeferencing.communication.panel2D.Scene2DPanel;
import org.deegree.tools.crs.georeferencing.model.CheckBoxListModel;
import org.deegree.tools.crs.georeferencing.model.ControllerModel;
import org.deegree.tools.crs.georeferencing.model.Footprint;
import org.deegree.tools.crs.georeferencing.model.RowColumn;
import org.deegree.tools.crs.georeferencing.model.Scene2D;
import org.deegree.tools.crs.georeferencing.model.Scene2DImplShape;
import org.deegree.tools.crs.georeferencing.model.Scene2DImplWMS;
import org.deegree.tools.crs.georeferencing.model.datatransformer.VectorTransformer;
import org.deegree.tools.crs.georeferencing.model.dialog.OptionDialogModel;
import org.deegree.tools.crs.georeferencing.model.exceptions.NumberException;
import org.deegree.tools.crs.georeferencing.model.mouse.FootprintMouseModel;
import org.deegree.tools.crs.georeferencing.model.mouse.GeoReferencedMouseModel;
import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint;
import org.deegree.tools.crs.georeferencing.model.points.FootprintPoint;
import org.deegree.tools.crs.georeferencing.model.points.GeoReferencedPoint;
import org.deegree.tools.crs.georeferencing.model.points.Point4Values;
import org.deegree.tools.crs.georeferencing.model.points.PointResidual;
import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint.PointType;
import org.deegree.tools.crs.georeferencing.model.textfield.AbstractCoordinateJumperModel;
import org.deegree.tools.crs.georeferencing.model.textfield.CoordinateJumperModel;
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

    private Scene2D model;

    private Scene2DValues sceneValues;

    private PointTableFrame tablePanel;

    private ParameterStore store;

    private Footprint footPrint;

    private CoordinateJumperModel textFieldModel;

    private OpenGLEventHandler glHandler;

    private GeoReferencedMouseModel mouseGeoRef;

    private FootprintMouseModel mouseFootprint;

    private Point2d changePoint;

    private boolean isHorizontalRefGeoref, isHorizontalRefFoot, start, isControlDown, selectedGeoref, selectedFoot;

    private boolean isZoomInGeoref, isZoomInFoot, isZoomOutGeoref, isZoomOutFoot;

    private boolean isInitGeoref, isInitFoot;

    private GeometryFactory geom;

    private CRS sourceCRS, targetCRS;

    private List<Triple<Point4Values, Point4Values, PointResidual>> mappedPoints;

    private ControllerModel conModel;

    private NavigationPanel optionNavPanel;

    public int order;

    private SettingsPanel optionSettPanel;

    private OptionDialog optionDialog;

    // private CoordinateJumperSpinnerDialog jumperDialog;
    private CoordinateJumperTextfieldDialog jumperDialog;

    private OpenWMS wmsStartDialog;

    private WMSParameterChooser wmsParameter;

    private GenericSettingsPanel optionSettingPanel;

    private JToggleButton buttonPanGeoref, buttonPanFoot, buttonZoomInGeoref, buttonZoominFoot, buttonZoomoutGeoref,
                            buttonZoomoutFoot, buttonCoord;

    private ButtonModel buttonModel;

    private CheckboxListTransformation checkBoxListTransform;

    private CheckBoxListModel modelTransformation;

    private RowColumn rc;

    public Controller( GRViewerGUI view ) {

        geom = new GeometryFactory();
        sceneValues = new Scene2DValues( geom );
        conModel = new ControllerModel( view, view.getFootprintPanel(), view.getScenePanel2D(), new OptionDialogModel() );

        this.start = false;

        this.glHandler = view.getOpenGLEventListener();
        this.textFieldModel = new CoordinateJumperModel();
        AbstractPanel2D.selectedPointSize = conModel.getDialogModel().getSelectionPointSize().first;
        AbstractPanel2D.zoomValue = conModel.getDialogModel().getResizeValue().first;

        this.mappedPoints = new ArrayList<Triple<Point4Values, Point4Values, PointResidual>>();

        view.addListeners( new ButtonListener() );
        view.addHoleWindowListener( new HoleWindowListener() );

        initToggleButtons();

        // init the Checkboxlist for Transformation
        modelTransformation = new CheckBoxListModel();
        checkBoxListTransform = new CheckboxListTransformation( modelTransformation );
        view.addToMenuTransformation( checkBoxListTransform );
        checkBoxListTransform.addCheckboxListener( new ButtonListener() );

        // init the transformation method
        this.tablePanel = new PointTableFrame();
        this.tablePanel.getSaveButton().setEnabled( false );
        this.tablePanel.getLoadButton().setEnabled( false );
        this.tablePanel.addTableModelListener( new TableChangedEventListener() );
        this.tablePanel.addActionButtonListener( new ButtonListener() );
        // transform = null;
        if ( conModel.getTransformationType() == null ) {
            order = 1;
            for ( JCheckBox box : modelTransformation.getList() ) {
                if ( ( box ).getText().startsWith( MENUITEM_TRANS_HELMERT ) ) {
                    conModel.setTransformationType( TransformationType.Helmert_4 );
                    view.activateTransformationCheckbox( box );
                    break;
                }
            }

        }

        isHorizontalRefGeoref = true;
        isHorizontalRefFoot = true;

    }

    /**
     * Initializes the navigation buttons that are registered for each map.
     */
    private void initToggleButtons() {
        buttonPanGeoref = conModel.getView().getNavigationPanelGeoref().getButtonPan();
        buttonPanFoot = conModel.getView().getNaviPanelFoot().getButtonPan();
        buttonZoomInGeoref = conModel.getView().getNavigationPanelGeoref().getButtonZoomIn();
        buttonZoominFoot = conModel.getView().getNaviPanelFoot().getButtonZoomIn();
        buttonZoomoutGeoref = conModel.getView().getNavigationPanelGeoref().getButtonZoomOut();
        buttonZoomoutFoot = conModel.getView().getNaviPanelFoot().getButtonZoomOut();
        buttonCoord = conModel.getView().getNavigationPanelGeoref().getButtonZoomCoord();
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
        selectedGeoref = buttonModel.isSelected();
        if ( selectedGeoref == false ) {
            isHorizontalRefGeoref = true;
        } else {
            checkSelected = true;
            buttonPanGeoref.setSelected( false );
            buttonZoomInGeoref.setSelected( false );
            buttonZoomoutGeoref.setSelected( false );
            buttonCoord.setSelected( false );
            isHorizontalRefGeoref = false;
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
                // jumperDialog = new CoordinateJumperSpinnerDialog( view );
                jumperDialog = new CoordinateJumperTextfieldDialog( conModel.getView() );
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

        boolean checkSelected = false;
        buttonModel = t.getModel();
        selectedFoot = buttonModel.isSelected();
        if ( selectedFoot == false ) {
            isHorizontalRefFoot = true;
        } else {
            checkSelected = true;
            buttonPanFoot.setSelected( false );
            buttonZoominFoot.setSelected( false );
            buttonZoomoutFoot.setSelected( false );
            isHorizontalRefFoot = false;
        }
        if ( t == buttonPanFoot ) {
            buttonPanFoot.setSelected( checkSelected );

        } else if ( t == buttonZoominFoot ) {
            buttonZoominFoot.setSelected( checkSelected );

        } else if ( t == buttonZoomoutFoot ) {
            buttonZoomoutFoot.setSelected( checkSelected );

        }

    }

    /**
     * Initializes the footprint scene.
     */
    private void initFootprintScene( String filePath ) {
        isInitFoot = true;
        if ( isInitGeoref ) {

            tablePanel.getSaveButton().setEnabled( true );
            tablePanel.getLoadButton().setEnabled( true );

        }

        this.footPrint = new Footprint( sceneValues, geom );
        conModel.getFootPanel().addScene2DMouseListener( new Scene2DMouseListener() );
        conModel.getFootPanel().addScene2DMouseMotionListener( new Scene2DMouseMotionListener() );
        conModel.getFootPanel().addScene2DMouseWheelListener( new Scene2DMouseWheelListener() );

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

    /**
     * Initializes the georeferenced scene.
     */
    private void initGeoReferencingScene( Scene2D scene2d ) {
        isInitGeoref = true;
        if ( isInitFoot ) {

            tablePanel.getSaveButton().setEnabled( true );
            tablePanel.getLoadButton().setEnabled( true );

        }

        mouseGeoRef = new GeoReferencedMouseModel();
        scene2d.init( sceneValues );
        init();

        conModel.getPanel().addScene2DMouseListener( new Scene2DMouseListener() );
        conModel.getPanel().addScene2DMouseMotionListener( new Scene2DMouseMotionListener() );
        conModel.getPanel().addScene2DMouseWheelListener( new Scene2DMouseWheelListener() );
    }

    /**
     * Updates the panels that are responsible for drawing the georeferenced points so that the once clicked points are
     * drawn into the right position.
     */
    private void updateDrawingPanels() {
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
     * 
     * Controls the ActionListener
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    class ButtonListener implements ActionListener {
        private boolean isRunIntoTrouble = false;

        @Override
        public void actionPerformed( ActionEvent e ) {
            Object source = e.getSource();
            if ( source instanceof JTextField ) {
                JTextField tF = (JTextField) source;
                if ( tF.getName().startsWith( JTEXTFIELD_COORDINATE_JUMPER ) ) {

                    fireTextfieldJumperDialog();

                }

            } else if ( source instanceof JToggleButton ) {
                if ( source instanceof JRadioButton ) {
                    if ( ( (JRadioButton) source ).getText().startsWith( ViewPanel.TWO ) ) {

                        conModel.getDialogModel().setSelectionPointSize( 2 );
                    } else if ( ( (JRadioButton) source ).getText().startsWith( ViewPanel.THREE ) ) {

                        conModel.getDialogModel().setSelectionPointSize( 3 );
                    } else if ( ( (JRadioButton) source ).getText().startsWith( ViewPanel.DEFAULT ) ) {

                        conModel.getDialogModel().setSelectionPointSize( 5 );

                    } else if ( ( (JRadioButton) source ).getText().startsWith( ViewPanel.SEVEN ) ) {

                        conModel.getDialogModel().setSelectionPointSize( 7 );
                    } else if ( ( (JRadioButton) source ).getText().startsWith( ViewPanel.TEN ) ) {

                        conModel.getDialogModel().setSelectionPointSize( 10 );
                    } else if ( ( (JRadioButton) source ).getText().startsWith( ViewPanel.CUSTOM ) ) {
                        if ( !( (ViewPanel) optionSettingPanel ).getTextFieldCustom().getText().equals( "" ) ) {
                            conModel.getDialogModel().setTextFieldKeyString(
                                                                             ( (ViewPanel) optionSettingPanel ).getTextFieldCustom().getText() );
                            int i;
                            try {
                                i = Integer.parseInt( conModel.getDialogModel().getTextFieldKeyString().second );
                                conModel.getDialogModel().setSelectionPointSize( i );
                            } catch ( NumberFormatException ex ) {
                                new ErrorDialog( optionDialog, JDialog.ERROR, "This is not a number" );
                            }

                        }
                    }

                } else if ( source instanceof JCheckBox ) {
                    JCheckBox selectedCheckbox = (JCheckBox) source;

                    new JCheckboxHandler( selectedCheckbox, conModel );

                    for ( String s : wmsParameter.getCheckBoxListLayerText() ) {

                        wmsParameter.fillSRSList( s );

                    }

                } else {
                    JToggleButton tb = (JToggleButton) source;

                    if ( tb.getName().startsWith( GUIConstants.JBUTTON_PAN ) ) {

                        if ( tb == buttonPanGeoref ) {
                            selectGeorefToggleButton( tb );
                            isZoomInGeoref = false;
                            isZoomOutGeoref = false;
                        } else {
                            selectFootprintToggleButton( tb );
                            isZoomInFoot = false;
                            isZoomOutFoot = false;
                        }
                    } else if ( tb.getName().startsWith( GUIConstants.JBUTTON_ZOOM_COORD ) ) {

                        if ( tb == buttonCoord ) {
                            selectGeorefToggleButton( tb );
                        } else {
                            selectFootprintToggleButton( tb );
                        }
                    } else if ( tb.getName().startsWith( GUIConstants.JBUTTON_ZOOM_IN ) ) {

                        if ( tb == buttonZoomInGeoref ) {
                            selectGeorefToggleButton( tb );
                            isZoomInGeoref = true;
                            isZoomOutGeoref = false;
                        } else {
                            selectFootprintToggleButton( tb );
                            isZoomInFoot = true;
                            isZoomOutFoot = false;
                        }
                    } else if ( tb.getName().startsWith( GUIConstants.JBUTTON_ZOOM_OUT ) ) {

                        if ( tb == buttonZoomoutGeoref ) {
                            selectGeorefToggleButton( tb );
                            isZoomInGeoref = false;
                            isZoomOutGeoref = true;
                        } else {
                            selectFootprintToggleButton( tb );
                            isZoomInFoot = true;
                            isZoomOutFoot = false;
                        }
                    }
                }
            } else if ( source instanceof JButton ) {

                if ( ( (JButton) source ).getText().startsWith( PointTableFrame.BUTTON_DELETE_SELECTED ) ) {
                    int[] tableRows = tablePanel.getTable().getSelectedRows();
                    int[] deleteableRows = new int[tableRows.length];
                    int i = 0;
                    for ( int tableRow : tableRows ) {
                        boolean contained = false;

                        for ( Triple<Point4Values, Point4Values, PointResidual> p : mappedPoints ) {
                            System.out.println( "[Controller] beforeRemoving: " + p.second + "\n" );
                            if ( p.first.getRc().getRow() == tableRow || p.second.getRc().getRow() == tableRow ) {

                                contained = true;
                                deleteableRows[i++] = tableRow;

                                System.out.println( "[Controller] afterRemoving: " + p.second + "\n\n" );
                                break;
                            }

                        }
                        if ( contained == false ) {

                            conModel.getFootPanel().setLastAbstractPoint( null, null, null );
                            conModel.getPanel().setLastAbstractPoint( null, null, null );
                        }
                    }
                    removeFromMappedPoints( tableRows );
                    updateResidualsWithLastAbstractPoint();
                    updateDrawingPanels();
                } else if ( ( (JButton) source ).getText().startsWith( PointTableFrame.LOAD_POINTTABLE ) ) {

                    FileInputHandler in = new FileInputHandler( tablePanel );
                    if ( in.getData() != null ) {
                        VectorTransformer vt = new VectorTransformer( in.getData(), sceneValues );
                        mappedPoints.clear();
                        mappedPoints.addAll( vt.getMappedPoints() );
                        updateDrawingPanels();
                    }

                } else if ( ( (JButton) source ).getText().startsWith( PointTableFrame.SAVE_POINTTABLE ) ) {

                    new FileOutputHandler( tablePanel );

                } else if ( ( (JButton) source ).getText().startsWith( PointTableFrame.BUTTON_DELETE_ALL ) ) {
                    removeAllFromMappedPoints();

                } else if ( ( (JButton) source ).getText().startsWith( GUIConstants.COMPUTE_BUTTON_TEXT ) ) {
                    // swap the tempPoints into the map now
                    if ( conModel.getFootPanel().getLastAbstractPoint() != null
                         && conModel.getPanel().getLastAbstractPoint() != null ) {
                        setValues();
                    }

                    conModel.setTransform( determineTransformationType( conModel.getTransformationType() ) );
                    List<Ring> polygonRing = conModel.getTransform().computeRingList();

                    updateResiduals( conModel.getTransformationType() );

                    conModel.getPanel().setPolygonList( polygonRing, sceneValues );

                    conModel.getPanel().repaint();

                    reset();
                } else if ( ( (JButton) source ).getText().startsWith( ButtonPanel.BUTTON_TEXT_CANCEL ) ) {
                    if ( optionDialog != null && optionDialog.isVisible() == true ) {
                        conModel.getDialogModel().transferOldToNew();
                        AbstractPanel2D.selectedPointSize = conModel.getDialogModel().getSelectionPointSize().first;
                        conModel.getPanel().repaint();
                        conModel.getFootPanel().repaint();
                        optionDialog.setVisible( false );
                    } else if ( jumperDialog != null && jumperDialog.isVisible() == true ) {
                        jumperDialog.setVisible( false );

                        selectedGeoref = false;
                        buttonModel.setSelected( false );
                        isHorizontalRefGeoref = true;

                    } else if ( wmsStartDialog != null && wmsStartDialog.isVisible() == true ) {
                        wmsStartDialog.setVisible( false );

                    } else if ( wmsParameter != null && wmsParameter.isVisible() == true ) {
                        wmsParameter.setVisible( false );
                        wmsStartDialog.setVisible( true );
                    }

                } else if ( ( (JButton) source ).getText().startsWith( ButtonPanel.BUTTON_TEXT_OK ) ) {
                    if ( optionDialog != null && optionDialog.isVisible() == true ) {

                        if ( optionSettingPanel != null ) {

                            if ( optionSettingPanel instanceof ViewPanel ) {
                                // if the custom radiobutton is selected and there is something inside the textField
                                if ( !( (ViewPanel) optionSettingPanel ).getTextFieldCustom().getText().equals( "" )
                                     && ( (ViewPanel) optionSettingPanel ).getRadioCustom().getSelectedObjects() != null ) {
                                    // here you have to check about the input for the custom textfield. Keylistener for
                                    // the textfield while typing in is problematic because you can workaround with
                                    // copy&paste...so this should be the way to go.

                                    String textInput = ( (ViewPanel) optionSettingPanel ).getTextFieldCustom().getText();
                                    if ( AbstractCoordinateJumperModel.validateInt( textInput ) ) {
                                        conModel.getDialogModel().setTextFieldKeyString( textInput );
                                        conModel.getDialogModel().setSelectionPointSize(
                                                                                         Integer.parseInt( conModel.getDialogModel().getTextFieldKeyString().second ) );
                                        isRunIntoTrouble = false;
                                    } else {
                                        new ErrorDialog( optionDialog, JDialog.ERROR,
                                                         "Insert numbers only into the textField!" );
                                        isRunIntoTrouble = true;
                                    }

                                }
                            } else if ( optionSettingPanel instanceof GeneralPanel ) {
                                String p = ( (GeneralPanel) optionSettingPanel ).getTextField(
                                                                                               ( (GeneralPanel) optionSettingPanel ).getZoomValue() ).getText();
                                String p1 = p.replace( ',', '.' );
                                conModel.getDialogModel().setResizeValue( new Double( p1 ).doubleValue() );
                                isRunIntoTrouble = false;
                            }
                        }
                        if ( isRunIntoTrouble == false ) {
                            conModel.getDialogModel().transferNewToOld();
                            AbstractPanel2D.selectedPointSize = conModel.getDialogModel().getSelectionPointSize().first;
                            conModel.getPanel().repaint();
                            conModel.getFootPanel().repaint();
                            optionDialog.setVisible( false );
                        }
                    } else if ( jumperDialog != null && jumperDialog.isVisible() == true ) {

                        fireTextfieldJumperDialog();
                    } else if ( wmsStartDialog != null && wmsStartDialog.isVisible() == true ) {
                        String mapURLString = wmsStartDialog.getTextField().getText();
                        wmsStartDialog.setVisible( false );
                        try {
                            wmsParameter = new WMSParameterChooser( wmsStartDialog, mapURLString );
                            wmsParameter.addCheckBoxListener( new ButtonListener() );
                        } catch ( MalformedURLException e1 ) {
                            new ErrorDialog( wmsStartDialog, JDialog.ERROR,
                                             "The requested URL is malformed! There is no response gotten from the server. " );
                            isRunIntoTrouble = true;
                        } catch ( NullPointerException e2 ) {
                            new ErrorDialog( wmsStartDialog, JDialog.ERROR,
                                             "The requested URL is malformed! There is no response gotten from the server. " );
                            isRunIntoTrouble = true;
                        }
                        if ( isRunIntoTrouble == false ) {
                            wmsParameter.addListeners( new ButtonListener() );
                            wmsParameter.setVisible( true );
                        }

                    } else if ( wmsParameter != null && wmsParameter.isVisible() == true ) {

                        URL mapURL = wmsParameter.getMapURL();
                        CRS crs = wmsParameter.getCheckBoxSRS();
                        String layers = wmsParameter.getCheckBoxListAsString().toString();
                        List<String> layerList = wmsParameter.getCheckBoxListLayerText();
                        String format = wmsParameter.getCheckBoxFormatAsString().toString();

                        if ( layers == null || layers.length() == 0 ) {
                            new ErrorDialog( wmsParameter, JDialog.ERROR,
                                             "There is no Layer selected. Please selected at least one. " );
                        } else if ( format == null || format.equals( "" ) ) {
                            new ErrorDialog( wmsParameter, JDialog.ERROR, "There is no format selected. " );
                        } else if ( crs == null ) {
                            new ErrorDialog( wmsParameter, JDialog.ERROR, "There is no CRS selected. " );
                        } else {
                            Envelope env = wmsParameter.getEnvelope( crs, layerList );
                            if ( env != null ) {
                                int qor = max( conModel.getPanel().getWidth(), conModel.getPanel().getHeight() );
                                store = new ParameterStore( mapURL, crs, format, layers, env, qor );
                                model = new Scene2DImplWMS( store, wmsParameter.getWmsClient() );
                                initGeoReferencingScene( model );
                                wmsParameter.setVisible( false );
                            } else {
                                new ErrorDialog( wmsParameter, JDialog.ERROR, "There is no Envelope for this request. " );
                            }
                        }

                    }
                }
            } else if ( source instanceof JMenuItem ) {

                if ( ( (JMenuItem) source ).getText().startsWith( GUIConstants.MENUITEM_EDIT_OPTIONS ) ) {
                    DefaultMutableTreeNode root = new DefaultMutableTreeNode( "Options" );

                    conModel.getDialogModel().createNodes( root );
                    optionDialog = new OptionDialog( conModel.getView(), root );
                    optionDialog.getButtonPanel().addListeners( new ButtonListener() );
                    optionNavPanel = optionDialog.getNavigationPanel();
                    optionSettPanel = optionDialog.getSettingsPanel();

                    // add the listener to the navigation panel
                    optionNavPanel.addTreeListener( new NavigationTreeSelectionListener() );

                    optionDialog.setVisible( true );

                } else if ( ( (JMenuItem) source ).getText().startsWith( GUIConstants.MENUITEM_OPEN_BUILDING ) ) {
                    List<String> list = new ArrayList<String>();
                    list.add( "gml" );
                    list.add( "xml" );
                    String desc = "(*.gml, *.xml) GML or CityGML-Files";
                    Pair<List<String>, String> supportedFiles = new Pair<List<String>, String>( list, desc );
                    List<Pair<List<String>, String>> supportedOpenFiles = new ArrayList<Pair<List<String>, String>>();
                    supportedOpenFiles.add( supportedFiles );
                    FileChooser fileChooser = new FileChooser( supportedOpenFiles, conModel.getView() );
                    String fileChoosed = fileChooser.getSelectedFilePath();
                    if ( fileChoosed != null ) {
                        initFootprintScene( fileChoosed );
                    }

                } else if ( ( (JMenuItem) source ).getText().startsWith( GUIConstants.MENUITEM_OPEN_SHAPEFILE ) ) {
                    List<String> list = new ArrayList<String>();
                    list.add( "shp" );

                    String desc = "(*.shp) Esri ShapeFiles";
                    Pair<List<String>, String> supportedFiles = new Pair<List<String>, String>( list, desc );
                    List<Pair<List<String>, String>> supportedOpenFiles = new ArrayList<Pair<List<String>, String>>();
                    supportedOpenFiles.add( supportedFiles );
                    FileChooser fileChooser = new FileChooser( supportedOpenFiles, conModel.getView() );
                    String fileChoosed = fileChooser.getSelectedFilePath();
                    if ( fileChoosed != null ) {
                        model = new Scene2DImplShape( fileChoosed, conModel.getPanel().getG2() );
                        initGeoReferencingScene( model );
                    }

                } else if ( ( (JMenuItem) source ).getText().startsWith( GUIConstants.MENUITEM_OPEN_WMS_LAYER ) ) {

                    wmsStartDialog = new OpenWMS( conModel.getView() );
                    wmsStartDialog.addListeners( new ButtonListener() );
                    wmsStartDialog.setVisible( true );

                }

            }

        }

        private void fireTextfieldJumperDialog() {
            try {
                textFieldModel.setTextInput( jumperDialog.getCoordinateJumper().getText() );

                System.out.println( textFieldModel.toString() );
                if ( textFieldModel.getSpanX() != -1 && textFieldModel.getSpanY() != -1 ) {

                    sceneValues.setCentroidWorldEnvelopePosition( textFieldModel.getxCoordinate(),
                                                                  textFieldModel.getyCoordiante(),
                                                                  textFieldModel.getSpanX(), textFieldModel.getSpanY(),
                                                                  PointType.GeoreferencedPoint );

                } else {
                    sceneValues.setCentroidWorldEnvelopePosition( textFieldModel.getxCoordinate(),
                                                                  textFieldModel.getyCoordiante(),
                                                                  PointType.GeoreferencedPoint );

                }
                jumperDialog.setVisible( false );
                selectedGeoref = false;
                buttonModel.setSelected( false );
                isHorizontalRefGeoref = true;
                conModel.getPanel().setImageToDraw( model.generateSubImageFromRaster( sceneValues.getEnvelopeGeoref() ) );
                conModel.getPanel().updatePoints( sceneValues );
                conModel.getPanel().repaint();

            } catch ( NumberException e1 ) {

                new ErrorDialog( conModel.getView(), JDialog.ERROR, e1.getMessage() );
            }

        }
    }

    /**
     * 
     * Controls if there is a change inside the table
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    class TableChangedEventListener implements TableModelListener {

        @Override
        public void tableChanged( TableModelEvent e ) {

            int row = e.getFirstRow();
            int column = e.getColumn();
            TableModel model = (TableModel) e.getSource();

            if ( column != -1 && model.getValueAt( row, column ) != null ) {
                String columnName = model.getColumnName( column );
                Object data = model.getValueAt( row, column );

                System.out.println( "[Controller] TableEvent row: " + row + " col: " + column + " colName: "
                                    + columnName + " data: " + data );
                for ( Triple<Point4Values, Point4Values, PointResidual> p : mappedPoints ) {
                    boolean changed = changePointLocation( p, data, row, column );
                    if ( changed ) {

                        updateDrawingPanels();

                    }
                }

                if ( row == e.getLastRow() ) {
                    Triple<Point4Values, Point4Values, PointResidual> newLastPair = new Triple<Point4Values, Point4Values, PointResidual>(
                                                                                                                                           conModel.getFootPanel().getLastAbstractPoint(),
                                                                                                                                           conModel.getPanel().getLastAbstractPoint(),
                                                                                                                                           null );
                    if ( conModel.getFootPanel().getLastAbstractPoint() != null
                         && conModel.getPanel().getLastAbstractPoint() != null ) {
                        boolean changed = changePointLocation( newLastPair, data, row, column );
                        if ( changed ) {

                            if ( conModel.getFootPanel().getLastAbstractPoint() != null
                                 && conModel.getPanel().getLastAbstractPoint() != null ) {
                                conModel.getPanel().setLastAbstractPoint( newLastPair.second.getNewValue(),
                                                                          newLastPair.second.getWorldCoords(),
                                                                          newLastPair.second.getRc() );
                                conModel.getFootPanel().setLastAbstractPoint( newLastPair.first.getNewValue(),
                                                                              newLastPair.first.getWorldCoords(),
                                                                              newLastPair.first.getRc() );
                            }
                            conModel.getPanel().repaint();
                            conModel.getFootPanel().repaint();

                        }
                    }
                }

            }
        }

    }

    /**
     * If there is a user change in the pointsTable like modifying a single point.
     * 
     * @param p
     *            that is viewed to be changed, not <Code>null</Code>.
     * @param data
     *            the new data, not <Code>null</Code>.
     * @param row
     *            the row to be changed, not <Code>null</Code>.
     * @param column
     *            the column to be changed, not <Code>null</Code>.
     */
    private boolean changePointLocation( Triple<Point4Values, Point4Values, PointResidual> p, Object data, int row,
                                         int column ) {
        boolean changed = false;
        AbstractGRPoint pixelValue = null;
        AbstractGRPoint worldCoords = null;
        int rowPoints = p.first.getRc().getRow();
        int fcpx = p.first.getRc().getColumnX();
        int fcpy = p.first.getRc().getColumnY();

        int scpx = p.second.getRc().getColumnX();
        int scpy = p.second.getRc().getColumnY();
        if ( row == rowPoints ) {
            if ( column == fcpx ) {
                worldCoords = new FootprintPoint( new Double( data.toString() ).doubleValue(),
                                                  p.first.getWorldCoords().getY() );
                int[] i = sceneValues.getPixelCoord( worldCoords );
                pixelValue = new FootprintPoint( i[0], i[1] );

                p.first = new Point4Values( pixelValue, pixelValue, pixelValue, worldCoords, p.first.getRc() );
                changed = true;
            } else if ( column == fcpy ) {
                worldCoords = new FootprintPoint( p.first.getWorldCoords().getX(),
                                                  new Double( data.toString() ).doubleValue() );
                int[] i = sceneValues.getPixelCoord( worldCoords );
                pixelValue = new FootprintPoint( i[0], i[1] );
                p.first = new Point4Values( pixelValue, pixelValue, pixelValue, worldCoords, p.first.getRc() );
                changed = true;
            } else if ( column == scpx ) {
                worldCoords = new GeoReferencedPoint( new Double( data.toString() ).doubleValue(),
                                                      p.second.getWorldCoords().getY() );
                int[] i = sceneValues.getPixelCoord( worldCoords );
                pixelValue = new GeoReferencedPoint( i[0], i[1] );

                p.second = new Point4Values( pixelValue, pixelValue, pixelValue, worldCoords, p.second.getRc() );
                changed = true;
            } else if ( column == scpy ) {
                worldCoords = new GeoReferencedPoint( p.second.getWorldCoords().getX(),
                                                      new Double( data.toString() ).doubleValue() );
                int[] i = sceneValues.getPixelCoord( worldCoords );
                pixelValue = new GeoReferencedPoint( i[0], i[1] );

                p.second = new Point4Values( pixelValue, pixelValue, pixelValue, worldCoords, p.second.getRc() );
                changed = true;
            }

        }
        return changed;
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
                        ( (GeneralPanel) optionSettingPanel ).setSnappingOnOff( conModel.getDialogModel().getSnappingOnOff().second );
                        ( (GeneralPanel) optionSettingPanel ).setInitialZoomValue( conModel.getDialogModel().getResizeValue().second );
                        break;
                    case ViewPanel:
                        optionSettingPanel = new ViewPanel();
                        ( (ViewPanel) optionSettingPanel ).setPointSize( conModel.getDialogModel().getSelectionPointSize().second );
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
                    isZoomInGeoref = buttonZoomInGeoref.isSelected();
                    isZoomOutGeoref = buttonZoomoutGeoref.isSelected();

                }
                if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {
                    mouseFootprint.setPointMousePressed( new Point2d( m.getX(), m.getY() ) );
                    isControlDown = m.isControlDown();
                    isZoomInFoot = buttonZoominFoot.isSelected();
                    isZoomOutFoot = buttonZoomoutFoot.isSelected();
                }
            }

        }

        @Override
        public void mouseReleased( MouseEvent m ) {
            Object source = m.getSource();
            boolean isFirstNumber = false;
            if ( source instanceof JPanel ) {
                // Scene2DPanel
                if ( ( (JPanel) source ).getName().equals( Scene2DPanel.SCENE2D_PANEL_NAME ) ) {
                    if ( model != null ) {
                        if ( isControlDown || isZoomInGeoref || isZoomOutGeoref ) {
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

                            if ( isZoomInGeoref ) {
                                if ( minPoint.getX() == maxPoint.getX() && minPoint.getY() == maxPoint.getY() ) {
                                    sceneValues.computeZoomedEnvelope(
                                                                       true,
                                                                       conModel.getDialogModel().getResizeValue().second,
                                                                       new GeoReferencedPoint( minPoint.getX(),
                                                                                               minPoint.getY() ) );
                                } else {
                                    Rectangle r = new Rectangle(
                                                                 new Double( minPoint.getX() ).intValue(),
                                                                 new Double( minPoint.getY() ).intValue(),
                                                                 Math.abs( new Double( maxPoint.getX()
                                                                                       - minPoint.getX() ).intValue() ),
                                                                 Math.abs( new Double( maxPoint.getY()
                                                                                       - minPoint.getY() ).intValue() ) );

                                    sceneValues.createZoomedEnvWithMinPoint( PointType.GeoreferencedPoint, r );

                                }
                            } else if ( isZoomOutGeoref ) {
                                sceneValues.computeZoomedEnvelope( false,
                                                                   conModel.getDialogModel().getResizeValue().second,
                                                                   new GeoReferencedPoint( maxPoint.getX(),
                                                                                           maxPoint.getY() ) );
                            }

                            conModel.getPanel().setImageToDraw(
                                                                model.generateSubImageFromRaster( sceneValues.getEnvelopeGeoref() ) );
                            conModel.getPanel().updatePoints( sceneValues );
                            conModel.getPanel().setZoomRect( null );
                            conModel.getPanel().repaint();
                        }

                        else {
                            if ( isHorizontalRefGeoref == true ) {
                                if ( start == false ) {
                                    start = true;
                                    conModel.getFootPanel().setFocus( false );
                                    conModel.getPanel().setFocus( true );
                                }
                                if ( conModel.getFootPanel().getLastAbstractPoint() != null
                                     && conModel.getPanel().getLastAbstractPoint() != null
                                     && conModel.getPanel().getFocus() == true ) {
                                    setValues();
                                }
                                if ( conModel.getFootPanel().getLastAbstractPoint() == null
                                     && conModel.getPanel().getLastAbstractPoint() == null
                                     && conModel.getPanel().getFocus() == true ) {
                                    tablePanel.addRow();
                                    isFirstNumber = true;
                                }

                                double x = m.getX();
                                double y = m.getY();
                                GeoReferencedPoint geoReferencedPoint = new GeoReferencedPoint( x, y );
                                GeoReferencedPoint g = (GeoReferencedPoint) sceneValues.getWorldPoint( geoReferencedPoint );
                                rc = tablePanel.setCoords( g );
                                conModel.getPanel().setLastAbstractPoint( geoReferencedPoint, g, rc );
                                if ( isFirstNumber == false ) {
                                    updateResidualsWithLastAbstractPoint();
                                }
                            } else {
                                // just pan
                                mouseGeoRef.setMouseChanging( new GeoReferencedPoint(
                                                                                      ( mouseGeoRef.getPointMousePressed().getX() - m.getX() ),
                                                                                      ( mouseGeoRef.getPointMousePressed().getY() - m.getY() ) ) );

                                sceneValues.moveEnvelope( mouseGeoRef.getMouseChanging() );
                                conModel.getPanel().setImageToDraw(
                                                                    model.generateSubImageFromRaster( sceneValues.getEnvelopeGeoref() ) );
                                conModel.getPanel().updatePoints( sceneValues );
                            }

                            conModel.getPanel().repaint();
                        }

                    }
                }
                // footprintPanel
                if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {

                    if ( isControlDown || isZoomInFoot || isZoomOutFoot ) {
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

                        if ( isZoomInFoot ) {
                            if ( minPoint.getX() == maxPoint.getX() && minPoint.getY() == maxPoint.getY() ) {
                                sceneValues.computeZoomedEnvelope(
                                                                   true,
                                                                   conModel.getDialogModel().getResizeValue().second,
                                                                   new FootprintPoint( minPoint.getX(), minPoint.getY() ) );
                            } else {
                                Rectangle r = new Rectangle(
                                                             new Double( minPoint.getX() ).intValue(),
                                                             new Double( minPoint.getY() ).intValue(),
                                                             Math.abs( new Double( maxPoint.getX() - minPoint.getX() ).intValue() ),
                                                             Math.abs( new Double( maxPoint.getY() - minPoint.getY() ).intValue() ) );

                                sceneValues.createZoomedEnvWithMinPoint( PointType.FootprintPoint, r );
                            }
                        } else if ( isZoomOutFoot ) {
                            sceneValues.computeZoomedEnvelope( false,
                                                               conModel.getDialogModel().getResizeValue().second,
                                                               new FootprintPoint( maxPoint.getX(), maxPoint.getY() ) );
                        }
                        conModel.getFootPanel().setZoomRect( null );
                        conModel.getFootPanel().updatePoints( sceneValues );
                        conModel.getFootPanel().repaint();

                    } else {
                        if ( isHorizontalRefFoot == true ) {

                            if ( start == false ) {
                                start = true;
                                conModel.getFootPanel().setFocus( true );
                                conModel.getPanel().setFocus( false );
                            }
                            if ( conModel.getFootPanel().getLastAbstractPoint() != null
                                 && conModel.getPanel().getLastAbstractPoint() != null
                                 && conModel.getFootPanel().getFocus() == true ) {
                                setValues();
                            }
                            if ( conModel.getFootPanel().getLastAbstractPoint() == null
                                 && conModel.getPanel().getLastAbstractPoint() == null
                                 && conModel.getFootPanel().getFocus() == true ) {
                                tablePanel.addRow();
                                isFirstNumber = true;
                            }
                            double x = m.getX();
                            double y = m.getY();
                            Pair<AbstractGRPoint, FootprintPoint> point = null;
                            if ( conModel.getDialogModel().getSnappingOnOff().first ) {
                                point = conModel.getFootPanel().getClosestPoint( new FootprintPoint( x, y ) );
                            } else {
                                point = new Pair<AbstractGRPoint, FootprintPoint>(
                                                                                   new FootprintPoint( x, y ),
                                                                                   (FootprintPoint) sceneValues.getWorldPoint( new FootprintPoint(
                                                                                                                                                   x,
                                                                                                                                                   y ) ) );
                            }
                            rc = tablePanel.setCoords( point.second );
                            conModel.getFootPanel().setLastAbstractPoint( point.first, point.second, rc );
                            if ( isFirstNumber == false ) {
                                updateResidualsWithLastAbstractPoint();
                            }

                        } else {
                            mouseFootprint.setMouseChanging( new FootprintPoint(
                                                                                 ( mouseFootprint.getPointMousePressed().getX() - m.getX() ),
                                                                                 ( mouseFootprint.getPointMousePressed().getY() - m.getY() ) ) );

                            sceneValues.moveEnvelope( mouseFootprint.getMouseChanging() );
                            conModel.getFootPanel().updatePoints( sceneValues );
                        }
                        conModel.getFootPanel().repaint();
                    }

                }
            }
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
                    if ( m.isControlDown() || isZoomInGeoref ) {
                        int x = new Double( mouseGeoRef.getPointMousePressed().getX() ).intValue();
                        int y = new Double( mouseGeoRef.getPointMousePressed().getY() ).intValue();
                        int width = new Double( m.getX() - mouseGeoRef.getPointMousePressed().getX() ).intValue();
                        int height = new Double( m.getY() - mouseGeoRef.getPointMousePressed().getY() ).intValue();
                        Rectangle rec = new Rectangle( x, y, width, height );
                        conModel.getPanel().setZoomRect( rec );
                        conModel.getPanel().repaint();
                    }
                }
                // footprintPanel
                if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {
                    if ( m.isControlDown() || isZoomInFoot ) {
                        int x = new Double( mouseFootprint.getPointMousePressed().getX() ).intValue();
                        int y = new Double( mouseFootprint.getPointMousePressed().getY() ).intValue();
                        int width = new Double( m.getX() - mouseFootprint.getPointMousePressed().getX() ).intValue();
                        int height = new Double( m.getY() - mouseFootprint.getPointMousePressed().getY() ).intValue();
                        Rectangle rec = new Rectangle( x, y, width, height );
                        conModel.getFootPanel().setZoomRect( rec );
                        conModel.getFootPanel().repaint();
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

        private AbstractGRPoint mouseOver;

        @Override
        public void mouseWheelMoved( MouseWheelEvent m ) {

            Object source = m.getSource();

            if ( source instanceof JPanel ) {

                // Scene2DPanel
                if ( ( (JPanel) source ).getName().equals( Scene2DPanel.SCENE2D_PANEL_NAME ) ) {
                    if ( model != null ) {
                        mouseOver = mouseGeoRef.getMouseMoved();
                        // resizing = .05f;
                        if ( m.getWheelRotation() < 0 ) {
                            zoomIn = true;
                        } else {
                            zoomIn = false;
                        }
                        sceneValues.computeZoomedEnvelope( zoomIn, conModel.getDialogModel().getResizeValue().second,
                                                           mouseOver );
                        conModel.getPanel().setImageToDraw(
                                                            model.generateSubImageFromRaster( sceneValues.getEnvelopeGeoref() ) );
                        conModel.getPanel().updatePoints( sceneValues );
                        conModel.getPanel().repaint();
                    }
                }
                // footprintPanel
                if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {

                    // resizing = .1f;
                    mouseOver = mouseFootprint.getMouseMoved();
                    if ( m.getWheelRotation() < 0 ) {
                        zoomIn = true;
                    } else {
                        zoomIn = false;
                    }
                    sceneValues.computeZoomedEnvelope( zoomIn, conModel.getDialogModel().getResizeValue().second,
                                                       mouseOver );
                    conModel.getFootPanel().updatePoints( sceneValues );
                    conModel.getFootPanel().repaint();
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
                if ( model != null && model.getGeneratedImage() != null ) {
                    init();

                    if ( sceneValues != null ) {

                        sceneValues.setDimensionFootpanel( new Rectangle( conModel.getFootPanel().getBounds().width,
                                                                          conModel.getFootPanel().getBounds().height ) );
                        conModel.getFootPanel().updatePoints( sceneValues );
                        conModel.getFootPanel().repaint();
                    }

                }
            }

        }

        @Override
        public void componentShown( ComponentEvent arg0 ) {
            // TODO Auto-generated method stub

        }

    }

    /**
     * Initializes the computing and the painting of the maps.
     */
    private void init() {

        if ( model != null ) {
            sceneValues.setGeorefDimension( new Rectangle( conModel.getPanel().getWidth(),
                                                           conModel.getPanel().getHeight() ) );
            conModel.getPanel().setImageDimension( sceneValues.getGeorefDimension() );
            conModel.getPanel().setImageToDraw( model.generateSubImage( sceneValues.getGeorefDimension() ) );
            conModel.getPanel().updatePoints( sceneValues );
            conModel.getPanel().repaint();
        }

    }

    /**
     * Sets values to the JTableModel.
     */
    private void setValues() {
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

    /**
     * Determines the transformationMethod by means of the type.
     * 
     * @param type
     *            of the transformationMethod, not <Code>null</Code>.
     * @return the transformationMethod to be used.
     */
    private TransformationMethod determineTransformationType( TransformationType type ) {
        TransformationMethod t = null;
        switch ( type ) {
        case Polynomial:
            t = new Polynomial( mappedPoints, footPrint, sceneValues, sourceCRS, targetCRS, order );
            break;
        case Helmert_4:
            t = new Helmert4Transform( mappedPoints, footPrint, sceneValues, sourceCRS, targetCRS, 1 );
            break;

        case Affine:
            t = new AffineTransformation( mappedPoints, footPrint, sceneValues, sourceCRS, targetCRS, 1 );
            break;
        }

        return t;
    }

    /**
     * Updates the model of the table to show the residuals of the already stored mappedPoints. It is based on the
     * Helmert transformation.
     * 
     * @param type
     * 
     */
    private void updateResiduals( TransformationType type ) {

        TransformationMethod t = determineTransformationType( type );
        PointResidual[] r = t.calculateResiduals();
        Vector<Vector<Double>> data = new Vector<Vector<Double>>();
        int counter = 0;
        for ( Triple<Point4Values, Point4Values, PointResidual> point : mappedPoints ) {
            Vector element = new Vector( 6 );
            element.add( point.second.getWorldCoords().getX() );
            element.add( point.second.getWorldCoords().getY() );
            element.add( point.first.getWorldCoords().getX() );
            element.add( point.first.getWorldCoords().getY() );
            element.add( r[counter].getX() );
            element.add( r[counter].getY() );
            data.add( element );

            point.third = r[counter++];

        }
        tablePanel.getModel().setDataVector( data, tablePanel.getColumnNamesAsVector() );
        tablePanel.getModel().fireTableDataChanged();
    }

    private void updateResidualsWithLastAbstractPoint() {
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
    private void addToMappedPoints( Point4Values mappedPointKey, Point4Values mappedPointValue, PointResidual residual ) {
        if ( mappedPointKey != null && mappedPointValue != null ) {
            this.mappedPoints.add( new Triple<Point4Values, Point4Values, PointResidual>( mappedPointKey,
                                                                                          mappedPointValue, residual ) );
        }

    }

    /**
     * Removes sample points in panels and the table.
     * 
     * @param pointFromTable
     *            that should be removed, could be <Code>null</Code>
     */
    private void removeFromMappedPoints( int[] tableRows ) {
        for ( int i = tableRows.length - 1; i >= 0; i-- ) {
            mappedPoints.remove( tableRows[i] );
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

                PointResidual r = new PointResidual( p.third.getX(), p.third.getY() );
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
     * Removes everything after a complete deletion of the points.
     */
    private void removeAllFromMappedPoints() {
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
    private void reset() {
        conModel.getPanel().setFocus( false );
        conModel.getFootPanel().setFocus( false );
        start = false;

    }

    public ControllerModel getConModel() {
        return conModel;
    }

}
