# GAA Challenge App – Presentation Notes (Harry's Contributions)

## My Contributions Overview

| Area | What I Built |
|---|---|
| **MobileApplication.kt** | App entry point – schedules background sync on startup |
| **WorkManager** | Periodic background sync of challenges every 15 min |
| **Supabase** | Remote database + auth client (signup, login, CRUD) |
| **Local Room DB** | Offline-first local database with reactive queries |
| **Data Layer / Data Flow** | Repository pattern connecting UI ↔ Room ↔ Supabase |
| **COTC Integration** | EmailJS integration to notify teams of join requests |
| **Report** | MetricsService – event tracking to Supabase `events` table |

---

## 1. MobileApplication.kt

**File:** `app/src/main/java/com/example/mobile/MobileApplication.kt`

**What it is:** Every Android app can have a custom `Application` class that runs before any screen appears. This is the best place to set up global services.

**What it does:**
- Extends Android's `Application` class
- Overrides `onCreate()` which fires once when the app process starts
- Schedules the WorkManager background sync job
- Referenced in `AndroidManifest.xml` as `android:name="com.example.mobile.MobileApplication"` so Android knows to use it

```kotlin
class MobileApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            ChallengeSyncWorker.WORK_NAME,         // unique name = "challenge_sync"
            ExistingPeriodicWorkPolicy.KEEP,        // don't restart if already queued
            PeriodicWorkRequestBuilder<ChallengeSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
                .build()
        )
    }
}
```

**Likely questions:**
- *Why not start WorkManager in MainActivity?* — `Application.onCreate()` runs once per process lifetime. `MainActivity` can be recreated many times (rotation, navigation). Starting here prevents duplicate scheduling.
- *What does `KEEP` mean?* — If the sync job is already scheduled (e.g. app was backgrounded and reopened), `KEEP` leaves the existing job alone instead of resetting its timer.

---

## 2. WorkManager

**File:** `app/src/main/java/com/example/mobile/worker/ChallengeSyncWorker.kt`

**What it is:** WorkManager is Android's recommended API for deferrable background work that must be guaranteed to run even if the app is closed or the device restarts.

**What it does:**
- `ChallengeSyncWorker` extends `CoroutineWorker` (coroutine-friendly background worker)
- Runs every **15 minutes** (the minimum interval WorkManager allows)
- Only runs when the device has a **network connection**
- Calls `DefaultChallengeRepository.refreshChallenges()` to pull fresh data from Supabase and update Room
- Returns `Result.success()` on success, `Result.retry()` on failure (WorkManager handles automatic backoff)

**Flow:**
```
App starts
  └─> MobileApplication.onCreate()
      └─> WorkManager schedules ChallengeSyncWorker (every 15 min, needs network)
          └─> ChallengeSyncWorker.doWork()
              └─> DefaultChallengeRepository.refreshChallenges()
                  ├─> Fetch challenges from Supabase
                  └─> Clear + re-insert into Room DB (atomic transaction)
```

**Likely questions:**
- *Why WorkManager and not a simple timer?* — Android kills background processes aggressively to save battery. WorkManager survives process death, device restarts, and Doze mode. A timer would not.
- *What happens if sync fails?* — `Result.retry()` is returned and WorkManager automatically retries with exponential backoff.
- *What does the network constraint do?* — The job is queued but won't execute until Wi-Fi or mobile data is available. This prevents failed requests from burning battery.
- *What is `enqueueUniquePeriodicWork`?* — Ensures only one instance of `"challenge_sync"` exists in the WorkManager queue at any time, preventing duplicates.

---

## 3. Supabase Integration

**File:** `app/src/main/java/com/example/mobile/SupabaseClient.kt`

**What it is:** Supabase is an open-source Firebase alternative providing a PostgreSQL database, authentication, and REST API. The Kotlin SDK wraps these into type-safe function calls.

**Modules used:**
- `auth-kt` — Email/password authentication
- `postgrest-kt` — SQL REST API for reading/writing database tables

**Database tables:**

| Table | Purpose |
|---|---|
| `users` | Team profiles (id, email, team_name, location) |
| `challenges` | Open challenges posted by teams |
| `challenge_join_requests` | Requests from teams wanting to join a challenge |
| `events` | Analytics events (from MetricsService) |

