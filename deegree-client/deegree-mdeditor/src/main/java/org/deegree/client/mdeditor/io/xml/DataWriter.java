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
package org.deegree.client.mdeditor.io.xml;

import static org.deegree.client.mdeditor.io.xml.XMLDataHandler.FILE_SUFFIX;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.client.mdeditor.configuration.Configuration;
import org.deegree.client.mdeditor.io.DataIOException;
import org.deegree.client.mdeditor.io.Utils;
import org.deegree.client.mdeditor.model.FormElement;
import org.deegree.client.mdeditor.model.FormField;
import org.deegree.client.mdeditor.model.FormGroup;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class DataWriter {

    private static final Logger LOG = getLogger( DataWriter.class );

    protected static final String DG_ELEM = "DataGroup";

    protected static final String DS_ELEM = "Dataset";

    /**
     * Writes the dataset. If a dataset with the given id exists, the dataset will be overwritten.
     * 
     * @param id
     *            the id of the dataset to write, if the id is null a new id is created
     * @param formGroups
     *            a list of form groups to write as dataset
     * @return the id of the written dataset
     * @throws DataIOException
     */
    static String writeDataset( String id, List<FormGroup> formGroups )
                            throws DataIOException {
        if ( id == null ) {
            id = Utils.createId();
        }
        String fileName = id;
        if ( !fileName.endsWith( FILE_SUFFIX ) ) {
            fileName = fileName + FILE_SUFFIX;
        }
        File file = new File( Configuration.getFilesDirURL(), fileName );
        if ( !file.exists() ) {
            try {
                file.createNewFile();
            } catch ( IOException e ) {
                LOG.debug( "Could not create file " + file.getAbsolutePath(), e );
                throw new DataIOException( "Could not create a new file " + file.getAbsolutePath() );
            }
        }

        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        try {
            FileOutputStream fos = new FileOutputStream( file );
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter( fos );

            writer.writeStartDocument();
            writer.writeStartElement( DS_ELEM );

            for ( FormGroup fg : formGroups ) {
                if ( !fg.isReferenced() ) {
                    append( writer, fg );
                }
            }

            writer.writeEndElement();
            writer.writeEndDocument();
            writer.close();
        } catch ( FileNotFoundException e ) {
            LOG.debug( "Could not find file " + file.getAbsolutePath() + " to write. ", e );
            throw new DataIOException( "Could not find file " + file.getAbsolutePath() + " to write. " + e.getMessage() );
        } catch ( XMLStreamException e ) {
            LOG.debug( "Could not write file " + file.getAbsolutePath() + ". ", e );
            throw new DataIOException( "Could not write file " + file.getAbsolutePath() + ". " + e.getMessage() );
        }
        return id;
    }

    /**
     * 
     * Writes the form group. If a form group with the given id exists, the form group will be overwritten.
     * 
     * @param id
     *            the id of the form group to write, if the id is null a new id is created
     * @param formGroup
     *            the form group to write
     * @return the id of the written form group
     * @throws DataIOException
     */
    static String writeDataGroup( String id, FormGroup formGroup )
                            throws DataIOException {
        if ( id == null ) {
            id = Utils.createId();
        }
        String fileName = id;
        if ( !id.endsWith( FILE_SUFFIX ) ) {
            fileName = id + FILE_SUFFIX;
        }
        File dir = new File( Configuration.getFilesDirURL(), formGroup.getId() );
        if ( !dir.exists() ) {
            dir.mkdir();
        }
        File file = new File( dir, fileName );
        if ( !file.exists() ) {
            try {
                file.createNewFile();
            } catch ( IOException e ) {
                LOG.debug( "Could not create file " + file.getAbsolutePath(), e );
                throw new DataIOException( "Could not create a new file " + file.getAbsolutePath() );
            }
        }

        try {
            LOG.debug( "Write values of form group with id " + formGroup.getId() + " in file " + file.toString() );
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            FileOutputStream fos = new FileOutputStream( file );
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter( fos );

            writer.writeStartDocument();
            writer.writeStartElement( DS_ELEM );
            writer.writeAttribute( "id", formGroup.getId() );

            append( writer, formGroup );

            writer.writeEndElement();
            writer.writeEndDocument();
            writer.close();
        } catch ( FileNotFoundException e ) {
            // should never happen!
            LOG.debug( "Could not find file " + file.getAbsolutePath(), e );
            throw new DataIOException( "Could not find file " + file.getAbsolutePath() );
        } catch ( XMLStreamException e ) {
            LOG.debug( "Could not write file " + file.getAbsolutePath() + ". ", e );
            throw new DataIOException( "Could not write file " + file.getAbsolutePath() + ". " + e.getMessage() );
        }

        return id;
    }

    private static void append( XMLStreamWriter writer, FormGroup fg )
                            throws XMLStreamException {
        // writer.writeStartElement( FG_ELEM );
        // writer.writeAttribute( "id", fg.getId() );

        for ( FormElement fe : fg.getFormElements() ) {
            if ( fe instanceof FormField ) {
                FormField ff = (FormField) fe;
                Object value = ff.getValue();
                if ( value != null ) {

                    if ( value instanceof List<?> && ( (List<?>) value ).size() > 0 ) {
                        writer.writeStartElement( "Element" );
                        writeId( writer, ff.getPath().toString() );
                        for ( Object o : (List<?>) value ) {
                            writeValue( writer, String.valueOf( o ) );
                        }
                        writer.writeEndElement();
                    } else if ( value instanceof Object[] && ( (Object[]) value ).length > 0 ) {
                        writer.writeStartElement( "Element" );
                        writeId( writer, ff.getPath().toString() );
                        Object[] array = (Object[]) value;
                        for ( int i = 0; i < array.length; i++ ) {
                            writeValue( writer, String.valueOf( array[i] ) );
                        }
                        writer.writeEndElement();
                    } else if ( ( String.valueOf( value ) ).length() > 0 ) {
                        writer.writeStartElement( "Element" );
                        writeId( writer, ff.getPath().toString() );
                        writeValue( writer, String.valueOf( value ) );
                        writer.writeEndElement();
                    }
                }
            } else if ( fe instanceof FormGroup ) {
                append( writer, (FormGroup) fe );
            }
        }
    }

    private static void writeValue( XMLStreamWriter writer, String value )
                            throws XMLStreamException {
        writer.writeStartElement( "value" );
        writer.writeCharacters( value );
        writer.writeEndElement();
    }

    private static void writeId( XMLStreamWriter writer, String path )
                            throws XMLStreamException {
        writer.writeStartElement( "id" );
        writer.writeCharacters( path );
        writer.writeEndElement();
    }
}
