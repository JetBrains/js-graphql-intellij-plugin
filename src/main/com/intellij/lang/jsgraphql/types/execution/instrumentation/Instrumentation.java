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
package com.intellij.lang.jsgraphql.types.execution.instrumentation;

import com.intellij.lang.jsgraphql.types.ExecutionInput;
import com.intellij.lang.jsgraphql.types.ExecutionResult;
import com.intellij.lang.jsgraphql.types.PublicSpi;
import com.intellij.lang.jsgraphql.types.execution.ExecutionContext;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.parameters.*;
import com.intellij.lang.jsgraphql.types.language.Document;
import com.intellij.lang.jsgraphql.types.schema.DataFetcher;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.validation.ValidationError;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.intellij.lang.jsgraphql.types.execution.instrumentation.SimpleInstrumentationContext.noOp;

/**
 * Provides the capability to instrument the execution steps of a GraphQL query.
 * <p>
 * For example you might want to track which fields are taking the most time to fetch from the backing database
 * or log what fields are being asked for.
 * <p>
 * Remember that graphql calls can cross threads so make sure you think about the thread safety of any instrumentation
 * code when you are writing it.
 * <p>
 * Each step gives back an {@link graphql.execution.instrumentation.InstrumentationContext} object.  This has two callbacks on it,
 * one for the step is `dispatched` and one for when the step has `completed`.  This is done because many of the "steps" are asynchronous
 * operations such as fetching data and resolving it into objects.
 */
@PublicSpi
public interface Instrumentation {

  /**
   * This will be called just before execution to create an object that is given back to all instrumentation methods
   * to allow them to have per execution request state
   *
   * @return a state object that is passed to each method
   */
  default InstrumentationState createState() {
    return null;
  }

  /**
   * This will be called just before execution to create an object that is given back to all instrumentation methods
   * to allow them to have per execution request state
   *
   * @param parameters the parameters to this step
   * @return a state object that is passed to each method
   */
  default InstrumentationState createState(InstrumentationCreateStateParameters parameters) {
    return createState();
  }

  /**
   * This is called right at the start of query execution and its the first step in the instrumentation chain.
   *
   * @param parameters the parameters to this step
   * @return a non null {@link InstrumentationContext} object that will be called back when the step ends
   */
  InstrumentationContext<ExecutionResult> beginExecution(InstrumentationExecutionParameters parameters);

  /**
   * This is called just before a query is parsed.
   *
   * @param parameters the parameters to this step
   * @return a non null {@link InstrumentationContext} object that will be called back when the step ends
   */
  InstrumentationContext<Document> beginParse(InstrumentationExecutionParameters parameters);

  /**
   * This is called just before the parsed query document is validated.
   *
   * @param parameters the parameters to this step
   * @return a non null {@link InstrumentationContext} object that will be called back when the step ends
   */
  InstrumentationContext<List<ValidationError>> beginValidation(InstrumentationValidationParameters parameters);

  /**
   * This is called just before the execution of the query operation is started.
   *
   * @param parameters the parameters to this step
   * @return a non null {@link InstrumentationContext} object that will be called back when the step ends
   */
  InstrumentationContext<ExecutionResult> beginExecuteOperation(InstrumentationExecuteOperationParameters parameters);

  /**
   * This is called each time an {@link graphql.execution.ExecutionStrategy} is invoked, which may be multiple times
   * per query as the engine recursively descends down over the query.
   *
   * @param parameters the parameters to this step
   * @return a non null {@link InstrumentationContext} object that will be called back when the step ends
   */
  ExecutionStrategyInstrumentationContext beginExecutionStrategy(InstrumentationExecutionStrategyParameters parameters);


  /**
   * This is called each time a subscription field produces a new reactive stream event value and it needs to be mapped over via the graphql field subselection.
   *
   * @param parameters the parameters to this step
   * @return a non null {@link InstrumentationContext} object that will be called back when the step ends
   */
  default InstrumentationContext<ExecutionResult> beginSubscribedFieldEvent(InstrumentationFieldParameters parameters) {
    return noOp();
  }