**Key functions:**

| Function | What it does |
|---|---|
| `signUpWithEmail()` | Registers a new user with Supabase Auth |
| `signInWithEmail()` | Logs in, sets an active session |
| `signOut()` | Clears the session |
| `hasActiveSession()` | Returns true if user is logged in |
| `registerTeam()` | Signs up + inserts a row in the `users` table |
| `getCurrentUserProfile()` | Fetches team_name and location for the logged-in user |
| `fetchChallenges()` | Returns all rows from the `challenges` table |
| `insertChallenge()` | Creates a new challenge row |
| `requestJoinChallenge()` | Validates, inserts a join request, sends email notification |
| `sendJoinRequestEmail()` | HTTP POST to EmailJS API |

**Security features built in:**
- Prevents users from joining their own challenge (email check)
- Prevents duplicate join requests (queries `challenge_join_requests` first)
- API keys are kept out of source code — injected via `BuildConfig` from `local.properties`

**Likely questions:**
- *How are API keys kept secure?* — Stored in `local.properties` (gitignored), injected into `BuildConfig` at build time via `build.gradle.kts`. Never hardcoded.
- *What is Postgrest?* — A REST API automatically generated from your PostgreSQL schema. Instead of writing SQL queries, you call Kotlin functions like `.from("challenges").select()`.
- *What happens if a network call fails?* — Exceptions propagate up to the repository, which catches them and maps them to error UI states shown to the user.

---

## 4. Local Room DB (Offline-First)

**Files:** `data/local/` folder

**What it is:** Room is Android's SQLite ORM (Object-Relational Mapper). It lets you define a local database using Kotlin annotations. The app caches Supabase data locally so it works offline.

### Components:

**AppDatabase.kt** — The database class
```kotlin
@Database(entities = [ChallengeEntity::class], version = 1)
@TypeConverters(ChallengeTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun challengeDao(): ChallengeDao
}
```
- Singleton — only one instance ever exists (thread-safe with `synchronized`)
- Declares which entities (tables) exist and which DAOs provide access

**ChallengeEntity.kt** — The database table row
```kotlin
@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey val id: String,
    val teamName: String,
    val skillLevel: String,
    val location: String,
    val date: LocalDate,           // stored as String via TypeConverter
    val createdByEmail: String?
)
```
- Maps directly to a `challenges` table in SQLite
- Includes mapper functions: `toDomain()` and `toEntity()` to convert between DB and domain models

**ChallengeDao.kt** — Data Access Object (the query layer)
```kotlin
@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenges WHERE date >= :today ORDER BY date ASC")
    fun observeActiveChallenges(today: LocalDate): Flow<List<ChallengeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(challenges: List<ChallengeEntity>)

    @Query("DELETE FROM challenges")
    suspend fun clearAll()
}
```
- All queries return `Flow<>` — the UI automatically updates whenever data changes
- `REPLACE` conflict strategy = insert new or update existing (upsert behaviour)

**ChallengeTypeConverters.kt** — Handles types Room can't store natively
- Converts `LocalDate` ↔ `String` because SQLite has no date type

**Likely questions:**
- *Why store data locally at all?* — Offline-first design. If there's no network, the user still sees cached challenges. The app doesn't break without internet.
- *What is a Flow?* — A Kotlin coroutine stream. When the database changes, the Flow emits a new list automatically. The ViewModel observes this and the UI recomposes — no manual refresh needed.
- *What does `OnConflictStrategy.REPLACE` mean?* — If a row with the same primary key already exists, replace it. This makes `upsertAll` work for both new and updated challenges.
- *What is a TypeConverter?* — Room only stores basic types (Int, String, etc.). TypeConverters teach Room how to convert complex types to/from those basic types.

---

## 5. Data Layer / Data Flow Architecture

**Files:** `data/repository/` folder

**Pattern used: Repository Pattern + MVVM**

The app is split into layers so each layer only knows about the layer below it:

