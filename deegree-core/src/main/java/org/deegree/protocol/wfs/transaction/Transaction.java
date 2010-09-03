package org.deegree.protocol.wfs.transaction;

import org.deegree.commons.tom.ows.Version;
import org.deegree.protocol.wfs.AbstractWFSRequest;

/**
 * Represents a <code>Transaction</code> request to a web feature service.
 * <p>
 * A <code>Transaction</code> consists of a sequence of {@link Insert}, {@link Update}, {@link Delete} and
 * {@link Native} operations.
 * <p>
 * From the WFS Specification 1.1.0 OGC 04-094 (#12, Pg.63):
 * <p>
 * <i> A <code>Transaction</code> request is used to describe data transformation operations that are to be applied to
 * web accessible feature instances. When the transaction has been completed, a web feature service will generate an XML
 * response document indicating the completion status of the transaction. </i>
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Transaction extends AbstractWFSRequest {

    private Iterable<TransactionOperation> operations;

    private String lockId;

    private ReleaseAction releaseAction;

    /** Controls how locked features are treated when a transaction request is completed. */
    public static enum ReleaseAction {

        /**
         * Indicates that the locks on all feature instances locked using the associated lockId should be released when
         * the transaction completes, regardless of whether or not a particular feature instance in the locked set was
         * actually operated upon.
         */
        ALL,

        /**
         * Indicates that only the locks on feature instances modified by the transaction should be released. The other,
         * unmodified, feature instances should remain locked using the same lockId so that subsequent transactions can
         * operate on those feature instances. If an expiry period was specified, the expiry counter must be reset to
         * zero after each transaction unless all feature instances in the locked set have been operated upon.
         */
        SOME
    }

    /**
     * Creates a new {@link Transaction} request.
     * 
     * @param version
     *            protocol version, must not be null
     * @param handle
     *            client-generated identifier, can be null
     * @param lockId
     *            lockd id, can be null
     * @param releaseAction
     *            controls how to treat locked features when the transaction has been completed, can be null
     *            (unspecified)
     * @param operations
     *            operations to be performed as parts of the transaction, can be null
     */
    public Transaction( Version version, String handle, String lockId, ReleaseAction releaseAction,
                        Iterable<TransactionOperation> operations ) {
        super( version, handle );
        this.lockId = lockId;
        this.releaseAction = releaseAction;
        this.operations = operations;
    }

    /**
     * Returns the lock identifier provided with this transaction.
     * 
     * @return the lock identifier provided with this transaction, or null if it is unspecified
     */
    public String getLockId() {
        return this.lockId;
    }

    /**
     * Returns the release action mode to be applied after the transaction has been executed successfully.
     * 
     * @see ReleaseAction
     * @return the release action mode to be applied after the transaction has been executed successfully, or null if it
     *         is unspecified
     */
    public ReleaseAction getReleaseAction() {
        return this.releaseAction;
    }

    /**
     * Returns the {@link TransactionOperation}s that are contained in the transaction.
     * 
     * @return the contained operations, can be null
     */
    public Iterable<TransactionOperation> getOperations() {
        return this.operations;
    }
}
