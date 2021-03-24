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
        argument(foo: <error descr="The type 'OutputType' is not an input type, but was used as an input type">OutputType</error>): Foo
    }

`;
