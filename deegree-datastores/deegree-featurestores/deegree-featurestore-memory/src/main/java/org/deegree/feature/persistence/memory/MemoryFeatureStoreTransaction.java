/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2013 by:
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
package org.deegree.feature.persistence.memory;

import static org.deegree.feature.i18n.Messages.getMessage;
import static org.deegree.protocol.wfs.transaction.action.IDGenMode.USE_EXISTING;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.Reference;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.genericxml.GenericXMLElement;
import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.lock.Lock;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.persistence.transaction.FeatureUpdater;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.xpath.TypedObjectNodeXPathEvaluator;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.IdFilter;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.ResourceId;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.linearization.GeometryLinearizer;
import org.deegree.geometry.linearization.LinearizationCriterion;
import org.deegree.geometry.linearization.NumPointsCriterion;
import org.deegree.geometry.primitive.Point;
import org.deegree.gml.utils.GMLObjectVisitor;
import org.deegree.gml.utils.GMLObjectWalker;
import org.deegree.protocol.wfs.transaction.action.IDGenMode;
import org.deegree.protocol.wfs.transaction.action.ParsedPropertyReplacement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FeatureStoreTransaction} implementation used by the {@link MemoryFeatureStore}.
 *
 * @see MemoryFeatureStore
 * @see StoredFeatures
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
class MemoryFeatureStoreTransaction implements FeatureStoreTransaction {

	private static final Logger LOG = LoggerFactory.getLogger(MemoryFeatureStoreTransaction.class);

	private final GeometryLinearizer linearizer = new GeometryLinearizer();

	private final LinearizationCriterion crit = new NumPointsCriterion(20);

	private final MemoryFeatureStore fs;

	private final StoredFeatures sf;

	private final LockManager lockManager;

	/**
	 * Creates a new {@link MemoryFeatureStoreTransaction} instance.
	 * @param fs invoking feature store instance, must not be <code>null</code>
	 * @param sf copy of features to work on, must not be <code>null</code>
	 * @param lockManager lock manager, must not be <code>null</code>
	 */
	MemoryFeatureStoreTransaction(MemoryFeatureStore fs, StoredFeatures sf, LockManager lockManager) {
		this.fs = fs;
		this.sf = sf;
		this.lockManager = lockManager;
	}

	@Override
	public void commit() throws FeatureStoreException {
		try {
			sf.rebuildIndexes();
		}
		catch (UnknownCRSException e) {
			throw new FeatureStoreException(e.getMessage());
		}
		fs.releaseTransaction(this, sf);
	}

	@Override
	public FeatureStore getStore() {
		return fs;
	}

	@Override
	public int performDelete(QName ftName, OperatorFilter filter, Lock lock) throws FeatureStoreException {

		String lockId = lock != null ? lock.getId() : null;
		FeatureType ft = fs.getSchema().getFeatureType(ftName);
		if (ft == null) {
			throw new FeatureStoreException(getMessage("TA_OPERATION_FT_NOT_SERVED", ftName));
		}

		FeatureCollection fc = sf.getFeatures(ft);
		int deleted = 0;
		if (fc != null) {
			try {
				TypedObjectNodeXPathEvaluator evaluator = new TypedObjectNodeXPathEvaluator();
				FeatureCollection delete = fc.getMembers(filter, evaluator);

				// check if all can be deleted
				for (Feature feature : delete) {
					if (!lockManager.isFeatureModifiable(feature.getId(), lockId)) {
						if (lockId == null) {
							throw new MissingParameterException(
									getMessage("TA_DELETE_LOCKED_NO_LOCK_ID", feature.getId()), "lockId");
						}
						throw new InvalidParameterValueException(
								getMessage("TA_DELETE_LOCKED_WRONG_LOCK_ID", feature.getId()), "lockId");
					}
				}

				deleted = delete.size();
				for (Feature feature : delete) {
					sf.removeFeature(feature);
					if (lock != null) {
						lock.release(feature.getId());
					}
				}
			}
			catch (FilterEvaluationException e) {
				throw new FeatureStoreException(e.getMessage(), e);
			}
		}
		return deleted;
	}