```
UI (Composable Screens)
    ↕ collects StateFlow
ViewModel (AuthViewModel, ChallengesViewModel)
    ↕ calls repository functions
Repository (DefaultChallengeRepository, DefaultAuthRepository)
    ↕ reads/writes            ↕ fetches/posts
Local Room DB             Supabase Remote API
```

### Repository Interfaces

Interfaces define the contract. ViewModels only know about the interface, not the implementation — this makes testing and swapping implementations easy.

```kotlin
interface ChallengeRepository {
    fun observeActiveChallenges(): Flow<List<Challenge>>
    fun observeChallenge(challengeId: String): Flow<Challenge?>
    suspend fun refreshChallenges()
    suspend fun requestJoinChallenge(challenge: Challenge)
    suspend fun createChallenge(skillLevel: String, date: LocalDate)
}
```

### DefaultChallengeRepository — The Core

Key design decision — **Room is the single source of truth:**

```
refreshChallenges():
  1. Fetch challenges list from Supabase
  2. Open a Room database transaction
  3. clearAll() — delete existing rows
  4. upsertAll() — insert fresh data
  5. Transaction commits atomically
     → if step 1 fails, steps 3-4 never run (DB stays intact)
     → UI Flow emits new list automatically
```

```
observeActiveChallenges():
  → Reads directly from Room (not Supabase)
  → Returns only challenges with date >= today
  → Returns a Flow that auto-updates when Room changes
```

```
createChallenge():
  1. Get current user profile (team_name, location) from Supabase
  2. Generate UUID for the new challenge
  3. Insert into Supabase (source of truth)
  4. Insert into Room (local cache)
  5. Flow emits — UI shows new challenge instantly
```

### ViewModels

**AuthViewModel** — handles login, register, logout
- Uses `StateFlow<AuthUiState>` to expose state to the UI
- Maps errors to human-readable messages
- Resets state when navigating (prevents stale error messages)

**ChallengesViewModel** — handles dashboard, create, join, detail
- Combines three flows (challenges list + loading flag + error) into one `DashboardUiState`
- Uses `stateIn(WhileSubscribed(5000))` — stops collecting 5 seconds after last subscriber (saves resources)

**UI States used:**

| State | Values |
|---|---|
| `AuthUiState` | `Idle`, `Loading`, `Success`, `Error(message)` |
| `DashboardUiState` | `Loading`, `Success(challenges)`, `Empty`, `Error(message)` |
| `CreateUiState` | `Idle`, `Loading`, `Success`, `Error(message)` |
| `JoinChallengeUiState` | `Idle`, `Loading`, `Success(message)`, `Error(message)` |

**Likely questions:**
- *Why use a Repository pattern?* — Decouples the ViewModel from knowing whether data comes from Room or Supabase. The ViewModel just calls `repository.observeActiveChallenges()` — it doesn't care where data comes from.
- *Why is Room the single source of truth and not Supabase?* — Network calls are slow and can fail. By reading from Room and only writing to Supabase (then syncing back), the UI is always fast and responsive.
- *What is a StateFlow?* — A hot flow that holds the latest value. When the UI subscribes, it immediately gets the current state. Safe for Jetpack Compose to collect.
- *What does `withTransaction` do?* — Wraps multiple Room operations into an atomic unit. Either all operations succeed, or none do. Prevents a partial update where the DB is empty after a failed sync.

---

## 6. COTC Integration (EmailJS)

**Located in:** `SupabaseClient.kt` — `requestJoinChallenge()` and `sendJoinRequestEmail()`

**What COTC integration means here:** When a team submits a join request, the challenge creator receives an email notification. This bridges the mobile app to external communication (email), which is the integration component.

**How it works:**

```
User taps "Request to Join"
  └─> requestJoinChallenge(challenge)
      ├─> Validate: user is not challenge owner
      ├─> Check: no existing join request exists
      ├─> Insert row into challenge_join_requests table in Supabase
      └─> sendJoinRequestEmail()
          └─> HTTP POST to EmailJS API
              with: requester email, challenge details, team name
```

**EmailJS integration:**
- Uses `ktor` HTTP client to POST to `https://api.emailjs.com/api/v1.0/email/send`
- Sends: service ID, template ID, requester email, challenge info
- API key injected from `BuildConfig.EMAILJS_PUBLIC_KEY`
- **Email failure is non-fatal** — if the email fails, the join request is still saved in Supabase. The app logs the error but does not show it to the user (metrics only).

