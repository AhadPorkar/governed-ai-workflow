#!/usr/bin/env python3
"""Run the separately restartable demo worker. Local access.json is never sent to a remote server."""
import argparse,json,os,subprocess
from build import ROOT
p=argparse.ArgumentParser();p.add_argument('--once',action='store_true');p.add_argument('--crash-after-commit',action='store_true');p.add_argument('--port',type=int,default=8095);a=p.parse_args()
tokens=json.loads((ROOT/'.local/access.json').read_text());env=os.environ.copy();env['GOVERNANCE_WORKER_TOKEN']=tokens['worker'];env['GOVERNANCE_URL']=f'http://127.0.0.1:{a.port}'
cmd=['java','-cp',str(ROOT/'build/classes'),'io.github.ahadporkar.ordertocash.governance.demo.WorkerMain']
if a.once:cmd.append('--once')
if a.crash_after_commit:cmd.append('--crash-after-commit')
raise SystemExit(subprocess.run(cmd,env=env,cwd=ROOT).returncode)
