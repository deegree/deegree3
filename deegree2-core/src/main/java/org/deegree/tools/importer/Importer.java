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
package org.deegree.tools.importer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;

/**
 *
 * This generic importer handles the way of inserting data. First data are loaded from source
 * container, followed by structural validation, transformation and content validation. Finally the
 * exporter insert data in target container. The classes the loader needed for this five steps must
 * be specified in 'classes.properties'.
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class Importer {

    private static final ILogger LOG = LoggerFactory.getLogger( Importer.class );

    private Loader loader;

    private StructValidator structValidator;

    private ContentValidator contentValidator;

    private Transformer transformer;

    private Exporter exporter;

    /**
     * The constructor reads the classes which should be used during import process from the
     * property file 'classes.properties' and instantiate them. Make sure that the desired classes
     * are configured in this file!
     */
    public Importer() {
        this( "classes.properties" );
    }

    public Importer( String propertiesFile ) {

        LOG.logInfo( Messages.getString( "Importer.BEGIN_INSTANTIATION" ) );
        // read properties file
        Properties classes = new Properties();
        try {
            InputStream is = Importer.class.getResourceAsStream( propertiesFile );
            InputStreamReader isr = new InputStreamReader( is );
            BufferedReader br = new BufferedReader( isr );
            String line = null;
            while ( ( line = br.readLine() ) != null ) {
                if ( !line.trim().startsWith( "#" ) ) {
                    String[] tmp = StringTools.toArray( line.trim(), "=", false );
                    classes.put( tmp[0], tmp[1] );
                }
            }
            // instantiate classes essential for importer
            String loaderClass = classes.getProperty( "loader" );
            LOG.logInfo( Messages.getString( "Importer.LOADER_CLASS", loaderClass ) );
            loader = (Loader) Class.forName( loaderClass ).newInstance();

            String structValidatorClass = classes.getProperty( "structValidator" );
            LOG.logInfo( Messages.getString( "Importer.STRUCTVALIDATOR_CLASS", structValidatorClass ) );
            structValidator = (StructValidator) Class.forName( structValidatorClass ).newInstance();

            String transformerClass = classes.getProperty( "transformer" );
            LOG.logInfo( Messages.getString( "Importer.TRANSFORMER_CLASS", transformerClass ) );
            transformer = (Transformer) Class.forName( transformerClass ).newInstance();

            String contentValidatorClass = classes.getProperty( "contentValidator" );
            LOG.logInfo( Messages.getString( "Importer.CONTENTVALIDATOR_CLASS", contentValidatorClass ) );
            contentValidator = (ContentValidator) Class.forName( contentValidatorClass ).newInstance();

            String exporterClass = classes.getProperty( "exporter" );
            LOG.logInfo( Messages.getString( "Importer.EXPORTER_CLASS", exporterClass ) );
            exporter = (Exporter) Class.forName( exporterClass ).newInstance();

            LOG.logInfo( Messages.getString( "Importer.END_INSTANTIATION" ) );

        } catch ( IOException e ) {
            LOG.logError( Messages.getString( "Importer.ERROR_READING_CLASSES_PROPERTIES", e.getMessage() ) );
        } catch ( ClassNotFoundException e ) {
            LOG.logError( Messages.getString( "Importer.ERROR_FIND_CLASSES", e.getMessage() ) );
        } catch ( InstantiationException e ) {
            LOG.logError( Messages.getString( "Importer.ERROR_INSTANTIATION", e.getMessage() ) );
        } catch ( IllegalAccessException e ) {
            LOG.logError( Messages.getString( "Importer.ERROR_ACCESS", e.getMessage() ) );
        }

    }

    /**
     * handles the import
     *
     * @param importObjects
     *            List of objects to import
     */
    public void handleImport( List<Object> importObjects ) {
        int numberObjects = importObjects.size();
        LOG.logInfo( Messages.getString( "Importer.BEGIN_IMPORT", numberObjects ) );
        long startTimeAll = System.currentTimeMillis();
        int successCounter = 0;
        int counter = 1;
        for ( Iterator iter = importObjects.iterator(); iter.hasNext(); ) {
            long startTime = System.currentTimeMillis();
            Object element = (Object) iter.next();
            Object loadedObject = load( element );
            if ( loadedObject != null ) {
                LOG.logInfo( Messages.getString( "Importer.OBJECT_LOADED", counter ) );
                if ( validateStructure( loadedObject ) ) {
                    Object transformedObject = transform( loadedObject );
                    if ( transformedObject != null ) {
                        LOG.logInfo( Messages.getString( "Importer.OBJECT_TRANSFORMED", counter ) );
                        if ( validateContent( transformedObject ) ) {
                            if ( export( transformedObject ) ) {
                                LOG.logInfo( Messages.getString( "Importer.OBJECT_EXPORTED", counter ) );
                                ++successCounter;
                            } else {
                                LOG.logInfo( Messages.getString( "Importer.ERROR_EXPORT" ) );
                            }
                        }
                    }
                }
                if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                    long endTime = System.currentTimeMillis();
                    LOG.logDebug( "Time needed to import one object: ", endTime - startTime );
                }
            }
            LOG.logInfo( Messages.getString( "Importer.IMPORT_ONE_OBJECT_END" ) );
            counter++;
        }
        long endTimeAll = System.currentTimeMillis();
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            String msg = StringTools.concat( 200, "Time needed to import ", successCounter, " object(s): " );
            LOG.logDebug( msg, endTimeAll - startTimeAll );
        }
        LOG.logInfo( Messages.getString( "Importer.IMPORT_END", successCounter, numberObjects ) );
    }

    private Object load( Object importObject ) {
        return loader.loadObject( importObject );
    }

    private boolean validateStructure( Object importObject ) {
        return structValidator.validate( importObject );
    }

    private Object transform( Object importObject ) {
        return transformer.transform( importObject );
    }

    private boolean validateContent( Object importObject ) {
        return contentValidator.validate( importObject );
    }

    private boolean export( Object importObject ) {
        return exporter.export( importObject );
    }

}
