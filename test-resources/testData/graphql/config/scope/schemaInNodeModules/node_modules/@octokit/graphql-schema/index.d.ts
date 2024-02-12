import { GraphQLError } from "graphql";

type JsonSchema = {
  [key: string]: any;
};

type Schema = {
  idl: string;
  json: JsonSchema;
};

type Validate = (query: ReadonlyArray<string> | Readonly<string>) => ReadonlyArray<GraphQLError>;

declare namespace GraphqlSchema {
  const validate: Validate;
  const schema: Schema;
}

export const schema: Schema
export const validate: Validate

export * from './schema'

export default GraphqlSchema
