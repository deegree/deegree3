/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.test.gui;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.deegree.test.services.wpvs.TestResultData;
import org.deegree.test.services.wpvs.WPVSStressTest;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;

/**
 * <code>StressTestController</code>
 *
 * @author <a href="mailto:ionita@deegree.org">Andrei Ionita</a>
 */
public class StressTestController extends HttpServlet {

	private static final long serialVersionUID = 1L;

	boolean testStarted = false;

	List<String> imgLinks;

	List<TestResultData> resultData;

	Boolean showImg;

	// private static Random generator;

	/**
	 *
	 */
	protected WPVSStressTest test = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public StressTestController() {
		super();
	}

	// nr of threads taken from the input form
	private int threads;

	// nr of requests ( per thread)
	private int requests;

	private class Processing implements Runnable {

		HttpSession session;

		private int threadNo;

		private int requestNo;

		private String capab;

		private Map<String, String> paramsSet;

		private boolean showImage;

		Processing(HttpSession session, int threads, int requests, String capab, Map<String, String> paramsSet,
				boolean showImg) {
			this.session = session;
			this.threadNo = threads;
			this.requestNo = requests;
			this.capab = capab;
			this.paramsSet = paramsSet;
			this.showImage = showImg;
		}

		@SuppressWarnings("unchecked")
		public void run() {
			try {
				Class<?> classname = WPVSStressTest.class;
				Constructor<?> ctor = WPVSStressTest.class.getDeclaredConstructor(int.class, int.class, String.class,
						Map.class);
				test = (WPVSStressTest) ctor.newInstance(threadNo, requestNo, capab, paramsSet);

				Method testMethod = classname.getMethod("test");
				testMethod.invoke(test);

				Method reqMethod = classname.getMethod("getResultData");
				resultData = (List<TestResultData>) reqMethod.invoke(test);

				Method imgMethod = classname.getMethod("getImgLinks");
				imgLinks = (List<String>) imgMethod.invoke(test);

				session.setAttribute("applicationState", "ready");
				session.setAttribute("resultData", resultData);
				session.setAttribute("imgLinks", imgLinks);
				session.setAttribute("showImage", showImage);

			}
			catch (SecurityException e1) {
				e1.printStackTrace();
			}
			catch (NoSuchMethodException e1) {
				e1.printStackTrace();
			}
			catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
			catch (InstantiationException e) {
				e.printStackTrace();
			}
			catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}

	}

	// /**
	// *
	// * @return a subset (comma-separated string) of randomly picked datasets
	// */
	// private static String getRandomDatasetSubset() {
	// String result = "";
	// try {
	// WPVSClient client = new WPVSClient( new URL( TestResultsStorage.getCapabilities() )
	// );
	// List<String> datasets = client.getQueryableDatasets();
	//
	// // m = total number of datasets
	// int m = datasets.size();
	//
	// if ( m > 1 ) {
	// // n = arbitrary number of datasets to be used
	// int n = generator.nextInt( m - 1 ) + 1;
	//
	// // last = previous generated position
	// int k, last = -1;
	// for ( int i = 0; i < n; i++ ) {
	// k = generator.nextInt( m - (last + 1) - (n - i) );
	// last = last + 1 + k;
	// if ( i != n - 1 )
	// result += datasets.get( last ) + ",";
	// else
	// result += datasets.get( last );
	// }
	// } else {
	// return datasets.get( 0 );
	// }
	// } catch ( MalformedURLException e ) {
	// e.printStackTrace();
	// }
	// return result;
	// }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String action = request.getParameter("action");
		String step1 = request.getParameter("step1");
		String step2 = request.getParameter("step2");
		String step3 = request.getParameter("step3");

		String imageSource = request.getParameter("imgsrc");
		String bigimg = request.getParameter("bigimg");
		String shot = request.getParameter("shot");
		String bigshot = request.getParameter("bigshot");

