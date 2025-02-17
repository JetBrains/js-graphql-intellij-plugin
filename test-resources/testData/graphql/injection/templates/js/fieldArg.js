const QUERY = (param) => gql`
  query {
    field(arg: ${param})
  }
`;


const val = 3;
const QUERY_RAIL = gql`query GetRail { rail(railId: ${val}) { id } }`;