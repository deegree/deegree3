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
 http://CoordinateSystem.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.cs.transformations;

import static org.deegree.cs.coordinatesystems.CRS.CRSType.COMPOUND;
import static org.deegree.cs.coordinatesystems.CRS.CRSType.GEOCENTRIC;
import static org.deegree.cs.coordinatesystems.CRS.CRSType.GEOGRAPHIC;
import static org.deegree.cs.coordinatesystems.CRS.CRSType.PROJECTED;
import static org.deegree.cs.transformations.coordinate.ConcatenatedTransform.concatenate;
import static org.deegree.cs.transformations.coordinate.MatrixTransform.createMatrixTransform;
import static org.deegree.cs.transformations.helmert.Helmert.createAxisAllignedTransformedHelmertTransformation;
import static org.deegree.cs.transformations.ntv2.NTv2Transformation.createAxisAllignedNTv2Transformation;
import static org.deegree.cs.utilities.MappingUtils.updateFromDefinedTransformations;
import static org.deegree.cs.utilities.Matrix.swapAndRotateGeoAxis;
import static org.deegree.cs.utilities.Matrix.swapAxis;
import static org.deegree.cs.utilities.Matrix.toStdValues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.vecmath.Matrix4d;

import org.deegree.crs.store.AbstractStore;
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.components.IEllipsoid;
import org.deegree.cs.components.IGeodeticDatum;
import org.deegree.cs.coordinatesystems.CRS.CRSType;
import org.deegree.cs.coordinatesystems.CompoundCRS;
import org.deegree.cs.coordinatesystems.GeocentricCRS;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.coordinatesystems.ICompoundCRS;
import org.deegree.cs.coordinatesystems.IGeocentricCRS;
import org.deegree.cs.coordinatesystems.IGeographicCRS;
import org.deegree.cs.coordinatesystems.IProjectedCRS;
import org.deegree.cs.coordinatesystems.ProjectedCRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.persistence.CRSStore;
import org.deegree.cs.refs.coordinatesystem.CRSRef;
import org.deegree.cs.transformations.coordinate.GeocentricTransform;
import org.deegree.cs.transformations.coordinate.IdentityTransform;
import org.deegree.cs.transformations.coordinate.MatrixTransform;
import org.deegree.cs.transformations.coordinate.ProjectionTransform;
import org.deegree.cs.transformations.helmert.Helmert;
import org.deegree.cs.transformations.ntv2.NTv2Transformation;
import org.deegree.cs.transformations.polynomial.LeastSquareApproximation;
import org.deegree.cs.utilities.Matrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>TransformationFactory</code> class is the central access point for all
 * transformations between different crs's.
 * <p>
 * It creates a transformation chain for two given ICoordinateSystems by considering their
 * type. For example the Transformation chain from EPSG:31466 ( a projected crs with
 * underlying geographic crs epsg:4314 using the DHDN datum and the TransverseMercator
 * Projection) to EPSG:28992 (another projected crs with underlying geographic crs
 * epsg:4289 using the 'new Amersfoort Datum' and the StereographicAzimuthal Projection)
 * would result in following Transformation Chain:
 * <ol>
 * <li>Inverse projection - thus getting the coordinates in lat/lon for geographic crs
 * epsg:4314</li>
 * <li>Geodetic transformation - thus getting x-y-z coordinates for geographic crs
 * epsg:4314</li>
 * <li>WGS84 transformation -thus getting the x-y-z coordinates for the WGS84 datum</li>
 * <li>Inverse WGS84 transformation -thus getting the x-y-z coordinates for the geodetic
 * from epsg:4289</li>
 * <li>Inverse geodetic - thus getting the lat/lon for epsg:4289</li>
 * <li>projection - getting the coordinates (in meters) for epsg:28992</li>
 * </ol>
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 */
public class TransformationFactory {

	private static Logger LOG = LoggerFactory.getLogger(TransformationFactory.class);

	private final ICRS wgs84;

	private CRSStore provider;

	private DSTransform preferredDSTransform;

	/**
	 * Defines the type of transformation to use while switching datums.
	 */
	public enum DSTransform {

		/** use the helmert transformation */
		HELMERT,
		/** Try to use an ntv2 transformation */
		NTv2;

		/** The property to use */
		public final static String DS_PROP = "PREFERRED_DATUM_TRANSFORM";

		/**
		 * Create a {@link DSTransform} from the {@link AbstractStore} TransformationType
		 * property. If <code>null</code> {@link #HELMERT} will be returned.
		 * @param store the {@link AbstractStore}
		 * @return the value of key {@link AbstractStore} TransformationType property of
		 * {@link #HELMERT} if the value was not known.
		 */
		public static DSTransform fromSchema(AbstractStore store) {
			DSTransform result = DSTransform.HELMERT;
			if (store != null && store.getTransformationType() != null
					&& "NTv2".equalsIgnoreCase(store.getTransformationType().value())) {
				result = NTv2;
			}
			return result;
		}

		/**
		 * @param transform to check for.
		 * @return true if the name of this data shift transformation hint equals the
		 * implementation name of the given transformation.
		 */
		public boolean isPreferred(Transformation transform) {
			return transform != null && name().equalsIgnoreCase(transform.getImplementationName());
		}

	}

	/**
	 * The default coordinate transformation factory. Will be constructed only when first
	 * needed.
	 * @param provider used to do lookups of transformations
	 */
	public TransformationFactory(CRSStore provider) {
		this.provider = provider;
		this.preferredDSTransform = provider.getPreferedTransformationType();

		this.wgs84 = provider.getCRSByCode(new CRSCodeType("4326", "epsg"));
	}

	/**
	 * Set the default transformation type to use for datum switching.
	 * @param datumTransform to be used preferably.
	 */
	public void setPreferredTransformation(DSTransform datumTransform) {
		this.preferredDSTransform = datumTransform;
	}

	/**
	 * Creates a transformation between two coordinate systems. This method will examine
	 * the coordinate systems in order to construct a transformation between them.
	 * @param sourceCRS Input coordinate system.
	 * @param targetCRS Output coordinate system.
	 * @return A coordinate transformation from <code>sourceCRS</code> to
	 * <code>targetCRS</code>.
	 * @throws TransformationException if no transformation path has been found.
	 * @throws IllegalArgumentException if the sourceCRS or targetCRS are
	 * <code>null</code>.
	 *
	 */
	public Transformation createFromCoordinateSystems(final ICRS sourceCRS, final ICRS targetCRS)
			throws TransformationException, IllegalArgumentException {
		return createFromCoordinateSystems(sourceCRS, targetCRS, null);
	}

