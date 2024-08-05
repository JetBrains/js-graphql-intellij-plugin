package com.intellij.lang.jsgraphql.ide.validation.fixes;

import com.intellij.codeInsight.daemon.impl.actions.SuppressByCommentFix;
import com.intellij.codeInspection.SuppressionUtil;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiParserFacade;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import static com.intellij.codeInspection.SuppressionUtilCore.SUPPRESS_INSPECTIONS_TAG_NAME;

public class GraphQLSuppressByCommentFix extends SuppressByCommentFix {

  private final @IntentionName String myText;

  public GraphQLSuppressByCommentFix(@NotNull String toolId,
                                     @NotNull Class<? extends PsiElement> suppressionHolderClass,
                                     @NotNull @IntentionName String text) {
    super(toolId, suppressionHolderClass);
    myText = text;
  }

  @Override
  public @IntentionName @NotNull String getText() {
    return myText;
  }

  @Override
  protected void createSuppression(@NotNull Project project,
                                   @NotNull PsiElement element,
                                   @NotNull PsiElement container) throws IncorrectOperationException {
    PsiComment comment = createComment(project, element);
    PsiElement parent = container.getParent();
    PsiElement added = parent.addBefore(comment, container);
    parent.addAfter(PsiParserFacade.getInstance(project).createWhiteSpaceFromText("\n"), added);
  }

  protected @NotNull PsiComment createComment(@NotNull Project project, @NotNull PsiElement element) {
    final String text = String.format(" %s %s", SUPPRESS_INSPECTIONS_TAG_NAME, myID);
    return SuppressionUtil.createComment(project, text, getCommentLanguage(element));
  }
}
