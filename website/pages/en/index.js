/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */

const React = require('react');

const CompLibrary = require('../../core/CompLibrary.js');

const MarkdownBlock = CompLibrary.MarkdownBlock; /* Used to read markdown */
const Container = CompLibrary.Container;
const GridBlock = CompLibrary.GridBlock;

class HomeSplash extends React.Component {
    render() {
        const {siteConfig, language = ''} = this.props;
        const {baseUrl, docsUrl} = siteConfig;
        const docsPart = `${docsUrl ? `${docsUrl}/` : ''}`;
        const langPart = `${language ? `${language}/` : ''}`;
        const docUrl = doc => `${baseUrl}${docsPart}${langPart}${doc}`;

        const SplashContainer = props => (
            <div className="homeContainer">
                <div className="homeSplashFade">
                    <div className="wrapper homeWrapper">{props.children}</div>
                </div>
            </div>
        );

        const ProjectTitle = () => (
            <div>
                <h2 className="projectTitle">
                    {siteConfig.title}
                    <small>{siteConfig.tagline}</small>
                </h2>
            </div>
        );

        const PromoSection = props => (
            <div className="section promoSection">
                <div className="promoRow">
                    <div className="pluginRowBlock">{props.children}</div>
                </div>
            </div>
        );

        const Button = props => (
            <div className="pluginWrapper buttonWrapper">
                <a className="button" href={props.href} target={props.target}>
                    {props.children}
                </a>
            </div>
        );

        return (
            <SplashContainer>
                <div className="inner">
                    <ProjectTitle siteConfig={siteConfig}/>
                    <PromoSection>
                        <Button href={docUrl('getting-started.html')}>Get started</Button>
                        <Button href="https://github.com/jimkyndemeyer/js-graphql-intellij-plugin">GitHub Repository</Button>
                    </PromoSection>
                </div>
            </SplashContainer>
        );
    }
}

class Index extends React.Component {
    render() {
        const {config: siteConfig, language = ''} = this.props;
        const {baseUrl} = siteConfig;

        const Block = props => (
            <Container
                padding={['bottom']}
                id={props.id}
                background={props.background}>
                <GridBlock
                    align="center"
                    contents={props.children}
                    layout={props.layout}
                />
            </Container>
        );

        const FeatureCallout = () => (
            <div
                className="productShowcaseSection paddingBottom paddingTop"
                style={{textAlign: 'center'}}>
                <h2>Acknowledgments</h2>
                <MarkdownBlock>
                    This plugin was heavily inspired by [GraphiQL](https://github.com/graphql/graphiql) from Facebook.
                </MarkdownBlock>
                <MarkdownBlock>
                    A number of language features, such as query and schema validation, are powered by [graphql-java](https://github.com/graphql-java/graphql-java).
                </MarkdownBlock>
                <MarkdownBlock>
                    A thanks also goes out to the [Apollo](https://github.com/apollographql) and [Prisma](https://github.com/prisma) teams for their continued efforts to improve the GraphQL Developer Experience.
                </MarkdownBlock>
                <MarkdownBlock>
                    [JProfiler Java profiler](https://www.ej-technologies.com/products/jprofiler/overview.html) license provided by ej-technologies GmbH.
                </MarkdownBlock>
                <img src={`${baseUrl}img/js-graphql-logo.png`} alt={siteConfig.title} style={{marginTop: "100px"}}/>
            </div>
        );

        const Features = () => (
            <Block layout="fourColumn">
                {[
                    {
                        content: 'The plugin discovers your local schema on the fly.<br/>Remote schemas are easily fetched using introspection.',
                        imageAlign: 'top',
                        title: 'Schema-Aware Development',
                    },
                    {
                        content: 'Consume schemas easily with the full-featured GraphQL editor.<br/>Jump to definitions and show usages for fields and types.',
                        imageAlign: 'top',
                        title: 'Auto-Completion and Error Highlighting',
                    },
                ]}
            </Block>
        );

        return (
            <div>
                <HomeSplash siteConfig={siteConfig} language={language}/>
                <div className="mainContainer">
                    <Features/>
                    <div className="ide" />
                    <FeatureCallout/>
                </div>
            </div>
        );
    }
}

module.exports = Index;
