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
package com.intellij.lang.jsgraphql.types.schema.validation;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.schema.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Internal
public class SchemaValidator {

  private final Set<GraphQLOutputType> processed = new LinkedHashSet<>();

  private List<SchemaValidationRule> rules = new ArrayList<>();

  public SchemaValidator() {
    rules.add(new NoUnbrokenInputCycles());
    rules.add(new TypeAndFieldRule());
  }

  SchemaValidator(List<SchemaValidationRule> rules) {
    this.rules = rules;
  }

  public List<SchemaValidationRule> getRules() {
    return rules;
  }

  public Set<SchemaValidationError> validateSchema(GraphQLSchema schema) {
    SchemaValidationErrorCollector validationErrorCollector = new SchemaValidationErrorCollector();

    checkTypes(schema, validationErrorCollector);
    checkSchema(schema, validationErrorCollector);

    traverse(schema.getQueryType(), rules, validationErrorCollector);
    if (schema.isSupportingMutations()) {
      traverse(schema.getMutationType(), rules, validationErrorCollector);
    }
    if (schema.isSupportingSubscriptions()) {
      traverse(schema.getSubscriptionType(), rules, validationErrorCollector);
    }
    return validationErrorCollector.getErrors();
  }

  private void checkSchema(GraphQLSchema schema, SchemaValidationErrorCollector validationErrorCollector) {
    for (SchemaValidationRule rule : rules) {
      rule.check(schema, validationErrorCollector);
    }
  }

  private void checkTypes(GraphQLSchema schema, SchemaValidationErrorCollector validationErrorCollector) {
    List<GraphQLNamedType> types = schema.getAllTypesAsList();
    types.forEach(type -> {
      for (SchemaValidationRule rule : rules) {
        rule.check(type, validationErrorCollector);
      }
    });
  }

  private void traverse(GraphQLOutputType root,
                        List<SchemaValidationRule> rules,
                        SchemaValidationErrorCollector validationErrorCollector) {
    if (processed.contains(root)) {
      return;
    }
    processed.add(root);
    if (root instanceof GraphQLFieldsContainer) {
      // this deliberately has open field visibility here since its validating the schema
      // when completely open
      for (GraphQLFieldDefinition fieldDefinition : ((GraphQLFieldsContainer)root).getFieldDefinitions()) {
        for (SchemaValidationRule rule : rules) {
          rule.check(fieldDefinition, validationErrorCollector);
        }
        traverse(fieldDefinition.getType(), rules, validationErrorCollector);
      }
    }
  }
}
