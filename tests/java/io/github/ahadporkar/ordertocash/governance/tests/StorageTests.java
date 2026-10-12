package io.github.ahadporkar.ordertocash.governance.tests;
import io.github.ahadporkar.ordertocash.governance.*;
import static io.github.ahadporkar.ordertocash.governance.tests.TestSupport.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
public final class StorageTests {
    public static void run() {
        test("file.restart-after-commit-before-ack",()->{
            Path dir=Files.createTempDirectory("governance-restart-");String id;Map<String,Object> receipt;
            try(var e=new Env(new FileStateStore(dir))){var c=e.approved("ORD-1001","Refund");id=e.id(c);receipt=e.engine.execute(WORKER,id);}
            try(var e=new Env(new FileStateStore(dir))){equal(1,e.engine.readyJobs(WORKER).size());equal(receipt,e.engine.execute(WORKER,id));e.engine.acknowledge(WORKER,id,(String)receipt.get("id"));equal(0,e.engine.readyJobs(WORKER).size());equal(1,Json.map(e.store.snapshot(),"effects").size());}
        });
        test("file.exclusive-writer-lock",()->{Path dir=Files.createTempDirectory("governance-lock-");try(var store=new FileStateStore(dir)){yes(store.snapshot()!=null);throwsAny(()->new FileStateStore(dir));}});
        test("file.closed-store-rejects",()->{var store=new FileStateStore(Files.createTempDirectory("governance-closed-"));store.close();throwsAny(store::snapshot);});
        test("file.malformed-state-fails-closed",()->{Path dir=Files.createTempDirectory("governance-corrupt-");Files.writeString(dir.resolve("governance-state.json"),"{not json}");throwsAny(()->new FileStateStore(dir));equal("{not json}",Files.readString(dir.resolve("governance-state.json")));});
        test("file.audit-corruption-fails-closed",()->{Path dir=Files.createTempDirectory("governance-audit-");try(var e=new Env(new FileStateStore(dir))){e.open("ORD-1001","Refund");}var s=Json.obj(Files.readString(dir.resolve("governance-state.json")));Json.object(Json.list(s,"audit").getFirst()).put("event","TAMPERED");Files.writeString(dir.resolve("governance-state.json"),Json.stringify(s));throwsAny(()->new FileStateStore(dir));});
        test("file.orphan-temporary-file-ignored",()->{Path dir=Files.createTempDirectory("governance-temp-");try(var e=new Env(new FileStateStore(dir))){e.open("ORD-1001","Refund");}Files.writeString(dir.resolve("governance-state.tmp"),"partial");try(var e=new Env(new FileStateStore(dir))){equal(1,Json.map(e.store.snapshot(),"cases").size());}});
        test("file.transaction-exception-preserves-disk",()->{Path dir=Files.createTempDirectory("governance-rollback-");try(var store=new FileStateStore(dir)){String before=Files.readString(dir.resolve("governance-state.json"));throwsAny(()->store.transaction(s->{s.put("schemaVersion",99);throw new IllegalStateException();}));equal(before,Files.readString(dir.resolve("governance-state.json")));}});
        test("file.schema-version-mismatch-fails-closed",()->{Path dir=Files.createTempDirectory("governance-schema-");var state=StateStore.empty();state.put("schemaVersion",99);Files.writeString(dir.resolve("governance-state.json"),Json.stringify(state));throwsAny(()->new FileStateStore(dir));});
    }
}
