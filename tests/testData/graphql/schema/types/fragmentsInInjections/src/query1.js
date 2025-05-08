gql`
    fragment FragmentOne on User {
        name
    }
`

gql`
    fragment FragmentTwo on User {
        name
        children {
            ...FragmentFour
        }
    }
`
