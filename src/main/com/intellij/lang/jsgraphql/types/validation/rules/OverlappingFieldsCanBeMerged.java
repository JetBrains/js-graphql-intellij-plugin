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
package com.intellij.lang.jsgraphql.types.validation.rules;


import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.execution.TypeFromAST;
import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.lang.jsgraphql.types.schema.*;
import com.intellij.lang.jsgraphql.types.validation.AbstractRule;
import com.intellij.lang.jsgraphql.types.validation.ValidationContext;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorCollector;

import java.util.*;

import static com.intellij.lang.jsgraphql.types.schema.GraphQLTypeUtil.*;
import static com.intellij.lang.jsgraphql.types.validation.ValidationErrorType.FieldsConflict;
import static java.lang.String.format;

/**
 * See http://facebook.github.io/graphql/June2018/#sec-Field-Selection-Merging
 */
@Internal
public class OverlappingFieldsCanBeMerged extends AbstractRule {


  private final List<FieldPair> alreadyChecked = new ArrayList<>();

  public OverlappingFieldsCanBeMerged(ValidationContext validationContext, ValidationErrorCollector validationErrorCollector) {
    super(validationContext, validationErrorCollector);
  }

  @Override
  public void leaveSelectionSet(SelectionSet selectionSet) {
    Map<String, List<FieldAndType>> fieldMap = new LinkedHashMap<>();
    Set<String> visitedFragmentSpreads = new LinkedHashSet<>();
    collectFields(fieldMap, selectionSet, getValidationContext().getOutputType(), visitedFragmentSpreads);
    List<Conflict> conflicts = findConflicts(fieldMap);
    for (Conflict conflict : conflicts) {
      addError(FieldsConflict, conflict.fields, conflict.reason);
    }
  }

  private List<Conflict> findConflicts(Map<String, List<FieldAndType>> fieldMap) {
    List<Conflict> result = new ArrayList<>();
    for (String name : fieldMap.keySet()) {
      List<FieldAndType> fieldAndTypes = fieldMap.get(name);
      for (int i = 0; i < fieldAndTypes.size(); i++) {
        for (int j = i + 1; j < fieldAndTypes.size(); j++) {
          Conflict conflict = findConflict(name, fieldAndTypes.get(i), fieldAndTypes.get(j));
          if (conflict != null) {
            result.add(conflict);
          }
        }
      }
    }
    return result;
  }

