//
// line deltas
//

const schema1 = gql`

    scalar Foo

    type ServerType {
        valueFromServer: Foo!
    }

    type Query {
        hello: Foo
    }

    query Foo {
        ... on <error descr="Fragment cannot be spread here as objects of type Query can never be of type ServerType">ServerType</error> {
            valueFromServer
        }
    }

    type EscapedBacktick {
        "Description with escaped backtick \`"
        field: Foo
    }
`;
