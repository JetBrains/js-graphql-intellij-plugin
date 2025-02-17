const QUERY = (param) => gql`
  query {
    user {
      ${param}
      ${param} id ${param}
      ${param}
      ... on User {
        ${param}
      }
    }
  }
`;
