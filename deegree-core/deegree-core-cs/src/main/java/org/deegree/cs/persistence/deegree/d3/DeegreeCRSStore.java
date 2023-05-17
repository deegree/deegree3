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

package org.deegree.cs.persistence.deegree.d3;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSResource;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.CRSConfigurationException;
import org.deegree.cs.persistence.AbstractCRSStore;
import org.deegree.cs.persistence.deegree.d3.parsers.CoordinateSystemParser;
import org.deegree.cs.persistence.deegree.d3.parsers.DatumParser;
import org.deegree.cs.persistence.deegree.d3.parsers.EllipsoidParser;
import org.deegree.cs.persistence.deegree.d3.parsers.PrimemeridianParser;
import org.deegree.cs.persistence.deegree.d3.parsers.ProjectionParser;
import org.deegree.cs.persistence.deegree.d3.parsers.TransformationParser;
import org.deegree.cs.projections.Projection;
import org.deegree.cs.transformations.Transformation;
import org.deegree.cs.transformations.TransformationFactory;
import org.deegree.cs.transformations.TransformationFactory.DSTransform;
import org.slf4j.Logger;

/**
 * The <code>DeegreeCRSStore</code> reads the deegree crs-config (based on it's own
 * xml-schema) and creates the CRS's (and their datums, conversion info's, ellipsoids and
 * projections) if requested.
 * <p>
 * Attention, although urn's are case-sensitive, the deegreeCRSProvider is not. All
 * incoming id's are toLowerCased!
 * </p>
 * <h2>Automatic loading of projection/transformation classes</h2> It is possible to
 * create your own projection/transformation classes, which can be automatically loaded.
 * <p>
 * You can achieve this loading by supplying the <b><code>class</code></b> attribute to a
 * <code>crs:projectedCRS/crs:projection</code> or
 * <code>crs:coordinateSystem/crs:transformation</code> element in the
 * 'deegree-crs-configuration.xml'. This attribute must contain the full class name (with
 * package), e.g. &lt;crs:projection class='my.package.and.projection.Implementation'&gt;
 * </p>
 * Because the loading is done with reflections your classes must sustain following
 * criteria:
 * <h3>Projections</h3>
 * <ol>
 * <li>It must be a sub class of {@link org.deegree.cs.projections.Projection}</li>
 * <li>A constructor with following signature must be supplied: <br/>
 * <code>
 * public MyProjection( <br/>
 * &emsp;&emsp;&emsp;&emsp;{@link org.deegree.cs.coordinatesystems.GeographicCRS} underlyingCRS,<br/>
 * &emsp;&emsp;&emsp;&emsp;double falseNorthing,<br/>
 * &emsp;&emsp;&emsp;&emsp;double falseEasting,<br/>
 * &emsp;&emsp;&emsp;&emsp;javax.vecmath.Point2d naturalOrigin,<br/>
 * &emsp;&emsp;&emsp;&emsp;{@link org.deegree.cs.components.Unit} units,<br/>
 * &emsp;&emsp;&emsp;&emsp;double scale,<br/>
 * &emsp;&emsp;&emsp;&emsp;java.util.List&lt;org.w3c.dom.Element&gt; yourProjectionElements<br/>
 * );<br/>
 * </code>
 * <p>
 * The first six parameters are common to all projections (for an explanation of their
 * meaning take a look at {@link Projection}). The last list, will contain all xml-dom
 * elements you supplied in the deegree configuration (child elements of the
 * crs:projection/crs:MyProjection), thus relieving you of the parsing of the
 * deegree-crs-configuration.xml document.
 * </p>
 * </li>
 * </ol>
 * <h3>Transformations</h3>
 * <ol>
 * <li>It must be a sub class of
 * {@link org.deegree.cs.transformations.polynomial.PolynomialTransformation}</li>
 * <li>A constructor with following signature must be supplied: <br/>
 * <code>
 * public MyTransformation( <br/>
 * &emsp;&emsp;&emsp;&emsp;java.util.list&lt;Double&gt; aValues,<br/>
 * &emsp;&emsp;&emsp;&emsp;java.util.list&lt;Double&gt; bValues,<br/>
 * &emsp;&emsp;&emsp;&emsp;{@link org.deegree.cs.coordinatesystems.CRS} targetCRS,<br/>
 * &emsp;&emsp;&emsp;&emsp;java.util.List&lt;org.w3c.dom.Element&gt; yourTransformationElements<br/>
 * );<br/>
 * </code>
 * <p>
 * The first three parameters are common to all polynomial values (for an explanation of
 * their meaning take a look at
 * {@link org.deegree.cs.transformations.polynomial.PolynomialTransformation}). Again, the
 * last list, will contain all xml-dom elements you supplied in the deegree configuration
 * (child elements of the crs:transformation/crs:MyTransformation), thus relieving you of
 * the parsing of the deegree-crs-configuration.xml document.
 * </p>
 * </li>
 * </ol>
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @param <T> The return type of the {@link CRSParser#getURIAsType(String)} method
 *
 */
