module.exports = {
  validate: require('./lib/validate'),
  schema: {
    get idl () {
      const join = require('path').join
      return require('fs').readFileSync(join(__dirname, 'schema.graphql'), 'utf8')
    },
    json: require('./schema.json')
  }
}