	@Override
	public int performDelete(IdFilter filter, Lock lock) throws FeatureStoreException {

		String lockId = lock != null ? lock.getId() : null;

		// check if all features can be deleted
		for (ResourceId id : filter.getSelectedIds()) {
			if (!lockManager.isFeatureModifiable(id.getRid(), lockId)) {
				if (lockId == null) {
					throw new MissingParameterException(getMessage("TA_DELETE_LOCKED_NO_LOCK_ID", id.getRid()),
							"lockId");
				}
				throw new InvalidParameterValueException(getMessage("TA_DELETE_LOCKED_WRONG_LOCK_ID", id.getRid()),
						"lockId");
			}
		}

		int deleted = 0;
		for (ResourceId id : filter.getSelectedIds()) {
			GMLObject obj = sf.getObjectById(id.getRid());
			if (obj != null) {
				if (obj instanceof Feature) {
					Feature f = (Feature) obj;
					sf.removeFeature(f);
					deleted++;
				}
			}
			if (lock != null) {
				lock.release(id.getRid());
			}
		}
		return deleted;
	}

	@Override
	public List<String> performInsert(FeatureCollection fc, IDGenMode mode) throws FeatureStoreException {

		long begin = System.currentTimeMillis();
		if (fs.getStorageCrs() != null) {
			LOG.debug("Transforming incoming feature collection to '" + fs.getStorageCrs() + "'");
			try {
				fc = transformGeometries(fc);
			}
			catch (Exception e) {
				e.printStackTrace();
				String msg = "Unable to transform geometries: " + e.getMessage();
				throw new FeatureStoreException(msg);
			}
		}
		else {
			LOG.debug("Checking CRS use in feature collection");
			checkCRS(fc);
		}
		long elapsed = System.currentTimeMillis() - begin;
		LOG.debug("Transforming / checking geometries took {} [ms]", elapsed);

		begin = System.currentTimeMillis();
		List<Feature> features = assignIds(fc, mode);
		elapsed = System.currentTimeMillis() - begin;
		LOG.debug("Assigning ids / finding features and geometries took {} [ms]", elapsed);

		begin = System.currentTimeMillis();
		for (Feature feature : features) {
			sf.addFeature(feature);
		}
		elapsed = System.currentTimeMillis() - begin;
		LOG.debug("Adding of features took {} [ms]", elapsed);

		List<String> fids = new ArrayList<String>(features.size());
		for (Feature f : features) {
			fids.add(f.getId());
		}
		return new ArrayList<String>(fids);
	}

	private void checkCRS(FeatureCollection fc) throws FeatureStoreException {
		GMLObjectVisitor visitor = new GMLObjectVisitor() {
			@Override
			public boolean visitGeometry(Geometry geom) {
				if (geom.getCoordinateSystem() != null && geom.getCoordinateDimension() != 1) {
					try {
						geom.getCoordinateSystem();
					}
					catch (Exception e) {
						throw new IllegalArgumentException(e.getMessage());
					}
				}
				return true;
			}

			@Override
			public boolean visitFeature(Feature feature) {
				return true;
			}

			@Override
			public boolean visitObject(GMLObject o) {
				return true;
			}

			@Override
			public boolean visitReference(Reference<?> ref) {
				return true;
			}
		};
		try {
			new GMLObjectWalker(visitor).traverse(fc);
		}
		catch (IllegalArgumentException e) {
			throw new FeatureStoreException(e.getMessage());
		}
	}

	/**
	 * Assigns an id to every {@link Feature} / {@link Geometry} in the given collection.
	 * @param fc feature collection, must not be <code>null</code>
	 * @param mode id generation mode, must not be <code>null</code>
	 * @return list of all features (including nested features) from the input collection,
	 * with ids
	 */
	private List<Feature> assignIds(final FeatureCollection fc, final IDGenMode mode) throws FeatureStoreException {
		final List<Feature> features = new ArrayList<Feature>(fc.size());
		GMLObjectVisitor visitor = new GMLObjectVisitor() {
			@SuppressWarnings("synthetic-access")
			@Override
			public boolean visitGeometry(Geometry geom) {
				String id = getGeometryId(geom, mode);
				if (sf.getObjectById(id) != null) {
					String msg = "Cannot insert geometry '" + id
							+ "'. This geometry already exists in the feature store.";
					throw new IllegalArgumentException(msg);
				}
				geom.setId(id);
				return true;
			}

			@SuppressWarnings("synthetic-access")
			@Override
			public boolean visitFeature(Feature feature) {
				String id = getFeatureId(feature, mode);
				if (sf.getObjectById(id) != null) {
					String msg = "Cannot insert feature '" + id
							+ "'. This feature already exists in the feature store.";
					throw new IllegalArgumentException(msg);
				}
				if (feature != fc) {
					feature.setId(id);
					features.add(feature);
				}
				return true;
			}

			@Override
			public boolean visitReference(Reference<?> ref) {
				return true;
			}

			@Override
			public boolean visitObject(GMLObject o) {
				return true;
			}
		};
		try {
			new GMLObjectWalker(visitor).traverse(fc);
		}
		catch (IllegalArgumentException e) {
			throw new FeatureStoreException(e.getMessage());
		}
		fixReferences(fc);
		return features;
	}

