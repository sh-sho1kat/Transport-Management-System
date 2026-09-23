#!/usr/bin/env python3
"""Generate the checked-in OpenAPI contract from this project's typed DTOs/controllers.
Intentionally supports the project's restricted Java record/controller conventions.
Use --check in CI to fail when the checked-in contract is stale.
"""
import json, re, sys
from pathlib import Path
root=Path(__file__).resolve().parents[1]
java=root/'backend/src/main/java/com/tms'
schemas={}
for name,values in re.findall(r'enum\s+(\w+)\s*\{([^}]+)\}',(java/'entity/Types.java').read_text()):
    schemas[name]={'type':'string','enum':[v.strip() for v in values.split(',')]}
def ref(name):return {'$ref':'#/components/schemas/'+name}
def schema(t):
    if t.startswith('ResponseEntity<'):return schema(t[15:-1])
    if t.startswith('PageResult<'):return ref('Page'+t[11:-1])
    if t.startswith('List<') or t.startswith('java.util.List<'):return {'type':'array','items':schema(t[t.index('<')+1:-1])}
    return {'String':{'type':'string'},'UUID':{'type':'string','format':'uuid'},'Instant':{'type':'string','format':'date-time'},'LocalDate':{'type':'string','format':'date'},'int':{'type':'integer','format':'int32'},'long':{'type':'integer','format':'int64'},'double':{'type':'number'},'boolean':{'type':'boolean'}}.get(t,ref(t))
for file in ['dto/request/Requests.java','dto/response/Responses.java']:
    request='request/' in file
    for name,body in re.findall(r'public record\s+(\w+)(?:<[^>]+>)?\s*\((.*?)\)\s*\{\s*\}',(java/file).read_text(),re.S):
        if name in ('PageResult','Confirmation'):continue
        # Strip annotation argument commas before separating record components.
        marked=re.sub(r'(@\w+)\(([^)]*)\)',lambda m:m[0].replace(',',';'),body)
        properties={};required=[]
        for field in marked.split(','):
            clean=re.sub(r'@\w+(?:\([^)]*\))?\s*','',field).strip()
            t,fieldname=clean.rsplit(None,1);prop=schema(t)
            size=re.search(r'@Size\((.*?)\)',field)
            if size:
                for key,value in re.findall(r'(min|max)\s*=\s*(\d+)',size[1]):prop[('min' if key=='min' else 'max')+('Items' if t.startswith('List<') else 'Length')]=int(value)
            for annotation,key in [('Min','minimum'),('Max','maximum')]:
                match=re.search('@'+annotation+r'\((\d+)\)',field)
                if match:prop[key]=int(match[1])
            if '@NotBlank' in field and prop.get('type')=='string':prop['minLength']=max(1,prop.get('minLength',0))
            if '@NotEmpty' in field and prop.get('type')=='array':prop['minItems']=max(1,prop.get('minItems',0))
            if '@Email' in field:prop['format']='email'
            pattern=re.search(r'@Pattern\(regexp="([^"]+)"\)',field)
            if pattern:prop['pattern']=pattern[1]
            if fieldname=='password':prop['format']='password';prop['writeOnly']=True
            properties[fieldname]=prop
            if not request or re.search('@Not(Null|Blank|Empty)',field) or t in ['int','long','boolean']:required.append(fieldname)
        schemas[name]={'type':'object','properties':properties,'required':required}
        if request:schemas[name]['additionalProperties']=False
for item in ['TripView','BookingView','Occupancy','AuditView']:
    schemas['Page'+item]={'type':'object','required':['items','page','size','totalItems','totalPages'],'properties':{'items':{'type':'array','items':ref(item)},'page':{'type':'integer'},'size':{'type':'integer'},'totalItems':{'type':'integer','format':'int64'},'totalPages':{'type':'integer'}}}
