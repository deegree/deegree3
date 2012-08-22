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
import org.deegree.model.filterencoding.Filter;

/**
 * A rule is used to attach a condition to and group the individual symbolizers used for rendering.
 * The Title and Abstract describe the rule and may be used to generate a legend, as may the
 * LegendGraphic. The Filter, ElseFilter, MinScale, and MaxScale elements allow the selection of
 * features and rendering scales for a rule. The scale selection works as follows. When a map is to
 * be rendered, the scale denominator is computed and all rules in all UserStyles that have a scale
 * outside of the request range are dropped. (This also includes Rules that have an ElseFilter.) An
 * ElseFilter is simply an ELSE condition to the conditions (Filters) of all other rules in the same
 * UserStyle. The exact meaning of the ElseFilter is determined after Rules have been eliminated for
 * not fitting the rendering scale. This definition of the behaviour of ElseFilters may seem a
 * little strange, but it allows for scale- dependent and scale-independent ELSE conditions. For the
 * Filter, only SqlExpression is available for specification, but this is a hack and should be
 * replaced with Filter as defined in WFS. A missing Filter element means "always true". If a set of
 * Rules has no ElseFilters, then some features may not be rendered (which is presumably the desired
 * behavior). The Scales are actually scale denominators (as double floats), so "10e6" would be
 * interpreted as 1:10M. A missing MinScale means there is no lower bound to the scale- denominator
 * range (lim[x-&gt;0+](x)), and a missing MaxScale means there is no upper bound (infinity). 0.28mm
 *
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp </a>
 * @author last edited by: $Author$
 * @version $Revision$ $Date$
 */

public class Rule implements Marshallable {

    private List<Symbolizer> symbolizers = null;

    /**
     *
     */
    private Filter filter = null;

    /**
     *
     */
    private LegendGraphic legendGraphic = null;

    private String abstract_ = null;

    private String name = null;

    private String title = null;

    private boolean elseFilter = false;

    private double maxScaleDenominator = 0;

    private double minScaleDenominator = 0;

    /**
     * default constructor
     */
    Rule() {
        symbolizers = new ArrayList<Symbolizer>();
    }

    /**
     * constructor initializing the class with the Rule
     * @param symbolizers
     * @param name
     * @param title
     * @param abstract_
     * @param legendGraphic
     * @param filter
     * @param elseFilter
     * @param minScaleDenominator
     * @param maxScaleDenominator
     */
    Rule( Symbolizer[] symbolizers, String name, String title, String abstract_, LegendGraphic legendGraphic,
          Filter filter, boolean elseFilter, double minScaleDenominator, double maxScaleDenominator ) {
        this();
        setSymbolizers( symbolizers );
        setName( name );
        setTitle( title );
        setAbstract( abstract_ );
        setLegendGraphic( legendGraphic );
        setFilter( filter );
        setElseFilter( elseFilter );
        setMinScaleDenominator( minScaleDenominator );
        setMaxScaleDenominator( maxScaleDenominator );
    }

    /**
     * returns the name of the rule. this for machine interpreting.
     *
     * @return the name of the rule
     *
     */
    public String getName() {
        return name;
    }

    /**
     * sets the name of the rule. this for machine interpreting.
     *
     * @param name
     *            the name of the rule
     *
     */
    public void setName( String name ) {
        this.name = name;
    }

    /**
     * returns the human readable title of the rule
     *
     * @return the title of the rule
     *
     */
    public String getTitle() {
        return title;
    }

    /**
     * sets the human readable title of the rule
     *
     * @param title
     *            the title of the rule
     *
     */
    public void setTitle( String title ) {
        this.title = title;
    }

    /**
     * returns the human readable abstract of the rule
     *
     * @return the abstract of the rule
     */
    public String getAbstract() {
        return abstract_;
    }

    /**
     * sets the human readable abstract of the rule
     *
     * @param abstract_
     *            the abstract of the rule
     */
    public void setAbstract( String abstract_ ) {
        this.abstract_ = abstract_;
    }

    /**
     * The LegendGraphic element gives an optional explicit Graphic symbol to be displayed in a
     * legend for this rule.
     *
     * @return the legendGraphic of the rule
     *
     */
    public LegendGraphic getLegendGraphic() {
        return legendGraphic;
    }

    /**
     * sets the LegendGraphic element
     *
     * @param legendGraphic
     *            the legendGraphic of the rule
     *
     */
    public void setLegendGraphic( LegendGraphic legendGraphic ) {
        this.legendGraphic = legendGraphic;
    }

    /**
     * The Filter element has a relatively straightforward meaning. The syntax of the Filter element
     * is defined in the WFS specification and allows both attribute (property) and spatial
     * filtering.
     *
     * @return the filter element
     *
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * sets the <Filter>
     *
     * @param filter
     *            the filter element
     *
     */
    public void setFilter( Filter filter ) {
        this.filter = filter;
    }

    /**
     * The ElseFilter allows rules to be specified that are activated for features are not selected
     * by any other rule in a feature-type style.
     *
     * @return true if the rule has an elseFilter
     */
    public boolean hasElseFilter() {
        return elseFilter;
    }