	private void fixReferences(final FeatureCollection fc) {
		GMLObjectVisitor visitor = new GMLObjectVisitor() {
			@Override
			public boolean visitGeometry(Geometry geom) {
				return true;
			}

			@Override
			public boolean visitFeature(Feature feature) {
				return true;
			}

			@Override
			public boolean visitReference(Reference<?> ref) {
				fixReference(ref);
				return true;
			}

			@Override
			public boolean visitObject(GMLObject o) {
				return true;
			}
		};
		new GMLObjectWalker(visitor).traverse(fc);
	}

	private void fixReference(Reference<?> ref) {
		if (ref.isResolved()) {
			ref.setURI("#" + ref.getId());
		}
	}

	private String getFeatureId(Feature feature, IDGenMode mode) {
		String fid = feature.getId();
		switch (mode) {
			case GENERATE_NEW: {
				fid = "FEATURE_" + generateNewId();
				break;
			}
			case REPLACE_DUPLICATE: {
				if (fid == null || sf.getObjectById(fid) != null) {
					fid = "FEATURE_" + generateNewId();
				}
			}
			case USE_EXISTING: {
				if (fid == null) {
					fid = "FEATURE_" + generateNewId();
				}
				break;
			}
		}
		return fid;
	}

	private String getGeometryId(Geometry geometry, IDGenMode mode) {
		String gid = geometry.getId();
		switch (mode) {
			case GENERATE_NEW: {
				gid = "GEOMETRY_" + generateNewId();
				break;
			}
			case REPLACE_DUPLICATE: {
				if (gid == null || sf.getObjectById(gid) != null) {
					gid = "FEATURE_" + generateNewId();
				}
			}
			case USE_EXISTING: {
				if (gid == null) {
					gid = "GEOMETRY_" + generateNewId();
				}
				break;
			}
		}
		return gid;
	}

	private FeatureCollection transformGeometries(FeatureCollection fc)
			throws IllegalArgumentException, UnknownCRSException, TransformationException {

		FeatureCollection transformedFc = new GenericFeatureCollection();
		GeometryTransformer transformer = new GeometryTransformer(fs.getStorageCrs());
		for (Feature feature : fc) {
			transformedFc.add(transformGeometries(feature, transformer));
		}
		return transformedFc;
	}

	private Feature transformGeometries(Feature feature, GeometryTransformer transformer)
			throws IllegalArgumentException, TransformationException, UnknownCRSException {

		// TODO Do not modify the incoming feature, but create a new one.
		for (Property prop : feature.getProperties()) {
			List<TypedObjectNode> children = prop.getChildren();
			if (children != null && !children.isEmpty()) {
				List<TypedObjectNode> newChildren = new ArrayList<TypedObjectNode>(children.size());
				for (TypedObjectNode child : children) {
					newChildren.add(transformGeometries(child, transformer));
				}
				prop.setChildren(newChildren);
			}
			else {
				TypedObjectNode value = prop.getValue();
				if (value != null) {
					prop.setValue(transformGeometries(value, transformer));
				}
			}
		}
		feature.setEnvelope(feature.calcEnvelope());
		return feature;
	}

