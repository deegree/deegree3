package org.deegree;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

//$HeadURL: $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

/**
 * <code>CommonsTestSuite</code> the test suite for all test defined in commons
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
@RunWith(Suite.class)
@SuiteClasses( {
org.deegree.commons.utils.ArrayToolsTest.class,
org.deegree.commons.utils.FileUtilsTest.class,
org.deegree.commons.utils.StringToolsTest.class,
org.deegree.model.coverage.raster.RasterEnvelopeTest.class,
org.deegree.model.crs.components.UnitTest.class,
org.deegree.model.crs.configuration.DeegreeCRSProviderTest.class,
org.deegree.model.crs.coordinatesystems.CompoundCRSTest.class,
org.deegree.model.crs.projections.azimuthal.LambertAzimuthalTest.class,
org.deegree.model.crs.projections.azimuthal.StereographicAlternativeTest.class,
org.deegree.model.crs.projections.azimuthal.StereographicTest.class,
org.deegree.model.crs.projections.conic.LambertConformalConicTest.class,
org.deegree.model.crs.projections.cylindric.TransverseMercatorTest.class,
org.deegree.model.crs.transformations.TransformationTest.class,
org.deegree.model.geometry.GeometryTest.class
} )
public class CommonsTestSuite {

    private static final int DOT_CLASS_LENGTH = ".class".length();

    /**
     * Logger for test cases
     */
    private static final Log LOG = LogFactory.getLog( CommonsTestSuite.class );

    private static final String packageName = "org.deegree.";

    /**
     * @param args will not be evaluated
     */
    public static void main( String[] args ) {
        CommonsTestSuite suite = new CommonsTestSuite();
        SuiteClasses annotation = suite.getClass().getAnnotation( SuiteClasses.class );
        Class<?>[] values = annotation.value();
        List<String> testClasses = new LinkedList<String>();
        try {
            Class<?> test = Class.forName( CommonsTestSuite.class.getName() );
            URL resource = test.getResource( "CommonsTestSuite.class" );
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
                sb.append( c ).append( ".class");
                if ( ++i < testClasses.size() ) {
                    sb.append( ",\n" );
                }
            }
            sb.append( "\n} )" );
        } else {
            sb.append( "\n\nNo new test classes found, the current @SuiteClasses annotation tag is up-to-date.\n\n");
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
                    className = className.replace( File.pathSeparatorChar, '.' );
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
                                    if ( m.getName().startsWith( "test" ) || m.getAnnotation( Test.class ) != null ) {
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
