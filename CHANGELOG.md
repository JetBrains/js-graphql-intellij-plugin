## 1.7.4 (2019-04-03)

Fixes:

- Fixed "DisposalException: Double release of editor" (#211)

## 1.7.3 (2019-01-26)

Fixes:

- Fixed missing syntax highlighting colors due to uninitialized JavaScript colors being referenced from GraphQL (#158)
- Set default charset to UTF-8 in query result viewer to align with JSON spec (#181)
- Query result viewer doesn't always reformat the response JSON (#209)
- Make the Query result viewer work with windows line endings in JSON responses (#191)

## 1.7.2 (2018-05-29)

Fixes:

- Restore whitespace tokens for top level fragment placeholders in Apollo to preserve them during format lines (#162)

## 1.7.1 (2018-05-11)

Fixes:

- Node.js outputs deprecation warnings using the error console, so silence them on order to be able to detect real errors when creating the process handler (#153)
- Fixed indentation issue with strongly typed placeholders by sending the current GraphQL environment to the language service (#130) 
- Removed 400 px large svg GraphQL and Relay logos used by Rider for the line marker (#147) 

## 1.7.0 (2018-03-04)

Features:

- Language Service 1.5.1: Support for strongly typed variable placeholders in GraphQL tagged templates (#130)
- Support for declaring annotations in Endpoint language (#15)

Fixes:

- Editor tab loaded on background thread during startup causes dispatch thread assertion error (#124)

## 1.6.3 (2018-01-02)

Fixes:

- 'Editor > Code Style' never loads In WebStorm 2017.3 (#120)

## 1.6.2 (2017-11-28)

Fixes:

- Resolve fragment references under progress in annotator to ensure WebStorm 2017.3 compatibility (#115)

## 1.6.1 (2017-10-04)

Fixes:

- 2017.3 compatibility (#105)

## 1.6.0 (2017-09-20)

Features:

- Support for Relay Modern fragments (#74)
- Language Service 1.5.0: Support for loading the schema from .graphql file (Relay Modern projects) 

Fixes:

- Fix 'Find Usages' dialog for non-GraphQL entities (#93)
- Fix case where the annotator would attempt to access an editor that is already disposed (#75)
- Show a notification when the Node.js process fails to start (#100)
- Remove Schema IDL warnings in .graphql files since this is the file extension Facebook uses to print schemas in Relay Modern (#85)

## 1.5.4 (2017-07-02)

Fixes:

- Only show the error console automatically on the first error in the project (#80) (#90)
- Send the variables editor text as-is since Gson always deserializes a JSON number as a Double (This turns a variable value of 1 into 1.0, making the value incompatible with the Int schema type) (#86)
- Auto-import is not placed on a new line in JS files with GraphQL templates (#91)

## 1.5.3 (2017-06-13)

Features:

- Support Relay Modern graphql.experimental tag (#74)

## 1.5.2 (2017-03-16)

Fixes:

- Pass "variables" in payload to GraphQL server as JSON. (#64)

## 1.5.1 (2017-02-12)

Fixes:

- 2017.1 EAP Not detecting Relay.QL usage (#60)

## 1.5.0 (2017-01-29)

Features:

- Language Service 1.4.0 based on graphql 0.9.1 and codemirror-graphql 0.6.2
- Experimental support for GraphQL Endpoint Language (#15)
- Full PSI support for arguments (#35, #51)
- Support for top level Apollo fragments template placeholders (#44)
- Keyboard shortcut can now be assigned to the restart language service action (#49)

## 1.4.4 (2016-11-28)

Fixes:

- Assertion failed: Caret model is in its update process. All requests are illegal at this point. (#42)

## 1.4.3 (2016-10-30)

Features:

- Add GraphQL configuration page for indentation (#29)

Fixes:

- Language Service 1.3.2: Object literal for variables in getFragment closes Relay.QL template expression.

## 1.4.2 (2016-09-25)

Features:

- Language Service 1.3.1: Support __schema root in schema.json (compatible with graphene)

Changes:

- Fixes formatting exception when using ".if" live template in JSFile with injected GraphQL (#26)

## 1.4.1 (2016-09-11)

Features:

- Support for gql tagged templates used by Apollo and Lokka GraphQL Clients (#25)
- Language Service 1.3.0 with Lokka and Apollo gql support (#25)
- Persist endpoint selection to project configuration

Changes:

- Fixes false Error in Relay Mutation (#23)


## 1.4.0 (2016-08-28)

Features:

- Language Service 1.2.0 based on graphql 0.7.0 and codemirror-graphql 0.5.4 (#22)
- Basic editor support for GraphQL Schema files (.graphqls) (#22)

## 1.3.3 (2016-08-17)

Changes:

- Fixes compatibility issue with IDEA 2016.2.2 (#18)

## 1.3.2 (2016-06-23)

Changes:

- Remove GraphQL schema from scratch file formats (#14)

## 1.3.1 (2016-06-09)

Changes:

- JSLanguageCompilerToolWindowManager: NoClassDefFoundError in WebStorm 2016.2 EAP (#13)
- Language Service 1.1.2 increases maximum size of JSON schema from 100kb to 32mb

## 1.3.0 (2016-05-21)

Features:

- Support for GraphQL scratch files
- Invoke reformat code action on query result

## 1.2.0 (2016-03-14)

Features:

- Contextual query support: Execute buffer, selection, or named operations at the caret position in the GraphQL editor

## 1.1.1 (2016-02-03)

Changes:

- Completion after ... fragment spread operator. (#4)
- Language Service 1.1.1 based on graphql 0.4.16 and codemirror-graphql 0.2.2

## 1.1.0 (2016-01-31)

Features:

- Support for GraphQL Schema Language


## v1.0.0 (2015-12-13)

Features:

- Initial release.
