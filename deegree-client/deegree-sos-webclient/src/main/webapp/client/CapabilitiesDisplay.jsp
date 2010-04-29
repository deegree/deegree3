<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.List"%>
<%@ page import="org.apache.axiom.om.OMAttribute"%>
<%@ page import="org.apache.axiom.om.OMElement"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="org.deegree.client.sos.storage.*"%>
<%@ page import="org.deegree.client.sos.storage.components.*"%>
<%@ page import="org.deegree.client.sos.utils.*"%>
<%@ page import="org.deegree.commons.utils.Pair"%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title></title>
        <link rel="stylesheet" type="text/css" href="style.css" media="screen">
    </head>
    <body leftmargin="10" rightmargin="10" topmargin="0" marginwidth="0"
    marginheight="0" link="#2D2A63" vlink="#2D2A63" alink="#E31952">

<%
StorageGetCapabilities storage = (StorageGetCapabilities) request.getAttribute( "storage" );

List<Pair<String, String>> serviceidentification = storage.getServiceIdentification();
List<Pair<String, String>> serviceprovider = storage.getServiceProvider();
%>
        <table border="1" cellspacing="0" cellpadding="3">
            <tr>
                <th colspan='2'>ServiceIdentification:</th>
            </tr>
<% 
for (Pair<String, String> pair : serviceidentification){
%>
            <tr>
                <td><%= pair.first %></td>
                <td><%= pair.second %></td>
            </tr>
<%          
    }
%>
        </table>
        <br />
        <br />
        <table border="1" cellspacing="0" cellpadding="3">
            <tr>
                <th colspan='2'>ServiceProvider:</th>
            </tr>
<% 
for (Pair<String, String> pair : serviceprovider){
%>
            <tr>
                <td><%= pair.first %></td>
                <td><%= pair.second %></td>
            </tr>
<% 
}
%>
        </table>
        <br />
        <br />
        <table border="1" cellspacing="0" cellpadding="3">
            <tr>
                <th colspan='2'>OperationsMetadata:</th>
            </tr>
<% 
List<Operation> operationsMetadata = storage.getOperationsMetadata();
for (Operation operation : operationsMetadata){
%>
            <tr>
                <td>OperationName</td>
                <td><%= operation.getName() %></td>
            </tr>
<% 
    List<Pair<String, String>> http = operation.getHttp();
    for(Pair<String, String> pair : http){
%>
            <tr>
                <td><%= pair.first %></td>
                <td><%= pair.second %></td>
            </tr>

<%
    }
    if (operation.getParameters().size() > 0){
        List<Parameter> parameters = operation.getParameters();
        for (Parameter parameter : parameters){
            List<String> allowedValues = parameter.getAllowedValues();
            for (String value : allowedValues){
%>
            <tr>
                <td><%= parameter.getName() %></td>
                <td><%= value %></td>
            </tr>
<%        
            }
        }
    }
}
%>
        </table>
        <br />
        <br />
        <table border="1" cellspacing="0" cellpadding="3">
            <tr>
                <th colspan='2'>Filter_Capabilities:</th>
            </tr>
<%
        
Filter_Capabilities filter_Capabilities = storage.getFilter_Capabilities();
List<String> geometryOperands = filter_Capabilities.getGeometryOperands();
for (String geometryOperand : geometryOperands){
%>
            <tr>
                <td>GeometryOperand</td>
                <td><%= geometryOperand %></td>
            </tr>
<%
}
List<Operator> spatialOperators = filter_Capabilities.getSpatialOperators();
for (Operator spatialOperator : spatialOperators){
%>
            <tr>
                <td>SpatialOperator</td>
                <td><%= spatialOperator.getName() %></td>
            </tr>
<%            
    if (spatialOperator.getOperands() != null){
        List<String> operandsFromSpatialOperator = spatialOperator.getOperands();
        for (String operand : operandsFromSpatialOperator){
%>
            <tr>
                <td>GeometryOperand of SpatialOperator</td>
                <td><%= operand %></td>
            </tr>
<%
        }
    }
}
        
List<String> temporalOperands = filter_Capabilities.getTemporalOperands();
for (String temporalOperand : temporalOperands){
%>
            <tr>
                <td>TemporalOperand</td>
                <td><%= temporalOperand %></td>
            </tr>
<%
        }
