package org.deegree.services.controller;

/**
 * 
 * Ecas extended Credentials containing an ecas ticket
 * 
 * @author <a href="erben@lat-lon.de">Alexander Erben</a>
 * @author last edited by: $Author: erben $
 * 
 * @version $Revision: $, $Date: $
 */
public class EcasCredentials extends Credentials {
    
    public EcasCredentials( String user, String password, String sessionId, String ticket ) {
        super( user, password, sessionId );
        this.ticket = ticket;
    }

    private String ticket;

    public String getTicket() {
        return ticket;
    }

    public void setTicket( String ticket ) {
        this.ticket = ticket;
    }
}