paths={}
for file in sorted((java/'controller').glob('*.java')):
    text=file.read_text();base=re.search(r'@RequestMapping\("([^"]+)"\)',text)[1]
    pattern=r'@(Get|Post|Patch|Delete)Mapping\((.*?)\)\s*(?:@ResponseStatus\([^)]+\)\s*)?public\s+([\w<>.?]+)\s+\w+\((.*?)\)\s*\{'
    for match in re.finditer(pattern,text,re.S):
        verb,mapped,result,args=match.groups();verb=verb.lower()
        for suffix in re.findall(r'"([^"]+)"',mapped):
            path=base+suffix
            public='/auth/' in path or verb=='get' and (path=='/api/v1/stops' or path.startswith('/api/v1/trips'))
            role='ADMIN' if '/admin/' in path else 'ADMIN or COUNTER_STAFF' if '/counter/' in path else 'assigned DRIVER' if '/driver/' in path else 'PASSENGER (owner)' if '/holds' in path or path=='/api/v1/bookings' else 'ADMIN, COUNTER_STAFF, or assigned DRIVER' if path.endswith('/payment') else 'owner, ADMIN, or COUNTER_STAFF' if '/bookings/' in path else 'authenticated account'
            security={} if public else {'SessionCookie':[]}
            if verb!='get':security['CsrfHeader']=[]
            operation={'summary':verb.upper()+' '+suffix,'description':('Public endpoint.' if public else 'Access: '+role+'. Service-level ownership and state checks also apply.')+(' Get /auth/csrf first, retain the session cookie, and send the returned token in X-CSRF-TOKEN.' if verb!='get' else ''),'tags':[file.stem.replace('Controller','')],'security':[security] if security else [],'parameters':[],'responses':{}}
            for field in re.findall(r'\{(\w+)\}',path):operation['parameters'].append({'name':field,'in':'path','required':True,'schema':{'type':'string','format':'uuid'}})
            for ann,t,name in re.findall(r'@RequestParam(?:\(([^)]*)\))?\s+(\w+)\s+(\w+)',args):
                param={'name':name,'in':'query','required':False,'schema':schema(t)}
                default=re.search('defaultValue="([^"]+)"',ann)
                if default:param['schema']['default']=int(default[1]) if t=='int' else default[1]
                if name=='page':param['schema']['minimum']=0
                if name=='size':param['schema'].update(minimum=1,maximum=100)
                operation['parameters'].append(param)
            request=re.search(r'@RequestBody\s+(\w+)',args)
            if request:operation['requestBody']={'required':True,'content':{'application/json':{'schema':ref(request[1])}}}
            if 'Idempotency-Key' in args:operation['parameters'].append({'name':'Idempotency-Key','in':'header','required':True,'schema':{'type':'string','pattern':'^[A-Za-z0-9_-]{8,100}$'},'description':'Persist one key and identical request body until confirmation is resolved. Same key with another body returns 409.'})
            code='204' if 'NO_CONTENT' in match[0] else '201' if 'CREATED' in match[0] or verb=='post' and suffix in ('/bookings','/counter/bookings') else '200'
            operation['responses'][code]={'description':'Success'}
            if result!='void':operation['responses'][code]['content']={'application/json':{'schema':schema(result)}}
            if verb=='post' and suffix in ('/bookings','/counter/bookings'):
                operation['responses']['200']={'description':'Idempotent replay; same booking ID, with current booking state.','content':{'application/json':{'schema':ref('BookingView')}}}
                for status in ['200','201']:operation['responses'][status]['headers']={'Idempotent-Replayed':{'schema':{'type':'boolean'}}}
            for status,label in [('400','Validation or malformed input'),('401','Session absent or revoked'),('403','Role denied or invalid CSRF token'),('404','Missing or concealed resource'),('409','Seat, schedule, state, or idempotency conflict'),('429','Rate limit exceeded'),('500','Unexpected server failure')]:operation['responses'][status]={'description':label,'content':{'application/json':{'schema':ref('Error')}}}
            paths.setdefault(path,{})[verb]=operation
spec={'openapi':'3.0.3','info':{'title':'Wayline Bus Operations API','version':'2.0.0','description':'Single-operator bus reservations. UUID identifiers; UTC timestamps; integer currency minor units. Counter sales and cash payment recording are supported; this API does not transfer money. All mutation requests require a CSRF token, including login. Exact CORS origins only. Page size 1–100. Resource catalog lists are unpaginated for a small operator.'},'servers':[{'url':'http://localhost:8088'}],'paths':paths,'components':{'securitySchemes':{'SessionCookie':{'type':'apiKey','in':'cookie','name':'JSESSIONID'},'CsrfHeader':{'type':'apiKey','in':'header','name':'X-CSRF-TOKEN'}},'schemas':schemas}}
# Verify every reference resolves, so controller/DTO changes cannot silently produce invalid schemas.
for name in re.findall(r'#/components/schemas/([\w]+)',json.dumps(spec)):
    assert name in schemas, f'Unknown schema: {name}'
output=json.dumps(spec,indent=2,ensure_ascii=False)+'\n';target=root/'docs/openapi.json'
if '--check' in sys.argv:
    assert target.read_text()==output,'OpenAPI is stale; run python3 scripts/openapi.py'
    print(f'OpenAPI contract verified: {sum(len(v) for v in paths.values())} operations, {len(schemas)} schemas.')
else:
    target.write_text(output);print('Wrote '+str(target))
