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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.MapUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.graphics.displayelements.DisplayElement;
import org.deegree.graphics.displayelements.DisplayElementFactory;
import org.deegree.graphics.displayelements.LabelDisplayElement;
import org.deegree.graphics.sld.UserStyle;
import org.deegree.io.datastore.PropertyPathResolvingException;
import org.deegree.model.coverage.grid.GridCoverage;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.spatialschema.GeometryException;

/**
 * A Theme is for usual a homogenious collection of Features coupled with a portrayal model for their graphical
 * representation. Considering the OGC Styled Layer Descriptor specification this is not nessecary the case. In
 * confirmation with the SLD a theme can be build from a lot of thematic completly different feature types.
 * <p>
 * </p>
 * From a theoretical point of view this isn't very satisfying. But it will be supported by the <tt>Theme</tt> class.
 * <p>
 * </p>
 * Assigned to the Theme are:
 * <ul>
 * <li>a Layer that contains the data (features)
 * <li>a Portrayal model that determines how the features shall be rendered
 * <li>a Selector that offers method for selection and de-selection of features
 * <li>a event listener that handles event occuring on a theme that's for usual part of a map.
 * </ul>
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Theme {

    private static final ILogger LOG = LoggerFactory.getLogger( Theme.class );

    private String name = null;

    private Layer layer = null;

    private UserStyle[] styles = null;

    private List<DisplayElement> displayElements = null;

    /**
     * the MapView (map) the theme is associated to
     * 
     */
    private MapView parent = null;

    /**
     * this ArrayList contains all DisplayElements (and so the features) that are marked as selected.
     */
    private List<Selector> selector = Collections.synchronizedList( new ArrayList<Selector>() );

    private List<Highlighter> highlighter = Collections.synchronizedList( new ArrayList<Highlighter>() );

    private List<EventController> eventController = Collections.synchronizedList( new ArrayList<EventController>() );

    /**
     * 
     * @param name
     * @param layer
     * @param styles
     */
    protected Theme( String name, Layer layer, UserStyle[] styles ) {
        this.layer = layer;
        this.name = name;
        displayElements = new ArrayList<DisplayElement>( 1000 );
        setStyles( styles );
    }

    /**
     * sets the parent MapView of the Theme.
     * 
     * @param parent
     *            of this theme
     * 
     */
    public void setParent( MapView parent ) {
        this.parent = parent;
    }

    /**
     * returns the name of the theme
     * 
     * @return the name of the theme
     * 
     */
    public String getName() {
        return name;
    }

    /**
     * renders the layer to the submitted graphic context
     * 
     * @param g
     *            to draw upon
     */
    public void paint( Graphics g ) {

        double scale = parent.getScale();

        if ( layer instanceof LazyRasterLayer ) {
            // re-create raster displayelements to adapt current
            // current boundingbox
            createLazyRasterDisplayElements();
        } else if ( layer instanceof OWSRasterLayer ) {
            createOWSRasterDisplayElements();
        } else if ( layer instanceof LazyFeatureLayer ) {
            createFeatureDisplayElements();
        }
        for ( int i = 0; i < displayElements.size(); i++ ) {
            DisplayElement de = displayElements.get( i );

            if ( de.doesScaleConstraintApply( scale ) ) {
                de.paint( g, parent.getProjection(), scale );
            }
        }

    }

    /**
     * renders the display elements matching the submitted ids
     * 
     * @param g
     *            to draw upon
     * @param ids
     *            of the id's to render
     */
    public void paint( Graphics g, String[] ids ) {

        double scale = parent.getScale();

        if ( layer instanceof LazyRasterLayer ) {
            // re-create raster displayelements to adapt current
            // current boundingbox
            createLazyRasterDisplayElements();
        } else if ( layer instanceof LazyFeatureLayer ) {
            createFeatureDisplayElements();
        }
        for ( int k = 0; k < displayElements.size(); k++ ) {
            DisplayElement de = displayElements.get( k );
            for ( int i = 0; i < ids.length; i++ ) {
                if ( de.getAssociateFeatureId().equals( ids[i] ) ) {
                    de.paint( g, parent.getProjection(), scale );
                    break;
                }
            }
        }
    }

    /**
     * renders the selected display elements of the layer
     * 
     * @param g
     *            to draw upon
     */
    public void paintSelected( Graphics g ) {

        double scale = parent.getScale();

        if ( layer instanceof LazyRasterLayer ) {
            // re-create raster displayelements to adapt current
            // current boundingbox
            createLazyRasterDisplayElements();
        } else if ( layer instanceof LazyFeatureLayer ) {
            createFeatureDisplayElements();
        }

        // if ( layer instanceof OWSRasterLayer ) {
        // //
        //
        // }

        for ( int i = 0; i < displayElements.size(); i++ ) {
            DisplayElement de = displayElements.get( i );
            if ( de.isSelected() ) {
                de.paint( g, parent.getProjection(), scale );
            }
        }

    }

    /**
     * renders the highlighted display elements of the layer
     * 
     * @param g
     *            to draw upon
     */
    public void paintHighlighted( Graphics g ) {

        double scale = parent.getScale();

        if ( layer instanceof LazyRasterLayer ) {
            // re-create raster displayelements to adapt current
            // current boundingbox
            createLazyRasterDisplayElements();
        } else if ( layer instanceof LazyFeatureLayer ) {
            createFeatureDisplayElements();
        }

        for ( int i = 0; i < displayElements.size(); i++ ) {
            DisplayElement de = displayElements.get( i );
            if ( de.isHighlighted() ) {
                de.paint( g, parent.getProjection(), scale );
            }
        }

    }

    /**
     * A selector is a class that offers methods for selecting and de-selecting single DisplayElements or groups of
     * DisplayElements. A selector may offers methods like 'select all DisplayElements within a specified bounding box'
     * or 'select all DisplayElements thats area is larger than 120 km' etc.
     * 
     * @param selector
     *            to which this theme will be added and vice versa
     */
    public void addSelector( Selector selector ) {
        this.selector.add( selector );
        selector.addTheme( this );
    }

    /**
     * @param selector
     *            to remove this theme from (and viceversa)
     * @see org.deegree.graphics.Theme#addSelector(Selector)
     */
    public void removeSelector( Selector selector ) {
        this.selector.remove( selector );
        selector.removeTheme( this );
    }

    /**
     * A Highlighter is a class that is responsible for managing the highlight capabilities for one or more Themes.
     * 
     * @param highlighter
     *            to add this theme to, and vice-versa
     */
    public void addHighlighter( Highlighter highlighter ) {
        this.highlighter.add( highlighter );
        highlighter.addTheme( this );
    }

    /**
     * @param highlighter
     *            to remove this theme from and vice-versa
     * @see org.deegree.graphics.Theme#addHighlighter(Highlighter)
     */
    public void removeHighlighter( Highlighter highlighter ) {
        this.highlighter.remove( highlighter );
        highlighter.removeTheme( this );
    }

    /**
     * adds an eventcontroller to the theme that's responsible for handling events that targets the theme.
     * 
     * @param controller
     *            to add this theme to, and vice-versa
     */
    public void addEventController( ThemeEventController controller ) {
        eventController.add( controller );
        controller.addTheme( this );
    }

    /**
     * @param controller
     *            to remove this theme from and vice-versa
     * @see org.deegree.graphics.Theme#addEventController(ThemeEventController)
     */
    public void removeEventController( ThemeEventController controller ) {
        eventController.remove( controller );
        controller.removeTheme( this );
    }

    /**
     * Sets the styles used for this <tt>Theme</tt>. If this method will be called all <tt>DisplayElement</tt>s will be
     * recreated to consider the new style definitions.
     * 
     * @param styles
     *            the style to set to this theme
     * 
     */
    public void setStyles( UserStyle[] styles ) {

        this.styles = styles;
        displayElements.clear();
        if ( layer instanceof FeatureLayer ) {
            createFeatureDisplayElements();
        } else if ( layer instanceof RasterLayer ) {
            createRasterDisplayElements();
        } else {
            createLazyRasterDisplayElements();
        }

    }

    /**
     * creates <code>DisplayElement</code>s for <code>Feature</code> instances
     */
    private void createFeatureDisplayElements() {
        displayElements.clear();
        DisplayElementFactory fac = new DisplayElementFactory();
        // keep LabelDisplayElements separate from the other elements
        // and append them to the end of the DisplayElement-list
        List<DisplayElement> labelDisplayElements = new ArrayList<DisplayElement>( 100 );
        try {
            // instance of FeatureLayer
            int cnt = ( (FeatureLayer) layer ).getSize();
            for ( int i = 0; i < cnt; i++ ) {
                Feature feature = ( (FeatureLayer) layer ).getFeature( i );
                featureToDisplayElement( styles, fac, labelDisplayElements, feature );
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
        }
        displayElements.addAll( labelDisplayElements );
    }

    /**
     * creates <code>DisplayElement</code>s for <code>GridCoverage</code> instances
     */
    private void createRasterDisplayElements() {
        displayElements.clear();
        DisplayElementFactory fac = new DisplayElementFactory();
        try {
            // instance of RasterLayer
            RasterLayer rl = (RasterLayer) layer;
            DisplayElement[] de = fac.createDisplayElement( rl.getRaster(), styles, rl.getRequest(),
                                                            parent.getPixelSize() );
            for ( int k = 0; k < de.length; k++ ) {
                displayElements.add( de[k] );
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
        }
    }

    /**
     * creates <code>DisplayElement</code>s for <code>GridCoverage</code> instances that are loaded depending on current
     * boundingbox.
     */
    private void createLazyRasterDisplayElements() {
        displayElements.clear();
        DisplayElementFactory fac = new DisplayElementFactory();
        try {
            if ( parent != null ) {
                LazyRasterLayer rl = (LazyRasterLayer) layer;
                double w = parent.getProjection().getDestRect().getWidth();
                double d = parent.getBoundingBox().getWidth() / w;
                GridCoverage gc = rl.getRaster( parent.getBoundingBox(), d );
                // gc can be null if e.g. the area covered by the raster
                // layer is outside the visible area.
                if ( gc != null ) {
                    DisplayElement[] de = fac.createDisplayElement( gc, styles, parent.getPixelSize() );
                    for ( int k = 0; k < de.length; k++ ) {
                        displayElements.add( de[k] );
                    }
                }
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new RuntimeException( StringTools.stackTraceToString( e ), e );
        }
    }

    private void createOWSRasterDisplayElements() {
        displayElements.clear();

        DisplayElementFactory fac = new DisplayElementFactory();
        try {
            if ( parent != null ) {
                OWSRasterLayer rl = (OWSRasterLayer) layer;
                double w = parent.getProjection().getDestRect().getWidth();
                double h = parent.getProjection().getDestRect().getHeight();
                GridCoverage gc = rl.getRaster( parent.getBoundingBox(), w, h );
                if ( gc != null ) {
                    DisplayElement[] de = fac.createDisplayElement( gc, styles, parent.getPixelSize() );
                    for ( int k = 0; k < de.length; k++ ) {
                        displayElements.add( de[k] );
                    }
                }
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
        }
    }

    /**
     * 
     * @param styles
     * @param fac
     * @param labelDisplayElements
     * @param feature
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws GeometryException
     * @throws PropertyPathResolvingException
     */
    private void featureToDisplayElement( UserStyle[] styles, DisplayElementFactory fac,
                                          List<DisplayElement> labelDisplayElements, Feature feature )
                            throws ClassNotFoundException, IllegalAccessException, InstantiationException,
                            NoSuchMethodException, InvocationTargetException, GeometryException,
                            PropertyPathResolvingException {
        DisplayElement[] de = fac.createDisplayElement( feature, styles, parent == null ? MapUtils.DEFAULT_PIXEL_SIZE
                                                                                       : parent.getPixelSize() );
        for ( int k = 0; k < de.length; k++ ) {
            if ( de[k] instanceof LabelDisplayElement ) {
                labelDisplayElements.add( de[k] );
            } else {
                displayElements.add( de[k] );
            }
        }
        FeatureProperty[] fp = feature.getProperties();
        for ( int i = 0; i < fp.length; i++ ) {
            if ( fp[i].getValue() != null && fp[i].getValue() instanceof Feature ) {
                featureToDisplayElement( styles, fac, labelDisplayElements, (Feature) fp[i].getValue() );
            }
        }
    }

    /**
     * returns the styles used for this <tt>Theme</tt>.
     * 
     * @return the styles used for this <tt>Theme</tt>.
     * 
     */
    public UserStyle[] getStyles() {
        return styles;
    }

    /**
     * returns the layer that holds the data of the theme
     * 
     * @return the layer that holds the data of the theme
     * 
     */
    public Layer getLayer() {
        return layer;
    }

    /**
     * Returns all <tt>DisplayElements</tt> that this <tt>Theme</tt> contains.
     * <p>
     * 
     * @return <tt>ArrayList</tt> containing <tt>DisplayElements</tt>
     * 
     */
    public List<DisplayElement> getDisplayElements() {
        return displayElements;
    }

    /**
     * returns the <tt>DisplayElements</tt> of the Theme
     * 
     * @param de
     *            to set to this theme.
     * 
     */
    public void setDisplayElements( List<DisplayElement> de ) {
        this.displayElements = de;
    }

}
