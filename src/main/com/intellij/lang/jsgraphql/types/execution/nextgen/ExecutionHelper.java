package com.intellij.lang.jsgraphql.types.execution.nextgen;

import com.intellij.lang.jsgraphql.types.ExecutionInput;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.execution.*;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.InstrumentationState;
import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.lang.jsgraphql.types.schema.GraphQLObjectType;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;

import java.util.List;
import java.util.Map;

import static com.intellij.lang.jsgraphql.types.execution.ExecutionContextBuilder.newExecutionContextBuilder;
import static com.intellij.lang.jsgraphql.types.execution.ExecutionStepInfo.newExecutionStepInfo;

@Internal
public class ExecutionHelper {

    private final FieldCollector fieldCollector = new FieldCollector();

    public static class ExecutionData {
        public ExecutionContext executionContext;
    }

    public ExecutionData createExecutionData(Document document,
                                             GraphQLSchema graphQLSchema,
                                             ExecutionId executionId,
                                             ExecutionInput executionInput,
                                             InstrumentationState instrumentationState) {

        NodeUtil.GetOperationResult getOperationResult = NodeUtil.getOperation(document, executionInput.getOperationName());
        Map<String, FragmentDefinition> fragmentsByName = getOperationResult.fragmentsByName;
        OperationDefinition operationDefinition = getOperationResult.operationDefinition;

        ValuesResolver valuesResolver = new ValuesResolver();
        Map<String, Object> inputVariables = executionInput.getVariables();
        List<VariableDefinition> variableDefinitions = operationDefinition.getVariableDefinitions();

        Map<String, Object> coercedVariables;
        coercedVariables = valuesResolver.coerceVariableValues(graphQLSchema, variableDefinitions, inputVariables);

        ExecutionContext executionContext = newExecutionContextBuilder()
                .executionId(executionId)
                .instrumentationState(instrumentationState)
                .graphQLSchema(graphQLSchema)
                .context(executionInput.getContext())
                .root(executionInput.getRoot())
                .fragmentsByName(fragmentsByName)
                .variables(coercedVariables)
                .document(document)
                .operationDefinition(operationDefinition)
                .build();

        ExecutionData executionData = new ExecutionData();
        executionData.executionContext = executionContext;
        return executionData;

    }

    public FieldSubSelection getFieldSubSelection(ExecutionContext executionContext) {
        OperationDefinition operationDefinition = executionContext.getOperationDefinition();
        GraphQLObjectType operationRootType = Common.getOperationRootType(executionContext.getGraphQLSchema(), operationDefinition);

        FieldCollectorParameters collectorParameters = FieldCollectorParameters.newParameters()
                .schema(executionContext.getGraphQLSchema())
                .objectType(operationRootType)
                .fragments(executionContext.getFragmentsByName())
                .variables(executionContext.getVariables())
                .build();

        MergedSelectionSet mergedSelectionSet = fieldCollector.collectFields(collectorParameters, operationDefinition.getSelectionSet());
        ExecutionStepInfo executionInfo = newExecutionStepInfo().type(operationRootType).path(ResultPath.rootPath()).build();

        FieldSubSelection fieldSubSelection = FieldSubSelection.newFieldSubSelection()
                .source(executionContext.getRoot())
                .localContext(executionContext.getLocalContext())
                .mergedSelectionSet(mergedSelectionSet)
                .executionInfo(executionInfo)
                .build();
        return fieldSubSelection;

    }
}
