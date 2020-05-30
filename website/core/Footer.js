/**
 * Copyright (c) 2017-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

const React = require('react');

class Footer extends React.Component {
    docUrl(doc, language) {
        const baseUrl = this.props.config.baseUrl;
        const docsUrl = this.props.config.docsUrl;
        const docsPart = `${docsUrl ? `${docsUrl}/` : ''}`;
        return `${baseUrl}${docsPart}${doc}`;
    }

    pageUrl(doc, language) {
        const baseUrl = this.props.config.baseUrl;
        return baseUrl + (language ? `${language}/` : '') + doc;
    }

    render() {
        return (
            <footer className="nav-footer" id="footer">
                <section className="sitemap">
                    <a href={this.props.config.baseUrl} className="nav-home">
                        {this.props.config.footerIcon && (
                            <img
                                src={this.props.config.baseUrl + this.props.config.footerIcon}
                                alt={this.props.config.title}
                                width="66"
                                height="58"
                            />
                        )}
                    </a>
                    <div>
                        <h5>Docs</h5>
                        <a href={this.docUrl('getting-started.html', this.props.language)}>
                            Getting Started
                        </a>
                        <a href={this.docUrl('developer-guide.html', this.props.language)}>
                            Developer guide
                        </a>
                        <a href={this.docUrl('graphql-config-examples.html', this.props.language)}>
                            Example projects
                        </a>
                    </div>
                    <div>
                        <h5>Community</h5>
                        <a href="https://graphql.org/"  target="_blank">
                            graphql.org
                        </a>
                        <a href="https://github.com/graphql-java/graphql-java" target="_blank">
                            graphql-java
                        </a>
                        <a href="https://github.com/kamilkisiela/graphql-config/tree/legacy" target="_blank">
                            graphql-config v2
                        </a>
                        <a href="https://github.com/chentsulin/awesome-graphql" target="_blank">
                            awesome-graphql
                        </a>
                        <a href="https://twitter.com/search?q=graphql" target="_blank" rel="noreferrer noopener">
                            Twitter
                        </a>
                        <a href="https://stackoverflow.com/questions/tagged/graphql" target="_blank" rel="noreferrer noopener">
                            Stack Overflow
                        </a>
                    </div>
                    <div>
                        <h5>More</h5>
                        <a href={`${this.props.config.baseUrl}blog`}>Blog</a>
                        <a href="https://github.com/jimkyndemeyer/js-graphql-intellij-plugin">GitHub</a>
                        <a
                            className="github-button"
                            href={this.props.config.repoUrl}
                            data-icon="octicon-star"
                            data-count-href="/jimkyndemeyer/js-graphql-intellij-plugin/stargazers"
                            data-show-count="true"
                            data-count-aria-label="# stargazers on GitHub"
                            aria-label="Star this project on GitHub">
                            Star
                        </a>
                    </div>
                </section>
                <section className="copyright">{this.props.config.copyright}</section>
            </footer>
        );
    }
}

module.exports = Footer;
