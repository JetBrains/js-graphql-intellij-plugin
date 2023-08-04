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
package com.intellij.lang.jsgraphql.types.schema;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.cachecontrol.CacheControl;
import com.intellij.lang.jsgraphql.types.execution.ExecutionId;
import com.intellij.lang.jsgraphql.types.execution.ExecutionStepInfo;
import com.intellij.lang.jsgraphql.types.execution.MergedField;
import com.intellij.lang.jsgraphql.types.execution.directives.QueryDirectives;
import com.intellij.lang.jsgraphql.types.language.Document;
import com.intellij.lang.jsgraphql.types.language.Field;
import com.intellij.lang.jsgraphql.types.language.FragmentDefinition;
import com.intellij.lang.jsgraphql.types.language.OperationDefinition;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;

import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * DelegatingDataFetchingEnvironment implements {@link com.intellij.lang.jsgraphql.types.schema.DataFetchingEnvironment} by delegating
 * to an underlying instance.  You can use this class to wrap the environment and perhaps change
 * values and behavior more easily.
 */
@SuppressWarnings("TypeParameterUnusedInFormals")
@PublicApi
public class DelegatingDataFetchingEnvironment implements DataFetchingEnvironment {

  protected final DataFetchingEnvironment delegateEnvironment;

  /**
   * Called to wrap an existing {@link com.intellij.lang.jsgraphql.types.schema.DataFetchingEnvironment}.
   *
   * @param delegateEnvironment the environment to wrap and delegate all method called to
   */
  public DelegatingDataFetchingEnvironment(DataFetchingEnvironment delegateEnvironment) {
    this.delegateEnvironment = delegateEnvironment;
  }

  public <T> T getSource() {
    return delegateEnvironment.getSource();
  }

  public Map<String, Object> getArguments() {
    return delegateEnvironment.getArguments();
  }

  public boolean containsArgument(String name) {
    return delegateEnvironment.containsArgument(name);
  }

  public <T> T getArgument(String name) {
    return delegateEnvironment.getArgument(name);
  }

  public <T> T getArgumentOrDefault(String name, T defaultValue) {
    return delegateEnvironment.getArgumentOrDefault(name, defaultValue);
  }

  public <T> T getContext() {
    return delegateEnvironment.getContext();
  }

  public <T> T getLocalContext() {
    return delegateEnvironment.getLocalContext();
  }

  public <T> T getRoot() {
    return delegateEnvironment.getRoot();
  }

  public GraphQLFieldDefinition getFieldDefinition() {
    return delegateEnvironment.getFieldDefinition();
  }

  @Deprecated
  public List<Field> getFields() {
    return delegateEnvironment.getFields();
  }

  public MergedField getMergedField() {
    return delegateEnvironment.getMergedField();
  }

  public Field getField() {
    return delegateEnvironment.getField();
  }

  public GraphQLOutputType getFieldType() {
    return delegateEnvironment.getFieldType();
  }

  public ExecutionStepInfo getExecutionStepInfo() {
    return delegateEnvironment.getExecutionStepInfo();
  }

  public GraphQLType getParentType() {
    return delegateEnvironment.getParentType();
  }

  public GraphQLSchema getGraphQLSchema() {
    return delegateEnvironment.getGraphQLSchema();
  }

  public Map<String, FragmentDefinition> getFragmentsByName() {
    return delegateEnvironment.getFragmentsByName();
  }

  public ExecutionId getExecutionId() {
    return delegateEnvironment.getExecutionId();
  }

  public DataFetchingFieldSelectionSet getSelectionSet() {
    return delegateEnvironment.getSelectionSet();
  }

  public QueryDirectives getQueryDirectives() {
    return delegateEnvironment.getQueryDirectives();
  }

  public <K, V> DataLoader<K, V> getDataLoader(String dataLoaderName) {
    return delegateEnvironment.getDataLoader(dataLoaderName);
  }

  @Override
  public DataLoaderRegistry getDataLoaderRegistry() {
    return delegateEnvironment.getDataLoaderRegistry();
  }

  @Override
  public Locale getLocale() {
    return delegateEnvironment.getLocale();
  }

  public CacheControl getCacheControl() {
    return delegateEnvironment.getCacheControl();
  }

  public OperationDefinition getOperationDefinition() {
    return delegateEnvironment.getOperationDefinition();
  }

  public Document getDocument() {
    return delegateEnvironment.getDocument();
  }

  public Map<String, Object> getVariables() {
    return delegateEnvironment.getVariables();
  }
}
