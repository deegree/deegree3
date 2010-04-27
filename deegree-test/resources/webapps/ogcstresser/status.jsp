<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page
	import="
	org.jfree.data.category.DefaultCategoryDataset, 
	org.jfree.chart.*, 
	org.jfree.chart.*, 
	org.jfree.chart.plot.*, 
	org.jfree.chart.*, 
	java.awt.*, 
	java.util.Set, 
	java.util.List, 
	org.deegree.test.services.wpvs.TestResultData, 
	java.text.DecimalFormat "%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<script type="text/javascript">

function refresh(period) {
	setTimeout("location.reload(true);", period);
}
</script>

<style type="text/css">
	body {
		font-size: 16px;
		font-family: sans-serif;
	}

	body table {
		padding: 5px;
		border-collapse: separate;
		border-spacing: 20px;
	}

	#table-body table {
		width: 100%;
	}

	#diagram {
		text-align: center;
	}

	#facts {
		text-align: center;
	}

	#facts table {
		padding: 2px;
		border-collapse: separate;
		border-spacing: 5px;
	}

	#main table {
		width: auto;
		padding: 0px;
		border-collapse: separate;
		border-spacing: 10px;
	}

	#internal-table table {
		font-size: 14px;
		padding: 0px;
		border-collapse: separate;
		border-spacing: 3px;
	}
	
	#progress-bar {
		text-align: center;
	}
	
	#progress-bar table {
		border-color: black;
    	border-style: solid;
    	border-width: 1px;
    	border-collapse: collapse;
    	width: 10px;
    	text-align: center;    	    
	}
	
	#progress-bar td {
		text-align: center;
	}
	
	td.orange {
		background-color: #fe5215;
	}
	
</style>

<title>deegree Services Test Interface</title>

</head>
<body onload="JavaScript:refresh(3000);">

<%@ include file="menu.html" %>