	/**
	 * Creates a transformation between two coordinate systems. This method will examine
	 * the coordinate systems in order to construct a transformation between them. The
	 * given list of transformations is taken into consideration.
	 * @param sourceCRS Input coordinate system.
	 * @param targetCRS Output coordinate system.
	 * @param transformationsToBeUsed
	 * @return A coordinate transformation from <code>sourceCRS</code> to
	 * <code>targetCRS</code>.
	 * @throws TransformationException
	 * @throws TransformationException if no transformation path has been found.
	 * @throws IllegalArgumentException if the sourceCRS or targetCRS are
	 * <code>null</code>.
	 *
	 */
	public Transformation createFromCoordinateSystems(ICRS sourceCRS, ICRS targetCRS,
			List<Transformation> transformationsToBeUsed) throws TransformationException {
		if (sourceCRS == null) {
			throw new IllegalArgumentException("The source CRS may not be null");
		}
		if (targetCRS == null) {
			throw new IllegalArgumentException("The target CRS may not be null");
		}
		if (!isSupported(sourceCRS) || !isSupported(targetCRS)) {
			throw new TransformationException(sourceCRS, targetCRS,
					"Either the target crs type or the source crs type was unknown");
		}

		if (sourceCRS.equals(targetCRS)) {
			LOG.debug("Source crs and target crs are equal, no transformation needed (returning identity matrix).");
			final Matrix matrix = new Matrix(sourceCRS.getDimension() + 1);
			matrix.setIdentity();
			return new IdentityTransform(sourceCRS, targetCRS);
		}

		sourceCRS = resolve(sourceCRS);
		targetCRS = resolve(targetCRS);

		List<Transformation> toBeUsed = copyTransformations(transformationsToBeUsed);

		// Do following steps:
		// 1) Check if the 'supplied' transformation already contains a path
		// from source to target.
		// 2) Call crs-config.getTransformation for source and target
		// 3) if no 'direct' transformation try to find an inverse.
		// 4) if helmert was defined but the provider transformation was NTv2,
		// create a chain.
		// 5) if source has direct transformation to target use this
		// 6) create a chain if none of the above apply.

		// check if the list of required transformations contains a 'direct'
		// transformation.
		Transformation result = getRequiredTransformation(toBeUsed, sourceCRS, targetCRS);

		if (result == null) {
			// create AxisFlipTransformation if only the axis order must be flipped
			if (sourceCRS.equalsWithFlippedAxis(targetCRS)) {
				return new AxisFlipTransformation(sourceCRS, targetCRS, new CRSIdentifiable(
						new CRSCodeType("tmp_" + sourceCRS.getCode() + "_flippedTo_" + targetCRS.getCode())));
			}
		}

		if (result == null) {
			// check if a 'direct' transformation could be loaded from the
			// configuration;
			result = getTransformation(sourceCRS, targetCRS);
			if (result == null || "Helmert".equals(result.getImplementationName())) {
				result = getTransformation(targetCRS, sourceCRS);
				if (result != null && !"Helmert".equals(result.getImplementationName())) {
					result.inverse();
				}
				else {
					result = null;
				}
			}
			if (result == null || ("NTv2".equals(result.getImplementationName())
					&& this.preferredDSTransform == DSTransform.HELMERT)) {
				// no configured transformation
				// check if the source crs has an alternative transformation for
				// the given target, if so use it
				if (sourceCRS.hasDirectTransformation(targetCRS)) {
					Transformation direct = sourceCRS.getDirectTransformation(targetCRS);
					if (direct != null) {
						LOG.debug("Using direct (polynomial) transformation instead of a helmert transformation: "
								+ direct.getImplementationName());
						result = direct;
					}
				}
				else {
					// special cases
					if (hasCode("epsg:3857", sourceCRS) && !hasCode("epsg:4326", targetCRS)) {
						// work around for wrong transformation from epsg:3857 by having
						// epsg:4326 as intermediate step
						result = concatenate(createTransformationFromCoordinateSystems(sourceCRS, wgs84),
								createTransformationFromCoordinateSystems(wgs84, targetCRS));
					}
					// original behavior
					else {
						result = createTransformationFromCoordinateSystems(sourceCRS, targetCRS);
					}
				}
			}
			if (result != null) {
				result = updateFromDefinedTransformations(toBeUsed, result);
			}
		}
		if (result == null) {
			LOG.debug("The resulting transformation was null, returning an identity matrix.");
			final Matrix matrix = new Matrix(sourceCRS.getDimension() + 1);
			matrix.setIdentity();
			result = createMatrixTransform(sourceCRS, targetCRS, matrix);
		}
		else {
			// allign axis, if NTv2 transformation
			if ("NTv2".equals(result.getImplementationName()) && this.preferredDSTransform == DSTransform.NTv2) {
				result = NTv2Transformation.createAxisAllignedNTv2Transformation((NTv2Transformation) result);
			}
			LOG.debug("Concatenating the result, with the conversion matrices.");
			result = concatenate(createMatrixTransform(sourceCRS, sourceCRS, toStdValues(sourceCRS, false)), result,
					createMatrixTransform(targetCRS, targetCRS, toStdValues(targetCRS, true)));
		}
		if (LOG.isDebugEnabled()) {
			StringBuilder output = new StringBuilder("The resulting transformation chain: \n");
			if (result == null) {
				output.append(" identity transformation (null)");
			}
			else {
				output = result.getTransformationPath(output);
			}
			LOG.debug(output.toString());

			if (result instanceof MatrixTransform) {
				LOG.debug("Resulting matrix transform:\n" + ((MatrixTransform) result).getMatrix());
			}

		}
		return result;
	}

	/**
	 * Determine if the given code identifies the given CRS.
	 * @param code the code to test
	 * @param crs the CRS to match against the code
	 * @return if the code identifies the given CRS
	 */
	private boolean hasCode(String code, ICRS crs) {
		return Arrays.stream(crs.getOrignalCodeStrings())
			.filter(oc -> oc != null)
			.anyMatch(oc -> oc.equalsIgnoreCase(code));
	}

	private Transformation createTransformationFromCoordinateSystems(ICRS sourceCRS, ICRS targetCRS)
			throws TransformationException {
		CRSType type = sourceCRS.getType();
		switch (type) {
			case COMPOUND:
				/**
				 * Compound --> Projected, Geographic, Geocentric or Compound
				 */
				return createFromCompound((ICompoundCRS) sourceCRS, targetCRS);
			case GEOCENTRIC:
				/**
				 * Geocentric --> Projected, Geographic, Geocentric or Compound
				 */
				return createFromGeocentric((IGeocentricCRS) sourceCRS, targetCRS);
			case GEOGRAPHIC:
				/**
				 * Geographic --> Geographic, Projected, Geocentric or Compound
				 */
				return createFromGeographic((IGeographicCRS) sourceCRS, targetCRS);
			case PROJECTED:
				/**
				 * Projected --> Projected, Geographic, Geocentric or Compound
				 */
				return createFromProjected((IProjectedCRS) sourceCRS, targetCRS);
			case VERTICAL:
			default:
				return null;
		}
	}

