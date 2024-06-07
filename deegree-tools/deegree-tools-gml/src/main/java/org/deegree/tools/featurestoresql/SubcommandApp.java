/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2024 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.tools.featurestoresql;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationContext;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public abstract class SubcommandApp {

	protected static void runJob(String[] args, ApplicationContext applicationContext) throws Exception {
		JobLauncher jobLauncher = applicationContext.getBean(JobLauncher.class);
		Job job = applicationContext.getBean(Job.class);
		Map<String, JobParameter<?>> jobParams = createJobParams(args);
		jobLauncher.run(job, new JobParameters(jobParams));
	}

	private static Map<String, JobParameter<?>> createJobParams(String[] args) {
		Map<String, JobParameter<?>> jobParams = new HashMap<>();
		for (String arg : args) {
			if (arg.startsWith("-")) {
				int firstIndex = arg.startsWith("-- ") ? 2 : 1;
				String key = arg.substring(firstIndex, arg.indexOf("="));
				if (arg.contains("=")) {
					String value = arg.substring(arg.indexOf("=") + 1);
					jobParams.put(key, new JobParameter<>(value, String.class));
				}
				else {
					jobParams.put(key, new JobParameter<>(true, Boolean.class));
				}
			}
		}
		return jobParams;
	}

	protected static boolean isHelpRequested(String[] args) {
		return args.length == 1
				|| (args.length > 1 && ("--help".equals(args[1]) || "-help".equals(args[1]) || "-h".equals(args[1])));
	}

}