List<Operator> temporalOperators = filter_Capabilities.getTemporalOperators();
for (Operator temporalOperator : temporalOperators){
%>
            <tr>
                <td>TemporalOperator</td>
                <td><%= temporalOperator.getName() %></td>
            </tr>
<%            
    if (temporalOperator.getOperands() != null){
        List<String> operandsFromTemporalOperator = temporalOperator.getOperands();
        for (String operand : operandsFromTemporalOperator){
%>
            <tr>
                <td>TemporalOperand of TemporalOperator</td>
                <td><%= operand %></td>
            </tr>
<%
        }
    }
}
        
List<String> comparisonOperators = filter_Capabilities.getComparisonOperators();
for (String comparisonOperator : comparisonOperators){
%>
            <tr>
                <td>ComparisonOperator</td>
                <td><%= comparisonOperator %></td>
            </tr>
<% 
}
       
if (filter_Capabilities.getArithmeticOperators() != null){
    OMElement arithmeticOperators = filter_Capabilities.getArithmeticOperators();
%>
            <tr>
                <td><%= arithmeticOperators.getLocalName() %></td>
                <td><%= arithmeticOperators.getText() %></td>
            </tr>
<%
}

if (filter_Capabilities.getLogicalOperators() != null){
    OMElement logicalOperators = filter_Capabilities.getLogicalOperators();
%>
            <tr>
                <td><%= logicalOperators.getLocalName() %></td>
                <td><%= logicalOperators.getText() %></td>
            </tr>
<%
}
        
OMElement id_Capabilities = filter_Capabilities.getId_Capabilities();
%>
            <tr>
                <td>Id_Capabilities</td>
                <td><%= id_Capabilities.getLocalName() %></td>
            </tr>
        </table>
        <br />
        <br />
        <table border="1" cellspacing="0" cellpadding="3">
            <tr>
                <th colspan='2'>Contents:</th>
            </tr>
