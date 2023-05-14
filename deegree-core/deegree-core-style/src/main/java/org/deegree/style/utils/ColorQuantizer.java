/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.style.utils;

/*
 * Helma License Notice
 *
 * The contents of this file are subject to the Helma License
 * Version 2.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://adele.helma.org/download/helma/license.txt
 *
 * Copyright 1998-2003 Helma Software. All Rights Reserved.
 *
 */

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.IndexColorModel;

/*
 * Modifications by Juerg Lehni:
 *
 * - Ported to Java from C
 * - Support for alpha-channels.
 * - Returns a BufferedImage of TYPE_BYTE_INDEXED with a IndexColorModel.
 * - Dithering of images through helma.image.DiffusionFilterOp by setting
 *   the dither parameter to true.
 * - Support for a transparent color, which is correctly rendered by GIFEncoder.
 *   All pixels with alpha < 0x80 are converted to this color when the parameter
 *   alphaToBitmask is set to true.
 */
/*
 %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 %                                                                             %
 %                                                                             %
 %                                                                             %
 %           QQQ   U   U   AAA   N   N  TTTTT  IIIII   ZZZZZ  EEEEE            %
 %          Q   Q  U   U  A   A  NN  N    T      I        ZZ  E                %
 %          Q   Q  U   U  AAAAA  N N N    T      I      ZZZ   EEEEE            %
 %          Q  QQ  U   U  A   A  N  NN    T      I     ZZ     E                %
 %           QQQQ   UUU   A   A  N   N    T    IIIII   ZZZZZ  EEEEE            %
 %                                                                             %
 %                                                                             %
 %         Methods to Reduce the Number of Unique Colors in an Image           %
 %                                                                             %
 %                                                                             %
 %                           Software Design                                   %
 %                             John Cristy                                     %
 %                              July 1992                                      %
 %                                                                             %
 %                                                                             %
 %  Copyright (C) 2003 ImageMagick Studio, a non-profit organization dedicated %
 %  to making software imaging solutions freely available.                     %
 %                                                                             %
 %  Permission is hereby granted, free of charge, to any person obtaining a    %
 %  copy of this software and associated documentation files ("ImageMagick"),  %
 %  to deal in ImageMagick without restriction, including without limitation   %
 %  the rights to use, copy, modify, merge, publish, distribute, sublicense,   %
 %  and/or sell copies of ImageMagick, and to permit persons to whom the       %
 %  ImageMagick is furnished to do so, subject to the following conditions:    %
 %                                                                             %
 %  The above copyright notice and this permission notice shall be included in %
 %  all copies or substantial portions of ImageMagick.                         %
 %                                                                             %
 %  The software is provided "as is", without warranty of any kind, express or %
 %  implied, including but not limited to the warranties of merchantability,   %
 %  fitness for a particular purpose and noninfringement.  In no event shall   %
 %  ImageMagick Studio be liable for any claim, damages or other liability,    %
 %  whether in an action of contract, tort or otherwise, arising from, out of  %
 %  or in connection with ImageMagick or the use or other dealings in          %
 %  ImageMagick.                                                               %
 %                                                                             %
 %  Except as contained in this notice, the name of the ImageMagick Studio     %
 %  shall not be used in advertising or otherwise to promote the sale, use or  %
 %  other dealings in ImageMagick without prior written authorization from the %
 %  ImageMagick Studio.                                                        %
 %                                                                             %
 %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 %
 %  Realism in computer graphics typically requires using 24 bits/pixel to
 %  generate an image.  Yet many graphic display devices do not contain the
 %  amount of memory necessary to match the spatial and color resolution of
 %  the human eye.  The Quantize methods takes a 24 bit image and reduces
 %  the number of colors so it can be displayed on raster device with less
 %  bits per pixel.  In most instances, the quantized image closely
 %  resembles the original reference image.
 %
 %  A reduction of colors in an image is also desirable for image
 %  transmission and real-time animation.
 %
 %  QuantizeImage() takes a standard RGB or monochrome images and quantizes
 %  them down to some fixed number of colors.
 %
 %  For purposes of color allocation, an image is a set of n pixels, where
 %  each pixel is a point in RGB space.  RGB space is a 3-dimensional
 %  vector space, and each pixel, Pi,  is defined by an ordered triple of
 %  red, green, and blue coordinates, (Ri, Gi, Bi).
 %
 %  Each primary color component (red, green, or blue) represents an
 %  intensity which varies linearly from 0 to a maximum value, Cmax, which
 %  corresponds to full saturation of that color.  Color allocation is
 %  defined over a domain consisting of the cube in RGB space with opposite
 %  vertices at (0,0,0) and (Cmax, Cmax, Cmax).  QUANTIZE requires Cmax =
 %  255.
 %
 %  The algorithm maps this domain onto a tree in which each node
 %  represents a cube within that domain.  In the following discussion
 %  these cubes are defined by the coordinate of two opposite vertices:
 %  The vertex nearest the origin in RGB space and the vertex farthest from
 %  the origin.
 %
 %  The tree's root node represents the the entire domain, (0,0,0) through
 %  (Cmax,Cmax,Cmax).  Each lower level in the tree is generated by
 %  subdividing one node's cube into eight smaller cubes of equal size.
 %  This corresponds to bisecting the parent cube with planes passing
 %  through the midpoints of each edge.
 %
 %  The basic algorithm operates in three phases: Classification,
 %  Reduction, and Assignment.  Classification builds a color description
 %  tree for the image.  Reduction collapses the tree until the number it
 %  represents, at most, the number of colors desired in the output image.
 %  Assignment defines the output image's color map and sets each pixel's
 %  color by restorage_class in the reduced tree.  Our goal is to minimize
 %  the numerical discrepancies between the original colors and quantized
 %  colors (quantization error).
 %
 %  Classification begins by initializing a color description tree of
 %  sufficient depth to represent each possible input color in a leaf.
 %  However, it is impractical to generate a fully-formed color description
 %  tree in the storage_class phase for realistic values of Cmax.  If
 %  colors components in the input image are quantized to k-bit precision,
 %  so that Cmax= 2k-1, the tree would need k levels below the root node to
 %  allow representing each possible input color in a leaf.  This becomes
 %  prohibitive because the tree's total number of nodes is 1 +
 %  sum(i=1, k, 8k).
 %
 %  A complete tree would require 19,173,961 nodes for k = 8, Cmax = 255.
 %  Therefore, to avoid building a fully populated tree, QUANTIZE: (1)
 %  Initializes data structures for nodes only as they are needed;  (2)
 %  Chooses a maximum depth for the tree as a function of the desired
 %  number of colors in the output image (currently log2(colormap size)).
 %
 %  For each pixel in the input image, storage_class scans downward from
 %  the root of the color description tree.  At each level of the tree it
 %  identifies the single node which represents a cube in RGB space
 %  containing the pixel's color.  It updates the following data for each
 %  such node:
 %
 %    n1: Number of pixels whose color is contained in the RGB cube which
 %    this node represents;
 %
 %    n2: Number of pixels whose color is not represented in a node at
 %    lower depth in the tree;  initially,  n2 = 0 for all nodes except
 %    leaves of the tree.
 %
 %    Sr, Sg, Sb: Sums of the red, green, and blue component values for all
 %    pixels not classified at a lower depth. The combination of these sums
 %    and n2  will ultimately characterize the mean color of a set of
 %    pixels represented by this node.
 %
 %    E: The distance squared in RGB space between each pixel contained
 %    within a node and the nodes' center.  This represents the
 %    quantization error for a node.
 %
 %  Reduction repeatedly prunes the tree until the number of nodes with n2
 %  > 0 is less than or equal to the maximum number of colors allowed in
 %  the output image.  On any given iteration over the tree, it selects
 %  those nodes whose E count is minimal for pruning and merges their color
 %  statistics upward. It uses a pruning threshold, Ep, to govern node
 %  selection as follows:
 %
 %    Ep = 0
 %    while number of nodes with (n2 > 0) > required maximum number of colors
 %      prune all nodes such that E <= Ep
 %      Set Ep to minimum E in remaining nodes
 %
 %  This has the effect of minimizing any quantization error when merging
 %  two nodes together.
 %
 %  When a node to be pruned has offspring, the pruning procedure invokes
 %  itself recursively in order to prune the tree from the leaves upward.
 %  n2,  Sr, Sg,  and  Sb in a node being pruned are always added to the
 %  corresponding data in that node's parent.  This retains the pruned
 %  node's color characteristics for later averaging.
 %
 %  For each node, n2 pixels exist for which that node represents the
 %  smallest volume in RGB space containing those pixel's colors.  When n2
 %  > 0 the node will uniquely define a color in the output image. At the
 %  beginning of reduction,  n2 = 0  for all nodes except a the leaves of
 %  the tree which represent colors present in the input image.
 %
 %  The other pixel count, n1, indicates the total number of colors within
 %  the cubic volume which the node represents.  This includes n1 - n2
 %  pixels whose colors should be defined by nodes at a lower level in the
 %  tree.
 %
 %  Assignment generates the output image from the pruned tree.  The output
 %  image consists of two parts: (1)  A color map, which is an array of
 %  color descriptions (RGB triples) for each color present in the output
 %  image;  (2)  A pixel array, which represents each pixel as an index
 %  into the color map array.
 %
 %  First, the assignment phase makes one pass over the pruned color
 %  description tree to establish the image's color map.  For each node
 %  with n2  > 0, it divides Sr, Sg, and Sb by n2 .  This produces the mean
 %  color of all pixels that classify no lower than this node.  Each of
 %  these colors becomes an entry in the color map.
 %
 %  Finally,  the assignment phase reclassifies each pixel in the pruned
 %  tree to identify the deepest node containing the pixel's color.  The
 %  pixel's value in the pixel array becomes the index of this node's mean
 %  color in the color map.
 %
 %  This method is based on a similar algorithm written by Paul Raveling.
 %
 %
 */

