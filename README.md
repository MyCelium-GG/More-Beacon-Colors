# More Beacon Colors

[![License](https://img.shields.io/github/license/MyCelium-GG/More-Beacon-Colors)](LICENSE)

Any color for your beacon beam, picked from a menu in the beacon. A button
beside the beacon screen opens it: solid colors from the 16 dyes or any
custom color, gradients through as many colors as you like, rainbows, color
cycles and pulses, with a live preview. On a server with the mod, colors are
saved with the world and every player with the mod sees them. On a server
without it, they are kept on your computer and only you see them.

Built on [Mycel](https://github.com/MyCelium-GG/MyCel-Lib) (`my.celium.org`), the shared
core for the MyCelium ecosystem: configuration, platform abstraction,
networking and diagnostics are reused.

| | |
|---|---|
| Mod id | `morebeaconcolors` |
| Minecraft | **26.3**, **26.2**, **26.1** (26.1-26.1.2) · Java **25** |
| Loaders | Fabric, Forge, NeoForge |
| Side | Both (client needed for the menu; the server shares colors) |
| Docs | [wiki.mycelium-gg.org/morebeaconcolors](https://wiki.mycelium-gg.org/morebeaconcolors/) |

![Three colored beacon beams](https://wiki.mycelium-gg.org/morebeaconcolors/img/beams.png)

![The beam color menu](https://wiki.mycelium-gg.org/morebeaconcolors/img/menu-gradient.png)

## Behavior

- **The button**: open a beacon and a beacon button appears just outside
  the top-right corner of its window, over a strip in the beam's current
  color. Click it for the color menu. *Back* returns to the beacon.
- **Styles**:
  - *Vanilla*: no override, stained glass decides (the default).
  - *Solid*: one color for the whole beam.
  - *Gradient*: fades through 2 to 16 colors, the first at the bottom, the
    last at the top of the world.
  - *Rainbow*: rainbow stripes rising up the beam.
  - *Color cycle*: the whole beam runs through every color.
  - *Pulse*: the whole beam fades through 2 to 16 colors and back.
- **Colors**: 16 dye colors and 8 extras, a saturation/brightness square
  with a hue bar, and a hex field. Gradients and pulses show a row of
  colors: click one to edit it, *+* adds a color after the selected one,
  *-* removes it, *Reverse* flips the order.
- **Speed**: slow, normal or fast for the animated styles.
- **Preview**: the beacon on its iron blocks under a day or night sky
  (click to switch), with the beam painted exactly as in the world. Scroll
  to zoom out up to the whole beam.
- **Apply** (or Enter) sets the style; *Reset* goes back to vanilla. The
  footer says when there are changes you have not applied.

### Who sees what

| Setup | Result |
|---|---|
| Server and client with the mod | Colors are saved with the world and everyone with the mod sees them. |
| Client with the mod, server without | Colors are kept in `config/morebeaconcolors-local.json` (per server address) and only you see them. |
| Client without the mod | Vanilla beams. Nothing to install to join. |

On a server, a player can only color a beacon they have open (within
8 blocks, like the beacon menu). Colors of a beacon that was broken are
forgotten.

### Where colors live

- `<world>/data/morebeaconcolors.json` on the server: one entry per colored
  beacon (dimension, position, style, colors as `#RRGGBB`). Hand-editable;
  a file that no longer parses is kept as `.broken`.
- `config/morebeaconcolors-local.json` on the client, for servers without
  the mod.

## Configuration (`config/morebeaconcolors.json`)

| Key | Default | Meaning |
|---|---|---|
| `enabled` | `true` | Master switch (also toggleable live via `Mycel.setEnabled`). |
| `showButton` | `true` | Show the color button on the beacon screen (client). |
| `allowAnimated` | `true` | Allow rainbow, color cycle and pulse beams (server). |
| `debug` | `false` | Log color changes, syncing and clean-ups. |

Reload live with `/mycel reload morebeaconcolors`. A config screen is
available from the mod list on Forge and NeoForge.

## Building

JDK 25, then:

```sh
./gradlew build            # all loaders + tests
./gradlew :common:test     # unit tests only
./gradlew :fabric:runClient
```

Styles, beam painting, colors and the JSON store live in `common/.../data`
without Minecraft classes and are covered by unit tests. The client repaints
beams by swapping the beacon's beam sections each tick (no mixins).

Mycel itself comes from [JitPack](https://jitpack.io/#MyCelium-GG/MyCel-Lib)
as one universal jar for all loaders
(the version is `mycel_version` in `gradle.properties`). No local setup needed.

## Releasing

Push a version tag (`git tag 1.0.0 && git push origin 1.0.0`). The
`release` workflow builds the three loader jars, attaches them to a GitHub
Release and uploads them to CurseForge once `curseforge_project_id` is set in
`gradle.properties` (token in the `CURSEFORGE_TOKEN` secret).

## License

MIT, see [LICENSE](LICENSE).
