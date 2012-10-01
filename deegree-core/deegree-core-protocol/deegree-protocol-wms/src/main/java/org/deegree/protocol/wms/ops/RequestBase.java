//$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.protocol.wms.ops;

import static org.deegree.commons.utils.CollectionUtils.unzipPair;
import static org.deegree.protocol.wms.ops.SLDParser.parse;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.StringReader;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.xml.stream.XMLInputFactory;

import org.deegree.commons.utils.Pair;
import org.deegree.filter.OperatorFilter;
import org.deegree.layer.LayerRef;
import org.deegree.protocol.ows.exception.OWSException;
import org.deegree.style.StyleRef;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public abstract class RequestBase {

    private static final Logger LOG = getLogger( RequestBase.class );

    // layer names may occur multiple times
    private List<Pair<String, OperatorFilter>> sldFilters = new ArrayList<Pair<String, OperatorFilter>>();

    protected LinkedList<LayerRef> layers = new LinkedList<LayerRef>();

    protected LinkedList<StyleRef> styles = new LinkedList<StyleRef>();

    protected HashMap<String, List<?>> dimensions = new HashMap<String, List<?>>();

    public abstract double getScale();

    public abstract List<LayerRef> getLayers();

    public void addSldFilter( String layerName, OperatorFilter filter ) {
        sldFilters.add( new Pair<String, OperatorFilter>( layerName, filter ) );
    }

    public List<Pair<String, OperatorFilter>> getSldFilters() {
        return sldFilters;
    }

    protected void handleSLD( String sld, String sldBody, LinkedList<LayerRef> layers )
                            throws OWSException {
        layers = new LinkedList<LayerRef>( layers );
        XMLInputFactory xmlfac = XMLInputFactory.newInstance();
        Pair<LinkedList<LayerRef>, LinkedList<StyleRef>> pair = null;
        if ( sld != null ) {
            try {
                pair = parse( xmlfac.createXMLStreamReader( sld, new URL( sld ).openStream() ), this );
            } catch ( ParseException e ) {
                LOG.trace( "Stack trace:", e );
                throw new OWSException( "The embedded dimension value in the SLD parameter value was invalid: "
                                        + e.getMessage(), "InvalidDimensionValue", "sld" );
            } catch ( Throwable e ) {
                LOG.trace( "Stack trace:", e );
                throw new OWSException( "Error when parsing the SLD parameter: " + e.getMessage(),
                                        "InvalidParameterValue", "sld" );
            }
        }
        if ( sldBody != null ) {
            try {
                pair = parse( xmlfac.createXMLStreamReader( new StringReader( sldBody ) ), this );
            } catch ( ParseException e ) {
                LOG.trace( "Stack trace:", e );
                throw new OWSException( "The embedded dimension value in the SLD_BODY parameter value was invalid: "
                                        + e.getMessage(), "InvalidDimensionValue", "sld_body" );
            } catch ( Throwable e ) {
                LOG.trace( "Stack trace:", e );
                throw new OWSException( "Error when parsing the SLD_BODY parameter: " + e.getMessage(),
                                        "InvalidParameterValue", "sld_body" );
            }
        }

        // if layers are referenced, clear the other layers out, else leave all in
        if ( pair != null && !layers.isEmpty() ) {
            // it might be in SLD that a layer has multiple styles, so we need to map to a list here
            HashMap<String, LinkedList<Pair<LayerRef, StyleRef>>> lays = new HashMap<String, LinkedList<Pair<LayerRef, StyleRef>>>();

            ListIterator<LayerRef> it = pair.first.listIterator();
            ListIterator<StyleRef> st = pair.second.listIterator();
            while ( it.hasNext() ) {
                LayerRef l = it.next();
                StyleRef s = st.next();
                String name = l.getName();
                if ( !layers.contains( l ) ) {
                    it.remove();
                    st.remove();
                } else {
                    LinkedList<Pair<LayerRef, StyleRef>> list = lays.get( name );
                    if ( list == null ) {
                        list = new LinkedList<Pair<LayerRef, StyleRef>>();
                        lays.put( name, list );
                    }

                    if ( s.getName() == null || s.getName().isEmpty() ) {
                        s = new StyleRef( "default" );
                    }

                    list.add( new Pair<LayerRef, StyleRef>( l, s ) );
                }
            }

            this.layers.clear();
            this.styles.clear();
            // to get the order right, in case it's different from the SLD order
            for ( LayerRef ref : layers ) {
                LinkedList<Pair<LayerRef, StyleRef>> l = lays.get( ref.getName() );
                if ( l == null ) {
                    throw new OWSException( "The SLD NamedLayer " + ref.getName() + " is invalid.",
                                            "InvalidParameterValue", "layers" );
                }
                Pair<ArrayList<LayerRef>, ArrayList<StyleRef>> p = unzipPair( l );
                this.layers.addAll( p.first );
                styles.addAll( p.second );
            }
        } else {
            if ( pair != null ) {
                this.layers = pair.first;
                styles = pair.second;
            }
        }
    }

    /**
     * @return returns a map with the requested dimension values
     */
    public HashMap<String, List<?>> getDimensions() {
        return dimensions;
    }

    /**
     * @param name
     * @param values
     */
    public void addDimensionValue( String name, List<?> values ) {
        dimensions.put( name, values );
    }

}
