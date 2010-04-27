function setModeForClient( m ){
  overviewFrame.setMode( m );
  var button = null;
  for(var i=0;i< buttons.length;i++){
    button = document.getElementById( buttons[i] );
    var tmpButton = buttons[i];
    if( buttons[i].toLowerCase() == m.toLowerCase() ){
      tmpButton += "_a";
    }
    button.src = "../images/" + tmpButton + ".gif";
  }
}
	
function initGeoTransform(){
  var b = wpvsRequest.getBboxAsArray();

  gtrans = new GeoTransform( b[0], b[1], b[2], b[3], 0, 0, oviewWidth-1, oviewHeight-1 );

}

function setSplitter( s ){
  wpvsRequest.setSplitter( s );
  redraw(false);
}
	
function setFooterText( text ){
  setElementText( text, 'footerTxtArea' );
}

function setElementText( text, elementId ){
  var txtArea = document.getElementById( elementId );
		
  var node = txtArea.firstChild;
  if ( node != null && txtArea != null ){
    txtArea.removeChild( node );
  }
	
  node = document.createTextNode( text );
  txtArea.appendChild( node );
}
		