public class ColorQuantizer {

	public static final int MAX_NODES = 266817;

	public static final int MAX_TREE_DEPTH = 8;

	public static final int MAX_CHILDREN = 16;

	public static final int MAX_RGB = 255;

	static class ClosestColor {

		int distance;

		int colorIndex;

	}

	static class Node {

		Cube cube;

		Node parent;

		Node children[];

		int numChildren;

		int id;

		int level;

		int uniqueCount;

		int totalRed;

		int totalGreen;

		int totalBlue;

		int totalAlpha;

		long quantizeError;

		int colorIndex;

		Node(Cube cube) {
			this(cube, 0, 0, null);
			this.parent = this;
		}

		Node(Cube cube, int id, int level, Node parent) {
			this.cube = cube;
			this.parent = parent;
			this.id = id;
			this.level = level;
			this.children = new Node[MAX_CHILDREN];
			this.numChildren = 0;
			if (parent != null) {
				parent.children[id] = this;
				parent.numChildren++;
			}
			cube.numNodes++;
		}

		void pruneLevel() {
			// Traverse any children.
			if (this.numChildren > 0)
				for (int id = 0; id < MAX_CHILDREN; id++)
					if (this.children[id] != null)
						this.children[id].pruneLevel();
			if (this.level == this.cube.depth)
				prune();
		}

