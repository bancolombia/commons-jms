// @ts-check
// `@type` JSDoc annotations allow editor autocompletion and type checking
// (when paired with `@ts-check`).
// There are various equivalent ways to declare your Docusaurus config.
// See: https://docusaurus.io/docs/api/docusaurus-config

import {themes as prismThemes} from 'prism-react-renderer';

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'Commons JMS',
  tagline: 'JMS Configuration Abstraction with Multi-Connection for queue listeners and producers, built on top of spring boot JMS. This library offers a performant setup for JMS Clients.',
  favicon: 'img/favicon.ico',

  // Set the production url of your site here
  url: 'https://bancolombia.github.io',
  // Set the /<baseUrl>/ pathname under which your site is served
  // For GitHub pages deployment, it is often '/<projectName>/'
  baseUrl: '/commons-jms/',

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: 'bancolombia', // Usually your GitHub org/user name.
  projectName: 'commons-jms', // Usually your repo name.

  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',

  // Even if you don't use internationalization, you can use this field to set
  // useful metadata like html lang. For example, if your site is Chinese, you
  // may want to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  markdown: { mermaid: true },
  themes: ['@docusaurus/theme-mermaid'],

  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          sidebarPath: './sidebars.js',
          // Please change this to your repo.
          // Remove this to remove the "edit this page" links.
          editUrl:
          'https://github.com/bancolombia/commons-jms/tree/docs/docs',
        },
        theme: {
          customCss: './src/css/custom.css',
        },
      }),
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      // Replace with your project's social card
      image: 'docs/img/commons-jms.png',
      navbar: {
        title: 'Commons JMS',
        logo: {
          alt: 'Commons JMS Logo',
          src: 'img/logo.svg',
        },
        items: [
          {
            type: 'docSidebar',
            sidebarId: 'tutorialSidebar',
            position: 'left',
            label: 'Docs',
          },
          {
            href: 'https://github.com/bancolombia/commons-jms',
            label: 'GitHub',
            position: 'right',
          },
        ],
      },
      footer: {
        style: 'dark',
        links: [
          {
            title: 'Docs',
            items: [
              {
                label: 'Overview',
                to: '/docs/intro',
              },
              {
                label: 'Commons JMS',
                to: '/docs/commons-jms/getting-started',
              },
            ],
          },
          {
            title: 'Community',
            items: [
              {
                label: 'Changelog',
                href: 'https://github.com/bancolombia/commons-jms/blob/main/CHANGELOG.md',
              },
              {
                label: 'Contributing',
                href: 'https://github.com/bancolombia/commons-jms/blob/main/CONTRIBUTING.md',
              },
            ],
          },
          {
            title: 'More',
            items: [
              {
                label: 'Bancolombia Tech',
                href: 'https://medium.com/bancolombia-tech',
              },
              {
                label: 'GitHub',
                href: 'https://github.com/bancolombia/commons-jms',
              },
              {
                label: 'Maven Central',
                href: 'https://central.sonatype.com/artifact/com.github.bancolombia/commons-jms-mq/versions',
              },
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} Bancolombia.`,
      },
      prism: {
        additionalLanguages: ['java', 'groovy', 'yaml'],
        theme: prismThemes.github,
        darkTheme: prismThemes.dracula,
      },
    }),
};

export default config;
