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
package org.deegree.commons.utils.log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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

/**
 * <code>DebuggingAnnotationProcessor</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */

@SupportedAnnotationTypes(value = { "org.deegree.commons.utils.log.DebuggingNotes" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions("log4j.outputfile")
public class DebuggingAnnotationProcessor extends AbstractProcessor {

    private String outFile;

    @Override
    public void init( ProcessingEnvironment env ) {
        super.init( env );
        outFile = env.getOptions().get( "log4j.outputfile" );
    }

    @Override
    public boolean process( Set<? extends TypeElement> annotations, RoundEnvironment roundEnv ) {
        try {
            PrintWriter out = new PrintWriter( new OutputStreamWriter( new FileOutputStream( outFile, true ), "UTF-8" ) );

            for ( TypeElement el : annotations ) {
                // we expect notes on a per class level, so the list should always have one element exactly
                Set<? extends Element> list = roundEnv.getElementsAnnotatedWith( el );
                Element cls = list.iterator().next();
                DebuggingNotes notes = cls.getAnnotation( DebuggingNotes.class );

                // this seems to be the only way to get to the actual qualified name?
                String qname = cls.toString();

                if ( !notes.error().isEmpty() ) {
                    out.println( "# " + notes.error() );
                    out.println( "#log4j.logger." + qname + "=ERROR" );
                    out.println();
                }
                if ( !notes.warn().isEmpty() ) {
                    out.println( "# " + notes.warn() );
                    out.println( "#log4j.logger." + qname + "=WARN" );
                    out.println();
                }
                if ( !notes.info().isEmpty() ) {
                    out.println( "# " + notes.info() );
                    out.println( "#log4j.logger." + qname + "=INFO" );
                    out.println();
                }
                if ( !notes.debug().isEmpty() ) {
                    out.println( "# " + notes.debug() );
                    out.println( "#log4j.logger." + qname + "=DEBUG" );
                    out.println();
                }
                if ( !notes.trace().isEmpty() ) {
                    out.println( "# " + notes.trace() );
                    out.println( "#log4j.logger." + qname + "=TRACE" );
                    out.println();
                }
            }

            out.close();
        } catch ( UnsupportedEncodingException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( FileNotFoundException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

}
