const QUERY = (param) => gql`
  type Query {
    user(input: UserInput): User
  }

  type User {
    id: ID
    name(arg: String): String ${param}
    active: Boolean ${param}
  }

  input UserInput {
    id: ID
    name: String ${param}
    active: Boolean
    role: UserRole
  }

  enum UserRole {
    ADMIN
    USER ${param}
  }

  query {
    user(input: {id: 1, name: "test", active: true, role: USER}) {
      id ${param}
      name(arg: "") ${param}
      active ${param}
    }
  }
`;
