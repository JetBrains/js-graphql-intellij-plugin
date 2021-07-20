import * as gql from 'graphql-tag';

const STR = `something`;

const DEPARTMENTS_QUERY = gql`
    type SomeType {
        id(arg: String = ${STR}): ID
    }

    query Departments($arg: String = ${STR}) @Dir(arg: ${STR}) {
        users(filter: {
            name: ${STR}
            addresses: ["abc", ${STR}]
        }) {
            ${STR}
        }

        ${STR}
    }

    ${STR}

    # --- Schema ---

    directive @Dir(arg: String) on QUERY

    type Query {
        users(filter: UserFilter): [User]
    }

    input UserFilter {
        name: String
        addresses: [String!]
    }

    type User {
        id: ID
        name: String
        phone: String
    }
`;
