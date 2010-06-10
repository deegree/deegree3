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

import static org.deegree.client.mdeditor.io.xml.XMLDataHandler.*;
import static org.deegree.commons.xml.stax.StAXParsingHelper.getText;
import static org.deegree.commons.xml.stax.StAXParsingHelper.moveReaderToFirstMatch;
import static org.deegree.commons.xml.stax.StAXParsingHelper.nextElement;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.client.mdeditor.io.DataIOException;
import org.deegree.client.mdeditor.io.Utils;
import org.deegree.client.mdeditor.model.DataGroup;
import org.deegree.client.mdeditor.model.Dataset;
import org.slf4j.Logger;

/**
 * 
 * reading a dataset or single form group
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class DataReader {

    private static final Logger LOG = getLogger( DataReader.class );

    private static QName dsQName = new QName( DS_ELEM );

    private static QName elemQName = new QName( ELEM_ELEM );

    private static QName dgQName = new QName( DG_ELEM );

    private static QName grpQName = new QName( GRP_ELEM );

    private static QName id = new QName( ID_ELEM );

    private static QName value = new QName( VALUE_ELEM );

    /**
     * 
     * @param file
     *            the file to read
     * @return the values of the form group stored in the given file
     * @throws DataIOException
     */
    static Map<String, Object> readDataGroup( File file )
                            throws DataIOException {
        if ( !file.exists() ) {
            throw new DataIOException( "File " + file.getAbsolutePath() + " does not exist." );
        }
        LOG.debug( "Read dataset from file " + file.getAbsolutePath() );
        try {

            XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( new FileReader( file ) );

            Map<String, Object> result = new HashMap<String, Object>();

            if ( !moveReaderToFirstMatch( xmlStream, dsQName ) ) {
                throw new DataIOException( "could not read datagroup" + file.getAbsolutePath()
                                           + ": root element does not exist" );
            }

            while ( !( xmlStream.isEndElement() && dsQName.equals( xmlStream.getName() ) ) ) {
                if ( elemQName.equals( xmlStream.getName() ) ) {
                    readElement( xmlStream, result );
                } else {
                    nextElement( xmlStream );
                }
            }

            return result;
        } catch ( Exception e ) {
            LOG.debug( "Could not read file " + file.getAbsolutePath() + ": ", e );
            throw new DataIOException( "Could not read file " + file.getAbsolutePath() + ": " + e.getMessage() );
        }
    }

    static Dataset readDataset( File file )
                            throws DataIOException {
        if ( !file.exists() ) {
            throw new DataIOException( "File " + file.getAbsolutePath() + " does not exist." );
        }
        LOG.debug( "Read dataset from file " + file.getAbsolutePath() );
        try {
            Dataset ds = new Dataset();

            XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( new FileReader( file ) );

            Map<String, Object> result = new HashMap<String, Object>();

            if ( !moveReaderToFirstMatch( xmlStream, dsQName ) ) {
                throw new DataIOException( "could not read dataset " + file.getAbsolutePath()
                                           + ": root element does not exist" );
            }

            while ( !( xmlStream.isEndElement() && dsQName.equals( xmlStream.getName() ) ) ) {
                if ( elemQName.equals( xmlStream.getName() ) ) {
                    readElement( xmlStream, result );
                } else if ( dgQName.equals( xmlStream.getName() ) ) {
                    parseDataGroup( xmlStream, ds );
                } else {
                    nextElement( xmlStream );
                }
            }

            ds.setValues( result );
            return ds;
        } catch ( Exception e ) {
            e.printStackTrace();
            LOG.debug( "Could not read file " + file.getAbsolutePath() + ": ", e );
            throw new DataIOException( "Could not read file " + file.getAbsolutePath() + ": " + e.getMessage() );
        }
    }

    private static void readElement( XMLStreamReader xmlStream, Map<String, Object> result )
                            throws XMLStreamException {
        nextElement( xmlStream );
        String path = getText( xmlStream, id, null, true );
        if ( path == null ) {
            LOG.info( "missing id; ignore this element" );
        } else {
            List<String> values = new ArrayList<String>();
            while ( xmlStream.isStartElement() && "value".equals( xmlStream.getLocalName() ) ) {
                values.add( getText( xmlStream, value, null, true ) );
            }
            if ( values.size() > 0 ) {
                Object value = values.size() > 1 ? values : values.get( 0 );
                result.put( path, value );
            }
        }
        nextElement( xmlStream );
    }

    private static void parseDataGroup( XMLStreamReader xmlStream, Dataset ds )
                            throws XMLStreamException {
        nextElement( xmlStream );
        String grpId = getText( xmlStream, id, null, true );
        if ( grpId == null ) {
            LOG.info( "missing id; ignore datagroup" );
            return;
        }
        List<DataGroup> dgs = new ArrayList<DataGroup>();
        while ( grpQName.equals( xmlStream.getName() ) ) {
            System.out.println( xmlStream.getLocalName() );
            nextElement( xmlStream );
            DataGroup dg = new DataGroup( Utils.createId() );
            Map<String, Object> values = new HashMap<String, Object>();
            while ( elemQName.equals( xmlStream.getName() ) ) {
                readElement( xmlStream, values );
            }
            dg.setValues( values );
            dgs.add( dg );
            nextElement( xmlStream );
        }
        ds.addDataGroup( grpId, dgs );
        nextElement( xmlStream );
    }

}
