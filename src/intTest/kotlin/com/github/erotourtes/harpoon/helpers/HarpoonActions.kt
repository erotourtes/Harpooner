package com.github.erotourtes.harpoon.helpers

enum class HarpoonActions(val actionName: String) {
    FileAdd("HarpoonerAddFile"),
    FileRemove("HarpoonerRemoveFile"),
    FileToggle("HarpoonerToggleFile"),

    File0Replace("HarpoonerReplaceFile0"),
    File1Replace("HarpoonerReplaceFile1"),
    File2Replace("HarpoonerReplaceFile2"),
    File3Replace("HarpoonerReplaceFile3"),
    File4Replace("HarpoonerReplaceFile4"),
    File5Replace("HarpoonerReplaceFile5"),
    File6Replace("HarpoonerReplaceFile6"),
    File7Replace("HarpoonerReplaceFile7"),
    File8Replace("HarpoonerReplaceFile8"),
    File9Replace("HarpoonerReplaceFile9"),

    File0Open("HarpoonerOpenFile0"),
    File1Open("HarpoonerOpenFile1"),
    File2Open("HarpoonerOpenFile2"),
    File3Open("HarpoonerOpenFile3"),
    File4Open("HarpoonerOpenFile4"),
    File5Open("HarpoonerOpenFile5"),
    File6Open("HarpoonerOpenFile6"),
    File7Open("HarpoonerOpenFile7"),
    File8Open("HarpoonerOpenFile8"),
    File9Open("HarpoonerOpenFile9"),

    FileOpenNext("HarpoonerNextFileAction"),
    FileOpenPrevious("HarpoonerPreviousFileAction"),

    QuickMenuOpen("HarpoonerQuickMenu"),
    QuickMenuToggle("HarpoonerToggleQuickMenu"),
    QuickMenuClear("HarpoonerClearMenu")
}
