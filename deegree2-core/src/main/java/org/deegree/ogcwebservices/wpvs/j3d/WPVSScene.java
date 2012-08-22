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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Light;
import javax.media.j3d.Node;
import javax.media.j3d.OrderedGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.ogcwebservices.wpvs.utils.SunLight;
import org.deegree.ogcwebservices.wpvs.utils.SunPosition;

/**
 * This class represents the basic class for creation of a 3D perspective views as specified in the
 * OGC Web Perpective View Service specification. A WPVS scene is defined by a scene model and a
 * date determining the light conditions. Additional elements are 3D or 2.5D-features that are
 * placed into the scene, atmospheric conditions influencing the light and visibility (e.g. fog,
 * rain etc., but currently not implemented) and additional light placed into the scene (e.g. street
 * lights, spots, lighted windows etc.).
 * <p>
 * -----------------------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:lupp@lat-lon.de">Katharina Lupp</a>
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 *
 * @author last edited by: $Author$
 * @version $Revision$ $Date$
 */
public class WPVSScene {

    private static ILogger LOG = LoggerFactory.getLogger( WPVSScene.class );

    private Calendar calendar;

    private OrderedGroup scene;

    private List<Light> lights;

    private ViewPoint viewPoint;

    private Node background;

    /**
     * Creates a new instance of WPVScene
     *
     * @param scene
     *            java3D representation of the scene.
     * @param viewPoint
     *            object that describes the viewers position and the looking direction
     * @param calendar
     *            describtion of the date and time for which the scene shall be rendered --> light
     *            conditions
     * @param lights
     *            lights in addition to sun and ambient light (e.g. street lights, spots etc.)
     * @param background
     *            scene background; have to be a <code>Shape3D</code> or a <code>Background</code>
     */
    public WPVSScene( OrderedGroup scene, ViewPoint viewPoint, Calendar calendar,
                     List<Light> lights, Node background ) {
        if ( lights != null ) {
            this.lights = lights;
        } else {
            this.lights = new ArrayList<Light>();
        }
        this.scene = scene;
        this.viewPoint = viewPoint;
        this.calendar = calendar;
        if( calendar == null ){
            LOG.logDebug( Messages.getMessage( "WPVS_STANDARD_TIME" ) );
            this.calendar = new GregorianCalendar(2007, 2, 21, 12, 00 );
        }
        this.background = background;
        createDayLight();
    }

    /**
     * creates the light that results from the sun (direct light) and the ambient of the sky.
     */
    private void createDayLight() {

        int latitute = 52;
        SunPosition sp = new SunPosition( calendar );
        SunLight sl = new SunLight( latitute, sp );
        Color3f sunlightColor = sl.calculateSunlight(  );
        double vPos = sp.getVerticalSunposition( latitute );
        double hPos = sp.getHorizontalSunPosition(  );

        Point3d p = getViewPoint().getObserverPosition();
        Point3d origin = new Point3d( p.x, p.y, p.z );
        BoundingSphere light_bounds = new BoundingSphere( origin, 250000 );

        // Directional Light: A DirectionalLight node defines an oriented light with an origin at
        // infinity.
        DirectionalLight headlight = new DirectionalLight();
        headlight.setInfluencingBounds( light_bounds );
        headlight.setColor( sunlightColor );
        headlight.setDirection( (float) Math.sin( hPos ), (float) Math.sin( vPos ), (float) -Math.abs( Math.cos( hPos ) ) );
        Vector3f tmp = new Vector3f();
        headlight.getDirection( tmp );
        lights.add( headlight );
        // Ambient Light: Ambient light is that light that seems to come from all directions.
        // Ambient light has only an ambient reflection component.
        // It does not have diffuse or specular reflection components.
        AmbientLight al = new AmbientLight();
        al.setInfluencingBounds( light_bounds );
        al.setColor( new Color3f( 0.7f * sunlightColor.x, 0.65f * sunlightColor.y, 0.6f * sunlightColor.z ) );

        lights.add( al );
    }

    /**
     * @return the background object of the scene.
     */
    public Node getBackground() {
        return background;
    }

    /**
     * @param background
     *            sets the <code>Background</code> object of the scene
     */
    public void setBackground( Node background ) {
        this.background = background;
    }


    /**
     * get the date and the time for determining time depending the light conditions of the scene
     *
     * @return describtion of the date and time for which the scene shall be rendered --> light
     *         conditions
     */
    public Calendar getDate() {
        return calendar;
    }

    /**
     * set the date and the time for determining time depending the light conditions of the scene
     *
     * @param calendar
     *            describtion of the date and time for which the scene shall be rendered --> light
     *            conditions
     */
    public void setDate( Calendar calendar ) {
        if ( calendar == null ) {
            LOG.logDebug( Messages.getMessage( "WPVS_STANDARD_TIME" ) );
            this.calendar = new GregorianCalendar(2007, 2, 21, 12, 00 );
        }
        this.calendar = calendar;
    }

    /**
     * @return Java3D representation of the scene.
     */
    public OrderedGroup getScene() {
        return scene;
    }

    /**
     * gets the position of the viewer, the directions he looks and his field of view in radians
     *
     * @return object that describes the viewers position and the point he looks at
     */
    public ViewPoint getViewPoint() {
        return viewPoint;
    }

    /**
     * defines the position of the viewer and the point he looks at.
     *
     * @param viewPoint
     *            object that describes the viewers position and the point he looks at
     */
    public void setViewPoint( ViewPoint viewPoint ) {
        this.viewPoint = viewPoint;
    }

    /**
     * adds a light to the scene. this can be ambient, directional and point light.
     *
     * @param light
     *            a light in addition to sun and basic ambient light (e.g. street lights, spots
     *            etc.)
     */
    public void addLight( Light light ) {
        this.lights.add( light );
    }

    /**
     * returns the lights of the scene
     *
     * @return lights including sun and basic ambient light (e.g. street lights, spots etc.)
     */
    public Light[] getLights() {
        return lights.toArray( new Light[lights.size()] );
    }

    /**
     * sets the lights of the scene. this can be ambient, directional and point light.
     *
     * @param lights
     *            lights in addition to sun and basic ambient light (e.g. street lights, spots etc.)
     */
    public void setLights( Light[] lights ) {
        this.lights.clear();
        setDate( calendar );
        createDayLight();
        if ( lights != null ) {
            for ( int i = 0; i < lights.length; i++ ) {
                addLight( lights[i] );
            }
        }
    }
}
