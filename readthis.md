# UlyxSpigot Handoff (Paper Base)

## 1) Muc tieu hien tai
- Fork Paper thanh **UlyxSpigot** voi config rieng `ulyxspigot/ulyxspigot.yml`.
- Hoan thien wire config -> code cho cac option quan trong truoc, build qua GitHub Actions.
- Theo yeu cau hien tai: **khong build local tren may Termux**.

## 2) Trang thai config/code hien tai (cap nhat 2026-03-21)
- File config:
  - `ulyxspigot/ulyxspigot.yml`
- Loader config:
  - `paper-server/src/main/java/org/ulyxspigot/ulyxspigot/UlyxConfig.java`
  - duoc goi tu `CraftServer` (`UlyxConfig.ensureLoaded()`)
- Da co code hoat dong cho:
  - `server-brand-name-display`
  - `asynchronous.tracker`
  - `asynchronous.pathfinding`
  - `asynchronous.data-saving`
  - `asynchronous.inventory-updates`
  - `asynchronous.packet-sending`
  - Nhieu behavior toggle (portal/chat/world save/weather/collision/AI/...)
  - `load-chunks`, `water-sensitive`, `entity-update-interval`
  - `disableGrassLightChecks`, `disableSnowLightChecks`, `disableSpawnerLightChecks`
  - Nhom `spawner.*`
  - `combat.mace.*`
  - `combat.knockback.*`

## 3) Cap nhat moi vua wire trong turn nay
### `alternative-farms.raids.old-behavior`
- Da wire vao death flow cua raider trong:
  - `paper-server/src/main/java/org/bukkit/craftbukkit/event/CraftEventFactory.java`
- Hanh vi:
  - Neu `old-behavior: true` va nguoi choi giet patrol captain:
    - cap lai `BAD_OMEN` theo kieu old behavior
    - dong thoi chan ominous bottle drop

### `alternative-farms.raids.drop-ominous-bottles`
- Da wire vao cung death flow trong `CraftEventFactory`.
- Hanh vi:
  - Neu `old-behavior: false` va `drop-ominous-bottles: false`:
    - bo `OMINOUS_BOTTLE` khoi drops cua raider
  - Neu `old-behavior: false` va `drop-ominous-bottles: true`:
    - giu hanh vi mac dinh (co the drop)

### Ho tro bo sung trong raid trigger
- File:
  - `paper-server/patches/sources/net/minecraft/world/entity/raid/Raids.java.patch`
- Khi `RaidTriggerEvent` bi cancel, effect bi remove theo mode:
  - `old-behavior: true` -> remove `BAD_OMEN`
  - `old-behavior: false` -> remove `RAID_OMEN`

### Hotfix tracker (item/wind charge rubber-band)
- File:
  - `paper-server/src/main/java/org/ulyxspigot/ulyxspigot/async/UlyxAsyncTracker.java`
- Nguyen nhan kha nang cao: non-living entity (item/projectile) bi tracker tick off-thread gay packet position dao chieu.
- Da sua:
  - ep `ItemEntity` va toan bo `Projectile` tick sync tren main thread
  - giu async cho nhom non-living con lai
- Workaround ngay lap tuc (neu chua co ban build moi):
  - dat `asynchronous.tracker.enabled: false` trong `ulyxspigot.yml`


### Combat update moi (turn nay)
- Da wire code hoat dong cho 5 key:
  - `combat.preventCriticalsIfSprinting`
  - `combat.criticalModifier`
  - `combat.disableSweepingEdge`
  - `combat.disableShieldEffectiveness`
  - `combat.disableHitDelay`
- File chinh:
  - `paper-server/patches/sources/net/minecraft/world/entity/player/Player.java.patch`
  - `paper-server/patches/sources/net/minecraft/world/entity/LivingEntity.java.patch`

### Combat update tiep theo (batch knockback)
- Da wire code hoat dong cho 5 key:
  - `combat.knockback.oldKnockback`
  - `combat.knockback.vertical`
  - `combat.knockback.horizontal`
  - `combat.knockback.friction`
  - `combat.knockback.verticalLimit`
- File chinh:
  - `paper-server/patches/sources/net/minecraft/world/entity/player/Player.java.patch`


### Combat update tiep theo (batch projectile + fishing + kb-scaling)
- Da wire code hoat dong cho 3 key:
  - `combat.disableKnockbackScaling`
  - `combat.fishingHooksDoKnockback`
  - `combat.fishingHooksPullEntities`
