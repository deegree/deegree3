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
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.vecmath.Point2d;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.standard.points.PointsList;
import org.deegree.rendering.r3d.model.geometry.GeometryQualityModel;
import org.deegree.rendering.r3d.model.geometry.SimpleAccessGeometry;
import org.deegree.rendering.r3d.opengl.display.OpenGLEventHandler;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.WorldRenderableObject;
import org.deegree.services.jaxb.wms.RequestableLayer;
import org.deegree.services.wms.MapService;
import org.deegree.services.wms.model.layers.FeatureLayer;
import org.deegree.services.wms.model.layers.Layer;
import org.deegree.services.wms.utils.MapController;
import org.deegree.style.se.parser.SymbologyParser;
import org.deegree.style.se.unevaluated.Style;
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

    public ICRS sourceCRS, targetCRS;

    public Footprint footPrint;

    public OpenGLEventHandler glHandler;

    private GeometryFactory geom = new GeometryFactory();

    public MapController mapController;

    public MapService service;

    public ActionListener transformationListener;

    public boolean systemExitOnClose;

    public DeegreeWorkspace workspace = DeegreeWorkspace.getInstance();

    public TransformationPoints points;
    
    public ApplicationState() {
        try {
            workspace.initAll();
        } catch ( ResourceInitException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Initializes the georeferenced scene.
     */
    public void initGeoReferencingScene() {
        this.isInitGeoref = true;
        if ( this.isInitFoot ) {
            this.tablePanel.getSaveButton().setEnabled( true );
            this.tablePanel.getLoadButton().setEnabled( true );
        }

        this.mouseGeoRef = new GeoReferencedMouseModel();
        this.init();
        Controller.removeListeners( this.conModel.getPanel() );
        this.conModel.getPanel().addScene2DMouseListener( new Scene2DMouseListener( this ) );
        this.conModel.getPanel().addScene2DMouseMotionListener( new Scene2DMouseMotionListener( this ) );
        this.conModel.getPanel().addScene2DMouseWheelListener( new Scene2DMouseWheelListener( this ) );
    }

    public void setupMapService() {
        service = new MapService( workspace );
        try {
            XMLInputFactory fac = XMLInputFactory.newInstance();
            String styleUrl = ApplicationState.class.getResource( "/org/deegree/tools/crs/georeferencing/style.xml" ).toExternalForm();
            XMLStreamReader in = fac.createXMLStreamReader( new StreamSource( styleUrl ) );
            Style style = SymbologyParser.INSTANCE.parse( in );
            service.registry.putAsDefault( "points", style );
            RequestableLayer cfg = new RequestableLayer();
            cfg.setName( "points" );
            cfg.setTitle( "Points Layer" );
            cfg.setFeatureStoreId( "pointsstore" );
            FeatureLayer lay = new FeatureLayer( service, cfg, service.getRootLayer(), workspace );
            Layer root = service.getRootLayer();
            root.addOrReplace( lay );
            service.layers.put( "points", lay );
            MapService.fillInheritedInformation( root, new LinkedList<ICRS>( root.getSrs() ) );
            if ( mapController != null ) {
                mapController.setLayers( new LinkedList<Layer>( root.getChildren() ) );
            }
        } catch ( Throwable e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Initializes the computing and the painting of the maps.
     */
    void init() {
        if ( this.mapController != null ) {
            this.sceneValues.setGeorefDimension( new Rectangle( this.conModel.getPanel().getWidth(),
                                                                this.conModel.getPanel().getHeight() ) );
            this.conModel.getPanel().setImageDimension( this.sceneValues.getGeorefDimension() );
            this.conModel.getPanel().updatePoints( this.sceneValues );
            Component glassPane = ( (JFrame) this.conModel.getPanel().getTopLevelAncestor() ).getGlassPane();
            MouseAdapter mouseAdapter = new MouseAdapter() {
                // else the wait cursor will not appear
            };
            glassPane.addMouseListener( mouseAdapter );
            glassPane.setCursor( getPredefinedCursor( Cursor.WAIT_CURSOR ) );
            glassPane.setVisible( true );
            this.conModel.getPanel().repaint();
            glassPane.removeMouseListener( mouseAdapter );
            glassPane.setCursor( getPredefinedCursor( Cursor.DEFAULT_CURSOR ) );
            glassPane.setVisible( false );
        }
    }

    /**
     * Initializes the footprint scene.
     */
    public void initFootprintScene( String filePath ) {
        this.isInitFoot = true;
        if ( this.isInitGeoref ) {
            this.tablePanel.getSaveButton().setEnabled( true );
            this.tablePanel.getLoadButton().setEnabled( true );
        }

        this.footPrint = new Footprint( this.sceneValues, this.geom );
        Controller.removeListeners( this.conModel.getFootPanel() );
        this.conModel.getFootPanel().addScene2DMouseListener( new FootprintMouseListener( this ) );
        this.conModel.getFootPanel().addScene2DMouseMotionListener( new Scene2DMouseMotionListener( this ) );
        this.conModel.getFootPanel().addScene2DMouseWheelListener( new Scene2DMouseWheelListener( this ) );

        this.mouseFootprint = new FootprintMouseModel();
        List<WorldRenderableObject> rese = File3dImporter.open( this.conModel.getView().getParent(), filePath );
        this.sourceCRS = null;
        for ( WorldRenderableObject res : rese ) {
            this.sourceCRS = res.getBbox().getCoordinateSystem();
            this.glHandler.addDataObjectToScene( res );
        }
        List<float[]> geometryThatIsTaken = new ArrayList<float[]>();
        for ( GeometryQualityModel g : File3dImporter.gm ) {

            ArrayList<SimpleAccessGeometry> h = g.getQualityModelParts();

            for ( SimpleAccessGeometry b : h ) {
                float[] fs = b.getGeometry();
                List<Point> ps = new ArrayList<Point>();
                GeometryFactory fac = new GeometryFactory();
                for ( int i = 0; i < fs.length; i += 3 ) {
                    ps.add( fac.createPoint( null, fs[i], fs[i + 1], null ) );
                }
                Points pts = new PointsList( ps );

                // whyever I put the convex hull stuff in here, that was probably nonsense
                // MultiPoint mp = fac.createMultiPoint( null, null, ps );
                // Geometry hull = mp.getConvexHull();
                // Points pts;
                // if ( hull instanceof Curve ) {
                // pts = ( (Curve) hull ).getControlPoints();
                // } else {
                // pts = ( (Surface) hull ).getExteriorRingCoordinates();
                // }
                float[] a = new float[pts.size() * 2 + 2];
                int idx = 0;
                for ( Point p : pts ) {
                    a[idx++] = (float) p.get0();
                    a[idx++] = (float) p.get1();
                }
                a[idx++] = a[0];
                a[idx++] = a[1];

                geometryThatIsTaken.add( a );
            }

        }

        this.footPrint.generateFootprints( geometryThatIsTaken );

        this.sceneValues.setDimensionFootpanel( new Rectangle( this.conModel.getFootPanel().getBounds().width,
                                                               this.conModel.getFootPanel().getBounds().height ) );
        this.conModel.getFootPanel().updatePoints( this.sceneValues );

        this.conModel.getFootPanel().setPolygonList( this.footPrint.getWorldCoordinateRingList(), this.sceneValues );

        this.conModel.getFootPanel().repaint();

    }

    public void initFootprintSceneFromLinearRings( List<Ring> rings ) {
        this.isInitFoot = true;
        if ( this.isInitGeoref ) {
            this.tablePanel.getSaveButton().setEnabled( true );
            this.tablePanel.getLoadButton().setEnabled( true );
        }

        this.footPrint = new Footprint( this.sceneValues, this.geom );
        Controller.removeListeners( this.conModel.getFootPanel() );
        this.conModel.getFootPanel().addScene2DMouseListener( new FootprintMouseListener( this ) );
        this.conModel.getFootPanel().addScene2DMouseMotionListener( new Scene2DMouseMotionListener( this ) );
        this.conModel.getFootPanel().addScene2DMouseWheelListener( new Scene2DMouseWheelListener( this ) );

        this.mouseFootprint = new FootprintMouseModel();

        this.footPrint.generateFootprintsFromLinearRings( rings );

        this.sceneValues.setDimensionFootpanel( new Rectangle( this.conModel.getFootPanel().getBounds().width,
                                                               this.conModel.getFootPanel().getBounds().height ) );
        this.conModel.getFootPanel().updatePoints( this.sceneValues );

        this.conModel.getFootPanel().setPolygonList( this.footPrint.getWorldCoordinateRingList(), this.sceneValues );

        this.conModel.getFootPanel().repaint();

    }

    /**
     * Resets the focus of the panels and the startPanel.
     */
    public void reset() {
        this.conModel.getPanel().setFocus( false );
        this.conModel.getFootPanel().setFocus( false );
        this.start = false;

    }

    /**
     * Determines the transformationMethod by means of the type.
     * 
     * @param type
     *            of the transformationMethod, not <Code>null</Code>.
     * @return the transformationMethod to be used.
     */
    public AbstractTransformation determineTransformationType( AbstractTransformation.TransformationType type ) {
        AbstractTransformation t = null;
        if ( this.targetCRS == null ) {
            return null;
        }
        switch ( type ) {
        case Polynomial:
            t = new Polynomial( this.points, this.footPrint, this.sceneValues, this.targetCRS, this.targetCRS,
                                this.conModel.getOrder() );
            break;
        case Helmert_4:
            t = new Helmert4Transform( this.points, this.footPrint, this.sceneValues, this.targetCRS,
                                       this.conModel.getOrder() );
            break;
        case Affine:
            t = new AffineTransformation( this.points, this.footPrint, this.sceneValues, this.targetCRS,
                                          this.targetCRS, this.conModel.getOrder() );
            break;
        }

        return t;
    }

    /**
     * Updates the panels that are responsible for drawing the georeferenced points so that the once clicked points are
     * drawn into the right position.
     */
    public void updateDrawingPanels() {
        this.conModel.getPanel().repaint();
        this.conModel.getFootPanel().repaint();

    }

}
