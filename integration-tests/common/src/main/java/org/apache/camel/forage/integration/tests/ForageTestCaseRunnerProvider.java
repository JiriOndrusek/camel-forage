package org.apache.camel.forage.integration.tests;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseRunnerProvider;
import org.citrusframework.context.TestContext;

public class ForageTestCaseRunnerProvider implements TestCaseRunnerProvider {

    @Override
    public ForageTestCaseRunner createTestCaseRunner(TestContext context) {
        return new ForageTestCaseRunner(context);
    }

    @Override
    public ForageTestCaseRunner createTestCaseRunner(TestCase testCase, TestContext context) {
        return new ForageTestCaseRunner(testCase, context);
    }
}
