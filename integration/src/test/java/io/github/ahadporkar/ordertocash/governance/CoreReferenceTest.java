package io.github.ahadporkar.ordertocash.governance;
import io.github.ahadporkar.ordertocash.governance.tests.AllTests;
import org.junit.jupiter.api.Test;
/** Bridges the dependency-free regression suite into the host Maven build. */
class CoreReferenceTest {
    @Test void coreRegressionSuite() throws Exception {AllTests.main(new String[0]);}
}
