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
package org.deegree.graphics;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.MapUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.graphics.displayelements.DisplayElement;
import org.deegree.graphics.optimizers.Optimizer;
import org.deegree.graphics.transformation.GeoTransform;
import org.deegree.graphics.transformation.WorldToScreenTransform;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.spatialschema.Envelope;

/**
 * This interface describes the data model of the map itself. It is built from themes containing
 * {@link DisplayElement}s to be rendered. Themes can be added and removed. Existing themes can be
 * re-arragned by changing their order.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class MapView {

    private static final ILogger LOG = LoggerFactory.getLogger( MapView.class );

    private String name = null;

    private HashMap<String, Theme> themes = null;

    private HashMap<String, Boolean> enabled = null;

    private List<Theme> themesL = null;

    private Theme activatedTh = null;

    private Envelope boundingbox = null;

    private CoordinateSystem crs = null;

    private List<EventController> eventCntr = Collections.synchronizedList( new ArrayList<EventController>() );

    private double scale;

    private double pixelsize = 0.00028;

    private GeoTransform projection = new WorldToScreenTransform();

    // list of Optimizers that are processed at the beginning of the paint ()-call
    private List<Optimizer> optimizers = new ArrayList<Optimizer>();

    /**
     *
     * @param name
     * @param boundingbox
     * @param pixelsize
     * @throws UnknownCRSException
     */
    protected MapView( String name, Envelope boundingbox, double pixelsize ) throws UnknownCRSException {
        this.name = name;
        this.pixelsize = pixelsize;
        themes = new HashMap<String, Theme>();
        themesL = new ArrayList<Theme>();
        enabled = new HashMap<String, Boolean>();
        setBoundingBox( boundingbox );
        crs = CRSFactory.create( "EPSG:4326" );
    }

    /**
     *
     * @param name
     * @param boundingbox
     * @param crs
     * @param pixelsize
     */
    protected MapView( String name, Envelope boundingbox, CoordinateSystem crs, double pixelsize ) {
        this.name = name;
        this.pixelsize = pixelsize;
        themes = new HashMap<String, Theme>();
        themesL = new ArrayList<Theme>();
        enabled = new HashMap<String, Boolean>();
        setBoundingBox( boundingbox );
        this.crs = crs;
    }

    /**
     * returns the name of the map
     * @return the name of the map
     *
     */
    public String getName() {
        return name;
    }

    /**
     * returns the Theme that matches the submitted name
     * @param name of the theme to get
     * @return the theme or <code>null</code> if not found.
     */
    public Theme getTheme( String name ) {
        return themes.get( name );
    }

    /**
     * returns the Theme that matches the submitted index
     * @param index of the theme
     * @return the Theme that matches the submitted index or <code>null</code> if the given index has no theme.
     */
    public Theme getTheme( int index ) {
        return themesL.get( index );
    }

    /**
     * returns the Themes in correct order. The first Theme (index == 0) shall be rendered at first
     * (bottom most).
     * @return the Themes in inserted order
     */
    public Theme[] getAllThemes() {
        return themesL.toArray( new Theme[themesL.size()] );
    }

    /**
     * Returns the current scale of the MapView.
     * @return the current scale of the MapView.
     *
     */
    public double getScale() {
        return scale;
    }

    public double getPixelSize() {
        return pixelsize;
    }
    
    /**
     * Returns the current scale of the MapView.
     * @param g the graphics to calculate the scale from
     * @return the current scale of the MapView.
     */
    public double getScale( Graphics g ){
        return MapUtils.calcScale( g.getClipBounds().width, g.getClipBounds().height, getBoundingBox(),
                                   getCoordinatesSystem(), pixelsize );
    }

    /**
     * adds a theme to the MapView
     * @param theme to add
     * @throws Exception if the coordinate system of the theme could not be set to the layer.
     */
    public void addTheme( Theme theme ) throws Exception
                          {
        themes.put( theme.getName(), theme );
        themesL.add( theme );
        enabled.put( theme.getName(), Boolean.TRUE );
        activatedTh = theme;
        theme.setParent( this );
        theme.getLayer().setCoordinatesSystem( crs );
    }

    /**
     * removes a theme from the MapView
     * @param theme to remove
     */
    public void removeTheme( Theme theme ) {
        if ( theme != null ) {
            enabled.remove( theme.getName() );
            themesL.remove( themesL.indexOf( theme ) );
            themes.remove( theme.getName() );
        }
    }

    /**
     * removes the theme that matches the submitted name from the MapView
     * @param name to of the theme to be removed.
     */
    public void removeTheme( String name ) {
        removeTheme( getTheme( name ) );
    }

    /**
     * removes the theme that matches the submitted index from the MapView
     * @param index of the theme to be removed
     */
    public void removeTheme( int index ) {
        removeTheme( themesL.get( index ) );
    }

    /**
     * removes all themes from the MapView.
     */
    public void clear() {
        themes.clear();
        themesL.clear();
        enabled.clear();
        activatedTh = null;
    }

    /**
     * swaps the positions of the submitted themes
     * @param first will be second
     * @param second will be first.
     */
    public void swapThemes( Theme first, Theme second ) {

        if ( themesL.contains( first ) && themesL.contains( second ) ) {
            int i1 = themesL.indexOf( first );
            int i2 = themesL.indexOf( second );
            themesL.set( i1, second );
            themesL.set( i2, first );
        }

    }

    /**
     * move a theme up for one index position (index = oldindex + 1)
     * @param theme to move up
     */
    public void moveUp( Theme theme ) {

        int idx = themesL.indexOf( theme );
        if ( idx < themesL.size() - 1 ) {
            Theme th = themesL.get( idx + 1 );
            swapThemes( theme, th );
        }

    }

    /**
     * move a theme down for one index position (index = oldindex - 1)
     * @param theme to move down
     */
    public void moveDown( Theme theme ) {

        int idx = themesL.indexOf( theme );
        if ( idx > 0 ) {
            Theme th = themesL.get( idx - 1 );
            swapThemes( theme, th );
        }

    }

    /**
     * enables or disables a theme that is part of the MapView. A theme that has been disabled won't
     * be rendered and usually doesn't react to events targeted to the MapView, but still is part of
     * the MapView.
     * @param theme to be dis/en-abled
     * @param enable true if enabled
     */
    public void enableTheme( Theme theme, boolean enable ) {
        enabled.put( theme.getName(), enable ? Boolean.TRUE : Boolean.FALSE );
    }

    /**
     * returns true if the passed theme is set to be enabled
     * @param theme to check
     * @return true if the passed theme is set to be enabled
     */
    public boolean isThemeEnabled( Theme theme ) {
        return enabled.get( theme.getName() ).booleanValue();
    }

    /**
     * activates a theme. Usually the activated theme is perferred to react to events (this doesn't
     * mean that other themes are not allowed to react to events).
     * @param theme to activate
     */
    public void activateTheme( Theme theme ) {
        activatedTh = theme;
    }

    /**
     * returns true if the passed theme is the one that is set to be activated
     * @param theme to check
     * @return true if the passed theme is the one that is set to be activated
     */
    public boolean isThemeActivated( Theme theme ) {
        return activatedTh.getName().equals( theme.getName() );
    }

    /**
     * returns the amount of themes within the MapView.
     * @return the amount of themes within the MapView.
     */
    public int getSize() {
        return themes.size();
    }

    /**
     * adds an eventcontroller to the MapView that's responsible for handling events that targets the
     * map. E.g.: zooming, panning, selecting a feature etc.
     * @param obj event controller to add
     */
    public void addEventController( MapEventController obj ) {
        eventCntr.add( obj );
        obj.addMapView( this );
    }

    /**
     * @param obj event controller to be removed
     * @see org.deegree.graphics.MapView#addEventController(MapEventController)
     */
    public void removeEventController( MapEventController obj ) {
        eventCntr.remove( obj );
        obj.removeMapView( this );
    }

    /**
     * A selector is a class that offers methods for selecting and de-selecting single
     * DisplayElements or groups of DisplayElements. A selector may offers methods like 'select all
     * DisplayElements within a specified bounding box' or 'select all DisplayElements thats area is
     * larger than 120 km' etc.
     * @param obj selector to added to all themes
     */
    public void addSelector( Selector obj ) {
        for ( int i = 0; i < themesL.size(); i++ ) {
            getTheme( i ).addSelector( obj );
        }
    }

    /**
     * @param obj selector to be removed
     * @see org.deegree.graphics.MapView#addSelector(Selector)
     */
    public void removeSelector( Selector obj ) {
        for ( int i = 0; i < themesL.size(); i++ ) {
            getTheme( i ).removeSelector( obj );
        }
    }

    /**
     * returns the BoundingBox (Envelope) of the MapView. This isn't necessary the BoundingBox of
     * the data that will be rendered. It's the boundingBox of the the visible area of the map
     * measured in its coordinate reference system.
     * @return the BoundingBox (Envelope) of the MapView.
     */
    public Envelope getBoundingBox() {
        return boundingbox;
    }

    /**
     * @param boundingbox to set.
     * @see org.deegree.graphics.MapView#getBoundingBox() this method may be used for zooming and
     *      panning the map
     */
    public void setBoundingBox( Envelope boundingbox ) {
        this.boundingbox = boundingbox;
        projection.setSourceRect( boundingbox );
    }

    /**
     * returns the coordinate reference system of the MapView
     * @return the coordinate reference system of the MapView
     */
    public CoordinateSystem getCoordinatesSystem() {
        return crs;
    }

    /**
     * sets the coordinate reference system of the map;
     * @param crs to be set
     * @throws Exception if the crs could not be set to the layers of the themes
     */
    public void setCoordinateSystem( CoordinateSystem crs ) throws Exception {
        this.crs = crs;
        for ( int i = 0; i < themesL.size(); i++ ) {
            Layer lay = getTheme( i ).getLayer();
            lay.setCoordinatesSystem( crs );
        }
    }

    /**
     * renders the map to the passed graphic context
     *
     * @param g
     * @throws RenderException
     *             thrown if the passed <tt>Graphic<tt> haven't
     *                         clipbounds. use g.setClip( .. );
     */
    public void paint( Graphics g )
                            throws RenderException {

        if ( g.getClipBounds() == null ) {
            throw new RenderException( "no clip bounds defined for graphic context" );
        }

        int x = g.getClipBounds().x;
        int y = g.getClipBounds().y;
        int w = g.getClipBounds().width;
        int h = g.getClipBounds().height;
        projection.setDestRect( x, y, w + x, h + y );

        try {
            double sc = getScale( g );
            LOG.logInfo( "OGC SLD scale denominator ", sc );
            scale = sc;
            // call all Optimizers
            optimize( g );
        } catch ( Exception e ) {
            e.printStackTrace();
            throw new RenderException( StringTools.stackTraceToString( e ) );
        }

        // paint all Themes
        for ( int i = 0; i < themesL.size(); i++ ) {
            if ( isThemeEnabled( getTheme( i ) ) ) {
                getTheme( i ).paint( g );
            }
        }

    }

    /**
     * renders the features marked as selected of all themes contained within the MapView
     *
     * @param g
     *            graphic context to render the map too
     * @throws RenderException
     *             thrown if the passed <tt>Graphic<tt> haven't
     *                         clipbounds. use g.setClip( .. );
     */
    public void paintSelected( Graphics g )
                            throws RenderException {

        if ( g.getClipBounds() == null ) {
            throw new RenderException( "no clip bounds defined for graphic context" );
        }

        int x = g.getClipBounds().x;
        int y = g.getClipBounds().y;
        int width = g.getClipBounds().width;
        int height = g.getClipBounds().height;
        projection.setDestRect( x - 2, y - 2, width + x, height + y );

        try {
            // call all Optimizers
            optimize( g );
        } catch ( Exception e ) {
            throw new RenderException( StringTools.stackTraceToString( e ) );
        }

        // paint all Themes
        for ( int i = 0; i < themesL.size(); i++ ) {
            if ( isThemeEnabled( getTheme( i ) ) ) {
                getTheme( i ).paintSelected( g );
            }
        }

    }

    /**
     * renders the features marked as highlighted of all themes contained within the MapView
     *
     * @param g
     *            graphic context to render the map too
     * @throws RenderException
     *             thrown if the passed <tt>Graphic<tt> haven't
     *                         clipbounds. use g.setClip( .. );
     */
    public void paintHighlighted( Graphics g )
                            throws RenderException {

        if ( g.getClipBounds() == null ) {
            throw new RenderException( "no clip bounds defined for graphic context" );
        }

        int x = g.getClipBounds().x;
        int y = g.getClipBounds().y;
        int width = g.getClipBounds().width;
        int height = g.getClipBounds().height;
        projection.setDestRect( x - 2, y - 2, width + x, height + y );

        try {
            // call all Optimizers
            optimize( g );
        } catch ( Exception e ) {
            throw new RenderException( StringTools.stackTraceToString( e ) );
        }

        // paint all Themes
        for ( int i = 0; i < themesL.size(); i++ ) {
            if ( isThemeEnabled( getTheme( i ) ) ) {
                getTheme( i ).paintHighlighted( g );
            }
        }

    }

    /**
     * A Highlighter is a class that is responsible for managing the highlight capabilities for one
     * or more Themes.
     * @param highlighter to added to all themes
     */
    public void addHighlighter( Highlighter highlighter ) {
        for ( int i = 0; i < themesL.size(); i++ ) {
            getTheme( i ).addHighlighter( highlighter );
        }
    }

    /**
     * @param highlighter to be removed from all themes
     * @see org.deegree.graphics.MapView#addHighlighter(Highlighter)
     */
    public void removeHighlighter( Highlighter highlighter ) {
        for ( int i = 0; i < themesL.size(); i++ ) {
            getTheme( i ).removeHighlighter( highlighter );
        }
    }

    /**
     * Returns the <tt>GeoTransform</tt> that is associated to this MapView.
     * <p>
     *
     * @return the associated <tt>GeoTransform</tt>-instance
     *
     */
    public GeoTransform getProjection() {
        return projection;
    }

    /**
     * Calls all registered <tt>Optimizer</tt> subsequently.
     *
     * @param g
     */
    private void optimize( Graphics g )
                            throws Exception {
        Graphics2D g2 = (Graphics2D) g;
        Iterator<Optimizer> it = optimizers.iterator();
        while ( it.hasNext() ) {
            Optimizer optimizer = it.next();
            optimizer.optimize( g2 );
        }
    }

    /**
     * Adds an <tt>Optimizer</tt>.
     *
     * @param optimizer
     */
    public void addOptimizer( Optimizer optimizer ) {
        optimizers.add( optimizer );
        optimizer.setMapView( this );
    }

    /**
     * Returns the <tt>Optimizer</tt>s.
     *
     * @return the <tt>Optimizer</tt>s.
     *
     */
    public Optimizer[] getOptimizers() {
        return optimizers.toArray( new Optimizer[0] );
    }

    /**
     * Sets the <tt>Optimizer<tt>s.
     * @param optimizers
     */
    public void setOptimizers( Optimizer[] optimizers ) {
        this.optimizers.clear();
        for ( int i = 0; i < optimizers.length; i++ ) {
            addOptimizer( optimizers[i] );
        }
    }

}
