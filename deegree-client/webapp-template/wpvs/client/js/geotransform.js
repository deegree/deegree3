function GeoTransform(srcx1, srcy1, srcx2, srcy2, destx1, desty1, destx2, desty2) {
    this.qx = 0.0;
    this.qy = 0.0;
    
    this.srcx1 = srcx1;
    this.srcy1 = srcy1;
    this.srcx2 = srcx2;
    this.srcy2 = srcy2;
    this.destx1 = destx1;
    this.desty1 = desty1;
    this.destx2 = destx2;
    this.desty2 = desty2;

    this.getDestX = getDestX;
    this.getDestY = getDestY;
    this.getSourceX = getSourceX;
    this.getSourceY = getSourceY;
    this.calculateQX = calculateQX;
    this.calculateQY = calculateQY;
    this.calculateQX();
    this.calculateQY();
        
}

function getDestX( srcx ) {
    return this.destx1 + (srcx - this.srcx1) * this.qx;
}

function getDestY( srcy ) {
    return this.desty1 + (this.desty2-this.desty1) - (srcy - this.srcy1) * this.qy;
}

function getSourceX( destx ) {  	  
    return (destx - this.destx1) / this.qx - (-this.srcx1);
}

function getSourceY( desty ) {
    return ( (this.desty2 - this.desty1) - (desty - this.desty1) ) / this.qy - (- this.srcy1);
}

function calculateQX() {
    this.qx = (this.destx2-this.destx1)/(this.srcx2-this.srcx1);
}
    
function calculateQY() {
    this.qy = (this.desty2-this.desty1)/(this.srcy2-this.srcy1);
}