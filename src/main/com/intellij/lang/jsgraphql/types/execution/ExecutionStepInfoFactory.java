package com.intellij.lang.jsgraphql.types.execution;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.introspection.Introspection;
import com.intellij.lang.jsgraphql.types.language.Argument;
import com.intellij.lang.jsgraphql.types.schema.*;

import java.util.List;
import java.util.Map;

@Internal
public class ExecutionStepInfoFactory {


    ValuesResolver valuesResolver = new ValuesResolver();


    public ExecutionStepInfo newExecutionStepInfoForSubField(ExecutionContext executionContext, MergedField mergedField, ExecutionStepInfo parentInfo) {
        GraphQLObjectType parentType = (GraphQLObjectType) parentInfo.getUnwrappedNonNullType();
        GraphQLFieldDefinition fieldDefinition = Introspection.getFieldDef(executionContext.getGraphQLSchema(), parentType, mergedField.getName());
        GraphQLOutputType fieldType = fieldDefinition.getType();
        List<Argument> fieldArgs = mergedField.getArguments();
        GraphQLCodeRegistry codeRegistry = executionContext.getGraphQLSchema().getCodeRegistry();
        Map<String, Object> argumentValues = valuesResolver.getArgumentValues(codeRegistry, fieldDefinition.getArguments(), fieldArgs, executionContext.getVariables());

        ResultPath newPath = parentInfo.getPath().segment(mergedField.getResultKey());

        return parentInfo.transform(builder -> builder
                .parentInfo(parentInfo)
                .type(fieldType)
                .fieldDefinition(fieldDefinition)
                .fieldContainer(parentType)
                .field(mergedField)
                .path(newPath)
                .arguments(argumentValues));
    }

    public ExecutionStepInfo newExecutionStepInfoForListElement(ExecutionStepInfo executionInfo, int index) {
        GraphQLList fieldType = (GraphQLList) executionInfo.getUnwrappedNonNullType();
        GraphQLOutputType typeInList = (GraphQLOutputType) fieldType.getWrappedType();
        ResultPath indexedPath = executionInfo.getPath().segment(index);
        return executionInfo.transform(builder -> builder
                .parentInfo(executionInfo)
                .type(typeInList)
                .path(indexedPath));
    }

}
