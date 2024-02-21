const QUERY = gql`
  query {
    __typename
  }
`

const TYPE = gql`
  type User {
    userId: ID
    userName: String
  }
`