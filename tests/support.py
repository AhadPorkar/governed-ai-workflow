import json, os, pathlib, socket, subprocess, sys, tempfile, time, urllib.request, urllib.error, uuid
ROOT=pathlib.Path(__file__).resolve().parents[1]
sys.path.insert(0,str(ROOT/'scripts'))
from run_demo import make_credentials
class Server:
    def __init__(self, rate=2000):
        self.temp=tempfile.TemporaryDirectory(prefix='governance-http-');self.folder=pathlib.Path(self.temp.name)
        self.tokens=make_credentials(self.folder);self.rate=rate
        with socket.socket() as s:s.bind(('127.0.0.1',0));self.port=s.getsockname()[1]
        self.base=f'http://127.0.0.1:{self.port}';self.process=None;self.logs=[];self.start()
    def start(self):
        log=open(self.folder/f'server-{len(self.logs)}.log','w');self.logs.append(log)
        cmd=['java','-cp',str(ROOT/'build/classes'),'io.github.ahadporkar.ordertocash.governance.demo.DemoServer','--port',str(self.port),'--state',str(self.folder/'state'),'--auth',str(self.folder/'auth.json'),'--web',str(ROOT/'web'),'--seed','--rate-limit',str(self.rate)]
        self.process=subprocess.Popen(cmd,stdout=log,stderr=subprocess.STDOUT,cwd=ROOT)
        for _ in range(100):
            try:
                if self.request('GET','/api/health',role=None)[0]==200:return
            except OSError:pass
            if self.process.poll() is not None:raise RuntimeError((self.folder/f'server-{len(self.logs)-1}.log').read_text())
            time.sleep(.1)
        raise RuntimeError('Server startup timeout')
    def stop(self,kill=False):
        if self.process and self.process.poll() is None:
            self.process.kill() if kill else self.process.terminate()
            try:self.process.wait(timeout=8)
            except subprocess.TimeoutExpired:self.process.kill();self.process.wait(timeout=5)
    def restart(self):self.stop(kill=True);self.start()
    def close(self):
        self.stop()
        for f in self.logs:f.close()
        self.temp.cleanup()
    def request(self,method,path,role='operator',body=None,headers=None,raw=None):
        h={}
        if role:h['Authorization']='Bearer '+self.tokens[role]
        if body is not None:h['Content-Type']='application/json';raw=json.dumps(body).encode()
        if headers:h.update(headers)
        req=urllib.request.Request(self.base+path,data=raw,headers=h,method=method)
        try:r=urllib.request.urlopen(req,timeout=15)
        except urllib.error.HTTPError as e:r=e
        with r:
            data=r.read();ctype=r.headers.get('Content-Type','');value=json.loads(data) if data and 'json' in ctype else data
            return r.status,value,dict(r.headers)
    def open(self,order='ORD-1001',text='Please refund my order.'):
        status,c,_=self.request('POST','/api/cases',body={'orderId':order,'request':text},headers={'Idempotency-Key':str(uuid.uuid4())})
        assert status==201,(status,c);return c
    def plan(self,c):
        status,result,_=self.request('POST',f'/api/cases/{c["id"]}/plan',body={});assert status==200,(status,result);return result
    def approve(self,c):
        status,result,_=self.request('POST',f'/api/cases/{c["id"]}/decisions','reviewer',{'proposalDigest':c['proposalDigest'],'approve':True,'reason':'Reviewed the exact action and supporting policy.'});assert status==200,(status,result);return result
    def worker(self,crash=False):
        env=os.environ.copy();env['GOVERNANCE_WORKER_TOKEN']=self.tokens['worker'];env['GOVERNANCE_URL']=self.base
        cmd=['java','-cp',str(ROOT/'build/classes'),'io.github.ahadporkar.ordertocash.governance.demo.WorkerMain','--once']
        if crash:cmd.append('--crash-after-commit')
        return subprocess.run(cmd,env=env,capture_output=True,text=True,timeout=20,cwd=ROOT)
    def showcase(self):
        complete=self.approve(self.plan(self.open('ORD-1005','Please refund my unavailable headset.')));assert self.worker().returncode==0
        blocked=self.plan(self.open('ORD-1003','Please refund this shipped order.'))
        escalated=self.plan(self.open('ORD-1004','Please help. I am not sure what to do.'))
        supplier=self.plan(self.open('ORD-1002','Please find an alternative supplier without increasing the price.'))
        pending=self.plan(self.open('ORD-1001','My headset cannot be delivered. Please refund the captured payment.'))
        return {'complete':complete,'blocked':blocked,'escalated':escalated,'supplier':supplier,'pending':pending}
