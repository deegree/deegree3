<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<!--
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.deegree.i18n.Messages" %>
<%@ page import="java.util.Locale" %>
<%
Locale loc = request.getLocale();
String lang = loc.getLanguage();   
if ( lang == null || ( !(lang.equals("en"))  && !(lang.equals("de")) ) ) {
    lang = "en";
}
String requestURL = request.getRequestURL().toString();
int idx = requestURL.lastIndexOf( '/' );
requestURL = requestURL.substring( 0, idx ) + "/services";
%>
-->
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <title>Insert title here</title>
        <link href="./css/metadataclient.css"  type="text/css" rel="stylesheet">
        <script type="text/javascript" src="./javascript/request_handler.js"></script>
        <script type="text/javascript" src="./javascript/json2.js"></script>
        <script type="text/javascript" src="./javascript/utils.js"></script>
        <script type="text/javascript">
<!--
           var tmp = window.location.pathname.split( '/' );
           var s = '';           
           for ( var i = 0; i < tmp.length-1; i++) {
               s += (tmp[i] + '/' ); 
           } 
           url = window.location.protocol + '//' + window.location.host + s + 'control';

           function init() {
        	   getElement( "TAKE" ).style.visibility = "hidden";
        	   getElement( "MENU" ).style.visibility = "hidden";
           }

           function selectDatabase() {
        	   var db = getSelectedValue( "DBVENDOR", null, "Postgres" );
        	   if ( db == "Postgres" ) {
        		   setInputValue("DBURL", "localhost:5432" );
        	   } else if ( db == "Oracle" ) {
        		   setInputValue("DBURL", "localhost:1521" );
        		   var win = window.open( 'oracle_<%=lang %>.html', 'Oracle', 'width=1000,height=400,left=10,top=100,scrollbars=yes,resizable=yes' );
        		    win.focus();   
        	   }
           }

           function testConnection() {
              var bean = JSON.stringify( new function() {
            	  this.action = 'testDBConnection';
                  this.db = getSelectedValue( "DBVENDOR", null, "Postgres" );
                  this.url = getInputValue( "DBURL" );
                  this.user = getInputValue( "DBUSER" );
                  this.pw = getInputValue( "DBPASSWORD" );
                  this.sid = getInputValue( "SID" );
               }, null, false );
              try {
                  submitPostRequest( url, handleTestConnection, bean );                    
              } catch(e) {
                  alert( 1 + " " + e );
              }
           }

           function handleTestConnection(response) {    
               if ( response.indexOf( "ERROR:" ) < 0 ) { 
            	   alert( '<%=Messages.get( loc, "CATMANAGE_SETUP_VALID" ) %>' );
            	   getElement( "TAKE" ).style.visibility = "visible";
            	   // for future version ...
                   // if ( response.indexOf( "true" ) >= 0 ) {
                   //     getElement( "EXITINGTABLE" ).disabled = false;
                   // } else {
                   //     getElement( "EXITINGTABLE" ).disabled = true;
                   // }
                   getElement( "EXITINGTABLE" ).disabled = false;
               } else {            	   
            	   alert( response );
               }
           }

           function take() {
               // TODO: The following lines must be uncommented on demo server to prevent unauthorized setup changes
               //alert( 'Because of security constraints this function is not available on this server!' );
               //return;
               if ( !validate() ) {
                   return;
               }
               var bean = JSON.stringify( new function() {
                   this.action = 'doConfiguration';
                   this.cswurl = getInputValue( "CSWURL" );
                   this.db = getSelectedValue( "DBVENDOR", null, "Postgres" );
                   this.url = getInputValue( "DBURL" );
                   this.user = getInputValue( "DBUSER" );
                   this.pw = getInputValue( "DBPASSWORD" );
                   this.sid = getInputValue( "SID" );
                   this.newTables = getElement( "NEWTABLES" ).checked;
                   this.transactions = getElement( "TRANSACTIONS" ).checked;
                   this.searchClient = getElement( "SEARCHCLIENT" ).checked;
                   this.editor = getElement( "EDITOR" ).checked;
                }, null, false );
               try {
            	   getElement( 'WAIT' ).style.visibility = 'visible';
                   submitPostRequest( url, handleConfiguration, bean );                    
               } catch(e) {
                   alert( 1 + " " + e );
               }
           }

           function handleConfiguration(response) {   
        	   alert( response );
        	   if ( response.indexOf( "ERROR:" ) < 0 ) {             
                   getElement( "MENU" ).style.visibility = "visible";
                   getElement( 'WAIT' ).style.visibility = 'hidden';
                   var tmp = window.location.pathname.split( '/' ); 
                   // restart tomcat web context to activate changes
                   var path = window.location.protocol + '//' + window.location.host + "/manager/html/reload?path="+ '/' + tmp[1];
                   submitGetRequest( path, function(resp) {}, null, false );   
        	   }
           }

           function validateDBSettings() {
               return true;
           }

           function validate() {
        	   if ( !validateDBSettings() ) {
            	   return false; 
        	   }
        	   return true;
           }
        
 -->        
       </script>
    </head>
    <body onload="init()">
	    <div id="boxSearch">
	        <div class="topFrame">
                <table cellpadding="0" cellspacing="0" border="0" summary="">
                    <tr>
                        <td><img border="0" src="./images/logo-deegree.png" alt="logo"/></td>
                        <td width="100"></td>
                        <td><h2>catalogueManager Setup</h2></td>
                    </tr>
                </table>	            
	        </div>
	        
	        <div class="menubarSetup">
	           <table id="MENU">
	               <tr>
	                   <td><a href="md_search.jsp">search</a></td>
	                   <td width="15"></td>
                       <td><a href="md_editor.jsp">editor</a></td>     
	                   <td width="15"></td>
