export declare function tagged(literals: string | readonly string[], ...args: any[]): any;

declare type SomeType<T> = T;

const QUERY_TYPED = /* GraphQL */ tagged`
    query {
        field
    }
` as SomeType<string>;