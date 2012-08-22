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
package org.deegree.model.feature;

import org.deegree.framework.util.StringTools;

/**
 *
 * @author Administrator
 */
public class FeatureException extends java.lang.Exception {

    private static final long serialVersionUID = -4448665267184258381L;

    private String st = "org.deegree.model.feature.FeatureException";

    /**
     * Creates a new instance of <code>FeatureException</code> without detail message.
     */
    public FeatureException() {
        // no message...
    }

    /**
     * Constructs an instance of <code>FeatureException</code> with the specified detail message.
     *
     * @param msg
     *            the detail message.
     */
    public FeatureException( String msg ) {
        super( msg );
    }

    /**
     * Constructs an instance of <code>FeatureException</code> with the specified detail message.
     *
     * @param msg
     *            the detail message.
     * @param e
     */
    public FeatureException( String msg, Exception e ) {
        this( msg );
        st = StringTools.stackTraceToString( e.getStackTrace() );
    }

    @Override
    public String toString() {
        return super.toString() + "\n" + st;
    }

}
