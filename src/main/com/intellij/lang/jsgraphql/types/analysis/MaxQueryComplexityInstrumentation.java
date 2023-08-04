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
package com.intellij.lang.jsgraphql.types.analysis;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.execution.AbortExecutionException;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.InstrumentationContext;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.SimpleInstrumentation;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.parameters.InstrumentationValidationParameters;
import com.intellij.lang.jsgraphql.types.validation.ValidationError;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.execution.instrumentation.SimpleInstrumentationContext.whenCompleted;
import static java.util.Optional.ofNullable;

/**
 * Prevents execution if the query complexity is greater than the specified maxComplexity.
 * <p>
 * Use the {@code Function<QueryComplexityInfo, Boolean>} parameter to supply a function to perform a custom action when the max complexity
 * is exceeded. If the function returns {@code true} a {@link AbortExecutionException} is thrown.
 */
@PublicApi
public class MaxQueryComplexityInstrumentation extends SimpleInstrumentation {

  private final int maxComplexity;
  private final FieldComplexityCalculator fieldComplexityCalculator;
  private final Function<QueryComplexityInfo, Boolean> maxQueryComplexityExceededFunction;

  /**
   * new Instrumentation with default complexity calculator which is `1 + childComplexity`
   *
   * @param maxComplexity max allowed complexity, otherwise execution will be aborted
   */
  public MaxQueryComplexityInstrumentation(int maxComplexity) {
    this(maxComplexity, (queryComplexityInfo) -> true);
  }

  /**
   * new Instrumentation with default complexity calculator which is `1 + childComplexity`
   *
   * @param maxComplexity                      max allowed complexity, otherwise execution will be aborted
   * @param maxQueryComplexityExceededFunction the function to perform when the max complexity is exceeded
   */
  public MaxQueryComplexityInstrumentation(int maxComplexity, Function<QueryComplexityInfo, Boolean> maxQueryComplexityExceededFunction) {
    this(maxComplexity, (env, childComplexity) -> 1 + childComplexity, maxQueryComplexityExceededFunction);
  }

  /**
   * new Instrumentation with custom complexity calculator
   *
   * @param maxComplexity             max allowed complexity, otherwise execution will be aborted
   * @param fieldComplexityCalculator custom complexity calculator
   */
  public MaxQueryComplexityInstrumentation(int maxComplexity, FieldComplexityCalculator fieldComplexityCalculator) {
    this(maxComplexity, fieldComplexityCalculator, (queryComplexityInfo) -> true);
  }

  /**
   * new Instrumentation with custom complexity calculator
   *
   * @param maxComplexity                      max allowed complexity, otherwise execution will be aborted
   * @param fieldComplexityCalculator          custom complexity calculator
   * @param maxQueryComplexityExceededFunction the function to perform when the max complexity is exceeded
   */
  public MaxQueryComplexityInstrumentation(int maxComplexity, FieldComplexityCalculator fieldComplexityCalculator,
                                           Function<QueryComplexityInfo, Boolean> maxQueryComplexityExceededFunction) {
    this.maxComplexity = maxComplexity;
    this.fieldComplexityCalculator = assertNotNull(fieldComplexityCalculator, () -> "calculator can't be null");
    this.maxQueryComplexityExceededFunction = maxQueryComplexityExceededFunction;
  }

  @Override
  public InstrumentationContext<List<ValidationError>> beginValidation(InstrumentationValidationParameters parameters) {
    return whenCompleted((errors, throwable) -> {
      if ((errors != null && errors.size() > 0) || throwable != null) {
        return;
      }
      QueryTraverser queryTraverser = newQueryTraverser(parameters);

      Map<QueryVisitorFieldEnvironment, Integer> valuesByParent = new LinkedHashMap<>();
      queryTraverser.visitPostOrder(new QueryVisitorStub() {
        @Override
        public void visitField(QueryVisitorFieldEnvironment env) {
          int childsComplexity = valuesByParent.getOrDefault(env, 0);
          int value = calculateComplexity(env, childsComplexity);

          valuesByParent.compute(env.getParentEnvironment(), (key, oldValue) ->
            ofNullable(oldValue).orElse(0) + value
          );
        }
      });
      int totalComplexity = valuesByParent.getOrDefault(null, 0);
      if (totalComplexity > maxComplexity) {
        QueryComplexityInfo queryComplexityInfo = QueryComplexityInfo.newQueryComplexityInfo()
          .complexity(totalComplexity)
          .build();
        boolean throwAbortException = maxQueryComplexityExceededFunction.apply(queryComplexityInfo);
        if (throwAbortException) {
          throw mkAbortException(totalComplexity, maxComplexity);
        }
      }
    });
  }

  /**
   * Called to generate your own error message or custom exception class
   *
   * @param totalComplexity the complexity of the query
   * @param maxComplexity   the maximum complexity allowed
   * @return a instance of AbortExecutionException
   */
  protected AbortExecutionException mkAbortException(int totalComplexity, int maxComplexity) {
    return new AbortExecutionException("maximum query complexity exceeded " + totalComplexity + " > " + maxComplexity);
  }

  QueryTraverser newQueryTraverser(InstrumentationValidationParameters parameters) {
    return QueryTraverser.newQueryTraverser()
      .schema(parameters.getSchema())
      .document(parameters.getDocument())
      .operationName(parameters.getOperation())
      .variables(parameters.getVariables())
      .build();
  }

  private int calculateComplexity(QueryVisitorFieldEnvironment queryVisitorFieldEnvironment, int childsComplexity) {
    if (queryVisitorFieldEnvironment.isTypeNameIntrospectionField()) {
      return 0;
    }
    FieldComplexityEnvironment fieldComplexityEnvironment = convertEnv(queryVisitorFieldEnvironment);
    return fieldComplexityCalculator.calculate(fieldComplexityEnvironment, childsComplexity);
  }

  private FieldComplexityEnvironment convertEnv(QueryVisitorFieldEnvironment queryVisitorFieldEnvironment) {
    FieldComplexityEnvironment parentEnv = null;
    if (queryVisitorFieldEnvironment.getParentEnvironment() != null) {
      parentEnv = convertEnv(queryVisitorFieldEnvironment.getParentEnvironment());
    }
    return new FieldComplexityEnvironment(
      queryVisitorFieldEnvironment.getField(),
      queryVisitorFieldEnvironment.getFieldDefinition(),
      queryVisitorFieldEnvironment.getFieldsContainer(),
      queryVisitorFieldEnvironment.getArguments(),
      parentEnv
    );
  }
}
