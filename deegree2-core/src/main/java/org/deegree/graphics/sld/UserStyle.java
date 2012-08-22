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
package org.deegree.graphics.sld;

import static org.deegree.framework.xml.XMLTools.escape;

import java.util.ArrayList;
import java.util.List;

import org.deegree.framework.xml.Marshallable;

/**
 * A user-defined allows map styling to be defined externally from a system and to be passed around
 * in an interoperable format.
 * <p>
 * </p>
 * A UserStyle is at the same semantic level as a NamedStyle used in the context of a WMS. In a
 * sense, a named style can be thought of as a reference to a hidden UserStyle that is stored inside
 * of a map server.
 *
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp</a>
 * @author last edited by: $Author$
 * @version $Revision$ $Date$
 */
public class UserStyle extends AbstractStyle implements Marshallable {
    private List<FeatureTypeStyle> featureTypeStyles = null;

    private String abstract_ = null;

    private String title = null;

    private boolean default_ = false;

    /**
     * constructor initializing the class with the <UserStyle>
     * @param name
     * @param title
     * @param abstract_
     * @param default_
     * @param featureTypeStyles
     */
    UserStyle( String name, String title, String abstract_, boolean default_, FeatureTypeStyle[] featureTypeStyles ) {
        super( name );

        this.featureTypeStyles = new ArrayList<FeatureTypeStyle>();

        setTitle( title );
        setAbstract( abstract_ );
        setDefault( default_ );
        setFeatureTypeStyles( featureTypeStyles );
    }

    /**
     * The Title is a human-readable short description for the style that might be displayed in a
     * GUI pick list.
     *
     * @return the title of the User-AbstractStyle
     *
     */
    public String getTitle() {
        return title;
    }

    /**
     * sets the &lt;Title&gt;
     *
     * @param title
     *            the title of the User-AbstractStyle
     *
     */
    public void setTitle( String title ) {
        this.title = title;
    }

    /**
     * the Abstract is a more exact description that may be a few paragraphs long.
     *
     * @return the abstract of the User-AbstractStyle
     */
    public String getAbstract() {
        return abstract_;
    }

    /**
     * sets the &lt;Abstract&gt;
     *
     * @param abstract_
     *            the abstract of the User-AbstractStyle
     */
    public void setAbstract( String abstract_ ) {
        this.abstract_ = abstract_;
    }

    /**
     * The IsDefault element identifies whether a style is the default style of a layer, for use in
     * SLD library mode when rendering or for storing inside of a map server. The default value is
     * <tt>false</tt>.
     *
     * @return true if the style ist the default style
     */
    public boolean isDefault() {
        return default_;
    }

    /**
     * sets the &lt;Default&gt;
     *
     * @param default_
     */
    public void setDefault( boolean default_ ) {
        this.default_ = default_;
    }

    /**
     * A UserStyle can contain one or more FeatureTypeStyles which allow the rendering of features
     * of specific types.
     * <p>
     * </p>
     * The FeatureTypeStyle defines the styling that is to be applied to a single feature type of a
     * layer.
     * <p>
     * </p>
     * The FeatureTypeStyle element identifies that explicit separation in SLD between the handling
     * of layers and the handling of features of specific feature types. The layer concept is unique
     * to WMS and SLD, but features are used more generally, such as in WFS and GML, so this
     * explicit separation is important.
     *
     * @return the FeatureTypeStyles of a User-AbstractStyle
     *
     */
    public FeatureTypeStyle[] getFeatureTypeStyles() {

        return featureTypeStyles.toArray( new FeatureTypeStyle[featureTypeStyles.size()] );
    }

    /**
     * sets the &lt;FeatureTypeStyle&gt;
     *
     * @param featureTypeStyles
     *            the FeatureTypeStyles of a User-AbstractStyle
     */
    public void setFeatureTypeStyles( FeatureTypeStyle[] featureTypeStyles ) {
        this.featureTypeStyles.clear();

        if ( featureTypeStyles != null ) {
            for ( int i = 0; i < featureTypeStyles.length; i++ ) {
                addFeatureTypeStyle( featureTypeStyles[i] );
            }
        }
    }

    /**
     * Adds a &lt;FeatureTypeStyle&gt;
     *
     * @param featureTypeStyle
     *            a FeatureTypeStyle to add
     */
    public void addFeatureTypeStyle( FeatureTypeStyle featureTypeStyle ) {
        featureTypeStyles.add( featureTypeStyle );
    }

    /**
     * Removes a &lt;FeatureTypeStyle&gt;
     *
     * @param featureTypeStyle
     */
    public void removeFeatureTypeStyle( FeatureTypeStyle featureTypeStyle ) {
        if ( featureTypeStyles.indexOf( featureTypeStyle ) != -1 ) {
            featureTypeStyles.remove( featureTypeStyles.indexOf( featureTypeStyle ) );
        }
    }

    /**
     * exports the content of the UserStyle as XML formated String
     *
     * @return xml representation of the UserStyle
     */
    public String exportAsXML() {

        StringBuffer sb = new StringBuffer( 100 );
        sb.append( "<UserStyle " );
        sb.append( "xmlns='http://www.opengis.net/sld' " );
        sb.append( "xmlns:gml='http://www.opengis.net/gml' " );
        sb.append( "xmlns:ogc='http://www.opengis.net/ogc' " );
        sb.append( "xmlns:xlink='http://www.w3.org/1999/xlink' >" );
        if ( name != null && !name.equals( "" ) ) {
            sb.append( "<Name>" ).append( escape( name ) ).append( "</Name>" );
        }
        if ( title != null && !title.equals( "" ) ) {
            sb.append( "<Title>" ).append( escape( title ) ).append( "</Title>" );
        }
        if ( abstract_ != null && !abstract_.equals( "" ) ) {
            sb.append( "<Abstract>" ).append( escape( abstract_ ) ).append( "</Abstract>" );
        }
        if ( default_ ) {
            sb.append( "<IsDefault>" ).append( 1 ).append( "</IsDefault>" );
        }
        for ( int i = 0; i < featureTypeStyles.size(); i++ ) {
            sb.append( ( (Marshallable) featureTypeStyles.get( i ) ).exportAsXML() );
        }
        sb.append( "</UserStyle>" );

        return sb.toString();
    }

}
