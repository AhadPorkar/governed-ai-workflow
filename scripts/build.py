#!/usr/bin/env python3
"""Offline Java 21 build. Requires no Maven, model credentials or container runtime."""
import argparse, pathlib, shutil, subprocess, sys
ROOT=pathlib.Path(__file__).resolve().parents[1]
def build(tests=False):
    if not shutil.which('javac') or not shutil.which('java'):
        raise SystemExit('Java 21 JDK is required; javac and java must be on PATH.')
    classes=ROOT/'build/classes'
    if classes.exists(): shutil.rmtree(classes)
    classes.mkdir(parents=True,exist_ok=True)
    roots=[ROOT/'src/main/java',ROOT/'demo/src']
    if tests: roots.append(ROOT/'tests/java')
    sources=sorted(str(p) for root in roots for p in root.rglob('*.java'))
    args=ROOT/'build/sources.txt'
    args.write_text('\n'.join('"'+p.replace('\\','/')+'"' for p in sources)+'\n')
    subprocess.run(['javac','--release','21','-encoding','UTF-8','-Xlint:all','-d',str(classes),'@'+str(args)],check=True,cwd=ROOT)
    subprocess.run(['jar','--create','--file',str(ROOT/'build/governed-ai-demo.jar'),'-C',str(classes),'.'],check=True,cwd=ROOT)
    print(f'Compiled {len(sources)} Java files. Integration adapters are a separate Maven gate.')
if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--tests',action='store_true');a=p.parse_args();build(a.tests)
