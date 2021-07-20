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
package com.intellij.lang.jsgraphql.types.schema.idl;

import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.InvalidSyntaxError;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.Definition;
import com.intellij.lang.jsgraphql.types.language.Document;
import com.intellij.lang.jsgraphql.types.language.SDLDefinition;
import com.intellij.lang.jsgraphql.types.parser.InvalidSyntaxException;
import com.intellij.lang.jsgraphql.types.parser.Parser;
import com.intellij.lang.jsgraphql.types.schema.idl.errors.NonSDLDefinitionError;
import com.intellij.lang.jsgraphql.types.schema.idl.errors.SchemaProblem;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.nio.charset.Charset.defaultCharset;

/**
 * This can take a graphql schema definition and parse it into a {@link TypeDefinitionRegistry} of
 * definitions ready to be placed into {@link SchemaGenerator} say
 */
@PublicApi
public class SchemaParser {

    /**
     * Parse a file of schema definitions and create a {@link TypeDefinitionRegistry}
     *
     * @param file the file to parse
     *
     * @return registry of type definitions
     *
     * @throws SchemaProblem if there are problems compiling the schema definitions
     */
    public TypeDefinitionRegistry parse(File file) throws SchemaProblem {
        try {
            return parse(Files.newBufferedReader(file.toPath(), defaultCharset()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parse a inputStream of schema definitions and create a {@link TypeDefinitionRegistry}
     *
     * @param inputStream the inputStream to parse
     *
     * @return registry of type definitions
     *
     * @throws SchemaProblem if there are problems compiling the schema definitions
     */
    public TypeDefinitionRegistry parse(InputStream inputStream) throws SchemaProblem {
        return parse(new InputStreamReader(inputStream));
    }

    /**
     * Parse a reader of schema definitions and create a {@link TypeDefinitionRegistry}
     *
     * @param reader the reader to parse
     *
     * @return registry of type definitions
     *
     * @throws SchemaProblem if there are problems compiling the schema definitions
     */
    public TypeDefinitionRegistry parse(Reader reader) throws SchemaProblem {
        try (Reader input = reader) {
            return parseImpl(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parse a string of schema definitions and create a {@link TypeDefinitionRegistry}
     *
     * @param schemaInput the schema string to parse
     *
     * @return registry of type definitions
     *
     * @throws SchemaProblem if there are problems compiling the schema definitions
     */
    public TypeDefinitionRegistry parse(String schemaInput) throws SchemaProblem {
        return parseImpl(new StringReader(schemaInput));
    }

    public TypeDefinitionRegistry parseImpl(Reader schemaInput) {
        try {
            Parser parser = new Parser();
            Document document = parser.parseDocument(schemaInput);

            return buildRegistry(document);
        } catch (InvalidSyntaxException e) {
            throw handleParseException(e.toInvalidSyntaxError());
        }
    }

    private SchemaProblem handleParseException(InvalidSyntaxError invalidSyntaxError) throws RuntimeException {
        return new SchemaProblem(Collections.singletonList(invalidSyntaxError));
    }

    /**
     * special method to build directly a TypeDefinitionRegistry from a Document
     * useful for Introspection =&gt; IDL (Document) =&gt; TypeDefinitionRegistry
     *
     * @param document containing type definitions
     *
     * @return the TypeDefinitionRegistry containing all type definitions from the document
     *
     * @throws SchemaProblem if an error occurs
     */
    public TypeDefinitionRegistry buildRegistry(Document document) {
        List<GraphQLError> errors = new ArrayList<>();
        TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry();
        List<Definition> definitions = document.getDefinitions();
        for (Definition definition : definitions) {
            if (definition instanceof SDLDefinition) {
                typeRegistry.add((SDLDefinition) definition);
            } else {
                errors.add(new NonSDLDefinitionError(definition));
            }
        }

        if (errors.size() > 0) {
            typeRegistry.addError(new SchemaProblem(errors));
        }
        return typeRegistry;
    }
}
