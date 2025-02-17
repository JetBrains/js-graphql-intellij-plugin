import { gql } from 'apollo-server';

const SCHEMA_1 = gql`
  enum FooTypes1 { ${"foo bar"} }
`;

const arrayOfThings = ["foo", "bar"];

const SCHEMA_2 =  gql`
  enum FooTypes2 { ${arrayOfThings.join(' ')} }
`;