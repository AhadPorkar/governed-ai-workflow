import json,os,pathlib,unittest
from support import Server,ROOT
from playwright.sync_api import sync_playwright, expect
class BrowserTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.p=sync_playwright().start()
        path=os.environ.get('CHROMIUM_PATH','/usr/bin/chromium')
        cls.browser=cls.p.chromium.launch(executable_path=path if pathlib.Path(path).exists() else None,headless=True,args=['--no-sandbox'])
        (ROOT/'build/screenshots').mkdir(parents=True,exist_ok=True)
    @classmethod
    def tearDownClass(cls):cls.browser.close();cls.p.stop()
    def setUp(self):
        self.s=Server();self.showcase=self.s.showcase()
        self.context=self.browser.new_context(viewport={'width':1440,'height':1150});self.page=self.context.new_page();self.errors=[]
        self.page.on('pageerror',lambda e:self.errors.append(str(e)))
        transport=os.environ.get('GOV_UI_TRANSPORT','native')
        if transport=='native': self.page.goto(self.s.base)
        elif transport=='harness':
            # Explicit DOM harness. This DOES NOT verify native navigation, CSP enforcement or browser network transport.
            self.page.set_content('<!doctype html><html><head><meta name="viewport" content="width=device-width, initial-scale=1"></head><body><div id="app"></div><div id="notice" role="status" aria-live="polite"></div></body></html>')
            def bridge(url,options):
                path='/'+url.lstrip('/')
                if not path.startswith('/api/'):raise ValueError('Only the local test API is permitted')
                status,body,headers=self.s.request(options.get('method','GET'),path,role=None,headers=options.get('headers',{}),raw=options.get('body','').encode() if options.get('body') is not None else None)
                return {'status':status,'body':json.dumps(body),'headers':headers}
            self.page.expose_function('__governanceFetch',bridge)
            self.page.evaluate("""()=>{
              const storage=new Map();Object.defineProperty(window,'localStorage',{value:{getItem:k=>storage.get(k)||null,setItem:(k,v)=>storage.set(k,String(v)),removeItem:k=>storage.delete(k)}});
              window.fetch=async(u,o={})=>{const r=await window.__governanceFetch(u,o);return {ok:r.status>=200&&r.status<300,status:r.status,json:async()=>JSON.parse(r.body)}};
              if(!crypto.randomUUID)crypto.randomUUID=()=> '10000000-1000-4000-8000-100000000000'.replace(/[018]/g,c=>(c^crypto.getRandomValues(new Uint8Array(1))[0]&15>>c/4).toString(16));
            }""")
            self.page.add_style_tag(content=(ROOT/'web/styles.css').read_text())
            self.page.add_script_tag(content=(ROOT/'web/app.js').read_text())
        else:raise RuntimeError('GOV_UI_TRANSPORT must be native or harness')
    def tearDown(self):self.context.close();self.s.close()
    def login(self,role='reviewer'):
        self.page.locator('#token').fill(self.s.tokens[role]);self.page.locator('#login-form button').click();self.page.locator('.case-row').first.wait_for()
    def test_english_desktop_and_no_horizontal_overflow(self):
        self.login();self.page.locator(f'[data-case="{self.showcase["pending"]["id"]}"]').click();self.page.locator('#reason').fill('The captured balance and customer request match. I approve this exact sandbox refund.')
        self.assertLessEqual(self.page.evaluate('document.documentElement.scrollWidth'),1440);self.page.screenshot(path=str(ROOT/'build/screenshots/workspace-en.png'),full_page=True);self.assertEqual([],self.errors)
    def test_german_desktop_and_localized_controls(self):
        self.login();self.page.locator('[data-language="de"]').click();self.page.locator(f'[data-case="{self.showcase["pending"]["id"]}"]').click();self.assertEqual('de',self.page.locator('html').get_attribute('lang'));self.assertIn('Freigabe',self.page.locator('#detail-panel').inner_text());self.assertLessEqual(self.page.evaluate('document.documentElement.scrollWidth'),1440);self.page.screenshot(path=str(ROOT/'build/screenshots/workspace-de.png'),full_page=True);self.assertEqual([],self.errors)
    def test_mobile_german_no_horizontal_overflow(self):
        self.page.set_viewport_size({'width':390,'height':844});self.login();self.page.locator('[data-language="de"]').click();self.assertLessEqual(self.page.evaluate('document.documentElement.scrollWidth'),390);self.page.screenshot(path=str(ROOT/'build/screenshots/workspace-de-mobile.png'),full_page=True);self.assertEqual([],self.errors)
    def test_operator_can_create_and_investigate(self):
        self.login('operator');self.page.locator('#request').fill('Please find an alternative supplier.');self.page.locator('#create-form button').click();self.page.locator('#investigate').wait_for();self.page.locator('#investigate').click();self.page.locator('.proposal').wait_for();self.assertIn('Change supplier',self.page.locator('.proposal').inner_text());self.assertEqual(0,self.page.locator('#decision-form').count());self.assertEqual([],self.errors)
    def test_model_and_customer_text_render_as_text_not_html(self):
        c=self.s.plan(self.s.open('ORD-1002','Please cancel <img src=x onerror="window.xss=1">'))
        self.login('operator');self.page.locator(f'[data-case="{c["id"]}"]').click();self.assertIn('<img',self.page.locator('.request-box').inner_text());self.assertEqual(0,self.page.locator('.request-box img').count());self.assertIsNone(self.page.evaluate('window.xss'));self.assertEqual([],self.errors)
    def test_reviewer_can_decline_without_effect(self):
        c=self.s.plan(self.s.open('ORD-1002','Please cancel this order.'));self.login();self.page.locator(f'[data-case="{c["id"]}"]').click();self.page.locator('#reason').fill('Need confirmation before cancellation.');self.page.locator('button[value="decline"]').click();expect(self.page.locator('.detail-top')).to_contain_text('Declined');self.assertEqual('DECLINED',self.s.request('GET',f'/api/cases/{c["id"]}')[1]['state'])
    def test_reviewer_approval_then_worker_receipt_visible(self):
        c=self.s.plan(self.s.open('ORD-1002','Please cancel this order.'));self.login();self.page.locator(f'[data-case="{c["id"]}"]').click();self.page.locator('#reason').fill('Unpaid order and verified cancellation request.');self.page.locator('button[value="approve"]').click();expect(self.page.locator('.detail-top')).to_contain_text('Approved');self.assertEqual(0,self.s.worker().returncode);self.page.locator('#refresh').click();self.page.locator('[data-tab="receipt"]').wait_for();self.page.locator('[data-tab="receipt"]').click();self.assertIn('"sandbox": true',self.page.locator('.tab-content').inner_text())
    def test_signout_removes_token_and_protected_workspace(self):
        self.login();self.page.locator('#signout').click();self.page.locator('#token').wait_for();self.assertEqual(0,self.page.locator('.case-row').count());self.assertIsNone(self.page.evaluate('localStorage.getItem("token")'));self.assertEqual('',self.page.locator('#token').input_value())
    def test_login_visual_and_keyboard_access(self):
        self.page.locator('#token').focus();self.assertEqual('token',self.page.evaluate('document.activeElement.id'));self.page.screenshot(path=str(ROOT/'build/screenshots/login-en.png'),full_page=True);self.page.locator('[data-language="de"]').click();self.page.screenshot(path=str(ROOT/'build/screenshots/login-de.png'),full_page=True)