    /**
     * sets the <ElseFilter>
     *
     * @param elseFilter
     *            an elseFilter
     *
     */
    public void setElseFilter( boolean elseFilter ) {
        this.elseFilter = elseFilter;
    }

    /**
     * The MinScaleDenominator and MaxScaleDenominator elements of a Rule define the range of
     * map-rendering scales for which the rule should be applied. The MinScaleDenominator and
     * MaxScaleDenominator elements, as their names suggest, are simply the minimum and maximum
     * ranges of scale (denominators) of maps for which a rule should apply.
     *
     * @return the MinScaleDenominator for the rule
     *
     */
    public double getMinScaleDenominator() {
        return minScaleDenominator;
    }

    /**
     * sets the <MinScaleDenominator>
     *
     * @param minScaleDenominator
     *            the MinScaleDenominator for the rule
     *
     */
    public void setMinScaleDenominator( double minScaleDenominator ) {
        this.minScaleDenominator = minScaleDenominator;
    }

    /**
     * The MinScaleDenominator and MaxScaleDenominator elements of a Rule define the range of
     * map-rendering scales for which the rule should be applied. The MinScaleDenominator and
     * MaxScaleDenominator elements, as their names suggest, are simply the minimum and maximum
     * ranges of scale (denominators) of maps for which a rule should apply.
     *
     * @return the MaxScaleDenominator for the rule
     *
     */
    public double getMaxScaleDenominator() {
        return maxScaleDenominator;
    }

    /**
     * sets the <MaxScaleDenominator>
     *
     * @param maxScaleDenominator
     *            the MaxScaleDenominator for the rule
     *
     */
    public void setMaxScaleDenominator( double maxScaleDenominator ) {
        this.maxScaleDenominator = maxScaleDenominator;
    }

    /**
     * Embedded inside of Rules, which group conditions for styling features, are Symbolizers. A
     * symbolizer describes how a feature is to appear on a map. The symbolizer describes not just
     * the shape that should appear but also such graphical properties as color and opacity. A
     * symbol is obtained by specifying one of a small number of different types of symbolizer and
     * then supplying parameters to override its default behaviour. Currently, five types of
     * symbolizers are defined.
     * <p>
     * </p>
     * The Symbolizers will be returned in the sequece of their occurence with in the rule
     * definition. Its the users function to determine what type of Symbolizer(s) are returned. This
     * can be done for example by using the <tt>instanceof</tt> operator of Java.
     *
     * @return the Symbolizer for the rule
     *
     */
    public Symbolizer[] getSymbolizers() {
        return symbolizers.toArray( new Symbolizer[symbolizers.size()] );
    }

    /**
     * sets the <Symbolizer>
     *
     * @param symbolizers
     *            symbolizers for the rule
     */
    public void setSymbolizers( Symbolizer[] symbolizers ) {
        this.symbolizers.clear();

        if ( symbolizers != null ) {
            for ( int i = 0; i < symbolizers.length; i++ ) {
                this.symbolizers.add( symbolizers[i] );
            }
        }
    }

    /**
     * adds a <Symbolizer>
     *
     * @param symbolizer
     *            symbolizer to add
     */
    public void addSymbolizer( Symbolizer symbolizer ) {
        symbolizers.add( symbolizer );
    }

    /**
     * Removes a <Symbolizer>from a set of Symbolizers.
     *
     * @param symbolizer
     *            symbolizer to remove
     */
    public void removeSymbolizer( Symbolizer symbolizer ) {
        if ( symbolizers.indexOf( symbolizer ) != -1 ) {
            symbolizers.remove( symbolizers.indexOf( symbolizer ) );
        }
    }

    /**
     * exports the content of the Rule as XML formated String
     *
     * @return xml representation of the Rule
     */
    public String exportAsXML() {

        StringBuffer sb = new StringBuffer( 1000 );
        sb.append( "<Rule>" );
        if ( name != null && !name.equals( "" ) ) {
            sb.append( "<Name>" ).append( escape( name ) ).append( "</Name>" );
        }
        if ( title != null && !title.equals( "" ) ) {
            sb.append( "<Title>" ).append( escape( title ) ).append( "</Title>" );
        }
        if ( abstract_ != null && !abstract_.equals( "" ) ) {
            sb.append( "<Abstract>" ).append( escape( abstract_ ) ).append( "</Abstract>" );
        }
        if ( legendGraphic != null ) {
            sb.append( "<LegendGraphic>" ).append( ( (Marshallable) legendGraphic ).exportAsXML() ).append(
                                                                                                            "</LegendGraphic>" );
        }
        if ( filter != null ) {
            sb.append( filter.to110XML() );
        }
        if ( elseFilter ) {
            sb.append( "<ElseFilter/>" );
        }
        sb.append( "<MinScaleDenominator>" ).append( minScaleDenominator );
        sb.append( "</MinScaleDenominator>" );
        sb.append( "<MaxScaleDenominator>" ).append( maxScaleDenominator );
        sb.append( "</MaxScaleDenominator>" );
        for ( int i = 0; i < symbolizers.size(); i++ ) {
            sb.append( ( (Marshallable) symbolizers.get( i ) ).exportAsXML() );
        }
        sb.append( "</Rule>" );

        return sb.toString();
    }
}
