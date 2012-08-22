<%
	String message = (String)request.getAttribute("MESSAGE");
%>
<html>
   <head>
      <title>CSW-Registry with eBRIM-profile authentication error</title>
   </head>
   <body>
      <p>Missing rights for delivering response to request:</p>
<%
	out.println(message);
%>      
   </body>
</html>
