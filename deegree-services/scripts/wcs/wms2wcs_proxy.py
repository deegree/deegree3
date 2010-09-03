#!/usr/bin/env python
"""
=============================================================
This is a proxy that converts WMS requests into WCS requests.
You have to alter the wms_capabilities.xml document to match
the coverages of your service.

To run you need python, and werkzeug.
On a debian system you can install the requirements with:
%> sudo apt-get install python-setuptools
%> sudo easy_install werkzeug

Then start with:
python wms2wcs_proxy.py runproxy
=============================================================
"""
__author__ = 'Oliver Tonnhofer'
__version__ = '0.0.1'

from werkzeug import script
from werkzeug import Request, Response
from werkzeug.exceptions import InternalServerError
from urllib import urlencode
from urllib2 import urlopen, HTTPError

WCS_WMS_MAP = dict(COVERAGE='LAYERS', CRS='SRS',
                   BBOX='BBOX', FORMAT='FORMAT',
                   WIDTH='WIDTH', HEIGHT='HEIGHT') 

WCS_URL='http://localhost:8080/d3/services?'
WMS_CAPABILITIES='wms_capabilities.xml'

def wcs_proxy(req):
    if req.args.get('REQUEST') == 'GetCapabilities':
        capa = open(WMS_CAPABILITIES).read()
        return Response(capa, mimetype='text/xml')
    wcs_args = convert_request_args(req.args)
    url = WCS_URL + urlencode(wcs_args)
    print 'WCS Request:', url
    resp = get_url(url)
    return Response(resp, mimetype=wcs_args['FORMAT'])

def get_url(url):
    try:
        return urlopen(url)
    except HTTPError, e:
        print e.read()
        raise InternalServerError

def convert_request_args(args):
    wcs_args = dict(REQUEST='GetCoverage', SERVICE='WCS', VERSION='1.0.0')
    for wcs_key, wms_key in WCS_WMS_MAP.items():
        wcs_args[wcs_key] = args.get(wms_key)
    return wcs_args

def wsgi_app(environ, start_response):
    req = Request(environ)
    if req.path.startswith('/services'):
        resp = wcs_proxy(req)
    else:
        resp = Response("nothing here, but look out for services", mimetype='text/plain')
    return resp(environ, start_response)

def make_app():
    return wsgi_app

action_runproxy = script.make_runserver(make_app, use_reloader=True)
action_runproxy.__doc__ = "Start the WMS2WCS proxy" 

script.run()
