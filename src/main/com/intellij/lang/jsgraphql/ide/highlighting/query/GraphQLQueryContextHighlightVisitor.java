/*
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.highlighting.query;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.intellij.codeInsight.highlighting.HighlightManager;
import com.intellij.codeInsight.highlighting.HighlightManagerImpl;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.hint.HintManagerImpl;
import com.intellij.codeInsight.hint.HintUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.jsgraphql.GraphQLBundle;
import com.intellij.lang.jsgraphql.ide.highlighting.GraphQLSyntaxAnnotator;
import com.intellij.lang.jsgraphql.ide.notifications.GraphQLNotificationUtil;
import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.ui.LightweightHint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public final class GraphQLQueryContextHighlightVisitor {

  private static final String QUERY_SELECT_OPERATION_HINT_PREF_KEY = "JSGraphQL.Query.Select.Operation.Hint";
  private static final String HIDE_LINK = "hide";
  private static final String SELECT_OPERATION_LINK = "select-operation";

  private static String getFragmentKey(GraphQLFragmentDefinition definition) {
    if (definition == null) {
      return "";
    }
    return GraphQLPsiUtil.getPhysicalFileName(definition.getContainingFile()) + ":" + definition.getName();
  }

  private static void removeHighlights(Editor editor, Project project) {
    HighlightManagerImpl highlightManager = (HighlightManagerImpl)HighlightManager.getInstance(project);
    for (RangeHighlighter rangeHighlighter : highlightManager.getHighlighters(editor)) {
      highlightManager.removeSegmentHighlighter(editor, rangeHighlighter);
    }
  }

  /**
   * Gets the contextual query to send to the server based on the selection or operation, if any, that wraps the current caret position
   *
   * @param editor the editor containing the query buffer
   * @return a query context with minimal query buffer that contains the current selection, operation, or the entire buffer if none is found
   */
  public static GraphQLQueryContext getQueryContextBufferAndHighlightUnused(@NotNull Editor editor) {
    Project project = editor.getProject();
    Document document = editor.getDocument();

    if (project != null) {
      PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
      if (psiFile != null) {
        boolean hasSelection = editor.getSelectionModel().hasSelection();
        CharSequence buffer = document.getImmutableCharSequence();
        int bufferLength = buffer.length();

        // if there's a selection we send that to the server, with an error callback that hints
        // that placing the caret inside an operation will include any used fragments
        if (hasSelection) {
          return runQueryForSelection(project, editor, psiFile, buffer, bufferLength);
        }

        // no selection -- see if the caret is inside an operation
        GraphQLOperationDefinition operationAtCursor = getOperationAtOffset(psiFile, editor.getCaretModel().getOffset());
        if (operationAtCursor != null) {
          Map<String, GraphQLFragmentDefinition> foundFragments = Maps.newHashMap();
          findFragmentsInsideOperation(operationAtCursor, foundFragments, null);
          Set<PsiElement> queryElements = Sets.newHashSet(foundFragments.values());
          queryElements.add(operationAtCursor);
          StringBuilder query = new StringBuilder(bufferLength);
          for (PsiElement psiElement : psiFile.getChildren()) {
            if (psiElement instanceof PsiWhiteSpace) {
              if (!queryElements.isEmpty()) {
                query.append(psiElement.getText());
              }
            }
            else {
              TextRange textRange = psiElement.getTextRange();
              String fragmentKey = "";
              if (psiElement instanceof GraphQLFragmentDefinition) {
                fragmentKey = getFragmentKey((GraphQLFragmentDefinition)psiElement);
              }
              if (queryElements.contains(psiElement) || foundFragments.containsKey(fragmentKey)) {
                queryElements.remove(psiElement);
                GraphQLFragmentDefinition fragmentDefinition = foundFragments.get(fragmentKey);
                if (fragmentDefinition != null) {
                  queryElements.remove(fragmentDefinition);
                }
                query.append(buffer.subSequence(textRange.getStartOffset(), textRange.getEndOffset()));
              }
              else {
                if (!queryElements.isEmpty()) {
                  // element is not part of the query context so add it as new-lined whitespace
                  for (int i = textRange.getStartOffset(); i < textRange.getEndOffset(); i++) {
                    char c = buffer.charAt(i);
                    if (c == '\n') {
                      // add new-line to preserve query line numbers for errors etc.
                      query.append(c);
                    }
                    else {
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
            query.append(GraphQLPsiUtil.getPhysicalFileName(queryElement.getContainingFile())).append("\" ----\n");
            query.append(queryElement.getText());
          }

          if (operationAtCursor.getNameIdentifier() != null) {
            // named operation
            showQueryContextHint(
              editor,
              GraphQLBundle.message(
                "graphql.hint.text.executed.named.operation",
                getOperationKind(operationAtCursor),
                operationAtCursor.getNameIdentifier().getText())
            );
          }
          else {
            // anonymous operation
            showQueryContextHint(
              editor,
              GraphQLBundle.message("graphql.hint.text.executed.anonymous.operation", getOperationKind(operationAtCursor))
            );
          }
          return new GraphQLQueryContext(query.toString(), null);
        }
      }
    }

    // fallback is the entire buffer
    VirtualFile file = FileDocumentManager.getInstance().getFile(document);
    if (file != null) {
      showQueryContextHint(editor, GraphQLBundle.message("graphql.hint.text.executed.buffer", file.getPresentableName()));
    }

    return new GraphQLQueryContext(document.getText(), null);
  }

  private static @NotNull GraphQLQueryContext runQueryForSelection(@NotNull Project project,
                                                                   @NotNull Editor editor,
                                                                   @NotNull PsiFile psiFile,
                                                                   @NotNull CharSequence buffer,
                                                                   int bufferLength) {
    // only send the selection, replacing everything else with line-space preserving whitespace
    StringBuilder query = new StringBuilder(bufferLength);
    Stream<Caret> carets = editor.getCaretModel().getAllCarets().stream();
    Collection<TextRange> selectedRanges = carets
      .filter(Caret::hasSelection)
      .map(caret -> new TextRange(caret.getSelectionStart(), caret.getSelectionEnd()))
      .sorted(Comparator.comparingInt(TextRange::getStartOffset))
      .toList();

    for (int i = 0; i < bufferLength; i++) {
      char c = buffer.charAt(i);
      if (c == '\n') {
        // add new-line to preserve query line numbers for errors etc.
        query.append(c);
      }
      else {
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
    Collection<TextRange> unusedRanges = new ArrayList<>();
    for (TextRange selectedRange : selectedRanges) {
      if (startOffset < selectedRange.getStartOffset()) {
        unusedRanges.add(new TextRange(startOffset, selectedRange.getStartOffset()));
      }
      startOffset = selectedRange.getEndOffset();
    }
    if (startOffset < bufferLength) {
      unusedRanges.add(new TextRange(startOffset, bufferLength));
    }

    for (TextRange unusedRange : unusedRanges) {
      highlightUnusedRange(editor, unusedRange);
    }

    showQueryContextHint(editor, GraphQLBundle.message("graphql.editor.hint.text.executed.selection"));

    return new GraphQLQueryContext(query.toString(), () -> {
      if (HIDE_LINK.equals(PropertiesComponent.getInstance(project).getValue(QUERY_SELECT_OPERATION_HINT_PREF_KEY))) {
        // user has clicked hide to not see this message again
        return;
      }
      // query error callback
      // add a hint to use caret position instead
      Notification notification = new Notification(
        GraphQLNotificationUtil.GRAPHQL_NOTIFICATION_GROUP_ID,
        GraphQLBundle.message("graphql.notification.title.limit.graphql.that.sent.to.server"),
        GraphQLBundle.message("graphql.editor.query.hint.description", SELECT_OPERATION_LINK, HIDE_LINK),
        NotificationType.INFORMATION
      );
      notification.setListener((source, event) -> {
        if (HIDE_LINK.equals(event.getDescription())) {
          PropertiesComponent.getInstance(project).setValue(QUERY_SELECT_OPERATION_HINT_PREF_KEY, HIDE_LINK);
        }
        else if (SELECT_OPERATION_LINK.equals(event.getDescription())) {
          placeCaretInsideFirstOperation(editor, psiFile);
          removeHighlights(editor, project);
        }
        source.expire();
      });
      Notifications.Bus.notify(notification, project);
    });
  }

  /**
   * Uses a range highlighter to show a range of unused text as dimmed
   */
  private static void highlightUnusedRange(Editor editor, TextRange textRange) {
    Project project = editor.getProject();
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
      return ((GraphQLTypedOperationDefinition)operation).getOperationType().getText();
    }
    return "operation";
  }


  /**
   * Shows a query context hint under the current caret position.
   * The hint hides after a few seconds or when the user interacts with the editor
   */
  private static void showQueryContextHint(Editor editor, @NlsContexts.HintText String hintText) {
    HintManagerImpl hintManager = HintManagerImpl.getInstanceImpl();
    JComponent label = HintUtil.createInformationLabel(hintText);
    LightweightHint lightweightHint = new LightweightHint(label);
    Point hintPosition = hintManager.getHintPosition(lightweightHint, editor, HintManager.UNDER);
    hintManager.showEditorHint(lightweightHint, editor, hintPosition, 0, 2000, false, HintManager.UNDER);
  }

  public static @Nullable GraphQLOperationDefinition getOperationAtOffset(@NotNull PsiFile psiFile, int offset) {
    if (offset == -1) return null;
    var element = InjectedLanguageManager.getInstance(psiFile.getProject()).findInjectedElementAt(psiFile, offset);
    if (element == null) {
      element = PsiUtilCore.getElementAtOffset(psiFile, offset);
    }
    return PsiTreeUtil.getTopmostParentOfType(element, GraphQLOperationDefinition.class);
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
          Project project = editor.getProject();
          if (project != null) {
            // try to find the name of the operation
            if (operationOrNull instanceof GraphQLSelectionSetOperationDefinition) {
              // unnamed query
              navigationTarget = ((GraphQLSelectionSetOperationDefinition)operationOrNull).getSelectionSet();
            }
            else if (operationOrNull.getNameIdentifier() != null) {
              navigationTarget = operationOrNull.getNameIdentifier();
            }
            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
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
          PsiReference reference = ((GraphQLFragmentSpread)element).getNameIdentifier().getReference();
          if (reference != null) {
            PsiElement fragmentDefinitionRef = reference.resolve();
            if (fragmentDefinitionRef instanceof GraphQLIdentifier) {
              if (fragmentDefinitionRef.getOriginalElement() instanceof GraphQLIdentifier) {
                fragmentDefinitionRef = fragmentDefinitionRef.getOriginalElement();
              }
              GraphQLFragmentDefinition fragment =
                PsiTreeUtil.getParentOfType(fragmentDefinitionRef, GraphQLFragmentDefinition.class);
              String fragmentKey = getFragmentKey(fragment);
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
