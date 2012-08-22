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

import org.deegree.framework.util.Parameter;
import org.deegree.framework.util.ParameterList;

/**
 * this class encapsulates the basic informations of a module that is part of an area of the GUI. Other classes may
 * extent this class by adding special attributes for more specialazied GUIs.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Module {
    private ModuleConfiguration moduleConfiguration = null;

    private String content = null;

    private String name = null;

    private String title = null;

    private boolean hidden = false;

    private ParameterList parameterList = null;

    private String type = "content";

    private String width = "0";

    private String height = "0";

    private int left = 0;

    private int right = 0;

    private int top = 0;

    private int bottom = 0;

    private boolean header;

    private boolean closable;

    private boolean overlay;
    
    private boolean collapsed;

    private String[] moduleJSList = new String[0];

    private String scrolling = "auto";
        

    /**
     * Creates a new Module object.
     * 
     * @param name
     *            name of the module
     * @param title
     *            title of the module
     * @param content
     *            the name of the page/class/file etc. containing the content of the module
     * @param hidden
     *            indicates if the module is visible or not
     * @param type
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @param width
     * @param height
     * @param overlay
     * @param header
     * @param closable
     * @param collapsed
     *            the state of the module in table layout of ExtJS. default is false
     * @param scrolling
     * @param moduleJSList
     * @param moduleConfiguration
     *            encapsulates the access to the modules configuration (may be <tt>null</tt>)
     * @param parameterList
     */
    public Module( String name, String title, String content, boolean hidden, String type, int left, int top, int right,
                   int bottom, String width, String height, boolean overlay, boolean header, boolean closable, boolean collapsed, 
                   String scrolling, String[] moduleJSList, ModuleConfiguration moduleConfiguration, ParameterList parameterList ) {
        setName( name );
        setTitle( title );
        setContent( content );
        setHidden( hidden );
        setModuleConfiguration( moduleConfiguration );
        setParameter( parameterList );
        setType( type );
        setWidth( width );
        setHeight( height );
        setLeft( left );
        setTop( top );
        setRight( right );
        setHeader( header );
        setClosable( closable );
        setCollapsed( collapsed );
        setOverlay( overlay );
        setBottom( bottom );
        setModuleJSList( moduleJSList );
        setScrolling( scrolling );
    }

    /**
     * returns the name of a module
     * 
     * @return the name of a module
     */
    public String getName() {
        return name;
    }

    /**
     * sets the name of a module
     * 
     * @param name
     */
    public void setName( String name ) {
        this.name = name;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title to set; if <code>title</code> == <code>null</code> the modules name will be used as title
     */
    public void setTitle( String title ) {
        if ( title == null ) {
            this.title = name;
        } else {
            this.title = title;
        }
    }

    /**
     * returns the name of the page/class/file etc. containing the content of the module
     * 
     * @return the name of the page/class/file etc. containing the content of the module
     */
    public String getContent() {
        return content;
    }

    /**
     * sets the name of the page/class/file etc. containing the content of the module
     * 
     * @param content
     */
    public void setContent( String content ) {
        this.content = content;
    }

    /**
     * returns true if the module is hidden. this will always be the case for modules that just offers functions to the
     * context. visible modules may offere the capability to be turned to visible or not.
     * 
     * @return <code>true</code> if the module is hidden.
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * sets the module to be hidden or visible. modules that only adds functions to a context will ignore this because
     * they are always hidden
     * 
     * @param hidden
     */
    public void setHidden( boolean hidden ) {
        this.hidden = hidden;
    }

    /**
     * returns the a specific confguration for a module. This may be <tt>null</tt> if the module doesn't need to be
     * configured.
     * 
     * @return the a specific confguration for a module. This may be <code>null</code>if the module doesn't need to be
     *         configured.
     */
    public ModuleConfiguration getModuleConfiguration() {
        return moduleConfiguration;
    }

    /**
     * sets the specific configuration for a module.
     * 
     * @param configuration
     */
    public void setModuleConfiguration( ModuleConfiguration configuration ) {
        this.moduleConfiguration = configuration;
    }

    /**
     * returns a list of parameters that will be passed to a class/object etc. that represents a module
     * 
     * @return parameters
     */
    public ParameterList getParameter() {
        return parameterList;
    }

    /**
     * sets a list of parameters that will be passed to a class/object etc. that represents a module
     * 
     * @param parameterList
     */
    public void setParameter( ParameterList parameterList ) {
        this.parameterList = parameterList;
    }

    /**
     * adds a parameter to the list of parameters that will be passed to a class/object etc. that represents a module
     * 
     * @param parameter
     */
    public void addParameter( Parameter parameter ) {
        parameterList.addParameter( parameter );
    }

    /**
     * removes a parameter to the list of parameters that will be passed to a class/object etc. that represents a module
     * 
     * @param name
     */
    public void removeParameter( String name ) {
        parameterList.removeParameter( name );
    }

    /**
     * see also org.deegree.clients.context.Module#setType(java.lang.String)
     * 
     * @param type
     *            to set
     */
    public void setType( String type ) {
        if ( type != null ) {
            this.type = type.toLowerCase();
        }
    }

    /**
     * see also org.deegree.clients.context.Module#getType()
     * 
     * @return the type.
     */
    public String getType() {
        return type;
    }

    /**
     * returns the width of the module in the GUI. If '0' will be returned the GUI can set the with like it is best
     * 
     * @return the width of the module in the GUI. If '0' will be returned the GUI can set the with like it is best
     */
    public String getWidth() {
        return this.width;
    }

    /**
     * sets the desired width of the module in the GUI. If '0' ist passed the GUI can set the with like it is best
     * 
     * @param width
     *            desired width of the module
     */
    public void setWidth( String width ) {
        this.width = width;
    }

    /**
     * returns the height of the module in the GUI. If '0' will be returned the GUI can set the with like it is best
     * 
     * @return the height of the module in the GUI. If '0' will be returned the GUI can set the with like it is best
     */
    public String getHeight() {
        return this.height;
    }

    /**
     * sets the desired height of the module in the GUI. If '0' ist passed the GUI can set the with like it is best
     * 
     * @param height
     *            desired width of the module
     */
    public void setHeight( String height ) {
        this.height = height;
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
     * @return moduleJSList
     */
    public String[] getModuleJSList() {
        return moduleJSList;
    }

    /**
     * @param list
     */
    public void setModuleJSList( String[] list ) {
        this.moduleJSList = list;
    }

    /**
     * return true is the module should has scrollbars in the GUI<br>
     * possible values are
     * <UL>
     * <li>no
     * <li>yes
     * <li>auto
     * </UL>
     * default is auto
     * 
     * @return <code>true</code> is the module should has scrollbars in the GUI possible values are
     */
    public String getScrolling() {
        return scrolling;
    }

    /**
     * @see #getScrolling()
     * @param scrollable
     */
    public void setScrolling( String scrollable ) {
        this.scrolling = scrollable;
    }

    /**
     * @param collapsed
     *            default value is false
     */
    public void setCollapsed( boolean collapsed ) {
        this.collapsed = collapsed;
    }

    /**
     * @return collapsed
     */
    public boolean isCollapsed() {
        return collapsed;
    }
}
