/*
    The MIT License (MIT)

    Copyright (c) 2015 Andreas Marek and Contributors

    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
    (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
    publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do
    so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
    OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
    LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
    CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.intellij.lang.jsgraphql.types.normalized;


import com.intellij.lang.jsgraphql.types.Assert;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.execution.ConditionalNodes;
import com.intellij.lang.jsgraphql.types.execution.MergedField;
import com.intellij.lang.jsgraphql.types.execution.ValuesResolver;
import com.intellij.lang.jsgraphql.types.introspection.Introspection;
import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.lang.jsgraphql.types.schema.*;

import java.util.*;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.introspection.Introspection.*;


/**
 * Creates a the direct NormalizedFields children, this means it goes only one level deep!
 * This also means the NormalizedFields returned dont have any children.
 */
@Internal
public class FieldCollectorNormalizedQuery {

    private final ConditionalNodes conditionalNodes = new ConditionalNodes();
    private final ValuesResolver valuesResolver = new ValuesResolver();

    public static class CollectFieldResult {
        private final List<NormalizedField> children;
        private final Map<NormalizedField, MergedField> mergedFieldByNormalized;

        public CollectFieldResult(List<NormalizedField> children, Map<NormalizedField, MergedField> mergedFieldByNormalized) {
            this.children = children;
            this.mergedFieldByNormalized = mergedFieldByNormalized;
        }

        public List<NormalizedField> getChildren() {
            return children;
        }

        public Map<NormalizedField, MergedField> getMergedFieldByNormalized() {
            return mergedFieldByNormalized;
        }
    }


    public CollectFieldResult collectFields(FieldCollectorNormalizedQueryParams parameters, NormalizedField normalizedField, MergedField mergedField, int level) {
        GraphQLUnmodifiedType fieldType = GraphQLTypeUtil.unwrapAll(normalizedField.getFieldDefinition().getType());
        // if not composite we don't have any selectionSet because it is a Scalar or enum
        if (!(fieldType instanceof GraphQLCompositeType)) {
            return new CollectFieldResult(Collections.emptyList(), Collections.emptyMap());
        }

        // result key -> ObjectType -> NormalizedField
        Map<String, Map<GraphQLObjectType, NormalizedField>> subFields = new LinkedHashMap<>();
        Map<NormalizedField, MergedField> mergedFieldByNormalizedField = new LinkedHashMap<>();
        Set<GraphQLObjectType> possibleObjects
                = new LinkedHashSet<>(resolvePossibleObjects((GraphQLCompositeType) fieldType, parameters.getGraphQLSchema()));
        for (Field field : mergedField.getFields()) {
            if (field.getSelectionSet() == null) {
                continue;
            }
            this.collectFields(parameters,
                    field.getSelectionSet(),
                    subFields,
                    mergedFieldByNormalizedField,
                    possibleObjects,
                    level,
                    normalizedField);
        }
        List<NormalizedField> children = subFieldsToList(subFields);
        return new CollectFieldResult(children, mergedFieldByNormalizedField);
    }

    public CollectFieldResult collectFromOperation(FieldCollectorNormalizedQueryParams parameters,
                                                   OperationDefinition operationDefinition,
                                                   GraphQLObjectType rootType) {
        Map<String, Map<GraphQLObjectType, NormalizedField>> subFields = new LinkedHashMap<>();
        Map<NormalizedField, MergedField> mergedFieldByNormalizedField = new LinkedHashMap<>();
        Set<GraphQLObjectType> possibleObjects = new LinkedHashSet<>();
        possibleObjects.add(rootType);
        this.collectFields(parameters, operationDefinition.getSelectionSet(), subFields, mergedFieldByNormalizedField, possibleObjects, 1, null);
        List<NormalizedField> children = subFieldsToList(subFields);
        return new CollectFieldResult(children, mergedFieldByNormalizedField);
    }

    private List<NormalizedField> subFieldsToList(Map<String, Map<GraphQLObjectType, NormalizedField>> subFields) {
        List<NormalizedField> children = new ArrayList<>();
        subFields.values().forEach(setMergedFieldWTCMap -> {
            children.addAll(setMergedFieldWTCMap.values());
        });
        return children;
    }

    private void collectFields(FieldCollectorNormalizedQueryParams parameters,
                               SelectionSet selectionSet,
                               Map<String, Map<GraphQLObjectType, NormalizedField>> result,
                               Map<NormalizedField, MergedField> mergedFieldByNormalizedField,
                               Set<GraphQLObjectType> possibleObjects,
                               int level,
                               NormalizedField parent) {

        for (Selection selection : selectionSet.getSelections()) {
            if (selection instanceof Field) {
                collectField(parameters, result, mergedFieldByNormalizedField, (Field) selection, possibleObjects, level, parent);
            } else if (selection instanceof InlineFragment) {
                collectInlineFragment(parameters, result, mergedFieldByNormalizedField, (InlineFragment) selection, possibleObjects, level, parent);
            } else if (selection instanceof FragmentSpread) {
                collectFragmentSpread(parameters, result, mergedFieldByNormalizedField, (FragmentSpread) selection, possibleObjects, level, parent);
            }
        }
    }

