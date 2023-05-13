package org.deegree.cs.transformations;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;

import org.deegree.cs.CoordinateTransformer;
import org.deegree.cs.components.IAxis;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TransformationAccuracy {

	private static Logger LOG = LoggerFactory.getLogger(TransformationAccuracy.class);

	/**
	 * Creates a {@link CoordinateTransformer} for the given coordinate system.
	 * @param targetCrs to which incoming coordinates will be transformed.
	 * @return the transformer which is able to transform coordinates to the given crs..
	 */
	private CoordinateTransformer getGeotransformer(ICRS targetCrs) {
		assertNotNull(targetCrs);
		return new CoordinateTransformer(targetCrs);
	}

	/**
	 * Creates an epsilon string with following layout axis.getName: origPoint -
	 * resultPoint = epsilon Unit.getName().
	 * @param sourceCoordinate on the given axis
	 * @param targetCoordinate on the given axis
	 * @param allowedEpsilon defined by test.
	 * @param axis of the coordinates
	 * @return a String representation.
	 */
	private String createEpsilonString(boolean failure, double sourceCoordinate, double targetCoordinate,
			double allowedEpsilon, IAxis axis) {
		double epsilon = sourceCoordinate - targetCoordinate;
		StringBuilder sb = new StringBuilder(400);
		sb.append(axis.getName()).append(" (result - orig = error [allowedError]): ");
		sb.append(sourceCoordinate).append(" - ").append(targetCoordinate);
		sb.append(" = ").append(epsilon).append(axis.getUnits());
		sb.append(" [").append(allowedEpsilon).append(axis.getUnits()).append("]");
		if (failure) {
			sb.append(" [FAILURE]");
		}
		return sb.toString();
	}

	/**
	 * Transforms the given coordinates in the sourceCRS to the given targetCRS and checks
	 * if they lie within the given epsilon range to the reference point. If successful
	 * the transformed will be logged.
	 * @param sourcePoint to transform
	 * @param targetPoint to which the result shall be checked.
	 * @param epsilons for each axis
	 * @param sourceCRS of the origPoint
	 * @param targetCRS of the targetPoint.
	 * @return the string containing the success string.
	 * @throws TransformationException
	 * @throws AssertionError if one of the axis of the transformed point do not lie
	 * within the given epsilon range.
	 */
	String doAccuracyTest(Point3d sourcePoint, Point3d targetPoint, Point3d epsilons, ICRS sourceCRS, ICRS targetCRS)
			throws TransformationException {
		assertNotNull(sourceCRS);
		assertNotNull(targetCRS);
		assertNotNull(sourcePoint);
		assertNotNull(targetPoint);
		assertNotNull(epsilons);

		CoordinateTransformer transformer = getGeotransformer(targetCRS);

		List<Point3d> tmp = new ArrayList<Point3d>(1);
		tmp.add(new Point3d(sourcePoint));
		Point3d result = transformer.transform(sourceCRS, tmp).get(0);
		assertNotNull(result);
		boolean xFail = Math.abs(result.x - targetPoint.x) > epsilons.x;
		String xString = createEpsilonString(xFail, result.x, targetPoint.x, epsilons.x, targetCRS.getAxis()[0]);
		boolean yFail = Math.abs(result.y - targetPoint.y) > epsilons.y;
		String yString = createEpsilonString(yFail, result.y, targetPoint.y, epsilons.y, targetCRS.getAxis()[1]);

		// Z-Axis if available
		boolean zFail = false;
		String zString = null;
		if (targetCRS.getDimension() == 3) {
			zFail = Math.abs(result.z - targetPoint.z) > epsilons.z;
			zString = createEpsilonString(zFail, result.z, targetPoint.z, epsilons.z, targetCRS.getAxis()[2]);
		}
		else if (targetCRS.getDimension() == 2 && sourceCRS.getDimension() == 2 && !Double.isNaN(sourcePoint.z)) {
			// 3rd coordinate should be passed
			double epsilon = result.z - targetPoint.z;
			zFail = Math.abs(epsilon) > 0;
			StringBuilder sb = new StringBuilder(400);
			sb.append("passed z (result - orig = error [allowedError]): ");
			sb.append(result.z).append(" - ").append(targetPoint.z);
			sb.append(" = ").append(epsilon);
			sb.append(" [").append(0).append("]");
			if (zFail) {
				sb.append(" [FAILURE]");
			}
			zString = sb.toString();
		}

		StringBuilder sb = new StringBuilder();
		if (xFail || yFail || zFail) {
			sb.append("[FAILED] ");
		}
		else {
			sb.append("[SUCCESS] ");
		}
		sb.append("Transformation (").append(sourceCRS.getCode().toString());
		sb.append(" -> ").append(targetCRS.getCode().toString()).append(")\n");
		sb.append(xString);
		sb.append("\n").append(yString);
		if (zString != null) {
			sb.append("\n").append(zString);
		}
		if (xFail || yFail || zFail) {
			throw new AssertionError(sb.toString());
		}
		return sb.toString();
	}

	/**
	 * Do an forward and inverse accuracy test.
	 * @param sourceCRS
	 * @param targetCRS
	 * @param source
	 * @param target
	 * @param forwardEpsilon
	 * @param inverseEpsilon
	 * @throws TransformationException
	 */
	protected void doForwardAndInverse(ICRS sourceCRS, ICRS targetCRS, Point3d source, Point3d target,
			Point3d forwardEpsilon, Point3d inverseEpsilon) throws TransformationException {
		StringBuilder output = new StringBuilder();
		output.append("Transforming forward/inverse -> projected with id: '");
		output.append(sourceCRS.getCode().toString());
		output.append("' and projected with id: '");
		output.append(targetCRS.getCode().toString());
		output.append("'.\n");

		boolean forwardSuccess = doForwardTransformation(sourceCRS, targetCRS, source, target, forwardEpsilon, output);
		boolean inverseSuccess = doInverseTransformation(sourceCRS, targetCRS, source, target, inverseEpsilon, output);

		LOG.debug(output.toString());
		assertEquals(true, forwardSuccess);
		assertEquals(true, inverseSuccess);

	}

	/**
	 * Do an forward and inverse accuracy test.
	 * @param sourceCRS
	 * @param targetCRS
	 * @param source
	 * @param target
	 * @param forwardEpsilon
	 * @param inverseEpsilon
	 * @throws TransformationException
	 */
	protected void doForward(ICRS sourceCRS, ICRS targetCRS, Point3d source, Point3d target, Point3d forwardEpsilon)
			throws TransformationException {
		StringBuilder output = new StringBuilder();
		output.append("Transforming '");
		output.append(sourceCRS.getCode().toString());
		output.append("' to '");
		output.append(targetCRS.getCode().toString());
		output.append("'.\n");

		// forward transform.
		boolean forwardSuccess = doForwardTransformation(sourceCRS, targetCRS, source, target, forwardEpsilon, output);

		LOG.debug(output.toString());
		assertEquals(true, forwardSuccess);

	}

	private boolean doForwardTransformation(ICRS sourceCRS, ICRS targetCRS, Point3d source, Point3d target,
			Point3d forwardEpsilon, StringBuilder output) throws TransformationException {
		boolean forwardSuccess = true;
		try {
			output.append("Forward transformation: ");
			output.append(doAccuracyTest(source, target, forwardEpsilon, sourceCRS, targetCRS));
		}
		catch (AssertionError ae) {
			output.append(ae.getLocalizedMessage());
			forwardSuccess = false;
		}
		return forwardSuccess;
	}

	private boolean doInverseTransformation(ICRS sourceCRS, ICRS targetCRS, Point3d source, Point3d target,
			Point3d inverseEpsilon, StringBuilder output) throws TransformationException {
		boolean inverseSuccess = true;
		try {
			output.append("\nInverse transformation: ");
			output.append(doAccuracyTest(target, source, inverseEpsilon, targetCRS, sourceCRS));
		}
		catch (AssertionError ae) {
			output.append(ae.getLocalizedMessage());
			inverseSuccess = false;
		}
		return inverseSuccess;
	}

}
