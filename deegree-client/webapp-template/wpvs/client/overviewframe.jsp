<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Internal Frame for Overview Map</title>
<!-- script LANGUAGE="JavaScript1.2" TYPE="text/javascript" src="js/mapnav_functions.js"></script>
<script LANGUAGE="JavaScript1.2" TYPE="text/javascript" src="js/wz_jsgraphics_box.js"></script-->
<script LANGUAGE="JavaScript1.2" TYPE="text/javascript" src="js/envelope.js"></script>
<script LANGUAGE="JavaScript1.2" TYPE="text/javascript" src="js/wz_jsgraphics.js"></script>
<script LANGUAGE="JavaScript1.2" TYPE="text/javascript" src="js/box2.js"></script>
<script LANGUAGE="JavaScript1.2" TYPE="text/javascript" src="js/event_handler.js"></script>
<script>
<!--
	var box = null;
	var isNav = true;
	var initX = 0;
	var initY = 0;
    var evnt = 0; 
	var mode = "zoomin";
	var loading;
  var count = 0;
	function init(){
		//All defined in js/event_handler.js
		document.onmousemove = initMouseMove;
		document.onmousedown = initMouseDown;
		document.onmouseup = initMouseUp;
		parent.overviewFrame = this;
		if ( box == null ) {
			box = new DragBox (parent.oviewHeight, parent.oviewHeight, 2, 2, '#FF0000');
			box.init();
			box.setListener( parent );
		}
        //defined in js/event_handler.js
		initEventHandling();
	}
	
	/**
	* Sets the mode e.g. zoomin, zoomout or move
	*/
	function setMode( m ){
		mode = m;
		if( box != null ){
			box.clear();
		}
	}
	
    /**
    * @param r the wms request.
    * Set the request to the wms and update the overview image
    */
	function setWMSRequest( r ){
		var imgNode = document.getElementById( 'overviewImg' );
		if( imgNode != null ){
            var node = document.getElementById( 'overviewImgNode' );
            node.style.display='block';
    
            //set the imgNode 
            imgNode.style.display='block';
			imgNode.src = r;
            var noImageNode = document.getElementById( 'noImage' );
            if( noImageNode != null ){
               noImageNode.style.display='none';
            }
		} 
		loading = window.setInterval("checkImageState()", 500 );		
	}
	
	function checkImageState(){
        var imgNode = document.getElementById( 'overviewImg' );
        //some dirty hacks:
        //if the overview image couldnot find the wms, the browser uses it's no image image, which is smaller as 50 pixels, 
        //the image is therefore completed *arghhhh*. The counter is a safe guard for slow wms's
	  	if( count++ == 10 || document.images[0].complete || imgNode.width > 50 ){
            var node = document.getElementById( 'overviewImgNode' );
            node.style.top = "0px";
            node.style.left = "0px";
            if( ( count == 11 && !document.images[0].complete ) || imgNode.width < 50 ){
              node.style.display='none';

              //display the no image availabe.
              var noImageNode = document.getElementById( 'noImage' );
              if( noImageNode != null ){
                noImageNode.style.display='block';
              }
           }	  	
			window.clearInterval(loading);
            count = 0;
        }
	}
	
-->
</script>
</head>
<!-- initDragBox is definded in event_handler.js -->
<body onload="init()" marginheight="0" marginwidth="0"-->
<!-- div id="progressImgNode" style="position:absolute; top:0px; left:10px; z-index:10;">
  <img id="progress" src="../images/empty.gif">
</div-->
<div id="overviewImgNode" style="position: absolute; top: 0px; left: 0px; z-index: 9;"><img id="overviewImg"
  src=""></div>
<div id="noImage" style="font-size: smaller; position: absolute; top: 0px; left: 0px; z-index: 6; display: none;">
<br />
No image<br />
available<br />
or<br>
no wms defined<br />
<br />
</div>
</body>
</html>
