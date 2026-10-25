# Changelog

All notable changes to More Beacon Colors are documented here.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
More Beacon Colors is versioned semantically (`MAJOR.MINOR.PATCH`).
It runs on Minecraft 26.3 (Fabric + Forge + NeoForge, Java 25) and requires
[Mycel](https://github.com/MyCelium-GG/MyCel-Lib) (`my.celium.org`).

## [1.0.0] - 2026-10-24

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
