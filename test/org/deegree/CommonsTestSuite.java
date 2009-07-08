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

package org.deegree;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>CommonsTestSuite</code> the test suite for all test defined in commons
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
@RunWith(Suite.class)
@SuiteClasses( {
org.deegree.commons.filter.Filter110XMLAdapterTest.class,
org.deegree.commons.utils.ArrayToolsTest.class,
org.deegree.commons.utils.FileUtilsTest.class,
org.deegree.commons.utils.StringToolsTest.class,
org.deegree.commons.utils.math.MathUtilsTest.class,
org.deegree.commons.utils.time.DateUtilsTest.class,
org.deegree.commons.xml.XMLAdapterTest.class,
org.deegree.coverage.raster.GeoTIFFTest.class,
org.deegree.coverage.raster.RasterReferenceTest.class,
org.deegree.coverage.raster.WorldFileAccessTest.class,
org.deegree.crs.components.UnitTest.class,
org.deegree.crs.configuration.deegree.DatabaseCRSProviderTest.class,
org.deegree.crs.configuration.deegree.DeegreeCRSProviderTest.class,
org.deegree.crs.configuration.deegree.TransformationAccuracyTest.class,
org.deegree.crs.configuration.gml.GMLCRSProviderTest.class,
org.deegree.crs.coordinatesystems.CompoundCRSTest.class,
org.deegree.crs.projections.azimuthal.LambertAzimuthalTest.class,
org.deegree.crs.projections.azimuthal.StereographicAlternativeTest.class,
org.deegree.crs.projections.azimuthal.StereographicTest.class,
org.deegree.crs.projections.conic.LambertConformalConicTest.class,
org.deegree.crs.projections.cylindric.TransverseMercatorTest.class,
org.deegree.crs.transformations.TransformationAccuracyTest.class,
org.deegree.feature.FilterEvaluationTest.class,
org.deegree.geometry.gml.GML311CurveSegmentParserTest.class,
org.deegree.geometry.gml.GML311GeometryParserTest.class,
org.deegree.geometry.gml.GML311SurfacePatchParserTest.class,
org.deegree.feature.gml.GMLFeatureExporterTest.class,
org.deegree.feature.gml.GMLFeatureParserTest.class,
org.deegree.geometry.gml.GMLGeometryExporterTest.class,
org.deegree.feature.gml.schema.XSModelGMLAnalyzerTest.class,
org.deegree.feature.gml.validation.GML311GeometryValidatorTest.class,
org.deegree.feature.xpath.FeatureXPathTest.class,
org.deegree.geometry.GeometryTest.class,
org.deegree.geometry.linearization.CurveLinearizerTest.class,
org.deegree.geometry.validation.GeometryValidatorTest.class
} )
public class CommonsTestSuite {

    private static final int DOT_CLASS_LENGTH = ".class".length();

    /**
     * Logger for test cases
     */
    private static final Logger LOG = LoggerFactory.getLogger( CommonsTestSuite.class );

    private static final String packageName = "org.deegree.";

    /**
     * @param args
     *            will not be evaluated
     */
    public static void main( String[] args ) {
        buildTestSuite( CommonsTestSuite.class );
    }

