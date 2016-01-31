![](docs/js-graphql-logo.png)

# JS GraphQL IntelliJ Plugin

GraphQL language support including Relay.QL tagged templates in JavaScript and TypeScript.

It provides the following features in IntelliJ IDEA, WebStorm, RubyMine, PhpStorm, and PyCharm:

- Schema-aware completion, error highlighting, and documentation
- Syntax highlighting, code-formatting, folding, commenter, and brace-matching
- 'Find Usages' and 'Go to Declaration' for schema types and fields
- Schema viewer and 'Go to Implementation' for schema interfaces
- 'Structure view' to navigate GraphQL and GraphQL Schema files
- Configurable GraphQL schema retrieval and reloading based on a local file or a url using 'then-request'
- Execute queries with variables against configurable endpoints

It depends on [js-graphql-language-service](https://github.com/jimkyndemeyer/js-graphql-language-service) that it manages using a Node.js process handler.

## Features demo

**Schema setup, completion, error highlighting, documentation**
![](docs/js-graphql-webstorm-demo.gif)

**Find usages, schema viewer, structure view**
![](docs/js-graphql-webstorm-usages-structure-demo.gif)

## FAQ

**Which IDEs are compatible with the plugin?**

The plugin is compatible with version 143+ of IntelliJ IDEA, WebStorm, RubyMine, PhpStorm, and PyCharm.

PyCharm CE and Android studio is not supported since the plugin depends on two other plugins: NodeJS and JavaScript.

**Where can I get the plugin?**

The plugin is available from the JetBrains Plugin Repository at https://plugins.jetbrains.com/plugin/8097?pr=

To install it, open "Settings", "Plugins", "Browse repositories..." and search for "JS GraphQL".

**How do I reload a GraphQL Schema that was loaded from a URL?**

In the the GraphQL tool window, select the "Current Errors" tab and click the "Restart JS GraphQL Language Service" button. 

## License
MIT