		void pruneToCubeDepth() {
			// Traverse any children.
			if (this.numChildren > 0)
				for (int id = 0; id < MAX_CHILDREN; id++)
					if (this.children[id] != null)
						this.children[id].pruneToCubeDepth();
			if (this.level > this.cube.depth)
				prune();
		}

		void prune() {
			// Traverse any children.
			if (this.numChildren > 0)
				for (int id = 0; id < MAX_CHILDREN; id++)
					if (this.children[id] != null)
						this.children[id].prune();
			// Merge color statistics into parent.
			this.parent.uniqueCount += this.uniqueCount;
			this.parent.totalRed += this.totalRed;
			this.parent.totalGreen += this.totalGreen;
			this.parent.totalBlue += this.totalBlue;
			this.parent.totalAlpha += this.totalAlpha;
			this.parent.children[this.id] = null;
			this.parent.numChildren--;
			this.cube.numNodes--;
		}

		void reduce(long pruningThreshold) {
			// Traverse any children.
			if (this.numChildren > 0)
				for (int id = 0; id < MAX_CHILDREN; id++)
					if (this.children[id] != null)
						this.children[id].reduce(pruningThreshold);
			if (this.quantizeError <= pruningThreshold)
				prune();
			else {
				// Find minimum pruning threshold.
				if (this.uniqueCount > 0)
					this.cube.numColors++;
				if (this.quantizeError < this.cube.nextThreshold)
					this.cube.nextThreshold = this.quantizeError;
			}
		}

