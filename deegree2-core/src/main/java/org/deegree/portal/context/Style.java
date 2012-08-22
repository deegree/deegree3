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

import org.deegree.ogcbase.ImageURL;

/**
 * this class encapsulates the style description as defined by the OGC Web Map Context specification
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 */
public class Style {
    private ImageURL legendURL = null;

    private SLD sld = null;

    private String abstract_ = null;

    private String name = null;

    private String title = null;

    private boolean current = false;

    /**
     * Creates a new Style object.
     *
     * @param name
     *            The name of the style
     * @param title
     *            The human-readable title of the style
     * @param abstract_
     *            A narrative description of the current style
     * @param legendURL
     *            location of an image of a map legend describing the current style
     * @param current
     *            true the current style is selected.
     *
     * @throws ContextException
     */
    public Style( String name, String title, String abstract_, ImageURL legendURL, boolean current )
                            throws ContextException {
        setName( name );
        setTitle( title );
        setAbstract( abstract_ );
        setLegendURL( legendURL );
        setCurrent( current );
    }

    /**
     * Creates a new Style object.
     *
     * @param sld
     *            define the style(s) of the layer with a <SLD> element.
     * @param current
     *            true the current style is selected.
     *
     * @throws ContextException
     */
    public Style( SLD sld, boolean current ) throws ContextException {
        setSld( sld );
        setCurrent( current );
    }

    /**
     * The name of the style (extracted from Capabilities by the Context document creator).
     *
     * @return the name of the style (extracted from Capabilities by the Context document creator).
     */
    public String getName() {
        return name;
    }

    /**
     * The human-readable title of the style (extracted from Capabilities by the Context document
     * creator).
     *
     * @return The human-readable title
     */
    public String getTitle() {
        return title;
    }

    /**
     * A narrative description of the current style (extracted from Capabilities by the Context
     * document creator).
     *
     * @return A narrative description of the current style
     */
    public String getAbstract() {
        return abstract_;
    }

    /**
     * The location of an image of a map legend describing the current style (extracted from
     * Capabilities by the Context document creator).
     *
     * @return The location of an image of a map legend
     */
    public ImageURL getLegendURL() {
        return legendURL;
    }

    /**
     * Each &lt;Style&gt; element may alternatively define the style(s) of the layer with a
     * &lt;SLD&gt; element.
     *
     * @return a &lt;SLD&gt; element.
     */
    public SLD getSld() {
        return sld;
    }

    /**
     * returns true the current style is selected.
     *
     * @return true if the current style is selected.
     */
    public boolean isCurrent() {
        return current;
    }

    /**
     * @param name to set
     * @throws ContextException if the name or the sld is null
     */
    public void setName( String name )
                            throws ContextException {
        if ( ( name == null ) && ( sld == null ) ) {
            throw new ContextException( "either name or sld must be different to null" );
        }

        this.name = name;
    }

    /**
     * @param title to set
     * @throws ContextException if the name or the sld is null
     */
    public void setTitle( String title )
                            throws ContextException {
        if ( ( title == null ) && ( sld == null ) ) {
            throw new ContextException( "either title or sld must be different to null" );
        }

        this.title = title;
    }

    /**
     *
     * @param abstract_ to set
     */
    public void setAbstract( String abstract_ ) {
        this.abstract_ = abstract_;
    }

    /**
     *
     * @param legendURL to set
     */
    public void setLegendURL( ImageURL legendURL ) {
        this.legendURL = legendURL;
    }

    /**
     *
     * @param sld to set
     * @throws ContextException if the name or the sld is null
     */
    public void setSld( SLD sld )
                            throws ContextException {
        if ( ( sld == null ) && ( title == null || name == null ) ) {
            throw new ContextException( "either sld or name and tile must be different to null" );
        }

        this.sld = sld;
    }

    /**
     *
     * @param current to set
     */
    public void setCurrent( boolean current ) {
        this.current = current;
    }

}
