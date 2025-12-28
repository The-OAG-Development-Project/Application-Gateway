# Documentation Overview
Documentation is written with VitePress and then deployed to GitHub Pages.
And most importantly we also want to have proper Javadoc documentation in the code.

# Location and Organisation of VitePress Documentation
 | Topic                               | Location |
|-------------------------------------|-----------------------------------------------------------------------------------------------------|
| Root Location of the documentation: | folder `www/docs` in repository https://github.com/The-OAG-Development-Project/Application-Gateway. | 
| Navigation Menu configuration:      | folder `www/docs/.vitepress` file `config.js` section `themeConfig.sidebar`.                        |
| Homepage:                           | `www/docs/index.md`                                                                                 |
| All other documenation pages:       | `www/docs/docs`                                                                                     |
| Logo of OAG:                        | `www/docs/public`                                                                                   |

# Building Documentation
The whole documentation is built with VitePress to folder `www/docs/.vitepress/dist`.
This is done with the Workflow defined in `gh-pages-deploy.yml`.
The Workflow is triggered whenever the main branch is updated.

# Deploying documentation
After building, the built documentation is copied to repository https://github.com/The-OAG-Development-Project/The-OAG-Development-Project.github.io.
This is also done by the Workflow defined in `gh-pages-deploy.yml` (last step).

The final deployment to the website available at https://the-oag-development-project.github.io/ is done by the standard Workflow `pages-build-deployment` of the repository https://github.com/The-OAG-Development-Project/The-OAG-Development-Project.github.io.
(See https://github.com/The-OAG-Development-Project/The-OAG-Development-Project.github.io/settings/pages).