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

import static java.lang.Math.max;
import static org.deegree.tools.crs.georeferencing.i18n.Messages.get;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.tree.DefaultMutableTreeNode;
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
import org.deegree.geometry.primitive.Ring;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.XMLTransformer;
import org.deegree.tools.crs.georeferencing.application.ApplicationState;
import org.deegree.tools.crs.georeferencing.application.ParameterStore;
import org.deegree.tools.crs.georeferencing.application.handler.FileInputHandler;
import org.deegree.tools.crs.georeferencing.application.handler.FileOutputHandler;
import org.deegree.tools.crs.georeferencing.application.handler.JCheckboxHandler;
import org.deegree.tools.crs.georeferencing.communication.FileChooser;
import org.deegree.tools.crs.georeferencing.communication.PointTableFrame;
import org.deegree.tools.crs.georeferencing.communication.dialog.ButtonPanel;
import org.deegree.tools.crs.georeferencing.communication.dialog.error.ErrorDialog;
import org.deegree.tools.crs.georeferencing.communication.dialog.menuitem.OpenWMS;
import org.deegree.tools.crs.georeferencing.communication.dialog.menuitem.WMSParameterChooser;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.GeneralPanel;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.OptionDialog;
import org.deegree.tools.crs.georeferencing.communication.dialog.option.ViewPanel;
import org.deegree.tools.crs.georeferencing.communication.panel2D.AbstractPanel2D;
import org.deegree.tools.crs.georeferencing.model.Scene2DImplShape;
import org.deegree.tools.crs.georeferencing.model.Scene2DImplWMS;
import org.deegree.tools.crs.georeferencing.model.datatransformer.VectorTransformer;
import org.deegree.tools.crs.georeferencing.model.points.Point4Values;
import org.deegree.tools.crs.georeferencing.model.points.PointResidual;
import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint.PointType;
import org.slf4j.Logger;