		void findClosestColor(int red, int green, int blue, int alpha, ClosestColor closest) {
			// Traverse any children.
			if (this.numChildren > 0)
				for (int id = 0; id < MAX_CHILDREN; id++)
					if (this.children[id] != null)
						this.children[id].findClosestColor(red, green, blue, alpha, closest);
			if (this.uniqueCount != 0) {
				// Determine if this color is "closest".
				int dr = (this.cube.colorMap[0][this.colorIndex] & 0xff) - red;
				int dg = (this.cube.colorMap[1][this.colorIndex] & 0xff) - green;
				int db = (this.cube.colorMap[2][this.colorIndex] & 0xff) - blue;
				int da = (this.cube.colorMap[3][this.colorIndex] & 0xff) - alpha;
				int distance = da * da + dr * dr + dg * dg + db * db;
				if (distance < closest.distance) {
					closest.distance = distance;
					closest.colorIndex = this.colorIndex;
				}
			}
		}

		int fillColorMap(byte colorMap[][], int index) {
			// Traverse any children.
			if (this.numChildren > 0)
				for (int id = 0; id < MAX_CHILDREN; id++)
					if (this.children[id] != null)
						index = this.children[id].fillColorMap(colorMap, index);
			if (this.uniqueCount != 0) {
				// Colormap entry is defined by the mean color in this cube.
				colorMap[0][index] = (byte) (this.totalRed / this.uniqueCount + 0.5);
				colorMap[1][index] = (byte) (this.totalGreen / this.uniqueCount + 0.5);
				colorMap[2][index] = (byte) (this.totalBlue / this.uniqueCount + 0.5);
				colorMap[3][index] = (byte) (this.totalAlpha / this.uniqueCount + 0.5);
				this.colorIndex = index++;
			}
			return index;
		}

	}

	static class Cube {

		Node root;

		int numColors;

		boolean addTransparency;

		// firstColor is set to 1 when when addTransparency is true!
		int firstColor;

		byte colorMap[][];

		long nextThreshold;

		int numNodes;

		int depth;

		Cube(int maxColors) {
			this.depth = getDepth(maxColors);
			this.numColors = 0;
			this.root = new Node(this);
		}

		int getDepth(int numColors) {
			// Depth of color tree is: Log4(colormap size)+2.
			int depth;
			for (depth = 1; numColors != 0; depth++)
				numColors >>= 2;
			if (depth > MAX_TREE_DEPTH)
				depth = MAX_TREE_DEPTH;
			if (depth < 2)
				depth = 2;
			return depth;
		}

