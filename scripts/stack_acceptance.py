#!/usr/bin/env python3
"""Opt-in smoke check against a running Spring stack. Not run in this delivery environment."""
import argparse,json,pathlib,time,urllib.request,urllib.error,uuid
p=argparse.ArgumentParser();p.add_argument('--url',default='http://localhost:8081/governance');p.add_argument('--access',type=pathlib.Path,required=True);p.add_argument('--confirm-synthetic',action='store_true');p.add_argument('--output',type=pathlib.Path,default=pathlib.Path('build/governance-stack-acceptance.json'));a=p.parse_args()
if not a.confirm_synthetic:p.error('--confirm-synthetic is required: this creates and executes a synthetic sandbox refund.')
tokens=json.loads(a.access.read_text());base=a.url.rstrip('/')
def call(method,path,role='operator',body=None,extra=None):
 h={'Authorization':'Bearer '+tokens[role]}
 if extra:h.update(extra)
 if body is not None:h['Content-Type']='application/json'
 req=urllib.request.Request(base+path,headers=h,method=method,data=json.dumps(body).encode() if body is not None else None)
 try:
  with urllib.request.urlopen(req,timeout=20) as res:return json.load(res)
 except urllib.error.HTTPError as e:raise RuntimeError(f'HTTP {e.code}: '+e.read().decode()[:500]) from e

def wait_case(id,states):
 deadline=time.monotonic()+90
 while time.monotonic()<deadline:
  c=call('GET','/api/cases/'+id)
  if c['state'] in states:return c
  if c['state'] in ['BLOCKED','ESCALATED','EXPIRED','INVALIDATED','DECLINED']:raise RuntimeError('Unexpected terminal state '+c['state'])
  time.sleep(1)
 raise RuntimeError('Timed out; inspect Kafka, Camunda, outbox and worker logs')

# Never calls /plan or /run: automatic progression must use the configured outbox and Camunda workers.
c=call('POST','/api/cases',body={'orderId':'ORD-1001','request':'Please refund my unavailable order.'},extra={'Idempotency-Key':str(uuid.uuid4())})
c=wait_case(c['id'],{'PENDING_APPROVAL'})
call('POST',f'/api/cases/{c["id"]}/decisions','reviewer',{'proposalDigest':c['proposalDigest'],'approve':True,'reason':'Synthetic acceptance: reviewed exact refund and evidence.'})
completed=wait_case(c['id'],{'EXECUTED'})
if completed.get('receipt',{}).get('sandbox') is not True:raise RuntimeError('Expected sandbox receipt')
result={'status':'PASSED','caseId':c['id'],'receiptId':completed['receipt']['id'],'sandbox':True,'verified':['HTTP case creation','automatic planning without /plan','automatic execution without /run','sandbox receipt'],'stillRequired':['inspect Camunda process completion and Kafka event delivery','broker restart and message replay tests','Camunda worker-kill fault injection','PostgreSQL failover and load testing']}
a.output.parent.mkdir(parents=True,exist_ok=True);a.output.write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result,indent=2))
