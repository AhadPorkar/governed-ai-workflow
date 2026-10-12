package io.github.ahadporkar.ordertocash.governance.tests;
import java.nio.file.Path;
public final class AllTests {
    public static void main(String[] args)throws Exception {
        CoreTests.run();SecurityTests.run();StorageTests.run();ModelTests.run();
        long failed=TestSupport.finish(Path.of(args.length==0?"build/test-results":args[0]));
        if(failed!=0)throw new AssertionError(failed+" Java tests failed");
    }
}
