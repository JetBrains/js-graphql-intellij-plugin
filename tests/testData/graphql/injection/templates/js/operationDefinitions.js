const QUERY = (param) => gql`
  ${param}

  query A {
    user {
      id
    }
  }
  
  ${param}
  
  query B {
    user {
     id
    }
  }

  ${param}
`;
