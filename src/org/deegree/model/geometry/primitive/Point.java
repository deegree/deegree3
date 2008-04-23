package org.deegree.model.geometry.primitive;

/**
 * @version 1.0
 * @created 03-Sep-2007 13:58:48
 */
public interface Point extends Primitive {

	public double getX();

	public double getY();

	public double getZ();

	/**
	 * 
	 * @param dimension
	 */
	public double get(int dimension);

	public double[] getAsArray();

}