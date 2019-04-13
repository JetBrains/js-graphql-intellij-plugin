//
// line deltas
//

const schema2 = gql`
    
    scalar Foo
    
    type OutputType {
        someField: Foo!
    }

    type Query {
        hello: Foo
    }

    type SchemaError {
        argument(foo: OutputType): Foo
    }

`;
