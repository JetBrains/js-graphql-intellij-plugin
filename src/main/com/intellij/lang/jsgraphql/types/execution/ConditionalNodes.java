package com.intellij.lang.jsgraphql.types.execution;

import com.intellij.lang.jsgraphql.types.Assert;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.VisibleForTesting;
import com.intellij.lang.jsgraphql.types.language.Directive;
import com.intellij.lang.jsgraphql.types.language.NodeUtil;

import java.util.List;
import java.util.Map;

import static com.intellij.lang.jsgraphql.types.Directives.IncludeDirective;
import static com.intellij.lang.jsgraphql.types.Directives.SkipDirective;

@Internal
public class ConditionalNodes {

    @VisibleForTesting
    ValuesResolver valuesResolver = new ValuesResolver();

    public boolean shouldInclude(Map<String, Object> variables, List<Directive> directives) {
        boolean skip = getDirectiveResult(variables, directives, SkipDirective.getName(), false);
        boolean include = getDirectiveResult(variables, directives, IncludeDirective.getName(), true);
        return !skip && include;
    }

    private boolean getDirectiveResult(Map<String, Object> variables, List<Directive> directives, String directiveName, boolean defaultValue) {
        Directive foundDirective = NodeUtil.findNodeByName(directives, directiveName);
        if (foundDirective != null) {
            Map<String, Object> argumentValues = valuesResolver.getArgumentValues(SkipDirective.getArguments(), foundDirective.getArguments(), variables);
            Object flag = argumentValues.get("if");
            Assert.assertTrue(flag instanceof Boolean, () -> String.format("The '%s' directive MUST have a value for the 'if' argument", directiveName));
            return (Boolean) flag;
        }
        return defaultValue;
    }

}
