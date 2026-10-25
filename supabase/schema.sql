-- Run this schema only in the dedicated HabitTracker Supabase project.
-- The existing foodme project must not be used for this app.

create extension if not exists pgcrypto;

create or replace function public.set_updated_at()
returns trigger
language plpgsql
security invoker
set search_path = ''
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

create table if not exists public.habits (
  id uuid primary key,
  user_id uuid not null references auth.users(id) on delete cascade,
  title text not null check (char_length(title) between 1 and 120),
  icon_key text not null,
  color_index smallint not null check (color_index between 0 and 20),
  created_on date not null,
  scheduled_weekdays smallint[] not null check (scheduled_weekdays <@ array[1,2,3,4,5,6,7]::smallint[]),
  completed_days date[] not null default '{}',
  updated_at timestamptz not null default now()
);

create table if not exists public.finance_profiles (
  user_id uuid primary key references auth.users(id) on delete cascade,
  display_currency text not null default 'USD' check (display_currency in ('RUB', 'USD', 'ARS')),
  rub_per_usd numeric check (rub_per_usd > 0),
  ars_per_usd numeric check (ars_per_usd > 0),
  updated_at timestamptz not null default now()
);

create table if not exists public.budgets (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  currency text not null check (currency in ('RUB', 'USD', 'ARS')),
  amount_minor bigint not null check (amount_minor >= 0),
  updated_at timestamptz not null default now(),
  unique (user_id)
);

create table if not exists public.finance_categories (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  name text not null check (char_length(name) between 1 and 80),
  color_index smallint not null default 0 check (color_index between 0 and 7),
  updated_at timestamptz not null default now(),
  unique (user_id, name)
);

create table if not exists public.finance_transactions (
  id uuid primary key,
  user_id uuid not null references auth.users(id) on delete cascade,
  title text not null check (char_length(title) between 1 and 160),
  amount_minor bigint not null check (amount_minor > 0),
  currency text not null check (currency in ('RUB', 'USD', 'ARS')),
  transaction_type text not null check (transaction_type in ('EXPENSE', 'INCOME', 'OWED_TO_ME', 'I_OWE')),
  category text not null default '',
  transaction_on date not null,
  updated_at timestamptz not null default now()
);

drop trigger if exists habits_set_updated_at on public.habits;
create trigger habits_set_updated_at before update on public.habits for each row execute function public.set_updated_at();
drop trigger if exists finance_profiles_set_updated_at on public.finance_profiles;
create trigger finance_profiles_set_updated_at before update on public.finance_profiles for each row execute function public.set_updated_at();
drop trigger if exists budgets_set_updated_at on public.budgets;
create trigger budgets_set_updated_at before update on public.budgets for each row execute function public.set_updated_at();
drop trigger if exists finance_transactions_set_updated_at on public.finance_transactions;
create trigger finance_transactions_set_updated_at before update on public.finance_transactions for each row execute function public.set_updated_at();
drop trigger if exists finance_categories_set_updated_at on public.finance_categories;
create trigger finance_categories_set_updated_at before update on public.finance_categories for each row execute function public.set_updated_at();

alter table public.habits enable row level security;
alter table public.finance_profiles enable row level security;
alter table public.budgets enable row level security;
alter table public.finance_transactions enable row level security;
alter table public.finance_categories enable row level security;

revoke all on public.habits, public.finance_profiles, public.budgets, public.finance_transactions, public.finance_categories from anon;
grant select, insert, update, delete on public.habits, public.finance_profiles, public.budgets, public.finance_transactions, public.finance_categories to authenticated;

drop policy if exists "users_manage_own_habits" on public.habits;
create policy "users_manage_own_habits" on public.habits for all to authenticated
using ((select auth.uid()) = user_id) with check ((select auth.uid()) = user_id);
drop policy if exists "users_manage_own_finance_profile" on public.finance_profiles;
create policy "users_manage_own_finance_profile" on public.finance_profiles for all to authenticated
using ((select auth.uid()) = user_id) with check ((select auth.uid()) = user_id);
drop policy if exists "users_manage_own_budgets" on public.budgets;
create policy "users_manage_own_budgets" on public.budgets for all to authenticated
using ((select auth.uid()) = user_id) with check ((select auth.uid()) = user_id);
drop policy if exists "users_manage_own_transactions" on public.finance_transactions;
create policy "users_manage_own_transactions" on public.finance_transactions for all to authenticated
using ((select auth.uid()) = user_id) with check ((select auth.uid()) = user_id);
drop policy if exists "users_manage_own_categories" on public.finance_categories;
create policy "users_manage_own_categories" on public.finance_categories for all to authenticated
using ((select auth.uid()) = user_id) with check ((select auth.uid()) = user_id);

create index if not exists habits_user_updated_idx on public.habits (user_id, updated_at desc);
create index if not exists transactions_user_date_idx on public.finance_transactions (user_id, transaction_on desc);
