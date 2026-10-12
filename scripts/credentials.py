#!/usr/bin/env python3
"""Generate expiring demo-only identities for an explicitly named private directory."""
import argparse,datetime,hashlib,json,os,pathlib,secrets
p=argparse.ArgumentParser();p.add_argument('directory',type=pathlib.Path);p.add_argument('--rotate',action='store_true');a=p.parse_args();d=a.directory.resolve();d.mkdir(parents=True,exist_ok=True)
if any((d/n).exists() for n in ['auth.json','access.json']) and not a.rotate:raise SystemExit('Existing credentials; --rotate is required. Restart afterward.')
rows=[];tokens={};expiry=(datetime.datetime.now(datetime.timezone.utc)+datetime.timedelta(hours=24)).isoformat().replace('+00:00','Z')
for name,roles in [('operator',['READ','OPERATOR']),('reviewer',['READ','REVIEWER']),('worker',['READ','WORKER']),('viewer',['READ'])]:
 t=secrets.token_urlsafe(32);tokens[name]=t;rows.append({'id':name,'tenant':'demo','roles':roles,'expiresAt':expiry,'tokenHash':hashlib.sha256(t.encode()).hexdigest()})
for name,value in [('auth.json',{'identities':rows}),('access.json',tokens)]:
 f=d/name;f.write_text(json.dumps(value,indent=2)+'\n')
 try:os.chmod(f,0o600)
 except OSError:pass
try:os.chmod(d,0o700)
except OSError:pass
print('Demo credentials created at '+str(d)+'. Tokens expire in 24 hours. Never commit access.json. Restart after rotation.')