	/**
	 * Calls the provider to find a 'configured' transformation, if found a copy will be
	 * returned.
	 * @param sourceCRS
	 * @param targetCRS
	 * @return a copy of a configured provider.
	 */
	private Transformation getTransformation(ICRS sourceCRS, ICRS targetCRS) {
		Transformation result = provider.getDirectTransformation(sourceCRS, targetCRS);
		if (result != null) {
			String implName = result.getImplementationName();
			// make a copy, so inverse can be called without changing the
			// original transformation, which might be cached
			// somewhere.
			if ("Helmert".equals(implName)) {
				Helmert h = (Helmert) result;
				result = new Helmert(h.dx, h.dy, h.dz, h.ex, h.ey, h.ez, h.ppm, h.getSourceCRS(), h.getTargetCRS(), h,
						h.areRotationsInRad());
			}
			else if ("NTv2".equals(implName)) {
				NTv2Transformation h = (NTv2Transformation) result;
				result = new NTv2Transformation(h.getSourceCRS(), h.getTargetCRS(), h, h.getGridfileRef());
			}
			else if ("leastsquare".equals(implName)) {
				LeastSquareApproximation h = (LeastSquareApproximation) result;
				result = new LeastSquareApproximation(h.getFirstParams(), h.getSecondParams(), h.getSourceCRS(),
						h.getTargetCRS(), h.getScaleX(), h.getScaleY(), h);
			}
			else {
				LOG.warn("The transformation with implementation name: " + implName + " could not be copied.");
			}
		}
		return result;
	}

	/**
	 * Iterates over the given transformations and creates a copy of the list without all
	 * duplicates.
	 * @param originalRequested the original requested transformations.
	 * @return a copy of the list without any duplicates or <code>null</code> if the given
	 * list was null or the empty list if the given list was empty.
	 */
	private List<Transformation> copyTransformations(List<Transformation> originalRequested) {
		if (originalRequested == null || originalRequested.isEmpty()) {
			return originalRequested;
		}

		List<Transformation> result = new ArrayList<Transformation>(originalRequested.size());

		// create a none duplicate list of the given transformations, remove all
		// 'duplicates'
		for (Transformation tr : originalRequested) {
			if (tr != null) {
				Iterator<Transformation> it = result.iterator();
				boolean isDuplicate = false;
				while (it.hasNext() && !isDuplicate) {
					Transformation tra = it.next();
					if (tra != null) {
						if (tra.equalOnCRS(tr)) {
							isDuplicate = true;
						}
					}
				}
				if (!isDuplicate) {
					if ("NTv2".equalsIgnoreCase(tr.getImplementationName())) {
						// rb: dirty hack, ntv2 needs lon/lat incoming
						// coordinates, if not set, swap them.
						// the axis must be swapped to fit ntv2 (which is
						// defined on lon/lat.
						tr = createAxisAllignedNTv2Transformation((NTv2Transformation) tr);
					}
					else if ("Helmert".equals(tr.getImplementationName())) {
						tr = createAxisAllignedTransformedHelmertTransformation((Helmert) tr);
					}
					result.add(tr);
					// add the transformation as insverse
					String newId = tr.getTargetCRS() + "_" + tr.getSourceCRS() + "_inverse";
					Transformation trInverse = tr.copyTransformation(new CRSIdentifiable(new CRSCodeType(newId)));
					trInverse.inverse();
					result.add(trInverse);
				}
			}
		}
		return result;
	}

	/**
	 * Iterates over the given transformations and removes a 'fitting' transformation from
	 * the list.
	 * @param requiredTransformations
	 * @param sourceCRS
	 * @param targetCRS
	 * @return the 'required' transformation or <code>null</code> if no fitting
	 * transfromation was found.
	 */
	private Transformation getRequiredTransformation(List<Transformation> requiredTransformations, ICRS sourceCRS,
			ICRS targetCRS) {
		if (requiredTransformations != null && !requiredTransformations.isEmpty()) {
			for (Transformation t : requiredTransformations) {
				if (t != null) {
					boolean matches = (sourceCRS != null) ? sourceCRS.equals(t.getSourceCRS())
							: t.getSourceCRS() == null;
					matches = matches && (targetCRS != null) ? targetCRS.equals(t.getTargetCRS())
							: t.getTargetCRS() == null;
					if (matches) {
						return t;
					}
				}
			}
		}
		return null;
	}

	/**
	 * @param crs
	 * @return true if the crs is one of the Types defined in ICoordinateSystem.
	 */
	private boolean isSupported(ICRS crs) {
		CRSType type = crs.getType();
		return type == COMPOUND || type == GEOCENTRIC || type == GEOGRAPHIC || type == CRSType.PROJECTED;
	}

	private Transformation createFromCompound(ICompoundCRS sourceCRS, ICRS targetCRS) throws TransformationException {
		ICompoundCRS target;
		if (targetCRS.getType() != COMPOUND) {
			target = new CompoundCRS(sourceCRS.getHeightAxis(), targetCRS, sourceCRS.getDefaultHeight(),
					new CRSIdentifiable(new CRSCodeType[] { CRSCodeType.valueOf(targetCRS.getCode() + "_compound") }));
		}
		else {
			target = (ICompoundCRS) targetCRS;
		}
		return createTransformation(sourceCRS, target);
	}

	private Transformation createFromGeocentric(IGeocentricCRS sourceCRS, ICRS targetCRS)
			throws TransformationException {

		targetCRS = resolve(targetCRS);
		Transformation result = null;
		CRSType type = targetCRS.getType();
		switch (type) {
			case COMPOUND:
				ICompoundCRS target = (ICompoundCRS) targetCRS;
				ICompoundCRS sTmp = new CompoundCRS(target.getHeightAxis(), sourceCRS, target.getDefaultHeight(),
						new CRSIdentifiable(
								new CRSCodeType[] { CRSCodeType.valueOf(sourceCRS.getCode() + "_compound") }));
				result = createTransformation(sTmp, target);
				break;
			case GEOCENTRIC:
				result = createTransformation(sourceCRS, (IGeocentricCRS) targetCRS);
				break;
			case GEOGRAPHIC:
				result = createTransformation(sourceCRS, (IGeographicCRS) targetCRS);
				break;
			case PROJECTED:
				result = createTransformation(sourceCRS, (IProjectedCRS) targetCRS);
				break;
			case VERTICAL:
				break;
		}
		return result;
	}

	private Transformation createFromProjected(IProjectedCRS sourceCRS, ICRS targetCRS) throws TransformationException {

		targetCRS = resolve(targetCRS);
		Transformation result = null;
		CRSType type = targetCRS.getType();
		switch (type) {
			case COMPOUND:
				ICompoundCRS target = (ICompoundCRS) targetCRS;
				ICompoundCRS sTmp = new CompoundCRS(target.getHeightAxis(), sourceCRS, target.getDefaultHeight(),
						new CRSIdentifiable(
								new CRSCodeType[] { CRSCodeType.valueOf(sourceCRS.getCode() + "_compound") }));
				result = createTransformation(sTmp, target);
				break;
			case GEOCENTRIC:
				result = createTransformation(sourceCRS, (IGeocentricCRS) targetCRS);
				break;
			case GEOGRAPHIC:
				result = createTransformation(sourceCRS, (IGeographicCRS) targetCRS);
				break;
			case PROJECTED:
				result = createTransformation(sourceCRS, (IProjectedCRS) targetCRS);
				break;
			case VERTICAL:
				break;
		}
		return result;
	}

