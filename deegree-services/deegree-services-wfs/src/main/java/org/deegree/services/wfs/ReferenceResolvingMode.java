package org.deegree.services.wfs;

import org.deegree.protocol.wfs.transaction.action.Insert;

/**
 * Enum type for discriminating the resolve reference strategy for {@link Insert} actions.
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public enum ReferenceResolvingMode {

    /** Check references. */
    CHECK_ALL,

    /** Check references internally. */
    CHECK_INTERNALLY,

    /** Do not check references. */
    SKIP_ALL

}