const QUERY = (param) => gql`
  query UserQuery($name: String = ${param}, $id: ID) {
    user(input: {name: $name, id: $id}) {
      name
    }
  }

  query UserQuery1($name: String ${param}, $id: ID) {
    user(input: {name: $name, id: $id}) {
      name
    }
  }

  query UserQuery2($name: String @dir ${param}, $id: ID) {
    user(input: {name: $name, id: $id}) {
      name
    }
  }

  query UserQuery3($name: String ${param} @dir, $id: ID) {
    user(input: {name: $name, id: $id}) {
      name
    }
  }
`;