    private void collectFragmentSpread(FieldCollectorNormalizedQueryParams parameters,
                                       Map<String, Map<GraphQLObjectType, NormalizedField>> result,
                                       Map<NormalizedField, MergedField> mergedFieldByNormalizedField,
                                       FragmentSpread fragmentSpread,
                                       Set<GraphQLObjectType> possibleObjects,
                                       int level,
                                       NormalizedField parent) {
        if (!conditionalNodes.shouldInclude(parameters.getVariables(), fragmentSpread.getDirectives())) {
            return;
        }
        FragmentDefinition fragmentDefinition = assertNotNull(parameters.getFragmentsByName().get(fragmentSpread.getName()));

        if (!conditionalNodes.shouldInclude(parameters.getVariables(), fragmentDefinition.getDirectives())) {
            return;
        }
        GraphQLCompositeType newCondition = (GraphQLCompositeType) parameters.getGraphQLSchema().getType(fragmentDefinition.getTypeCondition().getName());
        Set<GraphQLObjectType> newConditions = narrowDownPossibleObjects(possibleObjects, newCondition, parameters.getGraphQLSchema());
        collectFields(parameters, fragmentDefinition.getSelectionSet(), result, mergedFieldByNormalizedField, newConditions, level, parent);
    }

    private void collectInlineFragment(FieldCollectorNormalizedQueryParams parameters,
                                       Map<String, Map<GraphQLObjectType, NormalizedField>> result,
                                       Map<NormalizedField, MergedField> mergedFieldByNormalizedField,
                                       InlineFragment inlineFragment,
                                       Set<GraphQLObjectType> possibleObjects,
                                       int level, NormalizedField parent) {
        if (!conditionalNodes.shouldInclude(parameters.getVariables(), inlineFragment.getDirectives())) {
            return;
        }
        Set<GraphQLObjectType> newPossibleObjects = possibleObjects;

        if (inlineFragment.getTypeCondition() != null) {
            GraphQLCompositeType newCondition = (GraphQLCompositeType) parameters.getGraphQLSchema().getType(inlineFragment.getTypeCondition().getName());
            newPossibleObjects = narrowDownPossibleObjects(possibleObjects, newCondition, parameters.getGraphQLSchema());

        }
        collectFields(parameters, inlineFragment.getSelectionSet(), result, mergedFieldByNormalizedField, newPossibleObjects, level, parent);
    }

    private void collectField(FieldCollectorNormalizedQueryParams parameters,
                              Map<String, Map<GraphQLObjectType, NormalizedField>> result,
                              Map<NormalizedField, MergedField> mergedFieldByNormalizedField,
                              Field field,
                              Set<GraphQLObjectType> objectTypes,
                              int level,
                              NormalizedField parent) {
        if (!conditionalNodes.shouldInclude(parameters.getVariables(), field.getDirectives())) {
            return;
        }
        String name = field.getResultKey();
        result.computeIfAbsent(name, ignored -> new LinkedHashMap<>());
        Map<GraphQLObjectType, NormalizedField> existingFieldWTC = result.get(name);

        for (GraphQLObjectType objectType : objectTypes) {

            if (existingFieldWTC.containsKey(objectType)) {
                NormalizedField normalizedField = existingFieldWTC.get(objectType);

                MergedField mergedField1 = mergedFieldByNormalizedField.get(normalizedField);
                MergedField updatedMergedField = mergedField1.transform(builder -> builder.addField(field));
                mergedFieldByNormalizedField.put(normalizedField, updatedMergedField);

            } else {
                GraphQLFieldDefinition fieldDefinition;
                if (field.getName().equals(TypeNameMetaFieldDef.getName())) {
                    fieldDefinition = TypeNameMetaFieldDef;
                } else if (field.getName().equals(Introspection.SchemaMetaFieldDef.getName())) {
                    fieldDefinition = SchemaMetaFieldDef;
                } else if (field.getName().equals(Introspection.TypeMetaFieldDef.getName())) {
                    fieldDefinition = TypeMetaFieldDef;
                } else {
                    fieldDefinition = assertNotNull(objectType.getFieldDefinition(field.getName()), () -> String.format("no field with name %s found in object %s", field.getName(), objectType.getName()));
                }

                Map<String, Object> argumentValues = valuesResolver.getArgumentValues(fieldDefinition.getArguments(), field.getArguments(), parameters.getVariables());
                NormalizedField newFieldWTC = NormalizedField.newQueryExecutionField()
                        .alias(field.getAlias())
                        .arguments(argumentValues)
                        .objectType(objectType)
                        .fieldDefinition(fieldDefinition)
                        .level(level)
                        .parent(parent)
                        .build();
                existingFieldWTC.put(objectType, newFieldWTC);
                mergedFieldByNormalizedField.put(newFieldWTC, MergedField.newMergedField(field).build());
            }
        }
    }

    private Set<GraphQLObjectType> narrowDownPossibleObjects(Set<GraphQLObjectType> currentOnes,
                                                             GraphQLCompositeType typeCondition,
                                                             GraphQLSchema graphQLSchema) {

        List<GraphQLObjectType> resolvedTypeCondition = resolvePossibleObjects(typeCondition, graphQLSchema);
        if (currentOnes.size() == 0) {
            return new LinkedHashSet<>(resolvedTypeCondition);
        }

        Set<GraphQLObjectType> result = new LinkedHashSet<>(currentOnes);
        result.retainAll(resolvedTypeCondition);
        return result;
    }

    private List<GraphQLObjectType> resolvePossibleObjects(GraphQLCompositeType type, GraphQLSchema graphQLSchema) {
        if (type instanceof GraphQLObjectType) {
            return Collections.singletonList((GraphQLObjectType) type);
        } else if (type instanceof GraphQLInterfaceType) {
            return graphQLSchema.getImplementations((GraphQLInterfaceType) type);
        } else if (type instanceof GraphQLUnionType) {
            List types = ((GraphQLUnionType) type).getTypes();
            return new ArrayList<GraphQLObjectType>(types);
        } else {
            return Assert.assertShouldNeverHappen();
        }

    }

}
