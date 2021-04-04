// noinspection GraphQLUnresolvedReference

const a = 1;

const QUERY = gql`

    type Query {
        a: ID
        field(arg: ID): Int
    }


    type <error descr="\"User\" must define one or more fields"><error descr="The field type 'UnknownType' is not present when resolving type 'User'">User</error></error> {
        name: UnknownType
    }

    query {
        a
        bcd
        field(bar: 2)
    }

`;
