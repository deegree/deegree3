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

package org.deegree.ogcwebservices.csw;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.mail.EMailMessage;
import org.deegree.framework.mail.MailHelper;
import org.deegree.framework.mail.SendMailException;
import org.deegree.framework.mail.UnknownMimeTypeException;
import org.deegree.i18n.Messages;

/**
 * <code>DetectUnvalidCouplings</code>
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class DetectInvalidCouplings {

    private static final ILogger LOG = LoggerFactory.getLogger( DetectInvalidCouplings.class );

    private static final DetectInvalidCouplings instance = new DetectInvalidCouplings();

    private Timer timer;

    private DetectInvalidCouplings() {
        timer = new Timer();
        timer.schedule( new DetectInvalidCouplingsTask(), 1, 86400000 );
    }

    public static DetectInvalidCouplings getInstance() {
        return instance;
    }

    private class DetectInvalidCouplingsTask extends TimerTask {
        /*
         * (non-Javadoc)
         *
         * @see java.util.TimerTask#run()
         */
        @Override
        public void run() {
            try {
                Connection conn = CSW202PropertiesAccess.getConnection();
                if ( conn != null ) {
                    Map<String, List<String>> result = new HashMap<String, List<String>>();
                    Statement st = conn.createStatement();
                    ResultSet rsUuids = st.executeQuery( "SELECT distinct(uuidref) FROM operateson WHERE uuidref IS NOT null" );

                    while ( rsUuids.next() ) {
                        String uuidref = rsUuids.getString( "uuidref" );
                        PreparedStatement psCount = conn.prepareStatement( "SELECT count(fileIdentifier) as count FROM fileIdentifier WHERE fileIdentifier = ?" );
                        psCount.setString( 1, uuidref );
                        ResultSet rsCount = psCount.executeQuery();
                        if ( !rsCount.next() || rsCount.getInt( "count" ) == 0 ) {
                            List<String> tmp;
                            PreparedStatement psIds = conn.prepareStatement( "SELECT f.fileidentifier as id FROM fileidentifier f, md_metadata m, operateson o WHERE o.fk_serviceidentification = m.id AND o.uuidref =  ? AND o.fk_serviceidentification = m.id AND m.fk_fileidentifier = f.id" );
                            psIds.setString( 1, uuidref );
                            ResultSet rsIds = psIds.executeQuery();
                            while ( rsIds.next() ) {
                                String id = rsIds.getString( "id" );
                                if ( result.containsKey( uuidref ) ) {
                                    tmp = result.get( uuidref );
                                } else {
                                    tmp = new ArrayList<String>();
                                }
                                tmp.add( id );
                                result.put( uuidref, tmp );
                            }
                        }
                    }
                    printResult( result );
                    conn.close();
                }
            } catch ( SQLException e ) {
                LOG.logError( "Could not connect with database", e );
            }
        }

        private void printResult( Map<String, List<String>> result ) {
            StringBuffer sb = new StringBuffer();
            if ( result != null && result.size() > 0 ) {
                sb.append( Messages.getMessage( "CSW_INVALID_SERVICES_DETECTED" ) );
                for ( String uuidref : result.keySet() ) {
                    List<String> uuidrefs = result.get( uuidref );
                    String ids = "";
                    for ( String id : uuidrefs ) {
                        if ( ids.length() > 0 ) {
                            ids = ids + ", ";
                        }
                        ids = ids + id;
                    }
                    sb.append( Messages.getMessage( "CSW_INVALID_SERVICES_DETECTED_ENTRY", ids, uuidref ) );
                }

                try {
                    sendMail( sb.toString() );
                } catch ( SendMailException e ) {
                    LOG.logError( "Could not send inform admin about unknown coupled resources!", e );
                }
            } else {
                sb.append( Messages.getMessage( "CSW_NO_INVALID_SERVICES_DETECTED" ) );
            }
            LOG.logInfo( sb.toString() );
        }
    }

    private void sendMail( String message )
                            throws SendMailException {
        String sender = "do-not-return@deegree-csw.de";
        String receiver = CSW202PropertiesAccess.getString( "admin.email" );
        String subject = "Detected some unknown coupled resources";
        EMailMessage mm = new EMailMessage( sender, receiver, subject, message );
        try {
            mm.setMimeType( "text/html" );
        } catch ( UnknownMimeTypeException e ) {
            LOG.logError( e.getMessage() );
        }
        String smtpHost = CSW202PropertiesAccess.getString( "smtphost" );
        MailHelper.createAndSendMail( mm, smtpHost );
    }

    public static void main( String[] args ) {
        // Class.forName( "DetectInvalidCouplings");
    }

}
