//$$Header: $$
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

package org.deegree.graphics.charts;

import java.awt.Color;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * This class reads the configurations file for the charts and acts as a data class for the configurations It parses the
 * configurations file according to a given schema and throws an exception if the parsing failed It also assigns default
 * values to variables that are not mandatory and don't exist in the configs file
 *
 * @author <a href="mailto:elmasry@lat-lon.de">Moataz Elmasry</a>
 * @author last edited by: $Author: elmasri$
 *
 * @version $Revision$, $Date: 25 Apr 2008 13:50:16$
 */
public class ChartConfig {

    private static NamespaceContext cnxt = CommonNamespaces.getNamespaceContext();

    private boolean genAntiAliasing;

    private boolean genTextAntiAlias;

    private boolean genBorderVisible;

    private boolean plotOutlineVisible;

    private String genRectangleInsets;

    private Color genBackgroundColor = null;

    private String genFontFamily;

    private String genFontType;

    private double genFontSize;

    private double plotForegroundOpacity;

    private Color plotBackgroundColor = null;

    private double plotBackgroundOpacity;

    private double pieInteriorGap;

    private double pieLabelGap;

    private boolean pieCircular;

    private Color pieBaseSectionColor = null;

    private Color pieShadowColor = null;

    private boolean lineRenderLines;

    private boolean lineRenderShapes;

    /**
     * Takes in the path to the configurations file, parses it and holds its values
     *
     * @param configsPath
     *            XML file that contains the charts configurations
     * @throws SAXException
     * @throws IOException
     * @throws XMLParsingException
     */
    public ChartConfig( URL configsPath ) throws SAXException, IOException, XMLParsingException {

        Document doc = instantiateParser().parse( configsPath.openStream(), XMLFragment.DEFAULT_URL );
        Element root = doc.getDocumentElement();
        String prefix = root.getPrefix();
        String namespace = root.getNamespaceURI();
        URI uri = CommonNamespaces.buildNSURI( namespace );
        cnxt.addNamespace( prefix, uri );
        parseConfigurations( root );
    }

    /**
     * Creates a new instance of DocumentBuilder
     *
     * @return DocumentBuilder
     * @throws IOException
     */
    private static DocumentBuilder instantiateParser()
                            throws IOException {

        DocumentBuilder parser = null;

        try {
            DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
            fac.setNamespaceAware( true );
            fac.setValidating( false );
            fac.setIgnoringElementContentWhitespace( false );
            parser = fac.newDocumentBuilder();
            return parser;
        } catch ( ParserConfigurationException e ) {
            throw new IOException( Messages.getMessage( "GRA_CHART_ERROR_INIT_DOCBUILDER", e.getLocalizedMessage() ) );
        }
    }

    /**
     * This is the main parsing method. It parses the four parts of the xml configurations file: GeneralSettings,
     * PlotSettings, PiePlotSettings and LinePlotSettings
     *
     * @param root
     * @throws XMLParsingException
     */
    protected void parseConfigurations( Element root )
                            throws XMLParsingException {
        String prefix = root.getPrefix() + ":";
        parseGeneralChartSettings( XMLTools.getElement( root, StringTools.concat( 100, "./", prefix,
                                                                                  "GeneralChartSettings" ), cnxt ) );
        parsePlotSettings( XMLTools.getElement( root, StringTools.concat( 100, "./", prefix, "GeneralPlotSettings" ),
                                                cnxt ) );
        parsePiePlotSettings( XMLTools.getElement( root, StringTools.concat( 100, "./", prefix, "PiePlotSettings" ),
                                                   cnxt ) );
        parseLinePlotSettings( XMLTools.getElement( root, StringTools.concat( 100, "./", prefix, "LinePlotSettings" ),
                                                    cnxt ) );
    }