    public static void buildTestSuite( Class<?> suite ) {
        SuiteClasses annotation = suite.getAnnotation( SuiteClasses.class );
        Class<?>[] values = annotation.value();
        List<String> testClasses = new LinkedList<String>();
        try {
            Class<?> test = Class.forName( suite.getName() );
            URL resource = test.getResource( suite.getSimpleName() + ".class" );
            if ( resource == null ) {
                LOG.error( "Could not load resource: configuration.properties this is akward" );
            } else {
                File f = new File( resource.getFile() );
                String parent = f.getParent();
                f = new File( parent );
                if ( f.exists() ) {
                    LOG.info( f.getAbsolutePath() + " exists trying to load classes from: " + parent );
                    findAndAddClasses( parent, testClasses, f, new CustomFileFilter() );
                } else {
                    LOG.error( f.getAbsolutePath() + " does not denote the root directory of deegree test suite." );
                }
            }
        } catch ( ClassNotFoundException e ) {
            LOG.error( e.getMessage(), e );
        }
        StringBuilder sb = new StringBuilder( "Current annotation tag:\n" );
        sb.append( "@SuiteClasses( {\n" );
        int i = 0;
        for ( Class<?> cl : values ) {
            if ( testClasses.contains( cl.getName() ) ) {
                sb.append( cl.getName() ).append( ".class" );
                if ( ++i < values.length ) {
                    sb.append( ",\n" );
                }
                testClasses.remove( cl.getName() );
            }
        }
        sb.append( "\n} )" );
        if ( testClasses.size() > 0 ) {
            sb.append( "\n\nFound " ).append( testClasses.size() ).append( " new test classes:\n" );
            Collections.sort( testClasses );
            i = 0;
            for ( String c : testClasses ) {
                sb.append( c );
                if ( ++i < testClasses.size() ) {
                    sb.append( ",\n" );
                }
            }
            for ( Class<?> cl : values ) {
                testClasses.add( cl.getName() );
            }
            Collections.sort( testClasses );
            sb.append( "\n\nThe new annotation should be following:\n" );
            sb.append( "@SuiteClasses( {\n" );
            i = 0;
            for ( String c : testClasses ) {
                sb.append( c ).append( ".class" );
                if ( ++i < testClasses.size() ) {
                    sb.append( ",\n" );
                }
            }
            sb.append( "\n} )" );
        } else {
            sb.append( "\n\nNo new test classes found, the current @SuiteClasses annotation tag is up-to-date.\n\n" );
        }

        System.out.println( sb.toString() );
    }

    /**
     * @param classes
     * @param parent
     */
    private static void findAndAddClasses( final String prefix, List<String> classes, File parent,
                                           CustomFileFilter filter ) {
        if ( parent != null ) {
            File[] sons = parent.listFiles( filter );
            for ( File tmp : sons ) {
                if ( tmp.isDirectory() ) {
                    findAndAddClasses( prefix, classes, tmp, filter );
                } else {
                    String className = tmp.getAbsoluteFile().toString().substring( prefix.length() + 1 );
                    className = className.substring( 0, className.length() - DOT_CLASS_LENGTH );
                    className = className.replace( File.separatorChar, '.' );
                    className = packageName + className;
                    // sometimes on windows this is the default behavior
                    className = className.replace( '/', '.' );
                    Class<?> testClass = null;
                    try {
                        testClass = Class.forName( className, false, CommonsTestSuite.class.getClassLoader() );
                    } catch ( ClassNotFoundException cnfe ) {
                        LOG.error( cnfe.getLocalizedMessage(), cnfe );
                    }
                    if ( testClass == null ) {
                        LOG.error( "Could not load class: " + className );
                    } else {
                        Method[] methods = testClass.getMethods();
                        boolean hasTestMethod = false;
                        for ( Method m : methods ) {
                            if ( m != null ) {
                                if ( m.getName() != null && ( m.getModifiers() == Modifier.PUBLIC ) ) {
                                    if ( /* m.getName().startsWith( "test" ) || */m.getAnnotation( Test.class ) != null ) {
                                        hasTestMethod = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if ( hasTestMethod ) {
                            LOG.debug( "adding class: " + className );
                            classes.add( className );
                        } else {
                            LOG.debug( "Not adding class: " + className + " because it is does not have a test method." );
                        }

                    }
                }
            }
        }

    }

    /**
     *
     * The <code>CustomFileFilter</code> class adds functionality to the filefilter mechanism of the JFileChooser.
     *
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     *
     * @author last edited by: $Author$
     *
     * @version $Revision$, $Date$
     *
     */
    static class CustomFileFilter implements java.io.FileFilter {

        public boolean accept( File pathname ) {
            if ( pathname.isDirectory() ) {
                return true;
            }

            String extension = getExtension( pathname );
            if ( extension != null ) {
                if ( "class".equals( extension.trim() ) ) {
                    return true;
                }
            }
            return false;
        }

        private String getExtension( File f ) {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf( '.' );

            if ( i > 0 && i < s.length() - 1 ) {
                ext = s.substring( i + 1 ).toLowerCase();
            }
            return ext;
        }
    }
}
