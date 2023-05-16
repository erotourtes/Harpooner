<div align="center">
  <h1>Harpooner
    <img src="https://github.com/erotourtes/Harpooner/blob/main/src/main/resources/META-INF/pluginIcon.svg"  width="100" height="100">
  </h1>
  
  ![Build](https://github.com/erotourtes/Harpooner/workflows/Build/badge.svg)
  [![Version](https://img.shields.io/jetbrains/plugin/v/com.github.erotourtes.harpoon.svg)](https://plugins.jetbrains.com/plugin/21796-harpooner)
  [![Downloads](https://img.shields.io/jetbrains/plugin/d/com.github.erotourtes.harpoon.svg)](https://plugins.jetbrains.com/plugin/21796-harpooner)
</div>

Harpooner - Harpoon by Erotourtes for JetBrains products  
###### Inspired by [ThePrimeagen/harpoon](https://github.com/ThePrimeagen/harpoon)

## Preview
[Preview.webm](https://github.com/erotourtes/Harpooner/assets/67370189/6dfed402-ac46-48fe-8331-c620cdc301be)

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
