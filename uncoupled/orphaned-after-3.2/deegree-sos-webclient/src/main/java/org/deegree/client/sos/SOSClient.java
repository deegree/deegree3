package org.deegree.client.sos;

import static java.util.Collections.synchronizedMap;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.deegree.client.sos.requesthandler.HandleDescribeSensor;
import org.deegree.client.sos.requesthandler.HandleGetCapabilities;
import org.deegree.client.sos.requesthandler.HandleGetObservation;
import org.deegree.client.sos.requesthandler.kvp.GetKVPDescribeSensor;
import org.deegree.client.sos.requesthandler.kvp.GetKVPGetObservation;
import org.deegree.client.sos.requesthandler.kvp.KVPDescribeSensor;
import org.deegree.client.sos.requesthandler.kvp.KVPGetObservation;
import org.deegree.client.sos.storage.StorageGetObservation;
import org.deegree.client.sos.utils.ChartProcessing;
import org.deegree.client.sos.utils.Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet implementation class SOSClient handles the get-requests from the HTML-forms. The basic method doGet calls the
 * adequate method depending on parameter "request" for creating a "RequestHandler". This will get the response from the
 * sos and parse it. The parsed values are then either forwarded to a JSP or displayed by the Writer-class.<br>
 * 
 * @author <a href="mailto:neumeister@lat-lon.de">Ulrich Neumeister</a>
 * 
 */
