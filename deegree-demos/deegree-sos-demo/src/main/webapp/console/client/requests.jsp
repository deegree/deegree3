<%-- $HeadURL$ --%>
<%-- $Id$ --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isErrorPage="false" errorPage="error.jsp"
	import="java.io.*,java.awt.event.*"%>
<%
final File dir = new File( getServletContext().getRealPath( "/client/requests" ) );

final PrintWriter o = new PrintWriter(out);

ActionListener listener = new ActionListener() {
    
    private void go(File[] fs) throws IOException{
        if(fs == null) return;
        for(File f : fs){
            if(f.isDirectory()){
               	o.println("DIR " + f.getName());
               	go(f.listFiles());
                o.println("END DIR");
            }else{
                o.println("REQUEST " + f.getName());
            }
        }
    }
    
    public void actionPerformed(ActionEvent evt){
        try{
	        go(dir.listFiles());
        }catch(IOException e){
            // eat it, it's a JSP...
        }
	}
};

listener.actionPerformed(null);
%>
