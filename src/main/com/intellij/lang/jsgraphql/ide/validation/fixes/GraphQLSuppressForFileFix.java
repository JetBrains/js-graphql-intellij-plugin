package com.intellij.lang.jsgraphql.ide.validation.fixes;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.jsgraphql.GraphQLBundle;
import com.intellij.lang.jsgraphql.psi.GraphQLElementFactory;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.lang.jsgraphql.psi.GraphQLPsiUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class GraphQLSuppressForFileFix extends GraphQLSuppressByCommentFix {

  public GraphQLSuppressForFileFix(@NotNull String toolId) {
    super(toolId, GraphQLFile.class, GraphQLBundle.message("graphql.inspection.suppress.for.file"));
  }

  @Override
  protected @Nullable List<? extends PsiElement> getCommentsFor(@NotNull PsiElement container) {
    if (!(container instanceof GraphQLFile)) return Collections.emptyList();
    return GraphQLPsiUtil.getLeadingFileComments(((GraphQLFile)container));
  }

  @Override
  protected void createSuppression(@NotNull Project project,
                                   @NotNull PsiElement element,
                                   @NotNull PsiElement container) throws IncorrectOperationException {
    if (!(container instanceof GraphQLFile)) return;

    boolean isInjected = InjectedLanguageManager.getInstance(project).isInjectedFragment(((GraphQLFile)container));

    PsiComment comment = createComment(project, element);
    PsiElement firstChild = container.getFirstChild();
    if (firstChild != null) {
      PsiElement added = container.addBefore(comment, firstChild);
      container.addAfter(GraphQLElementFactory.createNewLine(project), added);
      if (isInjected) {
        container.addBefore(GraphQLElementFactory.createNewLine(project), added);
      }
    }
    else {
      container.add(comment);
    }
  }
}