public class SOSClient extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger( SOSClient.class );

    private Map<HttpSession, StorageGetObservation> observationStorages = synchronizedMap( new HashMap<HttpSession, StorageGetObservation>() );

    /**
     * @see HttpServlet#HttpServlet()
     */
    public SOSClient() {
        super();
    }

    /**
     * After entered this method, the request parameter will be checked and therefore a specific RequestHandler will be
     * 
     * 
     * @param request
     * @param response
     */
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) {

        LOG.trace( "entering doGet" );

        if ( request.getParameter( "request" ).equals( "GetCapabilities" ) ) {
            LOG.trace( "request=GetCapabilities" );
            doGetCapabilities( request, response );
        } else if ( request.getParameter( "request" ).equals( "DescribeSensorForm" ) ) {
            LOG.trace( "request=DescribeSensorForm" );
            doDescribeSensorForm( request, response );
        } else if ( request.getParameter( "request" ).equals( "GetObservationForm" ) ) {
            LOG.trace( "request=GetObservationForm" );
            doGetObservationForm( request, response );
        } else if ( request.getParameter( "request" ).equals( "DescribeSensor" ) ) {
            LOG.trace( "request=DescribeSensor" );
            doDescribeSensor( request, response );
        } else if ( request.getParameter( "request" ).equals( "GetObservation" ) ) {
            LOG.trace( "request=GetObservation" );
            doGetObservation( request, response );
        } else if ( request.getParameter( "request" ).equals( "generateChart" ) ) {
            LOG.trace( "request=generateChart" );
            doGenerateChart( request, response );
        }
        LOG.trace( "leaving doGet" );
    }

    private void doGetCapabilities( HttpServletRequest request, HttpServletResponse response ) {
        String url = request.getParameter( "soshost" ) + "?service=SOS&request=GetCapabilities";

        LOG.trace( "request is: " + url );

        response.setContentType( "text/html" );
        response.setCharacterEncoding( "UTF-8" );
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher( "/client/sos/CapabilitiesDisplay.jsp" );

        request.setAttribute( "storage", new HandleGetCapabilities( url ).getStorage() );
        try {
            dispatcher.forward( request, response );
        } catch ( ServletException e ) {
            LOG.error( "Unexpected error:", e.getLocalizedMessage() );
            LOG.debug( "Stack trace:", e );
        } catch ( IOException e ) {
            LOG.error( "Unexpected error:", e.getLocalizedMessage() );
            LOG.debug( "Stack trace:", e );
        }
    }

    private void doDescribeSensorForm( HttpServletRequest request, HttpServletResponse response ) {
        String url = request.getParameter( "soshost" ) + "?service=SOS&request=GetCapabilities";

        response.setContentType( "text/html" );
        response.setCharacterEncoding( "UTF-8" );
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher( "/client/sos/DescribeSensorForm.jsp" );

        KVPDescribeSensor kvps = new GetKVPDescribeSensor( new HandleGetCapabilities( url ).getStorage() ).getKVPDescribeSensor();

        request.setAttribute( "kvps", kvps );
        try {
            dispatcher.forward( request, response );
        } catch ( ServletException e ) {
            LOG.error( "Unexpected error:", e.getLocalizedMessage() );
            LOG.debug( "Stack trace:", e );
        } catch ( IOException e ) {
            LOG.error( "Unexpected error:", e.getLocalizedMessage() );
            LOG.debug( "Stack trace:", e );
        }
    }

    private void doGetObservationForm( HttpServletRequest request, HttpServletResponse response ) {
        String url = request.getParameter( "soshost" ) + "?service=SOS&request=GetCapabilities";

        response.setContentType( "text/html" );
        response.setCharacterEncoding( "UTF-8" );
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher( "/client/sos/GetObservationForm.jsp" );

        KVPGetObservation kvps = new GetKVPGetObservation( new HandleGetCapabilities( url ).getStorage() ).getKVPGetObservation();

        request.setAttribute( "kvps", kvps );
        try {
            dispatcher.forward( request, response );
        } catch ( ServletException e ) {
            LOG.error( "Unexpected error:", e.getLocalizedMessage() );
            LOG.debug( "Stack trace:", e );
        } catch ( IOException e ) {
            LOG.error( "Unexpected error:", e.getLocalizedMessage() );
            LOG.debug( "Stack trace:", e );
        }
    }

    private void doDescribeSensor( HttpServletRequest request, HttpServletResponse response ) {
        String path = "client/sos/DescribeSensorTemplate.template";
        path = getServletContext().getRealPath( path );

        String host = request.getParameter( "host" );
        String outputformat = request.getParameter( "outputformat" );
        String procedure = request.getParameter( "procedure" );

        HandleDescribeSensor handler = new HandleDescribeSensor( path, outputformat, procedure, host );

        if ( handler.getException() == null ) {
            try {
                PrintWriter pw = response.getWriter();
                new Writer( pw, handler.getStorage() );
                pw.flush();
                pw.close();
            } catch ( IOException e ) {
                LOG.error( "Unexpected error:", e.getLocalizedMessage() );
                LOG.debug( "Stack trace:", e );
            }
        } else {
            try {
                PrintWriter pw = response.getWriter();
                new Writer( pw, handler.getException() );
                pw.flush();
                pw.close();
            } catch ( IOException e ) {
                LOG.error( "Unexpected error:", e.getLocalizedMessage() );
                LOG.debug( "Stack trace:", e );
            }
        }
    }

    private void doGetObservation( HttpServletRequest request, HttpServletResponse response ) {
        Map<String, String> parameters = new HashMap<String, String>();

        String path = "client/sos/GetObservationTemplate.template";

        parameters.put( "path", getServletContext().getRealPath( path ) );
        parameters.put( "host", request.getParameter( "host" ) );
        parameters.put( "version", request.getParameter( "version" ) );
        parameters.put( "beginTime", request.getParameter( "beginTime" ) );
        parameters.put( "endTime", request.getParameter( "endTime" ) );
        parameters.put( "observedProperty", request.getParameter( "observedProperty" ) );
        parameters.put( "offering", request.getParameter( "offering" ) );
        parameters.put( "featureOfInterest", request.getParameter( "featureOfInterest" ) );
        parameters.put( "procedure", request.getParameter( "procedure" ) );
        parameters.put( "responseFormat", request.getParameter( "responseFormat" ) );
        parameters.put( "responseMode", request.getParameter( "responseMode" ) );
        parameters.put( "srsName", request.getParameter( "srsName" ) );
        parameters.put( "result", request.getParameter( "result" ) );
        parameters.put( "resultModel", request.getParameter( "resultModel" ) );

        HandleGetObservation handler = new HandleGetObservation( parameters );

        if ( handler.getException() == null ) {
            try {
                HttpSession session = request.getSession( true );
                observationStorages.put( session, handler.getStorage() );
                PrintWriter pw = response.getWriter();
                new Writer( pw, handler.getStorage() );
                pw.flush();
                pw.close();
            } catch ( IOException e ) {
                LOG.error( "Unexpected error:", e.getLocalizedMessage() );
                LOG.debug( "Stack trace:", e );
            }
        } else {
            try {
                PrintWriter pw = response.getWriter();
                new Writer( pw, handler.getException() );
                pw.flush();
                pw.close();
            } catch ( IOException e ) {
                LOG.error( "Unexpected error:", e.getLocalizedMessage() );
                LOG.debug( "Stack trace:", e );
            }
        }
    }

    private void doGenerateChart( HttpServletRequest request, HttpServletResponse response ) {
        HttpSession session = request.getSession( true );
        String procedure = request.getParameter( "procedure" );
        StorageGetObservation storage = observationStorages.get( session );
        BufferedImage image = new ChartProcessing( storage, procedure ).getImage();
        OutputStream os;
        try {
            os = response.getOutputStream();

            javax.imageio.ImageIO.write( image, "jpg", os );

            os.flush();
            os.close();
        } catch ( IOException e ) {
            LOG.error( "Unexpected error:", e.getLocalizedMessage() );
            LOG.debug( "Stack trace:", e );
        }
    }
}
