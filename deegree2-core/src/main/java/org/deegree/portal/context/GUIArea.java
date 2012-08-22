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
package org.deegree.portal.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * this interface describes the content of an area of a GUI. a GUI area contains zero ... n modules
 * described by the <tt>Module</tt> interface. A GUI area may be can be switched to be invisible.
 * indicated by the hidden attribute.
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 */
public class GUIArea {

    /**
     * A constant defining a 'west' direction.
     */
    public static final int WEST = 0;

    /**
     * A constant defining a 'EAST' direction.
     */
    public static final int EAST = 1;

    /**
     * A constant defining a 'SOUTH' direction.
     */
    public static final int SOUTH = 2;

    /**
     * A constant defining a 'NORTH' direction.
     */
    public static final int NORTH = 3;

    /**
     * A constant defining a 'CENTER' direction.
     */
    public static final int CENTER = 4;

    private HashMap<String, Module> modules = new HashMap<String, Module>();

    private boolean hidden = false;

    private int area = 0;

    private int width;

    private int height;

    private int left;

    private int top;

    private int right;

    private int bottom;

    private boolean overlay;

    private boolean header;

    private boolean closable;

    private List<Module> list = new ArrayList<Module>();

    /**
     * Creates a new GUIArea instance
     *
     * @param area
     * @param hidden
     * @param width
     * @param height
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @param overlay
     * @param header
     * @param closable
     * @param modules
     */
    public GUIArea( int area, boolean hidden, int width, int height, int left, int top, int right, int bottom,
                    boolean overlay, boolean header, boolean closable, Module[] modules ) {
        setArea( area );
        setHidden( hidden );
        setWidth( width );
        setHeight( height );
        setLeft( left );
        setTop( top );
        setRight( right );
        setBottom( bottom );
        setOverlay( overlay );
        setHeader( header );
        setClosable( closable );
        setModules( modules );
    }

    /**
     * returns area (north, west, east ...) assigned to an instance
     *
     * @return area
     */
    public int getArea() {
        return area;
    }

    /**
     * sets the name of a module
     *
     * @param area
     */
    public void setArea( int area ) {
        this.area = area;
    }

    /**
     * returns true if the GUIArea is hidden.
     *
     * @return true if area is hidden
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * sets the GUIArea to be hidden or visible.
     *
     * @param hidden
     */
    public void setHidden( boolean hidden ) {
        this.hidden = hidden;
    }

    /**
     * @return the bottom
     */
    public int getBottom() {
        return bottom;
    }

    /**
     * @param bottom
     *            the bottom to set
     */
    public void setBottom( int bottom ) {
        this.bottom = bottom;
    }

    /**
     * @return the left
     */
    public int getLeft() {
        return left;
    }

    /**
     * @param left
     *            the left to set
     */
    public void setLeft( int left ) {
        this.left = left;
    }

    /**
     * @return the right
     */
    public int getRight() {
        return right;
    }

    /**
     * @param right
     *            the right to set
     */
    public void setRight( int right ) {
        this.right = right;
    }

    /**
     * @return the top
     */
    public int getTop() {
        return top;
    }

    /**
     * @param top
     *            the top to set
     */
    public void setTop( int top ) {
        this.top = top;
    }

    /**
     * @return the overlay
     */
    public boolean isOverlay() {
        return overlay;
    }

    /**
     * @param overlay
     *            the overlay to set
     */
    public void setOverlay( boolean overlay ) {
        this.overlay = overlay;
    }

    /**
     * @return the closable
     */
    public boolean isClosable() {
        return closable;
    }

    /**
     * @param closable
     *            the closable to set
     */
    public void setClosable( boolean closable ) {
        this.closable = closable;
    }

    /**
     * @return the header
     */
    public boolean hasHeader() {
        return header;
    }

    /**
     * @param header
     *            the header to set
     */
    public void setHeader( boolean header ) {
        this.header = header;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height
     *            the height to set
     */
    public void setHeight( int height ) {
        this.height = height;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width
     *            the width to set
     */
    public void setWidth( int width ) {
        this.width = width;
    }

    /**
     * returns a module identified by its name
     *
     * @param name
     *
     * @return named module
     */
    public Module getModule( String name ) {
        return modules.get( name );
    }

    /**
     * returns all modules of a GUIArea
     *
     * @return all modules
     */
    public Module[] getModules() {
        Module[] cl = new Module[list.size()];
        return list.toArray( cl );

    }

    /**
     * sets the modules of a GUIArea
     *
     * @param modules
     */
    public void setModules( Module[] modules ) {
        this.modules.clear();
        this.list.clear();

        if ( modules != null ) {
            for ( int i = 0; i < modules.length; i++ ) {
                this.modules.put( modules[i].getName(), modules[i] );
                list.add( modules[i] );
            }
        }
    }

    /**
     * adds a module to a GUIArea
     *
     * @param module
     */
    public void addModul( Module module ) {
        modules.put( module.getName(), module );
        list.add( module );
    }

    /**
     * removes a module identified by its name from the GUIArea
     *
     * @param name
     *
     * @return removed module
     */
    public Module removeModule( String name ) {
        Module module = modules.remove( name );
        list.remove( module );
        return module;
    }
    
    /**
     * removes all modules from a {@link GUIArea}
     */
    public void removeAll() {
        list.clear();
    }

}
