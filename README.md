### TODO

## THEME

MAYBE DIFFERENT COLORS? DO LAST 

## LOGIN SCREEN

## REGISTER SCREEN

NOTHING MUCH FOR NOW

## DASHBOARD SCREEN

VIEW DETAILS SCREEN IS DONE, MAYBE CHANGE (Been changed a bit added a join challenge button which copuld use an email api to send an automatic message)

## ERROR HANDLING

✅ IMPLEMENTED MORE USER FRIENDLY MESSAGES




## Highest-impact checklist (do these first)
State handling polish

Add UiState (Loading/Success/Empty/Error) for dashboard + detail + create flows.

Layering cleanup

Move auth/register calls out of screens into repository/use-case and ViewModel.

Background/offline

Add WorkManager job for periodic challenge sync/retry queue.

Room robustness

Replace clear-and-reload with merge/upsert strategy.

Turn on schema export + add migration strategy notes.

Documentation

Rewrite README into rubric evidence document + GenAI transparency section.

Security hygiene

Move Supabase URL/key into secure config mechanism (at minimum build config/env handling for submission)


