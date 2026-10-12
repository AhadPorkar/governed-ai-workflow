import importlib.util,json,pathlib,re,shutil,subprocess,sys,tempfile,unittest,xml.etree.ElementTree as ET
ROOT=pathlib.Path(__file__).resolve().parents[1]
spec=importlib.util.spec_from_file_location('overlay',ROOT/'scripts/apply_overlay.py');overlay=importlib.util.module_from_spec(spec);spec.loader.exec_module(overlay)
class RepositoryTests(unittest.TestCase):
    def temporary(self,artifact='bpmn-order-workflow'):
        d=tempfile.TemporaryDirectory(prefix='governance-overlay-');self.addCleanup(d.cleanup);root=pathlib.Path(d.name)
        (root/'pom.xml').write_text('<project xmlns="http://maven.apache.org/POM/4.0.0"><modelVersion>4.0.0</modelVersion><artifactId>'+artifact+'</artifactId></project>');return root
    def test_overlay_rejects_foreign_invoice_project(self):
        with self.assertRaisesRegex(ValueError,'Wrong project'):overlay.inspect(self.temporary('rechnungflow'))
    def test_overlay_dry_run_does_not_write(self):
        target=self.temporary();r=overlay.inspect(target);self.assertGreater(len(r['files']),50);self.assertEqual(['pom.xml'],[p.name for p in target.iterdir()]);self.assertEqual('dry-run',r['mode'])
    def test_overlay_apply_preserves_original_and_creates_files(self):
        target=self.temporary();before=(target/'pom.xml').read_bytes();r=overlay.apply(target,overlay.inspect(target));self.assertEqual(before,(target/'pom.xml').read_bytes());self.assertEqual('applied',r['mode']);self.assertTrue((target/'src/main/resources/bpmn/governed-order-exception.bpmn').exists());self.assertEqual(0,r['modifiedExistingFiles']);self.assertEqual(0,r['deletedFiles'])
    def test_overlay_second_apply_is_identical_noop(self):
        target=self.temporary();overlay.apply(target,overlay.inspect(target));r=overlay.inspect(target);self.assertTrue(all(x['status']=='UNCHANGED' for x in r['files']));overlay.apply(target,r)
    def test_overlay_conflict_prevents_all_writes(self):
        target=self.temporary();_,dest=overlay.mapping()[0];p=target/dest;p.parent.mkdir(parents=True);p.write_text('existing private code');r=overlay.inspect(target)
        with self.assertRaisesRegex(ValueError,'Refusing'):overlay.apply(target,r)
        self.assertEqual('existing private code',p.read_text());self.assertEqual(2,len([q for q in target.rglob('*') if q.is_file()]))
    def test_overlay_detects_other_migration_with_same_version(self):
        target=self.temporary();p=target/'src/main/resources/db/migration';p.mkdir(parents=True);(p/'V100__other.sql').write_text('SELECT 1;')
        with self.assertRaisesRegex(ValueError,'Flyway V100'):overlay.inspect(target)
    def test_overlay_rejects_symlink_destination(self):
        target=self.temporary();other=self.temporary();(target/'src').symlink_to(other,target_is_directory=True)
        with self.assertRaisesRegex(ValueError,'Symlink'):overlay.inspect(target)
    def test_overlay_targets_are_unique_and_relative(self):
        rows=overlay.mapping();names=[x[1] for x in rows];self.assertEqual(len(names),len(set(names)));self.assertTrue(all(not pathlib.PurePosixPath(n).is_absolute() and '..' not in pathlib.PurePosixPath(n).parts for n in names))
    def test_bilingual_guides_have_matching_chapters(self):
        en={p.name for p in (ROOT/'docs/en').glob('*.md')};de={p.name for p in (ROOT/'docs/de').glob('*.md')};self.assertEqual(en,de);self.assertEqual(13,len(en));self.assertTrue(all(p.stat().st_size>1600 for p in (ROOT/'docs/de').glob('*.md')))
    def test_markdown_document_links_resolve(self):
        bad=[]
        for p in list((ROOT/'docs').rglob('*.md'))+[ROOT/'README.md',ROOT/'README.de.md',ROOT/'SECURITY.md']:
            for link in re.findall(r'\]\(([^)]+)\)',p.read_text()):
                if '://' in link or link.startswith('#'):continue
                relative=link.split('#')[0]
                if not (p.parent/relative).exists():bad.append((str(p.relative_to(ROOT)),link))
        self.assertEqual([],bad)
    def test_no_nul_characters_in_source_or_docs(self):
        for base in ['src','demo','integration','docs','scripts','tests','web','publishing']:
            for p in (ROOT/base).rglob('*'):
                if p.suffix in ['.java','.py','.js','.md','.json','.yml','.xml','.bpmn','.html','.css'] and '__pycache__' not in p.parts:self.assertNotIn(b'\0',p.read_bytes(),str(p))
    def test_openapi_has_ten_operations_and_strict_write_schemas(self):
        api=json.loads((ROOT/'openapi/governance.openapi.json').read_text());self.assertEqual('3.1.0',api['openapi']);ops=[o for row in api['paths'].values() for o in row.values()];self.assertEqual(10,len(ops));self.assertEqual(10,len({x['operationId'] for x in ops}))
        for n in ['CreateCase','Decision','Acknowledge','Empty','Proposal']:self.assertIs(False,api['components']['schemas'][n]['additionalProperties'])
        self.assertEqual([],api['paths']['/health']['get']['security'])
    def test_openapi_role_boundaries_are_explicit(self):
        api=json.loads((ROOT/'openapi/governance.openapi.json').read_text());self.assertEqual('REVIEWER',api['paths']['/cases/{id}/decisions']['post']['x-required-role']);self.assertEqual('WORKER',api['paths']['/jobs/{id}/run']['post']['x-required-role'])
    def test_bpmn_ids_references_and_flow_consistency(self):
        tree=ET.parse(ROOT/'integration/src/main/resources/bpmn/governed-order-exception.bpmn');nodes={e.get('id'):e for e in tree.iter() if e.get('id')};ids=[e.get('id') for e in tree.iter() if e.get('id')];self.assertEqual(len(ids),len(set(ids)))
        for n in tree.iter():
            for key in ['sourceRef','targetRef','messageRef','errorRef','attachedToRef','bpmnElement','default']:
                if n.get(key):self.assertIn(n.get(key),nodes)
            if n.tag.endswith('sequenceFlow'):
                self.assertIn(n.get('id'),[x.text for x in nodes[n.get('sourceRef')] if x.tag.endswith('outgoing')]);self.assertIn(n.get('id'),[x.text for x in nodes[n.get('targetRef')] if x.tag.endswith('incoming')])
    def test_bpmn_inherited_child_order_and_guarded_execution(self):
        tree=ET.parse(ROOT/'integration/src/main/resources/bpmn/governed-order-exception.bpmn');weights={'documentation':0,'extensionElements':1,'incoming':2,'outgoing':3}
        for n in tree.iter():
            if n.tag.split('}')[-1] in ['startEvent','endEvent','boundaryEvent','serviceTask','receiveTask','exclusiveGateway']:
                order=[weights.get(x.tag.split('}')[-1],4) for x in n];self.assertEqual(sorted(order),order)
        xml=(ROOT/'integration/src/main/resources/bpmn/governed-order-exception.bpmn').read_text();self.assertIn('GOV_AUTHORIZATION_INVALID',xml);self.assertIn('PT15M',xml);self.assertIn('governance-execute',xml)
    def test_sql_migration_is_additive_not_destructive(self):
        sql=(ROOT/'integration/src/main/resources/db/migration/V100__governed_ai_workflow.sql').read_text();self.assertIn('CREATE TABLE governance_state',sql);self.assertNotRegex(sql.upper(),r'\b(DROP|TRUNCATE|DELETE)\b');self.assertIn('JSONB NOT NULL',sql)
    def test_spring_overlay_is_opt_in_and_uses_current_baseline_imports(self):
        for p in (ROOT/'integration/src/main/java').rglob('*.java'):self.assertIn('ConditionalOnProperty',p.read_text())
        worker=next((ROOT/'integration/src/main/java').rglob('GovernanceWorkers.java')).read_text();self.assertIn('io.camunda.client.annotation.JobWorker',worker);self.assertNotIn('io.camunda.spring.client',worker);self.assertIn('engine.execute',worker)
    def test_evaluation_has_unique_synthetic_cases_and_both_languages(self):
        dataset=json.loads((ROOT/'evaluation/cases.json').read_text());rows=dataset['cases'];self.assertEqual(48,len(rows));self.assertEqual(48,len({r['id'] for r in rows}));self.assertEqual({'en','de'},{r['language'] for r in rows});self.assertTrue(all('acceptableStates' in r['expected'] for r in rows))
    def test_ui_javascript_parses(self):
        if not shutil.which('node'):self.skipTest('Node optional syntax check unavailable')
        r=subprocess.run(['node','--check',str(ROOT/'web/app.js')],capture_output=True,text=True);self.assertEqual(0,r.returncode,r.stderr)
    def test_ui_has_no_external_runtime_assets_or_persisted_tokens(self):
        html=(ROOT/'web/index.html').read_text();self.assertNotRegex(html,r'(src|href)=["\']https?://');js=(ROOT/'web/app.js').read_text();self.assertNotRegex(js,r'localStorage\.setItem\(["\'](?:token|authorization)')
    def test_gitignore_excludes_local_credentials(self):
        text=(ROOT/'.gitignore').read_text();self.assertIn('.local/',text);self.assertIn('.env',text)
    def test_provenance_never_claims_merged_repository(self):
        p=json.loads((ROOT/'PROVENANCE.json').read_text());self.assertIs(False,p['attachment_modified']);self.assertIn('not a clone or merged',p['delivery']);self.assertIn('not resolved',p['upstream_commit'])
    def test_native_browser_restriction_is_explicit(self):
        status=json.loads((ROOT/'evidence/environment/native-browser-status.json').read_text());self.assertEqual('BLOCKED_BY_ENVIRONMENT',status['status']);self.assertIn('CSP enforcement',status['notVerified'])
