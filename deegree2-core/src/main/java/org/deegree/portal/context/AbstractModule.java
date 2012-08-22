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

/**
 * describes the common access to modules embedded into the GUI described by general extension section of a web map
 * context document within the deegree framework.<p/> a module encapsulates GUI elements and/or functions that are used
 * by the GUI. The concrete implementation of the GUI (e.g. as JSP pages) is responsible for enabling the commuication
 * between the different modules of a context.
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 */
public abstract class AbstractModule {
    private ModuleConfiguration moduleConfiguration = null;

    private String name = null;

    private String content = null;

    private boolean hidden = false;

    /**
     * Creates a new AbstractModule object.
     *
     * @param name
     * @param content
     * @param hidden
     * @param moduleConfiguration
     */
    public AbstractModule( String name, String content, boolean hidden, ModuleConfiguration moduleConfiguration ) {
        setName( name );
        setContent( content );
        setHidden( hidden );
        setModuleConfiguration( moduleConfiguration );
    }

    /**
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
     * @return true if the module is hidden.
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
     * @return the a specific confguration for a module
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
}
