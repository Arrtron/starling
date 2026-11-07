# Experimental Android ARMv7 builds

The release APK remains ARM64. ARMv7 builds use `armeabi-v7a` and require Android
8 or newer. CPU ABI support alone does not guarantee that a device's graphics
driver can run the engine. Older PowerVR/Mali/Adreno hardware needs separate
on-device validation; a successful run on a modern tablet is not evidence for it.

## Build

Install the Android SDK/NDK pinned in `packaging/android/build_apk.py`, a JDK,
`cargo-ndk` and Rust's `armv7-linux-androideabi` target. Then:

```sh
python packaging/android/build_apk.py --abi armeabi-v7a --dev
```

`--abi arm64-v8a` preserves the default build. Each APK contains only its selected
ABI. The ARMv7 Java launchers and C++ runtime are 32-bit, and resource preparation
selects the pinned Android ARM32 OpenJDK 25 archive with SHA-256 verification.
The PR workflow builds ARMv7 as a separate experimental artifact; this does not
publish an ARMv7 release or replace the ARM64 release job.

## Graphics and address space

The renderer still selects available backends and checks their capabilities.
Legacy GLES driver workarounds are adapted from the Android fork: conditional
sprite selection, unrolled RGSS/overlay/emissive lookups, packed terrain varyings
and conditional HDR texture selection. Starling's tiled-face RGSS behavior, HDR
scene varying and all 128 emissive entries are preserved. These transformations
apply only to GLES; the Vulkan WGSL remains byte-for-byte unchanged.
Vulkan is optional in the ARMv7 manifest so a GLES-only device can attempt to
start; this does not guarantee support for every GLES driver. Actual API,
texture-array and shader limits remain authoritative.

Existing terrain texture-array splitting follows the adapter's layer limit,
with at most eight arrays. A pack that needs more layers than those arrays hold
is rejected rather than silently losing textures.

In a 32-bit process the shared GUI/entity atlas is bounded by the GPU's 2D side
limit and a 4096-pixel process limit, whichever is smaller. The latter bounds its
RGBA pixels to 64 MiB before decoded images, staging and other allocations.
Existing atlas packing can reduce large textures while preserving logical GUI
sizes. A pack that still cannot fit can fail to load.

The block-sprite top-level texture budget defaults to 64 MiB and is capped at
128 MiB on 32-bit builds, even if a larger value was saved. Mipmaps, retained
source textures, PBR maps and the rest of the game require additional memory;
this is not a total-process memory limit. ARM64 budgets remain unchanged.
Normal GPU arena growth on Android ARM32 uses steps of at most 8 MiB, following
the Android fork's policy while retaining Starling's newer paged arenas. A single
larger allocation request can still require a larger step.

The updater selects the APK matching the process ABI. A release with only an
ARM64 APK is not offered as an ARMv7 update. Native-mod libraries must also match
the process ABI; existing ARM64 prebuilt native mods are not ARMv7 builds.

## Relation to diagnostics

Android preparation-lock fixes and renderer-independent diagnostic recovery are
provided separately by PR #272. Until those changes reach the target branch,
full on-device preparation still has that known dependency. Pre-prepared private
resources can be used to validate the ARMv7 renderer independently. Never commit
Minecraft files, generated resource tables or prepared resources.
