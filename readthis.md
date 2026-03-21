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
- `combat.alternative-hit-registration`
- `combat.imitateSwordBlocking`

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
