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
 * The FeatureTypeStyle defines the styling that is to be applied to a single feature type of a
 * layer). This element may also be externally re-used outside of the scope of WMSes and layers.
 * <p>
 * </p>
 * The FeatureTypeStyle element identifies that explicit separation in SLD between the handling of
 * layers and the handling of features of specific feature types. The layer concept is unique to WMS
 * and SLD, but features are used more generally, such as in WFS and GML, so this explicit
 * separation is important.
 * <p>
 * ----------------------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp</a>
 * @version $Revision$ $Date$
 */
public class FeatureTypeStyle implements Marshallable {
    private List<Rule> rules = null;

    private List<String> semanticTypeIdentifier = null;

    private String abstract_ = null;

    private String featureTypeName = null;

    private String name = null;

    private String title = null;

    /**
     * default constructor
     */
    FeatureTypeStyle() {
        semanticTypeIdentifier = new ArrayList<String>();
        rules = new ArrayList<Rule>();
    }

    /**
     * constructor initializing the class with the <FeatureTypeStyle>
     * @param name
     * @param title
     * @param abstract_
     * @param featureTypeName
     * @param semanticTypeIdentifier
     * @param rules
     */
    FeatureTypeStyle( String name, String title, String abstract_, String featureTypeName,
                      String[] semanticTypeIdentifier, Rule[] rules ) {
        this();
        setName( name );
        setTitle( title );
        setAbstract( abstract_ );
        setFeatureTypeName( featureTypeName );
        setSemanticTypeIdentifier( semanticTypeIdentifier );
        setRules( rules );
    }

    /**
     * The Name element does not have an explicit use at present, though it conceivably might be
     * used to reference a feature style in some feature-style library.
     *
     * @return name
     *
     */
    public String getName() {
        return name;
    }

    /**
     * The Name element does not have an explicit use at present, though it conceivably might be
     * used to reference a feature style in some feature-style library. Sets the <Name> o
     *
     * @param name
     *            the name
     *
     */
    public void setName( String name ) {
        this.name = name;
    }

    /**
     * human-readable information about the style
     *
     * @return the title of the FeatureTypeStyle
     *
     */
    public String getTitle() {
        return title;
    }

    /**
     * sets the &lt;Title&gt;
     *
     * @param title
     *            the title of the FeatureTypeStyle
     *
     */
    public void setTitle( String title ) {
        this.title = title;
    }

    /**
     * human-readable information about the style
     *
     * @return an abstract of the FeatureTypeStyle
     */
    public String getAbstract() {
        return abstract_;
    }

    /**
     * sets &lt;Abstract&gt;
     *
     * @param abstract_
     *            an abstract of the FeatureTypeStyle
     */
    public void setAbstract( String abstract_ ) {
        this.abstract_ = abstract_;
    }

    /**
     * returns the name of the affected feature type
     *
     * @return the name of the FeatureTypeStyle as String
     *
     */
    public String getFeatureTypeName() {
        return featureTypeName;
    }

    /**
     * sets the name of the affected feature type
     *
     * @param featureTypeName
     *            the name of the FeatureTypeStyle
     *
     */
    public void setFeatureTypeName( String featureTypeName ) {
        this.featureTypeName = featureTypeName;
    }

    /**
     * The SemanticTypeIdentifier is experimental and is intended to be used to identify what the
     * feature style is suitable to be used for using community- controlled name(s). For example, a
     * single style may be suitable to use with many different feature types. The syntax of the
     * SemanticTypeIdentifier string is undefined, but the strings generic:line, generic:polygon,
     * generic:point, generic:text, generic:raster, and generic:any are reserved to indicate that a
     * FeatureTypeStyle may be used with any feature type with the corresponding default geometry
     * type (i.e., no feature properties are referenced in the feature-type style).
     *
     * @return the SemanticTypeIdentifiers from the FeatureTypeStyle as String-Array
     *
     */
    public String[] getSemanticTypeIdentifier() {
        return semanticTypeIdentifier.toArray( new String[semanticTypeIdentifier.size()] );
    }

