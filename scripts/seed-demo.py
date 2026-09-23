#!/usr/bin/env python3
"""Create fictional demo resources through the secured API, without clearing data."""
import json, os, sys, urllib.request, urllib.error, http.cookiejar
from datetime import datetime, timedelta, timezone
base = os.environ.get('API_URL', 'http://localhost:8088').rstrip('/') + '/api/v1'
email = os.environ.get('BOOTSTRAP_ADMIN_EMAIL','admin@example.test')
password = os.environ.get('BOOTSTRAP_ADMIN_PASSWORD','AdminPass123!')
demo_password = os.environ.get('DEMO_PASSWORD','DemoPass123!')
if len(demo_password) < 10:
    sys.exit('DEMO_PASSWORD must have at least 10 characters.')
client = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))
token = None

def api(path, method='GET', body=None):
    global token
    if method != 'GET' and token is None:
        token = api('/auth/csrf')
    headers = {'Content-Type': 'application/json'}
    if method != 'GET':
        headers[token['headerName']] = token['token']
    req = urllib.request.Request(base+path, data=None if body is None else json.dumps(body).encode(), method=method, headers=headers)
    try:
        with client.open(req, timeout=30) as response:
            raw = response.read()
            return json.loads(raw) if raw else None
    except urllib.error.HTTPError as error:
        detail = json.loads(error.read())
        raise RuntimeError(f'{method} {path}: {error.code} {detail.get("message", "Request failed")}') from None

api('/auth/login', 'POST', {'email':email, 'password':password})
token = None
staff = api('/admin/staff')
driver = next((s for s in staff if s['email']=='driver.demo@example.test'), None)
if not driver:
    driver = api('/admin/staff','POST',{'email':'driver.demo@example.test','password':demo_password,'displayName':'Demo Driver','phone':'000-DEMO-DRIVER','role':'DRIVER'})
try:
    api('/auth/register','POST',{'email':'passenger.demo@example.test','password':demo_password,'displayName':'Demo Passenger','phone':'000-DEMO-PASSENGER'})
except RuntimeError as e:
    if '409' not in str(e): raise
stops = api('/admin/stops')
ids = []
for name, city in [('Central Terminal','Dhaka'),('Port Terminal','Chattogram')]:
    stop = next((s for s in stops if s['name']==name and s['city']==city), None)
    if not stop:
        stop = api('/admin/stops','POST',{'name':name,'city':city,'address':'Fictional demo terminal','active':True})
    ids.append(stop['id'])
routes=api('/admin/routes')
route=next((r for r in routes if r['code']=='DEMO-DAC-CGP'),None)
if not route:
    route=api('/admin/routes','POST',{'code':'DEMO-DAC-CGP','name':'Dhaka → Chattogram','stopIds':ids,'active':True})
buses=api('/admin/buses')
bus=next((b for b in buses if b['registration']=='DEMO-COACH-01'),None)
if not bus:
    seats=[{'label':chr(65+i//4)+str(i%4+1),'rowNumber':i//4+1,'columnNumber':i%4+1,'blocked':i==39} for i in range(40)]
    bus=api('/admin/buses','POST',{'registration':'DEMO-COACH-01','busType':'Air-conditioned coach','active':True,'seats':seats})
# Read every page before deciding whether an existing departure can be reused.
trips=[]; page=0
while True:
    result=api('/admin/trips?size=100&page='+str(page));trips.extend(result['items']);page+=1
    if page>=result['totalPages']:break
for day in range(1,4):
    departure=(datetime.now(timezone.utc)+timedelta(days=day)).replace(hour=3,minute=0,second=0,microsecond=0)
    when=departure.isoformat().replace('+00:00','Z')
    existing=next((t for t in trips if t['routeId']==route['id'] and t['departureAt']==when and t['status']!='CANCELLED'),None)
    if existing:
        if existing['status']=='DRAFT': api('/admin/trips/'+existing['id']+'/publish','POST')
        continue
    trip=api('/admin/trips','POST',{'routeId':route['id'],'busId':bus['id'],'driverId':driver['id'],'departureAt':when,'arrivalAt':(departure+timedelta(hours=5)).isoformat(),'salesCloseAt':(departure-timedelta(minutes=30)).isoformat(),'fareMinor':120000,'currency':'BDT'})
    api('/admin/trips/'+trip['id']+'/publish','POST')
print('Demo ready: 1 coach, 1 route, 2 terminals, and 3 upcoming departures.')
print('Fictional accounts: passenger.demo@example.test and driver.demo@example.test. Use your DEMO_PASSWORD.')
