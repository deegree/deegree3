//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.client.mdeditor.mapping;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.client.mdeditor.configuration.Configuration;
import org.deegree.client.mdeditor.configuration.ConfigurationManager;
import org.deegree.client.mdeditor.configuration.ConfigurationException;
import org.deegree.client.mdeditor.configuration.mapping.MappingParser;
import org.deegree.client.mdeditor.io.DataIOException;
import org.deegree.client.mdeditor.io.Utils;
import org.deegree.client.mdeditor.model.DataGroup;
import org.deegree.client.mdeditor.model.FormField;
import org.deegree.client.mdeditor.model.mapping.MappingInformation;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class SchemaManager {

    private static final Logger LOG = getLogger( SchemaManager.class );

    private static Map<URL, MappingInformation> mappingMap = new HashMap<URL, MappingInformation>();

    /**
     * @param mappingURLs
     * @return the mappings
     */
    public static List<MappingInformation> getMappings( List<URL> mappingURLs ) {
        List<MappingInformation> mappings = new ArrayList<MappingInformation>();
        for ( URL mappingURL : mappingURLs ) {
            if ( !mappingMap.containsKey( mappingURL ) ) {
                try {
                    mappingMap.put( mappingURL, MappingParser.parseMapping( mappingURL ) );
                } catch ( ConfigurationException e ) {
                    mappingMap.put( mappingURL, null );
                    LOG.debug( "Could not read mapping from file " + mappingURL + ", insert null for this file.", e );
                    LOG.error( "Could not read mapping from file " + mappingURL, e.getMessage() );

                }
            }
            if ( mappingMap.get( mappingURL ) != null ) {
                mappings.add( mappingMap.get( mappingURL ) );
            }
        }
        return mappings;
    }

    /**
     * exports the given data in the selected mapping and writes the output in a file
     * 
     * @param mappingId
     *            the id of the mapping to use for export
     * @param formFields
     *            the form fields to export
     * @param configuration
     * @param map
     * @return the name of the created file
     * @throws DataIOException
     * @throws ConfigurationException
     */
    public static String export( String id, String mappingId, Configuration configuration, String confId,
                                 Map<String, List<DataGroup>> dataGroups )
                            throws DataIOException, ConfigurationException {
        String fileName = id;
        if ( fileName == null ) {
            fileName = Utils.createId();
        }
        // TODO: other formats!?
        if ( !fileName.endsWith( ".xml" ) ) {
            fileName = fileName + ".xml";
        }

        MappingInformation mapping = null;
        for ( MappingInformation mi : mappingMap.values() ) {
            if ( mi != null && mappingId.equals( mi.getId() ) ) {
                mapping = mi;
            }
        }

        if ( mapping != null ) {
            try {
                File f = new File( ConfigurationManager.getConfiguration().getExportDir(), fileName );
                if ( !f.exists() ) {

                    f.createNewFile();
                }
                MappingExporter.export( f, mapping, configuration, confId, dataGroups );

            } catch ( Exception e ) {
                LOG.debug( "Could not export dataset: ", e );
                throw new DataIOException( "Could not export dataset. The following error occured: " + e.getMessage() );
            }
        } else {
            throw new DataIOException( "Could not find schema mapping with id " + mappingId );
        }
        return fileName;
    }

    /**
     * validates the given form fields against the given mapping
     * 
     * @param selectedMapping
     *            the id of the mapping to use for validation
     * @param formFields
     *            the form fields to validate
     */
    public void validate( String selectedMapping, Map<String, FormField> formFields ) {
    }

}
