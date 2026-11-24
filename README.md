# Luno Fucked
**Fabric 1.21.1 | Author: FuckOffAds**

Cracked-account auto-griefer loop with a managed name pool.

---

## What it does

1. Press **G** on the title screen → bot starts
2. Picks the next name from the pool, creates a cracked session
3. Connects to `insanesmp.net` (configurable)
4. Sends `/reg <password> <password>` → waits 4s → `/redeem <code>` → `/tpa <target>`
5. Waits until it's within **3–4 blocks** of the target player
6. **Drops every item** in inventory
7. Disconnects → waits 2s → repeats with the next name in pool
8. If kicked mid-session → auto-reconnects in 3s

---

## Keys

| Key | Action |
|-----|--------|
| **G** | Start / Stop bot loop |
| **M** | Open Name Pool & Config menu |

---

## Config menu (M key)

- **Add names** — type in the box, click Add
- **Remove names** — right-click any name in the list
- **TPA Target** — player to teleport to and drop on
- **Register Password** — password used in `/reg`
- **Redeem Code** — code used in `/redeem`

Config saved to `.minecraft/config/lunofucked.json`

---

## Name Pool behaviour

Bot cycles through names in order. When all names are used once, it loops back and appends incrementing numbers so every account name stays unique. Example:

```
SparkyNox09 → YTLEgend69 → LunoLoL682 → SparkyNox109 → YTLEgend169 → …
```

---

## Build

```bash
./gradlew build
# output: build/libs/LunoFucked-1.0.0.jar
```

Or push to GitHub — the Actions workflow builds and uploads the JAR automatically.

---

## Status bar

Visible at the bottom of the title screen:

- **Idle** — `[LunoFucked] Idle — Press G to start | M for menu`
- **Running** — `[LunoFucked] Running | User: SparkyNox09 | State: TRACKING`
