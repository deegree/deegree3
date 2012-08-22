//$HeadURL$
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

package org.deegree.ogcwebservices.csw.manager;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.time.TimeDuration;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.TimeTools;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.ogcwebservices.AbstractOGCWebServiceRequest;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.w3c.dom.Element;

/**
 * <p>
 * The general model defines two operations in the Manager class that may be used to create or
 * update records in the catalogue. They are the transaction operation and the harvestRecords
 * operation. The transaction operation may be used to "push" data into the catalogue and is defined
 * in Subclause 10.11. of CS-W specification. This subclause defines the optional Harvest operation,
 * which is an operation that "pulls" data into the catalogue. That is, this operation only
 * references the data to be inserted or updated in the catalogue, and it is the job of the
 * catalogue service to resolve the reference, fetch that data, and process it into the catalogue.
 * </p>
 * <p>
 * The Harvest operation had two modes of operation, controlled by a flag in the request. The first
 * mode of operation is a synchronous mode in whice the CSW receives a Harvest request from the
 * client, processes it immediately, and sends the results to the client while the client waits. The
 * second mode of operation is asynchronous in that the server receives a Harvest request from the
 * client, and sends the client an immediate acknowledgement that the request has been successfully
 * received. The server can then process the Harvest request whenever it likes, taking as much time
 * as is required and then send the results of the processing to a URI specified in the original
 * Harvest request. This latter mode of operation is included to support Harvest requests that could
 * run for a period of time longer than most HTTP timeout’s will allow.
 * </p>
 * <p>
 * Processing a Harvest request means that the CSW resolves the URI pointing to the metadata
 * resource, parses the resource, and then creates or modifies metadata records in the catalogue in
 * order to register the resource. This operation may be performed only once or periodically
 * depending on how the client invokes the operation.
 * </p>
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class Harvest extends AbstractOGCWebServiceRequest {

    private static final long serialVersionUID = -3531806711669781486L;

    private static final ILogger LOG = LoggerFactory.getLogger( Harvest.class );

    private URI source = null;

    private URI resourceType = null;

    private String resourceFormat = null;

    private TimeDuration harvestInterval = null;

    private List<URI> responseHandler = null;

    private Date startTimestamp = null;

    /**
     * factory method for creating a Harvest request from its KVP representation
     *
     * @param param
     * @return the harvest
     * @throws InvalidParameterValueException
     * @throws MissingParameterValueException
     */
    public static Harvest create( Map<String, String> param )
                            throws InvalidParameterValueException, MissingParameterValueException {

        LOG.logInfo( "parse harvest request KVP-encoded" );

        String id = param.get( "ID" );
        if ( id == null ) {
            throw new MissingParameterValueException( "ID parameter must be set" );
        }

        String version = param.get( "VERSION" );
        if ( version == null ) {
            throw new MissingParameterValueException( "VERSION parameter must be set for " + "a harvest request" );
        }

        String tmp = param.get( "SOURCE" );
        if ( tmp == null ) {
            throw new MissingParameterValueException( "SOURCE parameter must be set for " + "a harvest request" );
        }
        URI source = null;
        try {
            source = new URI( tmp );
        } catch ( URISyntaxException e ) {
            throw new InvalidParameterValueException( tmp + " is no valid source URI for a " + "harvest request" );
        }

        tmp = param.get( "RESOURCETYPE" );
        URI resourceType = null;
        if ( tmp != null ) {
            try {
                resourceType = new URI( tmp );
            } catch ( URISyntaxException e ) {
                throw new InvalidParameterValueException( tmp + " is no valid resourceType URI "
                                                          + "for a harvest request" );
            }
        }

        String resourceFormat = param.get( "RESOURCEFORMAT" );
        if ( resourceFormat == null ) {
            resourceFormat = "text/xml";
        }

        List<URI> list = new ArrayList<URI>();
        tmp = param.get( "RESPONSEHANDLER" );
        URI responseHandler = null;
        if ( tmp != null ) {
            try {
                responseHandler = new URI( tmp );
            } catch ( URISyntaxException e ) {
                throw new InvalidParameterValueException( tmp + " is no valid resourceHandler URI "
                                                          + "for a harvest request" );
            }
            list.add( responseHandler );
        }

        tmp = param.get( "HARVESTINTERVAL" );
        TimeDuration timeDuration = null;
        if ( tmp != null ) {
            timeDuration = TimeDuration.createTimeDuration( tmp );
        }

        Date date = new GregorianCalendar().getTime();
        tmp = param.get( "STARTTIMESTAMP" );
        if ( tmp != null ) {
            date = TimeTools.createCalendar( tmp ).getTime();
        }

        return new Harvest( version, id, null, source, resourceType, resourceFormat, timeDuration, list, date );
    }

    /**
     * creates a Harvesting Request from its XM representation
     *
     * @param id
     * @param transRoot
     * @return the new transaction
     * @throws XMLParsingException
     */
    public static final Transaction create( String id, Element transRoot )
                            throws XMLParsingException {
        throw new UnsupportedOperationException( "create( String, Element )" );
    }

    /**
     *
     * @param version
     * @param id
     * @param vendorSpecificParameter
     * @param source
     * @param resourceType
     * @param resourceFormat
     * @param harvestTimeDuration
     * @param responseHandler
     */
    Harvest( String version, String id, Map<String, String> vendorSpecificParameter, URI source, URI resourceType,
             String resourceFormat, TimeDuration harvestTimeDuration, List<URI> responseHandler, Date startTimestamp ) {
        super( version, id, vendorSpecificParameter );
        this.source = source;
        this.resourceType = resourceType;
        this.resourceFormat = resourceFormat;
        this.harvestInterval = harvestTimeDuration;
        this.responseHandler = responseHandler;
        this.startTimestamp = startTimestamp;
    }

    /**
     * <p>
     * The HarvestTimeDuration parameter is used to specify the period of time, in ISO 8601 period
     * format, that should elapse before a CSW attempts to re-harvest the specified resource thus
     * refreshing it copy of a resource.
     * </p>
     * <p>
     * If no HarvestTimeDuration parameter is specified then the resource is harvested only once in
     * response to the Harvest request.
     * </p>
     *
     * @return the interval
     */
    public TimeDuration getHarvestInterval() {
        return harvestInterval;
    }

    /**
     * The ResourceFormat paramter is used to indicate the encoding used for the resource being
     * harvested. This parameter is included to support the harvesting of metadata resources
     * available in various formats such as plain text, XML or HTML. The values of this parameter
     * must be a MIME type. If the parameter is not specified then the default value of text/xml
     * should be assumed.
     *
     * @return the format
     */
    public String getResourceFormat() {
        return resourceFormat;
    }

    /**
     * The ResourceType parameter is a reference to a schema document that defines the structure of
     * the resource being harvested. This is an optional parameter and if it not specified then the
     * catalogue must employee other means to determine the type of resource being harvested. For
     * example, the catalogue may use schema references in the input document to determine the
     * resource type, or perhaps parse the root element to determine the type of metadata being
     * harvested (e.g. &lt;fgdc:metadata&gt; is the root element of an FGDC document).
     *
     * @return the uri
     */
    public URI getResourceType() {
        return resourceType;
    }

    /**
     * <p>
     * The ResponseHandler parameter is a flag that indicates how the Harvest operation should be
     * processed by a CSW server.
     * </p>
     * <p>
     * If the parameter is not present, then the Harvest operation is processed synchronously
     * meaning that the client sends the Harvest request to a CSW and then waits to receive a
     * HarvestResponse or exception message. The CSW immediately processes the Harvest request,
     * while the client waits for a response. The problem with this mode of operation is that the
     * client may timeout waiting for the server to process the request.
     * </p>
     * If the parameter is present, the Harvest operation is processed asynchronously. In this case,
     * the server responds immediately to a client’s request with an acknowledgement message. The
     * acknowlegment message echos the client’s request, using the &lt;EchoedRequest&gt; element,
     * and may include an optionally generated request identifier using the &lt;RequestId&gt;
     * element. The acknowledgement message tells the client that the request has been received and
     * notification of completion will be send to the URL specified as the value of the
     * ResponseHandler parameter. The Harvest request may then be processed at some later time
     * taking as much time as is required to complete the operation. When the operation is
     * completed, a HarvestResponse message or exception message (if a problem was encountered) is
     * sent to the URL specified as the value of the ResponseHandler parameter.
     *
     * @return the list of uris
     */
    public List<URI> getResponseHandler() {
        return responseHandler;
    }

    /**
     * The Source parameter is used to specify a URI reference to the metadata resource to be
     * harvested.
     *
     * @return the uri
     */
    public URI getSource() {
        return source;
    }

    /**
     * returns the deegree specific timestamp when harvesting shall start. If <code>null</code> is
     * returned harvesting shall start as soon as possible
     *
     * @return the date
     */
    public Date getStartTimestamp() {
        return startTimestamp;
    }

    /**
     * will always return 'CSW'
     */
    public String getServiceName() {
        return "CSW";
    }

}