<!-- 	                   
	                   <td><a href="javascript:alert('TODO')">Configuration</a></td>
	                   <td width="15"></td>
 -->	                   
                       <td><a href="csw_main.jsp">home</a></td>
	               </tr>
	           </table>
	        </div>
	    
	    
			<div id="setup">
				<div style="position:relative; left:25px; top:20px;">
		            <%=Messages.get( loc, "CATMANAGE_SETUP_CSWURL" ) %> <input title="tooltip" id="CSWURL" style="position: absolute; left: 200px; width: 400px;" value="<%=requestURL %>">
				</div>		
				<hr style="position:relative; left:0px; top:40px; width:720px">
				<div style="position:relative; left:25px; top:60px;">
		            <%=Messages.get( loc, "CATMANAGE_SETUP_DATABASE" ) %> <select id="DBVENDOR" title="tooltip" style="position: absolute; left: 200px; width: 400px;" onchange="selectDatabase()">							
								<option value="Postgres">PostgreSQL/PostGIS</option>
								<option value="Oracle">Oracle</option>
							  </select>
				</div>		
				<div style="position:relative; left:25px; top:70px;">
		            <%=Messages.get( loc, "CATMANAGE_SETUP_DATABASEURL" ) %> <input id="DBURL" title="tooltip" style="position: absolute; left: 200px; width: 400px;" value="localhost:5432">		
				</div>
                <div style="position:relative; left:25px; top:80px;">
                    <%=Messages.get( loc, "CATMANAGE_SETUP_SID" ) %> <input id="SID" title="tooltip" style="position: absolute; left: 200px; width: 400px;">     
                </div>
				<div style="position:relative; left:25px; top:90px;">
	                <%=Messages.get( loc, "CATMANAGE_SETUP_DBUSER" ) %> <input id="DBUSER" title="tooltip" style="position: absolute; left: 200px; width: 400px;">     
	            </div>
	            <div style="position:relative; left:25px; top:100px;">
	                <%=Messages.get( loc, "CATMANAGE_SETUP_DBPASSWORD" ) %> <input id="DBPASSWORD" title="tooltip" style="position: absolute; left: 200px; width: 400px;">     
	            </div>
	            
	            <div style="position:relative; left:25px; top:110px;">
	                <input title="tooltip" style="position: absolute; left: 200px; border: 1px solid #1C488F; background-color: #D9E7F8; font-size: 9pt; color: #15428B; padding: 1px 4px; -moz-border-radius: 3px 3px 3px 3px;  " type="button" value="test connection &amp; proceed" onclick="testConnection();">     
	            </div>   
	            <div style="position:relative; left:25px; top:145px;">
	                <%=Messages.get( loc, "CATMANAGE_SETUP_CREATETABLES" ) %> <input id="NEWTABLES" title="tooltip" name="CREATESCHEMA" type="radio" style="position: absolute; left: 200px; " checked="checked">                
	            </div>
	            <div style="position:relative; left:25px; top:150px;">
	                <%=Messages.get( loc, "CATMANAGE_SETUP_USETABLES" ) %> <input id="EXITINGTABLE" title="tooltip" name="CREATESCHEMA" type="radio" style="position: absolute; left: 200px; ">
	            </div>    
	            <hr style="position:relative; left:0px; top:170px; width:720px">
	            <div style="position:relative; left:25px; top:190px;">
	                <%=Messages.get( loc, "CATMANAGE_SETUP_ALLOWTRANSACTIONS" ) %> <input id="TRANSACTIONS" title="tooltip" type="checkbox" style="position: absolute; left: 200px; " checked="checked">     
	            </div>
	            <hr style="position:relative; left:0px; top:210px; width:720px">
	            <div style="position:relative; left:25px; top:230px;">
	                <%=Messages.get( loc, "CATMANAGE_SETUP_INSTALLEDITOR" ) %> <input id="EDITOR" title="tooltip" type="checkbox" style="position: absolute; left: 200px; " checked="checked">     
	            </div>  	  
	            <div style="position:relative; left:25px; top:250px;">
	                <%=Messages.get( loc, "CATMANAGE_SETUP_INSTALLSEARCH" ) %> <input id="SEARCHCLIENT" title="tooltip" type="checkbox" style="position: absolute; left: 200px; " checked="checked">     
	            </div>
	            <hr style="position:relative; left:0px; top:270px; width:720px">
	            <div style="position:relative; left:25px; top:290px;">
	                <input id="TAKE" title="tooltip" class="button" type="button" value='<%=Messages.get( loc, "CATMANAGE_SETUP_OK" ) %>' onclick="take();">     
	            </div>   
			</div>
		</div>
		<div id="WAIT" style="z-index:100; background:#FFFFFF; visibility:hidden; position:absolute; left:0px; top:0px; height:100%; width:100%; opacity: .80; filter: alpha(opacity=80); -moz-opacity: 0.8;">
            <img style="position:absolute; left:30%; top:30%; " alt="-" src="./images/progress.gif">
        </div>
	</body>
</html>
