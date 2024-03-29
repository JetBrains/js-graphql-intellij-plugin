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
package com.intellij.lang.jsgraphql.types.schema;

import com.intellij.lang.jsgraphql.types.PublicApi;

import java.util.List;
import java.util.Map;

/**
 * This class allows you to retrieve the selection set of fields that have been asked for when the
 * {@link DataFetcher} was invoked.
 * <p>
 * For example imagine we are fetching the field 'user' in the following query
 *
 * <pre>
 * {@code
 *
 *  {
 *      user {
 *          name
 *          age
 *          weight
 *          friends {
 *              name
 *          }
 *      }
 *  }
 * }
 * </pre>
 * <p>
 * The selection set in the case above consists of the fields "name, age, weight, friends and friends/name".
 * <p>
 * You can use this selection set perhaps to "peek" ahead and decide that field values you might need
 * from the underlying data system.  Imagine a SQL system where this might represent the SQL 'projection'
 * of columns say.
 * <p>
 * However composite types such as Interfaces and Unions add some complexity.  You cant know
 * ahead of time the exact field and object types involved.  There in fact be multiple possible `conditional` fields.
 * <p>
 * This class represents this by returning a list of fields and having two addressing mechanisms,
 * a simple `x/y` one and the more specific `Foo.x/Bar.y` mechanism.
 * <p>
 * For example imagine a `Pet` interface type that has `Cat` and `Dog` object type implementations.  The query might
 * be:
 *
 * <pre>
 * {@code
 *  {
 *      pet {
 *          name
 *      }
 *  }
 * }
 * </pre>
 * <p>
 * In the example above you have a `Cat.name`and `Dog.name` as possible sub selections of the `pet` field.  They are can be addressed by
 * either `name` or `Dog.name` or `Cat.name`
 *
 * <pre>
 * {@code
 *  selectionSet.contains("name") == true
 *  selectionSet.contains("Dog.name", "Cat.name") == true
 *
 *  List<SelectedField> petNames = selectionSet.getFields("name")
 *  petNames.size() == 2
 *
 *  List<SelectedField> dogNames = selectionSet.getFields("Dog.name")
 *  dogNames.size() == 1
 * }
 * </pre>
 * <p>
 * The simple naming is easier to work with but the type prefixed naming is more precise.
 * <p>
 * Another complication is any field aliasing that a client can specify.
 *
 * <pre>
 * {@code
 *  {
 *      pet {
 *          name(arg : "foo")
 *          ... on Dog {
 *             aliasedName : name(arg : "bar")
 *          }
 *     }
 *  }
 * }
 * </pre>
 * <p>
 * In the example above the `selectionSet.getFields("name")` actually returns three {@link com.intellij.lang.jsgraphql.types.schema.SelectedField}s,
 * one for `Dog.name`, one for `Cat.name` and one for `Dog.name` with an alias of `aliasedName`.  The arguments can
 * differ on {@link com.intellij.lang.jsgraphql.types.schema.SelectedField}s that have different {@link SelectedField#getResultKey()}s, hence the multiple
 * selected fields returned.
 * <p>
 * To help you there is the {@link #getFieldsGroupedByResultKey()} that returns a {@code Map<String,List<SelectedField>>} keyed
 * by result key, that is by the field alias or by the field name.
 */
@PublicApi
public interface DataFetchingFieldSelectionSet {

  /**
   * This will return true if the field selection set matches a specified "glob" pattern matching ie
   * the glob pattern matching supported by {@link java.nio.file.FileSystem#getPathMatcher}.
   * <p>
   * This will allow you to use '*', '**' and '?' as special matching characters such that "invoice/customer*" would
   * match an invoice field with child fields that start with 'customer'.
   *
   * @param fieldGlobPattern the glob pattern to match fields against
   * @return true if the selection set contains these fields
   * @see java.nio.file.FileSystem#getPathMatcher(String)
   */
  boolean contains(String fieldGlobPattern);

