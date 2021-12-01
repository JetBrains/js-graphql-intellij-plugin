![](docs/js-graphql-logo.png)

# GraphQL IntelliJ Plugin

GraphQL language support for WebStorm, IntelliJ IDEA and other IDEs based on the IntelliJ Platform.

## Features overview

- Full language support for the June 2018 GraphQL Specification including the Type System Definition Language (SDL)
- The plugin discovers your local schema on the fly. Remote schemas are easily fetched using introspection
- Schema discovery is configured using [graphql-config v2](https://github.com/kamilkisiela/graphql-config/tree/legacy) files, including support for multi-schema projects
- Built-in support for Relay and Apollo projects: `graphql` and `gql` tagged template literals in JavaScript and TypeScript are automatically recognized as GraphQL
- Execute queries using variables against configurable endpoints, including support for custom headers and environment variables
- Schema-aware completion, error highlighting, and documentation
- Syntax highlighting, code-formatting, folding, commenter, and brace-matching
- 'Find Usages' and 'Go to Declaration' for schema types, fields, and fragments
- 'Structure view' to navigate GraphQL files
- Load variables from .env files. Supported file names: `.env.local`,`.env.development.local`,`.env.development`,`.env.dev.local`,`.env.dev`,`.env`
- Built-in Relay and Apollo Federation type definitions

## Documentation

The main documentation site is available at https://jimkyndemeyer.github.io/js-graphql-intellij-plugin/

## Which IDEs are compatible with the plugin?

The plugin is compatible with version 2020.3+ of all IDEs based on the IntelliJ Platform, including but not limited to WebStorm, IntelliJ IDEA, Android Studio, RubyMine, PhpStorm, and PyCharm.

## Where can I get the plugin?

The plugin is published to the [JetBrains Plugin Repository](https://plugins.jetbrains.com/plugin/8097-js-graphql).

To install it, open your IDE "Settings", "Plugins", "Marketplace" and search for "GraphQL".

## Injections

### Tagged template literals

Supported tags are: `graphql`, `gql`, `Relay.QL`, `Apollo.gql`.
```
const QUERY = gql``;
```

### IntelliJ default comment-based injection

```
// language=GraphQL
const QUERY = `query { field }`;
```

### C-style comments
```
const QUERY = /* GraphQL */ `query { field }`;
```

### GraphQL comments
```
const QUERY = `
    #graphql
    
    query { field }
`;
```

## Acknowledgements

This plugin was heavily inspired by [GraphiQL](https://github.com/graphql/graphiql) from Facebook.

A number of language features such as query and schema validation are powered by [graphql-java](https://github.com/graphql-java/graphql-java).

A thanks also goes out to the [Apollo](https://github.com/apollographql) and [Prisma](https://github.com/prisma) teams for their continued efforts to improve the GraphQL Developer Experience.

And finally, a thank you to the [JetBrains WebStorm team](https://twitter.com/WebStormIDE) and the Alpha/Beta testers for all their help in getting the 2.0 release across the finish line. 

## License
MIT
