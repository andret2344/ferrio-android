# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this
repository.

## Build Commands

```bash
./gradlew assembleDebug       # Debug build
./gradlew assembleRelease     # Release build (minified, obfuscated, shrunk)
```

No tests exist yet.

## Project Overview

Ferrio is an Android calendar app for unusual holidays. Package:
`eu.andret.kalendarzswiatnietypowych`. This package can never be changed because it acts like the
app's identifier.

- **Java 11** with core library desugaring, **minSdk 29**, **targetSdk 36**.
- Gradle Kotlin DSL with version catalog (`gradle/libs.versions.toml`).
- No Kotlin code — pure Java.

## Architecture

**Data flow:** Activity/Fragment → ViewModel (LiveData) → AppRepository → Room DB + ApiClient

- **FerrioApplication** holds singleton `AppRepository` and `ApiClient` — no DI framework.
- **BaseActivity** provides shared `HolidayViewModel` and theme/preferences access.
- **AuthenticatedFragment** / **AuthenticatedDialogFragment** — base classes for screens requiring
  Firebase Auth; handle async API calls via `CompletableFuture` with lifecycle-aware cancellation.
- **HolidayRemoteMediator** syncs API → Room on refresh; posts `LoadState` (LOADING/SUCCESS/ERROR).
- **ApiClient** — custom `HttpsURLConnection` client (no Retrofit); Gson with
  `LOWER_CASE_WITH_UNDERSCORES` field naming.
- **API base URL:** `https://api.ferrio.app/v3`

## Key Patterns

- **Day adapters:** `BaseDayAdapter<VH>` → `DayAdapterDetailed`, `DayAdapterCompact`,
  `DayAdapterSimple` — three calendar grid display modes selectable in settings.
- **Suggestion adapters:** unified `SuggestionAdapter<T extends HolidaySuggestion>` handles both
  fixed and floating holiday suggestions.
- **ListFragment** with `newInstance(reportType, holidayType)` — replaces separate suggestion/report
  fragments.
- **TabbedListActivity** with `createIntent(context, reportType)` — replaces separate
  suggestion/report activities.
- All adapters use `ListAdapter` with `DiffUtil.ItemCallback`; no View Binding — uses
  `findViewById`.

## Code Conventions

- **Tabs** for indentation (not spaces).
- Aggressive use of `final` on all parameters, local variables, and fields.
- `@NonNull` / `@Nullable` annotations throughout.
- Layout XML IDs: `activity_*`, `fragment_*`, `adapter_*` prefixes.
- String resource keys: `snake_case` (e.g., `settings_key_theme_colorized`).
- **Date/time:** Always use `java.time` (`LocalDate`, `LocalTime`, `LocalDateTime`, `ZonedDateTime`)
  — never `java.util.Calendar` or `java.util.Date`. The `java.time` classes are thread-safe and
  available via core library desugaring.
- Locale: `Locale.ROOT` for machine-readable formatting, `Locale.getDefault()` for UI strings.
- Languages: English (default) + Polish (`values-pl/`).
- **Logcat tags:** Must start with `Ferrio-` prefix (e.g., `Ferrio-Widget`, `Ferrio-Api`) for easier
  filtering.

## Database

Room v2.8.4 with single entity `Holiday` (indexed on month+day). Destructive migration fallback for
versions 1–3, currently at version 4.

## Authentication

Firebase Auth (Google sign-in + anonymous). Login flow: `LoginActivity` → `AuthViewModel` →
`AuthRepository`. Token retrieved via `AuthHelper.getFirebaseToken()` (must be called off main
thread).