  /**
   * This is called just before a field is resolved into a value.
   *
   * @param parameters the parameters to this step
   * @return a non null {@link InstrumentationContext} object that will be called back when the step ends
   */
  InstrumentationContext<ExecutionResult> beginField(InstrumentationFieldParameters parameters);

  /**
   * This is called just before a field {@link DataFetcher} is invoked.
   *
   * @param parameters the parameters to this step
   * @return a non null {@link InstrumentationContext} object that will be called back when the step ends
   */
  InstrumentationContext<Object> beginFieldFetch(InstrumentationFieldFetchParameters parameters);


  /**
   * This is called just before the complete field is started.
   *
   * @param parameters the parameters to this step
   * @return a non null {@link InstrumentationContext} object that will be called back when the step ends
   */
  default InstrumentationContext<ExecutionResult> beginFieldComplete(InstrumentationFieldCompleteParameters parameters) {
    return noOp();
  }

  /**
   * This is called just before the complete field list is started.
   *
   * @param parameters the parameters to this step
   * @return a non null {@link InstrumentationContext} object that will be called back when the step ends
   */
  default InstrumentationContext<ExecutionResult> beginFieldListComplete(InstrumentationFieldCompleteParameters parameters) {
    return noOp();
  }

  /**
   * This is called to instrument a {@link ExecutionInput} before it is used to parse, validate
   * and execute a query, allowing you to adjust what query input parameters are used
   *
   * @param executionInput the execution input to be used
   * @param parameters     the parameters describing the field to be fetched
   * @return a non null instrumented ExecutionInput, the default is to return to the same object
   */
  default ExecutionInput instrumentExecutionInput(ExecutionInput executionInput, InstrumentationExecutionParameters parameters) {
    return executionInput;
  }

  /**
   * This is called to instrument a {@link graphql.language.Document} and variables before it is used allowing you to adjust the query AST if you so desire
   *
   * @param documentAndVariables the document and variables to be used
   * @param parameters           the parameters describing the execution
   * @return a non null instrumented DocumentAndVariables, the default is to return to the same objects
   */
  default DocumentAndVariables instrumentDocumentAndVariables(DocumentAndVariables documentAndVariables,
                                                              InstrumentationExecutionParameters parameters) {
    return documentAndVariables;
  }

  /**
   * This is called to instrument a {@link graphql.schema.GraphQLSchema} before it is used to parse, validate
   * and execute a query, allowing you to adjust what types are used.
   *
   * @param schema     the schema to be used
   * @param parameters the parameters describing the field to be fetched
   * @return a non null instrumented GraphQLSchema, the default is to return to the same object
   */
  default GraphQLSchema instrumentSchema(GraphQLSchema schema, InstrumentationExecutionParameters parameters) {
    return schema;
  }

  /**
   * This is called to instrument a {@link ExecutionContext} before it is used to execute a query,
   * allowing you to adjust the base data used.
   *
   * @param executionContext the execution context to be used
   * @param parameters       the parameters describing the field to be fetched
   * @return a non null instrumented ExecutionContext, the default is to return to the same object
   */
  default ExecutionContext instrumentExecutionContext(ExecutionContext executionContext, InstrumentationExecutionParameters parameters) {
    return executionContext;
  }


  /**
   * This is called to instrument a {@link DataFetcher} just before it is used to fetch a field, allowing you
   * to adjust what information is passed back or record information about specific data fetches.  Note
   * the same data fetcher instance maybe presented to you many times and that data fetcher
   * implementations widely vary.
   *
   * @param dataFetcher the data fetcher about to be used
   * @param parameters  the parameters describing the field to be fetched
   * @return a non null instrumented DataFetcher, the default is to return to the same object
   */
  default DataFetcher<?> instrumentDataFetcher(DataFetcher<?> dataFetcher, InstrumentationFieldFetchParameters parameters) {
    return dataFetcher;
  }

  /**
   * This is called to allow instrumentation to instrument the execution result in some way
   *
   * @param executionResult {@link CompletableFuture} of the result to instrument
   * @param parameters      the parameters to this step
   * @return a new execution result completable future
   */
  default CompletableFuture<ExecutionResult> instrumentExecutionResult(ExecutionResult executionResult,
                                                                       InstrumentationExecutionParameters parameters) {
    return CompletableFuture.completedFuture(executionResult);
  }
}
