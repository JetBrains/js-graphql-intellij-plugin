/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.annotator;

import com.google.common.collect.Lists;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression;
import com.intellij.lang.jsgraphql.ide.project.JSGraphQLLanguageUIProjectService;
import com.intellij.lang.jsgraphql.ide.injection.JSGraphQLLanguageInjectionUtil;
import com.intellij.lang.jsgraphql.ide.project.toolwindow.JSGraphQLErrorResult;
import com.intellij.lang.jsgraphql.languageservice.JSGraphQLNodeLanguageServiceClient;
import com.intellij.lang.jsgraphql.languageservice.api.Annotation;
import com.intellij.lang.jsgraphql.languageservice.api.AnnotationsResponse;
import com.intellij.lang.jsgraphql.languageservice.api.Pos;
import com.intellij.lang.jsgraphql.psi.JSGraphQLFile;
import com.intellij.lang.jsgraphql.schema.psi.JSGraphQLSchemaFile;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class JSGraphQLAnnotator extends ExternalAnnotator<JSGraphQLAnnotationResult, JSGraphQLAnnotationResult> {

    private final static Logger log = Logger.getInstance(JSGraphQLAnnotator.class);

    @Nullable
    @Override
    public JSGraphQLAnnotationResult collectInformation(@NotNull PsiFile file, @NotNull Editor editor, boolean hasErrors) {
        try {
            boolean isJavaScript = file instanceof JSFile;
            if(isJavaScript || file instanceof JSGraphQLFile) {
                CharSequence buffer = editor.getDocument().getCharsSequence();
                if (isJavaScript) {
                    // replace the JS with line-preserving whitespace to be ignored by GraphQL
                    buffer = getWhitespacePaddedGraphQL(file, buffer);
                }
                if (buffer.length() > 0) {
                    final boolean relay = JSGraphQLLanguageInjectionUtil.isRelayInjection(file);
                    final AnnotationsResponse annotations = JSGraphQLNodeLanguageServiceClient.getAnnotations(buffer.toString(), file.getProject(), relay);
                    return new JSGraphQLAnnotationResult(annotations, editor);
                }
            } else if(file instanceof JSGraphQLSchemaFile) {
                // no external annotation support yet for schema files
                return new JSGraphQLAnnotationResult(new AnnotationsResponse(), editor);
            }
        } catch (Throwable e) {
            if(e instanceof ProcessCanceledException) {
                // annotation was cancelled, e.g. due to an editor being closed
                return null;
            }
            log.error("Error during doAnnotate", e);
        }
        return null;
    }

    @Nullable
    @Override
    public JSGraphQLAnnotationResult doAnnotate(JSGraphQLAnnotationResult collectedInfo) {
        return collectedInfo;
    }

    @Override
    public void apply(@NotNull PsiFile file, JSGraphQLAnnotationResult annotationResult, @NotNull AnnotationHolder holder) {
        if(annotationResult != null) {
            try {
                final Editor editor = annotationResult.getEditor();
                final String fileName = file.getVirtualFile().getPath();
                final List<JSGraphQLErrorResult> errors = Lists.newArrayList();
                final JSGraphQLLanguageWarningAnnotator internalAnnotator = new JSGraphQLLanguageWarningAnnotator();
                file.accept(new PsiRecursiveElementVisitor() {
                    @Override
                    public void visitElement(PsiElement element) {
                        final com.intellij.lang.annotation.Annotation annotation = internalAnnotator.annotate(file, element, holder);
                        if(annotation != null) {
                            final LogicalPosition pos = editor.offsetToLogicalPosition(element.getTextOffset());
                            errors.add(new JSGraphQLErrorResult(annotation.getMessage(), fileName, annotation.getSeverity().myName, pos.line + 1, pos.column + 1));
                        }
                        super.visitElement(element);
                    }
                });
                AnnotationsResponse annotationsReponse = annotationResult.getAnnotationsReponse();
                if(annotationsReponse == null) {
                    return;
                }
                for (Annotation annotation : annotationsReponse.getAnnotations()) {
                    LogicalPosition from = getLogicalPosition(annotation.getFrom());
                    LogicalPosition to = getLogicalPosition(annotation.getTo());
                    int fromOffset = editor.logicalPositionToOffset(from);
                    int toOffset = editor.logicalPositionToOffset(to);
                    HighlightSeverity severity = "error".equals(annotation.getSeverity()) ? HighlightSeverity.ERROR : HighlightSeverity.WARNING;
                    if (fromOffset < toOffset) {
                        final String message = StringUtils.substringBefore(annotation.getMessage(), "\n");
                        holder.createAnnotation(severity, TextRange.create(fromOffset, toOffset), message);
                        errors.add(new JSGraphQLErrorResult(message, fileName, annotation.getSeverity(), from.line+1, from.column+1)); // +1 is for UI lines/columns
                    }
                }
                JSGraphQLLanguageUIProjectService jsGraphQLLanguageUIProjectService = JSGraphQLLanguageUIProjectService.getService(file.getProject());
                if(jsGraphQLLanguageUIProjectService != null) {
                    jsGraphQLLanguageUIProjectService.logErrorsInCurrentFile(file, errors);
                }
            } catch (Exception e) {
                log.error("Unable to apply annotations", e);
            } finally {
                annotationResult.releaseEditor();
            }
        }
    }


    // --- implementation ----

    private CharSequence getWhitespacePaddedGraphQL(PsiFile psiFile, CharSequence buffer) {
        // find the template expressions in the file
        Collection<JSStringTemplateExpression> stringTemplateExpressions = PsiTreeUtil.collectElementsOfType(psiFile, JSStringTemplateExpression.class);
        StringBuilder sb = new StringBuilder(0);
        Integer builderPos = null;
        for (JSStringTemplateExpression stringTemplateExpression : stringTemplateExpressions) {
            if(JSGraphQLLanguageInjectionUtil.isJSGraphQLLanguageInjectionTarget(stringTemplateExpression)) {
                final TextRange graphQLTextRange = JSGraphQLLanguageInjectionUtil.getGraphQLTextRange(stringTemplateExpression);
                if(builderPos == null) {
                    sb.setLength(buffer.length());
                    builderPos = 0;
                }
                // write the JS as whitespace so it'll be ignored by the GraphQL tooling, while preserving line numbers and columns.
                TextRange templateTextRange = stringTemplateExpression.getTextRange();
                int graphQLStartOffset = templateTextRange.getStartOffset() + graphQLTextRange.getStartOffset();
                int graphQLEndOffset = templateTextRange.getStartOffset() + graphQLTextRange.getEndOffset();
                applyWhiteSpace(buffer, sb, builderPos, graphQLStartOffset);
                String graphQLText = buffer.subSequence(graphQLStartOffset, graphQLEndOffset /* end is exclusive*/).toString();
                sb.replace(graphQLStartOffset, graphQLEndOffset /* end is exclusive*/, graphQLText);
                builderPos = graphQLEndOffset /* start next whitespace padding after the graph ql */;
            }
        }

        // last whitespace segment
        if(builderPos != null && builderPos < buffer.length()) {
            applyWhiteSpace(buffer, sb, builderPos, buffer.length());
        }

        return sb;
    }

    private void applyWhiteSpace(CharSequence source, StringBuilder target, int start, int end) {
        for(int i = start; i < end; i++) {
            char c = source.charAt(i);
            switch (c) {
                case '\t':
                case '\n':
                    target.setCharAt(i, c);
                    break;
                default:
                    target.setCharAt(i, ' ');
                    break;
            }
        }
    }

    @NotNull
    private LogicalPosition getLogicalPosition(Pos pos) {
        return new LogicalPosition(pos.getLine(), pos.getCh());
    }
}
