# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this
repository.

**Keep this file in sync with the code.** Whenever you change something this file describes —
package names, architecture, conventions, SDK levels, dependencies, auth flow, etc. — update the
corresponding section here in the same change. If a statement here becomes false, fix it.

## Build Commands

```bash
./gradlew assembleDebug       # Debug build
./gradlew assembleRelease     # Release build (minified, obfuscated, shrunk)
./gradlew test                # Run unit tests
```

Unit tests live under `app/src/test/` (currently only `UtilTest`).

## Project Overview

Ferrio is an Android calendar app for unusual holidays. Package:
`eu.andret.kalendarzswiatnietypowych`. This package can never be changed because it acts like the
app's identifier.

- **Java 11** with core library desugaring, **minSdk 23**, **targetSdk 37**, **compileSdk 37**.
- Gradle Kotlin DSL with version catalog (`gradle/libs.versions.toml`).
- No Kotlin code — pure Java.

## Architecture

**Data flow:** Activity/Fragment → ViewModel (LiveData) → AppRepository → Room DB + ApiClient

- **FerrioApplication** holds singleton `AppRepository` and `ApiClient` — no DI framework.
- **BaseActivity** provides shared `HolidayViewModel` and theme/preferences access.
- **AuthenticatedFragment** / **AuthenticatedDialogFragment** — base classes for screens requiring
  Firebase Auth. Expose `fetchAuthenticated(FetchFunction, onSuccess, onError)` and
  `submitAuthenticated(SubmitFunction, onSuccess, onError)` helpers. Both functional interfaces
  receive `(token, CancellableRequest)`; call sites forward the `CancellableRequest` to
  `ApiClient` so that `onDestroyView` triggers real OkHttp `Call.cancel()` on in-flight requests.
- **HolidayRemoteMediator** syncs API → Room on refresh; posts `LoadState` (LOADING/SUCCESS/ERROR).
- **ApiClient** — thin wrapper over OkHttp (no Retrofit). All methods are synchronous and throw
  `ApiException` on failure. Accepts an optional `CancellableRequest` for cancellation. Gson with
  `LOWER_CASE_WITH_UNDERSCORES` field naming.
- **API base URL:** `https://api.ferrio.app/v3`

## Key Patterns

- **Day adapters:** `BaseDayAdapter<VH>` → `DayAdapterDetailed`, `DayAdapterCompact`,
  `DayAdapterSimple` — three calendar grid display modes selectable in settings.
- **Suggestion adapters:** unified `SuggestionAdapter<T extends HolidaySuggestion>` handles both
  fixed and floating holiday suggestions.
- **ListFragment** with `newInstance(reportType, holidayType)` — used by the reports flow.
- **TabbedListActivity** with `createIntent(context, reportType)` — hosts the reports flow.
  The suggestion flow still uses the older `SuggestionActivity` +
  `FixedSuggestionFragment`/`FloatingSuggestionFragment`; migration is not complete.
- All adapters use `ListAdapter` with `DiffUtil.ItemCallback`; no View Binding — uses
  `findViewById`.
- **MainActivity month pager:** `viewPager2.setOffscreenPageLimit(11)` is **required** and must
  stay at `11`. There are exactly 12 `MonthFragment`s and inflating one on the UI thread when the
  user jumps to a non-adjacent month is visibly laggy, so all 12 must be kept resident. Do not
  lower this value, do not replace it with `OFFSCREEN_PAGE_LIMIT_DEFAULT`, and if `getItemCount()`
  ever changes from 12, update the limit to `count - 1` in the same change. This rule applies
  only to the month pager — `DayActivity`'s 367-page day pager must keep the default.

## Code Conventions

- **Line endings:** CRLF (`\r\n`) for every file you write or edit. The repo is on Windows and
  mixing LF into existing CRLF files causes noisy diffs and git `LF will be replaced by CRLF`
  warnings. If a tool or template emits LF, fix it before saving.
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
