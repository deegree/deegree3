//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.gml.feature;

import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.gml.GMLReference;
import org.deegree.gml.GmlReferenceResolveOptions;

/**
 * Invoked by the {@link GMLFeatureWriter} when references to {@link GMLObject}s need to be written.
 * <p>
 * This interface allows to customize the strategy for dealing with potential forward-references.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface GmlReferenceExportStrategy {

    /**
     * Invoked when the target of the given {@link GMLReference} has to be included in the output.
     * 
     * @param ref
     *            reference, never <code>null</code>
     * @param resolve
     *            resolve options this reference, never <code>null</code>
     * @return URI to write, never <code>null</code>
     */
    public String requireObject( GMLReference<?> ref, GmlReferenceResolveOptions resolveState );

    /**
     * Invoked when the target of the given {@link GMLReference} may be an external reference or a forward reference to
     * an object exported later.
     * 
     * @param ref
     *            reference, never <code>null</code>
     * @return URI to write, never <code>null</code>
     */
    public String handleReference( GMLReference<?> ref );

    public GmlReferenceResolveOptions getResolveOptions();

}
