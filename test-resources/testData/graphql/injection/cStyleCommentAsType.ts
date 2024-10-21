declare type SomeType<T> = T;

const QUERY_TYPED = /* GraphQL */ `
    query {
        field
    }
` as SomeType<string>;