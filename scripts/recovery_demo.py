#!/usr/bin/env python3
"""Run a real isolated worker crash and server restart; never touches .local or external systems."""
import json,pathlib,sys
from build import build,ROOT
build();sys.path.insert(0,str(ROOT/'tests'))
from support import Server
s=Server()
try:
 c=s.approve(s.plan(s.open()));crash=s.worker(crash=True)
 if crash.returncode!=73:raise RuntimeError('Expected actual worker exit 73, got '+str(crash.returncode))
 before=s.request('GET','/api/cases/'+c['id'])[1];receipt=before['receipt']['id']
 s.restart();replay=s.worker()
 if replay.returncode!=0:raise RuntimeError(replay.stderr)
 after=s.request('GET','/api/cases/'+c['id'])[1];state=json.loads((s.folder/'state/governance-state.json').read_text())
 assert after['receipt']['id']==receipt
 assert len(state['effects'])==1
 assert state['orders']['demo/ORD-1001']['refundedCents']==12900
 assert state['jobs'][c['id']]['attempts']==2
 assert state['jobs'][c['id']]['state']=='COMPLETED'
 result={'status':'PASSED','scenario':'Real standalone worker halted after commit; real server killed/restarted; fresh worker replayed and acknowledged.','workerExitCode':crash.returncode,'caseId':c['id'],'receiptId':receipt,'effects':len(state['effects']),'refundedCents':12900,'attempts':2,'jobState':'COMPLETED','sandbox':True,'notClaimed':'Camunda worker fault injection or real payment-provider effect.'}
 out=ROOT/'build/demo';out.mkdir(parents=True,exist_ok=True);(out/'crash-recovery.json').write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result,indent=2))
finally:s.close()
