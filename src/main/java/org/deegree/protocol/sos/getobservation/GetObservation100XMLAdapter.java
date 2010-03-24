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
package org.deegree.protocol.sos.getobservation;

import static java.util.Arrays.asList;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.crs.CRS;
import org.deegree.filter.comparison.ComparisonOperator;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.protocol.sos.SOSRequest100XMLAdapter;
import org.deegree.protocol.sos.filter.ProcedureFilter;
import org.deegree.protocol.sos.filter.PropertyFilter;
import org.deegree.protocol.sos.filter.ResultFilter;
import org.deegree.protocol.sos.filter.SpatialBBOXFilter;
import org.deegree.protocol.sos.filter.SpatialFilter;
import org.deegree.protocol.sos.filter.TimeFilter;
import org.slf4j.Logger;

/**
 * This is an xml adapter for SOS 1.0.0 GetObservation requests.
 * 
 * <p>
 * This class also implements the {@link GetObservation} interface. The {@link #parse()} method returns itself. The
 * elements are parsed lazy, on request.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class GetObservation100XMLAdapter extends SOSRequest100XMLAdapter {

    private static final Logger LOG = getLogger( GetObservation100XMLAdapter.class );

    /**
     * @param rootElement
     */
    public GetObservation100XMLAdapter( OMElement rootElement ) {
        this.setRootElement( rootElement );
    }

    /**
     * @return the parsed request
     */
    public GetObservation parse() {
        List<TimeFilter> eventTime = null;
        List<ResultFilter> resultFilter = null;
        eventTime = getEventTime();
        resultFilter = getResultFilter();
        return new GetObservation( getOffering(), getProcedures(), getObservedProperties(), eventTime,
                                   getFeatureOfInterest(), resultFilter, getResultModel(), getResponseFormat(),
                                   getResponseMode(), getSRSName() );

    }

    private List<String> getElementTextAsStrings( OMElement root, XPath xpath ) {
        List<OMElement> properties = getRequiredElements( root, xpath );
        List<String> result = new ArrayList<String>( properties.size() );
        for ( OMElement elem : properties ) {
            result.add( elem.getText() );
        }
        return result;
    }

    private List<PropertyFilter> getObservedProperties() {
        List<PropertyFilter> result = new LinkedList<PropertyFilter>();
        for ( String property : getElementTextAsStrings( rootElement, new XPath( "sos:observedProperty", nsContext ) ) ) {
            result.add( new PropertyFilter( property ) );
        }
        if ( result.size() == 0 ) {
            throw new XMLParsingException( this, rootElement,
                                           "need one or more observedProperty elements: observedProperty" );
        }
        return result;
    }

    private List<ProcedureFilter> getProcedures() {
        List<ProcedureFilter> result = new LinkedList<ProcedureFilter>();
        for ( OMElement procElem : getElements( rootElement, new XPath( "sos:procedure", nsContext ) ) ) {
            result.add( new ProcedureFilter( procElem.getText() ) );
        }
        return result;
    }

    private List<ResultFilter> getResultFilter() {
        List<ResultFilter> result = new LinkedList<ResultFilter>();
        // SOS 1.0.0 only supports a single result filter.
        OMElement procElem = getElement( rootElement, new XPath( "sos:result/*", nsContext ) );
        if ( procElem != null ) {
            try {
                XMLStreamReader xmlStream = procElem.getXMLStreamReaderWithoutCaching();
                // skip START_DOCUMENT
                xmlStream.nextTag();
                ComparisonOperator op = Filter110XMLDecoder.parseComparisonOperator( xmlStream );
                result.add( new ResultFilter( op ) );
            } catch ( XMLStreamException e ) {
                LOG.debug( "Stack trace", e );
                throw new ResultFilterException( this, procElem, e.getMessage() );
            } catch ( XMLParsingException e ) {
                LOG.debug( "Stack trace", e );
                throw new ResultFilterException( this, procElem, e.getMessage() );
            }
        }
        return result;
    }

    private Pair<List<String>, SpatialFilter> getFeatureOfInterest() {
        OMElement bboxElem = getElement( rootElement, new XPath( "/sos:GetObservation/sos:featureOfInterest/ogc:BBOX",
                                                                 nsContext ) );

        SpatialFilter filter = null;

        if ( bboxElem != null ) {
            Envelope env = parseEnvelope( bboxElem );
            if ( env != null ) {
                filter = new SpatialBBOXFilter( env );
            }
        }

        XPath xpath = new XPath( "/sos:GetObservation/sos:featureOfInterest/sos:ObjectID", nsContext );
        List<String> ids = asList( getNodesAsStrings( rootElement, xpath ) );

        return new Pair<List<String>, SpatialFilter>( ids, filter );
    }

    private Envelope parseEnvelope( OMElement bboxElem ) {
        String lCorner = getRequiredNodeAsString( bboxElem, new XPath( "gml:Envelope/gml:lowerCorner", nsContext ) );
        String uCorner = getRequiredNodeAsString( bboxElem, new XPath( "gml:Envelope/gml:upperCorner", nsContext ) );

        double[] min = parseCoordinates( lCorner );
        double[] max = parseCoordinates( uCorner );

        // TODO precision
        // double precision = Math.min( getPrecision( lCorner.split( " " ) ), getPrecision( uCorner.split( " " ) ) );
        GeometryFactory geomFactory = new GeometryFactory();
        CRS crs = new CRS( getSRSName() );
        return geomFactory.createEnvelope( min, max, crs );
    }

    private double getPrecision( String[] coords ) {
        double precision = 1;
        for ( String coord : coords ) {
            int idx = coord.lastIndexOf( "." );
            if ( idx != -1 ) {
                int digits = coord.length() - idx;
                precision = Math.min( precision, 1.0 * Math.pow( 10, -digits ) );
            }
        }
        return precision;
    }

    private double[] parseCoordinates( String corner ) {
        double[] result = new double[2];
        String[] parts = corner.split( " " );
        for ( int i = 0; i < result.length; i++ ) {
            result[i] = parseDouble( parts[i] );
        }
        return result;
    }

    private String getOffering() {
        return getRequiredStringNode( rootElement, new XPath( "/sos:GetObservation/sos:offering", nsContext ) );
    }

    private String getResponseFormat() {
        return getRequiredStringNode( rootElement, new XPath( "/sos:GetObservation/sos:responseFormat", nsContext ) );
    }

    private String getResponseMode() {
        return getNodeAsString( rootElement, new XPath( "/sos:GetObservation/sos:responseMode", nsContext ), "" );

    }

    private String getResultModel() {
        return getNodeAsString( rootElement, new XPath( "/sos:GetObservation/sos:resultModel", nsContext ), "" );
    }

    private String getSRSName() {
        return getNodeAsString( rootElement, new XPath( "@srsName", nsContext ), "" );
    }

    private List<TimeFilter> getEventTime() {
        OMElement observation = getElement( rootElement, new XPath( "/sos:GetObservation", nsContext ) );
        EventTime100XMLAdapter adapter = new EventTime100XMLAdapter( observation, getSystemId() );
        return adapter.parseTimeFilter();
    }

    /**
     * <code>ResultFilterException</code> is a hack to work around missing OWSException in core.
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public static class ResultFilterException extends XMLParsingException {
        private static final long serialVersionUID = 7363995988367835730L;

        /**
         * @param adapter
         * @param elem
         * @param msg
         */
        public ResultFilterException( XMLAdapter adapter, OMElement elem, String msg ) {
            super( adapter, elem, msg );
        }
    }

}