public class DeegreeCRSStore extends AbstractCRSStore {

	private static final Logger LOG = getLogger(DeegreeCRSStore.class);

	/** Default namespace of the crs configuration */
	public static final String CRS_NS = "http://www.deegree.org/crs";

	private final org.deegree.cs.persistence.deegree.d3.parsers.CoordinateSystemParser crs;

	private final DatumParser datums;

	private final ProjectionParser proj;

	private final TransformationParser trans;

	private final EllipsoidParser ellips;

	private final PrimemeridianParser pm;

	private Map<RESOURCETYPE, DeegreeReferenceResolver> typeToResolver = new HashMap<RESOURCETYPE, DeegreeReferenceResolver>();

	public DeegreeReferenceResolver getResolver(RESOURCETYPE resourceType) {
		if (!typeToResolver.containsKey(resourceType)) {
			typeToResolver.put(resourceType, new DeegreeReferenceResolver(this, resourceType));
		}
		return typeToResolver.get(resourceType);
	}

	/**
	 * @param properties containing information about the crs resource class and the file
	 * location of the crs configuration. If either is null the default mechanism is using
	 * the {@link DeegreeReferenceResolver} and the deegree-crs-configuration.xml
	 * @throws CRSConfigurationException if the give file or the
	 * default-crs-configuration.xml file could not be loaded.
	 */
	public DeegreeCRSStore(DSTransform prefTransformType, URL resolvedURL) {
		super(prefTransformType);
		TransformationFactory.DSTransform datumShift = TransformationFactory.DSTransform.HELMERT;
		if (resolvedURL == null) {
			throw new CRSConfigurationException(
					"Could not instantiate crs definitions, please make sure the coordinate system definitions are on the class path.");
		}

		try {
			XMLStreamReader reader = XMLInputFactory.newInstance()
				.createXMLStreamReader(resolvedURL.toExternalForm(), resolvedURL.openStream());
			// CRSConfigurations
			XMLStreamUtils.nextElement(reader);
			if (reader.getName().equals(new QName(CRS_NS, "CRSConfiguration"))) {
				// ProjectionsFile.
				XMLStreamUtils.nextElement(reader);
				/* instantiate the parsers */
				String cUrl = XMLStreamUtils.getText(reader, new QName(CRS_NS, "ProjectionsFile"),
						"projection-definitions.xml", true);
				URL url = XMLStreamUtils.resolve(cUrl, reader);
				proj = new ProjectionParser(this, url);

				cUrl = XMLStreamUtils.getText(reader, new QName(CRS_NS, "TransformationsFile"),
						"transformation-definitions.xml", true);
				url = XMLStreamUtils.resolve(cUrl, reader);
				trans = new TransformationParser(this, url, datumShift);

				cUrl = XMLStreamUtils.getText(reader, new QName(CRS_NS, "PrimeMeridiansFile"), "pm-definitions.xml",
						true);
				url = XMLStreamUtils.resolve(cUrl, reader);
				pm = new PrimemeridianParser(this, url);

				cUrl = XMLStreamUtils.getText(reader, new QName(CRS_NS, "EllispoidsFile"), "ellipsoid-definitions.xml",
						true);
				url = XMLStreamUtils.resolve(cUrl, reader);
				ellips = new EllipsoidParser(this, url);

				cUrl = XMLStreamUtils.getText(reader, new QName(CRS_NS, "DatumsFile"), "datum-definitions.xml", true);
				url = XMLStreamUtils.resolve(cUrl, reader);
				datums = new DatumParser(this, url);

				cUrl = XMLStreamUtils.getText(reader, new QName(CRS_NS, "CRSsFile"), "crs-definitions.xml", true);
				url = XMLStreamUtils.resolve(cUrl, reader);
				crs = new CoordinateSystemParser(this, url);
			}
			else {
				throw new CRSConfigurationException(
						"Could not instantiate crs definitions because the root element is not {" + CRS_NS
								+ "}:CRSConfiguration.");
			}
		}
		catch (XMLStreamException e) {
			throw new CRSConfigurationException(
					"Could not instantiate crs definitions because: " + e.getLocalizedMessage());
		}
		catch (FactoryConfigurationError e) {
			throw new CRSConfigurationException(
					"Could not instantiate crs definitions because: " + e.getLocalizedMessage());
		}
		catch (IOException e) {
			throw new CRSConfigurationException(
					"Could not instantiate crs definitions because: " + e.getLocalizedMessage());
		}
	}

