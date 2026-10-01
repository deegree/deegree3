package org.deegree.featureinfo.templating;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ServiceLoader;

import org.deegree.featureinfo.FeatureInfoManager;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GfiRetriever {

	private static final ServiceLoader<GfiTemplateProvider> gfiTemplateProviderLoader;

	static {
		gfiTemplateProviderLoader = ServiceLoader.load(GfiTemplateProvider.class);
		for (GfiTemplateProvider gfiTemplateProvider : gfiTemplateProviderLoader) {
			gfiTemplateProvider.init();
		}
	}

	/**
	 * @param gfiFile to retrieve the template from, may be <code>null</code>
	 * @param gfiTemplateParam the value of the gfiTemplate parameter, may be
	 * <code>null</code>
	 * @return the template from the gfiTemplateParam, if available or from the gfiFile if
	 * not <code>null</code>, if both are <code>null</code> or not available the default
	 * 'html.gfi' is returned
	 * @throws FileNotFoundException if the gfiFile could not be found
	 */
	InputStream retrieveTemplate(String gfiFile, String gfiTemplateParam) throws FileNotFoundException {
		if (gfiTemplateParam != null && !gfiTemplateParam.isEmpty()) {
			for (GfiTemplateProvider gfiTemplateProvider : gfiTemplateProviderLoader) {
				InputStream gfiTemplateFromProvider = gfiTemplateProvider.retrieveTemplate(gfiTemplateParam);
				if (gfiTemplateFromProvider != null)
					return gfiTemplateFromProvider;
			}
		}
		if (gfiFile == null) {
			return FeatureInfoManager.class.getResourceAsStream("html.gfi");
		}
		return new FileInputStream(gfiFile);
	}

}
