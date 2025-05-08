const QUERY = (param) => gql`
  type Query {
    ${param}
    user(arg: UserInput): User
    user1(arg: UserInput): User1
  }

  type User {
    id: ID
    name: String
    ${param}
    active: Boolean
  }
  
  type User1 {
    id: ID
    name: String
    active: Boolean
    role: UserRole
    ${param}
  }
  
  input UserInput {
    ${param}
    id: ID ${param}
    name: String
    ${param}
    active: Boolean
    role: UserRole
  }
  
  enum UserRole {
    ${param} ADMIN 
    ${param}
    USER ${param}
  }
  
  schema {
    ${param}
    query: Query
    ${param}
    mutation: Mutation
    ${param}
  }
  
  type Mutation {
    id: ID
  }
  
  query {
    user(arg: {id: 123, name: "test", active: true, role: ADMIN}) {
      id
      name
      active
    }
    
    user1(arg: {role: USER}) {
      id
      name
      active
    }
  }
`;
