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

package org.deegree.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.deegree.test.services.wpvs.WPVSStressTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>TestingToolBox</code> class is a command-line tool for testing the services.
 *
 * The implementation is adapted from org.deegree.tools.ToolBox
 *
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 *
 */
public class TestingToolBox {

	private final int NAME_PAD = 3;

	private final int DESCRIPTION_PAD = 5;

	private final int TEXT_WIDTH = 80;

	private final static Logger LOG = LoggerFactory.getLogger(TestingToolBox.class);

	private static TestingToolBox instance = null;

	private final TestInfo[] tests = { new TestInfo(WPVSStressTest.class,
			"Sends getView requests to the deegree3 WPVS. Multiple thread testing is supported.") };

	private void printList() {
		System.out.println("\nAvailable tests, start with 'd3tests <name> <args>', or omit:\n");

		// determine the maximum length of a tool name
		int maxLength = -1;
		for (TestInfo tool : tests) {
			if (tool.getName().length() > maxLength) {
				maxLength = tool.getName().length();
			}
		}

		for (TestInfo test : tests) {
			if (test != null) {
				System.out.print(createPadding(NAME_PAD));
				System.out.print(test.getName());

				// pad line up to maxLength
				System.out.print(createPadding(maxLength - test.getName().length()));

				System.out.print(createPadding(DESCRIPTION_PAD));

				printWrappedText(test.getDescription(), maxLength);
			}
		}
	}

	private void printWrappedText(String description, int maxLength) {
		// local string that will be chopped off from the left side
		String text = description;

		// maximum description width
		int widthLeft = TEXT_WIDTH - NAME_PAD - maxLength - DESCRIPTION_PAD;

		while (true) {
			int nMark = text.indexOf("\n");
			if (nMark != -1 && nMark < widthLeft) {
				System.out.print(text.substring(0, nMark));
				System.out.print(createPadding(NAME_PAD + maxLength + DESCRIPTION_PAD));
				text = text.substring(nMark + 1);

			}
			else {
				if (text.length() - widthLeft > 0) {

					// find last whitespace that occurs before TEXT_WIDH
					int index = -1;
					int newIndex;
					while ((newIndex = text.indexOf(" ", index + 1)) != -1) {
						if (newIndex <= widthLeft) {
							index = newIndex;

						}
						else {
							break;
						}
					}

					if (index == -1) {
						System.out.println();
						return;
					}
					System.out.println(text.substring(0, index));
					System.out.print(createPadding(NAME_PAD + maxLength + DESCRIPTION_PAD));
					text = text.substring(index + 1);
				}
				else {
					System.out.println(text);
					break;
				}
			}
		}
	}

	private synchronized static TestingToolBox getInstance() {
		if (instance == null) {
			instance = new TestingToolBox();
		}
		return instance;
	}

	private String createPadding(int padLength) {
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < padLength; i++) {
			s.append(" ");
		}
		return s.toString();
	}

	private TestInfo findTest(String testName) {
		TestInfo test = null;
		for (TestInfo testinfo : tests) {
			if (testinfo.getName().equals(testName)) {
				test = testinfo;
				break;
			}
		}
		return test;
	}

	/**
	 * Run one of the tests. A run with no arguments lists all the available tests.
	 * @param args name of the test followed by its respective arguments. Run with only
	 * the test name to find out these arguments.
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			getInstance().printList();
		}
		else {
			TestInfo test = getInstance().findTest(args[0]);
			if (test != null) {
				if (args.length > 1) {
					test.invoke(Arrays.copyOfRange(args, 1, args.length));
				}
				else {
					test.invoke(new String[0]);
				}
			}
			else {
				System.out.println("\nNo test with name '" + args[0] + "' available.");
				getInstance().printList();
			}
		}
	}

	private class TestInfo {

		private Class<?> mainClass;

		private String description;

		TestInfo(Class<?> mainClass, String description) {
			this.mainClass = mainClass;
			this.description = description;
		}

		@SuppressWarnings("synthetic-access")
		public void invoke(String[] args) {
			Method mainMethod;
			try {
				mainMethod = mainClass.getMethod("main", (new String[0]).getClass());
				mainMethod.invoke(null, new Object[] { args });
			}
			catch (SecurityException e) {
				LOG.error(e.getMessage(), e);
			}
			catch (NoSuchMethodException e) {
				LOG.error(e.getMessage(), e);
			}
			catch (IllegalArgumentException e) {
				LOG.error(e.getMessage(), e);
			}
			catch (IllegalAccessException e) {
				LOG.error(e.getMessage(), e);
			}
			catch (InvocationTargetException e) {
				LOG.error(e.getMessage(), e);
			}
		}

		/**
		 * @return the name of the tool
		 */
		public String getName() {
			if (mainClass == null) {
				return null;
			}
			return mainClass.getSimpleName();
		}

		/**
		 * @return the description of the test
		 */
		public String getDescription() {
			return description;
		}

		@Override
		public String toString() {
			if (mainClass == null) {
				return null;
			}
			return getName() + " - " + getDescription();
		}

	}

}
