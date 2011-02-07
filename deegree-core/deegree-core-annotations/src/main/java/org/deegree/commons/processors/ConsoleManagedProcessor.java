//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.commons.processors;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.apache.commons.io.FileUtils;
import org.deegree.commons.annotations.ConsoleManaged;
import org.slf4j.Logger;

/**
 * <code>LoggingAnnotationProcessor</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */

@SupportedAnnotationTypes(value = { "org.deegree.commons.annotations.ConsoleManaged" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions( { "basedir" })
public class ConsoleManagedProcessor extends AbstractProcessor {

    private static final Logger LOG = getLogger( ConsoleManagedProcessor.class );

    private String basedir;

    @Override
    public void init( ProcessingEnvironment env ) {
        super.init( env );
        basedir = env.getOptions().get( "basedir" );
        File parentFile = new File( basedir );
        if ( !parentFile.exists() && !parentFile.mkdirs() ) {
            LOG.warn( "Target directory could not be created: {}", parentFile );
        }
    }

    private void find( Element e )
                            throws IOException {
        ConsoleManaged managed = e.getAnnotation( ConsoleManaged.class );

        if ( managed != null ) {
            FileUtils.copyDirectory( new File( basedir, managed.directory() ),
                                     new File( "target/classes/META-INF/deegree/console" ) );
        }

        for ( Element e2 : e.getEnclosedElements() ) {
            find( e2 );
        }
    }

    @Override
    public boolean process( Set<? extends TypeElement> annotations, RoundEnvironment roundEnv ) {

        for ( Element e : roundEnv.getRootElements() ) {
            try {
                find( e );
            } catch ( IOException e1 ) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        return false;
    }

}
