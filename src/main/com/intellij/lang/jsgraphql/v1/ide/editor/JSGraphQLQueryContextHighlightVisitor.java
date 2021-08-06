/*
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.ide.editor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.HighlightVisitor;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.codeInsight.highlighting.HighlightManager;
import com.intellij.codeInsight.highlighting.HighlightManagerImpl;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.hint.HintManagerImpl;
import com.intellij.codeInsight.hint.HintUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.jsgraphql.GraphQLPluginDisposable;
import com.intellij.lang.jsgraphql.ide.highlighting.GraphQLSyntaxAnnotator;
import com.intellij.lang.jsgraphql.ide.notifications.GraphQLNotificationUtil;
import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.LightweightHint;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Highlights the operation at the cursor and any fragments in relies on in the editor.
 * Also provides the query buffer that corresponds to that highlight to execute it against a server.
 * Elements not included in query execution are dimmed down.
 */
public class JSGraphQLQueryContextHighlightVisitor implements HighlightVisitor, DumbAware {

    // operation user data
    private static final Key<Boolean> QUERY_HIGHLIGHT_LISTENER_ADDED = Key.create("JSGraphQL.Query.Highlighter.Listener.Added");
    private static final Key<TextRange> QUERY_OPERATION_TEXT_RANGE = Key.create("JSGraphQL.Query.Operation.TextRange");
    private static final Key<Boolean> QUERY_FROM_SELECTION = Key.create("JSGraphQL.Query.From.Selection");

    // select operation hint
    private static final String QUERY_SELECT_OPERATION_HINT_PREF_KEY = "JSGraphQL.Query.Select.Operation.Hint";
    private static final String HIDE_LINK = "hide";
    private static final String SELECT_OPERATION_LINK = "select-operation";
    public static final String QUERY_CONTEXT_HINT_MESSAGE = "Place the caret <a href=\"" +
        SELECT_OPERATION_LINK +
        "\">inside an operation</a>" +
        " to execute it on its own.<br> " +
        "Referenced fragments are automatically included.<br>" +
        "<div style=\"margin: 4px 0 4px 0;\">" +
        "<a style=\"text-decoration: none\" href=\"" +
        HIDE_LINK +
        "\">Don't show this again</a></div>";

    private static final String ELEMENT_INCLUDED_MESSAGE = "Element is included in query execution";


    @Override
    public boolean suitableForFile(@NotNull PsiFile file) {
        return file instanceof GraphQLFile;
    }

    @Override
    public void visit(@NotNull PsiElement element) {
    }