  private boolean isAlreadyChecked(Field field1, Field field2) {
    for (FieldPair fieldPair : alreadyChecked) {
      if (fieldPair.field1 == field1 && fieldPair.field2 == field2) {
        return true;
      }
      if (fieldPair.field1 == field2 && fieldPair.field2 == field1) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("ConstantConditions")
  private Conflict findConflict(String responseName, FieldAndType fieldAndTypeA, FieldAndType fieldAndTypeB) {

    Field fieldA = fieldAndTypeA.field;
    Field fieldB = fieldAndTypeB.field;

    if (isAlreadyChecked(fieldA, fieldB)) {
      return null;
    }
    alreadyChecked.add(new FieldPair(fieldA, fieldB));

    String fieldNameA = fieldA.getName();
    String fieldNameB = fieldB.getName();

    GraphQLType typeA = fieldAndTypeA.graphQLType;
    GraphQLType typeB = fieldAndTypeB.graphQLType;

    Conflict conflict = checkListAndNonNullConflict(responseName, fieldAndTypeA, fieldAndTypeB);
    if (conflict != null) {
      return conflict;
    }

    typeA = unwrapAll(typeA);
    typeB = unwrapAll(typeB);

    if (checkScalarAndEnumConflict(typeA, typeB)) {
      return mkNotSameTypeError(responseName, fieldA, fieldB, typeA, typeB);
    }

    // If the statically known parent types could not possibly apply at the same
    // time, then it is safe to permit them to diverge as they will not present
    // any ambiguity by differing.
    // It is known that two parent types could never overlap if they are
    // different Object types. Interface or Union types might overlap - if not
    // in the current state of the schema, then perhaps in some future version,
    // thus may not safely diverge.
    if (!sameType(fieldAndTypeA.parentType, fieldAndTypeB.parentType) &&
        fieldAndTypeA.parentType instanceof GraphQLObjectType &&
        fieldAndTypeB.parentType instanceof GraphQLObjectType) {
      return null;
    }

    if (!fieldNameA.equals(fieldNameB)) {
      String reason = format("%s: %s and %s are different fields", responseName, fieldNameA, fieldNameB);
      return new Conflict(responseName, reason, fieldA, fieldB);
    }

    if (!sameType(typeA, typeB)) {
      return mkNotSameTypeError(responseName, fieldA, fieldB, typeA, typeB);
    }


    if (!sameArguments(fieldA.getArguments(), fieldB.getArguments())) {
      String reason = format("%s: they have differing arguments", responseName);
      return new Conflict(responseName, reason, fieldA, fieldB);
    }
    SelectionSet selectionSet1 = fieldA.getSelectionSet();
    SelectionSet selectionSet2 = fieldB.getSelectionSet();
    if (selectionSet1 != null && selectionSet2 != null) {
      Set<String> visitedFragmentSpreads = new LinkedHashSet<>();
      Map<String, List<FieldAndType>> subFieldMap = new LinkedHashMap<>();
      collectFields(subFieldMap, selectionSet1, typeA, visitedFragmentSpreads);
      collectFields(subFieldMap, selectionSet2, typeB, visitedFragmentSpreads);
      List<Conflict> subConflicts = findConflicts(subFieldMap);
      if (!subConflicts.isEmpty()) {
        String reason = format("%s: %s", responseName, joinReasons(subConflicts));
        List<Field> fields = new ArrayList<>();
        fields.add(fieldA);
        fields.add(fieldB);
        fields.addAll(collectFields(subConflicts));
        return new Conflict(responseName, reason, fields);
      }
    }
    return null;
  }

  private Conflict checkListAndNonNullConflict(String responseName, FieldAndType fieldAndTypeA, FieldAndType fieldAndTypeB) {

    GraphQLType typeA = fieldAndTypeA.graphQLType;
    GraphQLType typeB = fieldAndTypeB.graphQLType;

    while (true) {
      if (isNonNull(typeA) || isNonNull(typeB)) {
        if (isNullable(typeA) || isNullable(typeB)) {
          String reason = format("%s: fields have different nullability shapes", responseName);
          return new Conflict(responseName, reason, fieldAndTypeA.field, fieldAndTypeB.field);
        }
      }
      if (isList(typeA) || isList(typeB)) {
        if (!isList(typeA) || !isList(typeB)) {
          String reason = format("%s: fields have different list shapes", responseName);
          return new Conflict(responseName, reason, fieldAndTypeA.field, fieldAndTypeB.field);
        }
      }
      if (isNotWrapped(typeA) && isNotWrapped(typeB)) {
        break;
      }
      typeA = unwrapOne(typeA);
      typeB = unwrapOne(typeB);
    }
    return null;
  }

  private boolean checkScalarAndEnumConflict(GraphQLType typeA, GraphQLType typeB) {
    if (isScalar(typeA) || isScalar(typeB)) {
      if (!sameType(typeA, typeB)) {
        return true;
      }
    }
    if (isEnum(typeA) || isEnum(typeB)) {
      if (!sameType(typeA, typeB)) {
        return true;
      }
    }
    return false;
  }

  private Conflict mkNotSameTypeError(String responseName, Field fieldA, Field fieldB, GraphQLType typeA, GraphQLType typeB) {
    String name1 = typeA != null ? simplePrint(typeA) : "null";
    String name2 = typeB != null ? simplePrint(typeB) : "null";
    String reason = format("%s: they return differing types %s and %s", responseName, name1, name2);
    return new Conflict(responseName, reason, fieldA, fieldB);
  }

  private List<Field> collectFields(List<Conflict> conflicts) {
    List<Field> result = new ArrayList<>();
    for (Conflict conflict : conflicts) {
      result.addAll(conflict.fields);
    }
    return result;
  }

  private String joinReasons(List<Conflict> conflicts) {
    StringBuilder result = new StringBuilder();
    result.append("(");
    for (Conflict conflict : conflicts) {
      result.append(conflict.reason);
      result.append(", ");
    }
    result.delete(result.length() - 2, result.length());
    result.append(")");
    return result.toString();
  }

  @SuppressWarnings("SimplifiableIfStatement")
  private boolean sameType(GraphQLType type1, GraphQLType type2) {
    if (type1 == null || type2 == null) {
      return true;
    }
    return type1.equals(type2);
  }

  @SuppressWarnings("SimplifiableIfStatement")
  private boolean sameValue(Value value1, Value value2) {
    if (value1 == null && value2 == null) {
      return true;
    }
    if (value1 == null) {
      return false;
    }
    if (value2 == null) {
      return false;
    }
    return new AstComparator().isEqual(value1, value2);
  }

  private boolean sameArguments(List<Argument> arguments1, List<Argument> arguments2) {
    if (arguments1.size() != arguments2.size()) {
      return false;
    }
    for (Argument argument : arguments1) {
      Argument matchedArgument = findArgumentByName(argument.getName(), arguments2);
      if (matchedArgument == null) {
        return false;
      }
      if (!sameValue(argument.getValue(), matchedArgument.getValue())) {
        return false;
      }
    }
    return true;
  }

  private Argument findArgumentByName(String name, List<Argument> arguments) {
    for (Argument argument : arguments) {
      if (argument.getName().equals(name)) {
        return argument;
      }
    }
    return null;
  }

  private void collectFields(Map<String, List<FieldAndType>> fieldMap,
                             SelectionSet selectionSet,
                             GraphQLType parentType,
                             Set<String> visitedFragmentSpreads) {
    if (selectionSet == null) return;

    for (Selection selection : selectionSet.getSelections()) {
      if (selection instanceof Field) {
        collectFieldsForField(fieldMap, parentType, (Field)selection);
      }
      else if (selection instanceof InlineFragment) {
        collectFieldsForInlineFragment(fieldMap, visitedFragmentSpreads, parentType, (InlineFragment)selection);
      }
      else if (selection instanceof FragmentSpread) {
        collectFieldsForFragmentSpread(fieldMap, visitedFragmentSpreads, (FragmentSpread)selection);
      }
    }
  }

  private void collectFieldsForFragmentSpread(Map<String, List<FieldAndType>> fieldMap,
                                              Set<String> visitedFragmentSpreads,
                                              FragmentSpread fragmentSpread) {
    FragmentDefinition fragment = getValidationContext().getFragment(fragmentSpread.getName());
    if (fragment == null) {
      return;
    }
    if (visitedFragmentSpreads.contains(fragment.getName())) {
      return;
    }
    visitedFragmentSpreads.add(fragment.getName());
    GraphQLType graphQLType = TypeFromAST.getTypeFromAST(getValidationContext().getSchema(),
                                                         fragment.getTypeCondition());
    collectFields(fieldMap, fragment.getSelectionSet(), graphQLType, visitedFragmentSpreads);
  }

  private void collectFieldsForInlineFragment(Map<String, List<FieldAndType>> fieldMap,
                                              Set<String> visitedFragmentSpreads,
                                              GraphQLType parentType,
                                              InlineFragment inlineFragment) {
    GraphQLType graphQLType = inlineFragment.getTypeCondition() != null
                              ? TypeFromAST.getTypeFromAST(getValidationContext().getSchema(), inlineFragment.getTypeCondition())
                              : parentType;
    collectFields(fieldMap, inlineFragment.getSelectionSet(), graphQLType, visitedFragmentSpreads);
  }

  private void collectFieldsForField(Map<String, List<FieldAndType>> fieldMap, GraphQLType parentType, Field field) {
    String responseName = field.getResultKey();
    if (!fieldMap.containsKey(responseName)) {
      fieldMap.put(responseName, new ArrayList<>());
    }
    GraphQLOutputType fieldType = null;
    GraphQLUnmodifiedType unwrappedParent = unwrapAll(parentType);
    if (unwrappedParent instanceof GraphQLFieldsContainer fieldsContainer) {
      GraphQLFieldDefinition fieldDefinition = getVisibleFieldDefinition(fieldsContainer, field);
      fieldType = fieldDefinition != null ? fieldDefinition.getType() : null;
    }
    fieldMap.get(responseName).add(new FieldAndType(field, fieldType, parentType));
  }

  private GraphQLFieldDefinition getVisibleFieldDefinition(GraphQLFieldsContainer fieldsContainer, Field field) {
    return fieldsContainer.getFieldDefinition(field.getName());
  }

  private static class FieldPair {
    final Field field1;
    final Field field2;

    public FieldPair(Field field1, Field field2) {
      this.field1 = field1;
      this.field2 = field2;
    }
  }

  private static class Conflict {
    final String responseName;
    final String reason;
    final List<Field> fields = new ArrayList<>();

    public Conflict(String responseName, String reason, Field field1, Field field2) {
      this.responseName = responseName;
      this.reason = reason;
      this.fields.add(field1);
      this.fields.add(field2);
    }

    public Conflict(String responseName, String reason, List<Field> fields) {
      this.responseName = responseName;
      this.reason = reason;
      this.fields.addAll(fields);
    }
  }


  private static class FieldAndType {
    final Field field;
    final GraphQLType graphQLType;
    final GraphQLType parentType;

    public FieldAndType(Field field, GraphQLType graphQLType, GraphQLType parentType) {
      this.field = field;
      this.graphQLType = graphQLType;
      this.parentType = parentType;
    }
  }
}
