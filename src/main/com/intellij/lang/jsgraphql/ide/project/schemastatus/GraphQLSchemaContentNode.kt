/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.schemastatus;

import com.google.common.collect.Lists;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.ide.util.gotoByName.ChooseByNamePopupComponent;
import com.intellij.ide.util.gotoByName.SimpleChooseByNameModel;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaInfo;
import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.lang.jsgraphql.types.schema.idl.ScalarInfo;
import com.intellij.lang.jsgraphql.types.schema.idl.TypeDefinitionRegistry;
import com.intellij.openapi.application.ModalityState;
import com.intellij.psi.PsiElement;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.CachingSimpleNode;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.util.List;
import java.util.Optional;

/**
 * Tree node which provides schema statistics
 */
public class GraphQLSchemaContentNode extends CachingSimpleNode {

    private final GraphQLSchemaInfo myValidatedSchema;

    public GraphQLSchemaContentNode(@NotNull SimpleNode parent, @NotNull GraphQLSchemaInfo validatedSchema) {
        super(parent);
        myValidatedSchema = validatedSchema;

        final List<String> parts = Lists.newArrayList();
        TypeDefinitionRegistry registry = validatedSchema.getRegistryInfo().getTypeDefinitionRegistry();
        parts.add(registry.getTypes(ObjectTypeDefinition.class).size() + " types");
        parts.add(registry.getTypes(InterfaceTypeDefinition.class).size() + " interfaces");
        parts.add(registry.getTypes(InputObjectTypeDefinition.class).size() + " inputs");
        parts.add(registry.getTypes(EnumTypeDefinition.class).size() + " enums");
        parts.add(registry.getTypes(UnionTypeDefinition.class).size() + " unions");
        parts.add(registry.scalars().size() - ScalarInfo.GRAPHQL_SPECIFICATION_SCALARS.size() + " scalars");
        parts.add(registry.getDirectiveDefinitions().size() + " directives");

        myName = "Schema discovery summary";

        final Object[] nonEmptyParts = parts.stream().filter(p -> p.charAt(0) != '0').toArray();
        if (nonEmptyParts.length > 0) {
            getTemplatePresentation().setLocationString("- " + StringUtils.join(nonEmptyParts, ", "));
        } else {
            final String message = validatedSchema.getRegistryInfo().isProcessedGraphQL() ? "- schema is empty" : "- no schema definitions were found";
            getTemplatePresentation().setLocationString(message);
        }

        getTemplatePresentation().setTooltip("Double click or press enter to search the schema registry");

        setIcon(AllIcons.Nodes.ModuleGroup);

    }

    @Override
    public void handleDoubleClickOrEnter(SimpleTree tree, InputEvent inputEvent) {
        ChooseByNamePopup popup = ChooseByNamePopup.createPopup(myProject,
            new SimpleChooseByNameModel(myProject, "Search schema registry \"" + getParent().getName() + "\"", null) {
                @Override
                public String[] getNames() {
                    final List<String> names = Lists.newArrayList();
                    myValidatedSchema.getRegistryInfo().getTypeDefinitionRegistry().types().values().forEach(
                        type -> names.add(type.getName()));
                    myValidatedSchema.getRegistryInfo().getTypeDefinitionRegistry().scalars().values().forEach(
                        type -> names.add(type.getName()));
                    myValidatedSchema.getRegistryInfo().getTypeDefinitionRegistry().getDirectiveDefinitions().values().forEach(
                        type -> names.add(type.getName()));
                    return names.toArray(new String[]{});
                }

                @Override
                protected Object[] getElementsByName(String name, String pattern) {
                    final TypeDefinitionRegistry registry = myValidatedSchema.getRegistryInfo().getTypeDefinitionRegistry();
                    Optional<TypeDefinition> type = registry.getType(name);
                    if (type.isPresent()) {
                        return new Object[]{type.get()};
                    }
                    final ScalarTypeDefinition scalarTypeDefinition = registry.scalars().get(name);
                    if (scalarTypeDefinition != null) {
                        return new Object[]{scalarTypeDefinition};
                    }
                    final Optional<DirectiveDefinition> directiveDefinition = registry.getDirectiveDefinition(name);
                    if (directiveDefinition.isPresent()) {
                        return new Object[]{directiveDefinition.get()};
                    }
                    return new Object[]{name};
                }

                @Override
                public ListCellRenderer getListCellRenderer() {
                    return new ColoredListCellRenderer() {

                        @Override
                        protected void customizeCellRenderer(@NotNull JList list,
                                                             Object value,
                                                             int index,
                                                             boolean selected,
                                                             boolean hasFocus) {
                            String elementName = getElementName(value);
                            if (elementName != null) {
                                this.append(elementName);
                                if (value instanceof AbstractNode) {
                                    final SourceLocation sourceLocation = ((AbstractNode) value).getSourceLocation();
                                    if (sourceLocation != null) {
                                        String sourceName = sourceLocation.getSourceName();
                                        if (sourceName != null) {
                                            sourceName = StringUtils.substringAfterLast(sourceName, "/");
                                            this.append(" - " + sourceName, SimpleTextAttributes.GRAYED_ATTRIBUTES);
                                        }
                                    }
                                }
                            }
                        }
                    };
                }

                @Nullable
                @Override
                public String getElementName(Object element) {
                    return element instanceof NamedNode ? ((NamedNode) element).getName() : null;
                }
            }, (PsiElement) null);
        popup.invoke(new ChooseByNamePopupComponent.Callback() {
            @Override
            public void elementChosen(Object element) {
                if (element instanceof AbstractNode) {
                    final SourceLocation sourceLocation = ((AbstractNode) element).getSourceLocation();
                    if (sourceLocation != null && sourceLocation.getSourceName() != null) {
                        GraphQLTreeNodeNavigationUtil.openSourceLocation(myProject, sourceLocation, true);
                    }
                }
            }
        }, ModalityState.NON_MODAL, false);

    }

    @Override
    public SimpleNode[] buildChildren() {
        return SimpleNode.NO_CHILDREN;
    }

    @Override
    public boolean isAlwaysLeaf() {
        return true;
    }


}