	private Transformation createFromGeographic(IGeographicCRS sourceCRS, ICRS targetCRS)
			throws TransformationException {

		targetCRS = resolve(targetCRS);
		Transformation result = null;
		CRSType type = targetCRS.getType();
		switch (type) {
			case COMPOUND:
				ICompoundCRS target = (ICompoundCRS) targetCRS;
				ICompoundCRS sTmp = new CompoundCRS(target.getHeightAxis(), sourceCRS, target.getDefaultHeight(),
						new CRSIdentifiable(
								new CRSCodeType[] { CRSCodeType.valueOf(sourceCRS.getCode() + "_compound") }));
				result = createTransformation(sTmp, target);
				break;
			case GEOCENTRIC:
				result = createTransformation(sourceCRS, (IGeocentricCRS) targetCRS);
				break;
			case GEOGRAPHIC:
				result = createTransformation(sourceCRS, (IGeographicCRS) targetCRS);
				break;
			case PROJECTED:
				result = createTransformation(sourceCRS, (IProjectedCRS) targetCRS);
				break;
			case VERTICAL:
				break;
		}
		return result;
	}

	/**
	 * @param sourceCRS
	 * @param targetCRS
	 * @return the transformation chain or <code>null</code> if the transformation
	 * operation is the identity.
	 * @throws TransformationException
	 */
	private Transformation createTransformation(IGeocentricCRS sourceCRS, IGeographicCRS targetCRS)
			throws TransformationException {
		final Transformation result = createTransformation(targetCRS, sourceCRS);
		if (result != null) {
			result.inverse();
		}
		return result;
	}

	/**
	 * @param sourceCRS
	 * @param targetCRS
	 * @return the transformation chain or <code>null</code> if the transformation
	 * operation is the identity.
	 * @throws TransformationException
	 */
	private Transformation createTransformation(IGeocentricCRS sourceCRS, IProjectedCRS targetCRS)
			throws TransformationException {
		final Transformation result = createTransformation(targetCRS, sourceCRS);
		if (result != null) {
			result.inverse();
		}
		return result;
	}

	/**
	 * This method is valid for all transformations which use a compound crs, because the
	 * extra heightvalues need to be considered throughout the transformation.
	 * @param sourceCRS
	 * @param targetCRS
	 * @return the transformation chain or <code>null</code> if the transformation
	 * operation is the identity.
	 * @throws TransformationException
	 */
	private Transformation createTransformation(ICompoundCRS sourceCRS, ICompoundCRS targetCRS)
			throws TransformationException {
		if (sourceCRS.getUnderlyingCRS().equals(targetCRS.getUnderlyingCRS())) {
			return null;
		}
		sourceCRS = ((CompoundCRS) resolve(sourceCRS));
		targetCRS = ((CompoundCRS) resolve(targetCRS));

		LOG.debug("Creating compound( " + sourceCRS.getUnderlyingCRS().getCode() + ") ->compound transformation( "
				+ targetCRS.getUnderlyingCRS().getCode() + "): from (source): " + sourceCRS.getCode() + " to(target): "
				+ targetCRS.getCode());
		final CRSType sourceType = sourceCRS.getUnderlyingCRS().getType();
		final CRSType targetType = targetCRS.getUnderlyingCRS().getType();
		Transformation result = null;
		// basic check for simple (invert) projections
		if (sourceType == PROJECTED && targetType == GEOGRAPHIC) {
			if ((((IProjectedCRS) resolve(sourceCRS.getUnderlyingCRS())).getGeographicCRS())
				.equals(targetCRS.getUnderlyingCRS())) {
				result = new ProjectionTransform((IProjectedCRS) resolve(sourceCRS.getUnderlyingCRS()));
				result.inverse();
			}
		}
		if (sourceType == GEOGRAPHIC && targetType == PROJECTED) {
			if ((((IProjectedCRS) resolve(targetCRS.getUnderlyingCRS())).getGeographicCRS())
				.equals(sourceCRS.getUnderlyingCRS())) {
				result = new ProjectionTransform((IProjectedCRS) resolve(targetCRS.getUnderlyingCRS()));
			}
		}
		if (result == null) {
			IGeocentricCRS sourceGeocentric;
			if (sourceType == GEOCENTRIC) {
				sourceGeocentric = (IGeocentricCRS) resolve(sourceCRS.getUnderlyingCRS());
			}
			else {
				sourceGeocentric = new GeocentricCRS(sourceCRS.getGeodeticDatum(),
						CRSCodeType.valueOf("tmp_" + sourceCRS.getCode() + "_geocentric"),
						sourceCRS.getName() + "_Geocentric");
			}
			IGeocentricCRS targetGeocentric;
			if (targetType == GEOCENTRIC) {
				targetGeocentric = (IGeocentricCRS) resolve(targetCRS.getUnderlyingCRS());
			}
			else {
				targetGeocentric = new GeocentricCRS(targetCRS.getGeodeticDatum(),
						CRSCodeType.valueOf("tmp_" + targetCRS.getCode() + "_geocentric"),
						targetCRS.getName() + "_Geocentric");
			}

			// Transformation helmertTransformation = createTransformation(
			// sourceGeocentric, targetGeocentric );

			Transformation sourceTransformationChain = null;
			Transformation targetTransformationChain = null;
			Transformation sourceT = null;
			Transformation targetT = null;
			IGeographicCRS sourceGeographic = null;
			IGeographicCRS targetGeographic = null;
			switch (sourceType) {
				case GEOCENTRIC:
					break;
				case PROJECTED:
					ICRS underlying = resolve(sourceCRS.getUnderlyingCRS());
					sourceTransformationChain = new ProjectionTransform((ProjectedCRS) underlying);
					sourceTransformationChain.inverse();
					sourceGeographic = ((IProjectedCRS) underlying).getGeographicCRS();
				case GEOGRAPHIC:
					underlying = sourceCRS.getUnderlyingCRS();
					if (sourceGeographic == null) {
						sourceGeographic = (IGeographicCRS) resolve(underlying);
					}
					sourceT = getToWGSTransformation(sourceGeographic);
					/*
					 * Only create a geocentric transform if the helmert transformation !=
					 * null, e.g. the datums and ellipsoids are not equal.
					 */
					// if ( helmertTransformation != null ) {
					// create a 2d->3d mapping.
					final Transformation axisAligned = createMatrixTransform(sourceGeographic, sourceGeocentric,
							swapAxis(sourceGeographic, GeographicCRS.WGS84));
					if (LOG.isDebugEnabled()) {
						StringBuilder sb = new StringBuilder(
								"Resulting axis alignment between source geographic and source geocentric is:");
						if (axisAligned == null) {
							sb.append(" not necessary");
						}
						else {
							sb.append("\n").append(((MatrixTransform) axisAligned).getMatrix());
						}
						LOG.debug(sb.toString());
					}
					final Transformation geoCentricTransform = new GeocentricTransform(sourceCRS, sourceGeocentric);
					// concatenate the possible projection with the axis alignment
					// and the geocentric transform.
					sourceTransformationChain = concatenate(sourceTransformationChain, axisAligned,
							geoCentricTransform);
					// }
					break;
				case COMPOUND:
					// cannot happen.
					break;
				case VERTICAL:
					LOG.warn("Vertical crs is currently not supported for the Compound crs.");
					break;
			}
			switch (targetType) {
				case GEOCENTRIC:
					break;
				case PROJECTED:
					targetTransformationChain = new ProjectionTransform(
							(IProjectedCRS) resolve(targetCRS.getUnderlyingCRS()));
					targetGeographic = ((IProjectedCRS) resolve(targetCRS.getUnderlyingCRS())).getGeographicCRS();
				case GEOGRAPHIC:
					if (targetGeographic == null) {
						targetGeographic = (IGeographicCRS) resolve(targetCRS.getUnderlyingCRS());
					}
					targetT = getToWGSTransformation(targetGeographic);
					/*
					 * Only create a geocentric transform if the helmert transformation !=
					 * null, e.g. the datums and ellipsoids are not equal.
					 */
					// if ( helmertTransformation != null ) {
					// create a 2d->3d mapping.
					final Transformation axisAligned = createMatrixTransform(targetGeocentric, targetGeographic,
							swapAxis(GeographicCRS.WGS84, targetGeographic));
					final Transformation geoCentricTransform = new GeocentricTransform(targetCRS, targetGeocentric);
					geoCentricTransform.inverse();
					// concatenate the possible projection with the axis alignment
					// and the geocentric transform.
					targetTransformationChain = concatenate(geoCentricTransform, axisAligned,
							targetTransformationChain);
					// }
					break;
				case COMPOUND:
					// cannot happen.
					break;
				case VERTICAL:
					LOG.warn("Vertical crs is currently not supported for the Compound crs.");
					break;
			}
			Transformation helmertTransformation = null;
			if (!isIdentity(sourceT) || !isIdentity(targetT)) {
				helmertTransformation = transformUsingPivot(sourceGeocentric, targetGeocentric, (Helmert) sourceT,
						(Helmert) targetT);
			}

			result = concatenate(sourceTransformationChain, helmertTransformation, targetTransformationChain);
		}
		if (result != null) {
			IdentityTransform srcT = null;
			IdentityTransform targetT = null;
			// set identity to false, to avoid detaching in the resulting concatenated
			// transform
			if (!sourceCRS.equals(result.getSourceCRS())) {
				srcT = new IdentityTransform(sourceCRS, result.getSourceCRS());
			}
			if (!targetCRS.equals(result.getTargetCRS())) {
				targetT = new IdentityTransform(result.getTargetCRS(), targetCRS);
			}
			result = concatenate(srcT, result, targetT, true);
		}
		return result;
	}

