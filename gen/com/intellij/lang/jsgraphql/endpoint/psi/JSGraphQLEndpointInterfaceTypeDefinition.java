// This is a generated file. Not intended for manual editing.
package com.intellij.lang.jsgraphql.endpoint.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface JSGraphQLEndpointInterfaceTypeDefinition extends JSGraphQLEndpointNamedTypeDefinition {

  @NotNull
  List<JSGraphQLEndpointAnnotation> getAnnotationList();

  @Nullable
  JSGraphQLEndpointFieldDefinitionSet getFieldDefinitionSet();

  @Nullable
  JSGraphQLEndpointNamedTypeDef getNamedTypeDef();

}
