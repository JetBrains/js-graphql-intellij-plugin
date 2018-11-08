# Developer Guide for JS GraphQL IntelliJ Plugin v2

This developer guide covers how to setup your project to get the most out of the GraphQL language tooling in this plugin.

The main features of the plugin are:

- Schema-aware completion, error highlighting, and documentation
- Completion and error highlighting for schema authoring using the GraphQL Type System Definition Language (SDL)
- Syntax highlighting, code-formatting, folding, commenter, and brace-matching
- 'Find Usages' and 'Go to Declaration' for schema types, fields, and arguments
- Execute queries with variables against configurable GraphQL endpoints
- Introspect GraphQL endpoints to generate schema declaration files using the GraphQL Type System Definition Language 
- Support for multi-schema projects using configurable project scopes or graphql-config files 

The most important aspect of using the plugin is to configure how schema types are discovered.
If the schema types are not discovered correctly, language features such as completion and error highlighting
will be based on the wrong type information.

Schemas and their types are declared using [GraphQL Type System Definition Language](http://facebook.github.io/graphql/June2018/#sec-Type-System)
which is also widely known as GraphQL Schema Definition Language (often abbreviated as SDL).

If you're authoring your schemas in SDL, the plugin provides the following features:

- Completion on types when defining fields, arguments, implemented interfaces etc.
- Error highlighting of schema errors such as unknown types, wrong use of types, missing fields when implementing interfaces
- Find usages in SDL and refactoring such as rename which will update the relevant queries, mutations etc.

For use cases where you don't declare the schema in the project, an introspection query can be executed against a
GraphQL endpoint URL to write the schema to a `.graphql` file as SDL. See [Working with GraphQL Endpoints](#working-with-graphql-endpoints).

## Project Structure and Schema Discovery
By default, the plugin assumes that your project only contains a single schema. If this is the case, you don't need
to take any action in terms of schema discovery. For a single-schema project, schema types are discovered as follows:

- All .graphql files in the "Project files" scope are processed for type definitions which are added to a singleton type registry
- If the IDE has JavaScript language support, injected GraphQL strings in the "Project files" scope are processed for all
  JavaScript file types. File extensions include `.js`, `.jsx`, `.ts`, and `.tsx`. Injected GraphQL is found based on
  Tagged Template Literals with one of the following tags: `graphql`, `.gql`, and `Relay.QL`.

For projects with multiple schemas, developers have to configure a scope for each schema. The purpose of a schema-specific
scope is to prevent types from being picked up in more than one GraphQL type registry, which would likely result in validation
errors as types appear to have been declared more than once. In addition, the scopes prevent non-conflicting types from
showing up in completions and ensure that validation only recognizes the types that belong to the current schema.

The plugin allows you to configure your schema scopes in two different ways:

- Using the IDE "Settings" page called "Appearance & Behaviour" > ["Scopes"](https://www.jetbrains.com/help/idea/2018.1/settings-scopes.html) with patterns for inclusion and exclusion
- Using [graphql-config](https://github.com/prismagraphql/graphql-config) configuration files with `includes` and `excludes` glob patterns 

### Setting up Multi-schema Projects using "Appearance & Behaviour" > "Scopes"
In a multi-schema project, each schema needs its own separate scope.

The recommended structure for multi-schema projects is to place each schema in a separate project module or folder.

For example:

```
- project root/
    - product a (schema one)/
        - schema files and graphql aware components
    - product b (schema two)/
        - schema files and graphql aware components
```

With this structure there is no need to use exclude patterns.

To setup the project, Open "Settings" > "Language & Frameworks" > "GraphQL" and select the "Multiple schemas".

The scopes can be created via the "Edit scopes" link:

- Click "Add scope"
- Click Shared scope
- Name it the same as the folder
- Select "Project" in the drop down above the project tree
- Select the "product a (schema one)" folder and click the "Include Recursively" button on the right
- Repeat for other schemas

Given a file which contains GraphQL, the plugin finds the first matching schema scope based on the file name, and then
proceeds with schema discovery by processing only the files that the scope accepts.

__Points to consider__:
- You don't have to restrict the patterns to specific file extensions since the plugin only searches relevant file types
- If you do limit to file extensions, make sure you include file extensions for components that use injected GraphQL, e.g. `.jsx`
- When adding scopes, use the "Shared" scopes to create scopes that can be checked into source control and used by other
  developers on the project
- If you have created scopes for uses other than GraphQL schema discovery, you should place the GraphQL scopes at the top
  of the list since the first matching scope is used by this plugin  

### Setting up Multi-schema Projects using graphql-config
The second option for multi-schema projects is graphql-config. With graphql-config you don't get the scopes UI which
displays the files in the scopes, but it may be the better option if you use other tools that support graphql-config.

To setup the project, Open "Settings" > "Language & Frameworks" > "GraphQL" and select the "graphql-config".

Please familiarize yourself with the [graphql-config format](https://github.com/prismagraphql/graphql-config/blob/master/specification.md)
before proceeding.

The next step is to decide where to place the `.graphqlconfig` file. The config file controls schema discovery from the
directory it's placed in, as well as any sub folders that don't have their own `.graphqlconfig`.
 
To create a `.graphqlconfig` file, right click a folder and select "New GraphQL Configuration File".

Depending on your preference, you can use a single `.graphqlconfig` file in a folder that is a parent to each schema
folder, or you can place `.graphqlconfig` files in each schema folder.

__Option A: Single config file:__

```
- project root/
    - .graphqlconfig <-----
    - product a (schema one)/
        - schema files and graphql aware components
    - product b (schema two)/
        - schema files and graphql aware components
```

With a single config file you need to separate the schemas using the `includes` globs of the `projects` field:

```
{
  "projects": {
    "product a": {
      "includes": ["product a (schema one)/**"]
    },
    "product b": {
      "includes": ["product b (schema two)/**"}
    }
  }
}
```

__Option B: Multiple config files:__

```
- project root/
    - product a (schema one)/
        - .graphqlconfig <-----
        - schema files and graphql aware components
    - product b (schema two)/
        - .graphqlconfig <-----
        - schema files and graphql aware components
```

With this approach the location of the config files creates separate scopes for the two schemas. 

### ---- TODO BELOW THIS LINE ----

### Working with GraphQL Endpoints


## Breaking changes from v1

## Troublshooting