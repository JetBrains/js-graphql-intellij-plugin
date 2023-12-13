package com.intellij.lang.jsgraphql.ide.validation.inspections;

import com.intellij.codeInspection.ex.UnfairLocalInspectionTool;

public final class GraphQLUnresolvedReferenceInspection extends GraphQLInspection implements UnfairLocalInspectionTool {
  public static final String SHORT_NAME = GraphQLInspection.getInspectionShortName(GraphQLUnresolvedReferenceInspection.class);
}
