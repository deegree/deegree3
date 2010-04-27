<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Internal Frame for Overview Map</title>
<script LANGUAGE="JavaScript1.2" TYPE="text/javascript"	src="js/mapnav_functions.js"></script>
<script LANGUAGE="JavaScript1.2" TYPE="text/javascript" src="js/box2.js"></script>
<script LANGUAGE="JavaScript1.2" TYPE="text/javascript"	src="js/wz_jsgraphics.js"></script>
<script LANGUAGE="JavaScript1.2" TYPE="text/javascript"	src="js/wz_jsgraphics_box.js"></script>
<script LANGUAGE="JavaScript1.2" TYPE="text/javascript"	src="js/envelope.js"></script>
<script LANGUAGE="JavaScript1.2" TYPE="text/javascript"	src="js/event_handler.js"></script>


<script>
<!--
  var loadingGetViewImage;
      
  
function init(){    
  parent.getviewFrame = this;    
}

/**
 * A function to get the image from a wpvs and set it to this frame top image.
 */  
function setWPVSRequest( r, viewWidth, viewHeight ){
    
  var imgNode = document.getElementById( 'getviewImg' );
  if( imgNode != null ){
    //imgNode.src = r;
  }
    
    
  var getviewImgNode = document.getElementById( 'getviewImgNode' );
    
  var children = getviewImgNode.childNodes;
  for( var i = 0;i<children.length;i++){
    //getviewImgNode.removeChild( children[i] );
    children[i].id = "old_" + i;
  }
  var x = new Image( viewWidth, viewHeight );
  //This is the place, where the wpvs is called.
  x.src = r;
  x.id = 'newImg';
  getviewImgNode.appendChild( x );
    
  imgNode = document.getElementById( 'progress' );
  if( imgNode != null ){
    imgNode.src = '../images/progress.gif';
  }
  loadingGetViewImage = window.setInterval("checkWPVSImageState()", 1000 );    
}
  
function checkWPVSImageState(){
  var finished = true;    
  for (i = 0; i < document.images.length; ++i) {
    if ( !document.images[i].complete ) {
      //              && document.images[i].id.indexOf('view') < 0 ) {
      finished = false;
    } 
  }
  if( finished ){
      
    //clear progress monitor
    node = document.getElementById( 'progress' );
    node.src = '../images/empty.gif';
            
    var getviewImgNode = document.getElementById( 'getviewImgNode' );
              
    var children = getviewImgNode.childNodes;
    for( var i = 0;i<children.length;i++){
      if( children[i].id != 'newImg' ){
	getviewImgNode.removeChild( children[i] );
      }
    }
    window.clearInterval(loadingGetViewImage);
  }
}
  
-->
</script>

</head>
<body onload="init();"
  marginheight="0" marginwidth="0" >
  
  <div id="progressImgNode" style="position:absolute; top:250px; left:550px; z-index:10;">
    <img id="progress"  src="">
  </div>  
  
  <div id="getviewImgNode" style="position:absolute; top:0px; left:0px; z-index:9;">
    
  </div>
</body>
</html>
