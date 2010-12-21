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
import static org.deegree.tools.crs.georeferencing.i18n.Messages.get;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.Triple;
import org.deegree.cs.CRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.Ring;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.XMLTransformer;
import org.deegree.rendering.r3d.model.geometry.GeometryQualityModel;
import org.deegree.rendering.r3d.model.geometry.SimpleAccessGeometry;
import org.deegree.rendering.r3d.opengl.display.OpenGLEventHandler;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.WorldRenderableObject;
import org.deegree.tools.crs.georeferencing.application.handler.FileInputHandler;
import org.deegree.tools.crs.georeferencing.application.handler.FileOutputHandler;
import org.deegree.tools.crs.georeferencing.application.handler.JCheckboxHandler;
import org.deegree.tools.crs.georeferencing.application.transformation.AbstractTransformation;
import org.deegree.tools.crs.georeferencing.application.transformation.AffineTransformation;
import org.deegree.tools.crs.georeferencing.application.transformation.Helmert4Transform;
import org.deegree.tools.crs.georeferencing.application.transformation.Polynomial;
import org.deegree.tools.crs.georeferencing.communication.FileChooser;
import org.deegree.tools.crs.georeferencing.communication.GRViewerGUI;
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
import org.deegree.tools.crs.georeferencing.model.mouse.FootprintMouseModel;
import org.deegree.tools.crs.georeferencing.model.mouse.GeoReferencedMouseModel;
import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint;
import org.deegree.tools.crs.georeferencing.model.points.FootprintPoint;
import org.deegree.tools.crs.georeferencing.model.points.GeoReferencedPoint;
import org.deegree.tools.crs.georeferencing.model.points.Point4Values;
import org.deegree.tools.crs.georeferencing.model.points.PointResidual;
import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint.PointType;
import org.deegree.tools.crs.georeferencing.model.textfield.CoordinateJumperModel;
import org.deegree.tools.rendering.viewer.File3dImporter;
import org.slf4j.Logger;