    /**
     * Highlights the operation, if any, that wraps the current caret position.
     * Fragments used from the operation are highlighted recursively.
     */
    @Override
    public boolean analyze(final @NotNull PsiFile file, boolean updateWholeFile, @NotNull HighlightInfoHolder holder, @NotNull Runnable action) {

        // run the default pass first (DefaultHighlightVisitor) which calls annotators etc.
        action.run();

        final PsiElement operationAtCursor = getOperationAtCursor(file);
        if (operationAtCursor != null && hasMultipleVisibleTopLevelElement(file)) {

            // store the range of the current operation for use in the caret listener
            file.putUserData(QUERY_OPERATION_TEXT_RANGE, operationAtCursor.getTextRange());

            final Color borderColor = EditorColorsManager.getInstance().getGlobalScheme().getColor(EditorColors.TEARLINE_COLOR);
            final TextAttributes textAttributes = new TextAttributes(null, null, borderColor, EffectType.ROUNDED_BOX, Font.PLAIN);
            final Map<String, GraphQLFragmentDefinition> foundFragments = Maps.newHashMap();
            findFragmentsInsideOperation(operationAtCursor, foundFragments, null);
            for (PsiElement psiElement : file.getChildren()) {
                boolean showAsUsed = false;
                if (psiElement instanceof GraphQLFragmentDefinition) {
                    GraphQLFragmentDefinition definition = (GraphQLFragmentDefinition) psiElement;
                    if (definition.getOriginalElement() instanceof GraphQLFragmentDefinition) {
                        // use the original PSI to compare since a separate editor tab has its own version of the PSI
                        definition = (GraphQLFragmentDefinition) definition.getOriginalElement();
                    }
                    showAsUsed = foundFragments.containsKey(getFragmentKey(definition));
                } else if (psiElement == operationAtCursor) {
                    showAsUsed = true;
                }
                if (showAsUsed) {
                    holder.add(
                        HighlightInfo
                            .newHighlightInfo(HighlightInfoType.INFORMATION)
                            .textAttributes(textAttributes)
                            .range(psiElement.getTextRange())
                            .description(ELEMENT_INCLUDED_MESSAGE)
                            .create()
                    );
                }
            }

        } else {
            file.putUserData(QUERY_OPERATION_TEXT_RANGE, null);
        }


        // find the editor that was highlighted and listen for caret changes to update the active operation
        UIUtil.invokeLaterIfNeeded(() -> {
            final FileEditor fileEditor = FileEditorManager.getInstance(file.getProject()).getSelectedEditor(file.getVirtualFile());
            if (fileEditor instanceof TextEditor) {
                final Editor editor = ((TextEditor) fileEditor).getEditor();
                if (!Boolean.TRUE.equals(editor.getUserData(QUERY_HIGHLIGHT_LISTENER_ADDED))) {
                    editor.getCaretModel().addCaretListener(new CaretListener() {
                        @Override
                        public void caretPositionChanged(@NotNull CaretEvent e) {
                            // re-highlight when the operation changes

                            final Editor currentEditor = e.getEditor();
                            final Project project = currentEditor.getProject();
                            if (project != null) {

                                final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(currentEditor.getDocument());
                                if (psiFile != null) {

                                    final TextRange previousOperationRange = psiFile.getUserData(QUERY_OPERATION_TEXT_RANGE);
                                    psiFile.putUserData(QUERY_FROM_SELECTION, currentEditor.getSelectionModel().hasSelection());

                                    boolean sameOperation = false;
                                    boolean hadOperation = (previousOperationRange != null);
                                    if (hadOperation) {
                                        // check if we're still inside the range of the previously highlighted op
                                        final int newOffset = currentEditor.logicalPositionToOffset(e.getNewPosition());
                                        sameOperation = previousOperationRange.contains(newOffset);
                                        if (sameOperation && !Boolean.TRUE.equals(psiFile.getUserData(QUERY_FROM_SELECTION))) {
                                            // still the same op, and we didn't select text before, so no need to proceed
                                            return;
                                        }
                                    }

                                    // remove existing unused query text range highlights
                                    removeHighlights(currentEditor, project);

                                    if (!sameOperation) {
                                        // moved to somewhere outside the previous operation
                                        if (hadOperation || getOperationAtCursor(psiFile) != null) {
                                            // perform a new highlighting pass
                                            DaemonCodeAnalyzer.getInstance(project).restart(psiFile);
                                        }
                                    }
                                }
                            }
                        }
                    }, GraphQLPluginDisposable.getInstance(file.getProject()));
                    // finally, indicate we've added the listener
                    editor.putUserData(QUERY_HIGHLIGHT_LISTENER_ADDED, true);
                }
            }
        });

        return true;
    }

    private static String getFragmentKey(GraphQLFragmentDefinition definition) {
        if (definition == null) {
            return "";
        }
        return GraphQLPsiUtil.getFileName(definition.getContainingFile()) + ":" + definition.getName();
    }

    private static void removeHighlights(Editor editor, Project project) {
        HighlightManagerImpl highlightManager = (HighlightManagerImpl) HighlightManager.getInstance(project);
        for (RangeHighlighter rangeHighlighter : highlightManager.getHighlighters(editor)) {
            highlightManager.removeSegmentHighlighter(editor, rangeHighlighter);
        }
    }

