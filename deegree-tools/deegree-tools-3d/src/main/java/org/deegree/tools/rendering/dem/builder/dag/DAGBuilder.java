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

package org.deegree.tools.rendering.dem.builder.dag;

import java.nio.ByteBuffer;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.deegree.rendering.r3d.multiresolution.MeshFragment;
import org.deegree.tools.rendering.dem.builder.PatchManager;

/**
 * Builds the DAG during the construction process.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class DAGBuilder {

	private int nextNodeId = 0;

	private int nextArcId = 0;

	private int nextPatchId = 0;

	// arcs and nodes in their final order (ordered by id)
	private Arc[] arcs;

	private Node[] nodes;

	private FragmentInfo[] patches;

	private DAGLevel[] dagLevels;

	// lookup of modification by location code
	private Node[] nodesByPatchCode;

	// lookup of patch id by location code
	private int[] patchIdByLocationCode;

	public DAGBuilder(int levels, PatchManager patchManager) {
		// add one level for drain node
		dagLevels = new DAGLevel[levels + 1];
		nodesByPatchCode = new Node[2 << levels];
		patchIdByLocationCode = new int[(2 << levels) - 2];
		nodes = new Node[nodesByPatchCode.length];
		arcs = new Arc[2 << levels];
		patches = new FragmentInfo[(2 << levels) - 2];

		Node rootNode = new Node(nextNodeId++);
		rootNode.locationCode1 = 2;
		rootNode.locationCode2 = 3;
		associateModification(rootNode, 2);
		associateModification(rootNode, 3);
		nodes[rootNode.id] = rootNode;

		dagLevels[0] = new DAGLevel();
		dagLevels[0].nodes.add(rootNode);

		for (int i = 1; i < levels; i++) {
			buildLevel(i);
		}

		for (int i = 0; i < levels - 1; i++) {
			addArcs(i);
		}

		// add drain node
		DAGLevel lastRealLevel = dagLevels[levels - 1];
		Node drainNode = new Node(nextNodeId);
		dagLevels[levels] = new DAGLevel();
		dagLevels[levels].nodes.add(drainNode);
		for (Node node : lastRealLevel.nodes) {
			node.lowestOutgoingArc = nextArcId;
			node.highestOutgoingArc = nextArcId;

			Arc arc = addArc(node, drainNode);
			if (node.locationCode3 == -1) {
				arc.locationCodes = new int[] { node.locationCode1, node.locationCode2 };
			}
			else {
				arc.locationCodes = new int[] { node.locationCode1, node.locationCode2, node.locationCode3,
						node.locationCode4 };
			}
		}
		nodes[drainNode.id] = drainNode;
		nextNodeId++;

		// cleanup
		nodesByPatchCode = null;
		Arc[] arcs2 = new Arc[nextArcId];
		for (int i = 0; i < arcs2.length; i++) {
			arcs2[i] = arcs[i];
		}
		arcs = arcs2;
		Node[] nodes2 = new Node[nextNodeId];
		for (int i = 0; i < nodes2.length; i++) {
			nodes2[i] = nodes[i];
		}
		nodes = nodes2;

		// generate consistent patch numbering (patches on the same arc must have
		// subsequent ids)
		// also calculate geometry error for the arcs
		for (Arc arc : arcs) {
			float error = Float.MIN_VALUE;
			arc.patchIds = new int[arc.locationCodes.length];
			for (int i = 0; i < arc.locationCodes.length; i++) {
				int locationCode = arc.locationCodes[i];
				FragmentInfo patch = patchManager.getPatchByLocationCode(locationCode);
				if (patch.geometricError > error) {
					error = patch.geometricError;
				}
				addPatch(locationCode, patch);
				patchIdByLocationCode[locationCode - 2] = patch.id;
				arc.patchIds[i] = patch.id;
			}
			arc.lowestPatch = arc.patchIds[0];
			arc.highestPatch = arc.patchIds[arc.patchIds.length - 1];
			arc.error = error;
		}
	}

	public Arc[] getArcs() {
		return arcs;
	}

	public Node[] getNodes() {
		return nodes;
	}

	public FragmentInfo[] getPatchInfo() {
		return patches;
	}

	public FragmentInfo getPatchByLocationCode(int locationCode) {
		int patchId = patchIdByLocationCode[locationCode - 2];
		return patches[patchId];
	}

	public void writeBlob(Blob targetBlob, short flags, short rowsPerMt) throws SQLException {

		int nodesSegmentSize = Node.SIZE * nodes.length;
		int arcsSegmentSize = Arc.SIZE * arcs.length;
		int patchesSegmentSize = MeshFragment.SIZE * patches.length;

		// write segments to blob
		byte[] headerBytes = buildHeader(nodes.length, arcs.length, patches.length, flags, rowsPerMt);
		int headerSegmentStart = 0;
		int nodesSegmentStart = headerSegmentStart + headerBytes.length;
		int arcsSegmentStart = nodesSegmentStart + nodesSegmentSize;
		int patchesSegmentStart = arcsSegmentStart + arcsSegmentSize;

		System.out.print("Writing header segment [" + headerSegmentStart + "-"
				+ (headerSegmentStart + headerBytes.length - 1) + "]...");
		targetBlob.setBytes(headerSegmentStart + 1, headerBytes);
		System.out.println("done.");

		System.out.print("Writing nodes segment [" + nodesSegmentStart + "-"
				+ (nodesSegmentStart + nodesSegmentSize - 1) + "]...");
		byte[] nodeBytes = new byte[Node.SIZE];
		ByteBuffer nodeBuffer = ByteBuffer.wrap(nodeBytes);
		int offset = 0;
		for (Node node : nodes) {
			nodeBuffer.rewind();
			float[][] bbox = getBBox(node);
			float error = getError(node);
			node.append(nodeBuffer, error, bbox);
			targetBlob.setBytes(offset + nodesSegmentStart + 1, nodeBytes);
			offset += Node.SIZE;
		}
		System.out.println("done.");

		System.out.print(
				"Writing arcs segment [" + arcsSegmentStart + "-" + (arcsSegmentStart + arcsSegmentSize - 1) + "]...");
		byte[] arcBytes = new byte[Arc.SIZE];
		ByteBuffer arcBuffer = ByteBuffer.wrap(arcBytes);
		offset = 0;
		for (Arc arc : arcs) {
			arcBuffer.rewind();
			arc.append(arcBuffer);
			targetBlob.setBytes(offset + arcsSegmentStart + 1, arcBytes);
			offset += Arc.SIZE;
		}
		System.out.println("done.");

		System.out.print("Writing patches segment [" + patchesSegmentStart + "-"
				+ (patchesSegmentStart + patchesSegmentSize - 1) + "]...");
		byte[] patchBytes = new byte[FragmentInfo.SIZE];
		ByteBuffer patchBuffer = ByteBuffer.wrap(patchBytes);
		offset = 0;
		for (FragmentInfo patch : patches) {
			patchBuffer.rewind();
			patch.append(patchBuffer);
			targetBlob.setBytes(offset + patchesSegmentStart + 1, patchBytes);
			offset += FragmentInfo.SIZE;
		}
		System.out.println("done.");
	}

	private float getError(Node node) {
		if (node.lowestOutgoingArc == -1) {
			return -1.0f;
		}
		return patches[arcs[node.lowestOutgoingArc].lowestPatch].geometricError;
	}

	private float[][] getBBox(Node node) {
		float[][] bbox = new float[][] { new float[] { Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE },
				new float[] { Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE } };
		if (node.lowestOutgoingArc != -1) {
			for (int arcId = node.lowestOutgoingArc; arcId <= node.highestOutgoingArc; arcId++) {
				Arc arc = arcs[arcId];
				for (int patchId = arc.lowestPatch; patchId <= arc.highestPatch; patchId++) {
					FragmentInfo patch = patches[patchId];
					float[][] patchBBox = patch.getBBox();
					bbox = mergeBBoxes(bbox, patchBBox);
				}
			}
		}
		return bbox;
	}

	private float[][] mergeBBoxes(float[][] bbox1, float[][] bbox2) {
		float[][] bbox = new float[][] { new float[] { bbox1[0][0], bbox1[0][1], bbox1[0][2] },
				new float[] { bbox1[1][0], bbox1[1][1], bbox1[1][2] } };
		for (int i = 0; i <= 2; i++) {
			if (bbox[0][i] > bbox2[0][i]) {
				bbox[0][i] = bbox2[0][i];
			}
		}
		for (int i = 0; i <= 2; i++) {
			if (bbox[1][i] < bbox2[1][i]) {
				bbox[1][i] = bbox2[1][i];
			}
		}
		return bbox;
	}

	private void buildLevel(int level) {
		DAGLevel lastLevel = dagLevels[level - 1];
		dagLevels[level] = new DAGLevel();
		for (Node node : lastLevel.nodes) {
			addChildNode(node, node.locationCode1, dagLevels[level]);
			addChildNode(node, node.locationCode2, dagLevels[level]);
			if (node.locationCode3 != -1) {
				addChildNode(node, node.locationCode3, dagLevels[level]);
				addChildNode(node, node.locationCode4, dagLevels[level]);
			}
		}
	}

	private void addChildNode(Node parentNode, int patchCode, DAGLevel level) {
		// check if corresponding child node already exists
		if (getModification(patchCode << 1) == null) {
			Node node = new Node(nextNodeId++);
			node.locationCode1 = patchCode << 1;
			node.locationCode2 = node.locationCode1 + 1;
			associateModification(node, node.locationCode1);
			associateModification(node, node.locationCode2);

			// check for diamond neighbour
			int diamondNeighbour = getLongEdgeNeighbour(patchCode);
			if (diamondNeighbour != -1) {
				node.locationCode3 = diamondNeighbour << 1;
				node.locationCode4 = node.locationCode3 + 1;
				associateModification(node, node.locationCode3);
				associateModification(node, node.locationCode4);
			}
			level.nodes.add(node);
			nodes[node.id] = node;
		}
	}

	/**
	 * Add arcs (from level i to i + 1).
	 * @param i
	 */
	private void addArcs(int i) {

		DAGLevel level = dagLevels[i];

		for (Node sourceNode : level.nodes) {
			sourceNode.lowestOutgoingArc = nextArcId;

			if (isDiamond(sourceNode.locationCode1, sourceNode.locationCode2)) {
				// 1 Kante mit 2 Patches
				Arc arc = addArc(sourceNode, getModification(sourceNode.locationCode1 << 1));
				arc.locationCodes = new int[] { sourceNode.locationCode1, sourceNode.locationCode2 };
			}
			else {
				// 2 Kanten mit jeweils 1 Patch
				Arc arc1 = addArc(sourceNode, getModification(sourceNode.locationCode1 << 1));
				arc1.locationCodes = new int[] { sourceNode.locationCode1 };
				Arc arc2 = addArc(sourceNode, getModification(sourceNode.locationCode2 << 1));
				arc2.locationCodes = new int[] { sourceNode.locationCode2 };
			}

			if (sourceNode.locationCode3 != -1) {
				if (isDiamond(sourceNode.locationCode3, sourceNode.locationCode4)) {
					// 1 Kante mit 4 Patches
					Arc arc = addArc(sourceNode, getModification(sourceNode.locationCode3 << 1));
					arc.locationCodes = new int[] { sourceNode.locationCode3, sourceNode.locationCode4 };
				}
				else {
					// 2 Kanten mit jeweils 1 Patch
					Arc arc1 = addArc(sourceNode, getModification(sourceNode.locationCode3 << 1));
					arc1.locationCodes = new int[] { sourceNode.locationCode3 };
					Arc arc2 = addArc(sourceNode, getModification(sourceNode.locationCode4 << 1));
					arc2.locationCodes = new int[] { sourceNode.locationCode4 };
				}
			}

		}
	}

	private Arc addArc(Node sourceNode, Node destinationNode) {

		sourceNode.highestOutgoingArc = nextArcId;

		if (destinationNode.lowestIncomingArc == -1 || destinationNode.lowestIncomingArc > nextArcId) {
			destinationNode.lowestIncomingArc = nextArcId;
		}

		if (destinationNode.lastIncomingArc != -1) {
			arcs[destinationNode.lastIncomingArc].nextArcWithSameDestination = nextArcId;
		}
		destinationNode.lastIncomingArc = nextArcId;

		Arc arc = new Arc(nextArcId, sourceNode.id, destinationNode.id);
		arcs[nextArcId] = arc;
		nextArcId++;
		return arc;
	}

	private boolean isDiamond(int patchCode1, int patchCode2) {
		return getModification(patchCode1 << 1) == getModification(patchCode2 << 1);
	}

	private FragmentInfo addPatch(int locationCode, FragmentInfo patchInfo) {
		patchInfo.id = nextPatchId;
		patches[nextPatchId] = patchInfo;
		nextPatchId++;
		return patchInfo;
	}

	/**
	 * Associates the given modification node with the given locationCode.
	 * @param node the modification node
	 * @param locationCode triangle location code introduced by the node
	 * @return
	 */
	private void associateModification(Node node, int locationCode) {
		nodesByPatchCode[locationCode - 2] = node;
	}

	/**
	 * Return the modification node that contains the triangle with the given location
	 * code.
	 * @param locationCode triangle location code
	 * @return
	 */
	private Node getModification(int locationCode) {
		return nodesByPatchCode[locationCode - 2];
	}

	private int getLongEdgeNeighbour(int locationCode) {
		String locationCodeString = Integer.toString(locationCode, 2).substring(1);
		String neighbourCodeString = getNeighbourCode(3, locationCodeString);
		if (neighbourCodeString == null) {
			return -1;
		}
		return Integer.parseInt("1" + neighbourCodeString, 2);
	}

	private String getNeighbourCode(int neighbourNum, String locationCode) {

		String neighbourCode = null;

		if (locationCode == null || locationCode.length() == 0) {
			neighbourCode = null;
		}
		else if (locationCode.length() == 1) {
			// locationCode == "0" || locationCode == "1"
			if (neighbourNum == 3) {
				if ("0".equals(locationCode)) {
					neighbourCode = "1";
				}
				else if ("1".equals(locationCode)) {
					neighbourCode = "0";
				}
			}
			else {
				neighbourCode = null;
			}
		}
		else {
			// locationCode.length () > 1
			char lastChar = locationCode.charAt(locationCode.length() - 1);
			String codePrefix = locationCode.substring(0, locationCode.length() - 1);
			if (lastChar == '0') {
				switch (neighbourNum) {
					case 1: {
						neighbourCode = getNeighbourCode(3, codePrefix);
						break;
					}
					case 2: {
						neighbourCode = codePrefix;
						break;
					}
					case 3: {
						neighbourCode = getNeighbourCode(2, codePrefix);
						break;
					}
					default: {
						assert false;
					}
				}
				if (neighbourCode != null) {
					neighbourCode += "1";
				}
			}
			else {
				switch (neighbourNum) {
					case 1: {
						neighbourCode = codePrefix;
						break;
					}
					case 2: {
						neighbourCode = getNeighbourCode(3, codePrefix);
						break;
					}
					case 3: {
						neighbourCode = getNeighbourCode(1, codePrefix);
						break;
					}
					default: {
						assert false;
					}
				}
				if (neighbourCode != null) {
					neighbourCode += "0";
				}
			}
		}
		return neighbourCode;
	}

	private void addCodes(List<String> codes, String currentCode, int length) {
		if (currentCode.length() == length) {
			codes.add(currentCode);
		}
		else {
			addCodes(codes, currentCode + "0", length);
			addCodes(codes, currentCode + "1", length);
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < dagLevels.length; i++) {
			sb.append("Level " + i + ":\n" + dagLevels[i] + "\n");
		}

		for (int i = 0; i < nextArcId; i++) {
			sb.append(arcs[i] + "\n");
		}
		sb.append(nodesByPatchCode.length + " nodes, " + nextArcId + " arcs.");
		return sb.toString();
	}

	public void printArrays() {
		System.out.println("Node array: ");
		for (int i = 0; i < nodes.length; i++) {
			System.out.println(nodes[i]);
		}
		System.out.println("\nArc array: ");
		for (int i = 0; i < arcs.length; i++) {
			System.out.println(arcs[i]);
		}
		System.out.println("\nPatch array: ");
		for (int i = 0; i < patches.length; i++) {
			System.out.println(patches[i]);
		}
		System.out.println(nodes.length + " nodes, " + arcs.length + " arcs, " + patches.length + " patches.");
	}

	public void printStats() {
		int arcsFromParent = 1;
		for (int i = 1; i < dagLevels.length; i++) {
			DAGLevel level = dagLevels[i];
			int numNodes = 0;
			int arcsToNext = 0;
			for (Node node : level.nodes) {
				if (node.lowestOutgoingArc != -1) {
					arcsToNext += node.highestOutgoingArc - node.lowestOutgoingArc + 1;
				}
				numNodes++;
			}
			System.out.println(
					"Level " + i + " has " + numNodes + " node(s) and " + arcsFromParent + " arc(s) to parent level.");
			arcsFromParent = arcsToNext;
		}
	}

	private byte[] buildHeader(int numNodes, int numArcs, int numPatches, short flags, short rowsPerMt) {
		byte[] bytes = new byte[4 * 4];
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		buffer.putShort(flags);
		buffer.putShort(rowsPerMt);
		buffer.putInt(numNodes);
		buffer.putInt(numArcs);
		buffer.putInt(numPatches);
		buffer.rewind();
		return bytes;
	}

	private class DAGLevel {

		int level;

		List<Node> nodes = new ArrayList<Node>();

		public String toString() {
			StringBuffer sb = new StringBuffer();
			for (Node node : nodes) {
				sb.append(node);
				sb.append('\n');
			}
			return sb.toString();
		}

	}

}
