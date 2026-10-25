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

The finance UI always shows the coefficients and direction used for conversion. ARS uses the Bybit P2P USDT sale price (`bid`) exposed by the public CriptoYa aggregator. RUB uses the public SAIKYO USDT purchase price. Neither read-only endpoint requires a key. The last successful values remain cached locally when the network is unavailable, and the user can switch to manual RUB and ARS rates at any time.

Project creation is intentionally not automated until the owner selects the Supabase organization and accepts any quoted cost.
