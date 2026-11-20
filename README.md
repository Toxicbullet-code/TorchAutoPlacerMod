# TorchAutoPlacer

A lightweight, client-side utility mod for **NeoForge 1.21.1** that automates block and torch placement based on your movement distance and surrounding light levels. Perfect for mining trips, building long pathways, and spawn-proofing tunnels efficiently.

---

## Features

* **Distance-Based Placement:** Automatically places blocks behind you once you reach your configured step interval (default: 3 blocks).
* **Smart Light Filtering:** Toggleable block-light level check ($\le 0$) to ensure torches only drop when lighting is genuinely needed to prevent mob spawns.
* **Inventory Fallback System:** Set any block (like grass, cobblestone, or wood) as your target material. If you run out in your hotbar, the mod seamlessly falls back to placing standard torches until you restock.
* **In-Game Settings Screen:** Access and configure all your toggles and preferences easily via the custom GUI screen by pressing **`K`** (configurable via keybinds).

---

## Controls & Usage

1. **Toggle GUI / Settings:** Press **`K`** (default) to open the configuration screen.
2. **Activation:** Turn on the auto-placer toggle inside the menu.
3. **Hotbar Setup:** Place your desired target block or torches anywhere in your active hotbar slots (0–8).

---

## Requirements

* **Minecraft:** `1.21.1`
* **Mod Loader:** NeoForge (`21.1.234` or higher)
* **Client-Side Only:** This mod runs purely on the client. It is safe to use on vanilla multiplayer servers that allow client-side utility mods.

---

## License

This project is open-source and licensed under the **GPL-3.0-only** license. See the [LICENSE](LICENSE) file for details.
