package com.intellij.lang.jsgraphql.types.execution.directives;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.Field;
import com.intellij.lang.jsgraphql.types.schema.GraphQLDirective;

import java.util.List;
import java.util.Map;

/**
 * This gives you access to the immediate directives on a {@link com.intellij.lang.jsgraphql.types.execution.MergedField}.  This does not include directives on parent
 * fields or fragment containers.
 * <p>
 * Because a {@link com.intellij.lang.jsgraphql.types.execution.MergedField} can actually have multiple fields and hence
 * directives on each field instance its possible that there is more than one directive named "foo"
 * on the merged field.  How you decide which one to use is up to your code.
 * <p>
 * NOTE: A future version of the interface will try to add access to the inherited directives from
 * parent fields and fragments.  This proved to be a non trivial problem and hence we decide
 * to give access to immediate field directives and provide this holder interface so we can
 * add the other directives in the future
 *
 * @see com.intellij.lang.jsgraphql.types.execution.MergedField
 */
@PublicApi
public interface QueryDirectives {

    /**
     * This will return a map of the directives that are immediately on a merged field
     *
     * @return a map of all the directives immediately on this merged field
     */
    Map<String, List<GraphQLDirective>> getImmediateDirectivesByName();


    /**
     * This will return a list of the named directives that are immediately on this merged field.
     *
     * Read above for why this is a list of directives and not just one
     *
     * @param directiveName the named directive
     *
     * @return a list of the named directives that are immediately on this merged field
     */
    List<GraphQLDirective> getImmediateDirective(String directiveName);

    /**
     * This will return a map of the {@link com.intellij.lang.jsgraphql.types.language.Field}s inside a {@link com.intellij.lang.jsgraphql.types.execution.MergedField}
     * and the immediate directives that are on each specific field
     *
     * @return a map of all directives on each field inside this
     */
    Map<Field, List<GraphQLDirective>> getImmediateDirectivesByField();
}
