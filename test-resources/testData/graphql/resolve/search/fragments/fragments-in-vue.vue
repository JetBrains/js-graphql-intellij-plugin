<template>

</template>

<script>
const QUERY = gql`
  type SomeType {
    id: ID
  }
`

const FRAGMENT = `
  #graphql
  fragment FragmentInVue on User {
    name
  }
`

export default {
  name: "fragments-in-vue"
}
</script>

<style scoped lang="text/css">

</style>