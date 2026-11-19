# Changelog

All notable changes to More Beacon Colors are documented here.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
More Beacon Colors is versioned semantically (`MAJOR.MINOR.PATCH`).
It runs on Minecraft 26.3, 26.2 and 26.1 (Fabric + Forge + NeoForge, Java 25) and requires
[Mycel](https://github.com/MyCelium-GG/MyCel-Lib) (`my.celium.org`).

## [1.3.0] - 2026-11-19

### Added
- In 11 languages: English, German, Spanish, French, Dutch, Brazilian Portuguese, Russian, Polish, Italian, Japanese and Simplified Chinese. Messages from the server reach each player in their own game language.

### Changed
- Needs Mycel 1.7.0.

## [1.2.0] - 2026-11-16

### Added
- Stripes: bands of your colors rising up the beam. Red and white is a candy cane.
- Aurora: your colors drift along the beam in slow, soft waves, like northern lights.
- Presets: one click for Candy Cane, Christmas Lights, Aurora, Frost, Fire, Ocean, Sunset or Pride, from the new Presets button in the color menu.
- Copy and paste: copy a beam (or Ctrl+C) and paste it on another beacon (or Ctrl+V). The beam goes to your clipboard as a short code like `beam:stripes:1:E53935,FFFFFF`, which also works when pasted from chat, so you can share beams with friends.

### Changed
- The style tiles are now in four columns; "Color cycle" is called "Cycle".
- Installed without Mycel, the game still starts: More Beacon Colors stays off and says in chat that Mycel is missing, with a download link.
- Players on version 1.1.2 or older see Stripes and Aurora beams as vanilla beams until they update.

## [1.1.2] - 2026-11-04

### Changed
- Built for Mycel 1.6.1: chat messages get the new MyCelium look, and Mycel can share anonymous stats if you agree. What is sent: https://wiki.mycelium-gg.org/privacy#mycel-stats

### Added
- Anonymous stats (how many beams get colored and which modes) so we know what to improve. They follow your answer on Mycel's stats screen; nothing else is asked. What is sent: https://wiki.mycelium-gg.org/privacy#stats-morebeaconcolors

## [1.1.1] - 2026-11-02

### Changed
- Settings screen: on/off settings now show their real state when it opens (fixed in Mycel).
- Built against the newest Fabric API, Forge and NeoForge, including the first stable NeoForge for 26.3.

## [1.1.0] - 2026-10-28

### Added

- Minecraft 26.2 and 26.1 (26.1, 26.1.1, 26.1.2) support. Each release now
  has jars for 26.3, 26.2 and 26.1 on Fabric, Forge and NeoForge; pick the
  ones named for your Minecraft.

### Changed

- Requires Mycel 1.5.1 (for the same Minecraft version).

## [1.0.1] - 2026-10-26

### Changed

- Requires Mycel 1.4.1.

### Fixed

- NeoForge no longer stops at a warning screen during start-up because of
  this mod's icon setting.

## [1.0.0] - 2026-10-25

### Added

- Initial release.
- A button beside the beacon screen opens a beam color menu.
- Styles: vanilla, solid, gradient, rainbow, color cycle and pulse.
- Gradients and pulses take 2 to 16 colors: add, remove, reverse.
- 16 dye colors plus 8 extras, a color picker and a hex field.
- Slow, normal and fast speeds for the animated styles.
- Live preview: the beacon on its iron blocks under a day or night sky,
  the beam painted as in the world; scroll to zoom out to the whole beam.
- Servers with the mod save colors in `<world>/data/morebeaconcolors.json`
  and show them to every player with the mod. On servers without it,
  colors are kept in `config/morebeaconcolors-local.json` and only you see
  them. Players without the mod see vanilla beams.
- Colors of broken beacons are forgotten.
- Config (`config/morebeaconcolors.json`): `enabled`, `showButton`,
  `allowAnimated`, `debug`; live reload via
  `/mycel reload morebeaconcolors`; config screen from the mod list on Forge
  and NeoForge.
- Built on Mycel 1.4.0
  (`com.github.MyCelium-GG:MyCel-Lib:1.3.1` via JitPack).
