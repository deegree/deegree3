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
package org.deegree.ogcwebservices.wms.capabilities;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.deegree.framework.xml.XMLParsingException;
import org.deegree.graphics.sld.AbstractStyle;
import org.deegree.graphics.sld.NamedLayer;
import org.deegree.graphics.sld.SLDFactory;
import org.deegree.graphics.sld.StyledLayerDescriptor;
import org.deegree.graphics.sld.UserLayer;
import org.deegree.graphics.sld.UserStyle;


/**
 * Zero or more Styles may be advertised for a Layer or collection of layers
 * using &lt;Style&gt; elements, each of which shall have &lt;Name&gt; and &lt;Title&gt; elements.
 * The style's Name is used in the Map request STYLES parameter. The Title is a
 * human-readable string. If only a single style is available, that style is
 * known as the "default" style and need not be advertised by the server.<p>
 * A Style may contain several other elements in the Capabilities XML DTD. In
 * particular, &lt;Abstract&gt; provides a narrative description while &lt;LegendURL&gt;
 * contains the location of an image of a map legend appropriate to the enclosing
 * Style. A &lt;Format&gt; element in LegendURL indicates the MIME type of the logo
 * image, and the attributes width and height state the size of the image in pixels.
 * </p>
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp</a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @version $Revision$
 */
public class Style  {
    static Map<String, UserStyle> styles = null;

    static {
        styles = new HashMap<String, UserStyle>( 100 );
        styles.put( "default", null );
    }

    private String abstract_ = null;
    private String name = null;
    private String title = null;
    private StyleSheetURL styleSheetURL = null;
    private StyleURL styleURL = null;
    private URL styleResource = null;
    private LegendURL[] legendURLList = null;

    /**
    * constructor initializing the class with the &lt;Style&gt;
     * @param name
     * @param title
     * @param abstract_
     * @param legendURLs
     * @param styleSheetURL
     * @param styleURL
     * @param styleResource
     * @throws XMLParsingException
    */
    public Style( String name, String title, String abstract_, LegendURL[] legendURLs,
                StyleSheetURL styleSheetURL, StyleURL styleURL, URL styleResource )
        throws XMLParsingException {

        this.name = name;
        this.title = title;
        this.abstract_ = abstract_;
        this.styleResource = styleResource;
        this.styleSheetURL = styleSheetURL;
        this.legendURLList = legendURLs;
        this.styleURL = styleURL;
        // only cache styles that belong to the local WMS
        // for Remote WMS, styleResource is always null
        if (styleResource != null) {
            synchronized ( styles ) {
                if ( !styles.containsKey( name ) ) {
                    loadStyles( styleResource );
                }
            }
        }
    }

    /**
     * loads style definitions from the submitted URL and writes them to the
     * styles HashMap. The styleResource must provide style definitions in a
     * SLD conform XML document.<p>
     * @param styleResource resource of the style defintions
     * @exception XMLParsingException thrown if the resource doesn't hold the style
     *            definitions in a SLD conform document
     */
    private static synchronized void loadStyles( URL styleResource ) throws XMLParsingException {
        try {
            StyledLayerDescriptor sld = SLDFactory.createSLD( styleResource );
            // get styles from named layers
            NamedLayer[] nl = sld.getNamedLayers();

            for ( int i = 0; i < nl.length; i++ ) {
                AbstractStyle[] sldStyles = nl[i].getStyles();

                for ( int j = 0; j < sldStyles.length; j++ ) {
                    if ( sldStyles[j] instanceof UserStyle ) {
                        UserStyle us = (UserStyle)sldStyles[j];
                        styles.put( us.getName(), us );
                    }
                }
            }

            // get styles from user layers
            UserLayer[] ul = sld.getUserLayers();

            for ( int i = 0; i < ul.length; i++ ) {
                AbstractStyle[] sldStyles = ul[i].getStyles();

                for ( int j = 0; j < sldStyles.length; j++ ) {
                    if ( sldStyles[j] instanceof UserStyle ) {
                        UserStyle us = (UserStyle)sldStyles[j];
                        styles.put( us.getName(), us );
                    }
                }
            }

        } catch ( Exception e ) {
            e.printStackTrace();
            throw new XMLParsingException( e.toString() );
        }
    }

    /**
     * @return the name - machine code - of the style
     */
    public String getName() {
        return name;
    }

    /**
     * @return the title (human readable) of the style
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return a short narrative description of the style
     */
    public String getAbstract() {
        return abstract_;
    }

    /**
     * @return an array of LegendURL objects that defines the location, size
     * and format of the legend graphics
     */
    public LegendURL[] getLegendURL() {
        return legendURLList;
    }

    /**
     * @return StyleSheeetURL that provides symbology information for each style of a layer.
     */
    public StyleSheetURL getStyleSheetURL() {
        return styleSheetURL;
    }

    /**
    * A Style element lists the name by which a style is requested and a
    * human-readable title for pick lists, optionally (and ideally) provides a
    * human-readable description, and optionally gives a style URL.
    *
    * @return the style URL data
    */
    public StyleURL getStyleURL() {
        return styleURL;
    }

    /**
     *
     * @return the URL where to access the XML (SLD) document definingthe style
     */
    public URL getStyleResource() {
        return styleResource;
    }

    /** returns the content of the style as SLD style object
     *
     * @return instance of org.deegree.graphics.sld.Style
     *
     */
    public UserStyle getStyleContent() {
        return styles.get( name );
    }

    @Override
    public String toString() {
        String ret = null;
        ret = "name = " + name + "\n";
        ret += ( "title = " + title + "\n" );
        ret += ( "abstract_ = " + abstract_ + "\n" );
        ret += ( "styleSheetURL = " + styleSheetURL + "\n" );
        ret += ( "styleURL = " + styleURL + "\n" );
        ret += ( "legendURLList = " + legendURLList + "\n" );
        return ret;
    }
}
