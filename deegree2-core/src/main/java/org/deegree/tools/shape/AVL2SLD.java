//$$HeadURL$$
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

package org.deegree.tools.shape;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.util.ImageUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.Marshallable;
import org.deegree.graphics.sld.AbstractLayer;
import org.deegree.graphics.sld.AbstractStyle;
import org.deegree.graphics.sld.ExternalGraphic;
import org.deegree.graphics.sld.FeatureTypeStyle;
import org.deegree.graphics.sld.Fill;
import org.deegree.graphics.sld.Font;
import org.deegree.graphics.sld.Graphic;
import org.deegree.graphics.sld.GraphicFill;
import org.deegree.graphics.sld.LabelPlacement;
import org.deegree.graphics.sld.LineSymbolizer;
import org.deegree.graphics.sld.Mark;
import org.deegree.graphics.sld.NamedLayer;
import org.deegree.graphics.sld.PointPlacement;
import org.deegree.graphics.sld.PointSymbolizer;
import org.deegree.graphics.sld.PolygonSymbolizer;
import org.deegree.graphics.sld.Rule;
import org.deegree.graphics.sld.Stroke;
import org.deegree.graphics.sld.StyleFactory;
import org.deegree.graphics.sld.StyledLayerDescriptor;
import org.deegree.graphics.sld.Symbolizer;
import org.deegree.graphics.sld.TextSymbolizer;
import org.deegree.io.shpapi.MainFile;
import org.deegree.io.shpapi.ShapeConst;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.filterencoding.FilterConstructionException;
import org.deegree.model.filterencoding.Literal;
import org.deegree.model.filterencoding.LogicalOperation;
import org.deegree.model.filterencoding.Operation;
import org.deegree.model.filterencoding.OperationDefines;
import org.deegree.model.filterencoding.PropertyIsCOMPOperation;
import org.deegree.model.filterencoding.PropertyName;

