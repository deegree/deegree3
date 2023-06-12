package org.deegree.protocol.wfs;

import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_NS;

import org.deegree.commons.tom.ows.Version;

/**
 * Enum type for discriminating between the different versions of the
 * <a href="http://www.opengeospatial.org/standards/wfs">OpenGIS Web Feature Service (WFS)
 * Implementation Specification</a>.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public enum WFSVersion {

	/** WFS version 1.0.0 (OGC document #02-058 ) */
	WFS_100("1.0.0", WFS_NS),
	/** WFS version 1.1.0 (OGC document #04-094 ) */
	WFS_110("1.1.0", WFS_NS),
	/** WFS version 2.0.0, ISO 19142 (OGC document #09-025r1 ) */
	WFS_200("2.0.0", WFS_200_NS);

	private final Version ogcVersion;

	private final String ns;

	private WFSVersion(String ogcVersion, String ns) {
		this.ogcVersion = Version.parseVersion(ogcVersion);
		this.ns = ns;
	}

	/**
	 * Returns a corresponding OGC/OWS {@link Version} object.
	 * @return corresponding version object, never <code>null</code>
	 */
	public Version getOGCVersion() {
		return ogcVersion;
	}

	/**
	 * Returns the corresponding XML namespace URI.
	 * @return corresponding XML namespace URI, never <code>null</code>
	 */
	public String getNamespaceUri() {
		return ns;
	}

	/**
	 * Returns the enum constant that corresponds to the given {@link Version}.
	 * @param ogcVersion version, must not be <code>null</code>
	 * @return corresponding constant, never <code>null</code>
	 * @throws IllegalArgumentException if the version is not a known/supported WFS
	 * version
	 */
	public static WFSVersion valueOf(Version ogcVersion) {
		for (WFSVersion wfsVersion : values()) {
			if (wfsVersion.ogcVersion.equals(ogcVersion)) {
				return wfsVersion;
			}
		}
		String msg = "Version '" + ogcVersion + "' does not denote a known/supported WFS version.";
		throw new IllegalArgumentException(msg);
	}

	/**
	 * Returns the enum constants that correspond to the given XML namespace URIs.
	 * @param ns XML namespace, must not be <code>null</code>
	 * @return corresponding constants, never <code>null</code> and contains at least one
	 * entry
	 * @throws NullPointerException if the namespace URI is <code>null</code>
	 * @throws IllegalArgumentException if the namespace URI does not correspond to a
	 * known/supported WFS version
	 */
	public static WFSVersion[] valuesForNamespaceUri(String ns) {
		if (ns == null) {
			String msg = "Namespace must not be null.";
			throw new NullPointerException(msg);
		}
		if (WFS_NS.equals(ns)) {
			return new WFSVersion[] { WFS_100, WFS_110 };
		}
		if (WFS_200_NS.equals(ns)) {
			return new WFSVersion[] { WFS_200 };
		}
		String msg = "Namespace URI '" + ns + "' does not correspond to a known/supported WFS version.";
		throw new IllegalArgumentException(msg);
	}

}