**Likely questions:**
- *Why EmailJS instead of Supabase Edge Functions?* — EmailJS is a simpler third-party service that sends emails via HTTP POST without needing a server. Good for prototyping and direct mobile integration.
- *Why is email failure non-fatal?* — The join request data is saved in Supabase regardless. The email is a notification convenience, not a core requirement. Failing it shouldn't block the user's action.
- *How is the join request duplicate check done?* — Before inserting, the app queries `challenge_join_requests` for a row matching both `challenger_email` and `challenge_id`. If found, it throws an error without inserting.

---

## 7. Report (MetricsService)

**File:** `app/src/main/java/com/example/mobile/MetricsService.kt`

**What it is:** A lightweight analytics/reporting service that tracks user actions by inserting event records into Supabase.

```kotlin
object MetricsService {
    suspend fun track(eventName: String) {
        try {
            SupabaseClient.postgrest["events"].insert(
                mapOf("event_name" to eventName)
            )
        } catch (_: Exception) {
            // Silently ignored — metrics must never disrupt the user
        }
    }
}
```

**Events tracked:**

| Event name | Where fired | Why |
|---|---|---|
| `"login_viewed"` | LoginScreen | Track how often login screen is shown |
| `"dashboard_viewed"` | DashboardScreen | Track active users |
| `"create_challenge_tapped"` | DashboardScreen FAB | Track feature engagement |
| `"club_joined"` | AppNavGraph after register | Track successful registrations |

**Design decisions:**
- `object` — Kotlin singleton, no instantiation needed
- All errors silently swallowed — metrics must never crash or block the app
- `suspend fun` — runs in a coroutine, does not block the main thread
- Stores to Supabase `events` table — can be queried for reports/dashboards

**Likely questions:**
- *Why is it a Kotlin `object`?* — Singleton pattern. One instance shared across the whole app. No need to pass it around or inject it.
- *Why catch and ignore all exceptions?* — Metrics are secondary to app function. A failed analytics call should never stop the user from using the app or cause a crash.
- *How would you build a report from this?* — Query the `events` table in Supabase by `event_name` and group/count. Could be visualised in Supabase Studio or exported.

---

## Complete Data Flow Diagram

```
User Action (UI)
    │
    ▼
Composable Screen (LoginScreen, DashboardScreen, etc.)
    │  collects StateFlow / calls ViewModel function
    ▼
ViewModel (AuthViewModel / ChallengesViewModel)
    │  calls repository
    ▼
Repository Interface (ChallengeRepository / AuthRepository)
    │
    ├──────────────────────────────────┐
    ▼                                  ▼
Local Room DB (ChallengeDao)     Supabase (SupabaseClient)
    │                                  │
    │  Flow<List<ChallengeEntity>>     │  List<Challenge> (on sync)
    │                                  │
    └──────────► Repository ◄──────────┘
                     │
                     │  Flow<List<Challenge>> (domain model)
                     ▼
               ViewModel emits StateFlow
                     │
                     ▼
               UI recomposes automatically


Background Sync (WorkManager - every 15 min):
    ChallengeSyncWorker
        └─> repository.refreshChallenges()
            ├─> Supabase.fetchChallenges()
            └─> Room.clearAll() + Room.upsertAll()
                    └─> Flow emits → UI updates automatically
```

---

## Key Technologies Summary

| Technology | Why used |
|---|---|
| **Kotlin Coroutines** | Async/non-blocking code without callbacks |
| **Flow** | Reactive data streams — UI auto-updates when data changes |
| **Room** | Local SQLite database with type-safe Kotlin API |
| **Supabase** | Hosted PostgreSQL + Auth + REST API |
| **WorkManager** | Guaranteed background work, survives app closure & reboots |
| **Jetpack Compose** | Declarative UI framework for Android |
| **MVVM Pattern** | Separates UI logic (ViewModel) from business logic (Repository) |
| **Repository Pattern** | Abstracts data source from UI — Room or Supabase, same interface |
| **EmailJS** | HTTP-based email sending without a custom backend server |
