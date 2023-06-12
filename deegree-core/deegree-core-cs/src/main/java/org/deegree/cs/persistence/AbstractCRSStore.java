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
 http://ICompoundCRS.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.cs.persistence;

import static org.deegree.cs.coordinatesystems.CRS.CRSType.COMPOUND;
import static org.deegree.cs.coordinatesystems.CRS.CRSType.PROJECTED;

import java.util.HashMap;
import java.util.Map;

import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.CRSResource;
import org.deegree.cs.components.Axis;
import org.deegree.cs.components.IAxis;
import org.deegree.cs.coordinatesystems.CRS;
import org.deegree.cs.coordinatesystems.CompoundCRS;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.coordinatesystems.ICompoundCRS;
import org.deegree.cs.coordinatesystems.IProjectedCRS;
import org.deegree.cs.coordinatesystems.ProjectedCRS;
import org.deegree.cs.exceptions.CRSConfigurationException;
import org.deegree.cs.refs.coordinatesystem.CRSRef;
import org.deegree.cs.transformations.TransformationFactory.DSTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base class for a {@link CRSStore} which has a caching mechanism for
 * {@link CRSIdentifiable}s and instantiates a given resolver used for inverse lookup.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @param <T> the type of object the parse method awaits.
 *
 */
public abstract class AbstractCRSStore implements CRSStore {

	public enum RESOURCETYPE {

		CRS, ELLIPSOID, PM, DATUM, PROJECTION, TRANSFORMATION

	}

	private static Logger LOG = LoggerFactory.getLogger(AbstractCRSStore.class);

	private Map<CRSCodeType, CRSResource> cachedIdentifiables = new HashMap<CRSCodeType, CRSResource>();

	private Map<CRSCodeType, CRSResource> cachedCRSXY = new HashMap<CRSCodeType, CRSResource>();

	private DSTransform prefTransformType = DSTransform.HELMERT;

	/**
	 * @param prefTransformType the preferred {@link DSTransform} type how to transform
	 * from this {@link CRSStore}
	 */
	public AbstractCRSStore(DSTransform prefTransformType) {
		this.prefTransformType = prefTransformType;
	}

	/**
	 * Retrieves the {@link ICRS} from the set provider that is identified by the given
	 * {@link CRSCodeType} id.
	 * @param id the {@link CRSCodeType} of the wanted crs
	 * @return the {@link ICRS} that corresponds to the id
	 * @throws CRSConfigurationException
	 */
	public ICRS getCRSByCode(CRSCodeType id) throws CRSConfigurationException {
		return getCRSByCode(id, false);
	}

	@Override
	public ICRS getCRSByCode(CRSCodeType id, boolean forceXY) throws CRSConfigurationException {
		ICRS result = null;
		if (id != null) {
			if (forceXY) {
				result = getCRSFromCache(cachedCRSXY, id, result);
			}
			if (result == null) {
				result = getCRSFromCache(cachedIdentifiables, id, result);
				if (result == null) {
					LOG.debug("No crs with id: " + id + " found in cache.");
					result = getCoordinateSystem(id.getOriginal());
				}
				if (forceXY && result != null) {
					result = createXYCoordinateSystem(result);
				}
			}
		}
		if (result == null) {
			LOG.debug("The id: " + id
					+ " could not be mapped to a valid deegree-crs, currently projectedCRS, geographicCRS, compoundCRS and geocentricCRS are supported.");
		}
		else {
			/**
			 * Adding the used underlying crs's to the cache.
			 */
			if (forceXY) {
				addIdToCache(cachedCRSXY, result, false);
				if (result.getType() == COMPOUND) {
					addIdToCache(cachedCRSXY, ((ICompoundCRS) result).getUnderlyingCRS(), false);
					if (((ICompoundCRS) result).getUnderlyingCRS().getType() == PROJECTED) {
						ICRS underlying = resolve(((ICompoundCRS) result).getUnderlyingCRS());
						addIdToCache(cachedCRSXY, ((ProjectedCRS) underlying).getGeographicCRS(), false);
					}
				}
				else if (result.getType() == PROJECTED) {
					addIdToCache(((IProjectedCRS) result).getGeographicCRS(), false);
				}
			}
			else {
				addIdToCache(result, false);
				if (result.getType() == COMPOUND) {
					addIdToCache(((ICompoundCRS) result).getUnderlyingCRS(), false);
					if (((ICompoundCRS) result).getUnderlyingCRS().getType() == PROJECTED) {
						ICRS underlying = resolve(((ICompoundCRS) result).getUnderlyingCRS());
						addIdToCache(((ProjectedCRS) underlying).getGeographicCRS(), false);
					}
				}
				else if (result.getType() == PROJECTED) {
					addIdToCache(((IProjectedCRS) result).getGeographicCRS(), false);
				}
			}
		}
		return result;
	}

