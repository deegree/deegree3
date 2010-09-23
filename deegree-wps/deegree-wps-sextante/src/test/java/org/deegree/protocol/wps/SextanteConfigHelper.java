//$HeadURL: http://svn.wald.intevation.org/svn/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.protocol.wps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.parameters.Parameter;

/**
 * This class writes the process content to the command line und uses the descriptions of SEXTANTE xml files.
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * @author last edited by: $Author: pabel $
 * 
 * @version $Revision: $, $Date: $
 */
public class SextanteConfigHelper {

    // private static String baseURL = "https://svn.forge.osor.eu/svn/sextante/tags/sextante_0.5/docs/xml/en/";

    private static final String rootURL = "/home/pabel/workspace/";

    private static final String baseURL = rootURL + "sextante/trunk/docs/xml/en/";

    private static final String baseURLAlt1 = rootURL + "sextante/tags/sextante_0.5/docs/xml/en/";

    private static final String baseURLAlt2 = rootURL + "sextante/trunk/docs/xml/es/";

    private static final String baseURLAlt3 = rootURL + "sextante/tags/sextante_0.5/docs/xml/es/";

    public SextanteConfigHelper() {
        Sextante.initialize();
    }

    public void writeAlgorithmAbstract( GeoAlgorithm alg )
                            throws IOException {

        // open process element
        System.out.print( "<Process id=\"" + alg.getCommandLineName() + "\">" );

        // create url
        String url = baseURL + alg.getClass().getPackage().getName() + "/" + alg.getCommandLineName() + ".xml";
        String urlAlt1 = baseURLAlt1 + alg.getClass().getPackage().getName() + "/" + alg.getCommandLineName() + ".xml";
        String urlAlt2 = baseURLAlt2 + alg.getClass().getPackage().getName() + "/" + alg.getCommandLineName() + ".xml";
        String urlAlt3 = baseURLAlt3 + alg.getClass().getPackage().getName() + "/" + alg.getCommandLineName() + ".xml";

        File xmlFile = new File( url );

        if ( !xmlFile.isFile() )
            xmlFile = new File( urlAlt1 );

        if ( !xmlFile.isFile() )
            xmlFile = new File( urlAlt2 );

        if ( !xmlFile.isFile() )
            xmlFile = new File( urlAlt3 );

        if ( !xmlFile.isFile() ) {
            if ( alg.getCommandLineName().equals( "vectorfieldcalculator" ) )
                xmlFile = new File( baseURLAlt2
                                    + "es.unex.sextante.tables.vectorfieldcalculator/vectorfieldcalculator.xml" );
        }

        // System.out.println( "<!-- " + xmlFile.getAbsoluteFile() + xmlFile.isFile() + "--!>" );

        if ( xmlFile.isFile() ) {

            FileReader fr = new FileReader( xmlFile );
            BufferedReader br = new BufferedReader( fr );

            String xmlFileAsString = "";

            String line;
            while ( ( line = br.readLine() ) != null ) {
                String lineModified = line.replace( "&#10;", "" ).replace( "&#237;", "i" ).replace( "&#186;", "o" ).replace(
                                                                                                                             "&#225;",
                                                                                                                             "a" ).replace(
                                                                                                                                            "&#243;",
                                                                                                                                            "o" ).replace(
                                                                                                                                                           "&#183;",
                                                                                                                                                           "." ).replace(
                                                                                                                                                                          "&#233;",
                                                                                                                                                                          "e" ).replace(
                                                                                                                                                                                         "\n",
                                                                                                                                                                                         "" ).replace(
                                                                                                                                                                                                       "&#241;",
                                                                                                                                                                                                       "" ).replace(
                                                                                                                                                                                                                     "*",
                                                                                                                                                                                                                     "" ).replace(
                                                                                                                                                                                                                                   "&#250;",
                                                                                                                                                                                                                                   "u" ).replace(
                                                                                                                                                                                                                                                  "&gt;",
                                                                                                                                                                                                                                                  "]" ).replace(
                                                                                                                                                                                                                                                                 "&lt;",
                                                                                                                                                                                                                                                                 "[" ).replace(
                                                                                                                                                                                                                                                                                "&amp;",
                                                                                                                                                                                                                                                                                "[AND-SYMBOL]" ).replace(
                                                                                                                                                                                                                                                                                                          "%",
                                                                                                                                                                                                                                                                                                          "[PERCENT-SYMBOL]" );
                xmlFileAsString += lineModified;
            }

            // create a HashMap with descriptions
            HashMap<String, String> descs = new HashMap<String, String>();

            String[] elemArray = xmlFileAsString.split( "<element " );
            for ( int i = 1; i < elemArray.length; i++ ) {

                String[] values = elemArray[i].split( "\"" );

                if ( values.length > 3 ) {

                    String key = values[1];
                    String value = values[3];
                    if ( value.equals( "" ) ) {
                        if ( values.length > 5 )
                            value = values[5];
                    }

                    descs.put( key, value );
                }

            }

            String algAbstract = descs.get( "DESCRIPTION" );

            // open and close abstract element
            System.out.print( "<Abstract>" + algAbstract + "</Abstract>" );

            // open input parameters element
            System.out.print( "<InputParameters>" );

            // write input parameter descriptions
            ParametersSet paramSet = alg.getParameters();
            for ( int i = 0; i < paramSet.getNumberOfParameters(); i++ ) {
                Parameter param = paramSet.getParameter( i );
                String desc = descs.get( param.getParameterName() );

                // open parameter element
                System.out.print( "<Parameter id=\"" + param.getParameterName() + "\">" );

                // open and close abstract element
                System.out.print( "<Abstract>" + desc + "</Abstract>" );

                // add selection values
                if ( alg.getCommandLineName().equals( "vectoraddfield" )
                     && param.getParameterName().equals( "FIELD_TYPE" ) )
                    System.out.println( "<SelectionValue id=\"INTEGER\" value=\"0\" /> <SelectionValue id=\"DOUBLE\" value=\"1\" /><SelectionValue id=\"STRING\" value=\"2\" />" );
                else if ( alg.getCommandLineName().equals( "splitlineswithpoints" )
                          && param.getParameterName().equals( "METHOD" ) )
                    System.out.println( "<SelectionValue id=\"START_VALUE\" value=\"0\" /><SelectionValue id=\"LAST_VALUE\" value=\"1\" />" );
                else if ( alg.getCommandLineName().equals( "smoothlines" )
                          && param.getParameterName().equals( "INTERMEDIATE_POINTS" ) )
                    System.out.println( " <SelectionValue id=\"NUMBER_OF_POINTS\" value=\"1-10\" />" );
                else if ( alg.getCommandLineName().equals( "smoothlines" )
                          && param.getParameterName().equals( "CURVE_TYPE" ) )
                    System.out.println( "<SelectionValue id=\"NATURAL_CUBIC_SPLINES\" value=\"0\" /><SelectionValue id=\"BEZIER_CURVES\" value=\"1\" /><SelectionValue id=\"BSPLINES\" value=\"2\" />" );
                else if ( alg.getCommandLineName().equals( "generateroutes" )
                          && param.getParameterName().equals( "METHOD" ) )
                    System.out.println( "<SelectionValue id=\"CREATION_METHOD_BROWNIAN_(doesn't work)\" value=\"0\" /><SelectionValue id=\"CREATION_METHOD_RECOMBINE_(works)\" value=\"1\" />" );
                else if ( alg.getCommandLineName().equals( "fixeddistancebuffer" )
                          && param.getParameterName().equals( "TYPES" ) )
                    System.out.println( "<SelectionValue id=\"BUFFER_OUTSIDE_POLY\" value=\"0\" /><SelectionValue id=\"BUFFER_INSIDE_POLY\" value=\"1\" /><SelectionValue id=\"BUFFER_INSIDE_OUTSIDE_POLY\" value=\"2\" />" );
                else if ( alg.getCommandLineName().equals( "fixeddistancebuffer" )
                          && param.getParameterName().equals( "RINGS" ) )
                    System.out.println( "<SelectionValue id=\"NUMBER_OF_RINGS\" value=\"0-*\" />" );

                // open close element
                System.out.print( "</Parameter>" );

            }

            // close input parameters element
            System.out.print( "</InputParameters>" );

            // open output parameters element
            System.out.print( "<OutputParameters>" );

            // write output parameter descriptions
            OutputObjectsSet outputSet = alg.getOutputObjects();
            for ( int i = 0; i < outputSet.getOutputObjectsCount(); i++ ) {
                Output output = outputSet.getOutput( i );
                String desc = descs.get( output.getName() );

                // open parameter element
                System.out.print( "<Parameter id=\"" + output.getName() + "\">" );

                // open and close abstract element
                System.out.print( "<Abstract>" + desc + "</Abstract>" );

                // open close element
                System.out.print( "</Parameter>" );
            }
            // close output parameters element
            System.out.print( "</OutputParameters>" );
        } else {
            // open and close abstract element
            System.out.print( "<Abstract></Abstract>" );

            // open input parameters element
            System.out.print( "<InputParameters>" );

            // write input parameter descriptions
            ParametersSet paramSet = alg.getParameters();
            for ( int i = 0; i < paramSet.getNumberOfParameters(); i++ ) {
                Parameter param = paramSet.getParameter( i );

                // open parameter element
                System.out.print( "<Parameter id=\"" + param.getParameterName() + "\">" );

                // open and close abstract element
                System.out.print( "<Abstract></Abstract>" );

                // open close element
                System.out.print( "</Parameter>" );

            }

            // close input parameters element
            System.out.print( "</InputParameters>" );

            // open output parameters element
            System.out.print( "<OutputParameters>" );

            // write output parameter descriptions
            OutputObjectsSet outputSet = alg.getOutputObjects();
            for ( int i = 0; i < outputSet.getOutputObjectsCount(); i++ ) {
                Output output = outputSet.getOutput( i );

                // open parameter element
                System.out.print( "<Parameter id=\"" + output.getName() + "\">" );

                // open and close abstract element
                System.out.print( "<Abstract></Abstract>" );

                // open close element
                System.out.print( "</Parameter>" );
            }
            // close output parameters element
            System.out.print( "</OutputParameters>" );
        }

        // open process element
        System.out.println( "</Process>" );

    }