<%
List<ObservationOffering> contents = storage.getContents();
for (ObservationOffering offering : contents){
%>        
            <tr>
                <td colspan='2'><center><br />OfferingId: <%= offering.getId() %></center></td>
            </tr>
<%            
    List<OMElement> metadata = offering.getMetadata();
    for (OMElement element : metadata){
%>        
            <tr>
                <td><%= element.getLocalName() %></td>
                <td><%= element.getText() %></td>
            </tr>
<%                
        Iterator<OMAttribute> attributes;
        for (attributes = element.getAllAttributes(); attributes.hasNext();){
            OMAttribute attribute = attributes.next();
%>        
            <tr>
                <td><%= attribute.getLocalName() %></td>
                <td><%= attribute.getAttributeValue() %></td>
            </tr>
<% 
        }
    }
            
    BoundedBy boundedBy = offering.getBoundedBy();
%>        
            <tr>
                <td>BoundedBy</td>
                <td><%= boundedBy.getType() %></td>
            </tr>
<% 
    if (boundedBy.getAttributes().size() > 0){
        List<OMAttribute> attributes = boundedBy.getAttributes();
        for(OMAttribute attribute : attributes){
%>        
            <tr>
                <td>Attribute: <%= attribute.getLocalName() %></td>
                <td><%= attribute.getAttributeValue() %></td>
            </tr>
<% 
            }
        }
    List<OMElement> elements = boundedBy.getElements();
    if (elements.size() > 0){
        for (OMElement element : elements){
%>        
            <tr>
                <td><%= element.getLocalName() %></td>
                <td><%= element.getText() %></td>
            </tr>
<% 
        }
    }
    String text = boundedBy.getText();
    if (text!=null && !text.trim().equals("")){
%>        
            <tr>
                <td>Text</td>
                <td><%= text %></td>
            </tr>
<% 
    }
            
    List<OMElement> intendedApplications = offering.getIntendedApplications();
    for (OMElement element : intendedApplications){
%>        
            <tr>
                <td><%= element.getLocalName() %></td>
                <td><%= element.getText() %></td>
            </tr>
<%                
        Iterator<OMAttribute> attributes;
        for (attributes = element.getAllAttributes(); attributes.hasNext();){
            OMAttribute attribute = attributes.next();
%>        
            <tr>
                <td><%= attribute.getLocalName() %></td>
                <td><%= attribute.getAttributeValue() %></td>
            </tr>
<% 
        }
    }
           
    Time time = offering.getTime();
    List<OMAttribute> attributesOfTime = time.getAttributesOfTime();
    for (OMAttribute attributeOfTime : attributesOfTime){                
%>
            <tr>
                <td>TimeAttribute <%= attributeOfTime.getLocalName() %></td>
                <td><%= attributeOfTime.getAttributeValue() %></td>
            </tr>
<% 
    }
    if (!time.getIsNull()){
        elements = time.getElements();
        for (OMElement element : elements){
%>        
            <tr>
                <td><%= element.getLocalName() %></td>
                <td><%= element.getText() %></td>
            </tr>
<% 
        }
    }
           
    List<String> procedures = offering.getProcedures();
    for(String procedure : procedures){
%>        
            <tr>
                <td>procedure</td>
                <td><%= procedure %></td>
            </tr>
<% 
    }
            
    List<String> observedProperties = offering.getObservedProperties();
    for (String observedProperty : observedProperties){
%>        
            <tr>
                <td>observedProperty</td>
                <td><%= observedProperty %></td>
            </tr>
<%                    
    }

    elements = offering.getFeaturesOfInterest();
    for(OMElement element : elements){
        if(!element.getText().trim().equals("")){
%>        
            <tr>
                <td><%= element.getLocalName() %></td>
                <td><%= element.getText() %></td>
            </tr>
<%          
        }
        Iterator<OMAttribute> attributes;
        for (attributes = element.getAllAttributes(); attributes.hasNext();){
            OMAttribute attribute = attributes.next();
%>        
            <tr>
                <td><%= element.getLocalName() %>: <%= attribute.getLocalName() %></td>
                <td><%= attribute.getAttributeValue() %></td>
            </tr>
<% 
        }
    }
    elements = offering.getResponseFormats();
    for(OMElement element : elements){
        if(!element.getText().trim().equals("")){
%>        
            <tr>
                <td><%= element.getLocalName() %></td>
                <td><%= element.getText() %></td>
            </tr>
<%          
        }
        Iterator<OMAttribute> attributes;
        for (attributes = element.getAllAttributes(); attributes.hasNext();){
            OMAttribute attribute = attributes.next();
%>        
            <tr>
                <td><%= element.getLocalName() %>: <%= attribute.getLocalName() %></td>
                <td><%= attribute.getAttributeValue() %></td>
            </tr>
<% 
        }
    }
    elements = offering.getResponseModes();
    for(OMElement element : elements){
        if(!element.getText().trim().equals("")){
%>        
            <tr>
                <td><%= element.getLocalName() %></td>
                <td><%= element.getText() %></td>
            </tr>
<%          
        }
        Iterator<OMAttribute> attributes;
        for (attributes = element.getAllAttributes(); attributes.hasNext();){
            OMAttribute attribute = attributes.next();
%>        
            <tr>
                <td><%= element.getLocalName() %>: <%= attribute.getLocalName() %></td>
                <td><%= attribute.getAttributeValue() %></td>
            </tr>
<% 
        }
    }
    elements = offering.getResultModels();
    for(OMElement element : elements){
        if(!element.getText().trim().equals("")){
%>        
            <tr>
                <td><%= element.getLocalName() %></td>
                <td><%= element.getText() %></td>
            </tr>
<%          
        }
        Iterator<OMAttribute> attributes;
        for (attributes = element.getAllAttributes(); attributes.hasNext();){
            OMAttribute attribute = attributes.next();
%>        
            <tr>
                <td><%= element.getLocalName() %>: <%= attribute.getLocalName() %></td>
                <td><%= attribute.getAttributeValue() %></td>
            </tr>
<% 
        }
    }
}
%>        
        </table>
        <br />
        <br />
        <br />
        <div>To continue, choose a button and click on it:</div>
        <br />
        <br />
        <table>
            <tr>
                <td>
                    <form action="SOSClient">
                        <button>DescribeSensor</button>
                        <input type="hidden" name="request" value="DescribeSensorForm">
                        <input type="hidden" name="soshost" value="<%= storage.getHost() %>">
                    </form>
                </td>
                <td>&nbsp;&nbsp;&nbsp;</td>
                <td>
                    <form action="SOSClient">
                        <button>GetObservation</button>
                        <input type="hidden" name="request" value="GetObservationForm">
                        <input type="hidden" name="soshost" value="<%= storage.getHost() %>">
                    </form>
                </td>
                <td>
                    <button onclick="history.back()">Back</button>
                </td>
            </tr>
        </table>
    </body>
</html>