	/**
	 * Creates a transformation between two geographic coordinate systems. This method is
	 * automatically invoked by {@link #createFromCoordinateSystems
	 * createFromCoordinateSystems(...)}. The default implementation can adjust axis order
	 * and orientation (e.g. transforming from <code>(NORTH,WEST)</code> to
	 * <code>(EAST,NORTH)</code>), performs units conversion and apply Bursa Wolf
	 * transformation if needed.
	 * @param sourceCRS Input coordinate system.
	 * @param targetCRS Output coordinate system.
	 * @return A coordinate transformation from <code>sourceCRS</code> to
	 * <code>targetCRS</code>.
	 * @throws TransformationException if no transformation path has been found.
	 */
	private Transformation createTransformation(final IGeographicCRS sourceCRS, final IGeographicCRS targetCRS)
			throws TransformationException {
		// check if a 'direct' transformation could be loaded from the
		// configuration;
		Transformation result = getTransformation(sourceCRS, targetCRS);
		if (result == null) {
			// maybe an inverse was defined?
			result = getTransformation(targetCRS, sourceCRS);
			if (result != null) {
				result.inverse();
			}
		}
		// prepare the found transformation if it is a helmert transfomation
		if (result != null && !isIdentity(result) && "Helmert".equalsIgnoreCase(result.getImplementationName())
				&& this.preferredDSTransform.isPreferred(result)) {
			LOG.debug("Creating geographic -> geographic transformation: from (source): " + sourceCRS.getCode()
					+ " to(target): " + targetCRS.getCode() + " based on a given Helmert transformation");

			final IGeodeticDatum sourceDatum = sourceCRS.getGeodeticDatum();
			final IGeodeticDatum targetDatum = targetCRS.getGeodeticDatum();
			String name = sourceCRS.getName() + "_Geocentric";
			final IGeocentricCRS sourceGCS = new GeocentricCRS(sourceDatum, sourceCRS.getCode(), name);
			name = targetCRS.getName() + "_Geocentric";
			final IGeocentricCRS targetGCS = new GeocentricCRS(targetDatum, targetCRS.getCode(), name);

			Transformation step1;
			Transformation step2;
			Transformation step3;
			// geographic->geocentric
			step1 = createTransformation(sourceCRS, sourceGCS);
			// transformation found in configuration
			step2 = result;
			// geocentric->geographic
			step3 = createTransformation(targetCRS, targetGCS);

			if (step3 != null) {
				step3.inverse();// call inverseTransform from step 3.
			}
			return concatenate(step1, step2, step3);

		}
		else if (result == null || "Helmert".equalsIgnoreCase(result.getImplementationName())
				|| !this.preferredDSTransform.isPreferred(result)) {
			LOG.debug("Creating geographic ->geographic transformation: from (source): " + sourceCRS.getCode()
					+ " to(target): " + targetCRS.getCode());
			// if a conversion needs to take place
			if (isEllipsoidTransformNeeded(sourceCRS, targetCRS)) {
				Transformation sourceT = getToWGSTransformation(sourceCRS);
				Transformation targetT = getToWGSTransformation(targetCRS);
				if ((!isIdentity(sourceT)) || (!isIdentity(targetT))) {

					// the default implementation uses the WGS84 as a pivot for
					// helmert transformations.
					if ((sourceT != null && "Helmert".equals(sourceT.getImplementationName()))
							|| (targetT != null && "Helmert".equals(targetT.getImplementationName()))) {
						Helmert sourceH = (Helmert) sourceT;
						Helmert targetH = (Helmert) targetT;
						final IGeodeticDatum sourceDatum = sourceCRS.getGeodeticDatum();
						final IGeodeticDatum targetDatum = targetCRS.getGeodeticDatum();

						/*
						 * If the two geographic coordinate systems use different
						 * ellipsoid, convert from the source to target ellipsoid through
						 * the geocentric coordinate system.
						 */
						Transformation step1;
						Transformation step2;
						Transformation step3;
						// use the WGS84 Geocentric transform if no toWGS84
						// parameters are given and the datums
						// ellipsoid is actually a sphere.
						String name = sourceCRS.getName() + "_Geocentric";
						final IGeocentricCRS sourceGCS = (sourceDatum.getEllipsoid().isSphere() && isIdentity(sourceH))
								? GeocentricCRS.WGS84 : new GeocentricCRS(sourceDatum, sourceCRS.getCode(), name);
						name = targetCRS.getName() + "_Geocentric";
						final IGeocentricCRS targetGCS = (targetDatum.getEllipsoid().isSphere() && isIdentity(targetH))
								? GeocentricCRS.WGS84 : new GeocentricCRS(targetDatum, targetCRS.getCode(), name);

						// geographic->geocentric
						step1 = createTransformation(sourceCRS, sourceGCS);
						// helmert->inv_helmert
						// step2 = createTransformation( sourceGCS, targetGCS );
						step2 = transformUsingPivot(sourceGCS, targetGCS, sourceH, targetH);
						// geocentric->geographic
						step3 = createTransformation(targetCRS, targetGCS);

						if (step3 != null) {
							step3.inverse();// call inverseTransform from step
							// 3.
						}
						return concatenate(step1, step2, step3);
					}
				}
			}

			/*
			 * Swap axis order, and rotate the longitude coordinate if prime meridians are
			 * different.
			 */
			final Matrix matrix = swapAndRotateGeoAxis(sourceCRS, targetCRS);
			result = createMatrixTransform(sourceCRS, targetCRS, matrix);

			if (LOG.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder(
						"Resulting axis alignment between source geographic and target geographic is:");
				if (result == null) {
					sb.append(" not necessary");
				}
				else {
					sb.append("\n").append(((MatrixTransform) result).getMatrix());
				}
				LOG.debug(sb.toString());
			}
		}
		if (result != null && ("NTv2".equals(result.getImplementationName()))) {
			result = createAxisAllignedNTv2Transformation((NTv2Transformation) result);
		}
		return result;
	}

