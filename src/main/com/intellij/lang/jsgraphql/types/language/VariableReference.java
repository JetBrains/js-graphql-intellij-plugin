package com.intellij.lang.jsgraphql.types.language;


import com.google.common.collect.ImmutableList;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.util.TraversalControl;
import com.intellij.lang.jsgraphql.types.util.TraverserContext;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.emptyList;
import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.emptyMap;
import static com.intellij.lang.jsgraphql.types.language.NodeChildrenContainer.newNodeChildrenContainer;
import static com.intellij.lang.jsgraphql.types.language.NodeUtil.assertNewChildrenAreEmpty;

@PublicApi
public class VariableReference extends AbstractNode<VariableReference> implements Value<VariableReference>, NamedNode<VariableReference> {

    private final String name;

    @Internal
    protected VariableReference(String name,
                                SourceLocation sourceLocation,
                                List<Comment> comments,
                                IgnoredChars ignoredChars,
                                Map<String, String> additionalData,
                                @Nullable PsiElement element,
                                @Nullable List<? extends Node> sourceNodes) {
        super(sourceLocation, comments, ignoredChars, additionalData, element, sourceNodes);
        this.name = name;
    }

    /**
     * alternative to using a Builder for convenience
     *
     * @param name of the variable
     */
    public VariableReference(String name) {
        this(name, null, emptyList(), IgnoredChars.EMPTY, emptyMap(), null, null);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Node> getChildren() {
        return emptyList();
    }

    @Override
    public NodeChildrenContainer getNamedChildren() {
        return newNodeChildrenContainer().build();
    }

    @Override
    public VariableReference withNewChildren(NodeChildrenContainer newChildren) {
        assertNewChildrenAreEmpty(newChildren);
        return this;
    }

    @Override
    public boolean isEqualTo(Node o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        VariableReference that = (VariableReference) o;

        return Objects.equals(this.name, that.name);
    }

    @Override
    public VariableReference deepCopy() {
        return new VariableReference(name, getSourceLocation(), getComments(), getIgnoredChars(), getAdditionalData(), getElement(), getSourceNodes());
    }

    @Override
    public String toString() {
        return "VariableReference{" +
            "name='" + name + '\'' +
            '}';
    }

    @Override
    public TraversalControl accept(TraverserContext<Node> context, NodeVisitor visitor) {
        return visitor.visitVariableReference(this, context);
    }

    public static Builder newVariableReference() {
        return new Builder();
    }

    public VariableReference transform(Consumer<Builder> builderConsumer) {
        Builder builder = new Builder(this);
        builderConsumer.accept(builder);
        return builder.build();
    }

    public static final class Builder implements NodeBuilder {
        private SourceLocation sourceLocation;
        private ImmutableList<Comment> comments = emptyList();
        private String name;
        private IgnoredChars ignoredChars = IgnoredChars.EMPTY;
        private Map<String, String> additionalData = new LinkedHashMap<>();
        private @Nullable PsiElement element;
        private @Nullable List<? extends Node> sourceNodes;

        private Builder() {
        }

        private Builder(VariableReference existing) {
            this.sourceLocation = existing.getSourceLocation();
            this.comments = ImmutableList.copyOf(existing.getComments());
            this.name = existing.getName();
            this.ignoredChars = existing.getIgnoredChars();
            this.additionalData = new LinkedHashMap<>(existing.getAdditionalData());
            this.element = existing.getElement();
            this.sourceNodes = existing.getSourceNodes();
        }

        public Builder sourceLocation(SourceLocation sourceLocation) {
            this.sourceLocation = sourceLocation;
            return this;
        }

        public Builder comments(List<Comment> comments) {
            this.comments = ImmutableList.copyOf(comments);
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder ignoredChars(IgnoredChars ignoredChars) {
            this.ignoredChars = ignoredChars;
            return this;
        }

        public Builder additionalData(Map<String, String> additionalData) {
            this.additionalData = assertNotNull(additionalData);
            return this;
        }

        public Builder additionalData(String key, String value) {
            this.additionalData.put(key, value);
            return this;
        }

        public Builder element(@Nullable PsiElement element) {
            this.element = element;
            return this;
        }

        public Builder sourceNodes(@Nullable List<? extends Node> sourceNodes) {
            this.sourceNodes = sourceNodes;
            return this;
        }

        public VariableReference build() {
            return new VariableReference(name, sourceLocation, comments, ignoredChars, additionalData, element, sourceNodes);
        }
    }
}