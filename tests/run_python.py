import argparse,importlib,json,pathlib,sys,time,unittest,xml.etree.ElementTree as ET
ROOT=pathlib.Path(__file__).resolve().parents[1]
class Recorded(unittest.TextTestResult):
    def __init__(self,*args,**kwargs):super().__init__(*args,**kwargs);self.records=[];self.starts={}
    def startTest(self,test):self.starts[test.id()]=time.perf_counter();super().startTest(test)
    def record(self,test,status,details=''):self.records.append({'name':test.id(),'status':status,'seconds':time.perf_counter()-self.starts[test.id()],'details':details})
    def addSuccess(self,test):super().addSuccess(test);self.record(test,'passed')
    def addFailure(self,test,err):super().addFailure(test,err);self.record(test,'failed',self._exc_info_to_string(err,test))
    def addError(self,test,err):super().addError(test,err);self.record(test,'error',self._exc_info_to_string(err,test))
    def addSkip(self,test,reason):super().addSkip(test,reason);self.record(test,'skipped',reason)
p=argparse.ArgumentParser();p.add_argument('--browser',action='store_true');p.add_argument('--repository',action='store_true');a=p.parse_args()
suite=unittest.TestSuite();loader=unittest.TestLoader()
for name in ['http_test']+(['browser_test'] if a.browser else [])+(['repository_test'] if a.repository else []):suite.addTests(loader.loadTestsFromModule(importlib.import_module(name)))
r=unittest.TextTestRunner(verbosity=2,resultclass=Recorded).run(suite)
out=ROOT/'build/test-results';out.mkdir(parents=True,exist_ok=True)
(out/'python-tests.json').write_text(json.dumps({'tests':r.records,'run':r.testsRun,'failures':len(r.failures),'errors':len(r.errors),'skipped':len(r.skipped)},indent=2)+'\n')
x=ET.Element('testsuite',name='governance-python',tests=str(r.testsRun),failures=str(len(r.failures)),errors=str(len(r.errors)),skipped=str(len(r.skipped)))
for row in r.records:
 c=ET.SubElement(x,'testcase',name=row['name'],time=str(row['seconds']))
 if row['status']!='passed':ET.SubElement(c,{'failed':'failure','error':'error','skipped':'skipped'}[row['status']],message=row['details'][:500]).text=row['details']
ET.ElementTree(x).write(out/'python-tests.xml',encoding='utf-8',xml_declaration=True)
raise SystemExit(0 if r.wasSuccessful() else 1)
