package org.deegree.commons.modules;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;

public class ClassloaderTest {

    public static void main( String[] args )
                            throws ClassNotFoundException, IOException {

        URL url = new URL(
                           "file:/home/schneider/.m2/repository/org/deegree/deegree-core-base/3.1-SNAPSHOT/deegree-core-base-3.1-SNAPSHOT.jar" );
        URLClassLoader loader = new URLClassLoader( Collections.singletonList( url ).toArray( new URL[1] ) );

        Enumeration<URL> urls = loader.findResources( "org.deegree.geometry.Geometry" );
        while ( urls.hasMoreElements() ) {
            URL ur = urls.nextElement();
            System.out.println( "HUHU: " + ur );
        }
    }

}