SET
search_path TO wa_workflow_api;

alter table idempotent_keys rename to idempotency_keys;

COMMIT;
