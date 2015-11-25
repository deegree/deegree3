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
package org.deegree.gml.reference;

import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.gml.GMLReference;

/**
 * Controls how to export xlinks to {@link GMLObject}s.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface GmlXlinkStrategy {

    /**
     * Returns the options for the resolving of references.
     * 
     * @return options for the resolving of references, never <code>null</code>
     */
    GmlXlinkOptions getResolveOptions();

    /**
     * Marks the {@link GMLObject} with the specified gmlId as exported.
     * 
     * @param gmlId
     *            of the {@link GMLObject} to mark as exported, should not be <code>null</code>
     */
    void addExportedId( String gmlId );

    /**
     * Marks the {@link GMLObject} with the specified gmlId and version as exported.
     * 
     * @param gmlId
     *            of the {@link GMLObject} to mark as exported, should not be <code>null</code>
     * @param version
     *            of the {@link GMLObject} to mark as exported
     */
    void addExportedId( String gmlId, int version );

    /**
     * Returns whether a {@link GMLObject} with the specified id has already been exported.
     * 
     * @param gmlId
     *            id of the object, must not be <code>null</code>
     * @return <code>true</code>, if the object has been exported, <code>false</code> otherwise
     */
    boolean isObjectExported( String gmlId );

    /**
     * Returns whether a {@link GMLObject} with the specified id and id has already been exported.
     * 
     * @param gmlId
     *            id of the object, must not be <code>null</code>
     * @param version
     *            of the object
     * @return <code>true</code>, if the object has been exported, <code>false</code> otherwise
     */
    boolean isObjectExported( String gmlId, int version );

    /**
     * Invoked when the target of the given {@link GMLReference} has to be included in the exported document.
     * 
     * @param ref
     *            reference, never <code>null</code>
     * @param options
     *            resolve options for the reference, never <code>null</code>
     * @return URI to write, never <code>null</code>
     */
    String requireObject( GMLReference<?> ref, GmlXlinkOptions options );

    /**
     * Invoked when the target of the given {@link GMLReference} may be an external reference or a forward reference to
     * an object exported later.
     * 
     * @param ref
     *            reference, never <code>null</code>
     * @return URI to write, never <code>null</code>
     */
    String handleReference( GMLReference<?> ref );

}
