/*http://10.19.1.220:8081/deegree/ogcwebservice?
request=GetView&
BOUNDINGBOX=3564879.89,5935040.21,3566520.11,5936759.79&
DATASETS=Hamburg_Overview&
POI=3564880,5935050,5&
YAW=0&PITCH=15&
ROLL=0&AOV=60&
DISTANCE=1500&
FARCLIPPINGPLANE=6000&
CRS=EPSG:31467&
WIDTH=800&
HEIGHT=600&SCALE=1.0&
STYLES=default&
DATETIME=2006-01-21T12:30:00&
SPLITTER=QUAD&
VERSION=1.0.0&
OUTPUTFORMAT=image/png
*/
function WPVSRequest( url, datasets, elevModel, bbox, poi, step, yaw, pitch, distance, crs, width, height, splitter, background ){

	this.background = background;
	this.url = url;
	this.bbox = bbox;
	this.datasets = datasets;
	this.elevModel = elevModel;
	this.poi = poi;
	this.step = step;
	this.yaw = yaw;
	this.aov = 40;
	this.pitch = pitch;
	this.roll = 0;
	this.distance = distance;
	this.crs = crs;
	this.width = width;
	this.height = height;
	this.splitter = splitter;	

	//functions
	this.setDatasets = setDatasets;
	this.setBbox = setBbox;
	this.setPOI = setPOI;
	this.setStep = setStep;
	this.setYaw = setYaw;
	this.setPitch = setPitch;
	this.setDistance = setDistance;
	this.setSplitter = setSplitter;
	this.createRequest = createRequest;
	this.setElevationModel = setElevationModel;
	this.getDistance = getDistance;
	
	this.getPOI = getPOI;
	this.getStep = getStep;
	this.getYaw = getYaw;
	this.getBboxAsArray = getBboxAsArray;
	this.setBboxArray = setBboxArray;
	this.getBbox = getBbox;
	this.getCRS = getCRS;
	
	function setDatasets( ds ){
		this.datasets = ds;
	}

	function setBbox( bbox ){
		this.bbox = bbox;
	}

	function setPOI( poi ){
		this.poi = poi;
	}
	
	function setStep( step ){
		this.step = step;
	}

	function setYaw( yaw ){
		this.yaw = yaw;
	}
	
	function setDistance( d ){
		this.distance = d;
		// rescale BBOX
//		var box = this.getBboxAsArray();
//		var cx = box[0] + ( box[2] - box[0] ) / 2.0;
//		var cy = box[1] + ( box[3] - box[1] ) / 2.0;
//		var d2 = d / 2.0;
//		var env = new Envelope( cx - d2, cy -d2, cx + d2, cy + d2 );
//		this.setBbox( env.toString() );
	}
	
	function setSplitter( s ){
		this.splitter = s;
	}
	
	function setPitch( p ){
		this.pitch = p;
	}
	
	function setElevationModel( em ){
		this.elevModel = em;
	}
	
	function getYaw(){
		return this.yaw;
	}
	
	function getPOI(){
		return this.poi;
	}
	
	function getStep(){
		return this.step;
	}
	
	function getDistance(){
		return this.distance;
	}
	
	function getBboxAsArray(){
		var s = this.bbox.split( "," );
		//alert(s[0]+","+s[1]+","+s[2]+","+s[3])
		return new Array( parseFloat(s[0]),
						  parseFloat(s[1]),
						  parseFloat(s[2]),
						  parseFloat(s[3]));
	}
	
	function setBboxArray( minx, miny, maxx, maxy ){
	
		var newBox = "" + String(minx.toFixed(2)) + "," 
						+ String(miny.toFixed(2)) + "," 
						+ String(maxx.toFixed(2)) + "," 
						+ String(maxy.toFixed(2)); 

		this.bbox = newBox;		
	}
	
	function getBbox(){
		return this.bbox;
	}
	
	function getCRS(){
		return this.crs;
	}
	
	function createRequest(){
	
		//var req = this.url + "request=GetView&BOUNDINGBOX=\n"+ this.bbox
		var req = this.url + "request=GetView&BOUNDINGBOX=\n"+ this.bbox + "&DATASETS=" + this.datasets;
		
		if( this.elevModel != null && this.elevModel != '' ){
			req += "&ELEVATIONMODEL=" + this.elevModel;
		}
		req += "&ROLL="+ this.roll
		+ "&AOV=" + this.aov
		+ "&FARCLIPPINGPLANE=30000&CRS="+ this.crs + "&WIDTH="+this.width
		+ "&HEIGHT=" + this.height + "&SCALE=1.0&STYLES=default"
		+ "&DATETIME=2007-03-21T12:00:00&EXCEPTIONFORMAT=INIMAGE"
		//+ "&SPLITTER="+ this.splitter 
		+ "&VERSION=1.0.0&OUTPUTFORMAT=image/png";
		if ( this.background != null ) {
			req = req + "&background=" + this.background;
		}
        req += "&POI="+ this.poi 
        + "&YAW=" + this.yaw 
        + "&PITCH=" + this.pitch 
        + "&DISTANCE=" + this.distance 
		
		return req;
	}

}