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
