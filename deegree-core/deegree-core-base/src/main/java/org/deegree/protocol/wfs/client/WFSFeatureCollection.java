//$HeadURL$
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
package org.deegree.protocol.wfs.client;

import java.util.Iterator;

import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.AppSchema;
import org.deegree.gml.GMLDocumentIdContext;

/**
 * Encapsulates the response to a <code>GetFeature</code> request while maintaining full scalability.
 * <p>
 * TODO in order to make this usable for <b>really</b> large amounts of complex features, {@link GMLDocumentIdContext}
 * needs to be rewritten (so it doesn't keep everything in memory).
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WFSFeatureCollection<T> {

    private int numberMatched;

    private int numberReturned;

    /**
     * Creates a new {@link WFSFeatureCollection} instance.
     * 
     * @param xmlStream
     * @param schema
     */
    WFSFeatureCollection( XMLStreamReader xmlStream, AppSchema schema ) {

    }

    public Object getTimeStamp() {
        return null;
    }

    public int getNumberReturned() {
        return numberReturned;
    }

    public int getNumberMatched() {
        return numberMatched;
    }

    public Iterator<T> getMembers() {
        return null;
    }

    public Iterator<GMLObject> getAdditionalObjects() {
        return null;
    }

    public GMLDocumentIdContext getIdContext() {
        return null;
    }

    public FeatureCollection toCollection() {
        return null;
    }
}
