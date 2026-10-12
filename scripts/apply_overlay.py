#!/usr/bin/env python3
"""Additive installer: dry-run by default, rejects foreign repositories and overwrite conflicts."""
import argparse,hashlib,json,os,pathlib,sys,xml.etree.ElementTree as ET
ROOT=pathlib.Path(__file__).resolve().parents[1]
def mapping():
    pairs=[]
    for source_root,target_root in [('src/main/java','src/main/java'),('integration/src/main','src/main'),('integration/src/test','src/test'),('tests/java','src/test/java'),('web','src/main/resources/static/governance')]:
        for source in sorted((ROOT/source_root).rglob('*')):
            if source.is_file():pairs.append((source,(pathlib.PurePosixPath(target_root)/source.relative_to(ROOT/source_root)).as_posix()))
    for source in sorted((ROOT/'docs').rglob('*')):
        if source.is_file():pairs.append((source,(pathlib.PurePosixPath('docs/governed-ai')/source.relative_to(ROOT/'docs')).as_posix()))
    for name in ['PROVENANCE.json','CHANGELOG.md','LICENSE','NOTICE.md']:
        p=ROOT/name
        if p.exists():pairs.append((p,'docs/governed-ai/package/'+name))
    for source,target in [('scripts/stack_acceptance.py','scripts/governance-acceptance.py'),('scripts/credentials.py','scripts/governance-credentials.py'),('openapi/governance.openapi.json','docs/governed-ai/governance.openapi.json')]:
        if (ROOT/source).exists():pairs.append((ROOT/source,target))
    targets=[target for _,target in pairs]
    if len(targets)!=len(set(targets)):raise ValueError('Duplicate overlay targets')
    return pairs

def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def inspect(target):
    root=target.resolve(strict=True);pom=root/'pom.xml'
    if not pom.is_file() or pom.is_symlink():raise ValueError('Target must contain its own pom.xml')
    tree=ET.parse(pom);ns={'m':'http://maven.apache.org/POM/4.0.0'}
    artifact=tree.getroot().findtext('m:artifactId',namespaces=ns) or tree.getroot().findtext('artifactId')
    if artifact!='bpmn-order-workflow':raise ValueError('Wrong project: expected bpmn-order-workflow. RechnungFlow is deliberately rejected.')
    rows=[]
    for source,relative in mapping():
        dest=root/relative
        for parent in [dest,*dest.parents]:
            if parent==root:break
            if parent.is_symlink():raise ValueError('Symlink target refused: '+relative)
        status='ADDED'
        if dest.exists():status='UNCHANGED' if dest.is_file() and digest(dest)==digest(source) else 'CONFLICT'
        rows.append({'source':source.relative_to(ROOT).as_posix(),'target':relative,'status':status,'sha256':digest(source),'bytes':source.stat().st_size})
    migrations=root/'src/main/resources/db/migration'
    collisions=[p.name for p in migrations.glob('V100__*') if p.name!='V100__governed_ai_workflow.sql']
    if collisions:raise ValueError('Flyway V100 already exists: '+', '.join(collisions)+'. Renumber deliberately before applying.')
    return {'target':str(root),'targetPomSha256':digest(pom),'mode':'dry-run','modifiedExistingFiles':0,'deletedFiles':0,'files':rows}

def apply(target,report):
    if any(r['status']=='CONFLICT' for r in report['files']):raise ValueError('Refusing all writes because an existing file differs.')
    created=[]
    try:
        for row in report['files']:
            if row['status']=='UNCHANGED':continue
            dest=target/row['target'];dest.parent.mkdir(parents=True,exist_ok=True)
            with dest.open('xb') as out:
                created.append(dest);out.write((ROOT/row['source']).read_bytes());out.flush();os.fsync(out.fileno())
        report['mode']='applied'
    except Exception:
        for p in reversed(created):p.unlink(missing_ok=True)
        raise
    return report

def main():
    p=argparse.ArgumentParser();p.add_argument('target',type=pathlib.Path);p.add_argument('--apply',action='store_true');p.add_argument('--report',type=pathlib.Path);a=p.parse_args()
    try:
        target=a.target.resolve(strict=True);report=inspect(target)
        if a.apply:report=apply(target,report)
        text=json.dumps(report,indent=2)+'\n'
        if a.report:a.report.parent.mkdir(parents=True,exist_ok=True);a.report.write_text(text)
        print(text)
        return 2 if any(r['status']=='CONFLICT' for r in report['files']) else 0
    except (OSError,ValueError,ET.ParseError) as e:print('Overlay refused: '+str(e),file=sys.stderr);return 2
if __name__=='__main__':raise SystemExit(main())
