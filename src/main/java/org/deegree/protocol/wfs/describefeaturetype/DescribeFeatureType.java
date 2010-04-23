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
package org.deegree.protocol.wfs.describefeaturetype;

import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.ows.Version;
import org.deegree.protocol.wfs.AbstractWFSRequest;

/**
 * Represents a <code>DescribeFeatureType</code> request to a WFS.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class DescribeFeatureType extends AbstractWFSRequest {

    private String outputFormat;

    private QName[] typeNames;

    private Map<String, String> nsBindings;

    /**
     * Creates a new {@link DescribeFeatureType} request.
     * 
     * @param version
     *            protocol version, may not be null
     * @param handle
     *            client-generated identifier, may be null
     * @param outputFormat
     *            requested output format, may be null
     * @param typeNames
     *            requested type names, may be null
     * @param nsBindings
     *            mapping between prefices and namespaces (key: prefix, value: namespace), empty string as a key ('') is
     *            the binding of the default namespace, null is returned if no <code>NAMESPACE</code> parameter is
     *            present, may be null
     */
    public DescribeFeatureType( Version version, String handle, String outputFormat, QName[] typeNames,
                                Map<String, String> nsBindings ) {
        super( version, handle );
        this.outputFormat = outputFormat;
        this.typeNames = typeNames;
        this.nsBindings = nsBindings;
    }

    /**
     * Returns the requested output format.
     * 
     * @return the requested output format, or null if unspecified
     */
    public String getOutputFormat() {
        return this.outputFormat;
    }

    /**
     * Returns the names of the feature types for which the schema is requested.
     * 
     * @return the names of the feature types for which the schema is requested, or null if unspecified
     */
    public QName[] getTypeNames() {
        return typeNames;
    }

    /**
     * Returns mapping between prefices and namespaces (key: prefix, value: namespace), empty string as a key ('') is
     * the binding of the default namespace, null is returned if no <code>NAMESPACE</code> parameter is present.
     * 
     * @return mapping between prefices and namespaces, or null if unspecified
     */
    public Map<String, String> getNsBindings() {
        return nsBindings;
    }

    @Override
    public String toString() {
        String s = "{version=" + getVersion() + ",handle=" + getHandle() + ",outputFormat=" + outputFormat
                   + ",typeNames=";
        if ( typeNames != null ) {
            s += "{";
            for ( int i = 0; i < typeNames.length; i++ ) {
                s += typeNames[i];
                if ( i != typeNames.length - 1 ) {
                    s += ",";
                }
            }
            s += "}";
        } else {
            s += "null";
        }
        s += ",namespace=";
        if ( nsBindings != null ) {
            s += "{";
            int i = 0;
            for ( String ns : nsBindings.keySet() ) {
                s +=  ns + "=" + nsBindings.get( ns );
                if ( i != nsBindings.size() - 1 ) {
                    s += ",";
                }
            }
            s += "}";
        } else {
            s += "null";
        }        
        s += "}";
        return s;
    }
}
