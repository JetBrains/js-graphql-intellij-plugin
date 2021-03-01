package com.intellij.lang.jsgraphql.types.schema;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.util.NodeAdapter;
import com.intellij.lang.jsgraphql.types.util.NodeLocation;

import java.util.List;
import java.util.Map;

@Internal
public class GraphQLSchemaElementAdapter implements NodeAdapter<GraphQLSchemaElement> {

    public static final GraphQLSchemaElementAdapter SCHEMA_ELEMENT_ADAPTER = new GraphQLSchemaElementAdapter();

    private GraphQLSchemaElementAdapter() {

    }

    @Override
    public Map<String, List<GraphQLSchemaElement>> getNamedChildren(GraphQLSchemaElement node) {
        return node.getChildrenWithTypeReferences().getChildren();
    }

    @Override
    public GraphQLSchemaElement withNewChildren(GraphQLSchemaElement node, Map<String, List<GraphQLSchemaElement>> newChildren) {
        SchemaElementChildrenContainer childrenContainer = SchemaElementChildrenContainer.newSchemaElementChildrenContainer(newChildren).build();
        return node.withNewChildren(childrenContainer);
    }

    @Override
    public GraphQLSchemaElement removeChild(GraphQLSchemaElement node, NodeLocation location) {
        SchemaElementChildrenContainer children = node.getChildrenWithTypeReferences();
        SchemaElementChildrenContainer newChildren = children.transform(builder -> builder.removeChild(location.getName(), location.getIndex()));
        return node.withNewChildren(newChildren);
    }
}