	/**
	 * Creates a transformation between a geographic and a projected coordinate systems.
	 * This method is automatically invoked by {@link #createFromCoordinateSystems
	 * createFromCoordinateSystems(...)}.
	 * @param sourceCRS Input coordinate system.
	 * @param targetCRS Output coordinate system.
	 * @return A coordinate transformation from <code>sourceCRS</code> to
	 * <code>targetCRS</code>.
	 * @throws TransformationException if no transformation path has been found.
	 */
	private Transformation createTransformation(final IGeographicCRS sourceCRS, final IProjectedCRS targetCRS)
			throws TransformationException {

		Transformation result = getTransformation(sourceCRS, targetCRS);
		if (isIdentity(result)) {
			LOG.debug("Creating geographic->projected transformation: from (source): " + sourceCRS.getCode()
					+ " to(target): " + targetCRS.getCode());
			final IGeographicCRS stepGeoCS = targetCRS.getGeographicCRS();

			final Transformation geo2geo = createTransformation(sourceCRS, stepGeoCS);
			if (LOG.isDebugEnabled()) {
				String message = "Resulting axis alignment between target geographic and target projected is:";
				LOG.debug(message);
			}
			final Transformation projection = new ProjectionTransform(targetCRS);
			result = concatenate(geo2geo, projection);
		}
		return result;
	}

	/**
	 * Creates a transformation between a geographic and a geocentric coordinate systems.
	 * Since the source coordinate systems doesn't have a vertical axis, height above the
	 * ellipsoid is assumed equals to zero everywhere. This method is automatically
	 * invoked by {@link #createFromCoordinateSystems createFromCoordinateSystems(...)}.
	 * @param sourceCRS Input geographic coordinate system.
	 * @param targetCRS Output coordinate system.
	 * @return A coordinate transformation from <code>sourceCRS</code> to
	 * <code>targetCRS</code>.
	 * @throws TransformationException if no transformation path has been found.
	 */
	private Transformation createTransformation(final IGeographicCRS sourceCRS, final IGeocentricCRS targetCRS)
			throws TransformationException {
		LOG.debug("Creating geographic -> geocentric transformation: from (source): " + sourceCRS.getCode()
				+ " to (target): " + targetCRS.getCode());
		Transformation result = getTransformation(sourceCRS, targetCRS);
		if (isIdentity(result)) {
			IGeocentricCRS sourceGeocentric = new GeocentricCRS(sourceCRS.getGeodeticDatum(),
					CRSCodeType.valueOf("tmp_" + sourceCRS.getCode() + "_geocentric"),
					sourceCRS.getName() + "_geocentric");
			Transformation ellipsoidTransform = null;
			if (isEllipsoidTransformNeeded(sourceCRS, targetCRS)) {
				// create a transformation between the two geocentrics
				Transformation sourceT = getToWGSTransformation(sourceCRS);
				Transformation targetT = getToWGSTransformation(targetCRS);
				if (!isIdentity(sourceT) || !isIdentity(targetT)) {
					if (isHelmert(sourceT) && isHelmert(targetT)) {
						ellipsoidTransform = transformUsingPivot(sourceGeocentric, targetCRS, (Helmert) sourceT,
								(Helmert) targetT);

					}
					else {
						// another kind of transformation (NTv2?) exist, handle
						// this..
						if (!isIdentity(sourceT)) {
							if (isHelmert(sourceT)) {
								ellipsoidTransform = transformUsingPivot(sourceGeocentric, GeocentricCRS.WGS84,
										(Helmert) sourceT, null);
							}
							else {
								ellipsoidTransform = sourceT;
							}
						}
						if (!isIdentity(targetT)) {
							if (isHelmert(targetT)) {
								Transformation t = transformUsingPivot(targetCRS, GeocentricCRS.WGS84,
										(Helmert) targetT, null);
								if (t != null) {
									// from wgs84 to target
									t.inverse();
									ellipsoidTransform = concatenate(ellipsoidTransform, t);
								}
							}
							else {
								// the targetT is going from target to wgs84
								targetT.inverse();
								ellipsoidTransform = concatenate(ellipsoidTransform, targetT);
							}
						}
					}
				}
				else {
					// if no helmert transformation is needed, the targetCRS
					// equals the source-geocentric.
					sourceGeocentric = targetCRS;
				}
			}
			final Transformation axisAlign = createMatrixTransform(sourceCRS, createWGSAlligned(sourceCRS),
					swapAndRotateGeoAxis(sourceCRS, GeographicCRS.WGS84));
			if (LOG.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder(
						"Resulting axis alignment between source geographic and target geocentric is:");
				if (axisAlign == null) {
					sb.append(" not necessary");
				}
				else {
					sb.append("\n").append(((MatrixTransform) axisAlign).getMatrix());
				}
				LOG.debug(sb.toString());
			}
			final Transformation geocentric = new GeocentricTransform(sourceCRS, sourceGeocentric);
			result = concatenate(axisAlign, geocentric, ellipsoidTransform);

		}
		return result;
	}

