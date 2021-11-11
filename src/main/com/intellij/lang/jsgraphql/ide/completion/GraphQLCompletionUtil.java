package com.intellij.lang.jsgraphql.ide.completion;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.AddSpaceInsertHandler;
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import org.jetbrains.annotations.NotNull;

public final class GraphQLCompletionUtil {
    private GraphQLCompletionUtil() {
    }

    @NotNull
    public static LookupElementBuilder createKeywordLookupElement(@NotNull GraphQLCompletionKeyword keyword) {
        return LookupElementBuilder.create(keyword.getText())
            .bold()
            .withInsertHandler(AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP);
    }

    @NotNull
    public static LookupElementBuilder createOperationNameKeywordLookupElement(@NotNull String operationName) {
        return LookupElementBuilder.create(operationName)
            .bold()
            .withInsertHandler(AddColonSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP);
    }

    @NotNull
    public static LookupElementBuilder createExtendTypeNameLookupElement(@NotNull String name) {
        return LookupElementBuilder.create(name).withInsertHandler(AddSpaceInsertHandler.INSTANCE);
    }

    @NotNull
    public static LookupElementBuilder createTypeNameLookupElement(@NotNull String name) {
        return LookupElementBuilder.create(name);
    }

    @NotNull
    public static LookupElementBuilder createDirectiveLocationLookupElement(@NotNull String name) {
        return LookupElementBuilder.create(name).bold();
    }

    @NotNull
    public static LookupElementBuilder createFieldOverrideLookupElement(@NotNull String signature, @NotNull String typeText) {
        return LookupElementBuilder.create(signature).withTypeText(typeText, true);
    }

    @NotNull
    public static LookupElementBuilder createDirectiveNameLookupElement(@NotNull String name, boolean hasRequiredArgs) {
        LookupElementBuilder element = LookupElementBuilder.create(name);
        if (hasRequiredArgs) {
            // found a required argument so insert the '()' for arguments
            element = element.withInsertHandler((ctx, item) -> {
                ParenthesesInsertHandler.WITH_PARAMETERS.handleInsert(ctx, item);
                AutoPopupController.getInstance(ctx.getProject()).autoPopupMemberLookup(ctx.getEditor(), null);
            });
        }
        return element;
    }

    @NotNull
    public static LookupElementBuilder createArgumentNameLookupElement(@NotNull String name, @NotNull String typeText) {
        return LookupElementBuilder.create(name)
            .withTypeText(typeText)
            .withInsertHandler(AddColonSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP);
    }
}
