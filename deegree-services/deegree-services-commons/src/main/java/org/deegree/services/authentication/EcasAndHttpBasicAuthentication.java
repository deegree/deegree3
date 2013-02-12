package org.deegree.services.authentication;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.util.Base64;
import org.apache.axiom.soap.SOAPEnvelope;
import org.deegree.services.controller.Credentials;
import org.deegree.services.controller.CredentialsProvider;
import org.deegree.services.controller.EcasCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Credentials Provider for http basic auth and ecas credentials
 * 
 * @author <a href="erben@lat-lon.de">Your Name</a>
 * @author last edited by: $Author: erben $
 * 
 * @version $Revision: $, $Date: $
 */
public class EcasAndHttpBasicAuthentication implements CredentialsProvider {

    private static Logger LOG = LoggerFactory.getLogger( EcasAndHttpBasicAuthentication.class );

    @Override
    public Credentials doKVP( Map<String, String> normalizedKVPParams, HttpServletRequest req,
                                  HttpServletResponse response )
                            throws SecurityException {
        String authorizationHeader = req.getHeader( "authorization" );
        if ( authorizationHeader != null ) {
            return doBasicAuthentication( req, response );
        }
        String ecasHeader = req.getHeader( "ECAS_ST" );
        if ( ecasHeader != null ) {
            return doEcasAuthentication( req, response );
        }
        return null;
    }

    private EcasCredentials doEcasAuthentication( HttpServletRequest req, HttpServletResponse response ) {
        LOG.debug( "header: " + req.getHeader( "ECAS_ST" ) );
        String ecasHeader = req.getHeader( "ECAS_ST" );
        if ( ecasHeader != null ) {
            String ticket = req.getHeader( "ECAS_ST" );
            LOG.debug( "ECAS ticket: " + ticket );
            return new EcasCredentials( null, null, null, ticket );
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.services.controller.CredentialProvider#doXML(javax.xml.stream.XMLStreamReader,
     * javax.servlet.http.HttpServletRequest)
     */
    @Override
    public Credentials doXML( XMLStreamReader reader, HttpServletRequest req, HttpServletResponse response )
                            throws SecurityException {
        String authorizationHeader = req.getHeader( "authorization" );
        if ( authorizationHeader != null ) {
            return doBasicAuthentication( req, response );
        }
        String ecasHeader = req.getHeader( "ECAS_ST" );
        if ( ecasHeader != null ) {
            return doEcasAuthentication( req, response );
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.services.controller.CredentialProvider#doSOAP(javax.xml.stream.XMLStreamReader,
     * javax.servlet.http.HttpServletRequest)
     */
    @Override
    public Credentials doSOAP( SOAPEnvelope soapDoc, HttpServletRequest req )
                            throws SecurityException {
        throw new UnsupportedOperationException( "SOAPSecurity is not implementable in HTTP BASIC!" );
        // OMElement requestHeader = soapDoc.getHeader();
        // SoapHeaderXMLAdapter soapXMLHeader = new SoapHeaderXMLAdapter();
        // soapXMLHeader.setRootElement( requestHeader );
        // SoapHeader soapHeader = soapXMLHeader.parseHeader();
        //
        // LOG.info( soapHeader.getUsername() + " " + soapHeader.getPassword() );
        // return new Credentials( soapHeader.getUsername(), soapHeader.getPassword() );

    }

    /**
     * Swapped method to provide the basic authentication.
     * 
     * @param req
     * @param response
     */
    private Credentials doBasicAuthentication( HttpServletRequest req, HttpServletResponse response ) {
        // look for HTTP Basic Authentification info
        LOG.debug( "header: " + req.getHeader( "authorization" ) );
        String authorizationHeader = req.getHeader( "authorization" );
        if ( authorizationHeader != null ) {
            if ( authorizationHeader.startsWith( "Basic " ) || authorizationHeader.startsWith( "BASIC " ) ) {
                LOG.debug( "Found basic authorization header: '" + authorizationHeader + "'." );
                // 6: length of "Basic "
                String encodedCreds = authorizationHeader.substring( 6 ).trim();
                LOG.debug( "encodedCreds: " + encodedCreds );
                String creds = new String( Base64.decode( encodedCreds ) );
                LOG.debug( "creds: " + creds );
                int delimPos = creds.indexOf( ':' );
                if ( delimPos != -1 ) {

                    String user = creds.substring( 0, delimPos );

                    String password = creds.substring( delimPos + 1 );

                    LOG.debug( "user: " + user );
                    LOG.debug( "password: " + password );
                    return new Credentials( user, password );

                }
            }
        }
        return null;
    }

    public void handleException( HttpServletResponse response, SecurityException e )
                            throws IOException {

        if ( e instanceof InvalidCredentialsException ) {
            doInvalidCredentialsExceptionException( response, (InvalidCredentialsException) e );
        } else if ( e != null ) {
            doAuthenticationException( response, e );
        }

    }

    /**
     * Handles the authentication.
     * 
     * @param response
     * @param e
     * @throws IOException
     */
    private void doAuthenticationException( HttpServletResponse response, SecurityException e )
                            throws IOException {

        LOG.debug( "SecurityException: " );
        response.reset();
        response.resetBuffer();
        response.setHeader( "WWW-Authenticate", "Basic realm=\" Backroom " );
        response.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
        response.flushBuffer();

    }

    /**
     * Handles the authentication.
     * 
     * @param response
     * @param e
     * @throws IOException
     */
    private void doInvalidCredentialsExceptionException( HttpServletResponse response, InvalidCredentialsException e )
                            throws IOException {

        LOG.debug( "exception should respond Forbidden: " );

        response.sendError( 403 );

    }

}
