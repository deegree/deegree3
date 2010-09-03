function setDefaultValues(){
    var element = document.getElementById( "select_offering" );
    document.getElementById( "offering" ).value = element.options[0].value;
                
    element = document.getElementById( "select_observedProperties" );
    document.getElementById( "observedProperties" ).value = element.options[0].value;

    element = document.getElementById( "select_responseFormat" );
    document.getElementById( "responseFormat" ).value = element.options[0].value;

    element = document.getElementById( "select_responseMode" );
    document.getElementById( "responseMode" ).value = element.options[0].value;

    element = document.getElementById( "select_result" );
    document.getElementById( "result" ).value = element.options[0].value;

    element = document.getElementById( "select_resultModel" );
    document.getElementById( "resultModel" ).value = element.options[0].value;
}

function setValue(content, target){
    document.getElementById(target).value = document.getElementById(content).firstChild.nodeValue;
    setUsability(target);
}

function generateChart(procedure){
	var url = "SOSClient?request=generateChart&procedure="+procedure;
    var div_chart = document.getElementById( "chart" );
    var img_node = document.createElement( "img" );
    img_node.setAttribute( "src", url, 1 );
    while (div_chart.firstChild) {
        div_chart.removeChild(div_chart.firstChild);
    }
    div_chart.appendChild( img_node );
}

function setValueList(element, id){
	document.getElementById( id ).value = element.options[element.options.selectedIndex].value;
}

function setValueMultiple(content, target){
    var element = document.getElementById(target);
    var text = '';
    for(var i = 0; i < content.options.length; i++){
        if(content.options[i].selected){
            text += '@@'+content.options[i].firstChild.nodeValue;
        }
    }
    element.value = text.substring(2);
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

function setUsability(name){
    var element = document.getElementById( name );
    if (element.disabled){
        element.disabled = false;
    }else{
        element.disabled = true;
    }
}

function setOffering(){
    var select = document.getElementById( "selectedOffering" );
    var offeringIndex = select.options[select.options.selectedIndex].value;

    var element = document.getElementById( "div_begin" );
    var textnode = document.createTextNode(mapBeginTime[offeringIndex]);
    element.replaceChild(textnode, element.firstChild);

    element = document.getElementById( "div_end" );
    textnode = document.createTextNode(mapEndTime[offeringIndex]);
    element.replaceChild(textnode, element.firstChild);

    element = document.getElementById( "id" );
    element.value = offeringIndex;
    while (element.firstChild) {
        element.removeChild(element.firstChild);
    }
    var node = document.createTextNode(offeringIndex);
    element.appendChild(node);

    element = document.getElementById( "name" );
    element.value = mapOffering[offeringIndex];
    while (element.firstChild) {
        element.removeChild(element.firstChild);
    }
    node = document.createTextNode(mapOffering[offeringIndex]);
    element.appendChild(node);

    element = document.getElementById( "featureOfInterest" );
    var list = new Array();
    if(offeringIndex != "..."){
        list = mapFeatureOfInterest[offeringIndex].split("@@");
    }
    while (element.firstChild) {
        element.removeChild(element.firstChild);
    }
    for(i=0;i<list.length;i++){
        node = new Option(list[i], list[i]);
        element.appendChild(node);
    }

    element = document.getElementById( "select_observedProperties" );
    list = new Array();
    if(offeringIndex != "..."){
        list = mapObservedProperties[offeringIndex].split("@@");
    }
    while (element.firstChild) {
        element.removeChild(element.firstChild);
    }
    for(i=0;i<list.length;i++){
        node = new Option(list[i], list[i]);
        element.appendChild(node);
    }

    element = document.getElementById( "select_procedures" );
    list = new Array();
    if(offeringIndex != "..."){
        list = mapProcedures[offeringIndex].split("@@");
    }
    while (element.firstChild) {
        element.removeChild(element.firstChild);
    }
    for(i=0;i<list.length;i++){
        node = new Option(list[i], list[i]);
        element.appendChild(node);
    }
    
    element = document.getElementById( "select_responseFormat" );
    list = new Array();
    if(offeringIndex != "..."){
        list = mapResponseFormat[offeringIndex].split("@@");
    }
    while (element.firstChild) {
        element.removeChild(element.firstChild);
    }
    for(i=0;i<list.length;i++){
        node = new Option(list[i], list[i]);
        element.appendChild(node);
    }
    
    element = document.getElementById( "select_responseMode" );
    list = new Array();
    if(offeringIndex != "..."){
        list = mapResponseMode[offeringIndex].split("@@");
    }
    while (element.firstChild) {
        element.removeChild(element.firstChild);
    }
    for(i=0;i<list.length;i++){
        node = new Option(list[i], list[i]);
        element.appendChild(node);
    }

    element = document.getElementById( "select_result" );
    list = new Array();
    if(offeringIndex != "..."){
        list = mapResult[offeringIndex].split("@@");
    }
    while (element.firstChild) {
        element.removeChild(element.firstChild);
    }
    for(i=0;i<list.length;i++){
        node = new Option(list[i], list[i]);
        element.appendChild(node);
    }
    
    element = document.getElementById( "select_resultModel" );
    list = new Array();
    if(offeringIndex != "..."){
        list = mapResultModel[offeringIndex].split("@@");
    }
    while (element.firstChild) {
        element.removeChild(element.firstChild);
    }
    for(i=0;i<list.length;i++){
        node = new Option(list[i], list[i]);
        element.appendChild(node);
    }
    
    element = document.getElementById( "div_srsName" );
    textnode = document.createTextNode(mapSRSName[offeringIndex]);
    element.replaceChild(textnode, element.firstChild);

    setDefaultValues();
}

function getData() {

    var select = document.getElementById( "featureOfInterest" );
    try {
    	var featureOfInterest = select.options[select.options.selectedIndex].value;
    } catch (e) {
    	alert("please select an offering first!");
    }

    var offering = document.getElementById( "offering" ).value;
    var observedProperty = document.getElementById( "observedProperties" ).value;
    var procedures = document.getElementById( "procedures" ).value;
    var responseFormat = document.getElementById( "responseFormat" ).value;
    var responseMode = document.getElementById( "responseMode" ).value;
    var result = document.getElementById( "result" ).value;
    var resultModel = document.getElementById( "resultModel" ).value;
    var srsName = document.getElementById( "srsName" ).value;

    var url = "SOSClient?request=GetObservation";
    url = url+"&version="+version;
    var eventTimeBegin = document.getElementById( "beginTime" ).value;
    var eventTimeEnd = document.getElementById( "endTime" ).value;
    
    if(eventTimeBegin != "" && eventTimeEnd != ""){
        url = url+"&beginTime="+eventTimeBegin;
        url = url+"&endTime="+eventTimeEnd;
    }else{
    	alert("please enter a value for both 'beginTime' and 'endTime'!");
    }
    if(featureOfInterest != null){
        url = url+"&featureOfInterest="+featureOfInterest;
    }
    url = url+"&observedProperty="+observedProperty;
    url = url+"&offering="+offering;
    if(procedures != null){
        url = url+"&procedure="+procedures;
    }
    url = url+"&responseFormat="+responseFormat;
    if(responseMode != null){
        url = url+"&responseMode="+responseMode;
    }
    if(result != null){
        url = url+"&result="+result;
    }
    if(resultModel != null){
        url = url+"&resultModel="+resultModel;
    }
    if (srsName != null){
        url = url+"&srsName="+srsName;
    }
    url = url+"&host="+host;
    
    //alert(url);
    
    submitForm(url);
}