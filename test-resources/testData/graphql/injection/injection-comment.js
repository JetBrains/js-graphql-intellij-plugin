const result = (
    // language=GraphQL
    {
        query: `

            extend type Query {
                manualExtension: String
            }

            fragment Login on Login {
                message
                success
            }

            mutation login($email: String! $pwd: String!) {
                login(email: $email pwd: $pwd) {
                    ...Login
                    ...<error descr="Unknown fragment spread \"OnlyTheUnknownFragmentShouldBeHighlightedAsError\"">OnlyTheUnknownFragmentShouldBeHighlightedAsError</error>
                }
            }

            query Foo {
                manualExtension
            }
        `
    }
);
console.log(result);
