const QUERY = (param) => gql`
  ${param}
  
  type Query {
    user(arg: String): User
    user1(arg: String): User1
  }

  ${param}
  ${param}

  type User {
    id: ID
    name: String
    active: Boolean
  }

  ${param}
  
  type User1 {
    id: ID
    name: String
    active: Boolean
  }
  
  ${param}
  
  query {
    user {
      id
      name
      active
    }
    
    user1 {
      id
      name
      active
    }
  }
`;