	private TypedObjectNode transformGeometries(TypedObjectNode value, GeometryTransformer transformer)
			throws IllegalArgumentException, TransformationException, UnknownCRSException {
		if (value == null) {
			// nothing to do
		}
		else if (value instanceof GenericXMLElement) {
			GenericXMLElement generic = (GenericXMLElement) value;
			List<TypedObjectNode> newChildren = new ArrayList<TypedObjectNode>(generic.getChildren().size());
			for (int i = 0; i < generic.getChildren().size(); i++) {
				TypedObjectNode child = generic.getChildren().get(i);
				TypedObjectNode transformed = transformGeometries(child, transformer);
				newChildren.add(transformed);
			}
			generic.setChildren(newChildren);
		}
		else if (value instanceof Property) {
			Property generic = (Property) value;
			List<TypedObjectNode> newChildren = new ArrayList<TypedObjectNode>(generic.getChildren().size());
			for (int i = 0; i < generic.getChildren().size(); i++) {
				TypedObjectNode child = generic.getChildren().get(i);
				TypedObjectNode transformed = transformGeometries(child, transformer);
				newChildren.add(transformed);
			}
			generic.setChildren(newChildren);
		}
		else if (value instanceof Geometry) {
			value = transformGeometry((Geometry) value, transformer);
		}
		else if (value instanceof PrimitiveValue) {
			// nothing to do
		}
		return value;
	}

	private Geometry transformGeometry(Geometry value, GeometryTransformer transformer)
			throws IllegalArgumentException, TransformationException {

		Geometry transformed = value;
		if (transformed.getCoordinateSystem() == null) {
			transformed.setCoordinateSystem(transformer.getTargetCRS());
		}
		else {
			transformed = linearizer.linearize(value, crit);
			if (!(transformed instanceof Point && transformed.getCoordinateDimension() == 1)) {
				transformed = transformer.transform(transformed, transformed.getCoordinateSystem());
			}
		}
		return transformed;
	}

	private String generateNewId() {
		return UUID.randomUUID().toString();
	}

	@Override
	public List<String> performUpdate(QName ftName, List<ParsedPropertyReplacement> replacementProps, Filter filter,
			Lock lock) throws FeatureStoreException {

		String lockId = lock != null ? lock.getId() : null;

		FeatureType ft = fs.getSchema().getFeatureType(ftName);
		if (ft == null) {
			throw new FeatureStoreException(getMessage("TA_OPERATION_FT_NOT_SERVED", ftName));
		}

		FeatureCollection fc = sf.getFeatures(ft);
		List<String> updatedFids = new ArrayList<String>();
		if (fc != null) {
			try {
				TypedObjectNodeXPathEvaluator evaluator = new TypedObjectNodeXPathEvaluator();
				FeatureCollection update = fc.getMembers(filter, evaluator);

				// check if all features can be updated
				for (Feature feature : update) {
					if (!lockManager.isFeatureModifiable(feature.getId(), lockId)) {
						if (lockId == null) {
							throw new MissingParameterException(
									getMessage("TA_UPDATE_LOCKED_NO_LOCK_ID", feature.getId()), "lockId");
						}
						throw new InvalidParameterValueException(
								getMessage("TA_UPDATE_LOCKED_WRONG_LOCK_ID", feature.getId()), "lockId");
					}
				}

				for (Feature feature : update) {
					updatedFids.add(feature.getId());
					new FeatureUpdater().update(feature, replacementProps);
					if (lock != null) {
						lock.release(feature.getId());
					}
				}
			}
			catch (FilterEvaluationException e) {
				throw new FeatureStoreException(e.getMessage(), e);
			}
		}
		return updatedFids;
	}

	@Override
	public String performReplace(Feature replacement, Filter filter, Lock lock, IDGenMode idGenMode)
			throws FeatureStoreException {
		if (filter instanceof IdFilter) {
			performDelete((IdFilter) filter, lock);
		}
		else {
			performDelete(replacement.getName(), (OperatorFilter) filter, lock);
		}
		GenericFeatureCollection col = new GenericFeatureCollection();
		col.add(replacement);
		List<String> ids = performInsert(col, USE_EXISTING);
		if (ids.isEmpty() || ids.size() > 1) {
			throw new FeatureStoreException("Unable to determine new feature id.");
		}
		return ids.get(0);
	}

	@Override
	public void rollback() throws FeatureStoreException {
		fs.releaseTransaction(this, null);
	}

}
