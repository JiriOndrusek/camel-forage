package org.apache.camel.forage.integration.tests.suites;

import org.junit.platform.suite.api.AfterSuite;
import org.junit.platform.suite.api.BeforeSuite;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Suite
@SuiteDisplayName("Quarkus")
// @SelectPackages("org.apache.camel.forage")
@SelectClasses(names = "org.apache.camel.forage.jdbc.IdempotentTest")
public class QuarkusSuite {

    private static final Logger LOG = LoggerFactory.getLogger(QuarkusSuite.class);

    @AfterSuite
    public static void afterSuite() {
        TestSuiteHelper.afterSuite();
    }

    @BeforeSuite
    public static void beforeSuite() {
        TestSuiteHelper.beforeSuite("quarkus", LOG);
    }
}
