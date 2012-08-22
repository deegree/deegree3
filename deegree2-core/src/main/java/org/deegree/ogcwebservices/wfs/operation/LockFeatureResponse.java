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

import org.deegree.ogcwebservices.DefaultOGCWebServiceResponse;

/**
 * Represents the response to a {@link LockFeature} request.
 *
 * In response to a &lt;LockFeature&gt; request, the web feature server shall generate an XML
 * document containing a lock identifier that a client application can reference when operating upon
 * the <code>locked</code> features. The response can also contain optional blocks depending on
 * the value of the <code>lockAction</code> attribute.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$
 */
public class LockFeatureResponse extends DefaultOGCWebServiceResponse {

    private String[] featuresLocked;

    private String[] featuresNotLocked;

    private String lockId;

    /**
     * Creates a new instance of <code>LockFeatureResponse</code>.
     *
     * @param request
     *            <code>LockFeature</code> request for which the response is generated
     * @param lockId
     *            lock identifier (must not be null)
     * @param featuresLocked
     *            ids of succesfully locked features (may not be null, but empty)
     * @param featuresNotLocked
     *            ids of features that could not be locked sucessfully (may not be null, but empty)
     */
    public LockFeatureResponse( LockFeature request, String lockId, String[] featuresLocked,
                                String[] featuresNotLocked ) {
        super( request );
        assert lockId != null;
        assert featuresLocked != null;
        assert featuresNotLocked != null;
        this.lockId = lockId;
        this.featuresLocked = featuresLocked;
        this.featuresNotLocked = featuresNotLocked;
    }

    /**
     * Returns the lock identifier.
     *
     * @return the lock identifier
     */
    public String getLockId() {
        return this.lockId;
    }

    /**
     * Returns the feature identifiers of all features that have been locked successfully.
     *
     * @return the feature identifiers of all features that have been locked successfully, (array
     *         may not be null, but empty)
     */
    public String[] getFeaturesLocked() {
        return this.featuresLocked;
    }

    /**
     * Returns the feature identifiers of all features that were requested for locking, but which
     * could not be locked.
     *
     * @return the feature identifiers of all features that were requested for locking, but which
     *         could not be locked, (array may not be null, but empty)
     */
    public String[] getFeaturesNotLocked() {
        return this.featuresNotLocked;
    }

    @Override
    public String toString() {
        String ret = this.getClass().getName() + ":\n";
        ret += ( "lockId: " + lockId + "\n" );
        for ( int i = 0; i < featuresLocked.length; i++ ) {
            ret += ( "featuresLocked: " + featuresLocked[i] + "\n" );
        }
        for ( int i = 0; i < featuresNotLocked.length; i++ ) {
            ret += ( "featuresNotLocked: " + featuresNotLocked[i] + "\n" );
        }
        return ret;
    }
}
