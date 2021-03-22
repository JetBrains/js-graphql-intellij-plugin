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
        argument(foo: <error descr="The type 'OutputType' [@9:5] is not an input type, but was used as an input type [@18:23]">OutputType</error>): Foo
    }

`;
