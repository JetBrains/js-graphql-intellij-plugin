package com.intellij.lang.jsgraphql.ide.validation;

import com.intellij.codeInspection.InspectionSuppressor;
import com.intellij.codeInspection.SuppressQuickFix;
import com.intellij.codeInspection.SuppressionUtil;
import com.intellij.lang.jsgraphql.GraphQLBundle;
import com.intellij.lang.jsgraphql.ide.validation.fixes.GraphQLSuppressByCommentFix;
import com.intellij.lang.jsgraphql.ide.validation.fixes.GraphQLSuppressForFileFix;
import com.intellij.lang.jsgraphql.psi.GraphQLDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLPsiUtil;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GraphQLInspectionSuppressor implements InspectionSuppressor {
  public static final Pattern SUPPRESS_IN_LINE_COMMENT_PATTERN = Pattern.compile("#" + SuppressionUtil.COMMON_SUPPRESS_REGEXP);

  @Override
  public boolean isSuppressedFor(@NotNull PsiElement element, @NotNull String toolId) {
    if (SuppressionUtil.getStatementToolSuppressedIn(element, toolId, GraphQLDefinition.class, SUPPRESS_IN_LINE_COMMENT_PATTERN) != null) {
      return true;
    }

    List<PsiComment> fileComments = GraphQLPsiUtil.getLeadingFileComments(element.getContainingFile());
    if (ContainerUtil.exists(fileComments, comment -> isSuppressedInComment(comment, toolId))) {
      return true;
    }

    return ContainerUtil.exists(
      GraphQLErrorFilter.EP_NAME.getExtensionList(),
      filter -> filter.isInspectionSuppressed(element.getProject(), toolId, element)
    );
  }

  @Override
  public SuppressQuickFix @NotNull [] getSuppressActions(@Nullable PsiElement element,
                                                         @NotNull String toolId) {
    if (element == null) return SuppressQuickFix.EMPTY_ARRAY;

    return new SuppressQuickFix[]{
      new GraphQLSuppressForFileFix(toolId),
      new GraphQLSuppressByCommentFix(
        toolId, GraphQLDefinition.class, GraphQLBundle.message("graphql.inspection.suppress.for.definition")),
    };
  }

  private static boolean isSuppressedInComment(@NotNull PsiComment comment, @NotNull String toolId) {
    String text = comment.getText();
    Matcher matcher = SUPPRESS_IN_LINE_COMMENT_PATTERN.matcher(text);
    return matcher.matches() && SuppressionUtil.isInspectionToolIdMentioned(matcher.group(1), toolId);
  }
}
