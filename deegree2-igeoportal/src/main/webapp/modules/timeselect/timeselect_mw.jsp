<%-- $HeadURL$
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.deegree.i18n.Messages" %>
<%@ page import="java.util.Locale" %>
<%
    Locale loc = request.getLocale();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UFT-8">
        <title>Timeselect_MainWindow</title>
        <link href="../../css/deegree.css" rel="stylesheet" type="text/css" />
        <style>
            .cdate {
                position : absolute;
                left : 10px;
                top : 20px;
                width : 300px;
                height : 50px;
            }
            .ctime {
                position : absolute;
                left : 10px;
                top : 80px;
                width : 200px;
                height : 50px;
            }           
            .cLayerGroup {
                position : absolute;
                left : 10px;
                top : 130px;
                width : 200px;
                height : 50px;
            }
            .cOK {
                position : absolute;
                left : 10px;
                top : 200px;
            }
        </style>
        <link rel="stylesheet" type="text/css" href="../../javascript/ext-3.3.1/resources/css/ext-all.css" />
        <link rel="stylesheet" type="text/css" title="blue"  href="../../javascript/ext-3.3.1/resources/css/xtheme-blue.css" /> 
        <link rel="stylesheet" type="text/css" title="gray"  href="../../javascript/ext-3.3.1/resources/css/xtheme-gray.css" />
        <link rel="stylesheet" type="text/css" title="black" href="../../javascript/ext-3.3.1/resources/css/xtheme-access.css" />

        <script type="text/javascript" src="../../javascript/ext-3.3.1/adapter/ext/ext-base.js"></script>
        <script type="text/javascript" src="../../javascript/ext-3.3.1/ext-all.js"></script>
        <script language="javascript1.2" src="./codethatcalendarstd.js"></script>
        <script language="javascript1.2" src="./scroller_ex.js"></script>
        <script language="javascript1.2" src="../../javascript/utils.js"></script>
        <script language="JavaScript1.2" type="text/javascript">
        <!--
        //this function does not contain any important module code. It is for demonstration purposes only!
    	    function hint() {
    	    	alert('weatherstation values are only available from 2009-01-01 to 2009-01-07 at midnight sharp.');
    	    }
            
            function init() {
            	var date = new Date();
                
                // COMMENT OUT to initialize with current date/time.
                // setting to 2009-01-01, as the example data provide only a short time frame
                date.setUTCFullYear(2009); date.setUTCMonth(0); date.setUTCDate(1); 
                date.setUTCHours(0); date.setUTCMinutes(0);
                // END OF COMMENT OUT
                
                var sel = document.getElementById( "hours" );
                sel.options[date.getUTCHours()].selected = true;
    
                var y = date.getUTCFullYear();
                if ( y < 10 ) {
                    y = "0" + y;
                }
              	var m = date.getUTCMonth() + 1;
              	if ( m < 10 ) {
                 	m = "0" + m;
              	}
              	var d = date.getUTCDate();
              	if ( d < 10 ) {
                  	d = "0" + d;
              	}
              	document.getElementById( "date" ).value = y + "-" + m + "-" + d;
                
              	//ask, whether Timeselect is integrated or opened in new window
                var layerGroups = parent.controller.mapModel.getLayerList().layerGroups;
              	sel = document.getElementById( "layerGroup" );
              	for ( i = 0; i < layerGroups.length; i++ ) {
            	  	appendOption( sel, layerGroups[i].getServiceName(), layerGroups[i].getServiceURL(), true );
              	}
    
              	sel = document.getElementById( "minutes" );  
                // integer division            
              	sel.options[Math.floor( date.getMinutes()/10 )].selected = true;

              	initGUI();
          	}

            /**
             * initializing ExtJS GUI elements. At the moment just Ext.Button elements will be used
             * In future version this dialog will be completly reimplemented as an ExtJS to avoid
             * opening an additional browser window
             */
             function initGUI() {
                  Ext.QuickTips.init();   
                  new Ext.Button({
                      tooltip: '<%=Messages.get( loc, "IGEO_STD_BTN_SETTIME" ) %>',
                      text: '<%=Messages.get( loc, "IGEO_STD_BTN_SETTIME" ) %>',
                      renderTo: 'buttonArea0',
                      width: 100,
                      height: 25,
                      handler: function(toggled){                               
                    	  accept();
                      }
                  });

                  new Ext.Button({
                      tooltip: '<%=Messages.get( loc, "IGEO_STD_BTN_CANCEL" ) %>',
                      text: '<%=Messages.get( loc, "IGEO_STD_BTN_CANCEL" ) %>',
                      renderTo: 'buttonArea1',
                      width: 100,
                      height: 25,
                      handler: function(toggled){                               
                    	  window.close();
                      }
                  });             
               }
            
          	function accept() {
        	  	var date = document.getElementById( 'date' ).value;
    
                // PLEASE CHOOSE WAY OF DEFINING TIME
                
                // EITHER: time is only taken at midnight each day.
                //var s = date + "T00:00:00Z";
    
                // OR: time is taken from user selections 
                // (comment out the blocks in function init() and in html as well)                 
        	  	var hour = getSelectedValue( 'hours' ); 
        	  	if ( parseInt( hour ) < 10 ) {
                  	hour = "0" + parseInt( hour );
              	}
        	  	var minute = getSelectedValue( 'minutes' ); 
        	  	if ( parseInt( minute ) < 10 ) {
        		  	minute = "0" + parseInt( minute );
              	}
              	var s = date + "T" + hour + ":" + minute + ":00Z";
    
              	var lgs = getSelectedValues( "layerGroup" );
              	// set TIME for each selected layergroup
        	  	var layerList = parent.controller.mapModel.getLayerList();
              	for ( var i = 0; i < lgs.length; i++) {
            	  	var layerGroup = layerList.getLayerGroupByURL( lgs[i] );
            	  	layerGroup.setTime( s );
              	}
        	  	parent.controller.repaint();
              	window.close();
          	}
	  	
            function register() {
                if ( parent.controller == null ) {
                    parent.controller = new parent.Controller();
                    parent.controller.init();
                }
                parent.controller.initTimeselect(document);
            }

          //-->
        </script>
    </head>
    <body onload="register();  init();" class="pDateTimeDialog">
        <div class="pDateTimeDialogHeader">
            <span style="position: absolute; width:280px; height:20px; text-decoration:none"><b>&nbsp;&nbsp;Date/Time Selection</b></span>
        </div>
        <div class="cdate">
            <form>
                <script language="javascript1.2">
                <!--
                var c1 = new CodeThatCalendar(caldef1);             
                //-->
                </script>
                <br>         
                <span style="position:absolute; text-decoration:none; width:60px">Date:</span>
                <input id="date" name="id1" disabled type="text" style="position:absolute; left:50px; width:140px;" onchange="hint();"> 
                <div class="buttonIcon" style="position:absolute; left:210px;" ><a href="javascript:c1.popup('id1');"><img src="./images/calendar.gif" title="open calendar" alt="Calendar" border="0" width="16" height="16"></a></div>
            </form>
        </div>
        <div class="ctime">
            <span style="position:absolute; text-decoration:none; width:60px">Time:</span>
            <!-- COMMENT OUT, IF USER SHALL NOT BE ABLE TO SELECT A TIME! -->
            <select id="hours" style="position:absolute; left:50px; width:60px" onchange="hint();">
                <option value="0">0 h</option>
                <option value="1">1 h</option>
                <option value="2">2 h</option>
                <option value="3">3 h</option>
                <option value="4">4 h</option>
                <option value="5">5 h</option>
                <option value="6">6 h</option>
                <option value="7">7 h</option>
                <option value="8">8 h</option>
                <option value="9">9 h</option>
                <option value="10">10 h</option>
                <option value="11">11 h</option>
                <option value="12">12 h</option>
                <option value="13">13 h</option>
                <option value="14">14 h</option>
                <option value="15">15 h</option>
                <option value="16">16 h</option>
                <option value="17">17 h</option>
                <option value="18">18 h</option>
                <option value="19">19 h</option>
                <option value="20">20 h</option>
                <option value="21">21 h</option>
                <option value="22">22 h</option>
                <option value="23">23 h</option>
            </select>
            <span style="position:absolute; text-decoration:none; left:113px;">:</span>
            <select id="minutes" style="position:absolute; left:120px; width:80px" onchange="hint();">
                 <option value="0" >00 min</option>
                 <option value="10">10 min</option>
                 <option value="20">20 min</option>
                 <option value="30">30 min</option>
                 <option value="40">40 min</option>
                 <option value="50">50 min</option>
            </select>
            <!-- COMMENT IN, IF USER SHALL NOT BE ABLE TO SELECT A TIME! -->
            <!-- <span style="position:absolute; text-decoration:none; left:113px;">00:00:00</span> -->
        </div>
        <div class="cLayerGroup">
            <span style="position:absolute; text-decoration:none; width:70px">LayerGroup:</span>
            <select id="layerGroup" style="position:absolute; left:80px; width:150px" size="3" multiple></select>    
        </div>
        <div class="cOK">
             <table>
	            <tr>
	                <td width="30">&nbsp;</td>
	                <td>
	                    <div id="buttonArea0"></div>
	                </td>
	                <td width="10">&nbsp;</td>
	                <td>
	                    <div id="buttonArea1"></div>
	                </td>
	            </tr>
	        </table>     
        </div>
        <div class="pDateTimeDialogFooter">
            <a style="position: absolute; width:280px; height:20px; text-decoration:none"></a>
        </div>
    </body>
</html>