	/**
	 * Create a new geographic crs with the same axis as the wgs.
	 * @param sourceCRS
	 * @return a new crs with same axis as wgs84.
	 */
	public final static IGeographicCRS createWGSAlligned(IGeographicCRS sourceCRS) {
		return new GeographicCRS(sourceCRS.getGeodeticDatum(), GeographicCRS.WGS84.getAxis(),
				new CRSCodeType("wgsalligned"));
	}

	private boolean isHelmert(Transformation transformation) {
		return transformation == null || ("Helmert".equalsIgnoreCase(transformation.getImplementationName()));
	}

	/**
	 * Tries to get a WGS84 transformation from the configuratin or the datum.
	 * @param sourceCRS to get a wgs84 transformation for.
	 * @return the helmert transformation for the default (pivot) transformation path
	 */
	private Transformation getToWGSTransformation(ICRS sourceCRS) {
		Transformation transform = sourceCRS.getGeodeticDatum().getWGS84Conversion();
		if (isIdentity(transform)) {
			if (sourceCRS.getType() != GEOCENTRIC) {
				transform = getTransformation(sourceCRS, GeographicCRS.WGS84);
			}
			else {
				transform = getTransformation(sourceCRS, GeocentricCRS.WGS84);
			}
		}
		return transform;
	}

	/**
	 * Creates a transformation between two projected coordinate systems. This method is
	 * automatically invoked by {@link #createFromCoordinateSystems
	 * createFromCoordinateSystems(...)}. The default implementation can adjust axis order
	 * and orientation. It also performs units conversion if it is the only extra change
	 * needed. Otherwise, it performs three steps:
	 *
	 * <ol>
	 * <li>Unproject <code>sourceCRS</code>.</li>
	 * <li>Transform from <code>sourceCRS.geographicCS</code> to <code>
	 * targetCRS.geographicCS</code>.</li>
	 * <li>Project <code>targetCRS</code>.</li>
	 * </ol>
	 * @param sourceCRS Input coordinate system.
	 * @param targetCRS Output coordinate system.
	 * @return A coordinate transformation from <code>sourceCRS</code> to
	 * <code>targetCRS</code>.
	 * @throws TransformationException if no transformation path has been found.
	 */
	private Transformation createTransformation(final IProjectedCRS sourceCRS, final IProjectedCRS targetCRS)
			throws TransformationException {
		Transformation result = getTransformation(sourceCRS, targetCRS);
		if (isIdentity(result)) {
			LOG.debug("Creating projected -> projected transformation: from (source): " + sourceCRS.getCode()
					+ " to(target): " + targetCRS.getCode());
			if (sourceCRS.getProjection().equals(targetCRS.getProjection())) {
				/*
				 * Swap axis order, and rotate the longitude coordinate if prime meridians
				 * are different.
				 */
				final Matrix matrix = swapAxis(sourceCRS, targetCRS);
				return createMatrixTransform(sourceCRS, targetCRS, matrix);
			}
			final IGeographicCRS sourceGeo = sourceCRS.getGeographicCRS();
			final IGeographicCRS targetGeo = targetCRS.getGeographicCRS();
			final Transformation inverseProjection = createTransformation(sourceCRS, sourceGeo);
			final Transformation geo2geo = createTransformation(sourceGeo, targetGeo);
			final Transformation projection = createTransformation(targetGeo, targetCRS);
			result = concatenate(inverseProjection, geo2geo, projection);
		}
		return result;
	}

	/**
	 * Creates a transformation between a projected and a geocentric coordinate systems.
	 * This method is automatically invoked by {@link #createFromCoordinateSystems
	 * createFromCoordinateSystems(...)}. This method doesn't need to be public since its
	 * decomposition in two step should be general enough.
	 * @param sourceCRS Input projected coordinate system.
	 * @param targetCRS Output coordinate system.
	 * @return A coordinate transformation from <code>sourceCRS</code> to
	 * <code>targetCRS</code>.
	 * @throws TransformationException if no transformation path has been found.
	 */
	private Transformation createTransformation(final IProjectedCRS sourceCRS, final IGeocentricCRS targetCRS)
			throws TransformationException {
		Transformation result = getTransformation(sourceCRS, targetCRS);
		if (isIdentity(result)) {
			LOG.debug("Creating projected -> geocentric transformation: from (source): " + sourceCRS.getCode()
					+ " to(target): " + targetCRS.getCode());
			final IGeographicCRS sourceGCS = sourceCRS.getGeographicCRS();

			final Transformation inverseProjection = createTransformation(sourceCRS, sourceGCS);
			final Transformation geocentric = createTransformation(sourceGCS, targetCRS);
			result = concatenate(inverseProjection, geocentric);
		}
		return result;
	}

	/**
	 * Creates a transformation between a projected and a geographic coordinate systems.
	 * This method is automatically invoked by {@link #createFromCoordinateSystems
	 * createFromCoordinateSystems(...)}. The default implementation returns
	 * <code>{@link #createTransformation(IGeographicCRS, IProjectedCRS)} createTransformation}(targetCRS,
	 * sourceCRS) inverse)</code>.
	 * @param sourceCRS Input coordinate system.
	 * @param targetCRS Output coordinate system.
	 * @return A coordinate transformation from <code>sourceCRS</code> to
	 * <code>targetCRS</code> or <code>null</code> if
	 * {@link ProjectedCRS#getGeographicCRS()}.equals targetCRS.
	 * @throws TransformationException if no transformation path has been found.
	 */
	private Transformation createTransformation(final IProjectedCRS sourceCRS, final IGeographicCRS targetCRS)
			throws TransformationException {
		Transformation result = getTransformation(sourceCRS, targetCRS);
		if (isIdentity(result)) {
			LOG.debug("Creating projected->geographic transformation: from (source): " + sourceCRS.getCode()
					+ " to(target): " + targetCRS.getCode());
			result = createTransformation(targetCRS, sourceCRS);
			if (result != null) {
				result.inverse();
			}
		}
		return result;

	}

