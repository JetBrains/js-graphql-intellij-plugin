/**
 * Copyright (c) 2017-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

// See https://docusaurus.io/docs/site-config for all the possible
// site configuration options.

const siteConfig = {
  title: 'JS GraphQL IntelliJ Plugin', // Title for your website.
  tagline: 'GraphQL language support for WebStorm, IntelliJ IDEA and other IDEs based on the IntelliJ Platform',
  // For github.io type URLs, you would set the url and baseUrl like:
  url: 'https://jimkyndemeyer.github.io',
  baseUrl: '/js-graphql-intellij-plugin/',

  // Used for publishing and more
  projectName: 'js-graphql-intellij-plugin',
  organizationName: 'jimkyndemeyer',
  // For top-level user or org sites, the organization is still the same.
  // e.g., for the https://JoelMarcey.github.io site, it would be set like...
  //   organizationName: 'JoelMarcey'

  // For no header links in the top nav bar -> headerLinks: [],
  headerLinks: [
    {doc: 'getting-started', label: 'Docs'},
    {blog: true, label: 'Blog'},
    {href: 'https://github.com/jimkyndemeyer/js-graphql-intellij-plugin', label: 'GitHub'},
  ],

  /* path to images for header/footer */
  // headerIcon: 'img/docusaurus.svg',
  // footerIcon: 'img/docusaurus.svg',
  favicon: 'img/favicon.png',

  /* Colors for website */
  colors: {
    primaryColor: '#E10098',
    secondaryColor: '#f26b00',
  },

  // This copyright info is used in /core/Footer.js and blog RSS/Atom feeds.
  copyright: `Copyright © ${new Date().getFullYear()} Jim Kynde Meyer and Contributors`,

  highlight: {
    // Highlight.js theme to use for syntax highlighting in code blocks.
    theme: 'default',
  },

  // Add custom scripts here that would be placed in <script> tags.
  scripts: ['https://buttons.github.io/buttons.js'],

  // On page navigation for the current documentation page.
  onPageNav: 'separate',
  // No .html extensions for paths.
  cleanUrl: true,

  // Open Graph and Twitter card images.
  ogImage: 'img/js-graphql-logo.png',
  twitterImage: 'img/js-graphql-logo.png',

  // Show documentation's last contributor's name.
  // enableUpdateBy: true,

  // Show documentation's last update time.
   enableUpdateTime: true,

  // You may provide arbitrary config keys to be used as needed by your
  // template. For example, if you need your repo's URL...
  repoUrl: 'https://github.com/jimkyndemeyer/js-graphql-intellij-plugin',

  algolia: {
    apiKey: '3a204bad724e39bb9e3921b7f684a541',
    indexName: 'JS GraphQL IntelliJ Plugin',
    algoliaOptions: {}
  },

};

module.exports = siteConfig;
