package com.intellij.lang.jsgraphql.ide.completion;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.AddSpaceInsertHandler;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GraphQLCompletionUtil {
    private static final int DEFAULT_SYMBOL_PRIORITY = 5;
    private static final int CONTEXT_SYMBOL_PRIORITY = 30;

    public static final int KEYWORD_PRIORITY = DEFAULT_SYMBOL_PRIORITY;
    public static final int CONTEXT_KEYWORD_PRIORITY = CONTEXT_SYMBOL_PRIORITY;

    public static final int FIELD_PRIORITY = CONTEXT_SYMBOL_PRIORITY + 10; // normal field
    public static final int FIELD_AUX_PRIORITY = FIELD_PRIORITY - 1; // _service from Federation
    public static final int FIELD_SYSTEM_PRIORITY = FIELD_PRIORITY - 2; // __typename

    public static final int IMPLEMENT_FIELD = FIELD_PRIORITY + 5;

    public static final int TYPE_NAME_PRIORITY = FIELD_PRIORITY;
    public static final int TYPE_NAME_AUX_PRIORITY = TYPE_NAME_PRIORITY - 1; // starts with _
    public static final int TYPE_NAME_SYSTEM_PRIORITY = TYPE_NAME_PRIORITY - 2; // introspection types

    public static final int ARGUMENT_NAME_PRIORITY = FIELD_PRIORITY;

    public static final InsertHandler<LookupElement> ARGUMENTS_LIST_HANDLER = (context, item) -> {
        ParenthesesInsertHandler.WITH_PARAMETERS.handleInsert(context, item);
        AutoPopupController.getInstance(context.getProject()).autoPopupMemberLookup(context.getEditor(), null);
    };

    /**
     * TODO: This is a temporary hack, should be handled by the formatter which is currently not implemented.
     */
    public static final InsertHandler<LookupElement> ON_KEYWORD_HANDLER = (context, item) -> {
        Document document = context.getDocument();
        CharSequence chars = document.getCharsSequence();
        int offset = context.getEditor().getCaretModel().getOffset();
        int itemStartOffset = offset - item.getLookupString().length();
        int prevCharOffset = itemStartOffset - 1;
        if (prevCharOffset > 0 && !StringUtil.isChar(chars, prevCharOffset, ' ')) {
            document.insertString(itemStartOffset, " ");
        }

        AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP.handleInsert(context, item);
    };

    private GraphQLCompletionUtil() {
    }

    @NotNull
    public static LookupElement createKeywordLookupElement(@NotNull GraphQLCompletionKeyword keyword) {
        return createKeywordLookupElement(keyword.getText(), KEYWORD_PRIORITY, AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP);
    }

    @NotNull
    public static LookupElement createContextKeywordLookupElement(@NotNull GraphQLCompletionKeyword keyword) {
        return createKeywordLookupElement(keyword.getText(), CONTEXT_KEYWORD_PRIORITY, AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP);
    }

    @NotNull
    public static LookupElement createContextKeywordLookupElement(@NotNull GraphQLCompletionKeyword keyword,
                                                                  @NotNull InsertHandler<LookupElement> insertHandler) {
        return createKeywordLookupElement(keyword.getText(), CONTEXT_KEYWORD_PRIORITY, insertHandler);
    }

    @NotNull
    public static LookupElement createOperationNameKeywordLookupElement(@NotNull GraphQLCompletionKeyword keyword) {
        return createKeywordLookupElement(keyword.getText(), CONTEXT_KEYWORD_PRIORITY, AddColonSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP);
    }

    @NotNull
    private static LookupElement createKeywordLookupElement(@NotNull String text,
                                                            int priority,
                                                            @Nullable InsertHandler<LookupElement> insertHandler) {
        LookupElementBuilder element = LookupElementBuilder.create(text).bold();
        if (insertHandler != null) {
            element = element.withInsertHandler(insertHandler);
        }
        return PrioritizedLookupElement.withPriority(element, priority);
    }

    @NotNull
    public static LookupElement createTypeNameLookupElement(@NotNull String name) {
        return createTypeNameLookupElement(name, null);
    }

    @NotNull
    public static LookupElement createExtendTypeNameLookupElement(@NotNull String name) {
        return createTypeNameLookupElement(name, AddSpaceInsertHandler.INSTANCE);
    }

    @NotNull
    private static LookupElement createTypeNameLookupElement(@NotNull String name, @Nullable InsertHandler<LookupElement> handler) {
        int priority = TYPE_NAME_PRIORITY;
        if (name.startsWith("__")) {
            priority = TYPE_NAME_SYSTEM_PRIORITY;
        } else if (name.startsWith("_")) {
            priority = TYPE_NAME_AUX_PRIORITY;
        }

        LookupElementBuilder element = LookupElementBuilder.create(name);
        if (handler != null) {
            element = element.withInsertHandler(handler);
        }

        return PrioritizedLookupElement.withPriority(element, priority);
    }

    @NotNull
    public static LookupElement createDirectiveLocationLookupElement(@NotNull String name) {
        return PrioritizedLookupElement.withPriority(
            LookupElementBuilder.create(name).bold(),
            TYPE_NAME_PRIORITY
        );
    }

    @NotNull
    public static LookupElement createImplementFieldLookupElement(@NotNull String signature, @NotNull String typeText) {
        return PrioritizedLookupElement.withPriority(
            LookupElementBuilder.create(signature).withTypeText(typeText, true),
            IMPLEMENT_FIELD
        );
    }

    @NotNull
    public static LookupElement createDirectiveNameLookupElement(@NotNull String name, boolean hasRequiredArgs) {
        LookupElementBuilder element = LookupElementBuilder.create(name);
        if (hasRequiredArgs) {
            // found a required argument so insert the '()' for arguments
            element = element.withInsertHandler(ARGUMENTS_LIST_HANDLER);
        }
        return PrioritizedLookupElement.withPriority(element, TYPE_NAME_PRIORITY);
    }

    @NotNull
    public static LookupElement createArgumentNameLookupElement(@NotNull String name, @NotNull String typeText) {
        return PrioritizedLookupElement.withPriority(
            LookupElementBuilder.create(name)
                .withTypeText(typeText)
                .withInsertHandler(AddColonSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP),
            ARGUMENT_NAME_PRIORITY
        );
    }

    @NotNull
    public static LookupElement createFieldNameLookupElement(@NotNull String name,
                                                             @Nullable String typeText,
                                                             boolean isDeprecated,
                                                             boolean hasRequiredArgs) {
        int priority = FIELD_PRIORITY;
        if (name.startsWith("__")) {
            priority = FIELD_SYSTEM_PRIORITY;
        } else if (name.startsWith("_")) {
            priority = FIELD_AUX_PRIORITY;
        }

        LookupElementBuilder element = LookupElementBuilder.create(name)
            .withStrikeoutness(isDeprecated);

        if (typeText != null) {
            element = element.withTypeText(typeText);
        }
        if (hasRequiredArgs) {
            element = element.withInsertHandler(ARGUMENTS_LIST_HANDLER);
        }
        return PrioritizedLookupElement.withPriority(element, priority);
    }
}
