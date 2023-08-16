# GraphQL IntelliJ Plugin

GraphQL language support for [WebStorm](https://www.jetbrains.com/webstorm/), [IntelliJ IDEA](https://www.jetbrains.com/idea/) and all other
[IDEs](https://www.jetbrains.com/products/#type=ide). The plugin works with all IDEs in the IntelliJ Platform.

# Table of Contents

* [Prerequisites](#prerequisites)
* [Installation](#installation)
* [Features](#features)
* [How to use](#how-to-use)
* [Configuration](#configuration)
    * [Basic configuration](#basic-configuration)
    * [Composite schema](#composite-schema)
    * [Remote schemas](#remote-schemas)
    * [Local introspection schemas](#local-introspection-schemas)
    * [Advanced configuration](#advanced-configuration)
    * [Multiple projects](#multiple-projects)
        * [Config per project](#config-per-project)
        * [Single config](#single-config)
    * [Configuration files](#configuration-files)
        * [Yaml](#yaml)
        * [JavaScript](#javascript)
        * [TypeScript](#typescript)
    * [Migration](#migration)
    * [Legacy configuration](#legacy-configuration)
    * [Environment variables](#environment-variables)
        * [.env files](#env-files)
        * [Manual configuration](#manual-configuration)
        * [System](#system)
* [Frameworks](#frameworks)
* [Introspection](#introspection)
    * [Rerun latest introspection](#rerun-latest-introspection)
* [Queries](#queries)
    * [Scratch files](#scratch-files)
* [Toolbar](#toolbar)
* [Tool window](#tool-window)
* [Injections](#injections)
* [Acknowledgements](#acknowledgements)
* [License](#license)

# Prerequisites

The plugin and this documentation assume you are already familiar with the GraphQL language. If you're not, please visit the official
[graphql.org](https://graphql.org/) website first.
The plugin works out of the box with popular GraphQL clients such as [Apollo GraphQL](https://www.apollographql.com/) and
[Relay](https://facebook.github.io/relay/), but you're free to choose your client and server framework.

# Installation

The plugin is available from [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/8097-js-graphql).
You can install it directly from your IDE via the `File | Settings/Preferences | Plugins` screen.
On the `Marketplace` tab simply search for `graphql` and select the `GraphQL` suggestion:

![marketplace](docs/assets/marketplace.png)

# Features

The main features of this plugin include:

- Full language support for GraphQL Specification including the Schema Definition Language (SDL).
- Schema-aware completion, error highlighting, and documentation.
- Syntax highlighting, code-formatting, folding, commenter, and brace-matching.
- The plugin [discovers your local schema](#configuration) on the fly. Remote schemas are easily fetched using
  introspection. You can introspect GraphQL endpoints to generate schema declaration files using the GraphQL Type System Definition
  Language.
- Support for [multi-schema projects](#multiple-projects) using configurable project scopes or
  `graphql-config` files. Schema discovery is configured using [graphql-config](https://the-guild.dev/graphql/config/docs)
  files, which includes support for multi-schema projects.
- Built-in support for [Relay](https://facebook.github.io/relay/) and [Apollo](https://www.apollographql.com/) projects:
  `graphql` and `gql` tagged template literals in JavaScript and TypeScript are automatically recognized as GraphQL.
- Execute queries using variables against configurable endpoints, including support for custom headers and environment variables.
- `Find Usages` and `Go to Declaration` for schema types, fields, and fragments.
- `Structure view` to navigate GraphQL files.
- Load variables from shell, `.env` files or setup them manually per configuration file.
- Built-in `Relay`, `Federation`, and `Apollo Kotlin` type definitions (You need to enable it
  in `Preferences / Settings | Languages & Frameworks | GraphQL`).

# How to use

This developer guide covers how to set up your project to get the most out of the GraphQL language tooling in this plugin.

It is important to configure how the schema types are discovered. If the schema types are not discovered correctly, language features such
as completion and error highlighting will be based on the wrong type information.

Schemas and their types are declared using GraphQL Type System Definition Language, which is also widely known as GraphQL Schema Definition
Language (often abbreviated as SDL). If you're authoring your schemas in SDL, the plugin provides the following features:

- Completion for types when defining fields, arguments, implemented interfaces, and so on.
- Error highlighting of schema errors such as unknown types, wrong use of types, and missing fields when implementing interfaces.
- Find usages in SDL and refactoring such as rename, which will update the relevant queries, mutations, and so on.

# Configuration

> GraphQL Config docs could be found [here](https://the-guild.dev/graphql/config/docs).

By default, the plugin assumes that your project contains a single schema. If this is the case, you don't need to perform any actions in
terms of schema discovery. For a single-schema project, schema types are discovered as follows: All `.graphql` files in the `Project files`
scope are processed for type definitions, which are added to a singleton type registry. If the IDE has JavaScript language support, injected
GraphQL strings in the `Project files` scope are processed for all JavaScript file types. File extensions
include `.js`, `.jsx`, `.ts`,`.tsx`, `.html` and html-based files like `.vue`.

For projects with multiple schemas, developers have to configure a scope for each schema. The purpose of a schema-specific scope is to
prevent types from being picked up in more than one GraphQL type registry, which would likely result in validation errors. This is because
these types will appear to have been declared more than once. In addition, the scopes prevent non-conflicting types from showing up in
completions and ensure that validation only recognizes the types that belong to the current schema.

However, it’s recommended to have a simple config in the project root. Otherwise, you will not be able to define a remote URL for making
GraphQL queries directly from the editor.

A documentation describing GraphQL Config itself could be found [here](https://the-guild.dev/graphql/config/docs).
In the following sections, we will discuss how to use it in the context of this plugin.

## Basic configuration

A simple configuration file `graphql.config.yml` can be created using a context action on the directory in the project view
via `New > GraphQL Config`:

```yaml
schema: schema.graphql
documents: '**/*.graphql'
```

Here, we expect a schema to be defined in a local file `schema.graphql`. The `documents` key is defined using a glob pattern that will
include any GraphQL operation in the current or nested directory. By operation, we mean only GraphQL queries and fragments, but not type
definitions.

Please note that paths are relative to the config directory, unless they are explicitly defined as absolute. Therefore, you do not need to
prefix them with `./`. Just `schema.graphql` is sufficient. The same applies to glob patterns.

## Composite schema

GraphQL Config can also assemble multiple modularized schemas into a single GraphQL schema object.

You can specify a list of files:

```yaml
schema:
  - ./foo.graphql
  - ./bar.graphql
  - ./baz.graphql
```

Alternatively, you can use a glob pattern to find and include pieces of schema:

```yaml
schema: ./*.graphql # includes every GraphQL file in current directory
# OR
schema: ./**/*.graphql # includes GraphQL files recursively
```

GraphQL Config looks for those files, reads the files and merges them to produce a GraphQL schema object.

## Remote schemas

If you have a GraphQL endpoint and do not have the local schema file yet, you can define one or more endpoints and make an introspection
query. This will load a schema from the server, convert it to a GraphQL file, and save it in the IDE's cache directory.

Depending on whether you need additional configuration for an endpoint, you can specify it as a string or an object with supplementary keys
containing data such as headers.

```yaml
schema: https://my.api.com/graphql

schema:
  - https://my.api.com/one/graphql
  - https://my.api.com/two/graphql

schema:
  - https://my.api.com/one/graphql:
      headers:
        Authorization: Bearer ${TOKEN}
```

> Pay special attention to the last example; it should have correct indentation.

Now it is required to run an introspection query manually to load a schema from the provided endpoint. You can do this in
[multiple ways](#introspection).

You probably need to authenticate with your remote service to run queries, and apparently you'll do this using HTTP headers and some kind of
token inside them. The best way to provide a token without hardcoding it in the config file is
through [environment variables](#environment-variables). An
example of this is a `TOKEN` variable in the code snippet above.

## Local introspection schemas

If you want to store an introspection result locally, you can configure an endpoint as it was done in the legacy configuration format.
Define one or multiple endpoints in the `endpoints` extension, and then make an introspection query. A file will be saved at the first path
in the corresponding `schema` section, for example, a `local.graphql` file in the example below.

```yaml
schema: local.graphql
extensions:
  endpoints:
    One:
      url: https://my.api.com/one/graphql
      headers:
        Authorization: bearer ${TOKEN}
    Two:
      url: https://my.api.com/two/graphql
```

## Advanced configuration

If you need more fine-grained control over which files should be included in the schema, you can use the optional `include` and `exclude`
keys. First, it checks a candidate file for exclusion. If it's not excluded, the file path is matched against the `include` pattern.

In that example, `schema.graphql` and every file inside the `src` directory except files in `__tests__` will be included in that project.

```yaml
schema: schema.graphql
exclude: 'src/**/__tests__/**'
include: src/**
```

> Remember that all files specified in `schema` or `documents` are included by default.

## Multiple projects

### Config per project

A config file defines a GraphQL "module" root, similar to how `package.json` or similar files do. Even if this file is empty, all files in
that directory and in the nested ones will use a schema associated with that configuration. Therefore, the simplest way to separate
different schemas is to create a configuration file inside each subdirectory, if they are completely independent, as is usually the case
with monorepos. With this approach, the location of the config files creates separate scopes for each subtree.

```
- project root/
    - product a (schema one)/
        - .graphql.config.yml <-----
        - schema files and graphql aware components
    - product b (schema two)/
        - .graphql.config.yml <-----
        - schema files and graphql aware components
```

### Single config

If you prefer to have a single configuration file, you can specify multiple projects in the same file.

```
- project root/
    - .graphql.config.yml <-----
    - frontend (schema one)/
        - schema files and graphql aware components
    - backend (schema two)/
        - schema files and graphql aware components
    - queries/
```

The configuration for that case should appear as follows:

```yaml
projects:
  frontend:
    schema: https://my.api.com/graphql
    documents: frontend/**/*.{graphql,js,ts}
  backend:
    schema: backend/schema.graphql
    documents: backend/**/*.graphql
```

Files are matched against projects in the order in which the projects are defined. Therefore, if a file matches several projects, the first
one will be chosen.

GraphQL operations are matched non-strictly when no `include` or `exclude` keys are defined for a specific project. This means that if a
query or fragment does not match any project explicitly, the file will be associated with the first project that does not have `include`
or `exclude` keys. In the example above, there is an additional root directory called `queries`, in addition to `backend` and `frontend`.
If `queries` contains some GraphQL documents that do not match any of the provided patterns, the first project, `frontend`, will be
associated with those queries.

To achieve this, you can add an `exclude` pattern to the `frontend` project configuration. This will associate the files in the `queries`
folder with the `backend` project.

```yaml
projects:
  frontend:
    schema: https://my.api.com/graphql
    documents: frontend/**/*.{graphql,js,ts}
    exclude: queries/**  # <--- will enable strict matching for that project
  backend:
    schema: backend/schema.graphql
    documents: backend/**/*.graphql
```

This does not apply to type definitions, as mentioned previously. The plugin only uses type definitions from files that strictly match
the `schema` or `include` keys.

> NOTE: Values on the root level are defaults for projects. Therefore, if a project does not define a property such as `schema`, `include`,
> or even `extensions`, it will take a value from the root if it exists.

## Configuration files

The plugin supports multiple configuration file formats. Here is a list of all the possible options:

- graphql.config.json
- graphql.config.js
- graphql.config.cjs
- graphql.config.ts
- graphql.config.yaml
- graphql.config.yml
- .graphqlrc (YAML and JSON)
- .graphqlrc.json
- .graphqlrc.yaml
- .graphqlrc.yml
- .graphqlrc.js
- .graphqlrc.ts

### Yaml

The official JetBrains YAML plugin should be installed. However, it should be bundled into every IntelliJ IDE by default, so it usually
doesn't require any action.

### JavaScript

Internally, we use Node.js to load a JavaScript or TypeScript config file. Therefore, Node.js should be installed and properly configured in
the IDE. Note that the JavaScript plugin should also be installed in the IDE. This type of configuration will not work in Community
versions, but should work in IntelliJ IDEA Ultimate, WebStorm, PHPStorm, PyCharm Professional, and other editions.

As mentioned before, we don't transpile config files, so they should be written using the appropriate module system for Node.js. Note that
in the following snippet, the `module.exports` syntax is used instead of `export default`.

```javascript
module.exports = {
    schema: 'https://localhost:8000'
}
```

To use ESM in your configuration, add `"type": "module"` to your package.json file.

### TypeScript

To load a TypeScript config, the `ts-node` package should be installed either locally in the project or globally. You can find the package
[here](https://github.com/TypeStrong/ts-node).

If you use ESM modules in your project and have configured them in package.json as `"type": "module"`, you will also need to set up an ESM
loader for `ts-node`. This process is described in detail [here](https://github.com/TypeStrong/ts-node#native-ecmascript-modules).

```json5
{
    "compilerOptions": {
        // or ES2015, ES2020
        "module": "ESNext"
    },
    "ts-node": {
        "esm": true
    }
}
```

## Migration

If you are using a legacy configuration file, such as `.graphqlconfig`, we recommend converting it to a modern format of your choice.
Subjectively, YAML files are the most convenient for this purpose. An automatic conversion tool is available: simply open the legacy file in
the editor and press `Migrate` on the notification panel at the top of the editor. This will update the config keys and environment variable
syntax, but won't change the structure of the file. So, it's possible that you may still need to update some parts of the config manually.

It is recommended to migrate existing `includes` patterns that were previously used to configure compound schemas to the new `schema` key.
Additionally, please note that [environment variables](#environment-variables) now have a different syntax and support specifying a default
value, for example.

> IMPORTANT: Starting from plugin version 4.0.0, schema configuration is strict 
> and won't implicitly include any SDL definitions unless they are explicitly specified in the GraphQL Config.

The only exception to the above rule is an empty configuration file that may only contain the `extensions` key. 
This configuration file will implicitly include any GraphQL file located under this directory.
```yaml
extensions:
  endpoints:
    dev: https://example.com/graphql
```

## Legacy configuration

If you still prefer to use a legacy configuration format (although we don't recommend it 😉), make sure to explicitly specify the paths to
schema files in the `includes` property. Otherwise, only the types from `schemaPath` will be used for schema construction.

```json5
 {
    // a default way to provide a single-source schema
    "schemaPath": "schema.graphql",
    "includes": [
        "Types1.graphql",
        "types/**/*.graphql",
        "src/files/*.{graphql,js,ts}",
        "everything/inside/**"
    ],
    "excludes": [
        "types/excluded/**"
    ]
}
```

## Environment variables

You can utilize environment variables in your configuration files to specify a schema path, URL, or a header value. The syntax for using
environment variables in configuration files is as follows:

```
${VARIABLE_NAME}
${VARIABLE_WITH_DEFAULT:./some/default/file.graphql}
${VARIABLE_QUOTED:"http://localhost:4000/graphql"}
```

You can load definitions from environment variables, with or without fallback values.

```yaml
schema: ${SCHEMA_FILE:./schema.json}
```

If you want to define a fallback endpoint, you probably need to wrap your value with quotation marks.

```yaml
schema: ${SCHEMA_ENDPOINT:"http://localhost:4000/graphql"}
```

### .env files

There are several ways to provide environment variables. The most recommended method is to create a dedicated file with the variable values.
To avoid exposing your credentials, please refrain from committing this file.

The following filenames are supported in order of priority from top to bottom:

- .env.local
- .env.development.local
- .env.development
- .env.dev.local
- .env.dev
- .env

The plugin searches for the specified file starting from the directory containing the corresponding configuration file and going up to the
project root.

In such files, environment variables are represented as simple key-value pairs separated by the `=` sign. Values can optionally be enclosed
in quotes.

```
S3_BUCKET="YOURS3BUCKET"
SECRET_KEY=YOURSECRETKEYGOESHERE # comment 
SECRET_HASH="something-with-a-#-hash"
```

### Manual configuration

If you decide not to use .env files, you can provide environment variables manually for each configuration file. There are multiple ways to
accomplish this.

1. To edit GraphQL environment variables in a config file, open the file in the editor and right-click to open the context menu. Select
   the `Edit GraphQL Environment Variables` action from the menu. This will open a modal dialog where you can provide values for each
   environment variable in the file.
2. You can open the same dialog by clicking on the [toolbar](#toolbar) inside any GraphQL file. The dialog will automatically find a
   matching configuration file.
3. Otherwise, only missing variables would be requested on the first introspection query or GraphQL request.

### System

If no variables are found in the .env files or set manually, the plugin will attempt to retrieve them from your system environment.

# Frameworks

## Gatsby

Create a `graphql.config.js` at the root of your project with the following contents:

```javascript
module.exports = require("./.cache/typegen/graphql.config.json")
```

# Introspection

To provide resolution, completion, and validation, a plugin requires a schema. This can be achieved by performing an introspection query,
which should be configured beforehand. This process is described in a separate section dedicated to plugin configuration.

The easiest way to make an introspection query is to press the `Run` button on the editor's gutter if you're using YAML or JSON config
files. This will make an introspection query and save a file locally, either to the IDE's cache or to the project sources, depending on the
configuration.

> TIP: If you don't want to open the introspection result file after each query, you can disable it in the GraphQL
> options: `Languages & Frameworks > GraphQL > Open the editor with introspection result`.

Another way to perform the same query is by using `Run Introspection Query` from the [toolbar](#toolbar).

Additionally, you can obtain the same result from the GraphQL [tool window](#tool-window) by right-clicking on the endpoint and selecting
the `Get GraphQL Schema from Endpoint` action.

To inspect the schema structure of an introspection file in the editor, use the `Open Introspection Schema` action in
the [toolbar](#toolbar). This will open a file for the selected endpoint.

## Rerun latest introspection

If your schema is constantly changing, and you find yourself repeatedly running the same introspection action against the same endpoint, it
may be more convenient to use the `Rerun Introspection Query` action. This can be found using the `Find Action` menu, which can be accessed
by pressing `Cmd/Ctrl + Shift + A`. Note that this option only becomes available after you have already performed an introspection query. If
desired, you can also assign a hotkey to this action from the `Find Action` window.

# Queries

To execute a query directly from the editor, place the caret on the query definition, and run the `Execute Query` action from the toolbar
either manually or by using the `Ctrl/Cmd + Enter` hotkey. The query will be sent to a selected endpoint in the toolbar.

If your query has variables, you can open a dedicated variables editor using the `Toggle Variables Editor` action on the same toolbar. You
can then provide the variables in JSON format.

Alternatively, you can create a GraphQL scratch file and use it as a playground for sending queries. The easiest way to create such a file
and associate it with the correct GraphQL config is to use the GraphQL [tool window](#tool-window). Simply double-click on the endpoint node
and choose `New GraphQL Scratch File.`

## Scratch files

If a leading comment in a GraphQL scratch file contains a string that follows the pattern `# config=<path>[!<optionalProjectName>]`, it will
use the specified config and project for resolving and type validation. Comments that follow this pattern are considered valid:

```
# config=/user/local/project/.graphqlrc.yml
# config=/user/local/project/.graphqlrc.yml!backend
```

> NOTE: it works only for queries and fragment definitions, type definitions in scratch files are ignored.

# Toolbar

GraphQL files have a toolbar that the plugin uses to provide access to the most commonly used actions in one place.

![toolbar](/docs/assets/toolbar.png)

- **Open Configuration File**: Open the corresponding configuration file, or create a new one if it does not exist.
- **Edit Environment Variables**: This opens a dialog that allows you to provide values for environment variables that are defined in the
  associated configuration file.
- **Toggle Variables Editor**: This opens an editor window where you can provide query variables in JSON format.
- **Endpoints list**: A list of known URLs is defined in a config file, which you can use to select a URL where the GraphQL queries should
  go or from where the introspection should be fetched.
- **Execute GraphQL**: Run a selected GraphQL query from the editor below.
- **Run Introspection Query**: This action refreshes an introspected schema from the selected endpoint.
- **Open Introspection Schema**: This command opens a local file that corresponding to a selected endpoint.

# Tool window

The GraphQL tool window is used to provide an overview of your GraphQL projects. It can show validation errors, perform introspection
queries, create scratches, and more.

![tool window](/docs/assets/toolwindow.png)

The main tab, `Schemas and Project Structure`, provides an overview of detected configuration files and the GraphQL schema associated with
each of them. You can perform several useful actions for multiple nodes in this tree view.

- **Schema discovery summary:** To search through the types discovered for each project, double-click on the corresponding node. This will
  open a dialog window that enables you to search through every type and navigate to the place where they are defined.

![types search](/docs/assets/types_search.png)

- **Schema errors:** Clicking on the error node will navigate you to the source of the error.

![schema errors](/docs/assets/schema_errors.png)

- **Endpoints:** Right-clicking on the endpoint will give you the ability to make an introspection query or to create a Scratch file
  associated with the selected configuration file and project.

![endpoints](/docs/assets/endpoints.png)

## Tool window toolbar

Take a look at the toolbar on the left side of the Tool window. It provides some useful actions:

- **Add Schema Configuration**: This feature allows you to create a configuration file in a selected directory, but it is added just for
  convenience. Nothing prevents you from creating a configuration file through the Project View.
- **Edit Selected Schema Configuration**: Opens a configuration file for the selected node in the tool window.
- **Restart Schema Discovery**: This action clears all loaded schemas and starts the discovery process from scratch. It can be useful in
  cases where cached data is causing issues.

# Injections

## Tagged template literals

Supported tags are: `graphql`, `gql`, `Relay.QL`, `Apollo.gql`.

```
const QUERY = gql``;
```

## IntelliJ default comment-based injection

```
// language=GraphQL
const QUERY = `query { field }`;
```

## C-style comments

```
const QUERY = /* GraphQL */ `query { field }`;
```

## GraphQL comments

```js
const QUERY = `
    #graphql
    
    query { field }
`;
```

# Acknowledgements

This plugin was heavily inspired by [GraphiQL](https://github.com/graphql/graphiql) from Facebook.

A number of language features such as query and schema validation are powered
by [graphql-java](https://github.com/graphql-java/graphql-java).

A thanks also goes out to the [Apollo](https://github.com/apollographql) and [Prisma](https://github.com/prisma)  teams for their continued
efforts to improve the GraphQL developer experience.

And finally, a thank you to the [JetBrains WebStorm team](https://twitter.com/WebStormIDE) and the Alpha/Beta testers for all their help.

# License

MIT


