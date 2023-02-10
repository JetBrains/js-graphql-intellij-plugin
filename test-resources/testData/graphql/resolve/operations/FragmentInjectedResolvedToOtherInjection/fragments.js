function fn() {
    gql`
        fragment UserFragment on User {
            name
        }
    `
}
