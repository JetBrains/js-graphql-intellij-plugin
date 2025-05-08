const QUERY = (param) => gql`
  query {
    user {
      ...UserFragment ${param}
      additionalData {
        ...UserDataFragment @dir ${param}
        ...UserDataFragment ${param} @dir
        ...UserDataFragment1
        ... on UserData ${param} {
          email
          ssn
        }
        ... on UserData @dir ${param} {
          email
          ssn
        }
        ... on UserData ${param} @dir  {
          email
          ssn
        }
      }
    }
  }
  
  fragment UserFragment on User ${param} {
    id
    name
  }
  
  fragment UserDataFragment on UserData @dir ${param} {
    ssn
    email
  }
  
  fragment UserDataFragment1 on UserData ${param} @dir {
    ssn
    email
  }
`;
