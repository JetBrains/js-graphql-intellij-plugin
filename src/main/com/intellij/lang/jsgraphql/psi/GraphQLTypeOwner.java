package com.intellij.lang.jsgraphql.psi;

import org.jetbrains.annotations.Nullable;

/**
 * An entity that has an explicit type declaration assigned to it, e.g. {@code filter: UserFilter}.
 */
public interface GraphQLTypeOwner extends GraphQLElement {
  @Nullable
  GraphQLType getType();
}
