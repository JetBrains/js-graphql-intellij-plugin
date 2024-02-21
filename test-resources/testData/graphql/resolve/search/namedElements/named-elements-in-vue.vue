<template>

</template>

<script>
const QUERY = gql`
  query {
    __typename
  }
`

const FRAGMENT = `
  #graphql
  enum UserRole {
    ADMIN
    USER
  }
`

export default {
  name: "fragments-in-vue"
}
</script>

<style scoped lang="text/css">

</style>