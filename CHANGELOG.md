<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Changelog

## [2.9.1] - 2021-03-02

### Fixed
- SSL certificate check could be disabled (#435)

## [2.9.0] - 2021-02-24

### Added
- GZip response handling (#427)

### Fixed
- Startup exception caused by Relay detection (#431)
- Search for configuration through the whole project root hierarchy (#423)

## [2.8.0] - 2021-02-16

### Added
- Loading env variables from .env file (#426)

### Fixed
- Executing a single selected query (#418)

## [2.7.1] - 2021-01-15

### Fixed
- Compatibility with 2021.1
- Possible cause of the broken auto-complete
- Fixed semantic highlighting when errors are in the same range (#383)

## [2.7.0] - 2020-11-25

### Fixed
- Compatibility with 2020.3
- Exception when using default parameter values (#412)

### Added
- @required, @inline and @preloadable directives for Relay
- Support for .gql file types

## [2.6.0] - 2020-09-03

### Changed

- Basic support for schema splitting with GraphQL Modules and similar tools (<a href="https://github.com/jimkyndemeyer/js-graphql-intellij-plugin/pull/374">#374</a>)
- Improved error handling for introspection queries
- Updated for compatibility with IntelliJ IDEA 2020.2
- Allow introspection query results with empty errors in the response (#272)

### Fixed

- Fix incorrect escaping of characters in scalar descriptions (#358)
- Fix wrong line separators error (#347)
- Fix escaping of GraphQL query variables (#364)
- Fix query execution and config creation for GraphQL fragments editor (#365, #356)
- Fix UI freeze during a request for a large introspection schema
 

## [2.5.0] - 2020-05-30

### Changed

- Upgraded to graphql-java 15.0 (#341)
    - Interfaces can implement other interfaces
    - Directive support on variable definitions
    - New specifiedBy scalar
    - Allow nullable arguments to be introduced on fields from interfaces when implemented in types (#338)

## [2.4.0] - 2020-03-13

### Changed

- Allow keywords to be used as field names (#239) (#264)
- Upgraded graphql-java to 14.0 (https://github.com/graphql-java/graphql-java/issues/1523)
- Refactored breaking changes from graphql-java 12.0 to 14.0

## [2.3.0] - 2019-12-15

### Changed

- Fix for Incompatible API changes in the upcoming IntelliJ Platform 2020.1 (#303)
- Fix for Nashorn deprecation warning (#287)
- Fix for Go to Definition always scrolls to the top of the schema (#289)
- Handle escaped backticks in gql tagged template literals (#279)
- Fixed assertion error when clicking settings tool button in injected GraphQL fragment editor window (#275)

## [2.2.0] - 2019-06-16

### Changed

- Added Code Style settings page to control indentation (#258, #92)

## [2.1.1] - 2019-06-12

### Changed

- Fixed: Enum value of TRUE breaks schema (#244)
- Add support for generics in TypeScript GraphQL tagged template literals (#200)
- Store caret position for contextual queries when it changes to avoid using the Editor UI from non-UI thread in query
  context highlighter (#256)
- Recognize JSON files with top-level __schema field as GraphQL introspection result in GraphQLIdentifierIndex (#242)
- Compatibility fixes for 2019.2 EAP (#256, #259)

## [2.1.0] - 2019-04-14

### Changed

- Upgraded to graphql-java 12.0 to improve performance in very large schemas (#238)
- Reintroduced contextual queries and automatically include referenced fragments across files (#243, #43, #94)
- Detect manual injections using language=GraphQL comment to discover schema types and fragments (#235)
- Don't clear the variables editor when the variables window is closed (#83)

## [2.0.0] - 2019-04-06

### Changed

- Support for the June 2018 GraphQL specification including SDL (#118, #133, #137, #129, #141, #144, #150, #164)
- Support for multiple schemas using graphql-config (#84, #125, #140, #145, #164)
- Replaced Node.js based language service with native parser, lexer, and graphql-java to be compatible with all IDEs
  based on the IntelliJ Platform (#62, #164)

## [1.7.4] - 2019-04-03

### Fixed

- Fixed "DisposalException: Double release of editor" (#211)

## [1.7.3] - 2019-01-26

### Fixed

- Fixed missing syntax highlighting colors due to uninitialized JavaScript colors being referenced from GraphQL (#158)
- Set default charset to UTF-8 in query result viewer to align with JSON spec (#181)
- Query result viewer doesn't always reformat the response JSON (#209)
- Make the Query result viewer work with windows line endings in JSON responses (#191)

## [1.7.2] - 2018-05-29

### Fixed

- Restore whitespace tokens for top level fragment placeholders in Apollo to preserve them during format lines (#162)

## [1.7.1] - 2018-05-11

### Fixed

- Node.js outputs deprecation warnings using the error console, so silence them on order to be able to detect real
  errors when creating the process handler (#153)
- Fixed indentation issue with strongly typed placeholders by sending the current GraphQL environment to the language
  service (#130)
- Removed 400 px large svg GraphQL and Relay logos used by Rider for the line marker (#147)

## [1.7.0] - 2018-03-04

### Added

- Language Service 1.5.1: Support for strongly typed variable placeholders in GraphQL tagged templates (#130)
- Support for declaring annotations in Endpoint language (#15)

### Fixed

- Editor tab loaded on background thread during startup causes dispatch thread assertion error (#124)

## [1.6.3] - 2018-01-02

### Fixed

- 'Editor > Code Style' never loads In WebStorm 2017.3 (#120)

## [1.6.2] - 2017-11-28

### Fixed

- Resolve fragment references under progress in annotator to ensure WebStorm 2017.3 compatibility (#115)

## [1.6.1] - 2017-10-04

### Fixed

- 2017.3 compatibility (#105)

## [1.6.0] - 2017-09-20

### Added

- Support for Relay Modern fragments (#74)
- Language Service 1.5.0: Support for loading the schema from .graphql file (Relay Modern projects)

### Fixed

- Fix 'Find Usages' dialog for non-GraphQL entities (#93)
- Fix case where the annotator would attempt to access an editor that is already disposed (#75)
- Show a notification when the Node.js process fails to start (#100)
- Remove Schema IDL warnings in .graphql files since this is the file extension Facebook uses to print schemas in Relay
  Modern (#85)

## [1.5.4] - 2017-07-02

### Fixed

- Only show the error console automatically on the first error in the project (#80) (#90)
- Send the variables editor text as-is since Gson always deserializes a JSON number as a Double (This turns a variable
  value of 1 into 1.0, making the value incompatible with the Int schema type) (#86)
- Auto-import is not placed on a new line in JS files with GraphQL templates (#91)

## [1.5.3] - 2017-06-13

### Added

- Support Relay Modern graphql.experimental tag (#74)

## [1.5.2] - 2017-03-16

### Fixed

- Pass "variables" in payload to GraphQL server as JSON. (#64)

## [1.5.1] - 2017-02-12

### Fixed

- 2017.1 EAP Not detecting Relay.QL usage (#60)

## [1.5.0] - 2017-01-29

### Added

- Language Service 1.4.0 based on graphql 0.9.1 and codemirror-graphql 0.6.2
- Experimental support for GraphQL Endpoint Language (#15)
- Full PSI support for arguments (#35, #51)
- Support for top level Apollo fragments template placeholders (#44)
- Keyboard shortcut can now be assigned to the restart language service action (#49)

## [1.4.4] - 2016-11-28

### Fixed

- Assertion failed: Caret model is in its update process. All requests are illegal at this point. (#42)

## [1.4.3] - 2016-10-30

### Added

- Add GraphQL configuration page for indentation (#29)

### Fixed

- Language Service 1.3.2: Object literal for variables in getFragment closes Relay.QL template expression.

## [1.4.2] - 2016-09-25

### Added

- Language Service 1.3.1: Support __schema root in schema.json (compatible with graphene)

### Changed

- Fixes formatting exception when using ".if" live template in JSFile with injected GraphQL (#26)

## [1.4.1] - 2016-09-11

### Added

- Support for gql tagged templates used by Apollo and Lokka GraphQL Clients (#25)
- Language Service 1.3.0 with Lokka and Apollo gql support (#25)
- Persist endpoint selection to project configuration

### Changed

- Fixes false Error in Relay Mutation (#23)

## [1.4.0] - 2016-08-28

### Added

- Language Service 1.2.0 based on graphql 0.7.0 and codemirror-graphql 0.5.4 (#22)
- Basic editor support for GraphQL Schema files (.graphqls) (#22)

## [1.3.3] - 2016-08-17

### Changed

- Fixes compatibility issue with IDEA 2016.2.2 (#18)

## [1.3.2] - 2016-06-23

### Changed

- Remove GraphQL schema from scratch file formats (#14)

## [1.3.1] - 2016-06-09

### Changed

- JSLanguageCompilerToolWindowManager: NoClassDefFoundError in WebStorm 2016.2 EAP (#13)
- Language Service 1.1.2 increases maximum size of JSON schema from 100kb to 32mb

## [1.3.0] - 2016-05-21

### Added

- Support for GraphQL scratch files
- Invoke reformat code action on query result

## [1.2.0] - 2016-03-14

### Added

- Contextual query support: Execute buffer, selection, or named operations at the caret position in the GraphQL editor

## [1.1.1] - 2016-02-03

### Changed

- Completion after ... fragment spread operator. (#4)
- Language Service 1.1.1 based on graphql 0.4.16 and codemirror-graphql 0.2.2

## [1.1.0] - 2016-01-31

### Added

- Support for GraphQL Schema Language

## [1.0.0] - 2015-12-13

### Added

- Initial release.
