# Android diagnostics

Open **Options → Diagnostics** in the title/setup menu or in a running game.
Reports stay on the device; export and sharing are explicit actions.

- **Start / stop performance recording** records one-second summaries of frame,
  CPU and GPU durations, frame phases, slow frames and named CPU/worker stages.
  Samples include memory, thermal data when available, streaming queues, rendering
  counters and the current graphics adapter. It is off by default.
- **Toggle detailed GPU profiling** enables the existing renderer's pass timers.
  The report includes p50/p95/p99/max and completed sample counts from their
  rolling 15-sample window. It adds overhead and requires GPU timestamp support;
  absent timings mean unavailable data, not zero cost. Readback is asynchronous.
- **Export diagnostic ZIP** packages bounded tails of Starling's existing
  `logs/latest.log`, `logs/previous.log` and crash reports, session checkpoints,
  Android exit history and retained performance reports. It uses the same logger
  and panic reporter as the game.
- **Share latest diagnostic ZIP** opens Android's share chooser for that ZIP only.
  It grants read access to that file, never to the app's data folder.

The session checkpoint records startup/menu/world-loading/in-world phases and
memory. A missing clean-exit marker indicates an interrupted process, which can
also be an Android kill or a force stop. It does not by itself prove a crash.
Fatal errors returned through the normal entry point keep that marker unset.

The Diagnostics screen itself needs a working renderer. A repeated failure before
the setup screen leaves reports on disk, but the in-game export cannot be opened.
Debug builds allow developers to retrieve private reports with `adb run-as`;
release builds do not. A renderer-independent recovery/export entry point is
still missing.

On Android 11 and newer, `ApplicationExitInfo` can distinguish native/Java crashes,
ANRs, low-memory and other exits. Availability depends on the OS. Up to eight
system traces, at most 1 MiB each, stay in private app storage. Binary native
traces contribute only a bounded crashed-thread summary to the ZIP; register and
memory dumps remain private. No replacement signal handler is installed.

Performance JSONL files in private `performance/` are capped at 8 MiB and 30
minutes, with three retained reports and an eight-sample writer queue. Diagnostic
ZIP payloads are capped at 4 MiB, 32 source files and 256 KiB per ordinary input;
three ZIPs are retained. Long JSONL exports retain a complete header and trailing
complete rows; the manifest identifies truncated or omitted inputs.

The exporter excludes account files, worlds and configuration and redacts known
credential/address patterns. Review the resulting archive before sharing because
arbitrary third-party log text cannot be guaranteed free of private data. Nothing
is uploaded automatically. CPU spans nest, worker totals overlap and GPU timings
arrive late: these numbers identify correlations, not proof of lock contention.

This port reuses Horizon Android's existing diagnostics, exit-history, tombstone
and performance recorder code. It adapts them to Starling's logging, streaming
workers, GPU timers, translation keys and in-game settings. It does not add an
automatic backend benchmark, change the selected graphics API or add a 32-bit APK.

Run `python tools/test_android_diagnostics.py` to test the production writers,
exporter, tombstone parser, frame summaries and span aggregation on the host.
Build an ARM64 APK with `python packaging/android/build_apk.py` for device tests.
