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

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.execution.ConditionalNodes;
import com.intellij.lang.jsgraphql.types.execution.ValuesResolver;
import com.intellij.lang.jsgraphql.types.introspection.Introspection;
import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.lang.jsgraphql.types.schema.*;
import com.intellij.lang.jsgraphql.types.util.TraversalControl;
import com.intellij.lang.jsgraphql.types.util.TraverserContext;

import java.util.Map;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.schema.GraphQLTypeUtil.unwrapAll;
import static com.intellij.lang.jsgraphql.types.util.TraverserContext.Phase.LEAVE;
import static java.lang.String.format;

/**
 * Internally used node visitor which delegates to a {@link QueryVisitor} with type
 * information about the visited field.
 */
@Internal
public class NodeVisitorWithTypeTracking extends NodeVisitorStub {


  private final QueryVisitor preOrderCallback;
  private final QueryVisitor postOrderCallback;
  private final Map<String, Object> variables;
  private final GraphQLSchema schema;
  private final Map<String, FragmentDefinition> fragmentsByName;

  private final ConditionalNodes conditionalNodes = new ConditionalNodes();
  private final ValuesResolver valuesResolver = new ValuesResolver();


  public NodeVisitorWithTypeTracking(QueryVisitor preOrderCallback,
                                     QueryVisitor postOrderCallback,
                                     Map<String, Object> variables,
                                     GraphQLSchema schema,
                                     Map<String, FragmentDefinition> fragmentsByName) {
    this.preOrderCallback = preOrderCallback;
    this.postOrderCallback = postOrderCallback;
    this.variables = variables;
    this.schema = schema;
    this.fragmentsByName = fragmentsByName;
  }

  @Override
  public TraversalControl visitDirective(Directive node, TraverserContext<Node> context) {
    // to avoid visiting arguments for directives we abort the traversal here
    return TraversalControl.ABORT;
  }

  @Override
  public TraversalControl visitInlineFragment(InlineFragment inlineFragment, TraverserContext<Node> context) {
    if (!conditionalNodes.shouldInclude(variables, inlineFragment.getDirectives())) {
      return TraversalControl.ABORT;
    }

    QueryVisitorInlineFragmentEnvironment inlineFragmentEnvironment =
      new QueryVisitorInlineFragmentEnvironmentImpl(inlineFragment, context, schema);

    if (context.getPhase() == LEAVE) {
      postOrderCallback.visitInlineFragment(inlineFragmentEnvironment);
      return TraversalControl.CONTINUE;
    }

    preOrderCallback.visitInlineFragment(inlineFragmentEnvironment);

    // inline fragments are allowed not have type conditions, if so the parent type counts
    QueryTraversalContext parentEnv = context.getVarFromParents(QueryTraversalContext.class);

    GraphQLCompositeType fragmentCondition;
    if (inlineFragment.getTypeCondition() != null) {
      TypeName typeCondition = inlineFragment.getTypeCondition();
      fragmentCondition = (GraphQLCompositeType)schema.getType(typeCondition.getName());
    }
    else {
      fragmentCondition = parentEnv.getUnwrappedOutputType();
    }
    // for unions we only have other fragments inside
    context.setVar(QueryTraversalContext.class, new QueryTraversalContext(fragmentCondition, parentEnv.getEnvironment(), inlineFragment));
    return TraversalControl.CONTINUE;
  }

  @Override
  public TraversalControl visitFragmentDefinition(FragmentDefinition node, TraverserContext<Node> context) {
    if (!conditionalNodes.shouldInclude(variables, node.getDirectives())) {
      return TraversalControl.ABORT;
    }

    QueryVisitorFragmentDefinitionEnvironment fragmentEnvironment =
      new QueryVisitorFragmentDefinitionEnvironmentImpl(node, context, schema);

    if (context.getPhase() == LEAVE) {
      postOrderCallback.visitFragmentDefinition(fragmentEnvironment);
      return TraversalControl.CONTINUE;
    }
    preOrderCallback.visitFragmentDefinition(fragmentEnvironment);

    QueryTraversalContext parentEnv = context.getVarFromParents(QueryTraversalContext.class);
    GraphQLCompositeType typeCondition = (GraphQLCompositeType)schema.getType(node.getTypeCondition().getName());
    context.setVar(QueryTraversalContext.class, new QueryTraversalContext(typeCondition, parentEnv.getEnvironment(), node));
    return TraversalControl.CONTINUE;
  }

