package com.intellij.lang.jsgraphql;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class GraphQLCompletionTestCaseBase extends GraphQLTestCaseBase {
    private static final String DEFAULT_SCHEMA_FILENAME = "Schema.graphql";

    protected LookupElement @Nullable [] doTest() {
        String sourceFile = getTestName(false);
        myFixture.configureByFile(sourceFile + ".graphql");
        return myFixture.complete(CompletionType.BASIC, 1);
    }

    protected LookupElement @Nullable [] doTestWithSchema() {
        return doTestWithSchema(DEFAULT_SCHEMA_FILENAME);
    }

    protected LookupElement @Nullable [] doTestWithSchema(@NotNull String schemaFileName) {
        myFixture.copyFileToProject(schemaFileName);
        return doTest();
    }

    protected static void checkEqualsOrdered(LookupElement @Nullable [] items, String... variants) {
        assertNotNull(items);
        assertOrderedEquals(ContainerUtil.map(items, LookupElement::getLookupString), variants);
    }

    private static void checkContainsAll(LookupElement @Nullable [] items, String... variants) {
        assertNotNull(items);
        List<String> variantsToCheck = new ArrayList<>(Arrays.asList(variants));
        variantsToCheck.removeAll(ContainerUtil.map(items, LookupElement::getLookupString));
        for (String variant : variantsToCheck) {
            fail("Missing completion variant: " + variant);
        }
    }

    public static void checkDoesNotContain(LookupElement @Nullable [] items, String... variants) {
        if (items == null) {
            return;
        }

        List<String> variantsToCheck = new ArrayList<>(Arrays.asList(variants));
        variantsToCheck.retainAll(ContainerUtil.map(items, LookupElement::getLookupString));
        for (String variant : variantsToCheck) {
            fail("Completion variant '" + variant + "' must not exist");
        }
    }

    protected void checkResult() {
        String sourceFile = getTestName(false);
        myFixture.checkResultByFile(sourceFile + "_after.graphql");
    }

    protected void checkResult(LookupElement @Nullable [] items, String lookupString) {
        assertNotNull(items);
        String sourceFile = getTestName(false);

        LookupElement lookupElement = ContainerUtil.find(items, i -> i.getLookupString().equals(lookupString));
        assertNotNull("Missing lookup element: " + lookupString, lookupElement);

        myFixture.getLookup().setCurrentItem(lookupElement);
        myFixture.type('\n');
        myFixture.checkResultByFile(sourceFile + "_after.graphql");
    }
}
