#!/usr/bin/env python3
"""Evaluate baseline or an explicitly configured real Ollama model; never approves or executes effects."""
import argparse,subprocess
from build import build,ROOT
p=argparse.ArgumentParser();p.add_argument('--provider',choices=['baseline','ollama'],default='baseline');p.add_argument('--model',default='');p.add_argument('--endpoint',default='http://127.0.0.1:11434/api/chat');p.add_argument('--no-build',action='store_true');a=p.parse_args()
if a.provider=='ollama' and not a.model:p.error('--model is required')
if not a.no_build:build()
out=ROOT/'build/evaluation'/('baseline-results.json' if a.provider=='baseline' else 'ollama-results.json')
subprocess.run(['java','-cp',str(ROOT/'build/classes'),'io.github.ahadporkar.ordertocash.governance.demo.EvaluationMain',str(ROOT/'evaluation/cases.json'),str(out),a.provider,a.endpoint,a.model],check=True,cwd=ROOT)
