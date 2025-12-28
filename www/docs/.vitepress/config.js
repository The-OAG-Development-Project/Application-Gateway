const {name, description, repository} = require('../../package')

module.exports = {

  title: name,

  description: description,

  ignoreDeadLinks: 'localhostLinks',

  head: [
    ['meta', {name: 'theme-color', content: '#3eaf7c'}],
    ['meta', {name: 'apple-mobile-web-app-capable', content: 'yes'}],
    ['meta', {name: 'apple-mobile-web-app-status-bar-style', content: 'black'}]
  ],

  base: '/',

  themeConfig: {
    repo: repository,
    editLinks: false,
    docsDir: '',
    editLinkText: '',
    lastUpdated: false,
    nav: [
      {
        text: 'Documentation',
        link: '/docs/',
      },
      {
        text: 'Swagger',
        link: 'https://app.swaggerhub.com/apis-docs/gianlucafrei/OAG/0.4#/'
      },
      {
        text: 'OWASP',
        link: 'https://owasp.org/www-project-application-gateway/'
      }
    ],
    sidebar: [
      {
        text: 'Getting Started',
        base: '/docs',
        items: [
          {text: 'Overview', link: '/'},
          {text: 'Terms you need to understand', link: '/Terms-you-need-to-understand'}
        ]
      },
      {
        text: 'Configuration',
        collapsed: true,
        base: '/docs',
        items: [
          {text: 'Overview', link: '/Configuration'},
          {text: 'Configuration File', link: '/Configuration-File'},
          {text: 'CSRF Protection', link: '/Csrf-Protection'},
          {text: 'Login Providers', link: '/Configuration-Login-Providers'},
          {text: 'Routes', link: '/Configuration-Routes'},
          {text: 'Security Profiles', link: '/Configuration-SecurityProfiles'},
          {text: 'Session Behaviour', link: '/Configuration-Session-Behaviour'},
          {text: 'Tracing, Correlation-Logging', link: '/Tracing-Log-Correlation-Correlation-Logging'},
          {text: 'User Mapping', link: '/Configuration-User-Mapping'},
          {text: 'Key management and JWT signer', link: '/Key-management-and-JWT-signer'},
          {text: 'Configure TLS and the certificate used by OAG', link: '/TLS-configuration'}
        ]
      },
      {
        text: 'Custom code extensions',
        collapsed: true,
        base: '/docs',
        items: [
          {text: 'Overview', link: '/Custom-(code)-extensions'},
          {text: 'Logging', link: '/Logging'},
          {text: 'Tracing, Log correlation', link: '/Tracing-Log-Correlation'},
          {text: 'JWT Signer and custom signature type', link: '/JWT-Signer-(add-custom-new-signature-type-to-JWT)'},
          {text: 'Key Rotation', link: '/Automatic-Key-Rotation'},
          {text: 'CSRF Protection', link: '/Csrf-Protection'},
        ]
      },
      {
        text: 'OAG API endpoints',
        collapsed: true,
        base: '/docs',
        items: [
          {text: 'API Endpoints', link: '/OAG-API---endpoints'},
          {text: 'Jwks Endpoint (public signing key)', link: '/JWKS-JWT-signing-public-keys-of-OAG'}
        ]
      },
      {
        text: 'Integrating with OAG',
        collapsed: true,
        base: '/docs',
        items: [
          {text: 'Overview', link: '/Integrating-with-OAG'},
          {text: 'User identity validation', link: '/Validating-user-identity'},
          {text: 'Token validation (Spring/Java)', link: '/Token-Validation-with-Java-Spring'},
          {text: 'URL Whitelisting / OAG provided URL', link: '/Whitelisting-of-URL\'s-(What-URLs-are-required-by-OAG)'}
        ]
      },
      {
        text: 'How To\'s',
        collapsed: true,
        base: '/docs',
        items: [
          {text: 'Deploy to Azure', link: '/HowTo-Deploy-OAG-on-Azure-App-Service'},
          {text: 'Developer Setup', link: '/Setup-for-OAG-development'},
          {text: 'Update Documentation', link: '/Update-Documentation'},
          {text: 'Create a new Release', link: '/Create-a-new-Release'}
        ]
      }],
    footer: {
      message: 'Open-source Apache 2 Licensed | Powered by a lot of love ❤️ (and code)',
      copyright: 'Copyright © OAG Contributors'
    }
  }
}
