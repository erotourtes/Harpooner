<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Harpooner Changelog

## [Unreleased]

## [1.3.2] - 2025-08-18

### Fixed

- Plugin compatibility

## [1.3.1] - 2025-08-17

### Fixed

- Plugin kotlin compatibility version

## [1.3.0] - 2025-08-17

### Added

- `HarpoonerReplaceFile0` through `HarpoonerReplaceFile9`: actions that replace the entry at positions 0–9 in the
  Harpoon menu with the current file.

## [1.2.0] - 2025-04-14

### Added

- `HarpoonerClearMenu` → Clear all saved paths.
- Plugin now sets `g:Harpooner` global variable if the IdeaVim plugin is installed

## [1.1.0] - 2025-03-02

### Added

- `HarpoonerToggleQuickMenu` - Toggle the Harpoon menu
- `HarpoonerToggleFile` - Toggle the current file in Harpoon - adds it if it's not already in the list, removes it if it
  is.
- `HarpoonerRemoveFile` - Remove the current file from Harpoon’s menu.

### Changed

- Recommended `ideavimrc` configuration in Readme

## [1.0.15] - 2024-06-17

### Added

- support for move next and previous files in harpooner list (#21)

## [1.0.14] - 2024-03-18

### Fixed

- removed until build

## [1.0.13] - 2024-03-12

### Fixed

- infinite loop via rename change listener with git

## [1.0.12] - 2024-02-25

### Added

- up to 10 open file actions

## [1.0.11] - 2024-02-23

### Fixed

- opening the menu with an open file action
- preserving the menu state (to be able to undo)
- fixed updating folds on the fly
- fixed listening to changes might have cause an infinite update

## [1.0.10] - 2024-02-21

### Added

- Auto changing paths on rename/move/delete files

### Fixed

- Folds with a non project files
- Absolute file paths
- Dispatcher thread called ui changes

## [1.0.9] - 2024-02-20

### Fixed

- Folds on a wrong editor
- Auto closing menu on editor startup

## [1.0.8] - 2024-02-19

### Added

- Auto save menu on typing (might be disabled in settings) PR #5
- Auto close menu on editor focus lost
- IdeaVIM as an optional dependency

### Fixed

- Removed deprecated methods
- Use github actions to run integration tests

## [1.0.7] - 2023-12-17

### Changed

- Updated the Gradle `pluginUntilBuild` property

## [1.0.6] - 2023-10-03

### Fixed

- Dynamic settings for the Harpooner menu (fixes #1)

## [1.0.5] - 2023-08-19

### Added

- Dynamic settings for the Harpooner menu

### Fixed

- Symbolic links now resolve correctly

### Changed

- View of the menu

## [1.0.4] - 2023-08-04

### Added

- Icon in the toolbar menu
- GitHub Actions — use Java `17` explicitly

### Fixed

- Compatibility with the latest Jetbrains IDEs

### Changed

- StartupActivity instead of ProjectManagerListener
- Upgrade Gradle Wrapper to `8.2.1`
- GitHub Actions — rearrange the Build workflow

## [1.0.3] - 2023-05-21

### Added

- Files the plugin generates (Harpooner Menu & Harpooner.xml) are added to a .gitignore

## [1.0.2] - 2023-05-14

### Added

- Added Folds to the Harpooner menu

## [1.0.1] - 2023-05-12

### Added

- Added auto-save feature for the menu file

### Changed

- Removed a project path in the Harpooner menu

## [1.0.0] - 2023-05-05

### Added

- Basic functionality of what the original Harpoon can do

[Unreleased]: https://github.com/erotourtes/Harpooner/compare/v1.0.8...HEAD

[1.0.8]: https://github.com/erotourtes/Harpooner/compare/v1.0.7...v1.0.8

[1.0.7]: https://github.com/erotourtes/Harpooner/compare/v1.0.6...v1.0.7

[1.0.6]: https://github.com/erotourtes/Harpooner/compare/v1.0.5...v1.0.6

[1.0.5]: https://github.com/erotourtes/Harpooner/compare/v1.0.4...v1.0.5

[1.0.4]: https://github.com/erotourtes/Harpooner/compare/v1.0.3...v1.0.4

[1.0.3]: https://github.com/erotourtes/Harpooner/compare/v1.0.2...v1.0.3

[1.0.2]: https://github.com/erotourtes/Harpooner/compare/v1.0.1...v1.0.2

[1.0.1]: https://github.com/erotourtes/Harpooner/compare/v1.0.0...v1.0.1

[1.0.0]: https://github.com/erotourtes/Harpooner/commits/v1.0.0