  @Override
  public TraversalControl visitFragmentSpread(FragmentSpread fragmentSpread, TraverserContext<Node> context) {
    if (!conditionalNodes.shouldInclude(variables, fragmentSpread.getDirectives())) {
      return TraversalControl.ABORT;
    }

    FragmentDefinition fragmentDefinition = fragmentsByName.get(fragmentSpread.getName());
    if (!conditionalNodes.shouldInclude(variables, fragmentDefinition.getDirectives())) {
      return TraversalControl.ABORT;
    }

    QueryVisitorFragmentSpreadEnvironment fragmentSpreadEnvironment =
      new QueryVisitorFragmentSpreadEnvironmentImpl(fragmentSpread, fragmentDefinition, context, schema);
    if (context.getPhase() == LEAVE) {
      postOrderCallback.visitFragmentSpread(fragmentSpreadEnvironment);
      return TraversalControl.CONTINUE;
    }

    preOrderCallback.visitFragmentSpread(fragmentSpreadEnvironment);

    QueryTraversalContext parentEnv = context.getVarFromParents(QueryTraversalContext.class);

    GraphQLCompositeType typeCondition = (GraphQLCompositeType)schema.getType(fragmentDefinition.getTypeCondition().getName());
    assertNotNull(typeCondition,
                  () -> format("Invalid type condition '%s' in fragment '%s'", fragmentDefinition.getTypeCondition().getName(),
                               fragmentDefinition.getName()));
    context.setVar(QueryTraversalContext.class, new QueryTraversalContext(typeCondition, parentEnv.getEnvironment(), fragmentDefinition));
    return TraversalControl.CONTINUE;
  }

  @Override
  public TraversalControl visitField(Field field, TraverserContext<Node> context) {
    QueryTraversalContext parentEnv = context.getVarFromParents(QueryTraversalContext.class);

    GraphQLFieldDefinition fieldDefinition =
      Introspection.getFieldDef(schema, (GraphQLCompositeType)unwrapAll(parentEnv.getOutputType()), field.getName());
    boolean isTypeNameIntrospectionField = fieldDefinition == Introspection.TypeNameMetaFieldDef;
    GraphQLFieldsContainer fieldsContainer =
      !isTypeNameIntrospectionField ? (GraphQLFieldsContainer)unwrapAll(parentEnv.getOutputType()) : null;
    GraphQLCodeRegistry codeRegistry = schema.getCodeRegistry();
    Map<String, Object> argumentValues =
      valuesResolver.getArgumentValues(codeRegistry, fieldDefinition.getArguments(), field.getArguments(), variables);
    QueryVisitorFieldEnvironment environment = new QueryVisitorFieldEnvironmentImpl(isTypeNameIntrospectionField,
                                                                                    field,
                                                                                    fieldDefinition,
                                                                                    parentEnv.getOutputType(),
                                                                                    fieldsContainer,
                                                                                    parentEnv.getEnvironment(),
                                                                                    argumentValues,
                                                                                    parentEnv.getSelectionSetContainer(),
                                                                                    context, schema);

    if (context.getPhase() == LEAVE) {
      postOrderCallback.visitField(environment);
      return TraversalControl.CONTINUE;
    }

    if (!conditionalNodes.shouldInclude(variables, field.getDirectives())) {
      return TraversalControl.ABORT;
    }

    TraversalControl traversalControl = preOrderCallback.visitFieldWithControl(environment);

    GraphQLUnmodifiedType unmodifiedType = unwrapAll(fieldDefinition.getType());
    QueryTraversalContext fieldEnv = (unmodifiedType instanceof GraphQLCompositeType)
                                     ? new QueryTraversalContext(fieldDefinition.getType(), environment, field)
                                     : new QueryTraversalContext(null, environment, field);// Terminal (scalar) node, EMPTY FRAME


    context.setVar(QueryTraversalContext.class, fieldEnv);
    return traversalControl;
  }


