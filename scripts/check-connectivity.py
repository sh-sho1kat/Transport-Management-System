#!/usr/bin/env python3
"""Local HTTP smoke check. Creates and cancels one fictional booking after demo seeding."""
import json, urllib.request, urllib.error, http.cookiejar, uuid
class Client:
    def __init__(self, origin='http://localhost:8088'):
        self.origin=origin;self.client=urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))
    def call(self,path,method='GET',body=None,key=None,expected=200):
        headers={'Content-Type':'application/json'}
        if method!='GET':
            token=self.call('/api/v1/auth/csrf');headers[token['headerName']]=token['token']
        if key:headers['Idempotency-Key']=key
        req=urllib.request.Request(self.origin+path,data=None if body is None else json.dumps(body).encode(),headers=headers,method=method)
        try:
            with self.client.open(req,timeout=20) as res:status=res.status;raw=res.read()
        except urllib.error.HTTPError as error:status=error.code;raw=error.read()
        assert status==expected,f'{method} {path}: expected {expected}, got {status}'
        return json.loads(raw) if raw else None
    def login(self,email,password):return self.call('/api/v1/auth/login','POST',{'email':email,'password':password})
checks=[]
client=Client();assert client.call('/actuator/health')['status']=='UP';checks.append('Backend health and database health')
for role,email,password in [('ADMIN','admin@example.test','AdminPass123!'),('DRIVER','driver.demo@example.test','DemoPass123!'),('PASSENGER','passenger.demo@example.test','DemoPass123!')]:
    c=Client();assert c.login(email,password)['role']==role;assert c.call('/api/v1/me')['role']==role;checks.append(role+' credentials and session')
client.login('passenger.demo@example.test','DemoPass123!')
trip=client.call('/api/v1/trips')['items'][0]
seat=next(s for s in client.call('/api/v1/trips/'+trip['id']+'/seats') if s['status']=='AVAILABLE')
hold=client.call('/api/v1/holds','POST',{'tripId':trip['id'],'seatNos':[seat['label']]},expected=201)
body={'holdId':hold['id'],'contactName':'Connectivity Demo','contactEmail':'passenger.demo@example.test','contactPhone':'000-CHECK'}
key=str(uuid.uuid4());booking=client.call('/api/v1/bookings','POST',body,key,201)
assert client.call('/api/v1/bookings','POST',body,key)['id']==booking['id'];checks.append('Hold, booking, and identical retry')
assert client.call('/api/v1/bookings/'+booking['id']+'/cancel','POST',{'reason':'Local connectivity verification'})['status']=='CANCELLED';checks.append('Booking cancellation')
client.call('/api/v1/admin/buses',expected=403);checks.append('Passenger blocked from administrator endpoint')
proxy=Client('http://localhost:5178');assert proxy.call('/api/v1/trips')['totalItems']>0
assert proxy.login('admin@example.test','AdminPass123!')['role']=='ADMIN';checks.append('Frontend proxy, CSRF and admin session')
with urllib.request.urlopen('http://localhost:5178',timeout=10) as response:assert b'<div id="root">' in response.read()
checks.append('Frontend HTML on port 5178')
print(json.dumps({'passed':len(checks),'checks':checks},indent=2))
