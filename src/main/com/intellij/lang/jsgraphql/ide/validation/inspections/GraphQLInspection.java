package com.intellij.lang.jsgraphql.ide.validation.inspections;

import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeInspection.InspectionProfile;
import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ex.InspectionProfileImpl;
import com.intellij.codeInspection.ex.ToolsImpl;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.annotation.ProblemGroup;
import com.intellij.openapi.project.Project;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GraphQLInspection extends LocalInspectionTool {
    public static boolean isToolEnabled(@NotNull Project project,
                                        @NotNull Class<? extends GraphQLInspection> inspectionClass,
                                        @Nullable PsiFile file) {
        InspectionProfileImpl inspectionProfile = InspectionProjectProfileManager.getInstance(project).getCurrentProfile();
        HighlightDisplayKey key = getHighlightDisplayKeyByClass(inspectionClass);
        ToolsImpl tools = inspectionProfile.getToolsOrNull(key.toString(), project);
        return tools != null && tools.isEnabled(file);
    }


    public static @NotNull HighlightDisplayKey getHighlightDisplayKeyByClass(@NotNull Class<? extends GraphQLInspection> inspectionClass) {
        String shortName = InspectionProfileEntry.getShortName(inspectionClass.getSimpleName());
        HighlightDisplayKey key = HighlightDisplayKey.find(shortName);
        if (key == null) {
            key = new HighlightDisplayKey(shortName, shortName);
        }
        return key;
    }


    public static @NotNull HighlightSeverity getSeverityForError(@NotNull Class<? extends GraphQLInspection> inspectionClass,
                                                                 @NotNull PsiFile file) {
        InspectionProfile inspectionProfile = InspectionProjectProfileManager.getInstance(file.getProject()).getCurrentProfile();
        HighlightDisplayKey highlightDisplayKey = getHighlightDisplayKeyByClass(inspectionClass);
        return inspectionProfile.getErrorLevel(highlightDisplayKey, file).getSeverity();
    }

    public static @NotNull AnnotationBuilder createAnnotationBuilder(@NotNull AnnotationHolder annotationHolder,
                                                                     @NotNull PsiElement element,
                                                                     @NotNull String message,
                                                                     @Nullable Class<? extends GraphQLInspection> inspectionClass) {
        HighlightSeverity severity = HighlightSeverity.ERROR;
        ProblemGroup problemGroup = null;

        if (inspectionClass != null) {
            severity = GraphQLInspection.getSeverityForError(inspectionClass, element.getContainingFile());
            problemGroup = () -> GraphQLInspection.getHighlightDisplayKeyByClass(inspectionClass).toString();
        }

        AnnotationBuilder builder = annotationHolder.newAnnotation(severity, message);
        if (problemGroup != null) {
            builder = builder.problemGroup(problemGroup);
        }
        return builder;
    }
}
