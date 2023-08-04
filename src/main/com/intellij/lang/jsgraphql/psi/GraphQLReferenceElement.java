package com.intellij.lang.jsgraphql.psi;

import org.jetbrains.annotations.Nullable;

public interface GraphQLReferenceElement extends GraphQLElement {

  @Nullable
  String getReferenceName();
}
