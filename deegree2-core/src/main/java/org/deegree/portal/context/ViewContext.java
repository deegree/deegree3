// $HeadURL$
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

package org.deegree.portal.context;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.security.drm.model.User;

/**
 * is the root class of the Web Map Context
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 */
public class ViewContext extends AbstractContext {
    private LayerList layerList = null;

    /**
     * Creates a new WebMapContext object.
     *
     * @param general
     *            general informations about the map context and its creator
     * @param layerList
     *            layers contained in the web map context
     * @throws ContextException when layerList is <code>null</code>
     */
    public ViewContext( General general, LayerList layerList ) throws ContextException {
        super( general );
        setLayerList( layerList );
    }

    /**
     * returns the list of layers contained in this context
     *
     * @return all layers of this context
     */
    public LayerList getLayerList() {
        return layerList;
    }

    /**
     * sets the list of layers to be contained in this context
     *
     * @param layerList
     *
     * @throws ContextException
     */
    public void setLayerList( LayerList layerList )
                            throws ContextException {
        if ( layerList == null ) {
            throw new ContextException( "layerList isn't allowed to be null" );
        }
        this.layerList = layerList;
    }

    /**
     * The function obtains a copy from the view context
     *
     * @param user
     * @param sessionID
     * @return a clone vc
     * @throws Exception

     */
    public ViewContext clone( User user, String sessionID )
                            throws Exception {
        XMLFragment xml = XMLFactory.export( this );
        return WebMapContextFactory.createViewContext( xml, user, sessionID );
    }
}