    public void writeAlgorithmAbstract( String commandLineName )
                            throws IOException {
        GeoAlgorithm alg = Sextante.getAlgorithmFromCommandLineName( commandLineName );

        if ( alg != null )
            writeAlgorithmAbstract( alg );
    }

    public static void main( String[] args )
                            throws IOException {

        SextanteConfigHelper t = new SextanteConfigHelper();

        // t.writeAlgorithmAbstract( "vectorfieldcalculator" );

        // write array
        String[] supportedAlgs = new String[] { "boundingbox", "centroids", "changelinedirection", "cleanpointslayer",
                                               "cleanvectorlayer", "clip", "countpoints", "delaunay", "difference",
                                               "extractendpointsoflines", "extractnodes", "geometricproperties",
                                               "geometricpropertieslines", "geometriestopoints", "intersection",
                                               "nodelines", "pointcoordinates", "polygonize", "polygonstopolylines",
                                               "polylinestopolygons", "polylinestosinglesegments", "removeholes",
                                               "removerepeatedgeometries", "splitmultipart", "splitpolylinesatnodes",
                                               "symdifference", "union", "vectormean", "clipbyrectangle",
                                               "groupnearfeatures", "joinadjacentlines", "linestoequispacedpoints",
                                               "perturbatepointslayer", "snappoints", "transform",
                                               "vectorspatialcluster", "fixeddistancebuffer", "generateroutes",
                                               "simplifylines", "simplifypolygons", "smoothlines",
                                               "splitlineswithpoints", "vectoraddfield", "vectorcluster",
                                               "vectorfieldcalculator" };

        for ( int i = 0; i < supportedAlgs.length; i++ ) {
            t.writeAlgorithmAbstract( supportedAlgs[i] );
            // System.out.println( "" );
        }

    }
}
