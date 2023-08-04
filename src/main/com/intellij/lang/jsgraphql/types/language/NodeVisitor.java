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
package com.intellij.lang.jsgraphql.types.language;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.util.TraversalControl;
import com.intellij.lang.jsgraphql.types.util.TraverserContext;

/**
 * Used by {@link NodeTraverser} to visit {@link Node}.
 */
@PublicApi
public interface NodeVisitor {
  TraversalControl visitArgument(Argument node, TraverserContext<Node> data);

  TraversalControl visitArrayValue(ArrayValue node, TraverserContext<Node> data);

  TraversalControl visitBooleanValue(BooleanValue node, TraverserContext<Node> data);

  TraversalControl visitDirective(Directive node, TraverserContext<Node> data);

  TraversalControl visitDirectiveDefinition(DirectiveDefinition node, TraverserContext<Node> data);

  TraversalControl visitDirectiveLocation(DirectiveLocation node, TraverserContext<Node> data);

  TraversalControl visitDocument(Document node, TraverserContext<Node> data);

  TraversalControl visitEnumTypeDefinition(EnumTypeDefinition node, TraverserContext<Node> data);

  TraversalControl visitEnumValue(EnumValue node, TraverserContext<Node> data);

  TraversalControl visitEnumValueDefinition(EnumValueDefinition node, TraverserContext<Node> data);

  TraversalControl visitField(Field node, TraverserContext<Node> data);

  TraversalControl visitFieldDefinition(FieldDefinition node, TraverserContext<Node> data);

  TraversalControl visitFloatValue(FloatValue node, TraverserContext<Node> data);

  TraversalControl visitFragmentDefinition(FragmentDefinition node, TraverserContext<Node> data);

  TraversalControl visitFragmentSpread(FragmentSpread node, TraverserContext<Node> data);

  TraversalControl visitInlineFragment(InlineFragment node, TraverserContext<Node> data);

  TraversalControl visitInputObjectTypeDefinition(InputObjectTypeDefinition node, TraverserContext<Node> data);

  TraversalControl visitInputValueDefinition(InputValueDefinition node, TraverserContext<Node> data);

  TraversalControl visitIntValue(IntValue node, TraverserContext<Node> data);

  TraversalControl visitInterfaceTypeDefinition(InterfaceTypeDefinition node, TraverserContext<Node> data);

  TraversalControl visitListType(ListType node, TraverserContext<Node> data);

  TraversalControl visitNonNullType(NonNullType node, TraverserContext<Node> data);

  TraversalControl visitNullValue(NullValue node, TraverserContext<Node> data);

  TraversalControl visitObjectField(ObjectField node, TraverserContext<Node> data);

  TraversalControl visitObjectTypeDefinition(ObjectTypeDefinition node, TraverserContext<Node> data);

  TraversalControl visitObjectValue(ObjectValue node, TraverserContext<Node> data);

  TraversalControl visitOperationDefinition(OperationDefinition node, TraverserContext<Node> data);

  TraversalControl visitOperationTypeDefinition(OperationTypeDefinition node, TraverserContext<Node> data);

  TraversalControl visitScalarTypeDefinition(ScalarTypeDefinition node, TraverserContext<Node> data);

  TraversalControl visitSchemaDefinition(SchemaDefinition node, TraverserContext<Node> data);

  TraversalControl visitSelectionSet(SelectionSet node, TraverserContext<Node> data);

  TraversalControl visitStringValue(StringValue node, TraverserContext<Node> data);

  TraversalControl visitTypeName(TypeName node, TraverserContext<Node> data);

  TraversalControl visitUnionTypeDefinition(UnionTypeDefinition node, TraverserContext<Node> data);

  TraversalControl visitVariableDefinition(VariableDefinition node, TraverserContext<Node> data);

  TraversalControl visitVariableReference(VariableReference node, TraverserContext<Node> data);
}
