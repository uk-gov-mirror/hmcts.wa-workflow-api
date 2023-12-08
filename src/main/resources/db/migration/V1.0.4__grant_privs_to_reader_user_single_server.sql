-- Grant usage and select privs on Reader user
GRANT USAGE ON SCHEMA wa_workflow_api TO "${dbReaderUsername}";
GRANT SELECT ON ALL TABLES IN SCHEMA wa_workflow_api TO "${dbReaderUsername}";
