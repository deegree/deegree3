//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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

package org.deegree.ogcwebservices.wpvs.j3d;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Light;
import javax.media.j3d.Locale;
import javax.media.j3d.Material;
import javax.media.j3d.OrderedGroup;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.MapUtils;
import org.deegree.i18n.Messages;

import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.MultiTransformGroup;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;

/**
 * The class provides the capabilitiy for rendering a <code>WPVSScene</code> to an offscreen graphic context that is
 * represent by a <code>BufferedImage</code>. That is, the returned BufferedImage of this class's renderScene method is
 * the response Object of a GetView request.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @version $Revision$ $Date$
 */
public class OffScreenWPVSRenderer extends Abstract3DRenderingEngine {

    private static ILogger LOG = LoggerFactory.getLogger( OffScreenWPVSRenderer.class );

    private Canvas3D offScreenCanvas3D;

    private SimpleUniverse universe;

    private final boolean renderAntialiased;

    /**
     * Initializes the render class with a default width and height (800x600) and a nearclipping plane of 2
     * 
     * @param canvas
     *            to render upon
     * 
     * @param scene
     *            to render
     */
    public OffScreenWPVSRenderer( Canvas3D canvas, WPVSScene scene ) {
        this( canvas, 2, scene, 800, 600, true );
    }

    /**
     * Initializes the renderer class with the submitted width and height and a nearclipping plane of 2
     * 
     * @param canvas
     *            to render upon
     * 
     * @param scene
     *            to render
     * @param width
     *            of the resulting image
     * @param height
     *            of the resulting image
     * @param antialiased
     *            true if the scene should be rendered antialiased
     */
    public OffScreenWPVSRenderer( Canvas3D canvas, WPVSScene scene, int width, int height, boolean antialiased ) {
        this( canvas, 2, scene, width, height, antialiased );
    }

    /**
     * Initializes the renderer class with the submitted width and height
     * 
     * @param canvas
     *            to render upon
     * @param nearClippingPlane
     *            of the scene's viewport.
     * 
     * @param scene
     *            to render
     * @param width
     *            of the resulting image
     * @param height
     *            of the resulting image
     * @param antialiased
     *            true if the scene should be rendered antialiased
     */
    public OffScreenWPVSRenderer( Canvas3D canvas, double nearClippingPlane, WPVSScene scene, int width, int height,
                                  boolean antialiased ) {
        super( scene, nearClippingPlane );

        // offScreenCanvas3D = WPVSConfiguration.getCanvas3D();
        offScreenCanvas3D = canvas;
        if ( offScreenCanvas3D == null ) {
            throw new IllegalArgumentException( "Did not retrieve a Canvas3D from the Configuraion, cannot proceed" );
        }
        offScreenCanvas3D.getScreen3D().setPhysicalScreenHeight( MapUtils.DEFAULT_PIXEL_SIZE * height );
        offScreenCanvas3D.getScreen3D().setPhysicalScreenWidth( MapUtils.DEFAULT_PIXEL_SIZE * width );

        offScreenCanvas3D.getScreen3D().setSize( width, height );
        BufferedImage renderedImage = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );

        ImageComponent2D imageComponent = new ImageComponent2D( ImageComponent.FORMAT_RGB, renderedImage );
        // new ImageComponent2D( ImageComponent.FORMAT_RGB, renderedImage );

        imageComponent.setCapability( ImageComponent.ALLOW_IMAGE_READ );

        offScreenCanvas3D.setOffScreenBuffer( imageComponent );

        universe = new SimpleUniverse( offScreenCanvas3D );

        OrderedGroup theScene = scene.getScene();
        BranchGroup sceneGroup = new BranchGroup();
        sceneGroup.addChild( theScene );

        addBackground( scene.getViewPoint(), sceneGroup, scene.getBackground() );

        sceneGroup.compile();
        BranchGroup lightGroup = new BranchGroup();
        Light[] lights = scene.getLights();
        for ( Light light : lights ) {
            lightGroup.addChild( light.cloneTree() );
        }
        lightGroup.compile();

