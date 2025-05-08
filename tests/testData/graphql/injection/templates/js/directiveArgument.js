const QUERY1 = (param) => gql`
  type User {
    id: ID @deprecated(reason: ${param})
  }
`;
