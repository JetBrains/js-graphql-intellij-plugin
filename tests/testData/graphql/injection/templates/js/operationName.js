import gql from 'graphql-tag';

export const queryName = 'myQueryName';

const query = gql`
  query ${queryName} {
    user {
      id
      name
    }
  }
`;

export default query;