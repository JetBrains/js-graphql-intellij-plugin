package com.intellij.lang.jsgraphql.types.language;

import com.google.common.collect.ImmutableList;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.collect.ImmutableKit;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.emptyList;

@PublicApi
public class ScalarTypeExtensionDefinition extends ScalarTypeDefinition {

    @Internal
    protected ScalarTypeExtensionDefinition(String name,
                                            List<Directive> directives,
                                            Description description,
                                            SourceLocation sourceLocation,
                                            List<Comment> comments,
                                            IgnoredChars ignoredChars,
                                            Map<String, String> additionalData,
                                            @Nullable PsiElement element,
                                            @Nullable List<? extends Node> sourceNodes) {
        super(name, directives, description, sourceLocation, comments, ignoredChars, additionalData, element, sourceNodes);
    }

    @Override
    public ScalarTypeExtensionDefinition deepCopy() {
        return new ScalarTypeExtensionDefinition(getName(), deepCopy(getDirectives()), getDescription(), getSourceLocation(), getComments(), getIgnoredChars(), getAdditionalData(), getElement(), getSourceNodes());
    }

    @Override
    public String toString() {
        return "ScalarTypeExtensionDefinition{" +
            "name='" + getName() + '\'' +
            ", directives=" + getDirectives() +
            '}';

    }

    public static Builder newScalarTypeExtensionDefinition() {
        return new Builder();
    }

    @Override
    public ScalarTypeExtensionDefinition withNewChildren(NodeChildrenContainer newChildren) {
        return transformExtension(builder -> builder
            .directives(newChildren.getChildren(CHILD_DIRECTIVES))
        );
    }

    public ScalarTypeExtensionDefinition transformExtension(Consumer<Builder> builderConsumer) {
        Builder builder = new Builder(this);
        builderConsumer.accept(builder);
        return builder.build();
    }

    public static final class Builder implements NodeDirectivesBuilder {
        private SourceLocation sourceLocation;
        private ImmutableList<Comment> comments = emptyList();
        private String name;
        private Description description;
        private ImmutableList<Directive> directives = emptyList();
        private IgnoredChars ignoredChars = IgnoredChars.EMPTY;
        private Map<String, String> additionalData = new LinkedHashMap<>();
        private @Nullable PsiElement element;
        private @Nullable List<? extends Node> sourceNodes;

        private Builder() {
        }

        private Builder(ScalarTypeDefinition existing) {
            this.sourceLocation = existing.getSourceLocation();
            this.comments = ImmutableList.copyOf(existing.getComments());
            this.name = existing.getName();
            this.description = existing.getDescription();
            this.directives = ImmutableList.copyOf(existing.getDirectives());
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

        public Builder description(Description description) {
            this.description = description;
            return this;
        }

        @Override
        public Builder directives(List<Directive> directives) {
            this.directives = ImmutableList.copyOf(directives);
            return this;
        }

        public Builder directive(Directive directive) {
            this.directives = ImmutableKit.addToList(directives, directive);
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

        public ScalarTypeExtensionDefinition build() {
            return new ScalarTypeExtensionDefinition(name,
                directives,
                description,
                sourceLocation,
                comments,
                ignoredChars,
                additionalData,
                element,
                sourceNodes);
        }
    }
}