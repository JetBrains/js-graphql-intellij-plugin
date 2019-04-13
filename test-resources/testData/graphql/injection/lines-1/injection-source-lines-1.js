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
        ... on ServerType {
            valueFromServer
        }
    }
`;