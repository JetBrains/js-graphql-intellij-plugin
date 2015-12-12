# Developing

## Setting up the Plugin SDK
- Add all jars in `<intellij home>/plugins/JavaScriptLanguage`
- Add the NodeNS.jar in `<user home>/.IntelliJIdea15/config/plugins/NodeJS/lib`
- Add the IntelliLang.jar in `<user home>/.IntelliJIdea15/config/plugins/IntelliLang/lib`

## Setting up Intellij Community (OpenApi) sources:
- https://github.com/JetBrains/intellij-plugins/tree/master/Dart

## Run-configuration options for interacting with the language service
By default the plugin uses the language service in `META-INF/dist/js-graphql-language-service.dist.js`.

During plugin development there are two options for working directly with the language service source files:

### Option 1: Let IntelliJ start the language service directly from its server.js file

```
-Djsgraphql.debug=true
-Djsgraphql.debug.languageServiceDistFile=<git directory>/js-graphql-language-service/bin/server.js
```
     
### Option 2: Let intelliJ connect to an already running language service instance

This run-configuration setup enabled debugging of the language service source code.

```
-Djsgraphql.debug=true
-Djsgraphql.debug.languageServiceUrl=http://localhost:3000/js-graphql-language-service
```

Note that no process handler console view is available since the plugin isn't responsible for running the Node.js process.