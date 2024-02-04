const { name, description, repository } = require('../../package')

module.exports = {

  title: name,

  description: description,

  head: [
    ['meta', { name: 'theme-color', content: '#3eaf7c' }],
    ['meta', { name: 'apple-mobile-web-app-capable', content: 'yes' }],
    ['meta', { name: 'apple-mobile-web-app-status-bar-style', content: 'black' }]
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
    sidebar: {
      '/docs/': [
        {
          title: 'Getting Started',
          collapsable: false,
          children: [
            '',
            'Terms-you-need-to-understand',
          ]
        },
        {
          title: 'Configuration',
          collapsable: true,
          children: [
            'Configuration',
            'Configuration-File',
            'Configuration-Login-Providers',
            'Configuration-Routes',
            'Configuration-SecurityProfiles',
            'Configuration-Session-Behaviour',
            'Tracing-Log-Correlation-Correlation-Logging',
            'Configuration-User-Mapping',
            'Key-management-and-JWT-signer'
          ]
        },
        {
          title: 'Custom code extensions',
          collapsable: true,
          children: [
            'Custom-(code)-extensions',
            'Logging',
            'Tracing-Log-Correlation',
            'JWT-Signer-(add-custom-new-signature-type-to-JWT)',
            'Automatic-Key-Rotation'
          ]
        },
        {
          title: 'OAG API endpoints',
          collapsable: true,
          children: [
            'OAG-API---endpoints',
            'JWKS-JWT-signing-public-keys-of-OAG',
            'Token-Validation-with-Java-Spring'
          ]
        },
        {
          title: 'Integrating with OAG',
          collapsable: true,
          children: [
            'Integrating-with-OAG',
            'Validating-user-identity'
          ]
        },
        {
          title: 'How To\'s',
          collapsable: true,
          children: [
            'HowTo-Deploy-OAG-on-Azure-App-Service',
            'Whitelisting-of-URL\'s-(What-URLs-are-required-by-OAG)',
            'Setup-for-OAG-development',
            'Create-a-new-Release'
          ]
        }
      ]
    }
  }
}