    /**
     * Indicates whether multiple visible top level psi elements exist.
     * If there's not, then there's no need to do contextual highlight
     */
    private boolean hasMultipleVisibleTopLevelElement(PsiFile file) {
        int visibleChildren = 0;
        for (PsiElement psiElement : file.getChildren()) {
            if (psiElement instanceof PsiWhiteSpace || psiElement instanceof PsiComment) {
                continue;
            }
            visibleChildren++;
            if (visibleChildren > 1) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @NotNull
    @Override
    public HighlightVisitor clone() {
        return new JSGraphQLQueryContextHighlightVisitor();
    }

    /**
     * Gets the contextual query to send to the server based on the selection or operation, if any, that wraps the current caret position
     *
     * @param editor the editor containing the query buffer
     * @return a query context with minimal query buffer that contains the current selection, operation, or the entire buffer if none is found
     */
    public static JSGraphQLQueryContext getQueryContextBufferAndHighlightUnused(final Editor editor) {
        if (editor.getProject() != null) {

            final PsiFile psiFile = PsiDocumentManager.getInstance(editor.getProject()).getPsiFile(editor.getDocument());
            if (psiFile != null) {

                final boolean hasSelection = editor.getSelectionModel().hasSelection();
                psiFile.putUserData(QUERY_FROM_SELECTION, hasSelection);

                final CharSequence editorBuffer = editor.getDocument().getCharsSequence();
                final int editorLength = psiFile.getTextLength();

                // if there's a selection we send that to the server, with an error callback that hints
                // that placing the caret inside an operation will include any used fragments
                if (hasSelection) {

                    // only send the selection, replacing everything else with line-space preserving whitespace
                    final StringBuilder query = new StringBuilder(editorLength);
                    final Stream<Caret> carets = editor.getCaretModel().getAllCarets().stream();
                    final Collection<TextRange> selectedRanges = carets
                        .filter(Caret::hasSelection)
                        .map(caret -> new TextRange(caret.getSelectionStart(), caret.getSelectionEnd()))
                        .sorted(Comparator.comparingInt(TextRange::getStartOffset))
                        .collect(Collectors.toList());

                    for (int i = 0; i < editorLength; i++) {
                        final char c = editorBuffer.charAt(i);
                        if (c == '\n') {
                            // add new-line to preserve query line numbers for errors etc.
                            query.append(c);
                        } else {
                            // non-line wrap so add as-is if selected, or as blank text if not selected
                            boolean selected = false;
                            for (TextRange selectedRange : selectedRanges) {
                                if (selectedRange.contains(i)) {
                                    query.append(c);
                                    selected = true;
                                    break;
                                }
                            }
                            if (!selected) {
                                query.append(' ');
                            }
                        }
                    }

                    // indicate in the editor which text wasn't used
                    int startOffset = 0;
                    final Collection<TextRange> unusedRanges = Lists.newArrayList();
                    for (TextRange selectedRange : selectedRanges) {
                        if (startOffset < selectedRange.getStartOffset()) {
                            unusedRanges.add(new TextRange(startOffset, selectedRange.getStartOffset()));
                        }
                        startOffset = selectedRange.getEndOffset();
                    }
                    if (startOffset < editorLength) {
                        unusedRanges.add(new TextRange(startOffset, editorLength));
                    }

                    for (TextRange unusedRange : unusedRanges) {
                        highlightUnusedRange(editor, unusedRange);
                    }

                    showQueryContextHint(editor, "Executed selection");

                    return new JSGraphQLQueryContext(query.toString(), () -> {
                        if (HIDE_LINK.equals(PropertiesComponent.getInstance(editor.getProject()).getValue(QUERY_SELECT_OPERATION_HINT_PREF_KEY))) {
                            // user has clicked hide to not see this message again
                            return;
                        }
                        // query error callback
                        // add a hint to use caret position instead
                        final Notification notification = new Notification(
                            GraphQLNotificationUtil.NOTIFICATION_GROUP_ID,
                            "Limit the GraphQL that is sent to the server?",
                            QUERY_CONTEXT_HINT_MESSAGE,
                            NotificationType.INFORMATION
                        );
                        notification.setListener((source, event) -> {
                            if (HIDE_LINK.equals(event.getDescription())) {
                                PropertiesComponent.getInstance(editor.getProject()).setValue(QUERY_SELECT_OPERATION_HINT_PREF_KEY, HIDE_LINK);
                            } else if (SELECT_OPERATION_LINK.equals(event.getDescription())) {
                                placeCaretInsideFirstOperation(editor, psiFile);
                                removeHighlights(editor, editor.getProject());
                            }
                            source.expire();
                        });
                        Notifications.Bus.notify(notification, editor.getProject());
                    });


                }

                // no selection -- see if the caret is inside an operation

                final GraphQLOperationDefinition operationAtCursor = getOperationAtCursor(psiFile);
                if (operationAtCursor != null) {
                    final Map<String, GraphQLFragmentDefinition> foundFragments = Maps.newHashMap();
                    findFragmentsInsideOperation(operationAtCursor, foundFragments, null);
                    Set<PsiElement> queryElements = Sets.newHashSet(foundFragments.values());
                    queryElements.add(operationAtCursor);
                    final StringBuilder query = new StringBuilder(editorLength);
                    for (PsiElement psiElement : psiFile.getChildren()) {
                        if (psiElement instanceof PsiWhiteSpace) {
                            if (!queryElements.isEmpty()) {
                                query.append(psiElement.getText());
                            }
                        } else {
                            final TextRange textRange = psiElement.getTextRange();
                            String fragmentKey = "";
                            if (psiElement instanceof GraphQLFragmentDefinition) {
                                fragmentKey = getFragmentKey((GraphQLFragmentDefinition) psiElement);
                            }
                            if (queryElements.contains(psiElement) || foundFragments.containsKey(fragmentKey)) {
                                queryElements.remove(psiElement);
                                GraphQLFragmentDefinition fragmentDefinition = foundFragments.get(fragmentKey);
                                if (fragmentDefinition != null) {
                                    queryElements.remove(fragmentDefinition);
                                }
                                query.append(editorBuffer.subSequence(textRange.getStartOffset(), textRange.getEndOffset()));
                            } else {
                                if (!queryElements.isEmpty()) {
                                    // element is not part of the query context so add it as new-lined whitespace
                                    for (int i = textRange.getStartOffset(); i < textRange.getEndOffset(); i++) {
                                        final char c = editorBuffer.charAt(i);
                                        if (c == '\n') {
                                            // add new-line to preserve query line numbers for errors etc.
                                            query.append(c);
                                        } else {
                                            // non-line wrap so add as blank text
                                            query.append(' ');
                                        }
                                    }
                                }
                                // tone down to indicate the text wasn't included
                                highlightUnusedRange(editor, textRange);
                            }
                        }
                    }

                    // include fragments from other PsiFiles
                    for (PsiElement queryElement : queryElements) {
                        query.append("\n\n# ---- fragment automatically included from \"");
                        query.append(GraphQLPsiUtil.getFileName(queryElement.getContainingFile())).append("\" ----\n");
                        query.append(queryElement.getText());
                    }

                    if (operationAtCursor.getNameIdentifier() != null) {
                        // named operation
                        showQueryContextHint(editor, "Executed " + getOperationKind(operationAtCursor) + " \"" + operationAtCursor.getNameIdentifier().getText() + "\"");
                    } else {
                        // anonymous operation
                        showQueryContextHint(editor, "Executed anonymous " + getOperationKind(operationAtCursor));
                    }
                    return new JSGraphQLQueryContext(query.toString(), null);
                }
            }

        }
        // fallback is the entire buffer
        final VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (file != null) {
            showQueryContextHint(editor, "Executed buffer \"" + file.getPresentableName() + "\"");
        }

        return new JSGraphQLQueryContext(editor.getDocument().getText(), null);
    }

    /**
     * Uses a range highlighter to show a range of unused text as dimmed
     */
    private static void highlightUnusedRange(Editor editor, TextRange textRange) {
        final Project project = editor.getProject();
        if (project != null) {
            HighlightManager.getInstance(project).addRangeHighlight(
                editor,
                textRange.getStartOffset(),
                textRange.getEndOffset(),
                GraphQLSyntaxAnnotator.UNUSED_FRAGMENT, true, true, null);
        }
    }

    /**
     * Determines the kind of operation keyword that defines an operation, e.g. "query" or "mutation"
     */
    private static String getOperationKind(GraphQLOperationDefinition operation) {
        if (operation instanceof GraphQLSelectionSetOperationDefinition) {
            return "query";
        }
        if (operation instanceof GraphQLTypedOperationDefinition) {
            return ((GraphQLTypedOperationDefinition) operation).getOperationType().getText();
        }
        return "operation";
    }


    /**
     * Shows a query context hint under the current caret position.
     * The hint hides after a few seconds or when the user interacts with the editor
     */
    private static void showQueryContextHint(Editor editor, String hintText) {
        final HintManagerImpl hintManager = HintManagerImpl.getInstanceImpl();
        final JComponent label = HintUtil.createInformationLabel(hintText);
        final LightweightHint lightweightHint = new LightweightHint(label);
        final Point hintPosition = hintManager.getHintPosition(lightweightHint, editor, HintManager.UNDER);
        hintManager.showEditorHint(lightweightHint, editor, hintPosition, 0, 2000, false, HintManager.UNDER);
    }

    /**
     * Gets the operation that wraps the current caret position, or <code>null</code> if none is found,
     * e.g. when outside any operation or inside a fragment definition
     */
    private static GraphQLOperationDefinition getOperationAtCursor(PsiFile psiFile) {

        final Integer caretOffset = psiFile.getUserData(JSGraphQLQueryContextCaretListener.CARET_OFFSET);

        if (caretOffset != null) {
            PsiElement currentElement = psiFile.findElementAt(caretOffset);
            while (currentElement != null && !(currentElement.getParent() instanceof PsiFile)) {
                currentElement = currentElement.getParent();
            }
            if (currentElement != null) {
                return asOperationOrNull(currentElement);
            }
        }
        return null;
    }

    /**
     * Returns the operation candidate if it's an operation, or <code>null</code>
     */
    private static GraphQLOperationDefinition asOperationOrNull(PsiElement operationCandidate) {
        return PsiTreeUtil.getParentOfType(operationCandidate, GraphQLOperationDefinition.class, false);
    }

    /**
     * Locates the first operation in the specified editor and places the caret inside it
     */
    private static void placeCaretInsideFirstOperation(Editor editor, PsiFile psiFile) {
        if (editor.isDisposed()) {
            return;
        }
        if (psiFile.isValid() && psiFile.getVirtualFile() != null && psiFile.getVirtualFile().isValid()) {
            for (PsiElement psiElement : psiFile.getChildren()) {
                if (psiElement instanceof PsiWhiteSpace) {
                    continue;
                }
                GraphQLOperationDefinition operationOrNull = asOperationOrNull(psiElement);
                if (operationOrNull != null) {
                    PsiElement navigationTarget = operationOrNull;
                    final Project project = editor.getProject();
                    if (project != null) {
                        // try to find the name of the operation
                        if (operationOrNull instanceof GraphQLSelectionSetOperationDefinition) {
                            // unnamed query
                            navigationTarget = ((GraphQLSelectionSetOperationDefinition) operationOrNull).getSelectionSet();
                        } else if (operationOrNull.getNameIdentifier() != null) {
                            navigationTarget = operationOrNull.getNameIdentifier();
                        }
                        final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                        fileEditorManager.openFile(psiFile.getVirtualFile(), true, true);
                        editor.getSelectionModel().removeSelection();
                        editor.getCaretModel().moveToOffset(navigationTarget.getTextOffset());
                        editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
                    }
                    return;
                }
            }
        }
    }

    /**
     * Locates the fragments used from inside an operation, and the fragments, if any, that are used from within those fragments
     *
     * @param operationOrFragment the operation to find used fragments for
     * @param foundFragments      a map fragments map keyed by filename:fragment-name to add the found fragments to
     * @param findMore            optional function to stop once a specific fragment has been found
     */
    private static void findFragmentsInsideOperation(PsiElement operationOrFragment, Map<String, GraphQLFragmentDefinition> foundFragments,
                                                     Function<GraphQLFragmentDefinition, Boolean> findMore) {

        operationOrFragment.accept(new PsiRecursiveElementVisitor() {

            private boolean done = false;

            @Override
            public void visitElement(@NotNull PsiElement element) {
                if (done) {
                    return;
                }
                if (element instanceof GraphQLFragmentSpread) {
                    final PsiReference reference = ((GraphQLFragmentSpread) element).getNameIdentifier().getReference();
                    if (reference != null) {
                        PsiElement fragmentDefinitionRef = reference.resolve();
                        if (fragmentDefinitionRef instanceof GraphQLIdentifier) {
                            if (fragmentDefinitionRef.getOriginalElement() instanceof GraphQLIdentifier) {
                                fragmentDefinitionRef = fragmentDefinitionRef.getOriginalElement();
                            }
                            final GraphQLFragmentDefinition fragment = PsiTreeUtil.getParentOfType(fragmentDefinitionRef, GraphQLFragmentDefinition.class);
                            final String fragmentKey = getFragmentKey(fragment);
                            if (fragment != null && !foundFragments.containsKey(fragmentKey)) {
                                foundFragments.put(fragmentKey, fragment);
                                if (findMore != null && !findMore.apply(fragment)) {
                                    // we're done
                                    done = true;
                                    return;
                                }
                                // also look for fragments inside this fragment
                                findFragmentsInsideOperation(fragment, foundFragments, findMore);
                            }

                        }
                    }

                }
                super.visitElement(element);
            }
        });
    }

}
