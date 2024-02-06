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
package org.deegree.moduleinfo;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to deegree module metadata (e.g. Maven artifact identifier and build
 * information). The information is obtained through {@link Package} or extracted from
 * <code>META-INF/MANIFEST.MF</code> on the classpath.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 * @since 3.4
 */
public final class ModuleInfo implements Comparable<ModuleInfo> {

	private static final String META_INF_MANIFEST = "/META-INF/MANIFEST.MF";

	private static final Logger LOG = LoggerFactory.getLogger(ModuleInfo.class);

	private final String artifactId;

	private final String version;

	private final String scmRevision;

	private final String buildDate;

	private final String buildBy;

	private ModuleInfo(String artifactId, String version, String scmRevision, String buildDate, String buildBy) {
		this.artifactId = artifactId;
		this.version = version;
		this.scmRevision = scmRevision;
		this.buildDate = buildDate;
		this.buildBy = buildBy;
	}

	public String getArtifactId() {
		return artifactId;
	}

	/**
	 * Returns the module's version.
	 * @return the version number
	 */
	public String getVersion() {
		return version;
	}

	@Override
	public int compareTo(ModuleInfo that) {
		return toString().compareTo(that.toString());
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ModuleInfo) {
			return this.toString().equals(o.toString());
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(artifactId);
		if (version != null) {
			sb.append("-");
			sb.append(version);
		}
		sb.append(" (git commit ");
		sb.append(scmRevision);
		sb.append(" build@");
		sb.append(buildDate);
		sb.append(" by ");
		sb.append(buildBy);
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Load module info from all modules implementing {@link ModuleInfoProvider}
	 * @return list of module info
	 */
	public static List<ModuleInfo> load() {
		return ServiceLoader.load(ModuleInfoProvider.class) //
			.stream()
			.filter(Objects::nonNull)
			.map(provider -> {
				try {
					return extractModuleInfo(provider.type());
				}
				catch (IOException ioe) {
					LOG.debug("Failed to extract module information for class '{}': {}", //
							provider.type().getName(), ioe.getMessage());
					LOG.trace("Exception", ioe);
					return null;
				}
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	/**
	 * Returns the {@link ModuleInfo} for the deegree module on the given classpath.
	 * @param classpathURL classpath url, must not be <code>null</code>
	 * @return module info or <code>null</code> (if the module does not have file
	 * META-INF/MANIFEST.MF)
	 * @throws IOException if accessing <code>META-INF/MANIFEST.MF</code> fails
	 */
	public static ModuleInfo extractModuleInfo(URL classpathURL) throws IOException {
		return extractModuleInfo(new URLClassLoader(new URL[] { classpathURL }));
	}

	/**
	 * Returns the {@link ModuleInfo} for the deegree module of the specified class.
	 *
	 * Note: metadata of jars build for deegree 3.5 an earlier are incompatible as they
	 * relied on reflection which deegree 3.6 and newer omits whenever feasible.
	 *
	 * @since 3.6
	 * @param clazz Class reference, must not be <code>null</code>
	 * @return module info or <code>null</code> (if the module does not have metadata)
	 */
	public static ModuleInfo extractModuleInfo(Class<?> clazz) throws IOException {
		Package pkg = clazz.getPackage();
		if (pkg == null) {
			return null;
		}
		String version = pkg.getImplementationVersion();
		String vendor = pkg.getImplementationVendor();
		return extractModuleInfo(version, vendor);
	}

	/**
	 * Extract module information from manifest vendor and version
	 */
	public static ModuleInfo extractModuleInfo(String version, String vendor) {

		if (version != null && vendor != null && vendor.contains(":")) {
			// Format: groupId:artifactId:buildNumber:buildTimestamp user.name
			String[] fragments = vendor.split(":", 4);
			if (fragments.length != 4 || fragments[0] == null || fragments[1] == null || fragments[2] == null
					|| fragments[3] == null) {
				return null;
			}
			// String buildGroupId = fragments[0];
			String buildArtifactId = fragments[1];
			String buildRev = fragments[2];
			int posSpace = fragments[3].indexOf(' ');
			if (posSpace == -1) {
				return null;
			}
			String buildDate = fragments[3].substring(0, posSpace);
			String buildBy = fragments[3].substring(posSpace + 1);
			return new ModuleInfo(buildArtifactId, version, buildRev, buildDate, buildBy);
		}
		return null;
	}

	/**
	 * Returns the {@link ModuleInfo} for the deegree module on the given classpath.
	 * @param classLoader classpath, must not be <code>null</code>
	 * @return module info or <code>null</code> (if the module does not have file
	 * META-INF/MANIFEST.MF)
	 * @throws IOException if accessing <code>META-INF/MANIFEST.MF</code> fails
	 */
	private static ModuleInfo extractModuleInfo(ClassLoader classLoader) throws IOException {
		URL classpathURL = classLoader.getResource(META_INF_MANIFEST);
		if (classpathURL != null) {
			try (InputStream buildInfoStream = classLoader.getResourceAsStream(META_INF_MANIFEST)) {
				Properties props = new Properties();
				props.load(buildInfoStream);
				String version = props.getProperty("Implementation-Version");
				String vendor = props.getProperty("Implementation-Vendor");

				if (version != null && vendor != null) {
					return extractModuleInfo(version, vendor);
				}

				// use < 3.6 notation
				String buildArtifactId = props.getProperty("deegree-build-artifactId",
						props.getProperty("build.artifactId"));
				if (buildArtifactId == null) {
					// skipping because this jar is not from deegree
					return null;
				}
				String buildBy = props.getProperty("deegree-build-by", props.getProperty("build.by"));
				String buildDate = props.getProperty("deegree-build-date", props.getProperty("build.date"));
				String buildRev = props.getProperty("deegree-build-rev", props.getProperty("build.svnrev"));
				return new ModuleInfo(buildArtifactId, version, buildRev, buildDate, buildBy);
			}
		}

		return null;
	}

}
