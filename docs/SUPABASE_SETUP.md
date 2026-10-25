# Supabase sync setup

The app remains fully usable offline. Cloud sync must use a dedicated HabitTracker project; do not apply the schema to the existing `foodme` project.

## Prepared

- `supabase/schema.sql` defines habits, budgets, transactions and currency preferences.
- Row Level Security limits every record to the authenticated user.
- Mobile clients must use only a Supabase publishable key. Never bundle a secret or service-role key.
- Local records already use stable UUIDs, so they can be upserted safely after login.

## Activation steps

1. Create a dedicated Supabase project in the organization and region selected by the owner.
2. Apply `supabase/schema.sql` as a migration.
3. Enable email or Apple authentication.
4. Put the project URL and publishable key in local build configuration, excluded from Git.
5. Add the Supabase Kotlin client and implement pull-then-push synchronization with `updated_at` conflict resolution.
6. Sync on login, app start and after every local write; keep an offline retry queue.
7. Run Supabase security and performance advisors before release.

## Bybit exchange rates

The finance UI always shows the actual coefficients used for conversion, for example `1 USD = 1 610 ARS`, together with the source label. Bybit's public V5 ticker API covers spot and derivative symbols, not fiat ARS/RUB pairs. Bybit's official P2P Open API can expose ad data, but it requires a verified P2P advertiser account and an authenticated API key. Automatic Bybit P2P refresh therefore needs advertiser credentials stored only in a Supabase Edge Function; such credentials must never be embedded in the Android or iOS app. Until those credentials are supplied, the app keeps an editable local rate and clearly labels it as manual.

Project creation is intentionally not automated until the owner selects the Supabase organization and accepts any quoted cost.