  /**
   * This will return true if the field selection set matches any of the specified "glob" pattern matches ie
   * the glob pattern matching supported by {@link java.nio.file.FileSystem#getPathMatcher}.
   * <p>
   * This will allow you to use '*', '**' and '?' as special matching characters such that "invoice/customer*" would
   * match an invoice field with child fields that start with 'customer'.
   *
   * @param fieldGlobPattern  the glob pattern to match fields against
   * @param fieldGlobPatterns optionally more glob pattern to match fields against
   * @return true if the selection set contains any of these these fields
   * @see java.nio.file.FileSystem#getPathMatcher(String)
   */
  boolean containsAnyOf(String fieldGlobPattern, String... fieldGlobPatterns);

  /**
   * This will return true if the field selection set matches all of the specified "glob" pattern matches ie
   * the glob pattern matching supported by {@link java.nio.file.FileSystem#getPathMatcher}.
   * <p>
   * This will allow you to use '*', '**' and '?' as special matching characters such that "invoice/customer*" would
   * match an invoice field with child fields that start with 'customer'.
   *
   * @param fieldGlobPattern  the glob pattern to match fields against
   * @param fieldGlobPatterns optionally more glob pattern to match fields against
   * @return true if the selection set contains all of these these fields
   * @see java.nio.file.FileSystem#getPathMatcher(String)
   */
  boolean containsAllOf(String fieldGlobPattern, String... fieldGlobPatterns);

  /**
   * This will return all selected fields.
   * <p>
   * The fields are guaranteed to be in pre-order as they appear in the query.
   *
   * @return a list of all selected fields or empty list if none match
   */
  List<SelectedField> getFields();

  /**
   * This will return all selected fields that are immediate child fields
   * of the field being fetched.
   * <p>
   * The fields are guaranteed to be in pre-order as they appear in the query.
   *
   * @return a list of all selected immediate child fields or empty list if none match
   */
  List<SelectedField> getImmediateFields();

  /**
   * This will return a list of selected fields that match a specified "glob" pattern matching ie
   * the glob pattern matching supported by {@link java.nio.file.FileSystem#getPathMatcher}.
   * <p>
   * This will allow you to use '*', '**' and '?' as special matching characters such that "invoice/customer*" would
   * match an invoice field with child fields that start with 'customer'.
   * <p>
   * The fields are guaranteed to be in pre-order as they appear in the query.
   *
   * @param fieldGlobPattern  the glob pattern to match fields against
   * @param fieldGlobPatterns optionally more glob pattern to match fields against
   * @return a list of selected fields or empty list if none match
   */
  List<SelectedField> getFields(String fieldGlobPattern, String... fieldGlobPatterns);

  /**
   * The result key of a selected field represents what the graphql return value will be.  The same {@link com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition}
   * may lead to a field being asked for multiple times (with differing arguments) if field aliases are used.  This method
   * helps you get all possible field invocations grouped by their result key.  The arguments are guaranteed to be the same if
   * the result key is the same, otherwise the query would not have validated correctly.
   *
   * @return a map of selected fields grouped by result key or an empty map if none match
   */
  Map<String, List<SelectedField>> getFieldsGroupedByResultKey();

  /**
   * The result key of a selected field represents what the graphql return value will be.  The same {@link com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition}
   * may lead to a field being asked for multiple times (with differing arguments) if field aliases are used.  This method
   * helps you get all possible field invocations grouped by their result key.  The arguments are guaranteed to be the same if
   * the result key is the same, otherwise the query would not have validated correctly.
   *
   * @param fieldGlobPattern  the glob pattern to match fields against
   * @param fieldGlobPatterns optionally more glob pattern to match fields against
   * @return a map of selected fields grouped by result key or an empty map if none match
   */
  Map<String, List<SelectedField>> getFieldsGroupedByResultKey(String fieldGlobPattern, String... fieldGlobPatterns);
}