- File chinh:
  - `paper-server/patches/sources/net/minecraft/world/entity/player/Player.java.patch`
  - `paper-server/patches/sources/net/minecraft/world/entity/projectile/FishingHook.java.patch`

### Combat update tiep theo (batch tiep theo)
- Da wire code hoat dong cho 8 key:
  - `combat.enableBowBoosting`
  - `combat.oldCollisionsProjectile`
  - `combat.disableNetheriteKnockbackResistance`
  - `combat.oldSharpnessDamageBuff`
  - `combat.oldToolAttackDamage`
  - `combat.oldEnchantedGappleEffects`
  - `combat.legacyBlastProtection`
  - `combat.revertArmorProtection`
- File chinh:
  - `paper-server/patches/sources/net/minecraft/world/entity/projectile/Projectile.java.patch`
  - `paper-server/patches/sources/net/minecraft/world/entity/player/Player.java.patch`
  - `paper-server/patches/sources/net/minecraft/world/entity/LivingEntity.java.patch`
  - `paper-server/patches/sources/net/minecraft/world/item/component/Consumable.java.patch`

### Developer/Experimental update (turn nay)
- Da them vao config + UlyxConfig cho nhom key moi:
  - `experimental.reducePlayerChunkSourceUpdates`
  - `experimental.reduceChunkMidTickTaskExecution`
  - `experimental.disableChunkNewerVersionLoadCheck`
  - `developer.recalculateChunksOutOfBounds`
  - `developer.allowInvalidEnchantLevels`
  - `developer.disableAsyncCatcher`
  - `developer.disableSessionLockFile`
- Da wire code hoat dong cho 5 key:
  - `experimental.disableChunkNewerVersionLoadCheck`
  - `experimental.reducePlayerChunkSourceUpdates`
  - `developer.allowInvalidEnchantLevels`
  - `developer.disableAsyncCatcher`
  - `developer.disableSessionLockFile`
- Reload guard cho 2 key nhay cam:
  - Neu `experimental.disableChunkNewerVersionLoadCheck: true` hoac `developer.disableSessionLockFile: true` trong file config, `/ulyx reload` se bi tu choi.
  - Cach xu ly: dat key ve `false` roi `/ulyx reload`, hoac restart server.
- File chinh:
  - `ulyxspigot/ulyxspigot.yml`
  - `paper-server/src/main/java/org/ulyxspigot/ulyxspigot/UlyxConfig.java`
  - `paper-server/src/main/java/org/spigotmc/AsyncCatcher.java`
  - `paper-server/src/main/java/org/bukkit/craftbukkit/inventory/CraftMetaItem.java`
  - `paper-server/src/main/java/org/bukkit/craftbukkit/inventory/CraftMetaEnchantedBook.java`
  - `paper-server/patches/sources/net/minecraft/world/level/chunk/storage/SerializableChunkData.java.patch`
  - `paper-server/patches/sources/net/minecraft/world/level/storage/LevelStorageSource.java.patch`
  - `paper-server/patches/sources/net/minecraft/server/network/ServerGamePacketListenerImpl.java.patch`

## 4) Cac key config van chua co code tac dong
- `behavior.disableInitialWorldSpawn`
- `experimental.reduceChunkMidTickTaskExecution`
- `developer.recalculateChunksOutOfBounds`

### Update moi (turn nay)
- Da wire `combat.alternative-hit-registration`:
  - Leniency hitbox/range trong `handleInteract` khi attack packet den tre/lech nhe.
  - File: `paper-server/patches/sources/net/minecraft/server/network/ServerGamePacketListenerImpl.java.patch`
- Da wire `combat.imitateSwordBlocking`:
  - Cho phep block damage bang sword (fallback theo trang thai su dung item; co duong fallback crouch + mainhand sword).
  - File: `paper-server/patches/sources/net/minecraft/world/entity/LivingEntity.java.patch`

## 5) Trang thai git local (sau turn nay)
- Dang co modified files:
  - `ulyxspigot/ulyxspigot.yml`
  - `paper-server/src/main/java/org/ulyxspigot/ulyxspigot/UlyxConfig.java`
  - `paper-server/src/main/java/org/spigotmc/AsyncCatcher.java`
  - `paper-server/src/main/java/org/bukkit/craftbukkit/inventory/CraftMetaItem.java`
  - `paper-server/src/main/java/org/bukkit/craftbukkit/inventory/CraftMetaEnchantedBook.java`
  - `paper-server/patches/sources/net/minecraft/world/level/chunk/storage/SerializableChunkData.java.patch`
  - `paper-server/patches/sources/net/minecraft/world/level/storage/LevelStorageSource.java.patch`
  - `paper-server/patches/sources/net/minecraft/server/network/ServerGamePacketListenerImpl.java.patch`
  - `readthis.md`