	private ICRS createXYCoordinateSystem(ICRS result) {
		switch (result.getType()) {
			case GEOGRAPHIC:
				return new GeographicCRS(((GeographicCRS) result).getGeodeticDatum(),
						forceXYAxisOrder(result.getAxis()), new CRSIdentifiable(result));
			case COMPOUND:
				CompoundCRS comp = (CompoundCRS) result;
				return new CompoundCRS(comp.getHeightAxis(), createXYCoordinateSystem(comp.getUnderlyingCRS()),
						comp.getDefaultHeight(), new CRSIdentifiable(comp));
		}
		return result;
	}

	private IAxis[] forceXYAxisOrder(IAxis[] axis) {
		if (axis != null && axis.length == 2
				&& (axis[0].getOrientation() == Axis.AO_NORTH || axis[0].getOrientation() == Axis.AO_SOUTH)) {
			IAxis[] xyAxis = new IAxis[2];
			xyAxis[0] = axis[1];
			xyAxis[1] = axis[0];
			return xyAxis;
		}
		return axis;
	}

	private ICRS getCRSFromCache(Map<CRSCodeType, CRSResource> cache, CRSCodeType id, ICRS result) {
		LOG.debug("Trying to load crs with id: " + id + " from cache.");
		if (LOG.isDebugEnabled()) {
			LOG.debug(cachedIdentifiables.keySet().toString());
		}
		if (cache.containsKey(id)) {
			CRSResource r = cache.get(id);
			LOG.debug("Found CRSIdentifiable: " + r.getCodeAndName() + " from given id: " + id);
			if (!(r instanceof ICRS)) {
				LOG.error("Found CRSIdentifiable: " + r.getCodeAndName()
						+ " but it is not a coordinate system, your db is inconsistent return null.");
				r = null;
			}
			result = (CRS) r;
		}
		return result;
	}

	/**
	 * @param crsDefinition containing the definition of a crs in the understood type.
	 * @return a {@link ICoordinateSystem} instance initialized with values from the given
	 * type definition fragment or <code>null</code> if the given crsDefinition is
	 * <code>null</code> or not known.
	 * @throws CRSConfigurationException if an error was found in the given crsDefintion
	 */
	// protected abstract ICoordinateSystem parseCoordinateSystem( T crsDefinition )
	// throws CRSConfigurationException;

	/**
	 * @param transformationDefinition containing the parameters needed to build a
	 * Transformation.
	 * @return a {@link Transformation} instance initialized with values from the given
	 * definition or <code>null</code> if the given transformationDefintion is
	 * <code>null</code>. If the parsed transformation is not supported or a
	 * {@link NotSupportedTransformation} will be returned.
	 * @throws CRSConfigurationException if an error was found in the given crsDefintion
	 */
	// public abstract Transformation parseTransformation( T transformationDefinition )
	// throws CRSConfigurationException;

	/**
	 * Clears the cache.
	 */
	public void clearCache() {
		try {
			synchronized (cachedIdentifiables) {
				cachedIdentifiables.clear();
				cachedIdentifiables.notifyAll();
			}
		}
		catch (Exception e) {
			LOG.warn("The clearing of the cache could not be forefullfilled because: " + e.getLocalizedMessage());
		}
	}

