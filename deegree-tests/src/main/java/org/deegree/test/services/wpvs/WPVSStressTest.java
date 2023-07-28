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

package org.deegree.test.services.wpvs;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.deegree.commons.utils.Pair;
import org.deegree.protocol.wpvs.WPVSConstants.WPVSRequestType;
import org.deegree.protocol.wpvs.client.WPVSClient;
import org.deegree.tools.CommandUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>WPVSStressTest</code> class generates the requests for the WPVSClient. The
 * current command-line parameters are:
 * <ul>
 * <li><b>capabilities</b> for the capabilities URL</li>
 * <li><b>threads</b> for the number of threads</li>
 * <li><b>requests</b> for the number of requests to be sent by each thread</li>
 * </ul>
 *
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 *
 */
public class WPVSStressTest {

	private final static Logger LOG = LoggerFactory.getLogger(WPVSStressTest.class);

	private int threads;

	private int requests;

	private String capabilities;

	private StringBuffer info;

	private WPVSClient client;

	private List<TestResultData> resultData;

	private List<String> imgLinks;

	private Map<String, String> paramsSet;

	// writer to a log file that describes how the tests ran (with statistics)
	private PrintWriter logWriter;

	public WPVSStressTest(int threads, int requests, String capabilities, Map<String, String> paramsSet) {
		this.requests = requests;
		this.threads = threads;
		this.capabilities = capabilities;
		this.paramsSet = paramsSet;
		System.out.println("the datasets received by the stressTest:" + paramsSet.get("datasets"));

		try {
			client = new WPVSClient(new URL(capabilities));
		}
		catch (MalformedURLException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	/**
	 * The <code>WPVSSender</code> class generates the requests to deegree3 WPVS in a
	 * thread
	 *
	 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
	 *
	 */
	private class WPVSSender implements Runnable {

		private int threadNo;

		// BBOX default values
		private int LOW_X = 423750;

		private int LOW_Y = 4512700;

		private int HIGH_X = 425500;

		private int HIGH_Y = 4513900;

		WPVSSender(int threadNo) {
			this.threadNo = threadNo;
		}

		/**
		 * @return the URL constructed with random values for the getView parameters
		 */
		private URL getGetViewURL() {
			Random generator = new Random();

			String stringURL = client.getAddress(WPVSRequestType.GetView, true);
			stringURL += "service=WPVS&request=GetView";

			// including a valid BBOX because it is required (although the actual values
			// are not currently evaluated --
			// 26 Jun 2009)
			stringURL += "&BOUNDINGBOX=" + LOW_X + "," + LOW_Y + "," + HIGH_X + "," + HIGH_Y;

			List<String> datasets;
			if (paramsSet.containsKey("datasets")) {
				String datasetsStr = paramsSet.get("datasets");
				String[] datasetsAr = datasetsStr.split(",");
				datasets = Arrays.asList(datasetsAr);
			}
			else {
				// get the list of all (usable) datasets
				datasets = client.getQueryableDatasets();
			}
			Iterator<String> it = datasets.iterator();
			stringURL += "&DATASETS=";
			if (it.hasNext())
				stringURL += it.next();
			while (it.hasNext())
				stringURL += "," + it.next();

			String elevModel;
			if (paramsSet.containsKey("elevModel"))
				elevModel = paramsSet.get("elevModel");
			else {
				List<String> models = client.getElevationModels();
				int randomIndex = generator.nextInt(models.size());
				elevModel = models.get(randomIndex);
			}
			stringURL += "&ELEVATIONMODEL=" + elevModel;

			String pitch;
			if (paramsSet.containsKey("pitch")) {
				pitch = paramsSet.get("pitch");
				if (pitch.contains("_")) {
					String[] interval = pitch.split("_");
					int pitch1 = Integer.parseInt(interval[0]);
					int pitch2 = Integer.parseInt(interval[1]);
					pitch = String.valueOf(generator.nextInt(pitch2 - pitch1 + 1) + pitch1);
				}
			}
			else {
				int sign = generator.nextInt(2);
				pitch = String.valueOf(generator.nextInt(90));
				if (sign == 1)
					pitch = "-" + pitch;
			}
			stringURL += "&PITCH=" + pitch;

			String yaw;
			if (paramsSet.containsKey("yaw")) {
				yaw = paramsSet.get("yaw");
				if (yaw.contains("_")) {
					String[] interval = yaw.split("_");
					int yaw1 = Integer.parseInt(interval[0]);
					int yaw2 = Integer.parseInt(interval[1]);
					yaw = String.valueOf(generator.nextInt(yaw2 - yaw1 + 1) + yaw1);
				}
			}
			else
				yaw = String.valueOf(generator.nextInt());
			stringURL += "&YAW=" + yaw;

			String roll;
			if (paramsSet.containsKey("roll")) {
				roll = paramsSet.get("roll");
				if (roll.contains("_")) {
					String[] interval = roll.split("_");
					int roll1 = Integer.parseInt(interval[0]);
					int roll2 = Integer.parseInt(interval[1]);
					roll = String.valueOf(generator.nextInt(roll2 - roll1 + 1) + roll1);
				}
			}
			else
				roll = String.valueOf(generator.nextInt());
			stringURL += "&ROLL=" + roll;

			String aov;
			if (paramsSet.containsKey("aov")) {
				aov = paramsSet.get("aov");
				if (aov.contains("_")) {
					String[] interval = aov.split("_");
					int aov1 = Integer.parseInt(interval[0]);
					int aov2 = Integer.parseInt(interval[1]);
					aov = String.valueOf(generator.nextInt(aov2 - aov1 + 1) + aov1);
				}
			}
			else
				aov = String.valueOf(generator.nextInt(181));
			stringURL += "&AOV=" + aov;

			if (paramsSet.containsKey("clipping"))
				stringURL += "&FARCLIPPINGPLANE=" + paramsSet.containsKey("clipping");
			else
				stringURL += "&FARCLIPPINGPLANE=10000";

			String width = "800";
			if (paramsSet.containsKey("width"))
				width = paramsSet.get("width");
			stringURL += "&WIDTH=" + width;

			String height = "600";
			if (paramsSet.containsKey("height"))
				height = paramsSet.get("height");
			stringURL += "&HEIGHT=" + height;

			String scale;
			if (paramsSet.containsKey("scale"))
				scale = paramsSet.get("scale");
			else
				scale = String.valueOf(generator.nextFloat());
			stringURL += "&SCALE=" + scale;

			stringURL += "&STYLES=default";
			stringURL += "&DATETIME=2007-03-21T12:00:00";
			stringURL += "&EXCEPTIONFORMAT=INIMAGE";
			stringURL += "&VERSION=" + "1.0.0";
			stringURL += "&OUTPUTFORMAT=" + "image/png";
			stringURL += "&BACKGROUND=" + "cirrus";

			Integer[] poiValues = new Integer[3];
			String crs = null;
			if (paramsSet.containsKey("poi")) {
				String poiStr = paramsSet.get("poi");
				String[] poiArray = poiStr.split(",");
				if (poiArray.length >= 3) {
					for (int index = 0; index <= 2; index++) {
						if (poiArray[index].contains("_")) {
							String[] interval = poiArray[0].split("_");
							float interval1 = Float.parseFloat(interval[0]);
							float interval2 = Float.parseFloat(interval[1]);
							poiValues[index] = generator.nextInt(Math.round(interval2 - interval1))
									+ Math.round(interval1);
						}
						else
							poiValues[index] = Math.round(Float.parseFloat(poiArray[0]));
					}
					if (poiArray.length == 4)
						crs = poiArray[3];
				}
			}
			else {
				poiValues[0] = 2568000 + generator.nextInt(2600768 - 2568000);
				poiValues[1] = 5606000 + generator.nextInt(5638768 - 5606000);
				poiValues[2] = 34 + generator.nextInt(400 - 34);
			}
			stringURL += "&POI=" + String.valueOf(poiValues[0]) + "," + String.valueOf(poiValues[1]) + ","
					+ String.valueOf(poiValues[2]);
			stringURL += "&CRS=" + crs;

			String distance;
			if (paramsSet.containsKey("distance")) {
				distance = paramsSet.get("distance");
				if (distance.contains("_")) {
					String[] interval = distance.split("_");
					int distance1 = Integer.parseInt(interval[0]);
					int distance2 = Integer.parseInt(interval[1]);
					distance = String.valueOf(generator.nextInt(distance2 - distance1 + 1) + distance1);
				}
			}
			else
				distance = String.valueOf(generator.nextInt(1000));
			stringURL += "&DISTANCE=" + distance;

			try {
				return new URL(stringURL);
			}
			catch (MalformedURLException e) {
				LOG.error(e.getMessage(), e);
				return null;
			}
		}

		public void run() {
			for (int i = 0; i < requests; i++) {
				URL url = getGetViewURL();
				LOG.info("URL: " + url);
				long startTime = System.currentTimeMillis();
				Pair<BufferedImage, String> response = null;
				try {
					response = client.getView(url);
				}
				catch (IOException e) {
					LOG.error(e.getLocalizedMessage(), e);
				}
				if (response != null) {
					long currentTime = System.currentTimeMillis();

					// store obtained info
					long elapsed = currentTime - startTime;
					resultData.add(new TestResultData(threadNo, i, elapsed));

					// write into log file
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
					Date date = new Date();
					logWriter.print(dateFormat.format(date) + "     " + elapsed);

					try {
						if (response.first != null) {
							File tempFile = File.createTempFile("wpvs", ".jpg");
							ImageIO.write(response.first, "jpg", tempFile);
							imgLinks.add(tempFile.getPath());

							logWriter.println("     1     " + tempFile.getName() + "     " + url);

						}
						else {
							logWriter.println("     0 ");
							// File tempFile = File.createTempFile( "wpvsOutXML", ".xml"
							// );
							// BufferedWriter writer = new BufferedWriter( new FileWriter(
							// tempFile) );
							// writer.write( response.second );
							// info =
							// new StringBuffer( "<a href=file://" + tempFile.getPath() +
							// ">xml file</a>" );
							// imgLinks.add( info.toString() );
						}

					}
					catch (IOException e) {
						LOG.error(e.getMessage(), e);
					}
				}
			}
		}

	}

	public List<TestResultData> getResultData() {
		return resultData;
	}

	public List<String> getImgLinks() {
		return imgLinks;
	}

	public void test() throws IOException {
		resultData = new ArrayList<TestResultData>();
		imgLinks = new ArrayList<String>();

		List<Thread> threadList = new LinkedList<Thread>();
		for (int i = 1; i <= threads; i++) {
			Thread t = new Thread(new WPVSSender(i));
			t.start();
			threadList.add(t);
		}

		// prepare Log file
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
		Date date = new Date();
		String currentDate = dateFormat.format(date);
		logWriter = new PrintWriter(new BufferedWriter(new FileWriter("/tmp/" + currentDate + "-WPVS-stress.log")));
		logWriter.println("START_DATE      START_TIME      ELAPSED       SUCCESS       " + "IMAGEFILE       URL");

		try {
			for (Thread t : threadList)
				while (t.isAlive())
					t.join();
		}
		catch (InterruptedException e) {
			LOG.error(e.getMessage(), e);
		}

		logWriter.close();
	}

	/**
	 * @return command-line options object
	 */
	private static Options initOptions() {
		Options opts = new Options();

		// Mandatory parameters
		Option opt = new Option("capabilities", true,
				"getCapabilites URL for the deegee3 WPVS; Please surround the URL with double quotes!");
		opt.setRequired(true);
		opts.addOption(opt);

		opt = new Option("threads", true, "number of Threads that will send requests to the deegee3 WPVS");
		opt.setRequired(true);
		opts.addOption(opt);

		opt = new Option("requests", true, "number of getView Requests PER Thread will sent to the deegee3 WPVS");
		opt.setRequired(true);
		opts.addOption(opt);

		// Optional parameters
		opt = new Option("datasets", true,
				"specify a list of datasets in " + "comma-separated format; by default value all datasets are used");
		opt.setRequired(false);
		opts.addOption(opt);

		opt = new Option("elevModel", true, "specify an elevation-model; default is " + "a random elevation-model");
		opt.setRequired(false);
		opts.addOption(opt);

		opt = new Option("width", true, "specify a width value for the resulting images; default is 800");
		opt.setRequired(false);
		opts.addOption(opt);

		opt = new Option("height", true, "specify a height value for the resulting images; default is 600");
		opt.setRequired(false);
		opts.addOption(opt);

		opt = new Option("pitch", true,
				"specify a Pitch value or interval " + "(using an underscore '_') between -90 and 90;"
						+ " unit of measure is degrees; the pitch is the "
						+ "angle that the direction of view makes with the Earth surface");
		opt.setRequired(false);
		opts.addOption(opt);

		opt = new Option("yaw", true, "specify a Yaw value or inverval " + "(using an underscore '_')");
		opt.setRequired(false);
		opts.addOption(opt);

		opt = new Option("roll", true, "specify a Roll value or interval " + "(using an underscore '_')");
		opt.setRequired(false);
		opts.addOption(opt);

		opt = new Option("distance", true, "specify a distance to the point of interest;"
				+ " an integer value or an interval (separated by underscore) are " + "valid inputs");
		opt.setRequired(false);
		opts.addOption(opt);

		opt = new Option("aov", true, "specify an Angel-of-View value or an interval "
				+ "(using an underscode '_') between 0 and 180 (measured in degrees)");
		opt.setRequired(false);
		opts.addOption(opt);

		// currently the Bounding Box is not evaluated; any valid description (xMin<xMax
		// && yMin<yMax)
		// yields an image with having the maximum BBox coverage
		// opt = new Option( "bbox", true, "specify a fixed Bounding Box - in the format
		// \"xLow, yLow, xHigh, yHigh\" "
		// );
		// opt.setRequired( false );
		// opts.addOption( opt );

		opt = new Option("poi", true,
				"specify a point-of-interest; valid inputs are" + " 'x, y, z, crs', in which x, y and z can be fixed "
						+ "floating-point values or intervals specified using an underscore"
						+ " e.g. xMin_xMax; the crs value can be omitted ");
		opt.setRequired(false);
		opts.addOption(opt);

		opt = new Option("scale", true,
				"specify a fixed scale for all tests; " + "by default scale values will be randomized ");
		opt.setRequired(false);
		opts.addOption(opt);

		opt = new Option("clipping", true, "specify a far-clipping-plane value, i.e "
				+ " a maximum distance over which dataset is rendered; default is 10000");
		opt.setRequired(false);
		opts.addOption(opt);

		opts.addOption("?", "help", false, "print (this) usage information");

		return opts;
	}

	/**
	 * @param options
	 */
	private static void printHelp(Options options) {
		CommandUtils.printHelp(options, WPVSStressTest.class.getSimpleName(), "", "");
	}

	/**
	 * @param args mandatory arguments: -capabilities, -requests, -threads. To see the
	 * others, provide the argument 'help'.
	 * @throws Exception when the execution is aborted on the base of a wrong argument (or
	 * help was invoked),
	 */
	public static void main(String args[]) throws Exception {
		Options options = initOptions();

		if (args != null && args.length > 0)
			if (args[0].contains("help") || args[0].contains("?")) {
				printHelp(options);
				throw new Exception("Help argument was provided. Argument support was shown.");
			}

		try {
			new PosixParser().parse(options, args);

			int noThreads = Integer.valueOf(options.getOption("threads").getValue());
			int noRequests = Integer.valueOf(options.getOption("requests").getValue());
			String getCapabilities = options.getOption("capabilities").getValue();

			Map<String, String> paramsSet = new HashMap<String, String>();
			String datasets = options.getOption("datasets").getValue();
			if (datasets != null && datasets.length() > 0)
				paramsSet.put("datasets", datasets);

			String elevModel = options.getOption("elevModel").getValue();
			if (elevModel != null && elevModel.length() > 0)
				paramsSet.put("elevModel", elevModel);

			String width = options.getOption("width").getValue();
			if (width != null) {
				Integer.parseInt(width);
				paramsSet.put("width", width);
			}

			String height = options.getOption("height").getValue();
			if (height != null) {
				Integer.parseInt(height);
				paramsSet.put("height", height);
			}

			String pitch = options.getOption("pitch").getValue();
			if (pitch != null) {
				checkIntegerOrBoundInterval(pitch, "pitch", -90, 90);
				paramsSet.put("pitch", pitch);
			}

			String yaw = options.getOption("yaw").getValue();
			if (yaw != null) {
				checkIntegerOrInterval(yaw, "yaw");
				paramsSet.put("yaw", yaw);
			}

			String roll = options.getOption("roll").getValue();
			if (roll != null) {
				checkIntegerOrInterval(roll, "roll");
				paramsSet.put("roll", roll);
			}

			String distance = options.getOption("distance").getValue();
			if (distance != null) {
				checkIntegerOrInterval(distance, "distance");
				paramsSet.put("distance", distance);
			}

			String aov = options.getOption("aov").getValue();
			if (aov != null) {
				checkIntegerOrBoundInterval(aov, "aov", 0, 180);
				paramsSet.put("aov", aov);
			}

			String poi = options.getOption("poi").getValue();
			String crs = null;
			if (poi != null) {
				String[] values = poi.split(",");
				if (values.length == 3) {
					checkFloatOrInterval(values[0], "poi, x-value");
					checkFloatOrInterval(values[1], "poi, y-value");
					checkFloatOrInterval(values[2], "poi, z-value");
				}
				else if (values.length == 4) {
					checkFloatOrInterval(values[0], "poi, x-value");
					checkFloatOrInterval(values[1], "poi, y-value");
					checkFloatOrInterval(values[2], "poi, z-value");
					crs = values[3];
				}
				else {
					System.err.println("ERROR: The poi argument must have 3 or 4 " + "comma-separated values");
				}
				paramsSet.put("poi", poi);

			}

			String scale = options.getOption("scale").getValue();
			if (scale != null && scale.length() > 0)
				paramsSet.put("scale", scale);

			String clipping = options.getOption("clipping").getValue();
			if (clipping != null) {
				Integer.parseInt(clipping);
				paramsSet.put("clipping", clipping);
			}

			WPVSStressTest current = new WPVSStressTest(noThreads, noRequests, getCapabilities, paramsSet);
			current.test();

		}
		catch (ParseException e) {
			System.err.println("ERROR: Invalid command line: " + e.getMessage());
		}
		catch (NumberFormatException e) {
			System.err.println("ERROR: " + e.getMessage());
		}
	}

	private static void checkFloatOrInterval(String arg, String name) throws Exception {
		if (!arg.contains("_")) {
			Float.parseFloat(arg);
		}
		else {
			String[] argInterval = arg.split("_");
			if (argInterval.length != 2) {
				String msg = "ERROR: The " + name
						+ " interval should be two floating point numbers separated by an underscore ('_') ";
				LOG.error(msg);
				throw new Exception(msg);
			}

			float a = Float.parseFloat(argInterval[0]);
			float b = Float.parseFloat(argInterval[1]);
			if (a >= b) {
				String msg = "ERROR: The " + name + " interval should consist of two numbers a_b with a < b";
				LOG.error(msg);
				throw new Exception(msg);
			}
		}
	}

	private static void checkIntegerOrInterval(String arg, String name) throws Exception {
		if (!arg.contains("_")) {
			Integer.parseInt(arg);
		}
		else {
			String[] argInterval = arg.split("_");
			if (argInterval.length != 2) {
				String msg = "ERROR: The " + name
						+ " interval should be two integers separated by an underscore ('_') ";
				LOG.error(msg);
				throw new Exception(msg);
			}

			int a = Integer.parseInt(argInterval[0]);
			int b = Integer.parseInt(argInterval[1]);
			if (a >= b) {
				String msg = "ERROR: The " + name + " interval should consist of two numbers a_b with a < b";
				LOG.error(msg);
				throw new Exception(msg);
			}
		}
	}

	private static void checkIntegerOrBoundInterval(String arg, String name, int lowEnd, int highEnd) throws Exception {
		if (!arg.contains("_")) {
			int argInt = Integer.parseInt(arg);
			if (argInt < lowEnd || argInt > highEnd) {
				String msg = "ERROR: The " + name + " value should be between " + lowEnd + " and " + highEnd;
				LOG.error(msg);
				throw new Exception(msg);
			}
		}
		else {
			String[] argInterval = arg.split("_");
			if (argInterval.length != 2) {
				String msg = "ERROR: The " + name
						+ " interval should be two integers separated by an underscore ('_') ";
				LOG.error(msg);
				throw new Exception(msg);
			}

			int arg1 = Integer.parseInt(argInterval[0]);
			if (arg1 < lowEnd || arg1 > highEnd) {
				String msg = "ERROR: The " + name + " interval values should be " + "between " + lowEnd + " and "
						+ highEnd;
				LOG.error(msg);
				throw new Exception(msg);
			}

			int arg2 = Integer.parseInt(argInterval[1]);
			if (arg2 < lowEnd || arg2 > highEnd) {
				String msg = "ERROR: The " + name + " interval values should be " + "between " + lowEnd + " and "
						+ highEnd;
				LOG.error(msg);
				throw new Exception(msg);
			}

			if (arg1 >= arg2) {
				String msg = "ERROR: The " + name + " interval should consist of two numbers a_b with a < b";
				LOG.error(msg);
				throw new Exception(msg);
			}
		}
	}

}
