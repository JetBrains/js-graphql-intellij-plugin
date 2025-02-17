const POLICY_PAGE_QUERY = (locale) => gql`
  query PolicyPageQuery($handle: String!) ${locale} {
    policy: metaobject(handle: {
      handle: $handle,
      type: "policies"
    }) {
      title: field(key: "title") {
        value
      }
      content: field(key: "content") {
        value
      }
      updatedAt
    }
  }
`;