		if (action != null && action.equalsIgnoreCase("status")) {
			String state = (String) request.getSession().getAttribute("applicationState");

			if (state != null && state.equalsIgnoreCase("wait")) {
				RequestDispatcher dispatcher = request.getRequestDispatcher("/status.jsp");

				HttpSession session = request.getSession();
				String appState = (String) session.getAttribute("applicationState");
				request.setAttribute("applicationState", appState);
				if (appState.equals("wait")) {
					String operation = (String) session.getAttribute("operationInProgress");
					request.setAttribute("opeartionInProgress", operation);
				}

				List<TestResultData> partialRes = test.getResultData();
				request.setAttribute("partialResults", partialRes);

				request.setAttribute("threads", threads);
				request.setAttribute("requests", requests);

				dispatcher.forward(request, response);

			}
			else if (state != null && state.equalsIgnoreCase("ready")) {
				RequestDispatcher dispatcher = request.getRequestDispatcher("/status.jsp");

				HttpSession session = request.getSession();

				request.setAttribute("applicationState", "ready");

				resultData = (List<TestResultData>) session.getAttribute("resultData");
				request.setAttribute("resultData", resultData);

				imgLinks = (List<String>) session.getAttribute("imgLinks");
				request.setAttribute("imgLinks", imgLinks);

				showImg = (Boolean) session.getAttribute("showImage");
				request.setAttribute("showImage", showImg);

				request.setAttribute("threads", threads);
				request.setAttribute("requests", requests);

				dispatcher.forward(request, response);

			}
			else {
				RequestDispatcher dispatcher = request.getRequestDispatcher("/status.jsp");
				request.setAttribute("applicationState", "welcome");
				dispatcher.forward(request, response);
			}

		}
		else if (action != null && action.equalsIgnoreCase("wpvs")) {
			RequestDispatcher dispatcher = request.getRequestDispatcher("/wpvs.jsp");
			dispatcher.forward(request, response);

		}
		else if (action != null && action.equalsIgnoreCase("wms")) {
			RequestDispatcher dispatcher = request.getRequestDispatcher("/wms.jsp");
			dispatcher.forward(request, response);

		}
		else if (step1 != null) {

			doStep1(request, response);

		}
		else if (step2 != null) {

			doStep2(request, response);

		}
		else if (step3 != null) {

			doStep3(request, response);

		}
		else if (imageSource != null) {

			drawDiagram(response, 400, 300);

		}
		else if (bigimg != null) {

			drawDiagram(response, 1000, 750);

		}
		else if (shot != null) {

			renderResizedImage(response, shot, 200, 150);

		}
		else if (bigshot != null) {

			renderImage(response, bigshot);
		}

	}

	private void renderImage(HttpServletResponse response, String bigshot) throws IOException {
		BufferedImage originalImage = ImageIO.read(new File(bigshot));

		response.setContentType("image/jpeg");
		OutputStream out = response.getOutputStream();
		ImageIO.write(originalImage, "jpg", out);

	}

	private void renderResizedImage(HttpServletResponse response, String shot, int width, int height)
			throws IOException {
		BufferedImage originalImage = ImageIO.read(new File(shot));

		BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = scaledImage.createGraphics();
		graphics.setComposite(AlphaComposite.Src);
		graphics.drawImage(originalImage, 0, 0, width, height, null);
		graphics.dispose();

		response.setContentType("image/jpeg");
		OutputStream out = response.getOutputStream();
		ImageIO.write(scaledImage, "jpg", out);
		out.close();

	}

	private void drawDiagram(HttpServletResponse response, int width, int height) throws IOException {

		int n = resultData.size();
		double[] values = new double[n];
		for (int i = 0; i < n; i++)
			values[i] = resultData.get(i).getTimeElapsed() / 1000.0;

		HistogramDataset dataset = new HistogramDataset();
		dataset.addSeries(new Double(1.0), values, n);

		JFreeChart chart = ChartFactory.createHistogram("timeVSfreq", "time", "frequency", dataset,
				PlotOrientation.VERTICAL, true, true, true);

		ChartRenderingInfo info = new ChartRenderingInfo();
		BufferedImage buf = chart.createBufferedImage(width, height, 1, info);

		response.setContentType("image/jpeg");
		OutputStream out = response.getOutputStream();
		ImageIO.write(buf, "jpg", out);
		out.close();

	}

	private void doStep1(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String capab = request.getParameter("capabilities");
		request.getSession().setAttribute("capab", capab);
		RequestDispatcher dispatcher = request.getRequestDispatcher("/wpvs_params.jsp");
		dispatcher.forward(request, response);

	}

	@SuppressWarnings("unchecked")
	private void doStep3(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		threads = Integer.valueOf(request.getParameter("threadNo"));
		requests = Integer.valueOf(request.getParameter("requestNo"));
		String imgornot = request.getParameter("imgornot");
		boolean showImage = false;
		if (imgornot != null && imgornot.equals("displayimg"))
			showImage = true;

		// process the request asynchronously
		String capab = (String) request.getSession().getAttribute("capab");
		Map<String, String> paramsSet = (HashMap<String, String>) request.getSession().getAttribute("paramsSet");

		Thread t = new Thread(new Processing(request.getSession(), threads, requests, capab, paramsSet, showImage));
		t.start();

		RequestDispatcher dispatcher = request.getRequestDispatcher("/status.jsp");

		HttpSession session = request.getSession();

		// set session attributes
		session.setAttribute("applicationState", "wait");
		session.setAttribute("operationInProgress", "WPVS");

		// send attributes to jsp page
		request.setAttribute("applicationState", "wait");
		request.setAttribute("operationInProgress", "WPVS");
		dispatcher.forward(request, response);

	}

	private void doStep2(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Map<String, String> paramsSet = new HashMap<String, String>();
		String[] datasets = request.getParameterValues("datasets");
		if (datasets != null) {
			String datasetsStr = "";
			for (int i = 0; i < datasets.length; i++)
				datasetsStr = datasetsStr + "," + datasets[i];
			paramsSet.put("datasets", datasetsStr);
		}

		String elevModel = request.getParameter("elevModel");
		if (elevModel != null && elevModel.length() > 0)
			paramsSet.put("elevModel", elevModel);

		String pitch = request.getParameter("pitch");
		if (pitch != null && pitch.length() > 0)
			paramsSet.put("pitch", pitch);

		String yaw = request.getParameter("yaw");
		if (yaw != null && yaw.length() > 0)
			paramsSet.put("yaw", yaw);

		String roll = request.getParameter("roll");
		if (roll != null && roll.length() > 0)
			paramsSet.put("roll", roll);

		String distance = request.getParameter("distance");
		if (distance != null && distance.length() > 0)
			paramsSet.put("distance", distance);

		String aov = request.getParameter("aov");
		if (aov != null && aov.length() > 0)
			paramsSet.put("aov", aov);

		String clipping = request.getParameter("clipping");
		if (clipping != null && clipping.length() > 0)
			paramsSet.put("clipping", clipping);

		String crs = request.getParameter("crs");
		if (crs != null && crs.length() > 0)
			paramsSet.put("crs", crs);

		String width = request.getParameter("width");
		if (width != null && width.length() > 0)
			paramsSet.put("width", width);

		String height = request.getParameter("height");
		if (height != null && height.length() > 0)
			paramsSet.put("height", height);

		String scale = request.getParameter("scale");
		if (scale != null && scale.length() > 0)
			paramsSet.put("scale", scale);

		request.getSession().setAttribute("paramsSet", paramsSet);

		RequestDispatcher dispatcher = request.getRequestDispatcher("/wpvs_threads.jsp");
		dispatcher.forward(request, response);
	}

}