## 6) Build/CI
- Khong chay build local theo yeu cau.
- Quy trinh de nghi:
  1. Commit
  2. Push len GitHub
  3. De GitHub Actions build va report

## 7) Checklist test nhanh sau khi Actions build xong
1. `old-behavior: true`, giet patrol captain:
   - player nhan `BAD_OMEN`
   - khong co ominous bottle drop
2. `old-behavior: false`, `drop-ominous-bottles: false`:
   - raider khong drop ominous bottle
3. `old-behavior: false`, `drop-ominous-bottles: true`:
   - raider drop theo hanh vi mac dinh
4. Test regression raid co ban:
   - trigger raid, finish raid, stop raid, check log va plugin event lien quan

## 8) Session handoff nhanh (de mo session khac tam thoi)
- Neu mo session moi, dung prompt ngan nay de tiep tuc dung context:
  - `Doc readthis.md truoc, tom tat trang thai hien tai trong 5 dong, roi tiep tuc task: <task-moi>.`
- Neu ban sua them code trong session tam, gui lai 1 trong 2 cach sau de session tiep theo "bien" code moi nhanh nhat:
  1. `git diff -- <duong-dan-file>`
  2. commit hash vua tao
- Nguyen tac tiep tuc:
  - khong build local tren may Termux
  - uu tien sua code + cap nhat patch/readthis
  - build de GitHub Actions xu ly

## 9) Dieu tra tuong thich Leaf vs plugin Wind Charge
- Doi chieu truc tiep trong thu muc `leaf-forcode`: khong tim thay patch rieng cho `Projectile.java`/`AbstractWindCharge` o nhanh Leaf hien co.
- Suy ra: Leaf dang giu hanh vi Paper goc o nhanh `ProjectileHitEvent` bi cancel (khong ep `discard` rieng cho `AbstractWindCharge`).
- Khac biet cua Ulyx truoc do: co them nhanh custom ep `discard(...Cause.HIT)` khi plugin cancel hit cua Wind Charge.
- Trang thai hien tai: theo yeu cau da restore lai nhanh `discard(...Cause.HIT)` trong `Projectile.java.patch` (projectile ve nhu cu).

## 10) Ghi chu test tiep theo
1. Chay lai plugin can thiep Wind Charge tren ban code moi.
2. Test case plugin cancel `ProjectileHitEvent` voi Wind Charge: dam bao khong con hien tuong item/projectile "quay lai" bat thuong.
3. Neu van loi, bat debug trong plugin de theo doi event cancel/respawn/velocity sau moi hit (hien tai projectile dang o trang thai goc cua Ulyx theo yeu cau).

### Misc log-cleaner + config comments fix (turn nay)
- Da them key moi:
  - `misc.log-cleaner.enabled`
  - `misc.log-cleaner.older-than`
  - `misc.log-cleaner.max-count`
- Vi tri trong config: dat ngay duoi `developer`.
- Da them code hoat dong:
  - Log cleaner chay 1 lan khi startup (khong chay khi `/ulyx reload`).
  - Xoa log cu hon `older-than` ngay.
  - Neu `max-count >= 0`, giu lai `max-count` file moi nhat, xoa phan con lai.
- Da fix tao config moi co comment `#`:
  - Neu thieu `ulyxspigot/ulyxspigot.yml`, server se copy tu resource template co comment.
  - Bat `parseComments(true)` de giu comment khi doc/ghi config.
- File chinh:
  - `paper-server/src/main/java/org/ulyxspigot/ulyxspigot/UlyxConfig.java`
  - `ulyxspigot/ulyxspigot.yml`
  - `paper-server/src/main/resources/configurations/ulyxspigot.yml`

### Fixes update (turn nay)
- Da wire code hoat dong cho:
  - `fixes.useSecureSeedLogic`
- Pham vi ban nay (an toan, de merge):
  - Khi bat config, slime chunk logic se dung seed da obfuscate thay vi seed raw.
  - Ap dung ca 2 duong:
    - `Chunk#isSlimeChunk` (Bukkit API)
    - Slime spawn check trong NMS (`Slime.java`)
- File chinh:
  - `paper-server/src/main/java/org/bukkit/craftbukkit/CraftChunk.java`
  - `paper-server/patches/sources/net/minecraft/world/entity/monster/Slime.java.patch`
