#!/usr/bin/env python3
"""Offline legacy MongoDB export review. Never connects to or writes a database."""
import argparse, hashlib, json, os, uuid
from pathlib import Path
parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('export',type=Path,help='JSON with trips, seatCollections, users, locations and times')
parser.add_argument('--output',type=Path,required=True,help='New private report directory')
args=parser.parse_args()
raw=args.export.read_bytes();source=json.loads(raw)
if not isinstance(source,dict) or not isinstance(source.get('trips'),list) or not isinstance(source.get('seatCollections'),dict):
    parser.error('Export must contain trips (array) and seatCollections (object keyed by exact legacy tripID).')
args.output.mkdir(mode=0o700,parents=True,exist_ok=False)
namespace=uuid.UUID('d33ac17b-88fc-4d20-a9d9-9c5f3c905f64')
def legacy_id(value):return str(value.get('$oid')) if isinstance(value,dict) and '$oid' in value else str(value or '')
def mapped(kind,key):return str(uuid.uuid5(namespace,kind+':'+key))
plan=[];exceptions=[];mapping=[];seen=set();booked=0;seat_count=0
for i,t in enumerate(source['trips']):
    key=legacy_id(t.get('_id'));trip_key=str(t.get('tripID',''))
    if not key or not trip_key or trip_key in seen:
        exceptions.append({'record':i,'legacyId':key,'reason':'Missing identifier or duplicate tripID; cannot safely associate seat collection.'});continue
    seen.add(trip_key);new_id=mapped('trip',key);mapping.append({'kind':'trip','legacyId':key,'legacyTripId':trip_key,'proposedUuid':new_id})
    seats=source['seatCollections'].get(trip_key)
    if seats is None:exceptions.append({'legacyId':key,'reason':'Missing seat collection mapping.'});seats=[]
    labels=set();candidate=[]
    for j,s in enumerate(seats):
        seat_count+=1;label=str(s.get('seatNo','')).strip();status=s.get('bookingStatus')
        if not label or label in labels or status not in ['booked','unbooked']:
            exceptions.append({'legacyId':key,'seatIndex':j,'reason':'Empty/duplicate label or unknown booking status.'});continue
        labels.add(label);sid=mapped('seat',key+':'+label);mapping.append({'kind':'seat','legacyId':legacy_id(s.get('_id')),'legacyTripId':trip_key,'proposedUuid':sid})
        row={'proposedId':sid,'label':label,'legacyStatus':status,'historicalSource':s,'accountClaim':'UNCLAIMED' if status=='booked' else None}
        if status=='booked':
            booked+=1;row['proposedHistoricalBookingId']=mapped('historical-booking',key+':'+label)
            exceptions.append({'legacyId':key,'seat':label,'reason':'Historical reservation requires verified account claim, contact review, fare/payment evidence. Never infer grouping from timestamps.'})
        candidate.append(row)
    plan.append({'proposedTripId':new_id,'legacyTripId':trip_key,'legacySource':t,'inventoryCandidates':candidate,'eligibleForActiveImport':False,'requires':['verified physical bus','ordered route and stops','timezone and exact departure','arrival','assigned driver','documented fare/currency','sales and cancellation policy','seat layout coordinates']})
    exceptions.append({'legacyId':key,'reason':'Legacy trip lacks required scheduling/fare/account evidence. Keep offline until reviewed.'})
for key in source['seatCollections']:
    if key not in seen:exceptions.append({'collectionKey':key,'reason':'Unmatched seat collection; preserve without importing.'})
summary={'sourceSha256':hashlib.sha256(raw).hexdigest(),'sourceTripCount':len(source['trips']),'reviewableTripCount':len(plan),'matchedSeatRecordCount':seat_count,'historicalBookedSeatCandidates':booked,'sourceUserCount':len(source.get('users',[])),'sourceLocationCount':len(source.get('locations',[])),'sourceTimeCount':len(source.get('times',[])),'activeRecordsImported':0,'exceptionCount':len(exceptions),'policy':'DRY_RUN_ONLY_NO_DATABASE_WRITES'}
for name,value in [('summary',summary),('id-mapping',mapping),('review-plan',plan),('exceptions',exceptions),('legacy-reference',{'users':source.get('users',[]),'locations':source.get('locations',[]),'times':source.get('times',[])})]:
    path=args.output/(name+'.json')
    fd=os.open(path,os.O_WRONLY|os.O_CREAT|os.O_EXCL,0o600)
    with os.fdopen(fd,'w') as f:json.dump(value,f,indent=2,ensure_ascii=False);f.write('\n')
print(json.dumps(summary,indent=2))
