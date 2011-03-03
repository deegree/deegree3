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

import static org.apache.commons.io.IOCase.INSENSITIVE;
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
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
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
        if ( basedir == null ) {
            LOG.info( "No basedir set - skipping processing of console managed annotations." );
            return;
        }
        File parentFile = new File( basedir );
        if ( !parentFile.exists() && !parentFile.mkdirs() ) {
            LOG.warn( "Target directory could not be created: {}", parentFile );
        }
    }

    private void find( Element e )
                            throws IOException {
        ConsoleManaged managed = e.getAnnotation( ConsoleManaged.class );

        if ( managed != null ) {
            IOFileFilter filter = new NameFileFilter( new String[] { "CVS", ".svn" }, INSENSITIVE );
            filter = new NotFileFilter( filter );
            FileUtils.copyDirectory( new File( basedir, managed.directory() ),
                                     new File( "target/classes/META-INF/deegree/console" ), filter );
        }

        for ( Element e2 : e.getEnclosedElements() ) {
            find( e2 );
        }
    }

    @Override
    public boolean process( Set<? extends TypeElement> annotations, RoundEnvironment roundEnv ) {
        if ( basedir == null ) {
            return false;
        }

        for ( Element e : roundEnv.getRootElements() ) {
            try {
                find( e );
            } catch ( IOException e1 ) {
                LOG.warn( "IO error when copying console files: {}", e1.getLocalizedMessage() );
            }
        }

        return false;
    }

}