<div id="table-body">
<table>
	<tr>
		<td>		 
			<% 
			String appState = (String) request.getAttribute( "applicationState" );
			
			if ( appState != null && appState.equals( "wait" ) ) { 
			
			    out.println( "The requests are being processed......\n" );							
				out.println( "The results will be found under " + 
				             "<a href=\"Controller?action=status\"><b>Status</b></a>." );
				out.println( "This may take some time depending on the number of requests, please be patient." );
				
				@SuppressWarnings("unchecked")
				List<TestResultData> partialRes = (List<TestResultData>) request.getAttribute( "partialResults" );
				
				Object threadsObj = request.getAttribute( "threads" );
				int threads = 0;
				if ( threadsObj != null )
					threads = (Integer) threadsObj;

				Object requestsObj = request.getAttribute( "requests" );
				int requests = 0;
				if ( requestsObj != null )
				   	requests = (Integer) requestsObj;
				
				int totalNoRequests = threads * requests;
				
				if ( partialRes != null ) {
					out.println( "<br /><br />" );
					out.println( "<p>" );
					for ( int i = 0; i < partialRes.size(); i++ ) {
					    int threadNo = partialRes.get(i).getThreadNumber();
					    int requestNo = partialRes.get(i).getRequestNumber() + 1;
					    double timeElapsed = partialRes.get(i).getTimeElapsed() / 1000.0;
					    
					    int currentRequest = i + 1; 
					    
					    out.println( "<b>Request " + requestNo + " / thread " +
					                 threadNo + "</b> .........." + 
					                 timeElapsed + " sec (" + 
	                                 currentRequest + "/" + totalNoRequests + ")<br />" );
					}
					out.println( "</p>" );
				}
								    								
				//Integer nrTests = (Integer) request.getAttribute( "totalNrTests" );
				//Integer complTests = (Integer) request.getAttribute( "completedTests" );
				//if ( complTests != null ) {
				  //  out.println( "<div id='progress-bar'>" );
				    //out.println( "<table>" );
				    //int totalUnits = 10;
				    //int usedUnits = complTests * 10 / nrTests;
				    //out.println( "<tr>" );
					//for ( int i = 0; i < totalUnits; i++ ) {
					    //if ( i < usedUnits )
					        //out.println( "<td class='orange'>&nbsp</td>" );
					    //else
					        //out.println( "<td>&nbsp</td>" );
					//}
					//out.println( "</tr>" );
					//out.println( "</table>" );
					//out.println( "</div>" );
				//}					
				//out.println();
				
			} else if ( appState != null && appState.equals( "ready" ) ) {
			    @SuppressWarnings("unchecked")
		      	List<TestResultData> resultData = (List<TestResultData>) request.getAttribute( "resultData" );
      			
			    @SuppressWarnings("unchecked")
			    List<String> imgLinks = (List<String>) request.getAttribute( "imgLinks" );
      			boolean showImg = ( Boolean ) request.getAttribute( "showImage" ); 
      			int n = imgLinks.size();                 
    
				out.println( "<div id=\"diagram\">" );
				out.println( "<a href=\"Controller?bigimg=true\"><img src=\"Controller?imgsrc=true\"></a>" );
				out.println( "</div>" );

				out.println( "<div id=\"facts\">" );
				out.println( "<table>" );
				out.println( "<tr>" );
				out.println( "<td>" );
				out.println( "<b>Average</b> time:" ); 
				long sumTime = 0;
				for ( int  i = 0; i < n; i++ ) 
	    			sumTime += resultData.get(i).getTimeElapsed(); 
     			
				DecimalFormat format1 = new DecimalFormat( "###.###" ); 
  	 			String output1 = format1.format( ( (double) ( sumTime / n ) ) / 1000 ); 
  	 			out.println( output1  + " sec" );
				out.println( "</td>" );
				out.println( "</tr>" );
				
				out.println( "<tr>" );
				out.println( "<td>" );
				out.println( "<b>Minimum</b> time:" );
		     	long min = Long.MAX_VALUE;
		     	for ( int  i = 0; i < n; i++ )
		        	if ( resultData.get(i).getTimeElapsed() < min )
		            	min = resultData.get(i).getTimeElapsed();
     			
		     	DecimalFormat format2 = new DecimalFormat( "###.###" );
	 			String output2 = format2.format( ( (double) min ) / 1000 );
  	 			out.println( output2  + " sec" );
				out.println( "</td>" );
				out.println( "</tr>" );
				
				out.println( "<tr>" );
				out.println( "<td>" );
				out.println( "<b>Maximum</b> time:" ); 
		     	long max = Long.MIN_VALUE;
		     	for ( int  i = 0; i < n; i++ )
		        	if ( resultData.get(i).getTimeElapsed() > max ) 
		        		max = resultData.get(i).getTimeElapsed();
     			
		     	DecimalFormat format3 = new DecimalFormat( "###.###" ); 
  	 			String output3 = format3.format( ( (double) max ) / 1000 );
  	 			out.println( output3  + " sec" );
				out.println( "</td>" );
				out.println( "</tr>" );
				out.println( "</table>" );
				out.println( "</div>" );

				if ( showImg ) { 
					out.println( "<div id=\"main\"><br />" );
					out.println( "<i>Note:</i> Each thumbnail is annotated with " + 
					             "(<i>thread #</i> / <i>request #</i> / <i>processing time</i>) <br />" );
					out.println( "<table>" );
					DefaultCategoryDataset dataset = new DefaultCategoryDataset();
					out.println( "<tr>" );
					//outputting five columns per row
					for ( int i = 0; i < n; i++ ) {  
      					 if ( i % 5 == 0 && i != 0)  
							out.println( "</tr><tr>" );
						
      					out.println( "<td>" );
						
      					out.println( "<div id=\"internal-table\">" );
						out.println( "<table>" );
						out.println( "<tr>" );
						out.println( "<td>" );
						out.print( resultData.get(i).getThreadNumber() + " / " + resultData.get(i).getRequestNumber() + 
					           	"/ " + ( (double) resultData.get(i).getTimeElapsed() / 1000 ) + " sec" );
						out.println( "</td>" );						
						out.println( "</tr>" );
						
						out.println( "<tr>" );
						out.println( "<td>" );
						out.println( "<a href=\"Controller?bigshot=" + imgLinks.get(i) + "\">" + 
					    	         "<img src=\"Controller?shot=" + imgLinks.get(i) + "\"></a>" );
						out.println( "</td>" );
						out.println( "</tr>" );
						out.println( "</table>" );					
						out.println( "</div>" );
						out.println( "</td>" );
						
						double timeElapsed = resultData.get(i).getTimeElapsed();
						dataset.addValue( timeElapsed, new Integer( resultData.get(i).getRequestNumber() + 1), 
					    	              new Integer( resultData.get(i).getThreadNumber() ) );
					}
					out.println( "</tr>" );
					out.println( "</table>" );
					out.println( "</div>" );
				}
			}
			%>
		</td>
	</tr>
</table>
</div>

</body>
</html>