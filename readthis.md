# UlyxSpigot Handoff (Paper Base)

## 1) Muc tieu hien tai
- Fork Paper thanh **UlyxSpigot**.
- Them config rieng: `ulyxspigot/ulyxspigot.yml`.
- Da wire thu 2 tinh nang de test:
  - `asynchronous.pathfinding`
  - `asynchronous.data-saving`
- Them config brand:
  - `server-brand-name-display` (mac dinh `UlyxSpigot`) de hien thi brand trong ping/F3.

## 2) Nhung gi da duoc them
- File config:
  - `ulyxspigot/ulyxspigot.yml`
- Class config + async helpers:
  - `paper-server/src/main/java/org/ulyxspigot/ulyxspigot/UlyxConfig.java`
  - `paper-server/src/main/java/org/ulyxspigot/ulyxspigot/async/UlyxAsyncPathfinding.java`
  - `paper-server/src/main/java/org/ulyxspigot/ulyxspigot/async/UlyxAsyncDataSaving.java`
- Patch da sua/them:
  - `paper-server/patches/features/0013-Optimize-Pathfinder-Remove-Streams-Optimized-collect.patch`
    - goi `UlyxAsyncPathfinding` trong `PathFinder.findPath(...)` khi config bat.
  - `paper-server/patches/features/0020-Incremental-chunk-and-player-saving.patch`
    - thay `this.playerIo.save(player)` bang nhanh async qua `UlyxAsyncDataSaving` khi config bat.
  - `paper-server/patches/features/0033-UlyxSpigot-Brand-Display.patch`
    - doi `MinecraftServer#getServerModName()` sang `UlyxConfig.getServerBrandNameDisplay()`.

## 3) Trang thai git local luc handoff
- Modified:
  - `.gitignore`
  - `paper-server/patches/features/0013-Optimize-Pathfinder-Remove-Streams-Optimized-collect.patch`
  - `paper-server/patches/features/0020-Incremental-chunk-and-player-saving.patch`
- Untracked:
  - `paper-server/patches/features/0033-UlyxSpigot-Brand-Display.patch`
  - `paper-server/src/main/java/org/ulyxspigot/`
  - `ulyxspigot/`

## 4) Van de build da gap tren Termux (Android)
- Ban dau fail `:paper-server:cloneSpigotBuildData` do `git` safe-directory (dubious ownership) trong thu muc tam random:
  - `paper-server/.gradle/caches/paperweight/taskCache/cloneSpigotBuildData.zip--*`
- Da tim ra va fix bang:
  - `git config --global --add safe.directory '/.../paper-server/.gradle/caches/paperweight/taskCache/*'`
- Sau do clone step da qua duoc.
- Build van fail o `:paper-server:compileJava` voi loi `package net.minecraft.* does not exist` trong `ca/spottedleaf/moonrise/...`.
- Dang nghi do environment/cache build tren Android bi vo khi task nang (decompile/setup) bi crash giua chung.

## 5) Lenh nen chay tren VPS Ubuntu 22.04
Chay trong root repo:

```sh
git config --global --add safe.directory "$(pwd)"
git config --global --add safe.directory "$(pwd)/paper-server/.gradle/caches/paperweight/taskCache/*"

GRADLE_OPTS='-Xmx1024m -Xms512m' ./gradlew --no-daemon --max-workers=4 :paper-server:setupMacheSources
GRADLE_OPTS='-Xmx1024m -Xms512m' ./gradlew --no-daemon --max-workers=4 :paper-server:createMojmapBundlerJar
```

Neu van bi loi compile `net.minecraft.* does not exist`, thu:

```sh
GRADLE_OPTS='-Xmx1024m -Xms512m' ./gradlew --no-daemon --max-workers=4 :paper-server:clean
GRADLE_OPTS='-Xmx1024m -Xms512m' ./gradlew --no-daemon --max-workers=4 :paper-server:setupMacheSources
GRADLE_OPTS='-Xmx1024m -Xms512m' ./gradlew --no-daemon --max-workers=4 :paper-server:createMojmapBundlerJar --stacktrace
```

## 6) Checklist test sau khi co jar
1. Chay server, xac nhan tao file `ulyxspigot/ulyxspigot.yml`.
2. Sua `server-brand-name-display`, restart, check brand o ping/F3.
3. Test `asynchronous.pathfinding.enabled`:
   - `true`: xem log co dong `[UlyxSpigot] Async pathfinding enabled...`.
   - `false`: dam bao fallback sync khong crash.
4. Test `asynchronous.data-saving.enabled`:
   - `true`: xem log co dong `[UlyxSpigot] Async data-saving enabled...`.
   - trigger save player (logout / save-all), theo doi co exception hay khong.
5. Chay regression co ban: join/quit, di chuyen mob, save world, restart server.

## 7) Ghi chu ky thuat quan trong
- Hai feature async hien tai la ban thu nghiem de test.
- `pathfinding` dang submit vao worker roi `future.get()`, nghia la van cho ket qua ngay trong luong goi ham (khong fire-and-forget).
- `data-saving` dang offload `playerIo.save(player)` sang pool, can theo doi ky thread-safety khi test thuc te.
- Config da bo qua `parallel/world-ticking` theo yeu cau user (khong dua vao file config hien tai).
