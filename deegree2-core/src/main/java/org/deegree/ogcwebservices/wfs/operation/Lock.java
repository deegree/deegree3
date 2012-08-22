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
package org.deegree.ogcwebservices.wfs.operation;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.filterencoding.Filter;
import org.deegree.ogcwebservices.wfs.WFService;

/**
 * Represents a <code>wfs:Lock</code> element (usually part of <code>wfs:LockFeature</code> documents).
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class Lock {

    private static final ILogger LOG = LoggerFactory.getLogger( Lock.class );

    private String handle;

    private QualifiedName typeName;

    private Filter filter;

    /**
     * Creates a new <code>Lock</code> instance from the given parameters.
     *
     * @param handle
     * @param typeName
     * @param filter
     */
    public Lock( String handle, QualifiedName typeName, Filter filter ) {
        this.handle = handle;
        this.typeName = typeName;
        this.filter = filter;
    }

    /**
     * Returns the lock's handle.
     *
     * @return the lock's handle (may be null)
     */
    public String getHandle() {
        return this.handle;
    }

    /**
     * Returns the name of the {@link FeatureType} that is affected by this lock.
     *
     * @return the name of the <code>FeatureType</code> (never null)
     */
    public QualifiedName getTypeName() {
        return this.typeName;
    }

    /**
     * Returns the filter that is used to select the {@link Feature} instances for locking.
     *
     * @return the filter that is used to select the <code>Feature<code> instances (may be null)
     */
    public Filter getFilter() {
        return this.filter;
    }

    /**
     * Adds missing namespaces in the names of targeted feature types.
     * <p>
     * If the {@link QualifiedName} of a targeted type has a null namespace, the first qualified feature type name of
     * the given {@link WFService} with the same local name is used instead.
     * <p>
     * Note: The method changes this request part (the feature type names) and should only be called by the
     * <code>WFSHandler</code> class.
     *
     * @param wfs
     *            {@link WFService} instance that is used for the lookup of proper (qualified) feature type names
     */
    public void guessMissingNamespaces( WFService wfs ) {
        if ( typeName.getNamespace() == null ) {
            if ( typeName.getLocalName().equals( typeName.getLocalName() ) ) {
                LOG.logWarning( "Requested feature type name has no namespace information. Guessing namespace for feature type '"
                                + typeName.getLocalName() + "' (quirks lookup mode)." );
                for ( QualifiedName ftName : wfs.getMappedFeatureTypes().keySet() ) {
                    if ( ftName.getLocalName().equals( typeName.getLocalName() ) ) {
                        LOG.logWarning( "Using feature type '" + ftName + "'." );
                        typeName = ftName;
                        break;
                    }
                }
            }
        }
    }
}
