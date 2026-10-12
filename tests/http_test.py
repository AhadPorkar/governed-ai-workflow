import concurrent.futures,json,pathlib,unittest,uuid
from support import Server
class HttpTests(unittest.TestCase):
    def setUp(self):self.s=Server()
    def tearDown(self):self.s.close()
    def test_health_explicitly_identifies_baseline_and_sandbox(self):
        status,b,_=self.s.request('GET','/api/health',None);self.assertEqual(200,status);self.assertTrue(b['sandbox']);self.assertEqual('deterministic-baseline',b['planner'])
    def test_anonymous_dashboard_denied(self):self.assertEqual(401,self.s.request('GET','/api/dashboard',None)[0])
    def test_security_response_headers(self):
        _,_,h=self.s.request('GET','/');self.assertEqual('nosniff',h.get('X-content-type-options'));self.assertIn("frame-ancestors 'none'",h.get('Content-security-policy',''));self.assertEqual('no-store',h.get('Cache-control'))
    def test_case_flow_real_http(self):
        c=self.s.approve(self.s.plan(self.s.open()));status,r,_=self.s.request('POST',f'/api/jobs/{c["id"]}/run','worker',{});self.assertEqual(200,status);self.assertEqual(12900,r['amountCents']);self.assertTrue(r['sandbox']);self.assertEqual(200,self.s.request('POST',f'/api/jobs/{c["id"]}/ack','worker',{'receiptId':r['id']})[0])
    def test_operator_cannot_impersonate_reviewer(self):
        c=self.s.plan(self.s.open());self.assertEqual(403,self.s.request('POST',f'/api/cases/{c["id"]}/decisions','operator',{'proposalDigest':c['proposalDigest'],'approve':True,'reason':'yes'})[0])
    def test_worker_cannot_submit_human_approval(self):
        c=self.s.plan(self.s.open());self.assertEqual(403,self.s.request('POST',f'/api/cases/{c["id"]}/decisions','worker',{'proposalDigest':c['proposalDigest'],'approve':True,'reason':'yes'})[0])
    def test_actor_field_in_body_rejected(self):
        status,b,_=self.s.request('POST','/api/cases',body={'orderId':'ORD-1001','request':'Refund','actor':'reviewer'},headers={'Idempotency-Key':str(uuid.uuid4())});self.assertEqual(400,status);self.assertEqual('INVALID_FIELDS',b['title'])
    def test_cross_tenant_read_hidden(self):
        c=self.s.open();self.assertEqual(404,self.s.request('GET',f'/api/cases/{c["id"]}','other')[0])
    def test_cross_tenant_dashboard_filtered(self):
        self.s.open();self.assertEqual([],self.s.request('GET','/api/dashboard','other')[1]['cases'])
    def test_cross_tenant_execution_hidden(self):
        c=self.s.approve(self.s.plan(self.s.open()));self.assertEqual(403,self.s.request('POST',f'/api/jobs/{c["id"]}/run','other',{})[0])
    def test_extra_proposal_arguments_rejected(self):
        c=self.s.open();self.assertEqual(400,self.s.request('POST',f'/api/cases/{c["id"]}/plan',body={'approved':True})[0])
    def test_forged_digest_rejected(self):
        c=self.s.plan(self.s.open());status,b,_=self.s.request('POST',f'/api/cases/{c["id"]}/decisions','reviewer',{'proposalDigest':'0'*64,'approve':True,'reason':'yes'});self.assertEqual(409,status);self.assertEqual('PROPOSAL_DIGEST_MISMATCH',b['title'])
    def test_missing_idempotency_key_rejected(self):self.assertEqual(400,self.s.request('POST','/api/cases',body={'orderId':'ORD-1001','request':'Refund'})[0])
    def test_idempotency_payload_conflict(self):
        h={'Idempotency-Key':'idem-1'};self.assertEqual(201,self.s.request('POST','/api/cases',body={'orderId':'ORD-1001','request':'Refund'},headers=h)[0]);self.assertEqual(409,self.s.request('POST','/api/cases',body={'orderId':'ORD-1001','request':'Cancel'},headers=h)[0])
    def test_duplicate_json_keys_rejected(self):self.assertEqual(400,self.s.request('POST','/api/cases',headers={'Content-Type':'application/json'},raw=b'{"orderId":"ORD-1001","orderId":"ORD-1002","request":"x"}')[0])
    def test_malformed_json_rejected(self):self.assertEqual(400,self.s.request('POST','/api/cases',headers={'Content-Type':'application/json'},raw=b'{')[0])
    def test_invalid_utf8_rejected(self):self.assertEqual(400,self.s.request('POST','/api/cases',headers={'Content-Type':'application/json'},raw=b'{"request":"\xff"}')[0])
    def test_oversized_body_rejected(self):self.assertEqual(413,self.s.request('POST','/api/cases',headers={'Content-Type':'application/json'},raw=b'x'*16385)[0])
    def test_non_json_content_type_rejected(self):self.assertEqual(415,self.s.request('POST','/api/cases',headers={'Content-Type':'text/plain'},raw=b'{}')[0])
    def test_cross_origin_request_rejected(self):self.assertEqual(403,self.s.request('POST','/api/cases',body={},headers={'Origin':'https://attacker.example'})[0])
    def test_no_credentials_in_query_strings(self):self.assertEqual(400,self.s.request('GET','/api/dashboard?token=secret')[0])
    def test_static_path_traversal_blocked(self):self.assertEqual(404,self.s.request('GET','/%2e%2e/.local/auth.json')[0])
    def test_method_not_allowed(self):self.assertEqual(405,self.s.request('DELETE','/api/cases')[0])
    def test_jobs_require_worker_role(self):self.assertEqual(403,self.s.request('GET','/api/jobs')[0])
    def test_receipt_cannot_be_acknowledged_before_effect(self):
        c=self.s.approve(self.s.plan(self.s.open()));self.assertEqual(409,self.s.request('POST',f'/api/jobs/{c["id"]}/ack','worker',{'receiptId':str(uuid.uuid4())})[0])
    def test_12_concurrent_http_retries_one_receipt(self):
        c=self.s.approve(self.s.plan(self.s.open()))
        with concurrent.futures.ThreadPoolExecutor(max_workers=6) as p:r=list(p.map(lambda _:self.s.request('POST',f'/api/jobs/{c["id"]}/run','worker',{}),range(12)))
        self.assertTrue(all(x[0]==200 for x in r));self.assertEqual(1,len({x[1]['id'] for x in r}))
    def test_server_process_restart_preserves_pending_approval(self):
        c=self.s.plan(self.s.open());self.s.restart();b=self.s.request('GET',f'/api/cases/{c["id"]}')[1];self.assertEqual(c['proposalDigest'],b['proposalDigest']);self.assertEqual('PENDING_APPROVAL',b['state'])
    def test_real_worker_crash_after_commit_before_ack_and_server_restart(self):
        c=self.s.approve(self.s.plan(self.s.open()));crash=self.s.worker(True);self.assertEqual(73,crash.returncode,crash.stderr)
        before=self.s.request('GET',f'/api/cases/{c["id"]}')[1];self.assertEqual('EXECUTED',before['state']);self.assertEqual(1,len(self.s.request('GET','/api/jobs','worker')[1]['jobs']))
        self.s.restart();recovery=self.s.worker();self.assertEqual(0,recovery.returncode,recovery.stderr)
        after=self.s.request('GET',f'/api/cases/{c["id"]}')[1];self.assertEqual(before['receipt'],after['receipt']);self.assertEqual([],self.s.request('GET','/api/jobs','worker')[1]['jobs'])
        state=json.loads((self.s.folder/'state/governance-state.json').read_text());self.assertEqual(1,len(state['effects']));self.assertEqual(12900,state['orders']['demo/ORD-1001']['refundedCents']);self.assertEqual(2,state['jobs'][c['id']]['attempts'])
class RateLimitTests(unittest.TestCase):
    def test_authenticated_principal_rate_limit(self):
        s=Server(rate=2)
        try:
            self.assertEqual(200,s.request('GET','/api/session')[0]);self.assertEqual(200,s.request('GET','/api/session')[0]);status,_,h=s.request('GET','/api/session');self.assertEqual(429,status);self.assertEqual('60',h.get('Retry-after'))
        finally:s.close()
