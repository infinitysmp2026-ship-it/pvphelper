# PvP Helper (Fabric, client-side, Minecraft 1.21.11)

One JAR, three independent helpers, one central left-click dispatcher.

Build (Java 21 required):

    ./gradlew build        # Windows: gradlew.bat build

Output: build/libs/pvphelper-1.0.0.jar  (do NOT use the -sources jar)

Toolchain (from the official FabricMC/fabric-example-mod branch 1.21.11):
Minecraft 1.21.11, Fabric Loader 0.19.5, Fabric API 0.141.6+1.21.11, Loom 1.18-SNAPSHOT, Mojang mappings, Gradle 9.7.1.

Config: <minecraft>/config/pvphelper.json. Keybinds: Options > Controls > Key Binds (category "Miscellaneous").
Default key for the settings screen: Right Shift.

NOTE: Servers / anti-cheats may block or punish assisted interactions. Client-side only; no packets are forged.
