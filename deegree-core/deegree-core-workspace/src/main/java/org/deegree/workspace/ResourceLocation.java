//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.workspace;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

/**
 * A resource location is responsible for being able to fetch the configuration content.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * 
 * @since 3.4
 */
public interface ResourceLocation<T extends Resource> {

    /**
     * Determines the namespace of the configuration.
     * 
     * @return the namespace, never <code>null</code>
     */
    String getNamespace();

    /**
     * Returns the identifier of the resource.
     * 
     * @return the identifier, never <code>null</code>
     */
    ResourceIdentifier<T> getIdentifier();

    /**
     * Returns the configuration content as stream.
     * 
     * @return the stream, never <code>null</code>
     */
    InputStream getAsStream();

    /**
     * Returns the configuration content as file. May not be available in all implementations!
     *
     * @return the file, or <code>null</code>, if resolving is not supported
     */
    File getAsFile();

    /**
     * Resolves a path relative to this location to an input stream. May not be available in all implementations!
     * 
     * @param path
     *            never <code>null</code>
     * @return the stream, or <code>null</code>, if resolving is not supported
     */
    InputStream resolve( String path );

    /**
     * Resolves a path relative to this location to a file. May not be available in all implementations!
     * 
     * @param path
     *            never <code>null</code>
     * @return the file, or <code>null</code>, if resolving is not supported
     */
    File resolveToFile( String path );

    /**
     * Resolves a path possibly relative to this location to a file. May not be available in all implementations!
     * 
     * @param path
     *            never <code>null</code>
     * @return the URL, or <code>null</code>, if resolving is not supported
     */
    URL resolveToUrl( String path );

    /**
     * Deactivates this resource location.
     */
    void deactivate();

    /**
     * Activates this resource location.
     */
    void activate();

    /**
     * Overwrites the resources' location with the content from the input stream.
     * 
     * @param in
     *            may not be <code>null</code>
     */
    void setContent( InputStream in );

}