	/**
	 * Creates a transformation between two geocentric coordinate systems. This method is
	 * automatically invoked by {@link #createFromCoordinateSystems
	 * createFromCoordinateSystems(...)}. The default implementation can adjust for axis
	 * order and orientation, adjust for prime meridian, performs units conversion and
	 * apply Bursa Wolf transformation if needed.
	 * @param sourceCRS Input coordinate system.
	 * @param targetCRS Output coordinate system.
	 * @return A coordinate transformation from <code>sourceCRS</code> to
	 * <code>targetCRS</code>.
	 * @throws TransformationException if no transformation path has been found.
	 */
	private Transformation createTransformation(final IGeocentricCRS sourceCRS, final IGeocentricCRS targetCRS)
			throws TransformationException {
		Transformation result = getTransformation(sourceCRS, targetCRS);
		if (isIdentity(result)) {
			LOG.debug("Creating geocentric->geocetric transformation: from (source): " + sourceCRS.getCode()
					+ " to(target): " + targetCRS.getCode());

			if (isEllipsoidTransformNeeded(sourceCRS, targetCRS)) {
				final IGeodeticDatum sourceDatum = sourceCRS.getGeodeticDatum();
				final IGeodeticDatum targetDatum = targetCRS.getGeodeticDatum();
				// convert from the source to target ellipsoid through the
				// geocentric coordinate system.
				if (!isIdentity(sourceDatum.getWGS84Conversion()) || !isIdentity(targetDatum.getWGS84Conversion())) {
					LOG.debug("Creating helmert transformation: source(" + sourceCRS.getCode() + ")->target("
							+ targetCRS.getCode() + ").");
					result = transformUsingPivot(sourceCRS, targetCRS, sourceDatum.getWGS84Conversion(),
							targetDatum.getWGS84Conversion());
				}
			}
			if (result == null) {
				// Swap axis order, and rotate the longitude coordinate if prime
				// meridians are different.
				final Matrix matrix = swapAxis(sourceCRS, targetCRS);
				result = createMatrixTransform(sourceCRS, targetCRS, matrix);
				if (LOG.isDebugEnabled()) {
					StringBuilder sb = new StringBuilder(
							"Resulting axis alignment between source geocentric and target geocentric is:");
					if (result == null) {
						sb.append(" not necessary");
					}
					else {
						sb.append("\n").append(((MatrixTransform) result).getMatrix());
					}
					LOG.debug(sb.toString());
				}
			}
		}
		return result;
	}

	private boolean isEllipsoidTransformNeeded(ICRS sourceCRS, ICRS targetCRS) {
		final IGeodeticDatum sourceDatum = sourceCRS.getGeodeticDatum();
		final IGeodeticDatum targetDatum = targetCRS.getGeodeticDatum();
		if (!sourceDatum.equals(targetDatum)) {
			final IEllipsoid sourceEllipsoid = sourceDatum.getEllipsoid();
			final IEllipsoid targetEllipsoid = targetDatum.getEllipsoid();
			// If the two coordinate systems use different ellipsoid, a
			// transformation needs to take place.
			return sourceEllipsoid != null && !sourceEllipsoid.equals(targetEllipsoid);
		}
		return false;
	}

	private Transformation transformUsingPivot(IGeocentricCRS sourceCRS, IGeocentricCRS targetCRS,
			Helmert sourceHelmert, Helmert targetHelmert) throws TransformationException {
		// Transform between different ellipsoids using Bursa Wolf parameters.
		Matrix tmp = swapAxis(sourceCRS, GeocentricCRS.WGS84);
		Matrix4d forwardAxisAlign = null;
		if (tmp != null) {
			forwardAxisAlign = new Matrix4d();
			tmp.get(forwardAxisAlign);
		}
		final Matrix4d forwardToWGS = getWGS84Parameters(sourceHelmert);
		final Matrix4d inverseToWGS = getWGS84Parameters(targetHelmert);
		tmp = swapAxis(GeocentricCRS.WGS84, targetCRS);
		Matrix4d resultMatrix = null;
		if (tmp != null) {
			resultMatrix = new Matrix4d();
			tmp.get(resultMatrix);
		}
		if (forwardAxisAlign == null && forwardToWGS == null && inverseToWGS == null && resultMatrix == null) {
			LOG.debug(
					"The given geocentric crs's do not need a helmert transformation (but they are not equal), returning identity");
			resultMatrix = new Matrix4d();
			resultMatrix.setIdentity();
		}
		else {
			LOG.debug("step1 matrix: \n " + forwardAxisAlign);
			LOG.debug("step2 matrix: \n " + forwardToWGS);
			LOG.debug("step3 matrix: \n " + inverseToWGS);
			LOG.debug("step4 matrix: \n " + resultMatrix);
			if (inverseToWGS != null) {
				inverseToWGS.invert(); // Invert in place.
				LOG.debug("inverseToWGS inverted matrix: \n " + inverseToWGS);
			}
			if (resultMatrix != null) {
				if (inverseToWGS != null) {
					resultMatrix.mul(inverseToWGS); // step4 = step4*step3
					LOG.debug("resultMatrix (after mul with inverseToWGS): \n " + resultMatrix);
				}
				if (forwardToWGS != null) {
					resultMatrix.mul(forwardToWGS); // step4 = step4*step3*step2
					LOG.debug("resultMatrix (after mul with forwardToWGS2): \n " + resultMatrix);
				}
				if (forwardAxisAlign != null) {
					resultMatrix.mul(forwardAxisAlign); // step4 =
					// step4*step3*step2*step1
				}
			}
			else if (inverseToWGS != null) {
				resultMatrix = inverseToWGS;
				if (forwardToWGS != null) {
					resultMatrix.mul(forwardToWGS); // step4 = step3*step2*step1
					LOG.debug("resultMatrix (after mul with forwardToWGS2): \n " + resultMatrix);
				}
				if (forwardAxisAlign != null) {
					resultMatrix.mul(forwardAxisAlign); // step4 =
					// step3*step2*step1
				}
			}
			else if (forwardToWGS != null) {
				resultMatrix = forwardToWGS;
				if (forwardAxisAlign != null) {
					resultMatrix.mul(forwardAxisAlign); // step4 = step2*step1
				}
			}
			else {
				resultMatrix = forwardAxisAlign;
			}
		}

		LOG.debug("The resulting helmert transformation matrix: from( " + sourceCRS.getCode() + ") to("
				+ targetCRS.getCode() + ")\n " + resultMatrix);
		return new MatrixTransform(sourceCRS, targetCRS, resultMatrix, "Helmert-Transformation");

	}

	/**
	 * True if the transformation is null || it's an identity (
	 * {@link Transformation#isIdentity()}.
	 * @param transformation to check for.
	 * @return true if the given transformation is null or an identity.
	 */
	public final static boolean isIdentity(Transformation transformation) {
		return transformation == null || transformation.isIdentity();
	}

	/**
	 * @return the WGS84 parameters as an affine transform, or <code>null</code> if not
	 * available.
	 */
	private Matrix4d getWGS84Parameters(final Helmert transformation) {
		if (!isIdentity(transformation)) {
			return transformation.getAsAffineTransform();
		}
		return null;
	}

	private ICRS resolve(ICRS crs) {
		if (crs instanceof CRSRef) {
			return ((CRSRef) crs).getReferencedObject();
		}
		return crs;
	}

}
