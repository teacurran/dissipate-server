printf '{
  "type": "service_account",
  "project_id": "@define",
  "private_key_id": "%s",
  "private_key": "%s",
  "client_email": "@define",
  "client_id": "@define",
  "auth_uri": "@define",
  "token_uri": "@define",
  "auth_provider_x509_cert_url": "@define",
  "client_x509_cert_url": "@define"
}
' "$PRIVATE_KEY_ID" "${PRIVATE_KEY}" > ./google_credentials.json
ls .
