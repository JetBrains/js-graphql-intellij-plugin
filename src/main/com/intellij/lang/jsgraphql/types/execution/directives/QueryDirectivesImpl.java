package com.intellij.lang.jsgraphql.types.execution.directives;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.execution.MergedField;
import com.intellij.lang.jsgraphql.types.language.Directive;
import com.intellij.lang.jsgraphql.types.language.Field;
import com.intellij.lang.jsgraphql.types.schema.GraphQLDirective;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

/**
 * These objects are ALWAYS in the context of a single MergedField
 *
 * Also note we compute these values lazily
 */
@Internal
public class QueryDirectivesImpl implements QueryDirectives {

    private final DirectivesResolver directivesResolver = new DirectivesResolver();
    private final MergedField mergedField;
    private final GraphQLSchema schema;
    private final Map<String, Object> variables;
    private volatile ImmutableMap<Field, List<GraphQLDirective>> fieldDirectivesByField;
    private volatile ImmutableMap<String, List<GraphQLDirective>> fieldDirectivesByName;

    public QueryDirectivesImpl(MergedField mergedField, GraphQLSchema schema, Map<String, Object> variables) {
        this.mergedField = mergedField;
        this.schema = schema;
        this.variables = variables;
    }

    private void computeValuesLazily() {
        synchronized (this) {
            if (fieldDirectivesByField != null) {
                return;
            }

            final Map<Field, List<GraphQLDirective>> byField = new LinkedHashMap<>();
            mergedField.getFields().forEach(field -> {
                List<Directive> directives = field.getDirectives();
                ImmutableList<GraphQLDirective> resolvedDirectives = ImmutableList.copyOf(
                        directivesResolver
                                .resolveDirectives(directives, schema, variables)
                                .values()
                );
                byField.put(field, resolvedDirectives);
            });

            Map<String, List<GraphQLDirective>> byName = new LinkedHashMap<>();
            byField.forEach((field, directiveList) -> directiveList.forEach(directive -> {
                String name = directive.getName();
                byName.computeIfAbsent(name, k -> new ArrayList<>());
                byName.get(name).add(directive);
            }));

            this.fieldDirectivesByName = ImmutableMap.copyOf(byName);
            this.fieldDirectivesByField = ImmutableMap.copyOf(byField);
        }
    }


    @Override
    public Map<Field, List<GraphQLDirective>> getImmediateDirectivesByField() {
        computeValuesLazily();
        return fieldDirectivesByField;
    }

    @Override
    public Map<String, List<GraphQLDirective>> getImmediateDirectivesByName() {
        computeValuesLazily();
        return fieldDirectivesByName;
    }

    @Override
    public List<GraphQLDirective> getImmediateDirective(String directiveName) {
        computeValuesLazily();
        return getImmediateDirectivesByName().getOrDefault(directiveName, emptyList());
    }
}
