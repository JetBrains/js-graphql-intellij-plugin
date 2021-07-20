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
package com.intellij.lang.jsgraphql.types.schema.idl;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.NamedNode;
import com.intellij.lang.jsgraphql.types.schema.*;
import com.intellij.lang.jsgraphql.types.util.TraversalControl;
import com.intellij.lang.jsgraphql.types.util.TraverserContext;
import com.intellij.lang.jsgraphql.types.util.TreeTransformerUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.intellij.lang.jsgraphql.types.util.TraversalControl.CONTINUE;

@Internal
class SchemaDirectiveWiringSchemaGeneratorPostProcessing implements SchemaGeneratorPostProcessing {

    private final SchemaGeneratorDirectiveHelper generatorDirectiveHelper = new SchemaGeneratorDirectiveHelper();
    private final TypeDefinitionRegistry typeRegistry;
    private final RuntimeWiring runtimeWiring;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder;
    private final Map<String, Object> directiveBehaviourContext = new HashMap<>();


    public SchemaDirectiveWiringSchemaGeneratorPostProcessing(TypeDefinitionRegistry typeRegistry, RuntimeWiring runtimeWiring, GraphQLCodeRegistry.Builder codeRegistryBuilder) {
        this.typeRegistry = typeRegistry;
        this.runtimeWiring = runtimeWiring;
        this.codeRegistryBuilder = codeRegistryBuilder;
    }


    @Override
    public GraphQLSchema process(GraphQLSchema originalSchema) {
        GraphQLSchema newSchema = SchemaTransformer.transformSchema(originalSchema, new Visitor());
        return newSchema.transform(builder -> {
            // they could have changed the code registry so rebuild it
            GraphQLCodeRegistry codeRegistry = this.codeRegistryBuilder.build();
            builder.codeRegistry(codeRegistry);
        });
    }

    public class Visitor extends GraphQLTypeVisitorStub {

        private SchemaGeneratorDirectiveHelper.Parameters mkBehaviourParams() {
            return new SchemaGeneratorDirectiveHelper.Parameters(typeRegistry, runtimeWiring, directiveBehaviourContext, codeRegistryBuilder);
        }

        private TraversalControl changOrContinue(GraphQLSchemaElement node, GraphQLSchemaElement newNode, TraverserContext<GraphQLSchemaElement> context) {
            if (node != newNode) {
                TreeTransformerUtil.changeNode(context, newNode);
            }
            return CONTINUE;
        }

        private boolean isIntrospectionType(GraphQLNamedType type) {
            return type.getName().startsWith("__");
        }

        private <T extends GraphQLNamedType> boolean notSuitable(T node, Function<T, NamedNode<?>> suitableFunc) {
            if (isIntrospectionType(node)) {
                return true;
            }
            NamedNode<?> definition = suitableFunc.apply(node);
            return definition == null;
        }

        @Override
        public TraversalControl visitGraphQLObjectType(GraphQLObjectType node, TraverserContext<GraphQLSchemaElement> context) {
            if (notSuitable(node, GraphQLObjectType::getDefinition)) {
                return CONTINUE;
            }
            GraphQLSchemaElement newNode = generatorDirectiveHelper.onObject(node, mkBehaviourParams());
            return changOrContinue(node, newNode, context);
        }

        @Override
        public TraversalControl visitGraphQLInterfaceType(GraphQLInterfaceType node, TraverserContext<GraphQLSchemaElement> context) {
            if (notSuitable(node, GraphQLInterfaceType::getDefinition)) {
                return CONTINUE;
            }
            GraphQLSchemaElement newNode = generatorDirectiveHelper.onInterface(node, mkBehaviourParams());
            return changOrContinue(node, newNode, context);
        }

        @Override
        public TraversalControl visitGraphQLEnumType(GraphQLEnumType node, TraverserContext<GraphQLSchemaElement> context) {
            if (notSuitable(node, GraphQLEnumType::getDefinition)) {
                return CONTINUE;
            }
            GraphQLSchemaElement newNode = generatorDirectiveHelper.onEnum(node, mkBehaviourParams());
            return changOrContinue(node, newNode, context);
        }

        @Override
        public TraversalControl visitGraphQLInputObjectType(GraphQLInputObjectType node, TraverserContext<GraphQLSchemaElement> context) {
            if (notSuitable(node, GraphQLInputObjectType::getDefinition)) {
                return CONTINUE;
            }
            GraphQLSchemaElement newNode = generatorDirectiveHelper.onInputObjectType(node, mkBehaviourParams());
            return changOrContinue(node, newNode, context);
        }

        @Override
        public TraversalControl visitGraphQLScalarType(GraphQLScalarType node, TraverserContext<GraphQLSchemaElement> context) {
            if (notSuitable(node, GraphQLScalarType::getDefinition)) {
                return CONTINUE;
            }
            GraphQLSchemaElement newNode = generatorDirectiveHelper.onScalar(node, mkBehaviourParams());
            return changOrContinue(node, newNode, context);
        }

        @Override
        public TraversalControl visitGraphQLUnionType(GraphQLUnionType node, TraverserContext<GraphQLSchemaElement> context) {
            if (notSuitable(node, GraphQLUnionType::getDefinition)) {
                return CONTINUE;
            }
            GraphQLSchemaElement newNode = generatorDirectiveHelper.onUnion(node, mkBehaviourParams());
            return changOrContinue(node, newNode, context);
        }
    }
}
