#!/usr/bin/env python3
"""Run offline Java contracts plus real HTTP/process tests; add --browser for Chromium checks."""
import argparse,subprocess,sys
from build import build,ROOT
p=argparse.ArgumentParser();p.add_argument('--browser',action='store_true');p.add_argument('--repository',action='store_true');a=p.parse_args()
build(tests=True)
subprocess.run(['java','-cp',str(ROOT/'build/classes'),'io.github.ahadporkar.ordertocash.governance.tests.AllTests',str(ROOT/'build/test-results')],cwd=ROOT,check=True)
cmd=[sys.executable,str(ROOT/'tests/run_python.py')]
if a.browser:cmd.append('--browser')
if a.repository:cmd.append('--repository')
subprocess.run(cmd,cwd=ROOT,check=True)
print('All selected local checks passed. This is not a Camunda/PostgreSQL/Kafka or real-model acceptance result.')
