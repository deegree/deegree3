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

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.Group;
import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Light;
import javax.media.j3d.Locale;
import javax.media.j3d.OrderedGroup;
import javax.media.j3d.View;
import javax.media.j3d.VirtualUniverse;
import javax.vecmath.Point3d;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;


/**
 * This class sill/shoud provide the ability to render a scence object (s.
 * com.sun.j3d.loaders.Scene) or a Canvas3D. It's currently used for testing.
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @version $Revision$ $Date$
 */
public class InteractiveWPVSRenderer extends Abstract3DRenderingEngine {

    private int width  = 800;

    private int height = 600;

    private Canvas3D offScreenCanvas3D;

    private View view;

    private boolean newSize;

    /**
     * initialzies the render class with a default width and height (801x601)
     * @param scene
     */
    public InteractiveWPVSRenderer(WPVSScene scene)
    {
        this( scene, 801, 601 );
    }

    /**
     * initialzies the render class with the submitted width and height
     * @param scene
     * @param width
     * @param height
     */
    public InteractiveWPVSRenderer(WPVSScene scene, int width, int height)
    {
        super( scene, 2 );
        this.width = width;
        this.height = height;
        view = new View();
    }

    /**
     * @param scene
     */
    public void setScene(WPVSScene scene) {
        this.scene = scene;
    }

    /**Create the VirtualUniverse for the application.
     * @return the VirtualUniverse for the application.
     */
    protected VirtualUniverse createVirtualUniverse() {
        return new VirtualUniverse();
    }

    /**
     * Simple utility method that creates a Locale for the
     * VirtualUniverse
     * @param u the universe
     * @return the Locale
     */
    protected Locale createLocale( VirtualUniverse u ) {
        return new Locale( u );
    }

    /**
     * returns the width of the offscreen rendering target
     * @return the width of the offscreen rendering target
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width the new width of the screen
     */
    public void setWidth( int width ) {
        newSize = true;
        this.width = width;
    }

    /**
     * returns the height of the offscreen rendering target
     * @return the height of the offscreen rendering target
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height the new Height of the screen
     */
    public void setHeight( int height ) {
        newSize = true;
        this.height = height;
    }

    /** renders the scene to an <code>BufferedImage</code>
     * @return a <code>BufferedImage</code> where the scene has been rendered to
     */
    public Object renderScene( ) {
        if ( newSize ) {
            view.removeCanvas3D( offScreenCanvas3D );

            offScreenCanvas3D = createOffscreenCanvas3D();

            newSize = false;
        }
        view.addCanvas3D( offScreenCanvas3D );
        view.startView();

        // The viewGroup contains nodes necessary for rendering, viewing, etc
        // View, ViewPlatform, Canvas3D, PhysBody, PhysEnviron, and Lights
        BranchGroup viewGroup = new BranchGroup();

        // The sceneGroup conatins obejcts of the scene graph
        BranchGroup sceneGroup = new BranchGroup();

        setView( view,  viewGroup );
//        createMouseBehaviours( viewGroup );

        OrderedGroup terrainGroup = new OrderedGroup();

        addBackground( scene.getViewPoint(), terrainGroup, scene.getBackground() );

        // add the lights to the view
        Light[] lights = scene.getLights();
        for (int i = 0; i < lights.length; i++) {
            viewGroup.addChild( lights[i] );
        }
        /*
        // add the terrain to the view
        Shape3D terrain[] = scene.getTerrain();
        for (int i = terrain.length-1; i >= 0; i--) {
            terrainGroup.addChild( terrain[i] );
        }
        sceneGroup.addChild( terrainGroup );

        // add the features to the view
        Group[] features = scene.getFeatures();
        for (int i = 0; i < features.length; i++) {
            sceneGroup.addChild( features[i] );
        }
        */
        sceneGroup.compile();

        viewGroup.compile();

        VirtualUniverse universe = createVirtualUniverse();
        Locale locale = createLocale( universe );

        locale.addBranchGraph(sceneGroup);
        locale.addBranchGraph(viewGroup);

        //BranchGroup mainGroup = new BranchGroup();
//        mainGroup.addChild( sceneGroup );
//        mainGroup.addChild( viewGroup );
//        mainGroup.addChild( bg[0] );

        return offScreenCanvas3D;
    }

    /**
     * creates and returns a canvas for offscreen rendering
     * @return a canvas for rendering
     */
    protected Canvas3D createOffscreenCanvas3D()
    {
        Canvas3D offScreenCanvas3D = createCanvas( false );

//        offScreenCanvas3D.getScreen3D().setSize( width, height );

        offScreenCanvas3D.getScreen3D().setPhysicalScreenHeight( 0.0254/90 * height );
        offScreenCanvas3D.getScreen3D().setPhysicalScreenWidth( 0.0254/90 * width );

        BufferedImage renderedImage =
            new BufferedImage( width, height, BufferedImage.TYPE_3BYTE_BGR );

        ImageComponent2D imageComponent =
            new ImageComponent2D( ImageComponent.FORMAT_RGB8, renderedImage );

        imageComponent.setCapability( ImageComponent.ALLOW_IMAGE_READ );

//        offScreenCanvas3D.setOffScreenBuffer( imageComponent );

        return offScreenCanvas3D;
    }

    @SuppressWarnings("unused")
    private void createMouseBehaviours( Group scene  ){
        Point3d origin = new Point3d(-2584400.880145242, 528.7904086212667, 5615449.9824785);
        BoundingSphere bounds = new BoundingSphere(origin, 250000);
//        TransformGroup viewTrans = vp.getViewPlatformTransform();

//      Create the rotate behavior node
        MouseRotate behavior1 = new MouseRotate(offScreenCanvas3D);
        scene.addChild(behavior1);
        behavior1.setSchedulingBounds(bounds);

        // Create the zoom behavior node
        MouseZoom behavior2 = new MouseZoom(offScreenCanvas3D);
        scene.addChild(behavior2);
        behavior2.setSchedulingBounds(bounds);

        // Create the translate behavior node
        MouseTranslate behavior3 = new MouseTranslate(offScreenCanvas3D);
        scene.addChild(behavior3);
        behavior3.setSchedulingBounds(bounds);
    }

    /**
     * Called to render the scene into the offscreen Canvas3D
     * @param offScreenCanvas3D the canvas to render into
     * @return the Rendered Image
     */
    protected RenderedImage getImage(Canvas3D offScreenCanvas3D)
    {
        offScreenCanvas3D.renderOffScreenBuffer();
        offScreenCanvas3D.waitForOffScreenRendering();

        ImageComponent2D imageComponent = offScreenCanvas3D.getOffScreenBuffer();

        return imageComponent.getImage();

    }

}