/**
 * 
 * Controls the ActionListener
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ButtonListener implements ActionListener {

    private static final Logger LOG = getLogger( ButtonListener.class );

    private boolean exceptionThrown = false;

    private ApplicationState state;

    public ButtonListener( ApplicationState state ) {
        this.state = state;
    }

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
                        state.selectGeorefToggleButton( tb );
                        state.isZoomInGeoref = false;
                        state.isZoomOutGeoref = false;
                    } else {
                        state.selectFootprintToggleButton( tb );
                        state.isZoomInFoot = false;
                        state.isZoomOutFoot = false;
                    }
                } else if ( tb.getName().startsWith( get( "JBUTTON_ZOOM_COORD" ) ) ) {

                    if ( tb == state.buttonCoord ) {
                        state.selectGeorefToggleButton( tb );
                    } else {
                        state.selectFootprintToggleButton( tb );
                    }
                } else if ( tb.getName().startsWith( get( "JBUTTON_ZOOM_IN" ) ) ) {

                    if ( tb == state.buttonZoomInGeoref ) {
                        state.selectGeorefToggleButton( tb );
                        state.isZoomInGeoref = true;
                        state.isZoomOutGeoref = false;
                    } else {
                        state.selectFootprintToggleButton( tb );
                        state.isZoomInFoot = true;
                        state.isZoomOutFoot = false;
                    }
                } else if ( tb.getName().startsWith( get( "JBUTTON_ZOOM_OUT" ) ) ) {

                    if ( tb == state.buttonZoomoutGeoref ) {
                        state.selectGeorefToggleButton( tb );
                        state.isZoomInGeoref = false;
                        state.isZoomOutGeoref = true;
                    } else {
                        state.selectFootprintToggleButton( tb );
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
                    state.removeFromMappedPoints( temp );
                }
                state.updateResidualsWithLastAbstractPoint();
                state.updateDrawingPanels();
            } else if ( ( (JButton) source ).getText().startsWith( PointTableFrame.LOAD_POINTTABLE ) ) {

                FileInputHandler in = new FileInputHandler( state.tablePanel );
                if ( in.getData() != null ) {
                    VectorTransformer vt = new VectorTransformer( in.getData(), state.sceneValues );
                    state.mappedPoints.clear();
                    state.mappedPoints.addAll( vt.getMappedPoints() );
                    state.updateDrawingPanels();
                }

            } else if ( ( (JButton) source ).getText().startsWith( PointTableFrame.SAVE_POINTTABLE ) ) {

                new FileOutputHandler( state.tablePanel );

            } else if ( ( (JButton) source ).getText().startsWith( PointTableFrame.BUTTON_DELETE_ALL ) ) {
                state.removeAllFromMappedPoints();

            } else if ( ( (JButton) source ).getText().startsWith( get( "RESET_VIEW_BUTTON_TEXT" ) ) ) {

                state.initGeoReferencingScene( state.model );
                if ( state.chosenFile != null ) {
                    state.initFootprintScene( state.chosenFile );

                    state.conModel.getFootPanel().updatePoints( state.sceneValues );
                    state.conModel.getFootPanel().repaint();
                }
                state.conModel.getPanel().updatePoints( state.sceneValues );
                state.conModel.getPanel().repaint();

            } else if ( ( (JButton) source ).getText().startsWith( get( "COMPUTE_BUTTON_TEXT" ) ) ) {
                // swap the tempPoints into the map now
                if ( state.conModel.getFootPanel().getLastAbstractPoint() != null
                     && state.conModel.getPanel().getLastAbstractPoint() != null ) {
                    state.setValues();
                }

                try {
                    state.conModel.setTransform( state.determineTransformationType( state.conModel.getTransformationType() ) );
                } catch ( UnknownCRSException e1 ) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                List<Ring> polygonRing = state.conModel.getTransform().computeRingList();

                state.updateResiduals( state.conModel.getTransformationType() );

                state.conModel.getPanel().setPolygonList( polygonRing, state.sceneValues );

                state.conModel.getPanel().repaint();

                state.reset();
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
                        state.wmsParameter.addCheckBoxListener( new ButtonListener( state ) );
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
                        state.wmsParameter.addListeners( new ButtonListener( state ) );
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
                            int qor = max( state.conModel.getPanel().getWidth(), state.conModel.getPanel().getHeight() );
                            state.store = new ParameterStore( mapURL, env.getCoordinateSystem(), format, layers, env,
                                                              qor );
                            state.model = new Scene2DImplWMS( state.store, state.wmsParameter.getWmsClient() );
                            state.initGeoReferencingScene( state.model );
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
                state.optionDialog.getButtonPanel().addListeners( new ButtonListener( state ) );
                state.optionNavPanel = state.optionDialog.getNavigationPanel();
                state.optionSettPanel = state.optionDialog.getSettingsPanel();

                // add the listener to the navigation panel
                state.optionNavPanel.addTreeListener( new NavigationTreeSelectionListener( state ) );

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
                state.chosenFile = fileChooser.getOpenPath();
                fileChooser = new FileChooser( supportedOpenFiles, state.conModel.getView(), false );
                File saveFile = fileChooser.getSaveFile();
                if ( state.chosenFile != null && saveFile != null ) {
                    XMLStreamReader reader = null;
                    XMLStreamWriter writer = null;
                    try {
                        XMLInputFactory inFac = XMLInputFactory.newInstance();
                        reader = inFac.createXMLStreamReader( new File( state.chosenFile ).toURI().toURL().openStream() );

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
                state.chosenFile = fileChooser.getOpenPath();
                if ( state.chosenFile != null ) {
                    state.initFootprintScene( state.chosenFile );
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
                    state.initGeoReferencingScene( state.model );
                }

            } else if ( ( (JMenuItem) source ).getText().startsWith( get( "MENUITEM_OPEN_WMS_LAYER" ) ) ) {

                state.wmsStartDialog = new OpenWMS( state.conModel.getView() );
                state.wmsStartDialog.addListeners( new ButtonListener( state ) );
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
