-- IBMS ISP Billing Management System
-- V1__init.sql — Canonical PostgreSQL DDL
-- 13 tables + enums + triggers

-- ─── Extensions ──────────────────────────────────────────────────────────────
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ─── Enums ───────────────────────────────────────────────────────────────────
CREATE TYPE user_role AS ENUM ('secretary', 'finance', 'payables', 'sysadmin');
CREATE TYPE account_status AS ENUM ('active', 'inactive', 'terminated', 'suspended');
CREATE TYPE topsheet_status AS ENUM ('draft', 'compiled', 'approved', 'paid', 'cancelled');
CREATE TYPE transfer_kind AS ENUM ('store_transfer', 'provider_transfer');
CREATE TYPE attachment_kind AS ENUM ('subscription_proof', 'store_proof', 'transfer_proof', 'topsheet_export', 'ocr_upload', 'other');
CREATE TYPE activity_action AS ENUM ('create', 'update', 'delete', 'compile', 'approve', 'pay', 'transfer', 'deactivate', 'reactivate', 'login');

-- ─── 1. users ────────────────────────────────────────────────────────────────
CREATE TABLE users (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    google_sub  TEXT NOT NULL UNIQUE,
    email       TEXT NOT NULL,
    display_name TEXT NOT NULL,
    role        user_role,
    avatar_url  TEXT,
    legacy_id   TEXT,  -- old Firebase UID for ETL mapping
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ─── 2. providers ────────────────────────────────────────────────────────────
CREATE TABLE providers (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name          TEXT NOT NULL,
    code          TEXT NOT NULL UNIQUE,
    contact_email TEXT,
    contact_phone TEXT,
    is_active     BOOLEAN NOT NULL DEFAULT true,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ─── 3. stores ───────────────────────────────────────────────────────────────
CREATE TABLE stores (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        TEXT NOT NULL,
    branch_code TEXT NOT NULL UNIQUE,
    address     TEXT,
    is_active   BOOLEAN NOT NULL DEFAULT true,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ─── 4. accounts ─────────────────────────────────────────────────────────────
CREATE TABLE accounts (
    id                       UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    provider_id              UUID NOT NULL REFERENCES providers(id),
    store_id                 UUID NOT NULL REFERENCES stores(id),
    account_number           TEXT NOT NULL,
    account_name             TEXT,
    rate                     NUMERIC(14,2) NOT NULL,
    installation_date        DATE,
    status                   account_status NOT NULL DEFAULT 'active',
    termination_requested_at TIMESTAMPTZ,
    created_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_provider_account_number UNIQUE (provider_id, account_number)
);

CREATE INDEX idx_accounts_store ON accounts(store_id);
CREATE INDEX idx_accounts_provider ON accounts(provider_id);
CREATE INDEX idx_accounts_status ON accounts(status);

-- ─── 5. attachments ──────────────────────────────────────────────────────────
CREATE TABLE attachments (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    kind          attachment_kind NOT NULL,
    file_name     TEXT NOT NULL,
    content_type  TEXT,
    storage_path  TEXT NOT NULL,  -- object-storage key
    uploaded_by_id UUID REFERENCES users(id),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ─── 6. store_attachments (join) ─────────────────────────────────────────────
CREATE TABLE store_attachments (
    store_id      UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    attachment_id UUID NOT NULL REFERENCES attachments(id) ON DELETE CASCADE,
    PRIMARY KEY (store_id, attachment_id)
);

-- ─── 7. account_attachments (join) ───────────────────────────────────────────
CREATE TABLE account_attachments (
    account_id    UUID NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    attachment_id UUID NOT NULL REFERENCES attachments(id) ON DELETE CASCADE,
    PRIMARY KEY (account_id, attachment_id)
);

-- ─── 8. invoice_sequences ────────────────────────────────────────────────────
CREATE TABLE invoice_sequences (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    provider_id UUID NOT NULL REFERENCES providers(id) UNIQUE,
    prefix      TEXT NOT NULL,
    next_number INTEGER NOT NULL DEFAULT 1
);

-- ─── 9. topsheets ────────────────────────────────────────────────────────────
CREATE TABLE topsheets (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    provider_id     UUID NOT NULL REFERENCES providers(id),
    billing_period  TEXT NOT NULL,  -- YYYY-MM
    invoice_number  TEXT,
    status          topsheet_status NOT NULL DEFAULT 'draft',
    total_amount    NUMERIC(14,2),
    line_count      INTEGER NOT NULL DEFAULT 0,
    compiled_by_id  UUID REFERENCES users(id),
    compiled_at     TIMESTAMPTZ,
    approved_by_id  UUID REFERENCES users(id),
    approved_at     TIMESTAMPTZ,
    paid_by_id      UUID REFERENCES users(id),
    paid_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_topsheets_provider_period ON topsheets(provider_id, billing_period);
CREATE INDEX idx_topsheets_status ON topsheets(status);

-- ─── 10. topsheet_details ────────────────────────────────────────────────────
CREATE TABLE topsheet_details (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    topsheet_id      UUID NOT NULL REFERENCES topsheets(id) ON DELETE CASCADE,
    account_id       UUID NOT NULL REFERENCES accounts(id),
    billing_period   TEXT NOT NULL,  -- YYYY-MM
    rate             NUMERIC(14,2) NOT NULL,
    active_days      INTEGER NOT NULL,
    total_days       INTEGER NOT NULL,
    prorated_amount  NUMERIC(14,2) NOT NULL,
    installation_date DATE,
    account_number   TEXT,
    store_name       TEXT,
    branch_code      TEXT,

    CONSTRAINT uq_account_per_period UNIQUE (account_id, billing_period)
);

CREATE INDEX idx_topsheet_details_topsheet ON topsheet_details(topsheet_id);

-- ─── 11. transfers ───────────────────────────────────────────────────────────
CREATE TABLE transfers (
    id                 UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_id         UUID NOT NULL REFERENCES accounts(id),
    kind               transfer_kind NOT NULL,
    from_store_id      UUID REFERENCES stores(id),
    to_store_id        UUID REFERENCES stores(id),
    from_provider_id   UUID REFERENCES providers(id),
    to_provider_id     UUID REFERENCES providers(id),
    new_account_number TEXT,
    proof_attachment_id UUID REFERENCES attachments(id),
    transferred_by_id  UUID REFERENCES users(id),
    transferred_at     TIMESTAMPTZ,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ─── 12. activities ──────────────────────────────────────────────────────────
CREATE TABLE activities (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    actor_id    UUID NOT NULL REFERENCES users(id),
    action      activity_action NOT NULL,
    entity_type TEXT NOT NULL,
    entity_id   UUID NOT NULL,
    description TEXT,
    metadata    JSONB,  -- before/after snapshots
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_activities_entity ON activities(entity_type, entity_id);
CREATE INDEX idx_activities_actor ON activities(actor_id);
CREATE INDEX idx_activities_created ON activities(created_at DESC);

-- ─── 13. email_log ───────────────────────────────────────────────────────────
CREATE TABLE email_log (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    recipient_email TEXT NOT NULL,
    subject         TEXT NOT NULL,
    template_name   TEXT,
    status          TEXT NOT NULL DEFAULT 'pending',  -- pending, sent, failed
    sent_at         TIMESTAMPTZ,
    error_message   TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ─── 14. ocr_templates ───────────────────────────────────────────────────────
CREATE TABLE ocr_templates (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    provider_name     TEXT NOT NULL,
    template_key      TEXT NOT NULL UNIQUE,
    extraction_schema JSONB,
    is_active         BOOLEAN NOT NULL DEFAULT true,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ─── 15. ocr_batches ─────────────────────────────────────────────────────────
CREATE TABLE ocr_batches (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    uploaded_by_id UUID NOT NULL REFERENCES users(id),
    template_id    UUID REFERENCES ocr_templates(id),
    file_name      TEXT NOT NULL,
    status         TEXT NOT NULL DEFAULT 'processing',  -- processing, completed, failed
    row_count      INTEGER NOT NULL DEFAULT 0,
    matched_count  INTEGER NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ─── 16. ocr_extracted_rows ──────────────────────────────────────────────────
CREATE TABLE ocr_extracted_rows (
    id                 UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    batch_id           UUID NOT NULL REFERENCES ocr_batches(id) ON DELETE CASCADE,
    account_number     TEXT,
    extracted_amount   NUMERIC(14,2),
    matched_account_id UUID REFERENCES accounts(id),
    variance_flag      BOOLEAN NOT NULL DEFAULT false,
    raw_data           JSONB,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_ocr_rows_batch ON ocr_extracted_rows(batch_id);

-- ─── Triggers: auto-update updated_at ────────────────────────────────────────
CREATE OR REPLACE FUNCTION trigger_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_updated_at_users BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

CREATE TRIGGER set_updated_at_providers BEFORE UPDATE ON providers
    FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

CREATE TRIGGER set_updated_at_stores BEFORE UPDATE ON stores
    FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

CREATE TRIGGER set_updated_at_accounts BEFORE UPDATE ON accounts
    FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

CREATE TRIGGER set_updated_at_topsheets BEFORE UPDATE ON topsheets
    FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

CREATE TRIGGER set_updated_at_ocr_templates BEFORE UPDATE ON ocr_templates
    FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();
