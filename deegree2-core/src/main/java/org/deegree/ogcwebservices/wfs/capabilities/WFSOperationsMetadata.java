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
package org.deegree.ogcwebservices.wfs.capabilities;

import java.util.ArrayList;
import java.util.List;

import org.deegree.ogcwebservices.getcapabilities.Operation;
import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.owscommon.OWSDomainType;

/**
 * Represents the <code>OperationMetadata</code> part in the capabilities document of a WFS
 * according to the <code>Web Feature Service Implementation Specification 1.1.0</code>.
 * <p>
 * In addition to the <code>GetCapabilities</code> operation that all <code>OWS 0.3</code>
 * compliant services must implement, it may define some or all of the following operations: <table
 * border="1">
 * <tr>
 * <th>Name</th>
 * <th>Mandatory?</th>
 * <th>Function</th>
 * </tr>
 * <tr>
 * <td><code>DescribeFeatureType</code></td>
 * <td align="center">X</td>
 * <td>The function of the <code>DescribeFeatureType</code> operation is to generate a schema
 * description of feature types serviced by a WFS implementation.</td>
 * </tr>
 * <tr>
 * <td><code>GetFeature</code></td>
 * <td align="center">X</td>
 * <td>The <code>GetFeature</code> operation allows retrieval of features from a web feature
 * service.</td>
 * </tr>
 * <tr>
 * <td><code>GetFeatureWithLock</code></td>
 * <td align="center">-</td>
 * <td>The lock action of the <code>GetFeatureWithLock</code> request is to attempt to lock all
 * identified feature instances. If all identified feature instances cannot be locked, then an
 * exception report should be generated.</td>
 * </tr>
 * <tr>
 * <td><code>GetGMLObject</code></td>
 * <td align="center">-</td>
 * <td>The <code>GetGMLObject</code> operation allows retrieval of features and elements by ID
 * from a web feature service.</td>
 * </tr>
 * <tr>
 * <td><code>LockFeature</code></td>
 * <td align="center">-</td>
 * <td>The purpose of the <code>LockFeature</code> operation is to expose a long term feature
 * locking mechanism to ensure consistency. The lock is considered long term because network latency
 * would make feature locks last relatively longer than native commercial database locks.</td>
 * </tr>
 * <tr>
 * <td><code>Transaction</code></td>
 * <td align="center">-</td>
 * <td>The <code>Transaction</code> operation is used to describe data transformation operations
 * that are to be applied to web accessible feature instances. When the transaction has been
 * completed, a web feature service will generate an XML response document indicating the completion
 * status of the transaction.</td>
 * </tr>
 * </table>
 *
 * @see org.deegree.ogcwebservices.getcapabilities.OperationsMetadata
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WFSOperationsMetadata extends OperationsMetadata {

    private static final long serialVersionUID = -3953425919713834071L;

    /**
     *
     */
    public static final String DESCRIBE_FEATURETYPE_NAME = "DescribeFeatureType";

    /**
     *
     */
    public static final String GET_FEATURE_NAME = "GetFeature";

    /**
     *
     */
    public static final String GET_FEATURE_WITH_LOCK_NAME = "GetFeatureWithLock";

    /**
     *
     */
    public static final String GET_GML_OBJECT_NAME = "GetGmlObject";

    /**
     *
     */
    public static final String LOCK_FEATURE_NAME = "LockFeature";

    /**
     *
     */
    public static final String TRANSACTION_NAME = "Transaction";

    private Operation describeFeatureType;

    private Operation getFeature;

    private Operation getFeatureWithLock;

    private Operation getGMLObject;

    private Operation lockFeature;

    private Operation transaction;

    /**
     * Constructs a new <code>WFSOperationsMetadata</code> instance from the given parameters.
     *
     * @param getCapabilities
     * @param describeFeatureType
     * @param getFeature
     * @param getFeatureWithLock
     *            optional operation (may be null)
     * @param getGMLObject
     *            optional operation (may be null)
     * @param lockFeature
     *            optional operation (may be null)
     * @param transaction
     *            optional operation (may be null)
     * @param parameters
     * @param constraints
     */
    public WFSOperationsMetadata( Operation getCapabilities, Operation describeFeatureType,
                                  Operation getFeature, Operation getFeatureWithLock,
                                  Operation getGMLObject, Operation lockFeature,
                                  Operation transaction, OWSDomainType[] parameters,
                                  OWSDomainType[] constraints ) {
        super( getCapabilities, parameters, constraints );
        this.describeFeatureType = describeFeatureType;
        this.getFeature = getFeature;
        this.getFeatureWithLock = getFeatureWithLock;
        this.getGMLObject = getGMLObject;
        this.lockFeature = lockFeature;
        this.transaction = transaction;
    }

    /**
     * Returns all <code>Operations</code> known to the WFS.
     *
     * @return all <code>Operations</code> known to the WFS.
     */
    @Override
    public Operation[] getOperations() {
        List<Operation> list = new ArrayList<Operation>( 10 );
        list.add( getFeature );
        list.add( describeFeatureType );
        list.add( getCapabilitiesOperation );
        if ( getFeatureWithLock != null ) {
            list.add( getFeatureWithLock );
        }
        if ( getGMLObject != null ) {
            list.add( getGMLObject );
        }
        if ( lockFeature != null ) {
            list.add( lockFeature );
        }
        if ( transaction != null ) {
            list.add( transaction );
        }

        Operation[] ops = new Operation[list.size()];
        return list.toArray( ops );
    }

    /**
     * @return Returns the describeFeatureType <code>Operation</code>.
     */
    public Operation getDescribeFeatureType() {
        return describeFeatureType;
    }

    /**
     * @param describeFeatureType
     *            The describeFeatureType <code>Operation</code> to set.
     */
    public void setDescribeFeatureType( Operation describeFeatureType ) {
        this.describeFeatureType = describeFeatureType;
    }

    /**
     * @return Returns the getFeature <code>Operation</code>.
     */
    public Operation getGetFeature() {
        return getFeature;
    }

    /**
     * @param getFeature
     *            The getFeature <code>Operation</code> to set.
     */
    public void setGetFeature( Operation getFeature ) {
        this.getFeature = getFeature;
    }

    /**
     * @return Returns the getFeatureWithLock <code>Operation</code>.
     */
    public Operation getGetFeatureWithLock() {
        return getFeatureWithLock;
    }

    /**
     * @param getFeatureWithLock
     *            The getFeatureWithLock <code>Operation</code> to set.
     */
    public void setGetFeatureWithLock( Operation getFeatureWithLock ) {
        this.getFeatureWithLock = getFeatureWithLock;
    }

    /**
     * @return Returns the getGMLObject <code>Operation</code>.
     */
    public Operation getGetGMLObject() {
        return getGMLObject;
    }

    /**
     * @param getGMLObject
     *            The getGMLObject <code>Operation</code> to set.
     */
    public void setGetGMLObject( Operation getGMLObject ) {
        this.getGMLObject = getGMLObject;
    }

    /**
     * @return Returns the lockFeature <code>Operation</code>.
     */
    public Operation getLockFeature() {
        return lockFeature;
    }

    /**
     * @param lockFeature
     *            The lockFeature <code>Operation</code> to set.
     */
    public void setLockFeature( Operation lockFeature ) {
        this.lockFeature = lockFeature;
    }

    /**
     * @return Returns the transaction <code>Operation</code>.
     */
    public Operation getTransaction() {
        return transaction;
    }

    /**
     * @param transaction
     *            The transaction <code>Operation</code> to set.
     */
    public void setTransaction( Operation transaction ) {
        this.transaction = transaction;
    }
}
