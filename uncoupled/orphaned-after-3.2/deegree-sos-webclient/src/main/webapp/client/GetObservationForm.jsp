<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="org.apache.axiom.om.OMAttribute"%>
<%@ page import="org.apache.axiom.om.OMElement"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Enumeration"%>
<%@ page import="org.deegree.client.sos.storage.StorageGetCapabilities"%>
<%@ page import="org.deegree.client.sos.utils.*"%>
<%@ page import="org.deegree.client.sos.requesthandler.kvp.KVPGetObservation"%>
<%@ page import="org.deegree.client.sos.requesthandler.kvp.KVPfromOffering"%>
<%@ page import="org.deegree.commons.utils.Pair"%>



<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title></title>
        <link rel="stylesheet" type="text/css" href="style.css" media="screen">
				<script language="JavaScript" type="text/javascript" src="client/GetObservationForm.js"></script>                        
        
                <script language="JavaScript1.2" type="text/javascript">
        <!--
<%
KVPGetObservation kvps = (KVPGetObservation) request.getAttribute( "kvps" );
%>
            var mapBeginTime = {};
            var mapEndTime = {};
            var mapOffering = {};
            var mapFeatureOfInterest = {};
            var mapObservedProperties = {};
            var mapProcedures = {};
            var mapResponseFormat = {};
            var mapResponseMode = {};
            var mapResult = {};
            var mapResultModel = {};
            var mapSRSName = {};

<% 
List<KVPfromOffering> kvpfromOffering = kvps.getKVPfromOffering();
for(KVPfromOffering element : kvpfromOffering){
    List<String> list = element.getFeatureOfInterest();
    String featureOfInterest = "";
    for(String string : list){
        featureOfInterest += string + "@@";
    }
    list = element.getObservedProperties();
    String observedProperties = "";
    for(String string : list){
        observedProperties += string + "@@";
    }
    List<Pair<String, String>> procs = element.getProcedures();
    String procedures = "";
    for(Pair<String, String> pair : procs){
        procedures += pair.first + "@@";
    }
    list = element.getResponseFormat();
    String responseFormat = "";
    for(String string : list){
        responseFormat += string + "@@";
    }
    list = element.getResponseMode();
    String responseMode = "";
    for(String string : list){
        responseMode += string + "@@";
    }
    list = element.getResult();
    String result = "";
    for(String string : list){
        result += string + "@@";
    }
    list = element.getResultModel();
    String resultModel = "";
    for(String string : list){
        resultModel += string + "@@";
    }
%>
            mapBeginTime.<%=element.getId()%> = "<%=element.getEventTime().first%>";
            mapEndTime.<%=element.getId()%> = "<%=element.getEventTime().second%>";
            mapOffering.<%=element.getId()%> = "<%=element.getName()%>";
            mapFeatureOfInterest.<%=element.getId()%> = "<%=featureOfInterest%>";
            mapObservedProperties.<%=element.getId()%> = "<%=observedProperties%>";
            mapProcedures.<%=element.getId()%> = "<%=procedures%>";
            mapResponseFormat.<%=element.getId()%> = '<%=responseFormat%>';
            mapResponseMode.<%=element.getId()%> = "<%=responseMode%>";
            mapResult.<%=element.getId()%> = "<%=result%>";
            mapResultModel.<%=element.getId()%> = "<%=resultModel%>";
            mapSRSName.<%=element.getId()%> = "<%=element.getSRSName()%>";
<%
}
%>
            var version = "<%=kvps.getVersion()%>";
            var host = "<%= kvps.getHost()%>";
        -->
        </script>
    </head>    
    <body leftmargin="10" rightmargin="10" topmargin="0" marginwidth="0"
    marginheight="0" link="#2D2A63" vlink="#2D2A63" alink="#E31952">
        <div>Select the parameters for your <i>GetObservation</i> request first and then click the <i>submit-button</i> below:</div>
        <br />
        <br />
        Please select an offering first:&emsp;&emsp;
        <select onchange="setOffering()" id="selectedOffering">  
            <option>...</option>           
