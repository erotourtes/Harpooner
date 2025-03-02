<div align="center">
  <h1>Harpooner
    <img src="https://github.com/erotourtes/Harpooner/blob/main/src/main/resources/META-INF/pluginIcon.svg"  width="100" height="100">
  </h1>
  
  ![Build](https://github.com/erotourtes/Harpooner/workflows/Build/badge.svg)
  [![Version](https://img.shields.io/jetbrains/plugin/v/com.github.erotourtes.harpoon.svg)](https://plugins.jetbrains.com/plugin/21796-harpooner)
  [![Downloads](https://img.shields.io/jetbrains/plugin/d/com.github.erotourtes.harpoon.svg)](https://plugins.jetbrains.com/plugin/21796-harpooner)
</div>

Harpooner - the Harpoon by Erotourtes for JetBrains products  
###### Inspired by [ThePrimeagen/harpoon](https://github.com/ThePrimeagen/harpoon)

## Preview
[Preview.webm](https://github.com/erotourtes/Harpooner/assets/67370189/6dfed402-ac46-48fe-8331-c620cdc301be)

### Description

<!-- Plugin description -->

Harpooner is a navigation plugin. You can "harpoon" a file and quickly open it later in the Tools menu or through the
keyboard shortcuts.
> Note: you need to configure the keyboard shortcuts yourself. See
> the [example](https://github.com/erotourtes/Harpooner#example) section.

#### Features
- The Harpooner menu is represented as a file within the IDE, thus seamlessly integrates with the [IdeaVim](https://github.com/JetBrains/ideavim) plugin.
- All changes made to the menu are automatically saved in real-time (configurable in settings).
- Automatic renaming of files is supported when they are moved, renamed, or deleted.
- Users have control over the length of visible paths displayed in the menu

This plugin is inspired by [Harpoon](https://github.com/ThePrimeagen/harpoon)
<!-- Plugin description end -->

### Example
You need to use [IdeaVim](https://plugins.jetbrains.com/plugin/164-ideavim) plugin to use the following keybindings.  

```Vim
" Harpoon
nmap <M-j> <action>(HarpoonerOpenFile0)
nmap <M-k> <action>(HarpoonerOpenFile1)
nmap <M-l> <action>(HarpoonerOpenFile2)
nmap <M-;> <action>(HarpoonerOpenFile3)

nmap <leader>hm <action>(HarpoonerQuickMenu)
nmap <leader>ht <action>(HarpoonerToggleQuickMenu)

nmap <leader>ha <action>(HarpoonerAddFile)
" nmap <leader>hr <action>(HarpoonerRemoveFile)
" nmap <leader>htf <action>(HarpoonerToggleFile)

nmap <leader>hn <action>(HarpoonerNextFileAction)
nmap <leader>hp <action>(HarpoonerPreviousFileAction)

" nmap <M-4> <action>(HarpoonerOpenFile4)
" nmap <M-5> <action>(HarpoonerOpenFile5)
" nmap <M-6> <action>(HarpoonerOpenFile6)
" nmap <M-7> <action>(HarpoonerOpenFile7)
" nmap <M-8> <action>(HarpoonerOpenFile8)
" nmap <M-9> <action>(HarpoonerOpenFile9)
```
> [!TIP]
> You can move code with 
> ```vim
> vmap J <action>(MoveLineDown)
> vmap K <action>(MoveLineUp)
> nmap <C-j> <action>(MoveLineDown)
> nmap <C-k> <action>(MoveLineUp)
> ```

### Settings
You can change the settings:
<kbd>Settings/Preferences</kbd> > <kbd>Tools</kbd> > <kbd>Harpooner Settings</kbd>

![settings1](https://github.com/erotourtes/Harpooner/assets/67370189/3073101f-d004-4321-b3ae-375c94496d9f)
> With show project path `on`
> ![settings2](https://github.com/erotourtes/Harpooner/assets/67370189/77949989-9a8c-4f04-9fbc-93afe163dd06)

> With the number of visible words: 1  
> ![settings3](https://github.com/erotourtes/Harpooner/assets/67370189/09dbde64-4d2f-4fe9-9cd4-2d46ddf3b890)

> With save on type `off`  
> Changes are saved after and only after closing the file


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
