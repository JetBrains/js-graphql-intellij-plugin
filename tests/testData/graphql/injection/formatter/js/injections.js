import gql from 'graphql-tag';

const func = (queryName) => {
    return     gql`
  query {
   rail(railId: ${queryName}) {
                        id
 }
  }
`;
};

// language=GraphQL
`
query {
rail(railId: ${param}) {
             name
 }
}
`;
