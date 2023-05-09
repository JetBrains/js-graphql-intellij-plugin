package com.intellij.lang.jsgraphql;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.testFramework.fixtures.TestLookupElementPresentation;
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

    protected LookupElement @Nullable [] doTestWithProject() {
        return doTestWithProject(".graphql");
    }

    protected LookupElement @Nullable [] doTestWithProject(@NotNull String ext) {
        String dirName = getTestName(false);
        myFixture.copyDirectoryToProject(dirName, "");
        reloadConfiguration();
        String filePath = dirName + ext;
        myFixture.configureFromTempProjectFile(filePath);
        return myFixture.complete(CompletionType.BASIC, 1);
    }

    protected static void checkEqualsOrdered(LookupElement @Nullable [] items, String @NotNull ... variants) {
        assertNotNull(items);
        assertOrderedEquals(ContainerUtil.map(items, LookupElement::getLookupString), variants);
    }

    protected static void checkTypeText(LookupElement @Nullable [] items, @NotNull String lookupString, @NotNull String expectedTypeText) {
        LookupElement lookupElement = findLookupElement(items, lookupString);
        checkTypeText(lookupElement, expectedTypeText);
    }

    protected static void checkTypeText(@NotNull LookupElement lookupElement, @NotNull String expectedTypeText) {
        assertEquals(expectedTypeText, TestLookupElementPresentation.renderReal(lookupElement).getTypeText());
    }

    protected static void checkTailText(LookupElement @Nullable [] items, @NotNull String lookupString, @NotNull String expectedTailText) {
        LookupElement lookupElement = findLookupElement(items, lookupString);
        checkTailText(lookupElement, expectedTailText);
    }

    protected static void checkTailText(@NotNull LookupElement lookupElement, @NotNull String expectedTailText) {
        assertEquals(expectedTailText, TestLookupElementPresentation.renderReal(lookupElement).getTailText());
    }

    protected static void checkDeprecated(LookupElement @Nullable [] items, @NotNull String lookupString, boolean isDeprecated) {
        LookupElement lookupElement = findLookupElement(items, lookupString);
        checkDeprecated(lookupElement, isDeprecated);
    }

    protected static void checkDeprecated(@NotNull LookupElement lookupElement, boolean isDeprecated) {
        assertEquals(isDeprecated, TestLookupElementPresentation.renderReal(lookupElement).isStrikeout());
    }

    protected static void checkContainsAll(LookupElement @Nullable [] items, String... variants) {
        assertNotNull(items);
        List<String> variantsToCheck = new ArrayList<>(Arrays.asList(variants));
        variantsToCheck.removeAll(ContainerUtil.map(items, LookupElement::getLookupString));
        for (String variant : variantsToCheck) {
            fail("Missing completion variant: " + variant);
        }
    }

    protected static void checkDoesNotContain(LookupElement @Nullable [] items, String... variants) {
        if (items == null) {
            return;
        }

        List<String> variantsToCheck = new ArrayList<>(Arrays.asList(variants));
        variantsToCheck.retainAll(ContainerUtil.map(items, LookupElement::getLookupString));
        for (String variant : variantsToCheck) {
            fail("Completion variant '" + variant + "' must not exist");
        }
    }

    protected static void checkEmpty(LookupElement @Nullable [] items) {
        assertNotNull(items);
        assertEmpty(items);
    }

    protected void checkResult() {
        String sourceFile = getTestName(false);
        myFixture.checkResultByFile(sourceFile + "_after.graphql");
    }

    protected void checkResult(LookupElement @Nullable [] items, String lookupString) {
        String sourceFile = getTestName(false);

        LookupElement lookupElement = findLookupElement(items, lookupString);

        myFixture.getLookup().setCurrentItem(lookupElement);
        myFixture.type('\n');
        myFixture.checkResultByFile(sourceFile + "_after.graphql");
    }

    protected static @NotNull LookupElement findLookupElement(LookupElement @Nullable [] items, @NotNull String lookupString) {
        assertNotNull(items);
        LookupElement lookupElement = ContainerUtil.find(items, i -> i.getLookupString().equals(lookupString));
        assertNotNull("Missing lookup element: " + lookupString, lookupElement);
        return lookupElement;
    }
}
