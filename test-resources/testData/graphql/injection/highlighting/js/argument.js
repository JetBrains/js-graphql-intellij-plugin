import gql from 'graphql-tag';

const <symbolName descr="identifiers//local function">createUserQuery</symbolName> = (<symbolName descr="identifiers//parameter">id</symbolName>) => {
  return gql`
    query {
      <symbolName descr="GRAPHQL_FIELD_NAME">user</symbolName>(<symbolName descr="GRAPHQL_ARGUMENT">userId</symbolName>: ${<symbolName descr="identifiers//parameter">id</symbolName>})
    }
  `;
};
