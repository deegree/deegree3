function Envelope(minx, miny, maxx, maxy) {
	this.minx = minx;
	this.miny = miny;
	this.maxx = maxx;
	this.maxy = maxy;
	
	this.toString  = toString;
	
	function toString() {
		return "" + this.minx + "," + this.miny + "," + this.maxx + "," + this.maxy;
	}
}