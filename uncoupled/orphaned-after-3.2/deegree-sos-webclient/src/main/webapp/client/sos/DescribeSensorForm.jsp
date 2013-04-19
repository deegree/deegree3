<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="org.apache.axiom.om.OMAttribute"%>
<%@ page import="org.apache.axiom.om.OMElement"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.List"%>
<%@ page import="org.deegree.client.sos.storage.StorageGetCapabilities"%>
<%@ page import="org.deegree.client.sos.requesthandler.kvp.KVPDescribeSensor"%>
<%@ page import="org.deegree.commons.utils.Pair"%>



<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title></title>
        <link rel="stylesheet" type="text/css" href="style.css" media="screen">
        <script language="JavaScript1.2" type="text/javascript">
        <!--
<%
KVPDescribeSensor kvps = (KVPDescribeSensor) request.getAttribute( "kvps" );
%>

            function getData() {

            	var version = "<%=kvps.getVersion()%>";
                var outputformat = document.getElementById( "outputformat" ).value;
                var procedure = document.getElementById( "procedure" ).value;

                var url = "SOSClient?request=DescribeSensor";
                url = url+"&version="+version;
                url = url+"&outputformat="+outputformat;
                url = url+"&procedure="+procedure;
                url = url+"&host=<%= kvps.getHost()%>";
                submitForm(url);
            }
            
            function submitForm(url){
            	var req = null; 
                if (window.XMLHttpRequest) {
                    req = new XMLHttpRequest();
                } else if (window.ActiveXObject) {
                    try {
                        req = new ActiveXObject("Msxml2.XMLHTTP");
                    } catch (e) {
                        try {
                            req = new ActiveXObject("Microsoft.XMLHTTP");
                        } catch (e) {}
                    }
                }
                req.onreadystatechange = function() { 
                    if(req.readyState == 4) {
                        if(req.status == 200) {
                            document.getElementById("data").innerHTML = req.responseText;   
                        } else {
                            document.getElementById("data").innerHTML= "Error: returned status code " + req.status + " " + req.statusText;
                        }   
                    } 
                }; 
                req.open("GET", url, true); 
                req.send(null); 
            }

            function setValue(element, id){
                document.getElementById( id ).value = element.options[element.options.selectedIndex].value;

            }

            function enter(){
            	var element = document.getElementById( "outp" );
                setValue(element, "outputformat");
                
                element = document.getElementById( "proc" );
                setValue(element, "procedure");
            }

            
        -->
        </script>
    </head>
    <body leftmargin="10" rightmargin="10" topmargin="0" marginwidth="0"
    marginheight="0" link="#2D2A63" vlink="#2D2A63" alink="#E31952" onload="enter()">
        <div>Please select the parameters for your <i>DescribeSensor</i> request first and then click the <i>submit-button</i> below:</div>
        <br />
        <br />
        <table border="1" cellspacing="0" cellpadding="3" width="130">
            <tr>
                <th>parameter</th>
                <th>possibilities</th>
                <th>value</th>
            </tr>
            <tr>
                <td>request</td>
                <td>DescribeSensor</td>
                <td><input type="text" disabled value="DescribeSensor" size="40"></td>
            </tr>
            <tr>
                <td>service</td>
                <td>SOS</td>
                <td><input type="text" disabled value="SOS" size="40"></td>
            </tr>
            <tr>
               <td>version</td>
               <td><%=kvps.getVersion()%></td>
               <td><input type="text" disabled value="<%=kvps.getVersion()%>" size="40"></td>
            </tr>
            <tr>
                <td>outputformat</td>
                <td>
                    <select id="outp" onchange="setValue(this, 'outputformat')">             
<%
List<String> outputFormats = kvps.getOutputFormat();
for (String outputformat : outputFormats){%>
                        <option><%=outputformat%></option>
<%
}
%>
                    </select>
                </td>
                <td><input type="text" id="outputformat" size="40"></input></td>
            </tr>
            <tr>
                <td>procedure</td>
                <td>
                    <select id="proc" onchange="setValue(this, 'procedure')">             
<%
List<Pair<String, String>> procedures = kvps.getProcedure();
for (Pair<String, String> procedure : procedures){
%>
                        <option value="<%=procedure.first %>"><%=procedure.second%></option>
<%
}
%>
                    </select>
                </td>
                <td><input type="text" id="procedure" size="40"></input></td>
            </tr>
        </table>
        <br/>
        <br/>
        <button onclick="getData()">Submit</button>
        <br/>
        <br/>
        <button onclick="history.back()">Back</button>
        <br/>
        <br/>
        <br/>
        <div id="data"></div>
    </body>
</html>