  @Override
  public TraversalControl visitArgument(Argument argument, TraverserContext<Node> context) {

    QueryTraversalContext fieldCtx = context.getVarFromParents(QueryTraversalContext.class);
    Field field = (Field)fieldCtx.getSelectionSetContainer();

    QueryVisitorFieldEnvironment fieldEnv = fieldCtx.getEnvironment();
    GraphQLFieldsContainer fieldsContainer = fieldEnv.getFieldsContainer();

    GraphQLFieldDefinition fieldDefinition = Introspection.getFieldDef(schema, fieldsContainer, field.getName());
    GraphQLArgument graphQLArgument = fieldDefinition.getArgument(argument.getName());
    String argumentName = graphQLArgument.getName();

    Object argumentValue = fieldEnv.getArguments().getOrDefault(argumentName, null);

    QueryVisitorFieldArgumentEnvironment environment = new QueryVisitorFieldArgumentEnvironmentImpl(
      fieldDefinition, argument, graphQLArgument, argumentValue, variables, fieldEnv, context, schema);

    QueryVisitorFieldArgumentInputValue inputValue = QueryVisitorFieldArgumentInputValueImpl
      .incompleteArgumentInputValue(graphQLArgument);

    context.setVar(QueryVisitorFieldArgumentEnvironment.class, environment);
    context.setVar(QueryVisitorFieldArgumentInputValue.class, inputValue);
    if (context.getPhase() == LEAVE) {
      return postOrderCallback.visitArgument(environment);
    }
    return preOrderCallback.visitArgument(environment);
  }

  @Override
  public TraversalControl visitObjectField(ObjectField node, TraverserContext<Node> context) {

    QueryVisitorFieldArgumentInputValueImpl inputValue = context.getVarFromParents(QueryVisitorFieldArgumentInputValue.class);
    GraphQLUnmodifiedType unmodifiedType = unwrapAll(inputValue.getInputType());
    //
    // technically a scalar type can have an AST object field - eg field( arg : Json) -> field(arg : { ast : "here" })
    if (unmodifiedType instanceof GraphQLInputObjectType inputObjectType) {
      GraphQLInputObjectField inputObjectTypeField = inputObjectType.getField(node.getName());

      inputValue = inputValue.incompleteNewChild(inputObjectTypeField);
      context.setVar(QueryVisitorFieldArgumentInputValue.class, inputValue);
    }
    return TraversalControl.CONTINUE;
  }

  @Override
  protected TraversalControl visitValue(Value<?> value, TraverserContext<Node> context) {
    if (context.getParentNode() instanceof VariableDefinition) {
      return TraversalControl.CONTINUE;
    }

    QueryVisitorFieldArgumentEnvironment fieldArgEnv = context.getVarFromParents(QueryVisitorFieldArgumentEnvironment.class);
    QueryVisitorFieldArgumentInputValueImpl inputValue = context.getVarFromParents(QueryVisitorFieldArgumentInputValue.class);
    // previous visits have set up the previous information
    inputValue = inputValue.completeArgumentInputValue(value);
    context.setVar(QueryVisitorFieldArgumentInputValue.class, inputValue);

    QueryVisitorFieldArgumentValueEnvironment environment = new QueryVisitorFieldArgumentValueEnvironmentImpl(
      schema, fieldArgEnv.getFieldDefinition(), fieldArgEnv.getGraphQLArgument(), inputValue, context,
      variables);

    if (context.getPhase() == LEAVE) {
      return postOrderCallback.visitArgumentValue(environment);
    }
    return preOrderCallback.visitArgumentValue(environment);
  }
}
