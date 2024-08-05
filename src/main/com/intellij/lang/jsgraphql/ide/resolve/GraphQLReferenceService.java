/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.resolve;

import com.google.common.collect.Maps;
import com.intellij.lang.jsgraphql.ide.search.GraphQLPsiSearchHelper;
import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLDirectiveImpl;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLFieldImpl;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLReferenceMixin;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaUtil;
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryTypes;
import com.intellij.lang.jsgraphql.types.schema.GraphQLType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.impl.AnyPsiChangeListener;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public class GraphQLReferenceService implements Disposable {

  private final Map<String, PsiReference> logicalTypeNameToReference = Maps.newConcurrentMap();
  private final GraphQLPsiSearchHelper myPsiSearchHelper;

  /**
   * Sentinel reference for use in concurrent maps which don't allow nulls
   */
  private static final PsiReference NULL_REFERENCE = new PsiReferenceBase<PsiElement>(
    new LeafPsiElement(GraphQLElementTypes.TYPE, "type"), true
  ) {
    @Override
    public @Nullable PsiElement resolve() {
      return null;
    }

    @Override
    public Object @NotNull [] getVariants() {
      return PsiReference.EMPTY_ARRAY;
    }
  };

  public static GraphQLReferenceService getService(@NotNull Project project) {
    return project.getService(GraphQLReferenceService.class);
  }

  public GraphQLReferenceService(final @NotNull Project project) {
    myPsiSearchHelper = GraphQLPsiSearchHelper.getInstance(project);

    project.getMessageBus().connect(this).subscribe(PsiManagerImpl.ANY_PSI_CHANGE_TOPIC, new AnyPsiChangeListener() {
      @Override
      public void beforePsiChanged(boolean isPhysical) {
        // clear the cache on each PSI change
        logicalTypeNameToReference.clear();
      }
    });
  }

  public @Nullable PsiElement resolveReference(@NotNull GraphQLReferenceMixin element) {
    PsiReference reference = innerResolveReference(element);
    return reference != null ? reference.resolve() : null;
  }

  private @Nullable PsiReference innerResolveReference(GraphQLReferenceMixin element) {
    if (element != null) {
      final PsiElement parent = element.getParent();
      if (parent instanceof GraphQLField) {
        return resolveFieldReference(element, (GraphQLField)parent);
      }
      if (parent instanceof GraphQLFragmentSpread) {
        return resolveFragmentDefinition(element);
      }
      if (parent instanceof GraphQLArgument) {
        return resolveArgument(element);
      }
      if (parent instanceof GraphQLObjectField) {
        return resolveObjectField(element, (GraphQLObjectField)parent);
      }
      if (parent instanceof GraphQLTypeName) {
        return resolveTypeName(element);
      }
      if (parent instanceof GraphQLEnumValue) {
        return resolveEnumValue(element);
      }
      if (parent instanceof GraphQLDirective) {
        return resolveDirective(element);
      }
      if (parent instanceof GraphQLFieldDefinition) {
        return resolveFieldDefinition(element, (GraphQLFieldDefinition)parent);
      }
    }
    return null;
  }

  private @Nullable PsiReference resolveFieldDefinition(@NotNull GraphQLReferenceMixin element, @NotNull GraphQLFieldDefinition fieldDefinition) {
    final String name = fieldDefinition.getName();
    if (name != null) {
      final GraphQLTypeSystemDefinition typeSystemDefinition =
        PsiTreeUtil.getParentOfType(element, GraphQLTypeSystemDefinition.class);
      if (typeSystemDefinition != null) {
        final GraphQLImplementsInterfaces implementsInterfaces =
          PsiTreeUtil.findChildOfType(typeSystemDefinition, GraphQLImplementsInterfaces.class);
        if (implementsInterfaces != null) {
          for (GraphQLTypeName implementedType : implementsInterfaces.getTypeNameList()) {
            final PsiReference typeReference = implementedType.getNameIdentifier().getReference();
            if (typeReference != null) {
              final PsiElement interfaceIdentifier = typeReference.resolve();
              if (interfaceIdentifier != null) {
                final GraphQLTypeSystemDefinition interfaceDefinition =
                  PsiTreeUtil.getParentOfType(interfaceIdentifier, GraphQLTypeSystemDefinition.class);
                final Collection<GraphQLFieldDefinition> interfaceFieldDefinitions =
                  PsiTreeUtil.findChildrenOfType(interfaceDefinition, GraphQLFieldDefinition.class);
                for (GraphQLFieldDefinition interfaceFieldDefinition : interfaceFieldDefinitions) {
                  if (name.equals(interfaceFieldDefinition.getName())) {
                    return createReference(element, interfaceFieldDefinition.getNameIdentifier());
                  }
                }
              }
            }
          }
        }
      }
    }
    return null;
  }

  private @Nullable PsiReference resolveArgument(@NotNull GraphQLReferenceMixin element) {
    final String name = element.getName();
    if (name != null) {
      final GraphQLDirectiveImpl directive = PsiTreeUtil.getParentOfType(element, GraphQLDirectiveImpl.class);
      // directive argument
      if (directive != null) {
        final GraphQLIdentifier directiveNameIdentifier = directive.getNameIdentifier();
        if (directiveNameIdentifier != null) {
          final PsiReference directiveReference = directiveNameIdentifier.getReference();
          if (directiveReference != null) {
            final PsiElement directiveIdentifier = directiveReference.resolve();
            if (directiveIdentifier != null) {
              final GraphQLArgumentsDefinition arguments =
                PsiTreeUtil.getNextSiblingOfType(directiveIdentifier, GraphQLArgumentsDefinition.class);
              if (arguments != null) {
                for (GraphQLInputValueDefinition inputValueDefinition : arguments.getInputValueDefinitionList()) {
                  if (name.equals(inputValueDefinition.getName())) {
                    return createInputValueDefinitionReference(element, inputValueDefinition);
                  }
                }
              }
            }
          }
        }
        return null;
      }
      // field argument
      final GraphQLFieldImpl field = PsiTreeUtil.getParentOfType(element, GraphQLFieldImpl.class);
      if (field != null) {
        final PsiReference fieldPsiReference = field.getNameIdentifier().getReference();
        if (fieldPsiReference != null) {
          final PsiElement resolvedPsiReference = fieldPsiReference.resolve();
          if (resolvedPsiReference != null) {
            if (resolvedPsiReference.getParent() instanceof GraphQLFieldDefinition fieldDefinition) {
              final GraphQLArgumentsDefinition argumentsDefinition = fieldDefinition.getArgumentsDefinition();
              if (argumentsDefinition != null) {
                for (GraphQLInputValueDefinition inputValueDefinition : argumentsDefinition.getInputValueDefinitionList()) {
                  if (name.equals(inputValueDefinition.getName())) {
                    return createInputValueDefinitionReference(element, inputValueDefinition);
                  }
                }
              }
            }
          }
        }
      }
    }
    return null;
  }

  @NotNull
  PsiReference createInputValueDefinitionReference(@NotNull GraphQLReferenceMixin element,
                                                   @NotNull GraphQLInputValueDefinition inputValueDefinition) {
    return createReference(element, inputValueDefinition.getNameIdentifier());
  }

  @Nullable
  PsiReference resolveFieldReference(@NotNull GraphQLReferenceMixin element, @NotNull GraphQLField field) {
    final String name = element.getName();
    Ref<PsiReference> reference = new Ref<>();
    if (name != null) {
      if (name.startsWith("__")) {
        // __typename or introspection fields __schema and __type which implicitly extends the query root type
        GraphQLResolveUtil.processFilesInLibrary(GraphQLLibraryTypes.SPECIFICATION, element, file -> {
          file.accept(new GraphQLRecursiveVisitor() {
            @Override
            public void visitElement(@NotNull GraphQLElement schemaElement) {
              // TODO: [vepanimas] rework to use com.intellij.psi.PsiElement.processDeclarations or something similar,
              //  now it traverses the whole tree including comments, punctuation, arguments and so on,
              //  but we actually expect only field declarations in two predefined object types
              if (schemaElement instanceof GraphQLReferenceElement &&
                  Objects.equals(((GraphQLReferenceElement)schemaElement).getReferenceName(), name)) {
                reference.set(createReference(element, schemaElement));
                stopWalking();
                return;
              }
              super.visitElement(schemaElement);
            }
          });
          return reference.isNull();
        });
      }
      final GraphQLTypeScopeProvider typeScopeProvider = PsiTreeUtil.getParentOfType(field, GraphQLTypeScopeProvider.class);
      if (reference.isNull() && typeScopeProvider != null) {
        GraphQLType typeScope = typeScopeProvider.getTypeScope();
        if (typeScope != null) {
          final GraphQLType fieldType = GraphQLSchemaUtil.getUnmodifiedType(typeScope);
          myPsiSearchHelper.processNamedElements(element, name, psiNamedElement -> {
            if (psiNamedElement.getParent() instanceof GraphQLFieldDefinition fieldDefinition) {
              if (!Objects.equals(fieldDefinition.getName(), name)) {
                // field name doesn't match, keep looking
                return true;
              }
              boolean isTypeMatch = false;
              final GraphQLTypeDefinition typeDefinition =
                PsiTreeUtil.getParentOfType(psiNamedElement, GraphQLTypeDefinition.class);
              if (typeDefinition != null) {
                final GraphQLTypeNameDefinition typeNameDefinition =
                  PsiTreeUtil.findChildOfType(typeDefinition, GraphQLTypeNameDefinition.class);
                isTypeMatch = typeNameDefinition != null &&
                              GraphQLSchemaUtil.getTypeName(fieldType).equals(typeNameDefinition.getName());
              }
              if (!isTypeMatch) {
                // check type extension
                final GraphQLTypeExtension typeExtension =
                  PsiTreeUtil.getParentOfType(psiNamedElement, GraphQLTypeExtension.class);
                if (typeExtension != null) {
                  final GraphQLTypeName typeName = PsiTreeUtil.findChildOfType(typeExtension, GraphQLTypeName.class);
                  isTypeMatch = typeName != null && GraphQLSchemaUtil.getTypeName(fieldType).equals(typeName.getName());
                }
              }
              if (isTypeMatch) {
                reference.set(createReference(element, psiNamedElement));
                return false; // done searching
              }
            }
            return true;
          });
        }
      }
    }
    return reference.get();
  }

  private @NotNull PsiReferenceBase<GraphQLReferenceMixin> createReference(@NotNull GraphQLReferenceMixin fromElement,
                                                                           @NotNull PsiElement resolvedElement) {
    return new PsiReferenceBase<>(fromElement, TextRange.from(0, fromElement.getTextLength())) {
      @Override
      public PsiElement resolve() {
        return resolvedElement;
      }

      @Override
      public Object @NotNull [] getVariants() {
        return PsiReference.EMPTY_ARRAY;
      }
    };
  }


  @Nullable
  PsiReference resolveTypeName(@NotNull GraphQLReferenceMixin element) {
    final String logicalTypeName = GraphQLPsiUtil.getPhysicalFileName(element.getContainingFile()) + ":" + element.getName();
    // intentionally not using computeIfAbsent here to avoid locking during long-running write actions
    // it's better to compute multiple times in certain rare cases than blocking
    // NOTE: concurrent hash map doesn't allow nulls, so using the NULL_REFERENCE sentinel value to avoid re-computation of unresolvable references
    PsiReference psiReference = logicalTypeNameToReference.get(logicalTypeName);
    if (psiReference == null) {
      psiReference = resolveUsingIndex(element,
                                       psiNamedElement -> psiNamedElement instanceof GraphQLIdentifier &&
                                                          psiNamedElement.getParent() instanceof GraphQLTypeNameDefinition);
      // use sentinel to avoid nulls
      logicalTypeNameToReference.putIfAbsent(logicalTypeName, psiReference != null ? psiReference : NULL_REFERENCE);
    }
    return psiReference != NULL_REFERENCE ? psiReference : null;
  }

  @Nullable
  PsiReference resolveFragmentDefinition(@NotNull GraphQLReferenceMixin element) {
    return resolveUsingIndex(element,
                             psiNamedElement -> psiNamedElement instanceof GraphQLIdentifier &&
                                                psiNamedElement.getParent() instanceof GraphQLFragmentDefinition
    );
  }

  private @Nullable PsiReference resolveObjectField(@NotNull GraphQLReferenceMixin element, @NotNull GraphQLObjectField field) {
    final String name = element.getName();
    if (name != null) {
      final GraphQLTypeScopeProvider fieldTypeScopeProvider = PsiTreeUtil.getParentOfType(field, GraphQLTypeScopeProvider.class);
      if (fieldTypeScopeProvider != null) {
        GraphQLType typeScope = fieldTypeScopeProvider.getTypeScope();
        if (typeScope != null) {
          final String namedTypeScope = GraphQLSchemaUtil.getUnmodifiedType(typeScope).getName();
          return resolveUsingIndex(element, psiNamedElement -> {
            if (psiNamedElement.getParent() instanceof GraphQLInputValueDefinition) {
              final GraphQLInputObjectTypeDefinition inputTypeDefinition =
                PsiTreeUtil.getParentOfType(psiNamedElement, GraphQLInputObjectTypeDefinition.class);
              if (inputTypeDefinition != null && inputTypeDefinition.getTypeNameDefinition() != null) {
                return namedTypeScope.equals(inputTypeDefinition.getTypeNameDefinition().getName());
              }
            }
            return false;
          });
        }
      }
    }
    return null;
  }

  private @Nullable PsiReference resolveEnumValue(@NotNull GraphQLReferenceMixin element) {
    final String name = element.getName();
    if (name != null) {
      final GraphQLTypeScopeProvider enumTypeScopeProvider = PsiTreeUtil.getParentOfType(element, GraphQLTypeScopeProvider.class);
      if (enumTypeScopeProvider != null) {
        GraphQLType typeScope = enumTypeScopeProvider.getTypeScope();
        if (typeScope != null) {
          final String namedTypeScope = GraphQLSchemaUtil.getUnmodifiedType(typeScope).getName();
          return resolveUsingIndex(element, psiNamedElement -> {
            if (psiNamedElement.getParent() instanceof GraphQLEnumValue) {
              final GraphQLEnumTypeDefinition enumTypeDefinition =
                PsiTreeUtil.getParentOfType(psiNamedElement, GraphQLEnumTypeDefinition.class);
              if (enumTypeDefinition != null && enumTypeDefinition.getTypeNameDefinition() != null) {
                return namedTypeScope.equals(enumTypeDefinition.getTypeNameDefinition().getName());
              }
            }
            return false;
          });
        }
      }
    }
    return null;
  }

  private @Nullable PsiReference resolveDirective(GraphQLReferenceMixin element) {
    return resolveUsingIndex(element, psiNamedElement ->
      psiNamedElement instanceof GraphQLIdentifier && psiNamedElement.getParent() instanceof GraphQLDirectiveDefinition);
  }

  private @Nullable PsiReference resolveUsingIndex(@NotNull GraphQLReferenceMixin element,
                                                   @NotNull Predicate<? super PsiNamedElement> predicate) {
    final String name = element.getName();
    Ref<PsiReference> reference = new Ref<>();
    if (name != null) {
      myPsiSearchHelper.processNamedElements(element, name, psiNamedElement -> {
        ProgressManager.checkCanceled();
        if (predicate.test(psiNamedElement)) {
          reference.set(new PsiReferenceBase<PsiNamedElement>(element, TextRange.from(0, element.getTextLength())) {
            @Override
            public @Nullable PsiElement resolve() {
              return psiNamedElement;
            }

            @Override
            public @NotNull Object @NotNull [] getVariants() {
              return PsiReference.EMPTY_ARRAY;
            }
          });
          return false; // done searching
        }
        return true;
      });
    }
    return reference.get();
  }

  @Override
  public void dispose() {
  }
}
