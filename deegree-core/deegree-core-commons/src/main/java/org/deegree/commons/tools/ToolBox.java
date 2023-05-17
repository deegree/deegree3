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

package org.deegree.commons.tools;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.deegree.commons.annotations.Tool;
import org.deegree.commons.utils.DeegreeAALogoUtils;
import org.reflections.Reflections;

/**
 * Allows for convenient starting and listing of available deegree command line tools.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class ToolBox {

	private final int NAME_PAD = 3;

	private final int DESCRIPTION_PAD = 5;

	private final int TEXT_WIDTH = 80;

	private ToolInfo[] tools;

	ToolBox(Set<Class<?>> tools) {
		this.tools = new ToolInfo[tools.size()];
		int i = 0;
		for (Class<?> cls : tools) {
			this.tools[i++] = new ToolInfo(cls);
		}
	}

	/**
	 * The list of tools is printed in a readable format
	 */
	private void printList() {
		DeegreeAALogoUtils.print(System.out);
		System.out.println(
				"\nAvailable tools, start with 'd3toolbox <tool> <args>' or omit the args to see all available parameters of the tool:\n");

		// determine the maximum length of a tool name
		int maxLength = -1;
		for (ToolInfo tool : tools) {
			if (tool.getName().length() > maxLength) {
				maxLength = tool.getName().length();
			}
		}

		for (ToolInfo tool : tools) {
			if (tool != null) {
				System.out.print(createPadding(NAME_PAD));
				System.out.print(tool.getName());

				// pad line up to maxLength
				System.out.print(createPadding(maxLength - tool.getName().length()));

				System.out.print(createPadding(DESCRIPTION_PAD));

				printWrappedText(tool.getDescription(), maxLength);
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

	private String createPadding(int padLength) {
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < padLength; i++) {
			s.append(" ");
		}
		return s.toString();
	}

	private ToolInfo findTool(String toolName) {
		ToolInfo tool = null;
		for (ToolInfo toolInfo : tools) {
			if (toolInfo.getName().equals(toolName)) {
				tool = toolInfo;
				break;
			}
		}
		return tool;
	}

	/**
	 * @param args
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static void main(String[] args) throws SecurityException, NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		Reflections reflections = new Reflections("org.deegree");
		Set<Class<?>> tools = reflections.getTypesAnnotatedWith(Tool.class);

		ToolBox toolbox = new ToolBox(tools);

		if (args.length == 0) {
			toolbox.printList();
		}
		else {
			if ("-update".equals(args[0])) {
				buildTestSuite(toolbox.getClass());
			}
			else {
				ToolInfo tool = toolbox.findTool(args[0]);
				if (tool != null) {
					if (args.length > 1) {
						tool.invoke(Arrays.copyOfRange(args, 1, args.length));
					}
					else {
						tool.invoke(new String[0]);
					}
				}
				else {
					System.out.println("\nNo tool with name '" + args[0] + "' available.");
					toolbox.printList();
				}
			}
		}
	}

	private static void buildTestSuite(Class<?> toolbox) {
		List<String> tools = new LinkedList<String>();
		List<String> mains = new LinkedList<String>();
		try {
			Class<?> test = Class.forName(toolbox.getName());
			URL resource = test.getResource(toolbox.getSimpleName() + ".class");
			if (resource == null) {
				System.err.println("Could not load: " + toolbox.getSimpleName() + " this is akward");
			}
			else {
				File f = new File(resource.toURI());
				String parent = f.getParent();
				f = new File(parent);
				if (f.exists()) {
					System.out.println(f.getAbsolutePath() + " exists trying to load classes from: " + parent);
					findAndAddClasses(parent, tools, mains, f, new CustomFileFilter());
				}
				else {
					System.err.println(f.getAbsolutePath() + " does not denote the root directory of deegree tools.");
				}
			}
		}
		catch (ClassNotFoundException e) {
			System.err.println(e.getMessage());
		}
		catch (URISyntaxException e) {
			System.err.println(e.getMessage());
		}
		StringBuilder sb = new StringBuilder("Current tools:\n");
		sb.append("private final ToolInfo[] tools = {\n");
		Collections.sort(tools, new Comparator<String>() {
			public int compare(String first, String second) {
				String fSub = first.substring(first.lastIndexOf("."));
				String sSub = second.substring(second.lastIndexOf("."));
				return fSub.compareTo(sSub);
			}
		});

		int i = 0;
		for (String s : tools) {
			sb.append("new ToolInfo( ").append(s).append(".class").append(" )");
			if (++i < tools.size()) {
				sb.append(",\n");
			}
		}

		sb.append(
				",\n\n//Following classes define public static void main methods, but do not implement the tools annotation (maybe fix them?)\n");
		i = 0;
		Collections.sort(mains);
		for (String s : mains) {
			// the toolbox is not a tool, we know!
			if (!s.equals(ToolBox.class.getName())) {
				sb.append("new ToolInfo( ").append(s).append(".class").append(" )");

				if (++i < mains.size()) {
					sb.append(",\n");
				}
			}
			else {
				++i;
			}
		}
		sb.append("\n};");

		System.out.println(sb.toString());
	}

	/**
	 * @param classes
	 * @param parent
	 */
	private static void findAndAddClasses(final String prefix, List<String> classes, List<String> mains, File parent,
			CustomFileFilter filter) {
		if (parent != null) {
			File[] sons = parent.listFiles(filter);
			for (File tmp : sons) {
				if (tmp.isDirectory()) {
					findAndAddClasses(prefix, classes, mains, tmp, filter);
				}
				else {
					String className = tmp.getAbsoluteFile().toString().substring(prefix.length() + 1);
					className = className.substring(0, className.length() - ".class".length());
					className = className.replace(File.separatorChar, '.');
					className = "org.deegree.tools." + className;
					// sometimes on windows this is the default behavior
					className = className.replace('/', '.');
					Class<?> testClass = null;
					try {
						testClass = Class.forName(className, false, ToolBox.class.getClassLoader());
					}
					catch (ClassNotFoundException cnfe) {
						System.err.println(cnfe.getLocalizedMessage());
					}
					if (testClass != null) {
						boolean isTool = true;

						Method[] methods = testClass.getMethods();
						isTool = false;

						for (Method m : methods) {
							if (m != null) {
								if ("main".equals(m.getName())
										&& (((m.getModifiers() & Modifier.PUBLIC) == Modifier.PUBLIC)
												&& ((m.getModifiers() & Modifier.STATIC) == Modifier.STATIC))
										&& (m.getReturnType().equals(void.class))) {

									isTool = true;
									break;
								}
							}
						}
						if (isTool) {
							try {
								isTool = (testClass.getAnnotation(Tool.class) != null);
							}
							catch (NullPointerException e) {
								isTool = false;
							}
							if (isTool) {
								classes.add(className);
							}
							else {
								mains.add(className);
							}
						}
					}

				}
			}
		}
	}

	/**
	 *
	 * The <code>CustomFileFilter</code> class adds functionality to the filefilter
	 * mechanism of the JFileChooser.
	 *
	 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
	 *
	 */
	static class CustomFileFilter implements java.io.FileFilter {

		public boolean accept(File pathname) {
			if (pathname.isDirectory()) {
				return true;
			}

			String extension = getExtension(pathname);
			if (extension != null) {
				if ("class".equals(extension.trim())) {
					return true;
				}
			}
			return false;
		}

		private String getExtension(File f) {
			String ext = null;
			String s = f.getName();
			int i = s.lastIndexOf('.');

			if (i > 0 && i < s.length() - 1) {
				ext = s.substring(i + 1).toLowerCase();
			}
			return ext;
		}

	}

	private class ToolInfo {

		private Class<?> mainClass;

		private String description;

		ToolInfo(Class<?> mainClass) {
			this.mainClass = mainClass;
			try {
				Tool annotation = this.mainClass.getAnnotation(Tool.class);
				this.description = annotation.value();
			}
			catch (NullPointerException e) {
				description = "[FAILURE] Does not implement the Tool annotation, a description is therefore not available.";
			}
			try {
				this.mainClass.getMethod("main", String[].class);
			}
			catch (Exception e) {
				System.err.println("The given class: " + mainClass.getSimpleName()
						+ " does not declare a main method and can thus not be invoked.");
				this.mainClass = null;
				description = null;
			}
		}

		/**
		 * Invoke the main method of the given class.
		 * @param args
		 * @throws SecurityException
		 * @throws NoSuchMethodException
		 * @throws IllegalArgumentException
		 * @throws IllegalAccessException
		 * @throws InvocationTargetException
		 */
		public void invoke(String[] args) throws SecurityException, NoSuchMethodException, IllegalArgumentException,
				IllegalAccessException, InvocationTargetException {
			Method mainMethod = mainClass.getMethod("main", (new String[0]).getClass());
			mainMethod.invoke(null, new Object[] { args });
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
		 * @return the description of the tool
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