- Da wire code hoat dong cho:
  - `fixes.fixPluginPlaceholderExploits`
- Diem hook:
  - `paper-server/src/main/java/io/papermc/paper/adventure/ChatProcessor.java`
- Hanh vi khi bat config:
  - Sanitizes `%` token khong hop le trong legacy chat format truoc `String.format`.
  - Giam rui ro format token ngoai y muon (`%placeholder%`, `%x`, `%...`) gay parse sai/exception.
  - Van giu `%s`, `%1$s`, `%2$s`, `%%` nhu binh thuong.



### Limiters update (turn nay)
- Da them block `limiters` vao ca 2 file config:
  - `paper-server/src/main/resources/configurations/ulyxspigot.yml`
  - `ulyxspigot/ulyxspigot.yml`
- Vi tri: dat ngay duoi `fixes`.
- Da wire logic cho nhom `limiters.redstone.*`:
  - `maxRedstonePerTick`: gioi han neighbor-update lien quan redstone
  - `maxPistonPerTick`: gioi han trigger piston moi tick
  - `maxHopperPerTick`: gioi han hopper transfer tick
  - `maxDispenserPerTick`: gioi han dispenser tick
  - `maxDropperPerTick`: gioi han dropper tick
  - `maxObserverPerTick`: gioi han observer tick
  - `maxPistonPush`: gioi han so block piston co the day (toi da 12)
  - `block-threshold.OBSERVER`: ap dung nguong bo sung cho observer
- File chinh:
  - `paper-server/src/main/java/org/ulyxspigot/ulyxspigot/UlyxConfig.java`
  - `paper-server/src/main/java/org/ulyxspigot/ulyxspigot/limiters/UlyxRedstoneLimiter.java`
  - `paper-server/patches/sources/net/minecraft/world/level/redstone/NeighborUpdater.java.patch`
  - `paper-server/patches/sources/net/minecraft/world/level/block/ObserverBlock.java.patch`
  - `paper-server/patches/sources/net/minecraft/world/level/block/piston/PistonBaseBlock.java.patch`
  - `paper-server/patches/sources/net/minecraft/world/level/block/entity/HopperBlockEntity.java.patch`
  - `paper-server/patches/sources/net/minecraft/world/level/block/DispenserBlock.java.patch`
  - `paper-server/patches/sources/net/minecraft/world/level/block/DropperBlock.java.patch`
- Da wire logic cho `limiters.remove-excess.*`:
  - `removeExcessMinecarts`
  - `removeExcessBoats`
  - `excessMinecartsLimit`
  - `excessBoatsLimit`
- Cach hoat dong:
  - Khi vehicle va cham, neu so luong minecart/boat cung cum va cham vuot nguong thi se remove bot entity du (uu tien remove entity dang va cham, bo qua entity dang co passenger/vehicle).
- File chinh bo sung:
  - `paper-server/src/main/java/org/ulyxspigot/ulyxspigot/limiters/UlyxVehicleLimiter.java`
  - `paper-server/patches/sources/net/minecraft/world/entity/vehicle/boat/AbstractBoat.java.patch`
  - `paper-server/patches/sources/net/minecraft/world/entity/vehicle/minecart/AbstractMinecart.java.patch`
- Da wire logic cho `limiters.non-tickable-entities`:
  - Hook trong vong tick entity tai `ServerLevel#tickNonPassenger` va `ServerLevel#tickPassenger`.
  - Neu entity type nam trong danh sach thi bo qua tick (khong chay `tick()/rideTick()`).
  - Co chan an toan: `PLAYER` van duoc tick de tranh gay loi nang server/gameplay.
- File chinh bo sung:
  - `paper-server/patches/sources/net/minecraft/server/level/ServerLevel.java.patch`


### Particles + Sounds update (turn nay)
- Da them doc config + getter cho nhom key `particles.*` va `sounds.*` trong `UlyxConfig`.
- Da wire code hoat dong (5 key de truoc):
  - `particles.disableSprintParticles`
  - `particles.disableFallParticles`
  - `particles.disableDeathParticles`
  - `particles.disableNewCombatParticles`
  - `sounds.disableShoulderEntityAmbientSound`
