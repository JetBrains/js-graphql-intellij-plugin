// This is a generated file. Not intended for manual editing.
package com.intellij.lang.jsgraphql.endpoint.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface JSGraphQLEndpointFieldDefinition extends PsiElement {

  @Nullable
  JSGraphQLEndpointAnnotations getAnnotations();

  @Nullable
  JSGraphQLEndpointArgumentsDefinition getArgumentsDefinition();

  @Nullable
  JSGraphQLEndpointCompositeType getCompositeType();

  @NotNull
  JSGraphQLEndpointProperty getProperty();

}