    /**
     * Parses the general settings of the configurations file. Refer to Resources for an example file and/or schema
     *
     * @param chartElem
     * @throws XMLParsingException
     */
    protected void parseGeneralChartSettings( Element chartElem )
                            throws XMLParsingException {
        String prefix = chartElem.getPrefix() + ":";
        genAntiAliasing = XMLTools.getRequiredNodeAsBoolean( chartElem, StringTools.concat( 100, "./", prefix,
                                                                                            "Antialiasing" ), cnxt );
        genTextAntiAlias = XMLTools.getRequiredNodeAsBoolean( chartElem, StringTools.concat( 100, "./", prefix,
                                                                                             "TextAntialiasing" ), cnxt );
        genBorderVisible = XMLTools.getRequiredNodeAsBoolean( chartElem, StringTools.concat( 100, "./", prefix,
                                                                                             "BorderVisibility" ), cnxt );
        genRectangleInsets = XMLTools.getRequiredNodeAsString( chartElem, StringTools.concat( 100, "./", prefix,
                                                                                              "RectangleInsets" ), cnxt );
        double genBackgroundOpacity = XMLTools.getRequiredNodeAsDouble( chartElem,
                                                                        StringTools.concat( 100, "./", prefix,
                                                                                            "BackgroundOpacity" ), cnxt );
        String color = XMLTools.getRequiredNodeAsString( chartElem, StringTools.concat( 100, "./", prefix,
                                                                                        "BackgroundColor" ), cnxt );
        genBackgroundColor = hexToColor( color, (float) genBackgroundOpacity );

        genFontFamily = XMLTools.getRequiredNodeAsString( chartElem, StringTools.concat( 100, "./", prefix,
                                                                                         "FontFamily" ), cnxt );

        genFontType = XMLTools.getRequiredNodeAsString( chartElem, StringTools.concat( 100, "./", prefix, "FontType" ),
                                                        cnxt );
        genFontSize = XMLTools.getRequiredNodeAsDouble( chartElem, StringTools.concat( 100, "./", prefix, "FontSize" ),
                                                        cnxt );

    }

    /**
     * Parses the plot settings of the configurations file. Refer to Resources for an example file and/or schema The
     * plot is the actual chart, while everything outside it is the general settings
     *
     * @param plotElem
     * @throws XMLParsingException
     */
    protected void parsePlotSettings( Element plotElem )
                            throws XMLParsingException {
        String prefix = plotElem.getPrefix() + ":";
        plotForegroundOpacity = XMLTools.getRequiredNodeAsDouble( plotElem, StringTools.concat( 100, "./", prefix,
                                                                                                "ForegroundOpacity" ),
                                                                  cnxt );
        plotBackgroundOpacity = XMLTools.getRequiredNodeAsDouble( plotElem, StringTools.concat( 100, "./", prefix,
                                                                                                "BackgroundOpacity" ),
                                                                  cnxt );
        plotBackgroundColor = hexToColor( XMLTools.getRequiredNodeAsString( plotElem,
                                                                            StringTools.concat( 100, "./", prefix,
                                                                                                "BackgroundColor" ),
                                                                            cnxt ), (float) plotBackgroundOpacity );
        plotOutlineVisible = XMLTools.getRequiredNodeAsBoolean( plotElem, StringTools.concat( 100, "./", prefix,
                                                                                              "OutlineVisibility" ),
                                                                cnxt );

    }

    /**
     * Parses the Pie plot settings of the configurations file. Refer to Resources for an example file and/or schema
     *
     * @param pieplotElem
     * @throws XMLParsingException
     */
    protected void parsePiePlotSettings( Element pieplotElem )
                            throws XMLParsingException {

        String prefix = pieplotElem.getPrefix() + ":";
        pieInteriorGap = XMLTools.getRequiredNodeAsDouble( pieplotElem, StringTools.concat( 100, "./", prefix,
                                                                                            "InteriorGap" ), cnxt );
        pieLabelGap = XMLTools.getRequiredNodeAsDouble( pieplotElem,
                                                        StringTools.concat( 100, "./", prefix, "LabelGap" ), cnxt );

        pieCircular = XMLTools.getRequiredNodeAsBoolean( pieplotElem,
                                                         StringTools.concat( 100, "./", prefix, "Circular" ), cnxt );
        double pieBaseSectionOpacity = XMLTools.getRequiredNodeAsDouble( pieplotElem,
                                                                         StringTools.concat( 100, "./", prefix,
                                                                                             "BaseSectionOpacity" ),
                                                                         cnxt );
        pieBaseSectionColor = hexToColor( XMLTools.getRequiredNodeAsString( pieplotElem,
                                                                            StringTools.concat( 100, "./", prefix,
                                                                                                "BaseSectionColor" ),
                                                                            cnxt ), (float) pieBaseSectionOpacity );

        double pieShadowOpacity = XMLTools.getRequiredNodeAsDouble( pieplotElem, StringTools.concat( 100, "./", prefix,
                                                                                                     "ShadowOpacity" ),
                                                                    cnxt );
        pieShadowColor = hexToColor( XMLTools.getRequiredNodeAsString( pieplotElem,
                                                                       StringTools.concat( 100, "./", prefix,
                                                                                           "ShadowColor" ), cnxt ),
                                     (float) pieShadowOpacity );
    }