        universe.addBranchGraph( sceneGroup );
        universe.addBranchGraph( lightGroup );
        this.renderAntialiased = antialiased;
    }

    /**
     * @param scene
     *            the Scene to render
     */
    public void setScene( WPVSScene scene ) {
        this.scene = scene;
    }

    /**
     * Renders the scene to a <code>BufferedImage</code>. This Method uses the viewmatrix of the the ViewPoint class,
     * which the defines the necessary viewing rotations and translations for the j3d scene.
     * 
     * @return a <code>BufferedImage</code> where the scene has been rendered to
     */
    public BufferedImage renderScene() {

        // setting up the transformation of the viewingplatform according to the requested
        // (calculated) values.
        ViewingPlatform vp = universe.getViewingPlatform();
        View view = universe.getViewer().getView();

        // view parameters
        view.setBackClipDistance( farClippingPlane );
        view.setFrontClipDistance( nearClippingPlane );
        view.setWindowEyepointPolicy( View.RELATIVE_TO_FIELD_OF_VIEW );
        view.setFieldOfView( scene.getViewPoint().getAngleOfView() );
        view.setSceneAntialiasingEnable( renderAntialiased );
        // view.setTransparencySortingPolicy( View.TRANSPARENCY_SORT_GEOMETRY );
        // view.setDepthBufferFreezeTransparent( false );

        MultiTransformGroup mtg = vp.getMultiTransformGroup();
        int transforms = mtg.getNumTransforms();
        TransformGroup transGroup = null;
        // if one or more transformgroups are present take the first one will be set to the
        // viewmatrix of the ViewPoint
        if ( transforms > 0 ) {
            transGroup = mtg.getTransformGroup( 0 );
        }
        if ( transGroup == null ) {
            LOG.logError( Messages.getMessage( "WPVS_NO_TRANSFORMS_ERROR" ) );
            return null;
        }
        Transform3D viewMatrix = scene.getViewPoint().getViewMatrix();
        transGroup.setTransform( viewMatrix );

        // draw the poi as a sphere if the Debug level is on.
        BranchGroup sphere = new BranchGroup();
        sphere.setCapability( BranchGroup.ALLOW_DETACH );
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            Appearance app = new Appearance();
            RenderingAttributes ra = new RenderingAttributes();
            ra.setDepthBufferEnable( true );
            app.setRenderingAttributes( ra );
            Material material = new Material();
            material.setAmbientColor( new Color3f( Color.WHITE ) );
            material.setDiffuseColor( new Color3f( Color.WHITE ) );
            app.setMaterial( material );
            Sphere s = new Sphere( 20, app );
            Transform3D t3d = new Transform3D();
            t3d.setTranslation( new Vector3d( scene.getViewPoint().getPointOfInterest() ) );
            TransformGroup tg = new TransformGroup();
            tg.setTransform( t3d );
            tg.addChild( s );
            sphere.addChild( tg );
            universe.addBranchGraph( sphere );
        }

        // Finally draw the scene
        view.startView();
        offScreenCanvas3D.renderOffScreenBuffer();
        offScreenCanvas3D.waitForOffScreenRendering();
        view.stopView();
        // removing the sphere out of the universe
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            Locale l = universe.getLocale();
            l.removeBranchGraph( sphere );
        }

        ImageComponent2D imageComponent = offScreenCanvas3D.getOffScreenBuffer();
        if ( imageComponent == null ) {
            LOG.logError( Messages.getMessage( "WPVS_NO_IMAGE_COMPONENT_ERROR" ) );
            return null;
        }
        /**
         * If using the TestWPVS class option NO_NEW_REQUEST, make sure to comment following two lines as well as the
         * WPVSConfiguration.releaseCanvas3D( canvas ); line in the DefaultGetViewHandler#handleRequest.
         */
        // and cleaning the universe and all of its used memory.
        universe.cleanup();
        view.removeAllCanvas3Ds();
        return imageComponent.getImage();
    }

    /**
     * Called to render the scene into the offscreen Canvas3D
     * 
     * @param offScreenCanvas3D
     *            to be rendered into
     * @return a buffered image as a result of the Rendering.
     */
    protected RenderedImage getImage( Canvas3D offScreenCanvas3D ) {

        offScreenCanvas3D.renderOffScreenBuffer();
        offScreenCanvas3D.waitForOffScreenRendering();

        ImageComponent2D imageComponent = offScreenCanvas3D.getOffScreenBuffer();

        return imageComponent.getImage();
    }
}
