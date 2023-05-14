# Harpooner

![Build](https://github.com/erotourtes/Harpooner/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/com.github.erotourtes.harpoon)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/com.github.erotourtes.harpoon)

## Template ToDo list

- [x] Create a new [IntelliJ Platform Plugin Template][template] project.
- [x] Get familiar with the [template documentation][template].
- [x] Adjust the [pluginGroup](./gradle.properties), [plugin ID](./src/main/resources/META-INF/plugin.xml)
  and [sources package](./src/main/kotlin).
- [x] Adjust the plugin description in `README` (see [Tips][docs:plugin-description])
- [x] Review
  the [Legal Agreements](https://plugins.jetbrains.com/docs/marketplace/legal-agreements.html?from=IJPluginTemplate).
- [ ] [Publish a plugin manually](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate)
  for the first time.
- [x] Set the `PLUGIN_ID` in the above README badges.
- [ ] Set the [Plugin Signing](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html?from=IJPluginTemplate)
  related [secrets](https://github.com/JetBrains/intellij-platform-plugin-template#environment-variables).
- [ ] Set
  the [Deployment Token](https://plugins.jetbrains.com/docs/marketplace/plugin-upload.html?from=IJPluginTemplate).
- [ ] Click the <kbd>Watch</kbd> button on the top of the [IntelliJ Platform Plugin Template][template] to be notified
  about releases containing new features and fixes.

### Description

<!-- Plugin description -->

Harpooner is a navigation plugin. You can "harpoon" a file and quickly open it later in the Tools menu or through the
keyboard shortcuts.
> Note: you need to configure the keyboard shortcuts yourself. See
> the [example](https://github.com/erotourtes/Harpooner#example) section.

The harpooner menu is a file. All changes to a file are saved after **closing the IDE tab**.  

This plugin is inspired by [Harpoon](https://github.com/ThePrimeagen/harpoon)
<!-- Plugin description end -->

### Example
You need to use [IdeaVim](https://plugins.jetbrains.com/plugin/164-ideavim) plugin to use the following keybindings.  

```Vim
" Harpoon
nmap <M-j> :action HarpoonerOpenFile0<cr>
nmap <M-k> :action HarpoonerOpenFile1<cr>
nmap <M-l> :action HarpoonerOpenFile2<cr>
nmap <M-;> :action HarpoonerOpenFile3<cr>

nmap <leader>hm :action HarpoonerQuickMenu<cr>
nmap <leader>ha :action HarpoonerAddFile<cr>
```

### Installation

- Using IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "
  Harpooner"</kbd> > <kbd>Install Plugin</kbd>

- Manually:

  Download the [latest release](https://github.com/erotourtes/Harpooner/releases/latest) and install it manually
  using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template

[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
