# Ender's NBT Workshop

A lightweight, powerful in-game NBT and Data Component editor mod for Minecraft 1.21.1 on Fabric. Inspired by tools like NBTExplorer and In-Game NBTEdit, this mod provides server-secured real-time editing of Blocks, Entities, and Items directly from within the game.

## Features

- **In-Game NBT Editor**: View and modify the data of any BlockEntity, Entity, or ItemStack without leaving the game.
- **Two Editing Modes**:
  - **Quick Edit (Items Only)**: A streamlined interface for held items allowing you to quickly change custom names, toggle unbreakable status, and add/remove/level-up enchantments via a convenient dropdown and +/- buttons (with smooth acceleration!).
  - **Raw NBT**: A fully expandable tree-view of raw NBT and Data Components for advanced data manipulation.
- **Client-Server Separation**: Mod logic is safely separated. Clients send requests to the server, and the server validates permissions (Requires OP Level 2) before broadcasting tag structures to the client.
- **Command & Keybind Access**: Access the menu swiftly through hotkeys or commands.

## Usage

You can open the editor in two ways:

1. **Keybind (Default `N`)**:
   - Aim at a **Block** or **Entity** and press `N` to open their raw NBT structure.
   - If you are not looking at any block or entity, the mod will automatically fallback to editing the **item in your main hand**.

2. **Commands**:
   - `/nbtedit hand` - Edits the item currently in your main hand.
   - `/nbtedit block` - Edits the BlockEntity you are looking at.
   - `/nbtedit entity` - Edits the Entity you are looking at.

## Requirements

- **Minecraft**: 1.21.1
- **Modloader**: Fabric Loader
- **Dependencies**: Fabric API

## Installation

1. Download the mod `.jar` file from the releases.
2. Drop it into your `mods/` folder on both the Client and the Server.
3. Make sure you also have the Fabric API installed.

## Building from source

Clone the repository and run the gradle build command:

```bash
git clone https://github.com/EnderkingTR/Ender-s-NBT-Workshop.git
cd Ender-s-NBT-Workshop
./gradlew build
```

The compiled mod will be located in the `build/libs` folder named `enders_nbt-1.0.0-1.21.1-fabric.jar`.

## License

This project is licensed under the MIT License.
