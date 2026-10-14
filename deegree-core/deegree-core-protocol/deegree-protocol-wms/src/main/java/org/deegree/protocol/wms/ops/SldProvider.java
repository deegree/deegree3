package org.deegree.protocol.wms.ops;

/**
 * Implementations of this class provides slds from a passed query sld param.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public interface SldProvider {

	/**
	 * Called once to initialize the SldProvider
	 */
	void init();

	/**
	 * @param sld the value of the sld query param, never <code>null</code>
	 * @return the content associated to the sld query param, may be <code>null</code> if
	 * not retrievable or parseable
	 */
	String retrieveSldBody(String sld);

}
