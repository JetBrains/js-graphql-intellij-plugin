const QUERY = gql`
    {
        field {
            user(role: <caret>)
        }
    }
`;
