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

import static java.util.Collections.singleton;
import static org.deegree.tools.crs.georeferencing.i18n.Messages.get;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.utils.Pair;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.utils.XMLTransformer;
import org.deegree.remoteows.wms.RemoteWMSStore;
import org.deegree.remoteows.wms.RemoteWMSStore.LayerOptions;
import org.deegree.services.wms.MapService;
import org.deegree.services.wms.model.layers.FeatureLayer;
import org.deegree.services.wms.model.layers.Layer;
import org.deegree.services.wms.model.layers.RemoteWMSLayer;
import org.deegree.services.wms.utils.MapController;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.tools.crs.georeferencing.application.ApplicationState;
import org.deegree.tools.crs.georeferencing.application.TransformationPoints;
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
import org.deegree.tools.crs.georeferencing.model.dialog.OptionDialogModel;
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

    private static final Color[] colors = new Color[] { new Color( 255, 0, 0, 128 ), new Color( 255, 175, 175, 128 ),
                                                       new Color( 255, 200, 0, 128 ), new Color( 255, 255, 0, 128 ),
                                                       new Color( 0, 255, 0, 128 ), new Color( 255, 0, 255, 128 ),
                                                       new Color( 0, 255, 255, 128 ), new Color( 0, 0, 255, 128 ) };

    private static int colorIndex = 0;

    private boolean exceptionThrown = false;

    private ApplicationState state;

    public ButtonListener( ApplicationState state ) {
        this.state = state;
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        Object source = e.getSource();
        if ( source instanceof JToggleButton ) {
            if ( source instanceof JRadioButton ) {
                int pointSize = ( (ViewPanel) this.state.optionSettingPanel ).getTbm().getButtons().get( source );
                this.state.conModel.getDialogModel().setSelectionPointSize( pointSize );
            } else if ( source instanceof JCheckBox ) {
                JCheckBox selectedCheckbox = (JCheckBox) source;
                new JCheckboxHandler( selectedCheckbox, this.state.conModel, this.state.wmsParameter );
            }
        } else if ( source instanceof JButton ) {

            if ( ( (JButton) source ).getText().startsWith( PointTableFrame.BUTTON_DELETE_SELECTED ) ) {
                int[] tableRows = this.state.tablePanel.getTable().getSelectedRows();
                state.points.delete( tableRows );
            } else if ( ( (JButton) source ).getText().startsWith( PointTableFrame.LOAD_POINTTABLE ) ) {
                state.points.load();
            } else if ( ( (JButton) source ).getText().startsWith( PointTableFrame.SAVE_POINTTABLE ) ) {

                new FileOutputHandler( this.state.tablePanel );

            } else if ( ( (JButton) source ).getText().startsWith( PointTableFrame.BUTTON_DELETE_ALL ) ) {
                this.state.points.removeAll();
            } else if ( ( (JButton) source ).getText().startsWith( ButtonPanel.BUTTON_TEXT_CANCEL ) ) {
                if ( this.state.optionDialog != null && this.state.optionDialog.isVisible() == true ) {
                    this.state.conModel.getDialogModel().transferOldToNew();
                    AbstractPanel2D.selectedPointSize = this.state.conModel.getDialogModel().getSelectionPointSize().first;
                    this.state.conModel.getPanel().repaint();
                    this.state.conModel.getFootPanel().repaint();
                    this.state.optionDialog.setVisible( false );
                } else if ( this.state.wmsStartDialog != null && this.state.wmsStartDialog.isVisible() == true ) {
                    this.state.wmsStartDialog.setVisible( false );

                } else if ( this.state.wmsParameter != null && this.state.wmsParameter.isVisible() == true ) {
                    this.state.wmsParameter.setVisible( false );
                    this.state.wmsStartDialog.setVisible( true );
                }

            } else if ( ( (JButton) source ).getText().startsWith( ButtonPanel.BUTTON_TEXT_OK ) ) {
                if ( this.state.optionDialog != null && this.state.optionDialog.isVisible() == true ) {

                    if ( this.state.optionSettingPanel != null ) {

                        if ( this.state.optionSettingPanel instanceof GeneralPanel ) {
                            String p = GeneralPanel.getTextField( ( (GeneralPanel) this.state.optionSettingPanel ).getZoomValue() ).getText();
                            String p1 = p.replace( ',', '.' );
                            this.state.conModel.getDialogModel().setResizeValue( new Double( p1 ).doubleValue() );
                            this.exceptionThrown = false;
                        }
                    }
                    if ( this.exceptionThrown == false ) {
                        this.state.conModel.getDialogModel().transferNewToOld();
                        AbstractPanel2D.selectedPointSize = this.state.conModel.getDialogModel().getSelectionPointSize().first;
                        this.state.conModel.getPanel().repaint();
                        this.state.conModel.getFootPanel().repaint();
                        this.state.optionDialog.setVisible( false );
                    }
                } else if ( this.state.wmsStartDialog != null && this.state.wmsStartDialog.isVisible() == true ) {
                    String mapURLString = this.state.wmsStartDialog.getTextField().getText();
                    this.state.wmsStartDialog.setVisible( false );
                    try {
                        this.state.wmsParameter = new WMSParameterChooser( this.state.wmsStartDialog, mapURLString );
                        this.state.wmsParameter.addCheckBoxListener( new ButtonListener( this.state ) );
                    } catch ( MalformedURLException e1 ) {
                        new ErrorDialog( this.state.wmsStartDialog, ImageObserver.ERROR,
                                         "The requested URL is malformed! There is no response gotten from the server. " );
                        this.exceptionThrown = true;
                    } catch ( NullPointerException e2 ) {
                        new ErrorDialog( this.state.wmsStartDialog, ImageObserver.ERROR,
                                         "The requested URL is malformed! There is no response gotten from the server. " );
                        this.exceptionThrown = true;
                    }
                    if ( this.exceptionThrown == false ) {
                        this.state.wmsParameter.addListeners( new ButtonListener( this.state ) );
                        this.state.wmsParameter.setVisible( true );
                    }

                } else if ( this.state.wmsParameter != null && this.state.wmsParameter.isVisible() == true ) {

                    ICRS crs = this.state.wmsParameter.getCheckBoxSRS();
                    String layers = this.state.wmsParameter.getCheckBoxListAsString().toString();
                    List<String> layerList = this.state.wmsParameter.getCheckBoxListLayerText();
                    String format = this.state.wmsParameter.getCheckBoxFormatAsString().toString();

                    if ( layers == null || layers.length() == 0 ) {
                        new ErrorDialog( this.state.wmsParameter, ImageObserver.ERROR,
                                         "There is no Layer selected. Please selected at least one. " );
                    } else if ( format == null || format.equals( "" ) ) {
                        new ErrorDialog( this.state.wmsParameter, ImageObserver.ERROR, "There is no format selected. " );
                    } else if ( crs == null ) {
                        new ErrorDialog( this.state.wmsParameter, ImageObserver.ERROR, "There is no CRS selected. " );
                    } else {
                        Envelope env = this.state.wmsParameter.getEnvelope( crs, layerList );
                        if ( env != null ) {

                            try {
                                env = new GeometryTransformer( crs ).transform( env );
                            } catch ( Throwable e1 ) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }

                            if ( this.state.service == null ) {
                                this.state.setupMapService();
                            }
                            Layer root = this.state.service.getRootLayer();
                            root.setSrs( Collections.singleton( env.getCoordinateSystem() ) );

                            HashMap<String, LayerOptions> layerMap = new HashMap<String, LayerOptions>();
                            for ( String l : layerList ) {
                                layerMap.put( l, new LayerOptions() );
                            }
                            RemoteWMSStore store = new RemoteWMSStore( this.state.wmsParameter.getWmsClient(),
                                                                       layerMap, layerList );
                            RemoteWMSLayer layer = new RemoteWMSLayer( this.state.service, store, "wms", "wms",
                                                                       this.state.service.getRootLayer() );
                            root.addOrReplace( layer );
                            Layer p = root.getChild( "points" );
                            root.getChildren().remove( p );
                            root.getChildren().addLast( p );
                            this.state.service.layers.put( "wms", layer );
                            root.setBbox( env );
                            MapService.fillInheritedInformation( root, new LinkedList<ICRS>( root.getSrs() ) );

                            this.state.mapController = new MapController( this.state.service,
                                                                          env.getCoordinateSystem(),
                                                                          this.state.conModel.getPanel().getWidth(),
                                                                          this.state.conModel.getPanel().getHeight() );
                            this.state.mapController.setLayers( new LinkedList<Layer>( root.getChildren() ) );

                            this.state.targetCRS = crs;
                            this.state.initGeoReferencingScene();
                            this.state.wmsParameter.setVisible( false );
                            if ( state.points == null ) {
                                state.points = new TransformationPoints( state );
                            }
                        } else {
                            new ErrorDialog( this.state.wmsParameter, ImageObserver.ERROR,
                                             "There is no Envelope for this request. " );
                        }
                    }

                }
            }
        } else if ( source instanceof JMenuItem ) {
            if ( ( (JMenuItem) source ).getText().startsWith( get( "MENUITEM_EDIT_OPTIONS" ) ) ) {
                DefaultMutableTreeNode root = new DefaultMutableTreeNode( "Options" );
                OptionDialogModel.createNodes( root );
                this.state.optionDialog = new OptionDialog( this.state.conModel.getView().getParent(), root );
                this.state.optionDialog.getButtonPanel().addListeners( new ButtonListener( this.state ) );
                this.state.optionNavPanel = this.state.optionDialog.getNavigationPanel();
                this.state.optionSettPanel = this.state.optionDialog.getSettingsPanel();

                // add the listener to the navigation panel
                this.state.optionNavPanel.addTreeListener( new NavigationTreeSelectionListener( this.state ) );

                this.state.optionDialog.setVisible( true );

            } else if ( ( (JMenuItem) source ).getText().startsWith( get( "MENUITEM_EXIT" ) ) ) {
                if ( state.systemExitOnClose ) {
                    System.exit( 0 );
                }
            } else if ( ( (JMenuItem) source ).getText().startsWith( get( "MENUITEM_SAVE_BUILDING" ) ) ) {
                List<String> list = new ArrayList<String>();
                list.add( "gml" );
                list.add( "xml" );
                String desc = "(*.gml, *.xml) GML or CityGML-Files";
                Pair<List<String>, String> supportedFiles = new Pair<List<String>, String>( list, desc );
                List<Pair<List<String>, String>> supportedOpenFiles = new ArrayList<Pair<List<String>, String>>();
                supportedOpenFiles.add( supportedFiles );
                FileChooser fileChooser = new FileChooser( supportedOpenFiles,
                                                           this.state.conModel.getView().getParent(), true );
                this.state.chosenFile = fileChooser.getOpenPath();
                fileChooser = new FileChooser( supportedOpenFiles, this.state.conModel.getView().getParent(), false );
                File saveFile = fileChooser.getSaveFile();
                if ( this.state.chosenFile != null && saveFile != null ) {
                    XMLStreamReader reader = null;
                    XMLStreamWriter writer = null;
                    try {
                        XMLInputFactory inFac = XMLInputFactory.newInstance();
                        reader = inFac.createXMLStreamReader( new File( this.state.chosenFile ).toURI().toURL().openStream() );

                        XMLOutputFactory outFac = XMLOutputFactory.newInstance();
                        writer = outFac.createXMLStreamWriter( new FileOutputStream( saveFile ) );

                        XMLTransformer transformer = new XMLTransformer( this.state.conModel.getTransform() );
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
                FileChooser fileChooser = new FileChooser( supportedOpenFiles,
                                                           this.state.conModel.getView().getParent(), true );
                this.state.chosenFile = fileChooser.getOpenPath();
                if ( this.state.chosenFile != null ) {
                    this.state.initFootprintScene( this.state.chosenFile );
                }
            } else if ( ( (JMenuItem) source ).getText().startsWith( get( "MENUITEM_OPEN_SHAPEFILE" ) ) ) {
                List<String> list = new ArrayList<String>();
                list.add( "shp" );

                String desc = "(*.shp) Esri ShapeFiles";
                Pair<List<String>, String> supportedFiles = new Pair<List<String>, String>( list, desc );
                List<Pair<List<String>, String>> supportedOpenFiles = new ArrayList<Pair<List<String>, String>>();
                supportedOpenFiles.add( supportedFiles );
                FileChooser fileChooser = new FileChooser( supportedOpenFiles,
                                                           this.state.conModel.getView().getParent(), true );
                String fileChoosed = fileChooser.getOpenPath();
                if ( fileChoosed != null ) {
                    if ( this.state.service == null ) {
                        state.setupMapService();
                    }

                    try {
                        if ( this.state.service.layers.get( "shape" ) != null ) {
                            this.state.service.layers.get( "shape" ).close();
                        }
                        Layer root = this.state.service.getRootLayer();
                        FeatureLayer layer = new FeatureLayer( this.state.service, "shape", "shape", root, fileChoosed );
                        root.addOrReplace( layer );
                        Layer p = root.getChild( "points" );
                        root.getChildren().remove( p );
                        root.getChildren().addLast( p );
                        this.state.service.layers.put( "shape", layer );
                        Envelope bbox = layer.getDataStore().getEnvelope( null );
                        root.setBbox( layer.getBbox() );
                        root.setSrs( singleton( bbox.getCoordinateSystem() ) );
                        this.state.service.registry.put( "shape", new Style( colors[colorIndex] ), true );
                        ++colorIndex;
                        if ( colorIndex == colors.length ) {
                            colorIndex = 0;
                        }
                        MapService.fillInheritedInformation( root, new LinkedList<ICRS>( root.getSrs() ) );

                        this.state.mapController = new MapController( this.state.service, bbox.getCoordinateSystem(),
                                                                      this.state.conModel.getPanel().getWidth(),
                                                                      this.state.conModel.getPanel().getHeight() );
                        this.state.mapController.setLayers( new LinkedList<Layer>( root.getChildren() ) );

                        this.state.targetCRS = bbox.getCoordinateSystem();
                        this.state.initGeoReferencingScene();
                        if ( state.points == null ) {
                            state.points = new TransformationPoints( state );
                        }
                    } catch ( Throwable e1 ) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                }

            } else if ( ( (JMenuItem) source ).getText().startsWith( get( "MENUITEM_OPEN_WMS_LAYER" ) ) ) {

                this.state.wmsStartDialog = new OpenWMS( this.state.conModel.getView().getParent() );
                this.state.wmsStartDialog.addListeners( new ButtonListener( this.state ) );
                this.state.wmsStartDialog.setVisible( true );

            }

        }

    }

}