/**
 * The <Code>Controller</Code> is responsible to bind the view with the model.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Controller {

    static final Logger LOG = getLogger( Controller.class );

    private Footprint footPrint;

    private OpenGLEventHandler glHandler;

    ApplicationState state = new ApplicationState();

    private GeometryFactory geom;

    String chosenFile;

    CRS sourceCRS;

    private CRS targetCRS;

    public Controller( GRViewerGUI view ) {

        geom = new GeometryFactory();
        state.sceneValues = new Scene2DValues( geom );

        state.conModel = new ControllerModel( view, view.getFootprintPanel(), view.getScenePanel2D(),
                                              new OptionDialogModel() );

        state.start = false;

        this.glHandler = view.getOpenGLEventListener();
        state.textFieldModel = new CoordinateJumperModel();
        AbstractPanel2D.selectedPointSize = state.conModel.getDialogModel().getSelectionPointSize().first;
        AbstractPanel2D.zoomValue = state.conModel.getDialogModel().getResizeValue().first;

        this.state.mappedPoints = new ArrayList<Triple<Point4Values, Point4Values, PointResidual>>();

        view.addListeners( new ButtonListener() );
        view.addHoleWindowListener( new HoleWindowListener() );

        initToggleButtons();

        // init the Checkboxlist for Transformation
        state.modelTransformation = new CheckBoxListModel();
        state.checkBoxListTransform = new CheckboxListTransformation( state.modelTransformation );
        view.addToMenuTransformation( state.checkBoxListTransform );
        state.checkBoxListTransform.addCheckboxListener( new ButtonListener() );

        // init the transformation method
        state.tablePanel = new PointTableFrame();
        state.tablePanel.getSaveButton().setEnabled( false );
        state.tablePanel.getLoadButton().setEnabled( false );
        state.tablePanel.addTableModelListener( new TableChangedEventListener() );
        state.tablePanel.addActionButtonListener( new ButtonListener() );
        // transform = null;
        if ( state.conModel.getTransformationType() == null ) {
            for ( JCheckBox box : state.modelTransformation.getList() ) {
                if ( ( box ).getText().startsWith( get( "MENUITEM_TRANS_HELMERT" ) ) ) {
                    state.conModel.setTransformationType( AbstractTransformation.TransformationType.Helmert_4 );
                    view.activateTransformationCheckbox( box );
                    break;
                }
            }

        }

        state.isHorizontalRefGeoref = true;
        state.isHorizontalRefFoot = true;

    }

    /**
     * Initializes the navigation buttons that are registered for each map.
     */
    private void initToggleButtons() {
        state.buttonPanGeoref = state.conModel.getView().getNavigationPanelGeoref().getButtonPan();
        state.buttonPanFoot = state.conModel.getView().getNaviPanelFoot().getButtonPan();
        state.buttonZoomInGeoref = state.conModel.getView().getNavigationPanelGeoref().getButtonZoomIn();
        state.buttonZoominFoot = state.conModel.getView().getNaviPanelFoot().getButtonZoomIn();
        state.buttonZoomoutGeoref = state.conModel.getView().getNavigationPanelGeoref().getButtonZoomOut();
        state.buttonZoomoutFoot = state.conModel.getView().getNaviPanelFoot().getButtonZoomOut();
        state.buttonCoord = state.conModel.getView().getNavigationPanelGeoref().getButtonZoomCoord();
    }

    /**
     * Selects one navigation button and deselects the other so that the focus is just on this one button. The
     * georeferencing for the georeferenced map will be turned off in this case. <br>
     * If the button is selected already, that will be deselected and there is a horizontal referencing possible again.
     * 
     * @param t
     *            the toggleButton that should be selected/deselected, not <Code>null</Code>.
     */
    void selectGeorefToggleButton( JToggleButton t ) {
        boolean checkSelected = false;
        state.buttonModel = t.getModel();
        state.selectedGeoref = state.buttonModel.isSelected();
        if ( state.selectedGeoref == false ) {
            state.isHorizontalRefGeoref = true;
        } else {
            checkSelected = true;
            state.buttonPanGeoref.setSelected( false );
            state.buttonZoomInGeoref.setSelected( false );
            state.buttonZoomoutGeoref.setSelected( false );
            state.buttonCoord.setSelected( false );
            state.isHorizontalRefGeoref = false;
        }
        if ( t == state.buttonPanGeoref ) {
            state.buttonPanGeoref.setSelected( checkSelected );
        } else if ( t == state.buttonZoomInGeoref ) {
            state.buttonZoomInGeoref.setSelected( checkSelected );
        } else if ( t == state.buttonZoomoutGeoref ) {
            state.buttonZoomoutGeoref.setSelected( checkSelected );
        } else if ( t == state.buttonCoord ) {
            state.buttonCoord.setSelected( checkSelected );
            if ( checkSelected == true ) {
                // state.jumperDialog = new CoordinateJumperSpinnerDialog( view );
                state.jumperDialog = new CoordinateJumperTextfieldDialog( state.conModel.getView() );
                state.jumperDialog.getCoordinateJumper().setToolTipText( state.textFieldModel.getTooltipText() );
                state.jumperDialog.addListeners( new ButtonListener() );
                state.jumperDialog.setVisible( true );
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
    void selectFootprintToggleButton( JToggleButton t ) {

        boolean checkSelected = false;
        state.buttonModel = t.getModel();
        state.selectedFoot = state.buttonModel.isSelected();
        if ( state.selectedFoot == false ) {
            state.isHorizontalRefFoot = true;
        } else {
            checkSelected = true;
            state.buttonPanFoot.setSelected( false );
            state.buttonZoominFoot.setSelected( false );
            state.buttonZoomoutFoot.setSelected( false );
            state.isHorizontalRefFoot = false;
        }
        if ( t == state.buttonPanFoot ) {
            state.buttonPanFoot.setSelected( checkSelected );

        } else if ( t == state.buttonZoominFoot ) {
            state.buttonZoominFoot.setSelected( checkSelected );

        } else if ( t == state.buttonZoomoutFoot ) {
            state.buttonZoomoutFoot.setSelected( checkSelected );

        }

    }

    /**
     * Initializes the footprint scene.
     */
    void initFootprintScene( String filePath ) {
        state.isInitFoot = true;
        if ( state.isInitGeoref ) {
            state.tablePanel.getSaveButton().setEnabled( true );
            state.tablePanel.getLoadButton().setEnabled( true );
        }

        this.footPrint = new Footprint( state.sceneValues, geom );
        removeListeners( state.conModel.getFootPanel() );
        state.conModel.getFootPanel().addScene2DMouseListener( new Scene2DMouseListener() );
        state.conModel.getFootPanel().addScene2DMouseMotionListener( new Scene2DMouseMotionListener() );
        state.conModel.getFootPanel().addScene2DMouseWheelListener( new Scene2DMouseWheelListener() );

        state.mouseFootprint = new FootprintMouseModel();
        List<WorldRenderableObject> rese = File3dImporter.open( state.conModel.getView(), filePath );
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

        state.sceneValues.setDimensionFootpanel( new Rectangle( state.conModel.getFootPanel().getBounds().width,
                                                                state.conModel.getFootPanel().getBounds().height ) );
        state.conModel.getFootPanel().updatePoints( state.sceneValues );

        state.conModel.getFootPanel().setPolygonList( footPrint.getWorldCoordinateRingList(), state.sceneValues );

        state.conModel.getFootPanel().repaint();

    }

    /**
     * Initializes the georeferenced scene.
     */
    void initGeoReferencingScene( Scene2D scene2d ) {
        state.isInitGeoref = true;
        if ( state.isInitFoot ) {

            state.tablePanel.getSaveButton().setEnabled( true );
            state.tablePanel.getLoadButton().setEnabled( true );

        }

        state.mouseGeoRef = new GeoReferencedMouseModel();
        scene2d.init( state.sceneValues );
        targetCRS = scene2d.getCRS();
        init();
        removeListeners( state.conModel.getPanel() );
        state.conModel.getPanel().addScene2DMouseListener( new Scene2DMouseListener() );
        state.conModel.getPanel().addScene2DMouseMotionListener( new Scene2DMouseMotionListener() );
        state.conModel.getPanel().addScene2DMouseWheelListener( new Scene2DMouseWheelListener() );
    }

    /**
     * Removes all the listeners of one component. http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4380536
     * 
     * @param comp
     */
    private static void removeListeners( Component comp ) {
        Method[] methods = comp.getClass().getMethods();
        for ( int i = 0; i < methods.length; i++ ) {
            Method method = methods[i];
            String name = method.getName();
            if ( name.startsWith( "remove" ) && name.endsWith( "Listener" ) ) {

                @SuppressWarnings("unchecked")
                Class<EventListener>[] params = (Class<EventListener>[]) method.getParameterTypes();
                if ( params.length == 1 ) {
                    EventListener[] listeners = null;
                    try {
                        listeners = comp.getListeners( params[0] );
                    } catch ( Exception e ) {
                        // It is possible that someone could create a listener
                        // that doesn't extend from EventListener. If so,
                        // ignore it
                        System.out.println( "Listener " + params[0] + " does not extend EventListener" );
                        continue;
                    }
                    for ( int j = 0; j < listeners.length; j++ ) {
                        try {
                            method.invoke( comp, new Object[] { listeners[j] } );
                            // System.out.println("removed Listener " + name + "for comp " + comp + "\n");
                        } catch ( Exception e ) {
                            System.out.println( "Cannot invoke removeListener method " + e );
                            // Continue on. The reason for removing all listeners is to
                            // make sure that we don't have a listener holding on to something
                            // which will keep it from being garbage collected. We want to
                            // continue freeing listeners to make sure we can free as much
                            // memory has possible
                        }
                    }
                } else {
                    // The only Listener method that I know of that has more than
                    // one argument is removePropertyChangeListener. If it is
                    // something other than that, flag it and move on.
                    if ( !name.equals( "removePropertyChangeListener" ) )
                        System.out.println( "    Wrong number of Args " + name );
                }
            }
        }
    }

    /**
     * Updates the panels that are responsible for drawing the georeferenced points so that the once clicked points are
     * drawn into the right position.
     */
    void updateDrawingPanels() {
        List<Point4Values> panelList = new ArrayList<Point4Values>();
        List<Point4Values> footPanelList = new ArrayList<Point4Values>();
        for ( Triple<Point4Values, Point4Values, PointResidual> p : state.mappedPoints ) {
            panelList.add( p.second );
            footPanelList.add( p.first );
        }

        state.conModel.getPanel().setSelectedPoints( panelList, state.sceneValues );
        state.conModel.getFootPanel().setSelectedPoints( footPanelList, state.sceneValues );

        state.conModel.getPanel().repaint();
        state.conModel.getFootPanel().repaint();

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

        private boolean exceptionThrown = false;

        @Override
        public void actionPerformed( ActionEvent e ) {
            Object source = e.getSource();
            if ( source instanceof JTextField ) {
                JTextField tF = (JTextField) source;
                if ( tF.getName().startsWith( get( "JTEXTFIELD_COORDINATE_JUMPER" ) ) ) {
                    fireTextfieldJumperDialog();
                }

            } else if ( source instanceof JToggleButton ) {
                if ( source instanceof JRadioButton ) {
                    int pointSize = ( (ViewPanel) state.optionSettingPanel ).getTbm().getButtons().get( source );
                    state.conModel.getDialogModel().setSelectionPointSize( pointSize );

                } else if ( source instanceof JCheckBox ) {
                    JCheckBox selectedCheckbox = (JCheckBox) source;

                    new JCheckboxHandler( selectedCheckbox, state.conModel, state.wmsParameter );

                } else {
                    JToggleButton tb = (JToggleButton) source;

                    if ( tb.getName().startsWith( get( "JBUTTON_PAN" ) ) ) {

                        if ( tb == state.buttonPanGeoref ) {
                            selectGeorefToggleButton( tb );
                            state.isZoomInGeoref = false;
                            state.isZoomOutGeoref = false;
                        } else {
                            selectFootprintToggleButton( tb );
                            state.isZoomInFoot = false;
                            state.isZoomOutFoot = false;
                        }
                    } else if ( tb.getName().startsWith( get( "JBUTTON_ZOOM_COORD" ) ) ) {

                        if ( tb == state.buttonCoord ) {
                            selectGeorefToggleButton( tb );
                        } else {
                            selectFootprintToggleButton( tb );
                        }
                    } else if ( tb.getName().startsWith( get( "JBUTTON_ZOOM_IN" ) ) ) {

                        if ( tb == state.buttonZoomInGeoref ) {
                            selectGeorefToggleButton( tb );
                            state.isZoomInGeoref = true;
                            state.isZoomOutGeoref = false;
                        } else {
                            selectFootprintToggleButton( tb );
                            state.isZoomInFoot = true;
                            state.isZoomOutFoot = false;
                        }
                    } else if ( tb.getName().startsWith( get( "JBUTTON_ZOOM_OUT" ) ) ) {

                        if ( tb == state.buttonZoomoutGeoref ) {
                            selectGeorefToggleButton( tb );
                            state.isZoomInGeoref = false;
                            state.isZoomOutGeoref = true;
                        } else {
                            selectFootprintToggleButton( tb );
                            state.isZoomInFoot = true;
                            state.isZoomOutFoot = false;
                        }
                    }
                }
            } else if ( source instanceof JButton ) {

                if ( ( (JButton) source ).getText().startsWith( PointTableFrame.BUTTON_DELETE_SELECTED ) ) {
                    int[] tableRows = state.tablePanel.getTable().getSelectedRows();
                    List<Integer> deleteableRows = new ArrayList<Integer>();

                    for ( int tableRow : tableRows ) {
                        boolean contained = false;

                        for ( Triple<Point4Values, Point4Values, PointResidual> p : state.mappedPoints ) {
                            System.out.println( "[Controller] beforeRemoving: " + p.second + "\n" );
                            if ( p.first.getRc().getRow() == tableRow || p.second.getRc().getRow() == tableRow ) {

                                contained = true;
                                deleteableRows.add( tableRow );

                                System.out.println( "[Controller] afterRemoving: " + p.second + "\n\n" );
                                break;
                            }

                        }
                        if ( contained == false ) {

                            state.conModel.getFootPanel().setLastAbstractPoint( null, null, null );
                            state.conModel.getPanel().setLastAbstractPoint( null, null, null );
                        }
                    }
                    if ( deleteableRows.size() != 0 ) {
                        int[] temp = new int[deleteableRows.size()];
                        for ( int i = 0; i < temp.length; i++ ) {
                            temp[i] = deleteableRows.get( i );
                        }
                        removeFromMappedPoints( temp );
                    }
                    updateResidualsWithLastAbstractPoint();
                    updateDrawingPanels();
                } else if ( ( (JButton) source ).getText().startsWith( PointTableFrame.LOAD_POINTTABLE ) ) {

                    FileInputHandler in = new FileInputHandler( state.tablePanel );
                    if ( in.getData() != null ) {
                        VectorTransformer vt = new VectorTransformer( in.getData(), state.sceneValues );
                        state.mappedPoints.clear();
                        state.mappedPoints.addAll( vt.getMappedPoints() );
                        updateDrawingPanels();
                    }

                } else if ( ( (JButton) source ).getText().startsWith( PointTableFrame.SAVE_POINTTABLE ) ) {

                    new FileOutputHandler( state.tablePanel );

                } else if ( ( (JButton) source ).getText().startsWith( PointTableFrame.BUTTON_DELETE_ALL ) ) {
                    removeAllFromMappedPoints();

                } else if ( ( (JButton) source ).getText().startsWith( get( "RESET_VIEW_BUTTON_TEXT" ) ) ) {

                    initGeoReferencingScene( state.model );
                    if ( chosenFile != null ) {
                        initFootprintScene( chosenFile );

                        state.conModel.getFootPanel().updatePoints( state.sceneValues );
                        state.conModel.getFootPanel().repaint();
                    }
                    state.conModel.getPanel().updatePoints( state.sceneValues );
                    state.conModel.getPanel().repaint();

                } else if ( ( (JButton) source ).getText().startsWith( get( "COMPUTE_BUTTON_TEXT" ) ) ) {
                    // swap the tempPoints into the map now
                    if ( state.conModel.getFootPanel().getLastAbstractPoint() != null
                         && state.conModel.getPanel().getLastAbstractPoint() != null ) {
                        setValues();
                    }

                    try {
                        state.conModel.setTransform( determineTransformationType( state.conModel.getTransformationType() ) );
                    } catch ( UnknownCRSException e1 ) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    List<Ring> polygonRing = state.conModel.getTransform().computeRingList();

                    updateResiduals( state.conModel.getTransformationType() );

                    state.conModel.getPanel().setPolygonList( polygonRing, state.sceneValues );

                    state.conModel.getPanel().repaint();

                    reset();
                } else if ( ( (JButton) source ).getText().startsWith( ButtonPanel.BUTTON_TEXT_CANCEL ) ) {
                    if ( state.optionDialog != null && state.optionDialog.isVisible() == true ) {
                        state.conModel.getDialogModel().transferOldToNew();
                        AbstractPanel2D.selectedPointSize = state.conModel.getDialogModel().getSelectionPointSize().first;
                        state.conModel.getPanel().repaint();
                        state.conModel.getFootPanel().repaint();
                        state.optionDialog.setVisible( false );
                    } else if ( state.jumperDialog != null && state.jumperDialog.isVisible() == true ) {
                        state.jumperDialog.setVisible( false );

                        state.selectedGeoref = false;
                        state.buttonModel.setSelected( false );
                        state.isHorizontalRefGeoref = true;

                    } else if ( state.wmsStartDialog != null && state.wmsStartDialog.isVisible() == true ) {
                        state.wmsStartDialog.setVisible( false );

                    } else if ( state.wmsParameter != null && state.wmsParameter.isVisible() == true ) {
                        state.wmsParameter.setVisible( false );
                        state.wmsStartDialog.setVisible( true );
                    }

                } else if ( ( (JButton) source ).getText().startsWith( ButtonPanel.BUTTON_TEXT_OK ) ) {
                    if ( state.optionDialog != null && state.optionDialog.isVisible() == true ) {

                        if ( state.optionSettingPanel != null ) {

                            if ( state.optionSettingPanel instanceof GeneralPanel ) {
                                String p = ( (GeneralPanel) state.optionSettingPanel ).getTextField(
                                                                                                     ( (GeneralPanel) state.optionSettingPanel ).getZoomValue() ).getText();
                                String p1 = p.replace( ',', '.' );
                                state.conModel.getDialogModel().setResizeValue( new Double( p1 ).doubleValue() );
                                exceptionThrown = false;
                            }
                        }
                        if ( exceptionThrown == false ) {
                            state.conModel.getDialogModel().transferNewToOld();
                            AbstractPanel2D.selectedPointSize = state.conModel.getDialogModel().getSelectionPointSize().first;
                            state.conModel.getPanel().repaint();
                            state.conModel.getFootPanel().repaint();
                            state.optionDialog.setVisible( false );
                        }
                    } else if ( state.jumperDialog != null && state.jumperDialog.isVisible() == true ) {

                        fireTextfieldJumperDialog();
                    } else if ( state.wmsStartDialog != null && state.wmsStartDialog.isVisible() == true ) {
                        String mapURLString = state.wmsStartDialog.getTextField().getText();
                        state.wmsStartDialog.setVisible( false );
                        try {
                            state.wmsParameter = new WMSParameterChooser( state.wmsStartDialog, mapURLString );
                            state.wmsParameter.addCheckBoxListener( new ButtonListener() );
                        } catch ( MalformedURLException e1 ) {
                            new ErrorDialog( state.wmsStartDialog, ImageObserver.ERROR,
                                             "The requested URL is malformed! There is no response gotten from the server. " );
                            exceptionThrown = true;
                        } catch ( NullPointerException e2 ) {
                            new ErrorDialog( state.wmsStartDialog, ImageObserver.ERROR,
                                             "The requested URL is malformed! There is no response gotten from the server. " );
                            exceptionThrown = true;
                        }
                        if ( exceptionThrown == false ) {
                            state.wmsParameter.addListeners( new ButtonListener() );
                            state.wmsParameter.setVisible( true );
                        }

                    } else if ( state.wmsParameter != null && state.wmsParameter.isVisible() == true ) {

                        URL mapURL = state.wmsParameter.getMapURL();
                        CRS crs = state.wmsParameter.getCheckBoxSRS();
                        String layers = state.wmsParameter.getCheckBoxListAsString().toString();
                        List<String> layerList = state.wmsParameter.getCheckBoxListLayerText();
                        String format = state.wmsParameter.getCheckBoxFormatAsString().toString();

                        if ( layers == null || layers.length() == 0 ) {
                            new ErrorDialog( state.wmsParameter, ImageObserver.ERROR,
                                             "There is no Layer selected. Please selected at least one. " );
                        } else if ( format == null || format.equals( "" ) ) {
                            new ErrorDialog( state.wmsParameter, ImageObserver.ERROR, "There is no format selected. " );
                        } else if ( crs == null ) {
                            new ErrorDialog( state.wmsParameter, ImageObserver.ERROR, "There is no CRS selected. " );
                        } else {
                            Envelope env = state.wmsParameter.getEnvelope( crs, layerList );
                            if ( env != null ) {
                                int qor = max( state.conModel.getPanel().getWidth(),
                                               state.conModel.getPanel().getHeight() );
                                state.store = new ParameterStore( mapURL, env.getCoordinateSystem(), format, layers,
                                                                  env, qor );
                                state.model = new Scene2DImplWMS( state.store, state.wmsParameter.getWmsClient() );
                                initGeoReferencingScene( state.model );
                                state.wmsParameter.setVisible( false );
                            } else {
                                new ErrorDialog( state.wmsParameter, ImageObserver.ERROR,
                                                 "There is no Envelope for this request. " );
                            }
                        }

                    }
                }
            } else if ( source instanceof JMenuItem ) {

                if ( ( (JMenuItem) source ).getText().startsWith( get( "MENUITEM_EDIT_OPTIONS" ) ) ) {
                    DefaultMutableTreeNode root = new DefaultMutableTreeNode( "Options" );

                    state.conModel.getDialogModel().createNodes( root );
                    state.optionDialog = new OptionDialog( state.conModel.getView(), root );
                    state.optionDialog.getButtonPanel().addListeners( new ButtonListener() );
                    state.optionNavPanel = state.optionDialog.getNavigationPanel();
                    state.optionSettPanel = state.optionDialog.getSettingsPanel();

                    // add the listener to the navigation panel
                    state.optionNavPanel.addTreeListener( new NavigationTreeSelectionListener() );

                    state.optionDialog.setVisible( true );

                } else if ( ( (JMenuItem) source ).getText().startsWith( get( "MENUITEM_EXIT" ) ) ) {
                    System.exit( 0 );
                } else if ( ( (JMenuItem) source ).getText().startsWith( get( "MENUITEM_SAVE_BUILDING" ) ) ) {
                    List<String> list = new ArrayList<String>();
                    list.add( "gml" );
                    list.add( "xml" );
                    String desc = "(*.gml, *.xml) GML or CityGML-Files";
                    Pair<List<String>, String> supportedFiles = new Pair<List<String>, String>( list, desc );
                    List<Pair<List<String>, String>> supportedOpenFiles = new ArrayList<Pair<List<String>, String>>();
                    supportedOpenFiles.add( supportedFiles );
                    FileChooser fileChooser = new FileChooser( supportedOpenFiles, state.conModel.getView(), true );
                    chosenFile = fileChooser.getOpenPath();
                    fileChooser = new FileChooser( supportedOpenFiles, state.conModel.getView(), false );
                    File saveFile = fileChooser.getSaveFile();
                    if ( chosenFile != null && saveFile != null ) {
                        XMLStreamReader reader = null;
                        XMLStreamWriter writer = null;
                        try {
                            XMLInputFactory inFac = XMLInputFactory.newInstance();
                            reader = inFac.createXMLStreamReader( new File( chosenFile ).toURI().toURL().openStream() );

                            XMLOutputFactory outFac = XMLOutputFactory.newInstance();
                            writer = outFac.createXMLStreamWriter( new FileOutputStream( saveFile ) );

                            XMLTransformer transformer = new XMLTransformer( state.conModel.getTransform() );
                            transformer.transform( reader, writer, GMLVersion.GML_31 );

                        } catch ( ClassCastException e1 ) {
                            LOG.trace( "Stack trace:", e1 );
                        } catch ( MalformedURLException e1 ) {
                            LOG.trace( "Stack trace:", e1 );
                        } catch ( XMLStreamException e1 ) {
                            LOG.trace( "Stack trace:", e1 );
                        } catch ( FactoryConfigurationError e1 ) {
                            LOG.trace( "Stack trace:", e1 );
                        } catch ( IOException e1 ) {
                            LOG.trace( "Stack trace:", e1 );
                        } catch ( UnknownCRSException e1 ) {
                            LOG.trace( "Stack trace:", e1 );
                        } catch ( TransformationException e1 ) {
                            LOG.trace( "Stack trace:", e1 );
                        } finally {
                            if ( reader != null ) {
                                try {
                                    reader.close();
                                } catch ( XMLStreamException e1 ) {
                                    LOG.trace( "Stack trace:", e1 );
                                }
                            }
                            if ( writer != null ) {
                                try {
                                    writer.close();
                                } catch ( XMLStreamException e1 ) {
                                    LOG.trace( "Stack trace:", e1 );
                                }
                            }
                        }
                    }
                } else if ( ( (JMenuItem) source ).getText().startsWith( get( "MENUITEM_OPEN_BUILDING" ) ) ) {
                    List<String> list = new ArrayList<String>();
                    list.add( "gml" );
                    list.add( "xml" );
                    String desc = "(*.gml, *.xml) GML or CityGML-Files";
                    Pair<List<String>, String> supportedFiles = new Pair<List<String>, String>( list, desc );
                    List<Pair<List<String>, String>> supportedOpenFiles = new ArrayList<Pair<List<String>, String>>();
                    supportedOpenFiles.add( supportedFiles );
                    FileChooser fileChooser = new FileChooser( supportedOpenFiles, state.conModel.getView(), true );
                    chosenFile = fileChooser.getOpenPath();
                    if ( chosenFile != null ) {
                        initFootprintScene( chosenFile );
                    }
                } else if ( ( (JMenuItem) source ).getText().startsWith( get( "MENUITEM_OPEN_SHAPEFILE" ) ) ) {
                    List<String> list = new ArrayList<String>();
                    list.add( "shp" );

                    String desc = "(*.shp) Esri ShapeFiles";
                    Pair<List<String>, String> supportedFiles = new Pair<List<String>, String>( list, desc );
                    List<Pair<List<String>, String>> supportedOpenFiles = new ArrayList<Pair<List<String>, String>>();
                    supportedOpenFiles.add( supportedFiles );
                    FileChooser fileChooser = new FileChooser( supportedOpenFiles, state.conModel.getView(), true );
                    String fileChoosed = fileChooser.getOpenPath();
                    if ( fileChoosed != null ) {
                        state.model = new Scene2DImplShape( fileChoosed, state.conModel.getPanel().getG2() );
                        initGeoReferencingScene( state.model );
                    }

                } else if ( ( (JMenuItem) source ).getText().startsWith( get( "MENUITEM_OPEN_WMS_LAYER" ) ) ) {

                    state.wmsStartDialog = new OpenWMS( state.conModel.getView() );
                    state.wmsStartDialog.addListeners( new ButtonListener() );
                    state.wmsStartDialog.setVisible( true );

                }

            }

        }

        private void fireTextfieldJumperDialog() {
            try {
                state.textFieldModel.setTextInput( state.jumperDialog.getCoordinateJumper().getText() );

                if ( state.textFieldModel.getSpanX() != -1 && state.textFieldModel.getSpanY() != -1 ) {

                    state.sceneValues.setCentroidWorldEnvelopePosition( state.textFieldModel.getxCoordinate(),
                                                                        state.textFieldModel.getyCoordiante(),
                                                                        state.textFieldModel.getSpanX(),
                                                                        state.textFieldModel.getSpanY(),
                                                                        PointType.GeoreferencedPoint );

                } else {
                    state.sceneValues.setCentroidWorldEnvelopePosition( state.textFieldModel.getxCoordinate(),
                                                                        state.textFieldModel.getyCoordiante(),
                                                                        PointType.GeoreferencedPoint );

                }
                state.jumperDialog.setVisible( false );
                state.selectedGeoref = false;
                state.buttonModel.setSelected( false );
                state.isHorizontalRefGeoref = true;
                state.conModel.getPanel().setImageToDraw(
                                                          state.model.generateSubImageFromRaster( state.sceneValues.getEnvelopeGeoref() ) );
                state.conModel.getPanel().updatePoints( state.sceneValues );
                state.conModel.getPanel().repaint();

            } catch ( NumberFormatException e1 ) {
                new ErrorDialog( state.conModel.getView(), ImageObserver.ERROR, e1.getMessage() );
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
                for ( Triple<Point4Values, Point4Values, PointResidual> p : state.mappedPoints ) {
                    boolean changed = changePointLocation( p, data, row, column );
                    if ( changed ) {

                        updateDrawingPanels();

                    }
                }

                if ( row == e.getLastRow() ) {
                    Triple<Point4Values, Point4Values, PointResidual> newLastPair = new Triple<Point4Values, Point4Values, PointResidual>(
                                                                                                                                           state.conModel.getFootPanel().getLastAbstractPoint(),
                                                                                                                                           state.conModel.getPanel().getLastAbstractPoint(),
                                                                                                                                           null );
                    if ( state.conModel.getFootPanel().getLastAbstractPoint() != null
                         && state.conModel.getPanel().getLastAbstractPoint() != null ) {
                        boolean changed = changePointLocation( newLastPair, data, row, column );
                        if ( changed ) {

                            if ( state.conModel.getFootPanel().getLastAbstractPoint() != null
                                 && state.conModel.getPanel().getLastAbstractPoint() != null ) {
                                state.conModel.getPanel().setLastAbstractPoint( newLastPair.second.getNewValue(),
                                                                                newLastPair.second.getWorldCoords(),
                                                                                newLastPair.second.getRc() );
                                state.conModel.getFootPanel().setLastAbstractPoint( newLastPair.first.getNewValue(),
                                                                                    newLastPair.first.getWorldCoords(),
                                                                                    newLastPair.first.getRc() );
                            }
                            state.conModel.getPanel().repaint();
                            state.conModel.getFootPanel().repaint();

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
    boolean changePointLocation( Triple<Point4Values, Point4Values, PointResidual> p, Object data, int row, int column ) {
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
                                                  p.first.getWorldCoords().y );
                int[] i = state.sceneValues.getPixelCoord( worldCoords );
                pixelValue = new FootprintPoint( i[0], i[1] );

                p.first = new Point4Values( pixelValue, pixelValue, pixelValue, worldCoords, p.first.getRc() );
                changed = true;
            } else if ( column == fcpy ) {
                worldCoords = new FootprintPoint( p.first.getWorldCoords().x,
                                                  new Double( data.toString() ).doubleValue() );
                int[] i = state.sceneValues.getPixelCoord( worldCoords );
                pixelValue = new FootprintPoint( i[0], i[1] );
                p.first = new Point4Values( pixelValue, pixelValue, pixelValue, worldCoords, p.first.getRc() );
                changed = true;
            } else if ( column == scpx ) {
                worldCoords = new GeoReferencedPoint( new Double( data.toString() ).doubleValue(),
                                                      p.second.getWorldCoords().y );
                int[] i = state.sceneValues.getPixelCoord( worldCoords );
                pixelValue = new GeoReferencedPoint( i[0], i[1] );

                p.second = new Point4Values( pixelValue, pixelValue, pixelValue, worldCoords, p.second.getRc() );
                changed = true;
            } else if ( column == scpy ) {
                worldCoords = new GeoReferencedPoint( p.second.getWorldCoords().x,
                                                      new Double( data.toString() ).doubleValue() );
                int[] i = state.sceneValues.getPixelCoord( worldCoords );
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
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) state.optionNavPanel.getTree().getLastSelectedPathComponent();

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
                        state.optionSettingPanel = new GeneralPanel( state.optionSettPanel );
                        ( (GeneralPanel) state.optionSettingPanel ).addCheckboxListener( new ButtonListener() );
                        ( (GeneralPanel) state.optionSettingPanel ).setSnappingOnOff( state.conModel.getDialogModel().getSnappingOnOff().second );
                        ( (GeneralPanel) state.optionSettingPanel ).setInitialZoomValue( state.conModel.getDialogModel().getResizeValue().second );
                        break;
                    case ViewPanel:
                        state.optionSettingPanel = new ViewPanel( new ButtonListener() );
                        ( (ViewPanel) state.optionSettingPanel ).getTbm().setPointSize(
                                                                                        state.conModel.getDialogModel().getSelectionPointSize().second );
                        // ( (ViewPanel) state.optionSettingPanel ).addRadioButtonListener( new ButtonListener() );

                        break;
                    }
                    state.optionSettPanel.setCurrentPanel( state.optionSettingPanel );
                    state.optionDialog.setSettingsPanel( state.optionSettPanel );

                } else {
                    state.optionSettPanel.reset();
                    state.optionDialog.reset();
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
    class Scene2DMouseListener extends MouseAdapter {

        @Override
        public void mouseEntered( MouseEvent m ) {
            Object source = m.getSource();
            if ( source instanceof JPanel ) {
                // Scene2DPanel
                if ( ( (JPanel) source ).getName().equals( Scene2DPanel.SCENE2D_PANEL_NAME ) ) {

                    state.mouseGeoRef.setMouseInside( true );

                }
                if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {

                    state.mouseFootprint.setMouseInside( true );

                }
            }
        }

        @Override
        public void mouseExited( MouseEvent m ) {
            Object source = m.getSource();
            if ( source instanceof JPanel ) {
                // Scene2DPanel
                if ( ( (JPanel) source ).getName().equals( Scene2DPanel.SCENE2D_PANEL_NAME ) ) {
                    state.mouseGeoRef.setMouseInside( false );

                }
                if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {
                    state.mouseFootprint.setMouseInside( false );

                }
            }
        }

        @Override
        public void mousePressed( MouseEvent m ) {
            Object source = m.getSource();
            if ( source instanceof JPanel ) {
                // Scene2DPanel
                if ( ( (JPanel) source ).getName().equals( Scene2DPanel.SCENE2D_PANEL_NAME ) ) {
                    state.mouseGeoRef.setPointMousePressed( new Point2d( m.getX(), m.getY() ) );
                    state.isControlDown = m.isControlDown();
                    state.isZoomInGeoref = state.buttonZoomInGeoref.isSelected();
                    state.isZoomOutGeoref = state.buttonZoomoutGeoref.isSelected();

                }
                if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {
                    state.mouseFootprint.setPointMousePressed( new Point2d( m.getX(), m.getY() ) );
                    state.isControlDown = m.isControlDown();
                    state.isZoomInFoot = state.buttonZoominFoot.isSelected();
                    state.isZoomOutFoot = state.buttonZoomoutFoot.isSelected();
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
                    if ( state.model != null ) {
                        if ( state.isControlDown || state.isZoomInGeoref || state.isZoomOutGeoref ) {
                            Point2d pointPressed = new Point2d( state.mouseGeoRef.getPointMousePressed().x,
                                                                state.mouseGeoRef.getPointMousePressed().y );
                            Point2d pointReleased = new Point2d( m.getX(), m.getY() );
                            Point2d minPoint;
                            Point2d maxPoint;
                            if ( pointPressed.x < pointReleased.x ) {
                                minPoint = pointPressed;
                                maxPoint = pointReleased;
                            } else {
                                minPoint = pointReleased;
                                maxPoint = pointPressed;
                            }

                            if ( state.isZoomInGeoref ) {
                                if ( minPoint.x == maxPoint.x && minPoint.y == maxPoint.y ) {
                                    state.sceneValues.computeZoomedEnvelope(
                                                                             true,
                                                                             state.conModel.getDialogModel().getResizeValue().second,
                                                                             new GeoReferencedPoint( minPoint.x,
                                                                                                     minPoint.y ) );
                                } else {
                                    Rectangle r = new Rectangle(
                                                                 new Double( minPoint.x ).intValue(),
                                                                 new Double( minPoint.y ).intValue(),
                                                                 Math.abs( new Double( maxPoint.x - minPoint.x ).intValue() ),
                                                                 Math.abs( new Double( maxPoint.y - minPoint.y ).intValue() ) );

                                    state.sceneValues.createZoomedEnvWithMinPoint( PointType.GeoreferencedPoint, r );

                                }
                            } else if ( state.isZoomOutGeoref ) {
                                state.sceneValues.computeZoomedEnvelope(
                                                                         false,
                                                                         state.conModel.getDialogModel().getResizeValue().second,
                                                                         new GeoReferencedPoint( maxPoint.x, maxPoint.y ) );
                            }

                            state.conModel.getPanel().setImageToDraw(
                                                                      state.model.generateSubImageFromRaster( state.sceneValues.getEnvelopeGeoref() ) );
                            state.conModel.getPanel().updatePoints( state.sceneValues );
                            state.conModel.getPanel().setZoomRect( null );
                            state.conModel.getPanel().repaint();
                        }

                        else {
                            if ( state.isHorizontalRefGeoref == true ) {
                                if ( state.start == false ) {
                                    state.start = true;
                                    state.conModel.getFootPanel().setFocus( false );
                                    state.conModel.getPanel().setFocus( true );
                                }
                                if ( state.conModel.getFootPanel().getLastAbstractPoint() != null
                                     && state.conModel.getPanel().getLastAbstractPoint() != null
                                     && state.conModel.getPanel().getFocus() == true ) {
                                    setValues();
                                }
                                if ( state.conModel.getFootPanel().getLastAbstractPoint() == null
                                     && state.conModel.getPanel().getLastAbstractPoint() == null
                                     && state.conModel.getPanel().getFocus() == true ) {
                                    state.tablePanel.addRow();
                                    isFirstNumber = true;
                                }

                                double x = m.getX();
                                double y = m.getY();
                                GeoReferencedPoint geoReferencedPoint = new GeoReferencedPoint( x, y );
                                GeoReferencedPoint g = (GeoReferencedPoint) state.sceneValues.getWorldPoint( geoReferencedPoint );
                                state.rc = state.tablePanel.setCoords( g );
                                state.conModel.getPanel().setLastAbstractPoint( geoReferencedPoint, g, state.rc );
                                if ( isFirstNumber == false ) {
                                    updateResidualsWithLastAbstractPoint();
                                }
                            } else {
                                // just pan
                                state.mouseGeoRef.setMouseChanging( new GeoReferencedPoint(
                                                                                            ( state.mouseGeoRef.getPointMousePressed().x - m.getX() ),
                                                                                            ( state.mouseGeoRef.getPointMousePressed().y - m.getY() ) ) );

                                state.sceneValues.moveEnvelope( state.mouseGeoRef.getMouseChanging() );
                                state.conModel.getPanel().setImageToDraw(
                                                                          state.model.generateSubImageFromRaster( state.sceneValues.getEnvelopeGeoref() ) );
                                state.conModel.getPanel().updatePoints( state.sceneValues );
                            }

                            state.conModel.getPanel().repaint();
                        }

                    }
                }
                // footprintPanel
                if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {

                    if ( state.isControlDown || state.isZoomInFoot || state.isZoomOutFoot ) {
                        Point2d pointPressed = new Point2d( state.mouseFootprint.getPointMousePressed().x,
                                                            state.mouseFootprint.getPointMousePressed().y );
                        Point2d pointReleased = new Point2d( m.getX(), m.getY() );
                        Point2d minPoint;
                        Point2d maxPoint;
                        if ( pointPressed.x < pointReleased.x ) {
                            minPoint = pointPressed;
                            maxPoint = pointReleased;
                        } else {
                            minPoint = pointReleased;
                            maxPoint = pointPressed;
                        }

                        if ( state.isZoomInFoot ) {
                            if ( minPoint.x == maxPoint.x && minPoint.y == maxPoint.y ) {
                                state.sceneValues.computeZoomedEnvelope(
                                                                         true,
                                                                         state.conModel.getDialogModel().getResizeValue().second,
                                                                         new FootprintPoint( minPoint.x, minPoint.y ) );
                            } else {
                                Rectangle r = new Rectangle(
                                                             new Double( minPoint.x ).intValue(),
                                                             new Double( minPoint.y ).intValue(),
                                                             Math.abs( new Double( maxPoint.x - minPoint.x ).intValue() ),
                                                             Math.abs( new Double( maxPoint.y - minPoint.y ).intValue() ) );

                                state.sceneValues.createZoomedEnvWithMinPoint( PointType.FootprintPoint, r );
                            }
                        } else if ( state.isZoomOutFoot ) {
                            state.sceneValues.computeZoomedEnvelope(
                                                                     false,
                                                                     state.conModel.getDialogModel().getResizeValue().second,
                                                                     new FootprintPoint( maxPoint.x, maxPoint.y ) );
                        }
                        state.conModel.getFootPanel().setZoomRect( null );
                        state.conModel.getFootPanel().updatePoints( state.sceneValues );
                        state.conModel.getFootPanel().repaint();

                    } else {
                        if ( state.isHorizontalRefFoot == true ) {

                            if ( state.start == false ) {
                                state.start = true;
                                state.conModel.getFootPanel().setFocus( true );
                                state.conModel.getPanel().setFocus( false );
                            }
                            if ( state.conModel.getFootPanel().getLastAbstractPoint() != null
                                 && state.conModel.getPanel().getLastAbstractPoint() != null
                                 && state.conModel.getFootPanel().getFocus() == true ) {
                                setValues();
                            }
                            if ( state.conModel.getFootPanel().getLastAbstractPoint() == null
                                 && state.conModel.getPanel().getLastAbstractPoint() == null
                                 && state.conModel.getFootPanel().getFocus() == true ) {
                                state.tablePanel.addRow();
                                isFirstNumber = true;
                            }
                            double x = m.getX();
                            double y = m.getY();
                            Pair<AbstractGRPoint, FootprintPoint> point = null;
                            if ( state.conModel.getDialogModel().getSnappingOnOff().first ) {
                                point = state.conModel.getFootPanel().getClosestPoint( new FootprintPoint( x, y ) );
                            } else {
                                point = new Pair<AbstractGRPoint, FootprintPoint>(
                                                                                   new FootprintPoint( x, y ),
                                                                                   (FootprintPoint) state.sceneValues.getWorldPoint( new FootprintPoint(
                                                                                                                                                         x,
                                                                                                                                                         y ) ) );
                            }
                            state.rc = state.tablePanel.setCoords( point.second );
                            state.conModel.getFootPanel().setLastAbstractPoint( point.first, point.second, state.rc );
                            if ( isFirstNumber == false ) {
                                updateResidualsWithLastAbstractPoint();
                            }

                        } else {
                            state.mouseFootprint.setMouseChanging( new FootprintPoint(
                                                                                       ( state.mouseFootprint.getPointMousePressed().x - m.getX() ),
                                                                                       ( state.mouseFootprint.getPointMousePressed().y - m.getY() ) ) );

                            state.sceneValues.moveEnvelope( state.mouseFootprint.getMouseChanging() );
                            state.conModel.getFootPanel().updatePoints( state.sceneValues );
                        }
                        state.conModel.getFootPanel().repaint();
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

        @Override
        public void run() {

            state.changePoint = new Point2d( changing.x, changing.y );

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
                    if ( m.isControlDown() || state.isZoomInGeoref ) {
                        int x = new Double( state.mouseGeoRef.getPointMousePressed().x ).intValue();
                        int y = new Double( state.mouseGeoRef.getPointMousePressed().y ).intValue();
                        int width = new Double( m.getX() - state.mouseGeoRef.getPointMousePressed().x ).intValue();
                        int height = new Double( m.getY() - state.mouseGeoRef.getPointMousePressed().y ).intValue();
                        Rectangle rec = new Rectangle( x, y, width, height );
                        state.conModel.getPanel().setZoomRect( rec );
                        state.conModel.getPanel().repaint();
                    }
                }
                // footprintPanel
                if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {
                    if ( m.isControlDown() || state.isZoomInFoot ) {
                        int x = new Double( state.mouseFootprint.getPointMousePressed().x ).intValue();
                        int y = new Double( state.mouseFootprint.getPointMousePressed().y ).intValue();
                        int width = new Double( m.getX() - state.mouseFootprint.getPointMousePressed().x ).intValue();
                        int height = new Double( m.getY() - state.mouseFootprint.getPointMousePressed().y ).intValue();
                        Rectangle rec = new Rectangle( x, y, width, height );
                        state.conModel.getFootPanel().setZoomRect( rec );
                        state.conModel.getFootPanel().repaint();
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
                    if ( state.mouseGeoRef != null ) {
                        state.mouseGeoRef.setMouseMoved( new GeoReferencedPoint( m.getX(), m.getY() ) );
                    }
                }
                // footprintPanel
                if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {
                    // System.out.println( m.getPoint() );
                    if ( state.mouseFootprint != null ) {
                        state.mouseFootprint.setMouseMoved( new FootprintPoint( m.getX(), m.getY() ) );
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
                    if ( state.model != null ) {
                        mouseOver = state.mouseGeoRef.getMouseMoved();
                        // resizing = .05f;
                        if ( m.getWheelRotation() < 0 ) {
                            zoomIn = true;
                        } else {
                            zoomIn = false;
                        }
                        state.sceneValues.computeZoomedEnvelope(
                                                                 zoomIn,
                                                                 state.conModel.getDialogModel().getResizeValue().second,
                                                                 mouseOver );
                        state.conModel.getPanel().setImageToDraw(
                                                                  state.model.generateSubImageFromRaster( state.sceneValues.getEnvelopeGeoref() ) );
                        state.conModel.getPanel().updatePoints( state.sceneValues );
                        state.conModel.getPanel().repaint();
                    }
                }
                // footprintPanel
                if ( ( (JPanel) source ).getName().equals( BuildingFootprintPanel.BUILDINGFOOTPRINT_PANEL_NAME ) ) {

                    // resizing = .1f;
                    mouseOver = state.mouseFootprint.getMouseMoved();
                    if ( m.getWheelRotation() < 0 ) {
                        zoomIn = true;
                    } else {
                        zoomIn = false;
                    }
                    state.sceneValues.computeZoomedEnvelope( zoomIn,
                                                             state.conModel.getDialogModel().getResizeValue().second,
                                                             mouseOver );
                    state.conModel.getFootPanel().updatePoints( state.sceneValues );
                    state.conModel.getFootPanel().repaint();
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
                if ( state.model != null && state.model.getGeneratedImage() != null ) {
                    init();

                    if ( state.sceneValues != null ) {

                        state.sceneValues.setDimensionFootpanel( new Rectangle(
                                                                                state.conModel.getFootPanel().getBounds().width,
                                                                                state.conModel.getFootPanel().getBounds().height ) );
                        state.conModel.getFootPanel().updatePoints( state.sceneValues );
                        state.conModel.getFootPanel().repaint();
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
    void init() {

        if ( state.model != null ) {
            state.sceneValues.setGeorefDimension( new Rectangle( state.conModel.getPanel().getWidth(),
                                                                 state.conModel.getPanel().getHeight() ) );
            state.conModel.getPanel().setImageDimension( state.sceneValues.getGeorefDimension() );
            state.conModel.getPanel().setImageToDraw(
                                                      state.model.generateSubImage( state.sceneValues.getGeorefDimension() ) );
            state.conModel.getPanel().updatePoints( state.sceneValues );
            state.conModel.getPanel().repaint();
        }

    }

    /**
     * Sets values to the JTableModel.
     */
    void setValues() {
        state.conModel.getFootPanel().addToSelectedPoints( state.conModel.getFootPanel().getLastAbstractPoint() );
        state.conModel.getPanel().addToSelectedPoints( state.conModel.getPanel().getLastAbstractPoint() );
        if ( state.mappedPoints != null && state.mappedPoints.size() >= 1 ) {
            addToMappedPoints( state.conModel.getFootPanel().getLastAbstractPoint(),
                               state.conModel.getPanel().getLastAbstractPoint(), null );
            updateResiduals( state.conModel.getTransformationType() );
        } else {
            addToMappedPoints( state.conModel.getFootPanel().getLastAbstractPoint(),
                               state.conModel.getPanel().getLastAbstractPoint(), null );
        }
        state.conModel.getFootPanel().setLastAbstractPoint( null, null, null );
        state.conModel.getPanel().setLastAbstractPoint( null, null, null );

    }

    /**
     * Determines the transformationMethod by means of the type.
     * 
     * @param type
     *            of the transformationMethod, not <Code>null</Code>.
     * @return the transformationMethod to be used.
     * @throws UnknownCRSException
     */
    AbstractTransformation determineTransformationType( AbstractTransformation.TransformationType type )
                            throws UnknownCRSException {
        AbstractTransformation t = null;
        switch ( type ) {
        case Polynomial:

            t = new Polynomial( state.mappedPoints, footPrint, state.sceneValues, sourceCRS, targetCRS,
                                state.conModel.getOrder() );

            break;
        case Helmert_4:
            t = new Helmert4Transform( state.mappedPoints, footPrint, state.sceneValues, targetCRS,
                                       state.conModel.getOrder() );
            break;

        case Affine:
            t = new AffineTransformation( state.mappedPoints, footPrint, state.sceneValues, sourceCRS, targetCRS,
                                          state.conModel.getOrder() );
            break;
        }

        return t;
    }

    /**
     * Updates the model of the table to show the residuals of the already stored state.mappedPoints. It is based on the
     * Helmert transformation.
     * 
     * @param type
     * 
     */
    void updateResiduals( AbstractTransformation.TransformationType type ) {

        try {
            AbstractTransformation t = determineTransformationType( type );
            PointResidual[] r = t.calculateResiduals();
            if ( r != null ) {
                Vector<Vector<? extends Double>> data = new Vector<Vector<? extends Double>>();
                int counter = 0;
                for ( Triple<Point4Values, Point4Values, PointResidual> point : state.mappedPoints ) {
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
                state.tablePanel.getModel().setDataVector( data, state.tablePanel.getColumnNamesAsVector() );
                state.tablePanel.getModel().fireTableDataChanged();
            }
        } catch ( UnknownCRSException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    void updateResidualsWithLastAbstractPoint() {
        if ( state.conModel.getFootPanel().getLastAbstractPoint() != null
             && state.conModel.getPanel().getLastAbstractPoint() != null ) {
            state.mappedPoints.add( new Triple<Point4Values, Point4Values, PointResidual>(
                                                                                           state.conModel.getFootPanel().getLastAbstractPoint(),
                                                                                           state.conModel.getPanel().getLastAbstractPoint(),
                                                                                           null ) );
            updateMappedPoints();
            updateResiduals( state.conModel.getTransformationType() );

            // remove the last element...should be the before inserted value
            state.mappedPoints.remove( state.mappedPoints.size() - 1 );
        } else {
            updateMappedPoints();
            updateResiduals( state.conModel.getTransformationType() );
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
            this.state.mappedPoints.add( new Triple<Point4Values, Point4Values, PointResidual>( mappedPointKey,
                                                                                                mappedPointValue,
                                                                                                residual ) );
        }

    }

    /**
     * Removes sample points in panels and the table.
     * 
     * @param tableRows
     *            that should be removed, could be <Code>null</Code>
     */
    void removeFromMappedPoints( int[] tableRows ) {
        for ( int i = tableRows.length - 1; i >= 0; i-- ) {
            state.mappedPoints.remove( tableRows[i] );
        }

    }

    /**
     * Updates the rowNumber of the remained state.mappedPoints
     */
    private void updateMappedPoints() {

        List<Triple<Point4Values, Point4Values, PointResidual>> temp = new ArrayList<Triple<Point4Values, Point4Values, PointResidual>>();

        int counter = 0;
        for ( Triple<Point4Values, Point4Values, PointResidual> p : state.mappedPoints ) {
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
        state.mappedPoints.clear();
        state.mappedPoints.addAll( temp );
    }

    /**
     * Removes everything after a complete deletion of the points.
     */
    void removeAllFromMappedPoints() {
        state.mappedPoints = new ArrayList<Triple<Point4Values, Point4Values, PointResidual>>();
        state.tablePanel.removeAllRows();
        state.conModel.getPanel().removeAllFromSelectedPoints();
        state.conModel.getFootPanel().removeAllFromSelectedPoints();
        state.conModel.getFootPanel().setLastAbstractPoint( null, null, null );
        state.conModel.getPanel().setPolygonList( null, null );
        state.conModel.getPanel().setLastAbstractPoint( null, null, null );
        state.conModel.getPanel().repaint();
        state.conModel.getFootPanel().repaint();
        reset();

    }

    /**
     * Resets the focus of the panels and the startPanel.
     */
    void reset() {
        state.conModel.getPanel().setFocus( false );
        state.conModel.getFootPanel().setFocus( false );
        state.start = false;

    }

    public ControllerModel getConModel() {
        return state.conModel;
    }

}