/**
 * <p>
 * This class converts ESRI *.avl files to OGC SLD documents. The current version of this tool isn't
 * able to convert each and every construction that is possible with an *.avl file. But most of the
 * common expressions will be mapped.
 * </p>
 * <p>
 * Because SLD (version 1.0.0) does not know inline bitmap or fill pattern definition directly
 * (maybe it is possible using some SVG tags) all polygon fill patterns must be converted to images
 * that are written to the file system and referenced as external graphic by the created SLD style.
 * The similar is true for symbol definitions. SLD just 'knowns' a few predefined symbols that are
 * not able to capture all symbols known by ArcView. In this context deegree also will extend the
 * well known symbol by using ASCII codes to references symbols defined in an available font. This
 * will enable deegree to transform the ArcView option defining a symbol by using an ACSII character
 * directly. At least even if the synthax for this is SLD compliant most SLD implementation probably
 * won't be able to evaluate this.
 * </p>
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class AVL2SLD {

    private String fileRootName = null;

    private String targetDir = null;

    private Map<String, Map<String, Object>> blocks = new HashMap<String, Map<String, Object>>();

    private static AVLPointSymbolCodeList cl = null;

    /**
     * @param fileRootName
     * @param targetDir
     */
    public AVL2SLD( String fileRootName, String targetDir ) {
        this.fileRootName = fileRootName;
        this.targetDir = targetDir;
        if ( !this.targetDir.endsWith( "/" ) ) {
            this.targetDir = this.targetDir + "/";
        }
    }

    /**
     * reads the avl file assigned to a class instance
     *
     * @throws IOException
     */
    public void read()
                            throws IOException {
        Reader reader = new FileReader( fileRootName + ".avl" );
        StringBuffer sb = new StringBuffer( 50000 );
        int c = 0;
        while ( ( c = reader.read() ) > -1 ) {
            if ( c == 9 )
                c = ' ';
            // if ( c != 10 && c != 13) {
            sb.append( (char) c );
            // }
        }
        reader.close();

        // create a entry in 'blocks' for each '(' ... ')'
        // enclosed section
        String[] s1 = splitavl( sb.toString() );

        for ( int i = 1; i < s1.length; i++ ) {
            // write each KVP of section to a map and store it
            // in the blocks map.
            // the Class, Pattern and he Child key will be treated
            // as array because it isn't unique
            int pos = s1[i].indexOf( ' ' );
            if ( pos < 0 )
                continue;
            String section = s1[i].substring( 0, pos ).trim();
            String[] s2 = StringTools.toArray( s1[i].substring( pos, s1[i].length() ), ":\n", false );
            Map<String, Object> block = new HashMap<String, Object>();
            for ( int j = 0; j < s2.length; j = j + 2 ) {
                if ( s2[j].trim().equals( "Class" ) ) {
                    List<String> list = (List<String>) block.get( "Class" );
                    if ( list == null ) {
                        list = new ArrayList<String>();
                    }
                    list.add( s2[j + 1] );
                    block.put( s2[j], list );
                } else if ( s2[j].trim().equals( "Child" ) ) {
                    List<String> list = (List<String>) block.get( "Child" );
                    if ( list == null ) {
                        list = new ArrayList<String>();
                    }
                    list.add( s2[j + 1] );
                    block.put( s2[j], list );
                } else if ( s2[j].trim().equals( "Pattern" ) ) {
                    List<String> list = (List<String>) block.get( "Pattern" );
                    if ( list == null ) {
                        list = new ArrayList<String>();
                    }
                    list.add( s2[j + 1] );
                    block.put( s2[j], list );
                } else if ( s2[j].trim().equals( "Bits" ) ) {
                    List<String> list = (List<String>) block.get( "Bits" );
                    if ( list == null ) {
                        list = new ArrayList<String>();
                    }
                    list.add( s2[j + 1] );
                    block.put( s2[j], list );
                } else {
                    block.put( s2[j], s2[j + 1].trim() );
                }
            }
            blocks.put( section, block );
        }

    }

    /**
     * returns a <tt>Style</tt>. For each class/child contained in a avl file one <tt>Rule</tt>
     * will be created
     *
     * @return a <tt>Style</tt>. For each class/child contained in a avl file one <tt>Rule</tt>
     *         will be created
     * @throws IOException
     * @throws Exception
     */
    public AbstractStyle getStyle()
                            throws IOException, Exception {
        Map odb = blocks.get( "ODB.1" );
        String roots = (String) odb.get( "Roots" );
        Map legend = blocks.get( "Legend." + roots );
        String filterCol = null;
        if ( legend.get( "FieldNames" ) != null ) {
            Map block = blocks.get( "AVStr." + legend.get( "FieldNames" ) );
            filterCol = (String) block.get( "S" );
            filterCol = StringTools.validateString( filterCol, "\"" ).toUpperCase();
        }

        int geoType = getGeometryType();

        AbstractStyle style = null;
        switch ( geoType ) {
        case ShapeConst.SHAPE_TYPE_POINT:
            style = createPointStyle( legend, filterCol );
            break;
        case ShapeConst.SHAPE_TYPE_POLYLINE:
            style = createLinesStyle( legend, filterCol );
            break;
        case ShapeConst.SHAPE_TYPE_POLYGON:
            style = createPolygonStyle( legend, filterCol );
            break;
        case ShapeConst.SHAPE_TYPE_MULTIPOINT:
            style = createPointStyle( legend, filterCol );
            break;
        default:
            throw new Exception( "unknown geometry type: " + geoType );
        }
        return style;
    }

    /**
     * creates a <tt>StyledLayerDescriptor</tt> from the avl file assigned to the instace of a
     * <tt>AVLReader</tt>. The returned instance of a <tt>StyledLayerDescriptor</tt> just
     * contains one style that may containes several <tt>Rule</tt>s
     *
     * @return a <tt>StyledLayerDescriptor</tt> created from the avl file assigned to the instace
     *         of a <tt>AVLReader</tt>. The returned instance of a <tt>StyledLayerDescriptor</tt>
     *         just contains one style that may containes several <tt>Rule</tt>s
     * @throws IOException
     * @throws Exception
     */
    public StyledLayerDescriptor getStyledLayerDescriptor()
                            throws IOException, Exception {
        AbstractStyle style = getStyle();
        String[] t = StringTools.toArray( fileRootName, "/", false );
        String name = "default:" + t[t.length - 1];
        AbstractLayer layer = new NamedLayer( name, null, new AbstractStyle[] { style } );
        return new StyledLayerDescriptor( new AbstractLayer[] { layer }, "1.0.0" );
    }

    /**
     * @return parse a string and return array of blocks between braces "(" and ")". It accounts for
     *         braces in quoted strings.
     *
     * @param s
     *            string to parse
     */
    public static String[] splitavl( String s ) {
        if ( s == null || s.equals( "" ) ) {
            return new String[0];
        }

        Pattern pat = Pattern.compile( "\\(([^)\"]|\"[^\"]*\")*\\)" );
        Matcher mat = pat.matcher( s );
        int prevend = 0;
        ArrayList<String> vec = new ArrayList<String>();
        while ( mat.find() ) {
            int start = mat.start();
            int end = mat.end();
            if ( prevend < start - 1 ) {
                String str = s.substring( prevend, start ).trim();
                if ( str.length() > 0 ) {
                    vec.add( str );
                }
            }
            String str = s.substring( start + 1, end - 1 ).trim();
            if ( str.length() > 0 ) {
                vec.add( str );
            }
            prevend = end;
        }
        if ( prevend < s.length() - 1 ) {
            String str = s.substring( prevend ).trim();
            if ( str.length() > 0 ) {
                vec.add( str );
            }
        }

        // no value selected
        if ( vec.size() == 0 ) {
            return new String[0];
        }

        return vec.toArray( new String[vec.size()] );
    }

    private int getGeometryType()
                            throws IOException {
        MainFile mf = new MainFile( fileRootName );
        int type = mf.getShapeTypeByRecNo( 1 );
        mf.close();
        return type;
    }

    /**
     * creates a <tt>Style</tt>. For each class/child contained in a avl file one <tt>Rule</tt>
     * will be created
     *
     * @param legend
     * @param filterCol
     * @return a <tt>Style</tt>.
     */
    // private AbstractStyle[] createPointStyles( Map legend, String filterCol ) {
    // AbstractStyle[] styles = null;
    // return styles;
    // }
    /**
     * creates a <tt>Style</tt>. For each class/child contained in a avl file one <tt>Rule</tt>
     * will be created
     *
     * @param legend
     * @param filterCol
     * @return a <tt>Style</tt>.
     */
    private AbstractStyle createPointStyle( Map legend, String filterCol )
                            throws FilterConstructionException {
        try {
            cl = new AVLPointSymbolCodeList();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        String tmp = (String) legend.get( "Symbols" );
        Map block = blocks.get( "SymList." + tmp );
        List classes = (List) legend.get( "Class" );
        List children = (List) block.get( "Child" );

        List<Rule> list = new ArrayList<Rule>( classes.size() );
        for ( int i = 0; i < classes.size(); i++ ) {
            String clNo = (String) classes.get( i );
            Map clss = blocks.get( "LClass." + clNo );
            String childNo = (String) children.get( i );
            Map<String, Object> child = blocks.get( "CMkSym." + childNo );
            Rule rule = null;
            if ( child == null ) {
                child = blocks.remove( "BMkSym." + childNo );
                rule = createSimplePointRule( clss, child, filterCol );
            } else {
                rule = createComplexPointRule( clss, child, filterCol );
            }
            if ( rule != null ) {
                list.add( rule );
            }
        }
        Rule[] rules = list.toArray( new Rule[list.size()] );
        FeatureTypeStyle fts = StyleFactory.createFeatureTypeStyle( rules );

        String[] t = StringTools.toArray( fileRootName, "/", false );
        String name = "default:" + t[t.length - 1];
        return StyleFactory.createStyle( name, null, null, fts );
    }

    /**
     * creates a Style for a line symbol
     *
     * @param clss
     * @param child
     * @return a Style for a line symbol
     */
    private Rule createSimplePointRule( Map clss, Map<String, Object> child, String filterCol ) {

        if ( clss.get( "IsNoData" ) != null ) {
            return null;
        }

        String label = (String) clss.get( "Label" );
        label = StringTools.validateString( label, "\"" );
        Filter filter = createFilter( clss, filterCol );
        // get foreground color
        String clrIdx = (String) child.get( "Color" );
        Map<String, Object> color = blocks.get( "TClr." + clrIdx );
        double fgOpacity = 1f;
        if ( color != null && "\"Transparent\"".equals( color.get( "Name" ) ) ) {
            fgOpacity = 0f;
        }
        Color fgColor = createColor( color );
        if ( fgColor == null ) {
            fgColor = Color.BLACK;
        }
        // get background color (what ever this means)
        clrIdx = (String) child.get( "BgColor" );
        color = blocks.get( "TClr." + clrIdx );
        double bgOpacity = 1f;
        if ( color != null && "\"Transparent\"".equals( color.get( "Name" ) ) ) {
            bgOpacity = 0f;
        }
        Color bgColor = createColor( color );
        if ( bgColor == null ) {
            bgColor = Color.BLACK;
        }

        double size = Double.parseDouble( (String) child.get( "Size" ) );
        double rotation = Double.parseDouble( (String) child.get( "Angle" ) );

        // creation of a font from a avl file is a triky thing because
        // esri uses their own font names and codes
        // --> don' know if this works
        String fntIdx = (String) child.get( "Font" );
        Map fntMap = blocks.get( "NFont." + fntIdx );
        Font font = createFont( fntMap );
        PointPlacement pp = StyleFactory.createPointPlacement();
        LabelPlacement labelPlacement = StyleFactory.createLabelPlacement( pp );
        TextSymbolizer textSym = StyleFactory.createTextSymbolizer( fgColor, font, filterCol, labelPlacement );

        String patternIdx = (String) ( (ArrayList) child.get( "Pattern" ) ).get( 0 );
        String symbol = cl.getSymbol( patternIdx );

        // create a Mark with a stroke and fill to controll both
        // opacities
        Stroke stroke = StyleFactory.createStroke( fgColor, 1, fgOpacity, null, "mitre", "butt" );
        Fill fill = StyleFactory.createFill( bgColor, bgOpacity );
        Mark mark = StyleFactory.createMark( symbol, fill, stroke );

        Graphic graphic = StyleFactory.createGraphic( null, mark, 1, size, rotation );
        PointSymbolizer pointSym = StyleFactory.createPointSymbolizer( graphic );

        return StyleFactory.createRule( new Symbolizer[] { pointSym, textSym }, label, label, "", null, filter, false,
                                        0, 9E99 );
    }

    private Rule createComplexPointRule( Map clss, Map<String, Object> child, String filterCol )
                            throws FilterConstructionException {

        if ( clss.get( "IsNoData" ) != null ) {
            return null;
        }

        String tmp = (String) child.get( "Symbols" );
        Map block = blocks.get( "SymList." + tmp );
        List children = (List) block.get( "Child" );

        List<Symbolizer> smbls = new ArrayList<Symbolizer>();
        for ( int i = 0; i < children.size(); i++ ) {
            String childNo = (String) children.get( i );
            Map child_ = blocks.get( "CMkSym." + childNo );
            Rule rule = null;
            if ( child_ == null ) {
                child = blocks.remove( "BMkSym." + childNo );
                rule = createSimplePointRule( clss, child, filterCol );
            } else {
                rule = createComplexPointRule( clss, child, filterCol );
            }
            Symbolizer[] sym = rule.getSymbolizers();
            for ( int j = 0; j < sym.length; j++ ) {
                smbls.add( sym[j] );
            }
        }
        Symbolizer[] sym = new Symbolizer[smbls.size()];
        sym = smbls.toArray( sym );

        String label = (String) clss.get( "Label" );
        label = StringTools.validateString( label, "\"" );
        Filter filter = createFilter( clss, filterCol );

        return StyleFactory.createRule( sym, label, label, "", null, filter, false, 0, 9E99 );
    }

    private Font createFont( Map fntMap ) {
        String idx = (String) fntMap.get( "Family" );
        String family = (String) blocks.get( "AVStr." + idx ).get( "S" );
        idx = (String) fntMap.get( "Name" );
        String name = (String) blocks.get( "AVStr." + idx ).get( "S" );
        idx = (String) fntMap.get( "Style" );
        String style = (String) blocks.get( "AVStr." + idx ).get( "S" );
        String weight = (String) fntMap.get( "Weight" );
        String wideness = (String) fntMap.get( "Wideness" );

        boolean italic = style.equals( "Italic" );
        boolean bold = Integer.parseInt( weight ) > 1;

        return StyleFactory.createFont( family, italic, bold, 12 );
    }

    /**
     * creates a <tt>Style</tt>. For each class/child contained in a avl file one <tt>Rule</tt>
     * will be created
     *
     * @param legend
     * @param filterCol
     * @return a <tt>Style</tt>.
     */
    private AbstractStyle createLinesStyle( Map legend, String filterCol ) {
        String tmp = (String) legend.get( "Symbols" );
        Map block = blocks.get( "SymList." + tmp );
        List classes = (List) legend.get( "Class" );
        List children = (List) block.get( "Child" );
        List<Rule> list = new ArrayList<Rule>( classes.size() );
        for ( int i = 0; i < classes.size(); i++ ) {
            String clNo = (String) classes.get( i );
            Map clss = blocks.get( "LClass." + clNo );
            String childNo = (String) children.get( i );
            Map<String, Object> child = blocks.get( "BLnSym." + childNo );

            if ( child == null ) {
                // won't be treated correctly because we can't transform
                // lines with sambols at the moment
                child = blocks.get( "CLnSym." + childNo );
            }
            Rule rule = createLineRule( clss, child, filterCol );
            if ( rule != null ) {
                list.add( rule );
            }
        }
        Rule[] rules = list.toArray( new Rule[list.size()] );
        FeatureTypeStyle fts = StyleFactory.createFeatureTypeStyle( rules );

        String[] t = StringTools.toArray( fileRootName, "/", false );
        String name = "default:" + t[t.length - 1];
        return StyleFactory.createStyle( name, null, null, fts );
    }

    /**
     * creates a Style for a line symbol
     *
     * @param clss
     * @param child
     * @return a Style for a line symbol
     */
    private Rule createLineRule( Map clss, Map<String, Object> child, String filterCol ) {

        if ( clss.get( "IsNoData" ) != null ) {
            return null;
        }

        String label = (String) clss.get( "Label" );
        label = StringTools.validateString( label, "\"" );
        Filter filter = createFilter( clss, filterCol );

        String clrIdx = (String) child.get( "Color" );
        Map<String, Object> color = blocks.get( "TClr." + clrIdx );
        double opacity = 1f;
        if ( color != null && "\"Transparent\"".equals( color.get( "Name" ) ) && color.size() > 0 ) {
            opacity = 0f;
        }
        Color colour = createColor( color );
        if ( colour == null ) {
            colour = Color.BLACK;
        }

        double width = 1.0; // default Width
        if ( child.get( "Width" ) != null ) {
            width = Double.parseDouble( (String) child.get( "Width" ) ) + width;
        }
        // double width = Double.parseDouble( (String)child.get("Width") ) + 1;
        List<String> pl = (List<String>) child.get( "Pattern" );

        if ( child.get( "Pattern" ) == null ) { // create a default pattern List if
            // it is null
            pl = new ArrayList<String>();
            for ( int i = 0; i < 16; i++ ) { // Fill the default List with
                // default values "0.00000000000000"
                pl.add( "0.00000000000000" );
            }
        }

        // List pl = (List)child.get("Pattern");
        float[] dashArray = createDashArray( pl );
        Stroke stroke = StyleFactory.createStroke( colour, width, opacity, dashArray, "mitre", "butt" );
        LineSymbolizer lineSym = StyleFactory.createLineSymbolizer( stroke );

        return StyleFactory.createRule( new Symbolizer[] { lineSym }, label, label, "", null, filter, false, 0, 9E99 );
    }

    /**
     * creates a <tt>Style</tt>. For each class/child contained in a avl file one <tt>Rule</tt>
     * will be created
     *
     * @param legend
     * @param filterCol
     * @return a <tt>Style</tt>.
     */
    private AbstractStyle createPolygonStyle( Map legend, String filterCol )
                            throws Exception {
        String tmp = (String) legend.get( "Symbols" );
        Map block = blocks.get( "SymList." + tmp );
        List classes = (List) legend.get( "Class" );
        List children = (List) block.get( "Child" );
        List<Rule> list = new ArrayList<Rule>( classes.size() );
        for ( int i = 0; i < classes.size(); i++ ) {
            String clNo = (String) classes.get( i );
            Map clss = blocks.get( "LClass." + clNo );
            String childNo = (String) children.get( i );
            Map<String, Object> child = blocks.get( "BShSym." + childNo );
            Rule rule = null;
            if ( child == null ) {
                // VShSym is a vector polygon fill
                child = blocks.get( "VShSym." + childNo );
                if ( child == null ) {
                    rule = createPolygonVecRule( clss, filterCol );
                } else {
                    rule = createPolygonVecRule( clss, child, filterCol );
                }
            } else {
                rule = createPolygonBMPRule( clss, child, filterCol );
            }
            if ( rule != null ) {
                list.add( rule );
            }
            // TODO
            // write special method for vector polygon fill
        }
        Rule[] rules = list.toArray( new Rule[list.size()] );
        FeatureTypeStyle fts = StyleFactory.createFeatureTypeStyle( rules );

        String[] t = StringTools.toArray( fileRootName, "/", false );
        String name = "default:" + t[t.length - 1];
        return StyleFactory.createStyle( name, null, null, fts );
    }

    /**
     * creates a Style for a line symbol
     *
     * @param clss
     * @param child
     * @return a Style for a line symbol
     */
    private Rule createPolygonBMPRule( Map clss, Map<String, Object> child, String filterCol )
                            throws Exception {

        if ( clss.get( "IsNoData" ) != null ) {
            return null;
        }

        String label = (String) clss.get( "Label" );
        label = StringTools.validateString( label, "\"" );

        Filter filter = null;
        if ( filterCol != null ) {
            filter = createFilter( clss, filterCol );
        }
        // get foreground color
        String clrIdx = (String) child.get( "Color" );
        Map<String, Object> color = blocks.get( "TClr." + clrIdx );
        double opacity = 1f;
        if ( color != null && "\"Transparent\"".equals( color.get( "Name" ) ) ) {
            opacity = 0f;
        }

        Color fgColor = createColor( color );
        if ( fgColor == null ) {
            fgColor = Color.BLACK;
        }
        // get color of the outlining stroke
        clrIdx = (String) child.get( "OutlineColor" );
        color = blocks.get( "TClr." + clrIdx );
        double outLOpacity = 1f;
        if ( color != null && "\"Transparent\"".equals( color.get( "Name" ) ) ) {
            outLOpacity = 0f;
        }
        Color outLColor = createColor( color );
        if ( outLColor == null ) {
            outLColor = Color.BLACK;
        }
        // get background color
        clrIdx = (String) child.get( "BgColor" );
        color = blocks.get( "TClr." + clrIdx );
        double bgOpacity = 1f;
        if ( color != null && "\"Transparent\"".equals( color.get( "Name" ) ) ) {
            bgOpacity = 0f;
        }
        Color bgColor = createColor( color );

        // create fill pattern as an image that will be referenced as
        // external graphic
        String stippleIdx = (String) child.get( "Stipple" );
        String src = null;
        if ( stippleIdx != null ) {
            Map stipple = blocks.get( "Stipple." + stippleIdx );
            src = createExternalGraphicFromStipple( stipple, label, fgColor, bgColor );
        }

        double width = Double.parseDouble( (String) child.get( "OutlineWidth" ) ) + 1;
        Stroke stroke = StyleFactory.createStroke( outLColor, width, outLOpacity, null, "mitre", "butt" );
        Fill fill = null;
        if ( stippleIdx != null ) {
            ExternalGraphic eg = StyleFactory.createExternalGraphic( "file:///" + src, "image/gif" );
            Graphic graph = StyleFactory.createGraphic( eg, null, opacity, 10, 0 );
            GraphicFill gf = StyleFactory.createGraphicFill( graph );
            fill = StyleFactory.createFill( fgColor, opacity, gf );
        } else {
            fill = StyleFactory.createFill( fgColor, opacity );
        }
        PolygonSymbolizer polySym = StyleFactory.createPolygonSymbolizer( stroke, fill );
        return StyleFactory.createRule( new Symbolizer[] { polySym }, label, label, "", null, filter, false, 0, 9E99 );
    }

    /**
     * creates a Style for a line symbol
     *
     * @param clss
     * @param child
     * @return a Style for a line symbol
     */
    private Rule createPolygonVecRule( Map clss, Map<String, Object> child, String filterCol )
                            throws Exception {

        if ( clss.get( "IsNoData" ) != null ) {
            return null;
        }

        String label = (String) clss.get( "Label" );
        label = StringTools.validateString( label, "\"" );

        Filter filter = null;
        if ( filterCol != null ) {
            filter = createFilter( clss, filterCol );
        }
        // get foreground color
        String clrIdx = (String) child.get( "Color" );
        Map<String, Object> color = blocks.get( "TClr." + clrIdx );
        double opacity = 1f;
        if ( color != null && "\"Transparent\"".equals( color.get( "Name" ) ) ) {
            opacity = 0f;
        }
        Color fgColor = createColor( color );
        if ( fgColor == null ) {
            fgColor = Color.BLACK;
        }
        // get color of the outlining stroke
        clrIdx = (String) child.get( "OutlineColor" );
        color = blocks.get( "TClr." + clrIdx );
        double outLOpacity = 1f;
        if ( color != null && "\"Transparent\"".equals( color.get( "Name" ) ) ) {
            outLOpacity = 0f;
        }
        Color outLColor = createColor( color );
        if ( outLColor == null ) {
            outLColor = Color.BLACK;
        }
        // get background color
        clrIdx = (String) child.get( "BgColor" );
        color = blocks.get( "TClr." + clrIdx );
        double bgOpacity = 1f;
        if ( color != null && "\"Transparent\"".equals( color.get( "Name" ) ) ) {
            bgOpacity = 0f;
        }
        Color bgColor = createColor( color );

        // create fill pattern as an image that will be referenced as
        // external graphic
        String stippleIdx = (String) child.get( "Stipple" );
        String src = null;
        if ( stippleIdx != null ) {
            Map stipple = blocks.get( "Stipple." + stippleIdx );
            src = createExternalGraphicFromStipple( stipple, label, fgColor, bgColor );
        }

        double width = Double.parseDouble( (String) child.get( "OutlineWidth" ) ) + 1;
        Stroke stroke = StyleFactory.createStroke( outLColor, width, outLOpacity, null, "mitre", "butt" );
        Fill fill = null;
        if ( stippleIdx != null ) {
            ExternalGraphic eg = StyleFactory.createExternalGraphic( "file:///" + src, "image/gif" );
            Graphic graph = StyleFactory.createGraphic( eg, null, opacity, 10, 0 );
            GraphicFill gf = StyleFactory.createGraphicFill( graph );
            fill = StyleFactory.createFill( fgColor, opacity, gf );
        } else {
            fill = StyleFactory.createFill( fgColor, opacity );
        }
        PolygonSymbolizer polySym = StyleFactory.createPolygonSymbolizer( stroke, fill );
        return StyleFactory.createRule( new Symbolizer[] { polySym }, label, label, "", null, filter, false, 0, 9E99 );
    }
    
    /**
     * creates a Style for a line symbol
     *
     * @param clss
     * @return a Style for a line symbol
     */
    private Rule createPolygonVecRule( Map clss, String filterCol )
                            throws Exception {

        if ( clss.get( "IsNoData" ) != null ) {
            return null;
        }

        String label = (String) clss.get( "Label" );
        label = StringTools.validateString( label, "\"" );

        Filter filter = null;
        if ( filterCol != null ) {
            filter = createFilter( clss, filterCol );
        }
        // get foreground color
        double opacity = 1f;
        Color fgColor = Color.BLACK;
        
        // get color of the outlining stroke
        double outLOpacity = 0f;
        Color outLColor =  Color.BLACK;
            
        // create fill pattern as an image that will be referenced as
        // external graphic
        
        Stroke stroke = StyleFactory.createStroke( outLColor, 1, outLOpacity, null, "mitre", "butt" );
        Fill fill = StyleFactory.createFill( fgColor, opacity );
        PolygonSymbolizer polySym = StyleFactory.createPolygonSymbolizer( stroke, fill );
        return StyleFactory.createRule( new Symbolizer[] { polySym }, label, label, "", null, filter, false, 0, 9E99 );
    }

    /**
     * creates an image from a stipple and stores it as a gif image. The method returns the full
     * name of the stored image.
     *
     * @param stipple
     * @return an image from a stipple and stores it as a gif image. The method returns the full
     *         name of the stored image.
     */
    private String createExternalGraphicFromStipple( Map stipple, String label, Color fg, Color bg )
                            throws Exception {

        if ( label != null ) {
            label = label.replace( ' ', '_' );
            label = label.replace( '.', '_' );
            label = label.replace( ';', '_' );
            label = label.replace( ',', '_' );
            label = label.replace( '-', '_' );
            label = label.replace( ':', '_' );
        }
        String tmp = (String) stipple.get( "Columns" );
        int cols = Integer.parseInt( tmp );
        tmp = (String) stipple.get( "Rows" );
        int rows = Integer.parseInt( tmp );

        List bList = (List) stipple.get( "Bits" );
        StringBuffer bStr = new StringBuffer( 1000 );
        for ( int i = 0; i < bList.size(); i++ ) {
            String[] t = StringTools.toArray( ( (String) bList.get( i ) ).trim(), " ", false );
            for ( int j = 0; j < t.length; j++ ) {
                bStr.append( t[j] );
            }
        }

        char[] ch = bStr.toString().toCharArray();

        BufferedImage bi = createFillPattern( cols, rows, ch, fg, bg );

        final String format = "gif";

        String[] t = StringTools.toArray( fileRootName, "/", false );
        String base = t[t.length - 1];
        StringBuffer fileName = new StringBuffer();
        fileName.append( targetDir ).append( base );
        if ( label != null ) {
            fileName.append( "_" ).append( label ).append( "." ).append( format );
        } else {
            fileName.append( "." ).append( format );
        }

        // FileOutputStream fos = new FileOutputStream( targetDir + base + '_' + label + "." +
        // format );
        FileOutputStream fos = new FileOutputStream( fileName.toString() );
        ImageUtils.saveImage( bi, fos, format, 1.0f );
        // Encoders.encodeGif(fos,bi);
        fos.close();

        if ( targetDir.startsWith( "/" ) ) {
            if ( label != null ) {
                return targetDir.substring( 1, targetDir.length() ) + base + '_' + label + "." + format;
            } else {
                return targetDir.substring( 1, targetDir.length() ) + base + "." + format;
            }
        } else {
            if ( label != null ) {
                return targetDir + base + '_' + label + "." + format;
            } else {
                return targetDir + base + "." + format;
            }

        }

    }

    /**
     * creates a <tt>BufferedImage</tt> using the stipple contained in the passed char array.
     *
     * @param cols
     * @param rows
     * @param ch
     * @return a <tt>BufferedImage</tt> using the stipple contained in the passed char array.
     */
    private BufferedImage createFillPattern( int cols, int rows, char[] ch, Color fg, Color bg ) {
        BufferedImage bi = new BufferedImage( cols, rows, BufferedImage.TYPE_INT_ARGB );
        int cntChar = 0;
        byte[] bTmp = null;
        char chr = ' ';
        int hexCnt = 0;
        if ( cols % 8 != 0 ) {
            hexCnt = ( cols / 8 + 1 ) * 8;
        } else {
            hexCnt = cols;
        }
        for ( int i = 0; i < rows; i++ ) {
            for ( int j = 0; j < hexCnt; j++ ) {
                if ( j % 4 == 0 ) {
                    chr = ch[cntChar++];
                    bTmp = getBits( chr );
                }
                if ( j < cols ) {
                    if ( bTmp[j % 4] == 0 ) {
                        if ( bg != null ) {
                            bi.setRGB( j, i, bg.getRGB() );
                        }
                    } else {
                        bi.setRGB( j, i, fg.getRGB() );
                    }
                }
            }
        }
        return bi;
    }

    private byte[] getBits( int ch ) {
        switch ( ch ) {
        case '0':
            return new byte[] { 0, 0, 0, 0 };
        case '1':
            return new byte[] { 0, 0, 0, 1 };
        case '2':
            return new byte[] { 0, 0, 1, 0 };
        case '3':
            return new byte[] { 0, 0, 1, 1 };
        case '4':
            return new byte[] { 0, 1, 0, 0 };
        case '5':
            return new byte[] { 0, 1, 0, 1 };
        case '6':
            return new byte[] { 0, 1, 1, 0 };
        case '7':
            return new byte[] { 0, 1, 1, 1 };
        case '8':
            return new byte[] { 1, 0, 0, 0 };
        case '9':
            return new byte[] { 1, 0, 0, 1 };
        case 'a':
            return new byte[] { 1, 0, 1, 0 };
        case 'b':
            return new byte[] { 1, 0, 1, 1 };
        case 'c':
            return new byte[] { 1, 1, 0, 0 };
        case 'd':
            return new byte[] { 1, 1, 0, 1 };
        case 'e':
            return new byte[] { 1, 1, 1, 0 };
        case 'f':
            return new byte[] { 1, 1, 1, 1 };
        default:
            return new byte[] { 0, 0, 0, 0 };
        }
    }

    /**
     * @return creates a dasharray for constructing a stroke from the pattern entries of a avl
     *         xxxSym. block
     *
     * @param pl
     */
    private float[] createDashArray( List<String> pl ) {
        int cnt = 0;
        for ( int i = 0; i < pl.size(); i++ ) {
            if ( Float.parseFloat( pl.get( i ) ) > 0 ) {
                cnt++;
            } else {
                break;
            }
        }
        float[] pattern = null;
        if ( cnt > 0 ) {
            pattern = new float[cnt];
            for ( int i = 0; i < pattern.length; i++ ) {
                pattern[i] = Float.parseFloat( pl.get( i ) );
            }
        }
        return pattern;
    }

    /**
     * creates a AWT color from a val color block
     *
     * @param color
     * @return a AWT color from a val color block
     */
    private Color createColor( Map<String, Object> color ) {
        StringBuffer hex = new StringBuffer( "0x" );
        if ( color != null && !"\"Transparent\"".equals( color.get( "Name" ) ) ) {
            String red = (String) color.get( "Red" );
            if ( red == null )
                red = "0x0000";
            int c = (int) ( ( Integer.decode( red ).intValue() / 65535f ) * 255 );
            if ( c < 15 ) {
                hex.append( 0 );
            }
            hex.append( Integer.toHexString( c ) );
            String green = (String) color.get( "Green" );
            if ( green == null )
                green = "0x0000";
            c = (int) ( ( Integer.decode( green ).intValue() / 65535f ) * 255 );
            if ( c < 15 ) {
                hex.append( 0 );
            }
            hex.append( Integer.toHexString( c ) );
            String blue = (String) color.get( "Blue" );
            if ( blue == null )
                blue = "0x0000";
            c = (int) ( ( Integer.decode( blue ).intValue() / 65535f ) * 255 );
            if ( c < 15 ) {
                hex.append( 0 );
            }
            hex.append( Integer.toHexString( c ) );
        } else {
            // hex.append("000000");
            return null;
        }
        return Color.decode( hex.toString() );
    }

    private Filter createFilter( Map clss, String filterCol ) {

        if ( clss.get( "Label" ) == null ) {
            return null;
        }
        Filter filter = null;

        String maxN = (String) clss.get( "MaxStr" );
        String minN = (String) clss.get( "MinStr" );
        if ( maxN != null && minN != null ) {
            filter = createStringFilter( clss, filterCol );
        } else {
            filter = createNumberFilter( clss, filterCol );
        }

        return filter;
    }

    private Filter createStringFilter( Map clss, String filterCol ) {
        Filter filter;
        Operation oper = null;
        String maxN = (String) clss.get( "MaxStr" );
        String minN = (String) clss.get( "MinStr" );
        maxN = maxN.substring( 1, maxN.length() - 1 );
        minN = minN.substring( 1, minN.length() - 1 );
        if ( maxN.equals( minN ) ) {
            oper = new PropertyIsCOMPOperation( OperationDefines.PROPERTYISEQUALTO,
                                                new PropertyName( new QualifiedName( filterCol ) ), new Literal( minN ) );
        } else {
            ArrayList<Operation> list = new ArrayList<Operation>();
            Operation op = new PropertyIsCOMPOperation( OperationDefines.PROPERTYISLESSTHANOREQUALTO,
                                                        new PropertyName( new QualifiedName( filterCol ) ),
                                                        new Literal( maxN ) );
            list.add( op );
            op = new PropertyIsCOMPOperation( OperationDefines.PROPERTYISGREATERTHANOREQUALTO,
                                              new PropertyName( new QualifiedName( filterCol ) ), new Literal( minN ) );
            list.add( op );

            oper = new LogicalOperation( OperationDefines.AND, list );
        }
        filter = new ComplexFilter( oper );
        return filter;
    }

    private Filter createNumberFilter( Map clss, String filterCol ) {

        String maxN = (String) clss.get( "MaxNum" );
        String minN = (String) clss.get( "MinNum" );
        if ( maxN == null ) {
            maxN = "9E99";
        }
        if ( minN == null ) {
            minN = "-9E99";
        }
        double t1 = Double.parseDouble( minN );
        double t2 = Double.parseDouble( maxN );
        Operation oper = null;
        if ( t1 == t2 ) {
            // if t1 == t2 no range is defined and so an 'is equal to'
            // opertaion must be used
            if ( ( (int) t1 ) == t1 ) {
                // if no significant fraction values are present
                // cast the value to int
                oper = new PropertyIsCOMPOperation( OperationDefines.PROPERTYISEQUALTO,
                                                    new PropertyName( new QualifiedName( filterCol ) ),
                                                    new Literal( "" + ( (int) t1 ) ) );
            } else {
                oper = new PropertyIsCOMPOperation( OperationDefines.PROPERTYISEQUALTO,
                                                    new PropertyName( new QualifiedName( filterCol ) ),
                                                    new Literal( "" + t1 ) );
            }
        } else {
            // if t1 != t2 range of valid values is defined and so an so two
            // operation (one for each border of the range) are used
            ArrayList<Operation> list = new ArrayList<Operation>();
            Operation op = new PropertyIsCOMPOperation( OperationDefines.PROPERTYISLESSTHANOREQUALTO,
                                                        new PropertyName( new QualifiedName( filterCol ) ),
                                                        new Literal( maxN ) );
            list.add( op );
            op = new PropertyIsCOMPOperation( OperationDefines.PROPERTYISGREATERTHANOREQUALTO,
                                              new PropertyName( new QualifiedName( filterCol ) ), new Literal( minN ) );
            list.add( op );

            oper = new LogicalOperation( OperationDefines.AND, list );

        }

        return new ComplexFilter( oper );

    }

    private static String[] getAVLFiles( String dir ) {
        File file = new File( dir );
        return file.list( new DFileFilter() );

    }

    private static void printHelp() {

        System.out.println( "Converts ESRI *.avl files to OGC SLD documents. The current version of " );
        System.out.println( "this tool isn't able to convert each and every construction that is " );
        System.out.println( "possible with an *.avl file. But most of the common expressions will be mapped." );
        System.out.println( "Supported are *.avl files for point, lines and polygons." );
        System.out.println( "" );
        System.out.println( "-sourceFile -> full path to the *.avl file to be converted. " );
        System.out.println( "           in the same directory a shapefile with the same name as the *.avl" );
        System.out.println( "               file must exist! (conditional, -sourceFile or -sourceDir must " );
        System.out.println( "               be defined)" );
        System.out.println( "-sourceDir  -> directory containing one or more *.avl files. for each existing" );
        System.out.println( "               *.avl file a shapefile with the same base name must exist. " );
        System.out.println( "               (conditional, -sourceFile or -sourceDir must be defined)" );
        System.out.println( "-targetDir  -> directory where the created SLD document(s) will be stored. (optional)" );
        System.out.println( "-h  -> print this help" );
    }

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main( String[] args )
                            throws Exception {

        Map<String, String> map = new HashMap<String, String>();

        for ( int i = 0; i < args.length; i += 2 ) {
            map.put( args[i], args[i + 1] );
        }

        if ( map.get( "-sourceFile" ) == null && map.get( "-sourceDir" ) == null && map.get( "-h" ) == null ) {
            System.out.println( "-sourceFile or -sourceDir must be defined!" );
            System.out.println();
            printHelp();
            System.exit( 1 );
        }

        if ( map.get( "-h" ) != null ) {
            printHelp();
            System.exit( 0 );
        }

        String targetDir = ".";
        String[] sourceFiles = null;
        if ( map.get( "-sourceFile" ) != null ) {
            sourceFiles = new String[] { map.get( "-sourceFile" ) };
            // set the default target directory to the sourceFile's directory
            targetDir = sourceFiles[0].substring( 0, sourceFiles[0].lastIndexOf( "/" ) );
        }

        if ( sourceFiles == null ) {
            String sourceDir = map.get( "-sourceDir" );
            sourceFiles = getAVLFiles( sourceDir );
            for ( int i = 0; i < sourceFiles.length; i++ ) {
                sourceFiles[i] = sourceDir + '/' + sourceFiles[i];
            }
            // set the default target directory to the source directory
            targetDir = sourceDir;

        }

        // String targetDir = ".";
        if ( map.get( "-targetDir" ) != null ) {
            targetDir = map.get( "-targetDir" );
        }

        for ( int i = 0; i < sourceFiles.length; i++ ) {
            System.out.println( "processing: " + sourceFiles[i] );
            int pos = sourceFiles[i].lastIndexOf( '.' );
            String file = sourceFiles[i].substring( 0, pos );
            AVL2SLD avl = new AVL2SLD( file, targetDir );
            avl.read();
            StyledLayerDescriptor sld = avl.getStyledLayerDescriptor();
            String[] t = StringTools.toArray( file, "/", false );
            FileWriter fos = new FileWriter( targetDir + '/' + t[t.length - 1] + ".xml" );
            fos.write( ( (Marshallable) sld ).exportAsXML() );
            fos.close();
        }
    }

    /**
     *
     *
     * @version $Revision$
     * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
     */
    private static class DFileFilter implements FilenameFilter {
        /**
         * @param f
         * @return true if accepted
         */
        public boolean accept( File f, String name ) {
            int pos = name.lastIndexOf( "." );
            String ext = name.substring( pos + 1 );
            return ext.toUpperCase().equals( "AVL" );
        }
    }

}
