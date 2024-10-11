package com.intellij.lang.jsgraphql.ide.validation.inspections;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeInsight.daemon.impl.analysis.FileHighlightingSetting;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightingSettingsPerFile;
import com.intellij.codeInsight.intention.EmptyIntentionAction;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.*;
import com.intellij.codeInspection.ex.InspectionProfileImpl;
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.annotation.ProblemGroup;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.jsgraphql.ide.validation.GraphQLProblemGroup;
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class GraphQLInspection extends LocalInspectionTool {
  private static final Logger LOG = Logger.getInstance(GraphQLInspection.class);

  public static boolean isToolEnabled(@NotNull Project project,
                                      @NotNull Class<? extends GraphQLInspection> inspectionClass,
                                      @Nullable PsiElement context) {
    HighlightDisplayKey highlightDisplayKey = getHighlightDisplayKeyByClass(inspectionClass);
    if (highlightDisplayKey == null) {
      return false;
    }
    return isToolEnabled(project, highlightDisplayKey, context);
  }

  private static boolean isToolEnabled(@NotNull Project project,
                                       @NotNull HighlightDisplayKey highlightDisplayKey,
                                       @Nullable PsiElement context) {
    InspectionProfileImpl inspectionProfile = InspectionProjectProfileManager.getInstance(project).getCurrentProfile();
    return inspectionProfile.isToolEnabled(highlightDisplayKey, context);
  }

  private static @Nullable HighlightDisplayKey getHighlightDisplayKeyByClass(@NotNull Class<? extends GraphQLInspection> inspectionClass) {
    String shortName = getShortName(inspectionClass.getSimpleName());
    return HighlightDisplayKey.find(shortName);
  }

  public static @NotNull String getInspectionShortName(@NotNull Class<? extends GraphQLInspection> inspectionClass) {
    return getShortName(inspectionClass.getSimpleName());
  }

  private static @NotNull HighlightSeverity getSeverity(@NotNull HighlightDisplayKey highlightDisplayKey,
                                                        @NotNull PsiElement element) {
    return getHighlightDisplayLevel(highlightDisplayKey, element).getSeverity();
  }

  public static @NotNull HighlightDisplayLevel getHighlightDisplayLevel(@NotNull Class<? extends GraphQLInspection> inspectionClass,
                                                                        @NotNull PsiElement element) {
    HighlightDisplayKey highlightDisplayKey = getHighlightDisplayKeyByClass(inspectionClass);
    if (highlightDisplayKey == null) {
      LOG.error("Inspection " + inspectionClass + " is not registered");
      return HighlightDisplayLevel.ERROR;
    }
    return getHighlightDisplayLevel(highlightDisplayKey, element);
  }

  private static @NotNull HighlightDisplayLevel getHighlightDisplayLevel(@NotNull HighlightDisplayKey highlightDisplayKey,
                                                                         @NotNull PsiElement element) {
    InspectionProfile inspectionProfile = InspectionProjectProfileManager.getInstance(element.getProject()).getCurrentProfile();
    return inspectionProfile.getErrorLevel(highlightDisplayKey, element);
  }

  private static @Nullable LocalInspectionToolWrapper getInspectionToolWrapper(@NotNull PsiElement element,
                                                                               @NotNull String inspectionId,
                                                                               @NotNull InspectionProfile inspectionProfile) {
    return (LocalInspectionToolWrapper)inspectionProfile.getInspectionTool(inspectionId, element);
  }

  private static boolean isSuppressedInHostLanguage(@NotNull Project project,
                                                    @NotNull PsiFile file,
                                                    @NotNull String toolId) {
    InjectedLanguageManager injectedLanguageManager = InjectedLanguageManager.getInstance(project);
    boolean isInjected = injectedLanguageManager.isInjectedFragment(file);
    if (!isInjected) return false;

    PsiElement context = file.getContext();
    if (context == null) {
      return false;
    }
    for (InspectionSuppressor inspectionSuppressor : LanguageInspectionSuppressors.INSTANCE.allForLanguage(context.getLanguage())) {
      if (inspectionSuppressor.isSuppressedFor(context, toolId)) {
        return true;
      }
    }
    return false;
  }

  public static void createAnnotation(@NotNull AnnotationHolder annotationHolder,
                                      @NotNull PsiElement element,
                                      @NotNull @Nls String message) {
    createAnnotation(annotationHolder, element, message, null, null);
  }

  public static void createAnnotation(@NotNull AnnotationHolder annotationHolder,
                                      @NotNull PsiElement element,
                                      @NotNull @Nls String message,
                                      @Nullable Function<AnnotationBuilder, AnnotationBuilder> builderConsumer) {
    createAnnotation(annotationHolder, element, message, null, builderConsumer);
  }

  public static void createAnnotation(@NotNull AnnotationHolder annotationHolder,
                                      @NotNull PsiElement element,
                                      @NotNull @Nls String message,
                                      @Nullable Class<? extends GraphQLInspection> inspectionClass,
                                      @Nullable Function<AnnotationBuilder, AnnotationBuilder> annotationBuilderProcessor) {
    AnnotationBuilder annotationBuilder = createAnnotationBuilder(annotationHolder, element, message, inspectionClass);
    if (annotationBuilder == null) {
      return;
    }

    if (annotationBuilderProcessor != null) {
      annotationBuilder = annotationBuilderProcessor.apply(annotationBuilder);
    }
    annotationBuilder.create();
  }

  public static @Nullable AnnotationBuilder createAnnotationBuilder(@NotNull AnnotationHolder annotationHolder,
                                                                    @NotNull PsiElement element,
                                                                    @NotNull @Nls String message,
                                                                    @Nullable Class<? extends GraphQLInspection> inspectionClass) {
    HighlightSeverity severity = HighlightSeverity.ERROR;
    List<IntentionAction> fixes = new ArrayList<>();
    ProblemGroup problemGroup = null;

    if (inspectionClass != null) {
      HighlightDisplayKey key = getHighlightDisplayKeyByClass(inspectionClass);
      if (key == null) {
        return null;
      }
      String toolId = key.getShortName();
      PsiFile file = element.getContainingFile();
      Project project = element.getProject();

      InspectionProfileImpl profile = InspectionProjectProfileManager.getInstance(project).getCurrentProfile();
      LocalInspectionToolWrapper inspectionToolWrapper = getInspectionToolWrapper(element, toolId, profile);
      if (inspectionToolWrapper == null || !isToolEnabled(project, key, file)) {
        return null;
      }

      final LocalInspectionTool tool = inspectionToolWrapper.getTool();
      if (SuppressionUtil.inspectionResultSuppressed(element, tool) || isSuppressedInHostLanguage(project, file, toolId)) {
        return null;
      }

      severity = getSeverity(key, element.getContainingFile());
      problemGroup = new GraphQLProblemGroup(toolId);

      String displayName = HighlightDisplayKey.getDisplayNameByKey(key);
      if (displayName != null) {
        fixes.add(new EmptyIntentionAction(displayName));
      }
    }

    AnnotationBuilder builder = annotationHolder.newAnnotation(severity, message);
    if (problemGroup != null) {
      builder = builder.problemGroup(problemGroup);
    }

    for (IntentionAction fix : fixes) {
      builder = builder.withFix(fix);
    }
    return builder;
  }

  public static boolean isEditorInspectionHighlightingDisabled(@NotNull Project project, @NotNull PsiFile file) {
    VirtualFile virtualFile = file.getVirtualFile();
    if (virtualFile != null && GraphQLLibraryManager.getInstance(project).isLibraryRoot(virtualFile)) {
      return true;
    }

    return HighlightingSettingsPerFile.getInstance(project)
             .getHighlightingSettingForRoot(file) != FileHighlightingSetting.FORCE_HIGHLIGHTING;
  }
}