- Diem hook:
  - `paper-server/patches/sources/net/minecraft/world/entity/Entity.java.patch`
  - `paper-server/patches/sources/net/minecraft/world/entity/LivingEntity.java.patch`
  - `paper-server/patches/sources/net/minecraft/server/level/ServerLevel.java.patch`
  - `paper-server/patches/sources/net/minecraft/server/level/ServerPlayer.java.patch`
  - `paper-server/src/main/java/org/ulyxspigot/ulyxspigot/UlyxConfig.java`


- Da wire them 5 key (dot nay):
  - `particles.disableBlockBreakParticles`
  - `particles.disableWaterSplashParticles`
  - `particles.disableBubbleColumnParticles`
  - `particles.disableEffectParticles`
  - `sounds.disableNewCombatSounds`
- Cach hook:
  - Particle filters duoc xu ly tap trung trong `ServerLevel#sendParticlesSource` theo particle key name.
  - Combat attack sounds duoc gate trong `Player#playServerSideSound` va nhanh deflect projectile.

- Da wire them 2 key cuoi (dot nay):
  - `particles.disableSpawnerParticles`
  - `sounds.disableFootStepSounds`
- Cach hook:
  - Packet-level filter trong `ServerCommonPacketListenerImpl#send(...)`.
  - `disableFootStepSounds`: chan packet sound co sound-key chua `step`.
  - `disableSpawnerParticles`: chan packet particle `smoke`/`flame` neu vi tri particle trung/gan block `SPAWNER`.
- File bo sung:
  - `paper-server/src/main/java/org/ulyxspigot/ulyxspigot/network/UlyxPacketFilters.java`
  - `paper-server/patches/sources/net/minecraft/server/network/ServerCommonPacketListenerImpl.java.patch`


### Async pathfinding update (turn nay)
- Da wire hoat dong cho `asynchronous.pathfinding.enabled` va `asynchronous.pathfinding.threads`.
- Diem hook: `PathFinder#findPath(...)`.
- Cach hoat dong:
  - Luc tinh path A* se duoc boc qua `UlyxAsyncPathfinding.supply(...)`.
  - Neu config tat: chay sync nhu cu.
  - Neu config bat: pathfinding chay tren worker pool theo so thread da config.
- File:
  - `paper-server/patches/sources/net/minecraft/world/level/pathfinder/PathFinder.java.patch`

### Async + reference sync update (turn nay)
- Da push batch code moi len `origin/main` truoc do voi commit `ba8e511`.
- Noi dung chinh cua batch nay:
  - Them `asynchronous.mob-spawning.enabled` va class `UlyxAsyncMobSpawning`.
  - Cap nhat `UlyxAsyncWorldTicking`.
  - Them limiter `limiters.item.max-merge-attempts-per-tick` qua `UlyxItemMergeLimiter` + hook trong `ItemEntity`.
  - Them them cac toggle/config moi trong `UlyxConfig` va 2 file yaml cho nhom:
    - `asynchronous.chunks-sending`
    - `experimental.compactPalettes`
    - `experimental.netty-transport-type`
    - `misc.sentry.*`
    - `sounds.disablePiglinAngerSound`
    - `sounds.disableShieldSounds`
    - `sounds.disablePistonSounds`
    - `performance.optimiseDataPacks`
    - `performance.cacheWorldConfigurations`
    - `performance.disableServerDebug`
    - `performance.biome-seed.*`
    - `performance.dynamic-brain.*`
    - `behavior.disableProjectileMarginExpansion`
    - `behavior.structures.mineshaftMinYLevel`
    - `behavior.structures.strongholdMinYLevel`
    - `behavior.structures.strongholdMaxYLevel`
- Patch/NMS hook moi trong batch nay:
  - `NaturalSpawner.java.patch`
  - `ProjectileUtil.java.patch`
  - `MineshaftStructure.java.patch`
  - `StrongholdStructure.java.patch`
  - `PistonBaseBlock.java.patch`
  - `EventLoopGroupHolder.java.patch`
  - `ServerChunkCache.java.patch`
  - `MinecraftServer.java.patch`
  - `ZombifiedPiglin.java.patch`
- Reference source da dua vao repo de doi chieu code:
  - `divinemc-forcode/`
  - `leaf-forcode/`
  - `Pufferfish/`
- Luu y reference folders:
  - Chi de tham chieu code, khong duoc include trong `settings.gradle.kts` hay build graph cua repo chinh.
  - `Pufferfish` goc la mot nested git repo; khi dua vao repo nay can add theo file content, khong add kieu embedded submodule.
- Van giu quy tac local:
  - khong push `.tmp-universe-classes/` vi >100MB
  - khong build local tren Termux neu khong duoc yeu cau ro rang
