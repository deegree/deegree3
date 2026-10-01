package org.deegree.featureinfo.templating;

import java.io.InputStream;

/**
 * Implementations of this class provides gfi templates from a passed query param.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public interface GfiTemplateProvider {

	/**
	 * Called once to initialize the GfiTemplateProvider
	 */
	void init();

	/**
	 * @param gfiTemplateParam the value of the query param, never <code>null</code>
	 * @return the content associated to the query param, may be <code>null</code> if not
	 * retrievable or parseable
	 */
	InputStream retrieveTemplate(String gfiTemplateParam);

}
