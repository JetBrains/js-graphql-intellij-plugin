package com.intellij.lang.jsgraphql.types.schema.transform;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.schema.GraphQLNamedSchemaElement;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchemaElement;

/**
 * Container to pass additional context about a schema element (ie., field) to {@link VisibleFieldPredicate}.
 */
@PublicApi
public interface VisibleFieldPredicateEnvironment {

    GraphQLNamedSchemaElement getSchemaElement();

    /**
     * Get the element's immediate parent node.
     *
     * @return parent node
     */
    GraphQLSchemaElement getParentElement();

    class VisibleFieldPredicateEnvironmentImpl implements VisibleFieldPredicateEnvironment {

        private final GraphQLNamedSchemaElement schemaElement;
        private final GraphQLSchemaElement parentElement;

        public VisibleFieldPredicateEnvironmentImpl(GraphQLNamedSchemaElement schemaElement,
                                                    GraphQLSchemaElement parentElement) {
            this.schemaElement = schemaElement;
            this.parentElement = parentElement;
        }

        @Override
        public GraphQLNamedSchemaElement getSchemaElement() {
            return schemaElement;
        }

        @Override
        public GraphQLSchemaElement getParentElement() {
            return parentElement;
        }
    }
}