    /**
     * Sets the SemanticTypeIdentifiers.
     *
     * @param semanticTypeIdentifiers
     *            SemanticTypeIdentifiers for the FeatureTypeStyle
     */
    public void setSemanticTypeIdentifier( String[] semanticTypeIdentifiers ) {
        semanticTypeIdentifier.clear();

        if ( semanticTypeIdentifiers != null ) {
            for ( int i = 0; i < semanticTypeIdentifiers.length; i++ ) {
                semanticTypeIdentifier.add( semanticTypeIdentifiers[i] );
            }
        }
    }

    /**
     * adds the &lt;SemanticTypeIdentifier&gt;
     *
     * @param semanticTypeIdentifier
     *            SemanticTypeIdentifier to add
     */
    public void addSemanticTypeIdentifier( String semanticTypeIdentifier ) {
        this.semanticTypeIdentifier.add( semanticTypeIdentifier );
    }

    /**
     * Removes an &lt;SemanticTypeIdentifier&gt;.
     *
     * @param semanticTypeIdentifier
     *            SemanticTypeIdentifier to remove
     */
    public void removeSemanticTypeIdentifier( String semanticTypeIdentifier ) {
        this.semanticTypeIdentifier.remove( this.semanticTypeIdentifier.indexOf( semanticTypeIdentifier ) );
    }

    /**
     * Rules are used to group rendering instructions by feature-property conditions and map scales.
     * Rule definitions are placed immediately inside of feature-style definitions.
     *
     * @return the rules of the FeatureTypeStyle as Array
     *
     */
    public Rule[] getRules() {
        return rules.toArray( new Rule[rules.size()] );
    }

    /**
     * sets the &lt;Rules&gt;
     *
     * @param rules
     *            the rules of the FeatureTypeStyle as Array
     */
    public void setRules( Rule[] rules ) {
        this.rules.clear();

        if ( rules != null ) {
            for ( int i = 0; i < rules.length; i++ ) {
                this.rules.add( rules[i] );
            }
        }
    }

    /**
     * adds the &lt;Rules&gt;
     *
     * @param rule
     *            a rule
     */
    public void addRule( Rule rule ) {
        rules.add( rule );
    }

    /**
     * removes a rule
     *
     * @param rule
     *            a rule
     */
    public void removeRule( Rule rule ) {
        rules.remove( rules.indexOf( rule ) );
    }

    /**
     * exports the content of the FeatureTypeStyle as XML formated String
     *
     * @return xml representation of the FeatureTypeStyle
     */
    public String exportAsXML() {

        StringBuffer sb = new StringBuffer( 1000 );
        sb.append( "<FeatureTypeStyle>" );
        if ( name != null && !name.equals( "" ) ) {
            sb.append( "<Name>" ).append( escape( name ) ).append( "</Name>" );
        }
        if ( title != null && !title.equals( "" ) ) {
            sb.append( "<Title>" ).append( escape( title ) ).append( "</Title>" );
        }
        if ( abstract_ != null && !abstract_.equals( "" ) ) {
            sb.append( "<Abstract>" ).append( escape( abstract_ ) ).append( "</Abstract>" );
        }
        if ( featureTypeName != null && !featureTypeName.equals( "" ) ) {
            sb.append( "<FeatureTypeName>" ).append( escape( featureTypeName ) ).append( "</FeatureTypeName>" );
        }
        for ( int i = 0; i < semanticTypeIdentifier.size(); i++ ) {
            sb.append( "<SemanticTypeIdentifier>" ).append( escape( semanticTypeIdentifier.get( i ) ) ).append(
                                                                                                      "</SemanticTypeIdentifier>" );
        }
        for ( int i = 0; i < rules.size(); i++ ) {
            sb.append( ( (Marshallable) rules.get( i ) ).exportAsXML() );
        }
        sb.append( "</FeatureTypeStyle>" );

        return sb.toString();
    }
}