<%
for (KVPfromOffering element : kvpfromOffering){
%>
            <option value="<%=element.getId() %>"><%=element.getId()%></option>
<%
}
%>
        </select>
        <br />
        <br />
        <table border="1" cellspacing="0" cellpadding="3" width="130">
            <tr>
                <th>parameter</th>
                <th>use?</th>
                <th>possibilities</th>
                <th>value</th>
            </tr>
            <tr>
                <td>request</td>
                <td><input type="checkbox" checked disabled"></td>
                <td>GetObservation</td>
                <td><input type="text" disabled value="GetObservation" size="40"></td>
            </tr>
            <tr>
                <td>service</td>
                <td><input type="checkbox" checked disabled"></td>
                <td>SOS</td>
                <td><input type="text" disabled value="SOS" size="40"></td>
            </tr>
            <tr>
                <td>version</td>
                <td><input type="checkbox" checked disabled"></td>
                <td><%=kvps.getVersion()%></td>
                <td><input type="text" value="<%=kvps.getVersion()%>" size="40"></td>
            </tr>
            <tr>
                <td>eventTime: begin</td>
                <td><input type="checkbox" onchange="setValue('div_begin','beginTime')"></td>
                <td><div id="div_begin"> </div></td>
                <td><input type="text" id="beginTime" disabled value="" size="40"></td>
            </tr>
            <tr>
                <td>eventTime: end</td>
                <td><input type="checkbox" onchange="setValue('div_end', 'endTime')"></td>
                <td><div id="div_end"> </div></td>
                <td><input type="text" id="endTime" disabled value="" size="40"></td>
            </tr>
            <tr>
                <td>offering</td>
                <td><input type="checkbox" checked disabled"></td>
                <td>
                    <select id="select_offering" onchange="setValueList(this, 'offering')">
                        <option id="id"> </option>
                        <option id="name"> </option>
                    </select>
                </td>
                <td><input type="text" id="offering" value="" size="40"></td>
            </tr>
            <tr>
                <td>featureOfInterest</td>
                <td><input type="checkbox" onchange="setUsability('featureOfInterest')"></td>
                <td><select id="featureOfInterest" disabled></select></td>
                <td><input type="text" disabled value="" size="40"></td>
            </tr>
            <tr>
                <td>observedProperty</td>
                <td><input type="checkbox" checked disabled"></td>
                <td><select multiple id="select_observedProperties" onchange="setValueMultiple(this, 'observedProperties')"></select></td>
                <td><input type="text" id="observedProperties" value="" size="40"></td>
            </tr>
            <tr>
                <td>procedure</td>
                <td><input type="checkbox" onchange="setUsability('select_procedures')"></td>
                <td><select multiple id="select_procedures" disabled onchange="setValueMultiple(this, 'procedures')"></select></td>
                <td><input type="text" id="procedures" value="" size="40"></td>
            </tr>
            <tr>
                <td>responseFormat</td>
                <td><input type="checkbox" checked disabled"></td>
                <td><select id="select_responseFormat" onchange="setValueList(this, 'responseFormat')"></select></td>
                <td><input type="text" id="responseFormat" value="" size="40"></td>
            </tr>
            <tr>
                <td>responseMode</td>
                <td><input type="checkbox" onchange="setUsability('select_responseMode')"></td>
                <td><select id="select_responseMode" disabled onchange="setValueList(this, 'responseMode')"></select></td>
                <td><input type="text" id="responseMode" value="" size="40"></td>
            </tr>
            <tr>
                <td>result</td>
                <td><input type="checkbox" onchange="setUsability('select_result')"></td>
                <td><select id="select_result" disabled onchange="setValueList(this, 'result')"></select></td>
                <td><input type="text" id="result" value="" size="40"></td>
            </tr>
            <tr>
                <td>resultModel</td>
                <td><input type="checkbox" onchange="setUsability('select_resultModel')"></td>
                <td><select id="select_resultModel" disabled onchange="setValueList(this, 'resultModel')"></select></td>
                <td><input type="text" id="resultModel" value="" size="40"></td>
            </tr>
            <tr>
                <td>srsName</td>
                <td><input type="checkbox" onchange="setValue('div_srsName', 'srsName')"></td>
                <td><div id="div_srsName"> </div></td>
                <td><input type="text" id="srsName" disabled value="" size="40"></td>
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
        <br/>
        <br/>
        <div id="chart"></div>
    </body>
</html>