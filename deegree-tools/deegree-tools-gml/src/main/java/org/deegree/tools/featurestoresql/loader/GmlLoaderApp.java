/*-
 * #%L
 * deegree-cli-utility
 * %%
 * Copyright (C) 2016 - 2021 lat/lon GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.deegree.tools.featurestoresql.loader;

import org.deegree.tools.featurestoresql.CommonConfiguration;
import org.deegree.tools.featurestoresql.JobRepositoryConfiguration;
import org.deegree.tools.featurestoresql.SubcommandApp;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Entry point of the command line interface of GmlLoader.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GmlLoaderApp extends SubcommandApp {

	public static void run(String[] args) throws Exception {
		if (isHelpRequested(args)) {
			GmlLoaderHelpUsage.printUsage();
		}
		else if (args.length < 4) {
			printUnexpectedNumberOfParameters(args);
			GmlLoaderHelpUsage.printUsage();
		}
		else {
			ApplicationContext applicationContext = new AnnotationConfigApplicationContext(
					JobRepositoryConfiguration.class, CommonConfiguration.class, GmlLoaderConfiguration.class);
			runJob(args, applicationContext);
		}
	}

	private static void printUnexpectedNumberOfParameters(String[] args) {
		System.out.println("Number of arguments is invalid, must be at least three but was " + (args.length - 1));
		System.out.println();
	}

}