	@Override
	public void init() {
		// Nothing to do
	}

	@Override
	public List<ICRS> getAvailableCRSs() throws CRSConfigurationException {
		List<ICRS> allSystems = new LinkedList<ICRS>();
		Set<String> knownIds = new HashSet<String>();
		List<CRSCodeType[]> allCRSIDs = getAvailableCRSCodes();
		final int total = allCRSIDs.size();
		// int count = 0;
		// int percentage = (int) Math.round( total / 100.d );
		// int number = 0;
		LOG.info("Trying to create a total of " + total + " coordinate systems.");
		for (CRSCodeType[] crsID : allCRSIDs) {
			if (crsID != null) {
				String id = crsID[0].getOriginal();
				if (id != null && !"".equals(id.trim())) {
					// if ( count++ % percentage == 0 ) {
					// System.out.println( ( number ) + ( ( number++ < 10 ) ? " " : "" ) +
					// "% created" );
					// }
					if (!knownIds.contains(id.toLowerCase())) {
						allSystems.add(getCRSByCode(CRSCodeType.valueOf(id)));
						for (CRSCodeType code : crsID) {
							knownIds.add(code.getOriginal().toLowerCase());
						}
					}
				}
			}
		}
		System.out.println();
		return allSystems;
	}

	@Override
	public List<CRSCodeType[]> getAvailableCRSCodes() throws CRSConfigurationException {
		return crs.getAvailableCRSs();
	}

	/**
	 * Returns an {@link CRSResource} with the given ID and from the given
	 * {@link RESOURCETYPE}. IF resourceType is <code>null</code> the an arbitrary
	 * {@link CRSResource} with the id will be returned. If no such an {@link CRSResource}
	 * could be found <code>null</code> will be returned.
	 * @param id id the resource, must not be<code>null</code>
	 * @param resourceType the type of the resource to return or <code>null</code> all for
	 * all types should be looked
	 * @return the {@link CRSResource} Object or <code>null</code> if no such Object was
	 * found.
	 */
	public CRSResource getCRSResource(String id, RESOURCETYPE resourceType) {
		// try to get from cache
		CRSResource result = getCachedIdentifiable(id);
		// not in cache? parse resource!
		if (result == null) {
			if (resourceType != null) {
				switch (resourceType) {
					case CRS:
						result = crs.getCRSForId(id);
						break;
					case DATUM:
						result = datums.getGeodeticDatumForId(id);
						break;
					case ELLIPSOID:
						result = ellips.getEllipsoidForId(id);
						break;
					case PM:
						result = pm.getPrimeMeridianForId(id);
						break;
					case PROJECTION:
						result = proj.getProjectionForId(id);
						break;
					case TRANSFORMATION:
						result = trans.getTransformationForId(id);
						break;
				}
			}
			else {
				for (RESOURCETYPE type : RESOURCETYPE.values()) {
					result = getCRSResource(id, type);
					if (result != null) {
						break;
					}
				}
			}
			addIdToCache(result, false);
		}
		return result;
	}

	@Override
	public ICRS getCoordinateSystem(String id) {
		return (ICRS) getCRSResource(id, RESOURCETYPE.CRS);
	}

	@Override
	public Transformation getDirectTransformation(ICRS sourceCRS, ICRS targetCRS) throws CRSConfigurationException {
		return trans.getTransformation(sourceCRS, targetCRS);
	}

	@Override
	public Transformation getDirectTransformation(String id) throws CRSConfigurationException {
		return trans.getTransformationForId(id);
	}

	@Override
	public CRSResource getCRSResource(CRSCodeType id) throws CRSConfigurationException {
		return getCRSResource(id.getOriginal(), null);
	}

}
