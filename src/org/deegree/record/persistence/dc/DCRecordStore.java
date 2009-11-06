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
package org.deegree.record.persistence.dc;

import org.deegree.protocol.csw.CSWConstants.ResultType;
import org.deegree.protocol.csw.CSWConstants.SetOfReturnableElements;

import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMNamespace;
import org.deegree.commons.configuration.JDBCConnections;
import org.deegree.commons.configuration.PooledConnection;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.utils.time.DateUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.record.persistence.RecordStore;
import org.deegree.record.persistence.RecordStoreException;

/**
 * {@link RecordStore} implementation of Dublin Core.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class DCRecordStore implements RecordStore {

    public String output;

    private final QName typeNames = new QName( "", "Record", "csw" );

    public DCRecordStore() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.record.persistence.RecordStore#describeRecord(javax.xml.stream.XMLStreamWriter)
     */
    @Override
    public void describeRecord() {

        // statt n File wird hier die DB angefragt!
        File file = new File( "/home/thomas/workspace/d3_core/src/org/deegree/record/persistence/dc/dc.xsd" );

        // if(typeNames.equals( new QName("","Record", "csw") )){
        XMLAdapter ada = new XMLAdapter( file );

        System.out.println( ada.toString() );
        output = ada.toString();
        OMNamespace elem = ada.getRootElement().getDefaultNamespace();
        // ada.getNamespaceContext( ada.getRootElement() );

        // this.typeNames = new QName(elem.getNamespaceURI());
        // }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.record.persistence.RecordStore#destroy()
     */
    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.record.persistence.RecordStore#init()
     */
    @Override
    public void init()
                            throws RecordStoreException {

        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.record.persistence.RecordStore#getTypeNames()
     */
    @Override
    public QName getTypeName() {

        // int i = SELECT ID FROM formattype WHERE type = 'csw:Record'
        // if(i == 1){ new QName("", "Record", "csw");

        return typeNames;
    }

    /**
     * @return the output
     */
    public String getOutput() {
        return output;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.record.persistence.RecordStore#getRecords(javax.xml.stream.XMLStreamWriter,
     * javax.xml.namespace.QName)
     */
    @Override
    public void getRecords( XMLStreamWriter writer, QName typeName, SetOfReturnableElements returnatbleElement,
                            JDBCConnections con, ResultType resultType, String conId )
                            throws SQLException, XMLStreamException {

        String formatType = null;
        switch ( returnatbleElement ) {

        case brief:
            formatType = "RecordBrief";
            break;
        case summary:
            formatType = "RecordSummary";
            break;
        case full:
            formatType = "RecordFull";
            break;
        }// muss dann noch gecatcht werden

        switch ( resultType ) {
        case results:

            doResultsOnGetRecord( writer, typeName, formatType, returnatbleElement, con );
            break;
        case hits:

            doHitsOnGetRecord( writer, typeName, formatType, returnatbleElement, con, conId );
            break;
        case validate:

            doValidateOnGetRecord( writer, typeName, returnatbleElement, con );
            break;
        }

    }

    /**
     * 
     * @param writer
     * @param typeName
     * @param formatType
     * @param returnatbleElement
     * @param con
     * @throws SQLException
     * @throws XMLStreamException
     */
    private void doHitsOnGetRecord( XMLStreamWriter writer, QName typeName, String formatType, SetOfReturnableElements returnatbleElement,
                                    JDBCConnections con, String conId )
                            throws SQLException, XMLStreamException {

        int countRows = 0;
        String selectStMt = "SELECT count(id) FROM datasets";

        // ConnectionManager.addConnections( con );
        for ( PooledConnection pool : con.getPooledConnection() ) {
            Connection conn = ConnectionManager.getConnection( conId );
            ResultSet rs = conn.createStatement().executeQuery( selectStMt );

            while ( rs.next() ) {
                countRows = rs.getInt( 1 );
                System.out.println( rs.getInt( 1 ) );
            }

            writer.writeAttribute( "elementSet", returnatbleElement.name() );

            // writer.writeAttribute( "recordSchema", "");

            writer.writeAttribute( "numberOfRecordsMatched", Integer.toString( countRows ) );

            writer.writeAttribute( "numberOfRecordsReturned", Integer.toString( 0 ) );

            writer.writeAttribute( "nextRecord", Integer.toString( 1 ) );

            writer.writeAttribute( "expires", DateUtils.formatISO8601Date( new Date() ) );

            conn.close();
        }

    }

    /**
     * 
     * @param writer
     * @param typeName
     * @param formatType
     * @param returnatbleElement
     * @param con
     * @throws SQLException
     * @throws XMLStreamException
     */
    private void doResultsOnGetRecord( XMLStreamWriter writer, QName typeName, String formatType,
                                       SetOfReturnableElements returnatbleElement, JDBCConnections con )
                            throws SQLException, XMLStreamException {
        String selectStMt = "SELECT count(id) FROM datasets";

        for ( PooledConnection pool : con.getPooledConnection() ) {
            Connection conn = ConnectionManager.getConnection( pool.getId() );
            ResultSet rs = conn.createStatement().executeQuery( selectStMt );

            while ( rs.next() ) {

            }

            conn.close();
        }

    }

    /**
     * 
     * @param writer
     * @param typeName
     * @param returnatbleElement
     * @param con
     * @throws SQLException
     * @throws XMLStreamException
     */
    private void doValidateOnGetRecord( XMLStreamWriter writer, QName typeName,
                                        SetOfReturnableElements returnatbleElement, JDBCConnections con )
                            throws SQLException, XMLStreamException {
        // TODO Auto-generated method stub

    }

}
