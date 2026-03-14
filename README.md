# Mobile — Sports Challenge App

An Android application for GAA clubs and teams to post and discover open match challenges. Built with Kotlin and Jetpack Compose, targeting Android API 34+.

---

## Features

- Register a team with name and location
- Log in and browse upcoming challenges from other teams
- Post your own open challenge with skill level and date
- View challenge details and send a join request (triggers an email to the challenge creator)
- Challenges are cached locally and stay available offline

---

## Architecture

The app follows the recommended Android layered architecture:

```
UI Layer        → Compose screens + ViewModels (StateFlow)
Data Layer      → Repository interfaces + Room (local) + Supabase (remote)
Domain Layer    → Pure Kotlin models (no Android dependencies)
```

**Key components:**
- `ui/screens/` — Compose screens (Login, Register, Dashboard, CreateChallenge, ChallengeDetail)
- `presentation/` — ViewModels and sealed UI state classes
- `navigation/` — `AppNavGraph`, `Routes`
- `data/local/` — Room entity, DAO, database, type converters
- `data/repository/` — `ChallengeRepository` interface + `DefaultChallengeRepository`
- `domain/challenges/Challenge.kt` — domain model
- `SupabaseClient.kt` — all remote operations (auth, CRUD, email)
- `MetricsService.kt` — lightweight event tracking to Supabase `events` table
- `worker/ChallengeSyncWorker.kt` — background sync via WorkManager
- `MobileApplication.kt` — app entry point, schedules WorkManager

---

## Screens & Navigation

| Screen | Route |
|--------|-------|
| Login | `login` |
| Register | `register` |
| Dashboard | `dashboard` |
| Create Challenge | `create_challenge` |
| Challenge Detail | `challenge_detail/{challengeId}` |

- `challengeId` is passed as a `NavType.StringType` argument to the detail screen
- Back stack is cleared on login/register so the user cannot navigate back to the auth screens
- On app launch, an active Supabase session skips straight to Dashboard

---

## Local Persistence — Room

- **Entity:** `ChallengeEntity` — maps to the `challenges` table
- **DAO:** `ChallengeDao` — all queries return `Flow` for automatic UI updates
- **Type converter:** `ChallengeTypeConverters` — converts `kotlinx.datetime.LocalDate` ↔ `String`
- **Database:** `AppDatabase` — singleton, database file `mobile4.db`
- `refreshChallenges()` uses `database.withTransaction {}` — clears and re-inserts atomically, so a failed sync never leaves an empty database
- Data survives app restarts; the cached list is shown immediately on next launch while a fresh remote fetch runs in the background

---

## Cloud Database — Supabase

**Tables:**
| Table | Purpose |
|-------|---------|
| `users` | Team profiles (id, email, team_name, location) |
| `challenges` | Open match challenges |
| `challenge_join_requests` | Join requests (challenge_id, requester_email, target_email, status) |
| `events` | Metrics/analytics events |

**Auth:** Supabase Auth with email/password (`signUpWith`, `signInWith`, `signOut`, `currentSessionOrNull`)

**Email notifications:** When a join request is submitted, an HTTP POST is made to the EmailJS API notifying the challenge creator by email.

---

## Background Sync — WorkManager

**Worker:** `ChallengeSyncWorker`
- Extends `CoroutineWorker` — runs entirely off the main thread
- Calls `DefaultChallengeRepository.refreshChallenges()` to fetch from Supabase and update Room
- Returns `Result.retry()` on failure — WorkManager handles backoff automatically

**Scheduling:** `MobileApplication.onCreate()`
- `PeriodicWorkRequest` with a 15-minute interval (WorkManager minimum)
- Constrained to `NetworkType.CONNECTED` — only runs when online
- `ExistingPeriodicWorkPolicy.KEEP` — reopening the app does not enqueue duplicates
- First execution is after the initial 15-minute interval (login triggers an immediate manual refresh)

---

## Concurrency

- All ViewModel operations use `viewModelScope.launch` — cancelled automatically when the ViewModel is cleared
- Network calls run under `withContext(Dispatchers.IO)`
- Room DAO functions are `suspend` — Room dispatches them off the main thread internally
- `runCatching` used for structured error handling in ViewModels

---

## Logging

Filter by tag in Logcat:

| Tag | What it logs |
|-----|-------------|
| `MobileApplication` | WorkManager enqueued on startup |
| `DefaultChallengeRepository` | Number of challenges fetched from Supabase |
| `ChallengeSyncWorker` | Sync start, success, retry on failure |
| `ChallengesViewModel` | Errors in refresh, create, join |
| `AuthViewModel` | Errors in login, register, logout |
| `SupabaseClient` | Email send attempts and failures |

---

## Environment Variables

Set in `local.properties` at the project root (not committed to git):

```
MOBILE_APP_SUPABASE_URL=...
MOBILE_APP_SUPABASE_KEY=...
EMAILJS_PUBLIC_KEY=...
```

Accessed via `BuildConfig` in code.

---

## Setup

1. Clone the repo
2. Add the required keys to `local.properties`
3. Open in Android Studio and sync Gradle
4. Run on an emulator or device with API 34+
