#!/usr/bin/env python3
"""Start a local synthetic demo; create expiring credentials outside the repository only when absent."""
import argparse, hashlib, json, os, pathlib, secrets, subprocess, sys
from datetime import datetime, timezone, timedelta
from build import build,ROOT

def make_credentials(folder,force=False):
    folder.mkdir(parents=True,exist_ok=True)
    auth=folder/'auth.json';access=folder/'access.json'
    if auth.exists() and not force:
        if not access.exists(): raise SystemExit('Auth exists without access.json; use --rotate-credentials explicitly.')
        return json.loads(access.read_text())
    identities=[];tokens={}
    for name,roles,tenant in [('operator',['READ','OPERATOR'],'demo'),('reviewer',['READ','REVIEWER'],'demo'),('worker',['READ','WORKER'],'demo'),('viewer',['READ'],'demo'),('other',['READ','OPERATOR'],'other')]:
        token=secrets.token_urlsafe(32);tokens[name]=token
        identities.append({'id':name,'tenant':tenant,'roles':roles,'tokenHash':hashlib.sha256(token.encode()).hexdigest(),'expiresAt':(datetime.now(timezone.utc)+timedelta(hours=24)).isoformat().replace('+00:00','Z')})
    auth.write_text(json.dumps({'identities':identities},indent=2)+'\n');access.write_text(json.dumps(tokens,indent=2)+'\n')
    try: os.chmod(folder,0o700);os.chmod(auth,0o600);os.chmod(access,0o600)
    except OSError: pass
    return tokens

if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--port',type=int,default=8095);p.add_argument('--provider',choices=['baseline','ollama'],default='baseline');p.add_argument('--model',default='');p.add_argument('--model-endpoint',default='http://127.0.0.1:11434/api/chat');p.add_argument('--rotate-credentials',action='store_true');p.add_argument('--no-build',action='store_true');a=p.parse_args()
    if a.provider=='ollama' and not a.model:p.error('--model is required with --provider ollama')
    if not a.no_build: build()
    access=make_credentials(ROOT/'.local',a.rotate_credentials)
    print('Local demo credentials (24-hour expiry) are in .local/access.json. Do not commit or publish this file.',flush=True)
    print('Paste the operator token into the UI; use the independent reviewer token for approval.',flush=True)
    command=['java','-cp',str(ROOT/'build/classes'),'io.github.ahadporkar.ordertocash.governance.demo.DemoServer','--port',str(a.port),'--seed','--provider',a.provider,'--model',a.model,'--model-endpoint',a.model_endpoint]
    try: subprocess.run(command,cwd=ROOT,check=True)
    except KeyboardInterrupt: pass
