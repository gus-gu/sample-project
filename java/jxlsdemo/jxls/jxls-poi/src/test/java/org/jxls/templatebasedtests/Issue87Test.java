package org.jxls.templatebasedtests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.jxls.JxlsTester;
import org.jxls.TestWorkbook;
import org.jxls.common.Context;

/**
 * Formula recalculation
 */
public class Issue87Test {

    @Test
    public void test() throws Exception {
        // Prepare
        Context context = new Context();
        context.putVar("title", "Issue 87");
        context.putVar("name", "No Name");

        // Test
        JxlsTester tester = JxlsTester.xlsx(getClass()).setFullFormulaRecalculationOnOpening(true);
        tester.processTemplate(context);

        // Verify
        try (TestWorkbook w = tester.getWorkbook()) {
            assertTrue("ForceFormulaRecalculation must be set to true", w.isForceFormulaRecalculation());
        }
    }
}