    /**
     * Parses the Line plot settings of the configurations file. Refer to Resources for an example file and/or schema
     *
     * @param lineplotElem
     * @throws XMLParsingException
     */
    protected void parseLinePlotSettings( Element lineplotElem )
                            throws XMLParsingException {
        String prefix = lineplotElem.getPrefix() + ":";
        lineRenderLines = XMLTools.getRequiredNodeAsBoolean( lineplotElem, StringTools.concat( 100, "./", prefix,
                                                                                               "RenderLines" ), cnxt );
        lineRenderShapes = XMLTools.getRequiredNodeAsBoolean( lineplotElem, StringTools.concat( 100, "./", prefix,
                                                                                                "RenderShapes" ), cnxt );
    }

    /**
     * It converts a hexdecimal to a awt.color instance
     *
     * @param hexDecimal
     *            it can start with # or not
     * @param alpha
     *            between 0..1
     * @return generated color
     * @throws NumberFormatException
     *             if the given hex decimal could not be parsed.
     */
    protected Color hexToColor( String hexDecimal, float alpha )
                            throws NumberFormatException {
        if ( hexDecimal.startsWith( "0x" ) ) {
            hexDecimal = hexDecimal.substring( 2, hexDecimal.length() );
        }
        if ( hexDecimal.length() == 6 ) {
            float r = Integer.parseInt( hexDecimal.substring( 0, 2 ), 16 );
            float g = Integer.parseInt( hexDecimal.substring( 2, 4 ), 16 );
            float b = Integer.parseInt( hexDecimal.substring( 4, 6 ), 16 );
            return new Color( r / 255f, g / 255f, b / 255f, alpha );
        }
        throw new NumberFormatException( Messages.getMessage( "GRA_CHART_BAD_FORMAT_HEX", hexDecimal ) );
    }

    /**
     * @return is enabled Antialiasing for the chart
     */
    public boolean isGenAntiAliasing() {
        return genAntiAliasing;
    }

    /**
     * @return Background color of the chart
     */
    public Color getGenBackgroundColor() {
        return genBackgroundColor;
    }

    /**
     * @return is cart borders visible
     */
    public boolean isGenBorderVisible() {
        return genBorderVisible;
    }

    /**
     * @return font family of the chart
     */
    public String getGenFontFamily() {
        return genFontFamily;
    }

    /**
     * @return font size of the chart
     */
    public double getGenFontSize() {
        return genFontSize;
    }

    /**
     * @return font type of the chart
     */
    public String getGenFontType() {
        return genFontType;
    }

    /**
     * @return is chart outline visible
     */
    public boolean isPlotOutlineVisible() {
        return plotOutlineVisible;
    }

    /**
     * @return RectangleInsets of the chart
     */
    public String getGenRectangleInsets() {
        return genRectangleInsets;
    }

    /**
     * @return is enabled Text AntiAliasing
     */
    public boolean isGenTextAntiAlias() {
        return genTextAntiAlias;
    }

    /**
     * @return is enabled line rendering in Line Chart
     */
    public boolean isLineRenderLines() {
        return lineRenderLines;
    }

    /**
     * @return is enabled shape rendering in Line Chart
     */
    public boolean isLineRenderShapes() {
        return lineRenderShapes;
    }

    /**
     * @return BaseSectionColor of Pie chart
     */
    public Color getPieBaseSectionColor() {
        return pieBaseSectionColor;
    }

    /**
     * @return is the Pie Chart circular
     */
    public boolean isPieCircular() {
        return pieCircular;
    }

    /**
     * @return interior gap of the Pie chart
     */
    public double getPieInteriorGap() {
        return pieInteriorGap;
    }

    /**
     * @return label gap of the Pie Chart
     */
    public double getPieLabelGap() {
        return pieLabelGap;
    }

    /**
     * @return Background color of the general plot
     */
    public Color getPlotBackgroundColor() {
        return plotBackgroundColor;
    }

    /**
     * @return Background opacity of the general plot
     */
    public double getPlotBackgroundOpacity() {
        return plotBackgroundOpacity;
    }

    /**
     * @return Foreground opacity of the plot
     */
    public double getPlotForegroundOpacity() {
        return plotForegroundOpacity;
    }

    /**
     * @return Shadow Color of the Pie Chart
     */
    public Color getPieShadowColor() {
        return pieShadowColor;
    }
}
