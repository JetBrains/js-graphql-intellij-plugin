const QUERY = gql`
  query {
    __typename
  }
`

const FRAGMENT = gql`
  fragment SomeFragmentInJs on User {
    name
  }
`