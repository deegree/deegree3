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

import java.util.HashSet;
import java.util.Set;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameter;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.context.ApplicationContext;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public abstract class SubcommandApp {

	protected static void runJob(String[] args, ApplicationContext applicationContext) throws Exception {
		JobOperator jobLauncher = applicationContext.getBean(JobOperator.class);
		Job job = applicationContext.getBean(Job.class);
		Set<JobParameter<?>> jobParams = createJobParams(args);
		jobLauncher.run(job, new JobParameters(jobParams));
	}

	private static Set<JobParameter<?>> createJobParams(String[] args) {
		Set<JobParameter<?>> jobParams = new HashSet<>();
		for (String arg : args) {
			if (arg.startsWith("-")) {
				int firstIndex = arg.startsWith("-- ") ? 2 : 1;
				String key = arg.substring(firstIndex, arg.indexOf("="));
				if (arg.contains("=")) {
					String value = arg.substring(arg.indexOf("=") + 1);
					jobParams.add(new JobParameter<>(key, value, String.class));
				}
				else {
					jobParams.add(new JobParameter<>(key, true, Boolean.class));
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