	/**
	 * The id are what they are, not trimming 'upcasing' or other modifications will be
	 * done in this method.
	 * @param expectedType The class of type T which is expected.
	 * @param <V> the type to cast to if the casting fails, null will be returned.
	 * @param ids to search the cache for
	 * @return the {@link CRSIdentifiable} of the first matching id or <code>null</code>
	 * if it was not found.
	 */
	public <V extends CRSResource> V getCachedIdentifiable(Class<V> expectedType, CRSResource ids) {
		if (ids == null) {
			return null;
		}
		return getCachedIdentifiable(expectedType, ids.getCodes());
	}

	/**
	 * The id are what they are, not trimming 'upcasing' or other modifications will be
	 * done in this method.
	 * @param expectedType The class of type T which is expected.
	 * @param <V> the type to cast to if the casting fails, null will be returned.
	 * @param ids to search the cache for
	 * @return the {@link CRSIdentifiable} of the first matching id or <code>null</code>
	 * if it was not found.
	 */
	public <V extends CRSResource> V getCachedIdentifiable(Class<V> expectedType, String[] ids) {
		if (ids == null || ids.length == 0) {
			return null;
		}
		V result = null;
		for (int i = 0; i < ids.length && result == null; i++) {
			result = getCachedIdentifiable(expectedType, ids[i]);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Searched for id: " + ids[i] + " resulted in: "
						+ ((result == null) ? "null" : result.getCode()));
			}
		}
		return result;
	}

	/**
	 * Get a {@link CRSIdentifiable} (actually a type V that extends it) from the cache
	 * that corresponds to the a {@link CRSCodeType}. An array of code types is given; the
	 * first identifiable that is found in (for a code, when they are checked in order) is
	 * returned
	 * @param <V>
	 * @param expectedType the type of the sought object
	 * @param ids an array of {@link CRSCodeType}s
	 * @return the identifiable found in the cache corresponding to the (first) id
	 */
	public <V extends CRSResource> V getCachedIdentifiable(Class<V> expectedType, CRSCodeType[] ids) {
		if (ids == null || ids.length == 0) {
			return null;
		}
		V result = null;
		for (int i = 0; i < ids.length && result == null; i++) {
			result = getCachedIdentifiable(expectedType, ids[i]);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Searched for id: " + ids[i] + " resulted in: "
						+ ((result == null) ? "null" : result.getCode()));
			}
		}
		return result;
	}

	/**
	 * The id is as it is, not trimming 'upcasing' or other modifications will be done in
	 * this method.
	 * @param expectedType The class of type T which is expected.
	 * @param <V> the type to cast to if the casting fails, null will be returned.
	 * @param id to search the cache for
	 * @return the {@link CRSIdentifiable} or <code>null</code> if it was not found or the
	 * wrong type was found.
	 */
	@SuppressWarnings("unchecked")
	public <V extends CRSResource> V getCachedIdentifiable(Class<V> expectedType, String id) {
		if (id == null) {
			return null;
		}
		V result = null;
		try {
			result = (V) cachedIdentifiables.get(CRSCodeType.valueOf(id));
		}
		catch (ClassCastException cce) {
			LOG.error("Given id is not of type: " + expectedType.getCanonicalName() + " found following error: "
					+ cce.getLocalizedMessage());
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Searched for id: " + id + " resulted in: " + ((result == null) ? "null" : result.getCode()));
		}
		return result;
	}

	/**
	 * Get a {@link CRSIdentifiable} (actually a type V that extends it) from the cache
	 * that corresponds to the a {@link CRSCodeType}.
	 * @param <V>
	 * @param expectedType the type of the sought object
	 * @param id a {@link CRSCodeType}
	 * @return the identifiable found in the cache corresponding to the id
	 */
	@SuppressWarnings("unchecked")
	private <V extends CRSResource> V getCachedIdentifiable(Class<V> expectedType, CRSCodeType id) {
		if (id == null) {
			return null;
		}
		V result = null;
		try {
			result = (V) cachedIdentifiables.get(id);
		}
		catch (ClassCastException cce) {
			LOG.error("Given id is not of type: " + expectedType.getCanonicalName() + " found following error: "
					+ cce.getLocalizedMessage());
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Searched for id: " + id + " resulted in: " + ((result == null) ? "null" : result.getCode()));
		}
		return result;
	}

	/**
	 * The id is as it is, not trimming 'upcasing' or other modifications will be done in
	 * this method.
	 * @param <V> the type to cast to if the casting fails, null will be returned.
	 * @param id to search the cache for
	 * @return the {@link CRSIdentifiable} or <code>null</code> if it was not found or the
	 * wrong type was found.
	 */
	@SuppressWarnings("unchecked")
	public <V extends CRSResource> V getCachedIdentifiable(String id) {
		if (id == null) {
			return null;
		}
		V result = (V) cachedIdentifiables.get(CRSCodeType.valueOf(id));
		if (LOG.isDebugEnabled()) {
			LOG.debug("Searched for id: " + id + " resulted in: " + ((result == null) ? "null" : result.getCode()));
		}
		return result;
	}

	/**
	 * Get a {@link CRSIdentifiable} (actually a type V that extends it) from the cache
	 * that corresponds to the a {@link CRSCodeType}.
	 * @param <V>
	 * @param id a {@link CRSCodeType}
	 * @return a {@link CRSIdentifiable}-extending object that corresponds to the given id
	 */
	@SuppressWarnings("unchecked")
	public <V extends CRSResource> V getCachedIdentifiable(CRSCodeType id) {
		if (id == null) {
			return null;
		}
		V result = (V) cachedIdentifiables.get(id);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Searched for id: " + id + " resulted in: " + ((result == null) ? "null" : result.getCode()));
		}
		return result;
	}

	/**
	 * Add the id to the cache, by mapping it to all its identifiers.
	 * @param <V> type of CRSIdentifiable
	 * @param resource to insert into cache
	 * @param update if true an existing identifiable in the cache will be overwritten.
	 * @return the identifiable
	 */
	public synchronized <V extends CRSResource> V addIdToCache(V resource, boolean update) {
		return addIdToCache(cachedIdentifiables, resource, update);
	}

	private synchronized <V extends CRSResource> V addIdToCache(Map<CRSCodeType, CRSResource> cache, V identifiable,
			boolean update) {
		if (identifiable == null) {
			return null;
		}
		for (CRSCodeType idString : identifiable.getCodes()) {
			// if ( idString != null && !"".equals( idString.trim() ) ) {
			if (idString != null) {
				if (cache.containsKey(idString) && cache.get(idString) != null) {
					if (update) {
						LOG.debug("Updating cache with new identifiable: " + idString);
						cache.put(idString, identifiable);
					}
				}
				else {
					LOG.debug("Adding new identifiable to cache: " + idString);
					cache.put(idString, identifiable);
				}
			}
			else {
				LOG.debug("Not adding the null string id to the cache of identifiable: " + identifiable.getCode());
			}
		}
		return identifiable;
	}

	@Override
	public DSTransform getPreferedTransformationType() {
		return prefTransformType;
	}

	// @Override
	// public ICRS getCRSByName( String id, boolean forceXY )
	// throws UnknownCRSException {
	// ICRS cachedIdentifiable = getCachedIdentifiable( ICRS.class, id );
	// if ( cachedIdentifiable != null )
	// return cachedIdentifiable;
	//
	// return getCoordinateSystem( id );
	// }

	/**
	 * @param id
	 * @return
	 */
	public abstract ICRS getCoordinateSystem(String id);

	protected ICRS resolve(ICRS crs) {
		if (crs instanceof CRSRef) {
			return ((CRSRef) crs).getReferencedObject();
		}
		return crs;
	}

}