		void classifyImageColors(BufferedImage image, boolean alphaToBitmask) {
			this.addTransparency = false;
			this.firstColor = 0;

			Node node, child;
			int x, px, y, index, level, id, count;
			int pixel, red, green, blue, alpha;
			int bisect, midRed, midGreen, midBlue, midAlpha;

			int width = image.getWidth();
			int height = image.getHeight();

			// Classify the first 256 colors to a tree depth of MAX_TREE_DEPTH.
			int levelThreshold = MAX_TREE_DEPTH;
			// create a BufferedImage of only 1 pixel height for fetching the rows
			// of the image in the correct format (ARGB)
			// This speeds up things by more than factor 2, compared to the standard
			// BufferedImage.getRGB solution
			BufferedImage row = new BufferedImage(width, 1, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = row.createGraphics();
			int pixels[] = ((DataBufferInt) row.getRaster().getDataBuffer()).getData();
			// make sure alpha values do not add up for each row:
			g2d.setComposite(AlphaComposite.Src);
			// calculate scanline by scanline in order to safe memory.
			// It also seems to run faster like that
			for (y = 0; y < height; y++) {
				g2d.drawImage(image, null, 0, -y);
				// now pixels contains the rgb values of the row y!
				if (this.numNodes > MAX_NODES) {
					// Prune one level if the color tree is too large.
					this.root.pruneLevel();
					this.depth--;
				}
				for (x = 0; x < width;) {
					pixel = pixels[x];
					red = (pixel >> 16) & 0xff;
					green = (pixel >> 8) & 0xff;
					blue = (pixel >> 0) & 0xff;
					alpha = (pixel >> 24) & 0xff;
					if (alphaToBitmask)
						alpha = alpha < 0x80 ? 0 : 0xff;

					// skip same pixels, but count them
					px = x;
					for (++x; x < width; x++)
						if (pixels[x] != pixel)
							break;
					count = x - px;

					// Start at the root and descend the color cube tree.
					if (alpha > 0) {
						index = MAX_TREE_DEPTH - 1;
						bisect = (MAX_RGB + 1) >> 1;
						midRed = bisect;
						midGreen = bisect;
						midBlue = bisect;
						midAlpha = bisect;
						node = this.root;
						for (level = 1; level <= levelThreshold; level++) {
							id = (((red >> index) & 0x01) << 3 | ((green >> index) & 0x01) << 2
									| ((blue >> index) & 0x01) << 1 | ((alpha >> index) & 0x01));
							bisect >>= 1;
							midRed += (id & 8) != 0 ? bisect : -bisect;
							midGreen += (id & 4) != 0 ? bisect : -bisect;
							midBlue += (id & 2) != 0 ? bisect : -bisect;
							midAlpha += (id & 1) != 0 ? bisect : -bisect;
							child = node.children[id];
							if (child == null) {
								// Set colors of new node to contain pixel.
								child = new Node(this, id, level, node);
								if (level == levelThreshold) {
									this.numColors++;
									if (this.numColors == 256) {
										// More than 256 colors; classify to the
										// cube_info.depth tree depth.
										levelThreshold = this.depth;
										this.root.pruneToCubeDepth();
									}
								}
							}
							// Approximate the quantization error represented by
							// this node.
							node = child;
							int r = red - midRed;
							int g = green - midGreen;
							int b = blue - midBlue;
							int a = alpha - midAlpha;
							node.quantizeError += count * (r * r + g * g + b * b + a * a);
							this.root.quantizeError += node.quantizeError;
							index--;
						}
						// Sum RGB for this leaf for later derivation of the mean
						// cube color.
						node.uniqueCount += count;
						node.totalRed += count * red;
						node.totalGreen += count * green;
						node.totalBlue += count * blue;
						node.totalAlpha += count * alpha;
					}
					else if (!this.addTransparency) {
						this.addTransparency = true;
						this.numColors++;
						this.firstColor = 1; // start at 1 as 0 will be the transparent
												// color
					}
				}
			}
		}

		void reduceImageColors(int maxColors) {
			this.nextThreshold = 0;
			while (this.numColors > maxColors) {
				long pruningThreshold = this.nextThreshold;
				this.nextThreshold = this.root.quantizeError - 1;
				this.numColors = this.firstColor;
				this.root.reduce(pruningThreshold);
			}
		}

		BufferedImage assignImageColors(BufferedImage image, boolean dither, boolean alphaToBitmask) {
			// Allocate image colormap.
			this.colorMap = new byte[4][this.numColors];
			this.root.fillColorMap(this.colorMap, this.firstColor);
			// create the right color model, depending on transparency settings:
			IndexColorModel icm;

			int width = image.getWidth();
			int height = image.getHeight();

			if (alphaToBitmask) {
				if (this.addTransparency) {
					icm = new IndexColorModel(this.depth, this.numColors, this.colorMap[0], this.colorMap[1],
							this.colorMap[2], 0);
				}
				else {
					icm = new IndexColorModel(this.depth, this.numColors, this.colorMap[0], this.colorMap[1],
							this.colorMap[2]);
				}
			}
			else {
				icm = new IndexColorModel(this.depth, this.numColors, this.colorMap[0], this.colorMap[1],
						this.colorMap[2], this.colorMap[3]);
			}

			// create the indexed BufferedImage:
			BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, icm);

			if (dither)
				new DiffusionFilterOp().filter(image, dest);
			else {
				ClosestColor closest = new ClosestColor();
				// convert to indexed color
				byte[] dst = ((DataBufferByte) dest.getRaster().getDataBuffer()).getData();

				// create a BufferedImage of only 1 pixel height for fetching
				// the rows of the image in the correct format (ARGB)
				// This speeds up things by more than factor 2, compared to the
				// standard BufferedImage.getRGB solution
				BufferedImage row = new BufferedImage(width, 1, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2d = row.createGraphics();
				int pixels[] = ((DataBufferInt) row.getRaster().getDataBuffer()).getData();
				// make sure alpha values do not add up for each row:
				g2d.setComposite(AlphaComposite.Src);
				// calculate scanline by scanline in order to safe memory.
				// It also seems to run faster like that
				Node node;
				int x, y, i, id;
				int pixel, red, green, blue, alpha;
				int pos = 0;
				for (y = 0; y < height; y++) {
					g2d.drawImage(image, null, 0, -y);
					// now pixels contains the rgb values of the row y!
					// filter this row now:
					for (x = 0; x < width;) {
						pixel = pixels[x];
						red = (pixel >> 16) & 0xff;
						green = (pixel >> 8) & 0xff;
						blue = (pixel >> 0) & 0xff;
						alpha = (pixel >> 24) & 0xff;

						if (alphaToBitmask)
							alpha = alpha < 128 ? 0 : 0xff;

						byte col;
						if (alpha == 0 && this.addTransparency) {
							col = 0; // transparency color is at position 0 of color map
						}
						else {
							// walk the tree to find the cube containing that
							// color
							node = this.root;
							for (i = MAX_TREE_DEPTH - 1; i > 0; i--) {
								id = (((red >> i) & 0x01) << 3 | ((green >> i) & 0x01) << 2 | ((blue >> i) & 0x01) << 1
										| ((alpha >> i) & 0x01));
								if (node.children[id] == null)
									break;
								node = node.children[id];
							}

							// Find the closest color.
							closest.distance = Integer.MAX_VALUE;
							node.parent.findClosestColor(red, green, blue, alpha, closest);
							col = (byte) closest.colorIndex;
						}

						// first color
						dst[pos++] = col;

						// next colors the same?
						for (++x; x < width; x++) {
							if (pixels[x] != pixel)
								break;
							dst[pos++] = col;
						}
					}
				}
			}
			return dest;
		}

	}

	public static BufferedImage quantizeImage(BufferedImage image, int maxColors, boolean dither,
			boolean alphaToBitmask) {
		Cube cube = new Cube(maxColors);
		cube.classifyImageColors(image, alphaToBitmask);
		cube.reduceImageColors(maxColors);
		return cube.assignImageColors(image, dither, alphaToBitmask);
	}

}
