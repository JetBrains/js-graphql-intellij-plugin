const QUERY = /* GraphQL */ `
  query {
    __typename
  }
`

const FRAGMENT = gql`
  fragment SomeFragmentInJs on User {
    name
  }
`