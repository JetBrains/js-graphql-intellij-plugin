const QUERY = (param) => gql`
  schema ${param} {
    query: Query
  }

  type Query ${param} {
    user(input: UserInput): User
  }

  type User implements BaseModel ${param} {
    id: ID
    name(arg: String): String
    active: Boolean
  }
  
  type User1 @dir ${param} {
    user(input: UserInput): User
  }

  type User2 ${param} @dir {
    user(input: UserInput): User
  }

  input UserInput ${param} {
    id: ID
    name: String
    active: Boolean
    role: UserRole
    data: JSON
  }

  interface BaseModel ${param} {
    id: ID
  }

  enum UserRole ${param} {
    ADMIN
    USER
  }

  scalar JSON ${param}

  union DataTypes ${param} = User

  directive @dir on QUERY |
    MUTATION |
    SUBSCRIPTION |
    FIELD |
    FRAGMENT_DEFINITION |
    FRAGMENT_SPREAD |
    INLINE_FRAGMENT |
    VARIABLE_DEFINITION |
    SCHEMA |
    SCALAR |
    OBJECT |
    FIELD_DEFINITION |
    ARGUMENT_DEFINITION |
    INTERFACE |
    UNION |
    ENUM |
    ENUM_VALUE |
    INPUT_OBJECT |
    INPUT_FIELD_DEFINITION

  query {
    user(input: {id: 1, name: "test", active: true, role: USER, data: {}}) {
      id
      name(arg: "")
      active
    }
  }
`;
