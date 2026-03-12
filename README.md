### TODO

## THEME

MAYBE DIFFERENT COLORS? DO LAST 

## Highest-impact checklist (do these first)
State handling polish

Add UiState (Loading/Success/Empty/Error) for dashboard + detail + create flows.

Layering cleanup

Move auth/register calls out of screens into repository/use-case and ViewModel.

Background/offline

Room robustness

Replace clear-and-reload with merge/upsert strategy.

Turn on schema export + add migration strategy notes.

Documentation

Rewrite README into rubric evidence document + GenAI transparency section.

Security hygiene

## Background Sync (WorkManager)

Challenges are kept up to date in the background using `WorkManager`.

The Background Sync is ran every 15 minutes after app startup as refreshChallenges() is already ran on login.

**Worker:** `ChallengeSyncWorker` (`worker/ChallengeSyncWorker.kt`)
- Extends `CoroutineWorker` so the sync runs in a coroutine, off the main thread.
- Calls `DefaultChallengeRepository.refreshChallenges()` which fetches from Supabase and updates the local Room database in a single transaction.
- Returns `Result.retry()` on failure — WorkManager will back off and retry automatically.

**Scheduling:** `MobileApplication.onCreate()`
- Registers a `PeriodicWorkRequest` with a 15-minute interval (the WorkManager minimum).
- Constrained to only run when the device has a network connection (`NetworkType.CONNECTED`).
- Uses `ExistingPeriodicWorkPolicy.KEEP` so reopening the app does not queue duplicate workers.
- Enqueued once on app start (before any Activity is created); first execution occurs after the 15-minute interval.


## Environment Variables

Supabase URL and Key are now set in the `local.properties` file located at the project root.
They are called `SUPABASE_URL` and `SUPABASE_KEY`.
They are accessed via BuildConfig.




