#!/bin/bash
echo "Testing unauthorized access..."
CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8083/api/dashboard/summary)
echo "Unauthorized CODE: $CODE"

echo "Logging in..."
JSON_RESP=$(curl -s -X POST http://localhost:8083/auth/login)
echo "Login Response: $JSON_RESP"
TOKEN=$(echo "$JSON_RESP" | grep -oP '"token":"\K[^"]+')
echo "Extracted Token: $TOKEN"

echo "Testing authorized access..."
CODE2=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $TOKEN" http://localhost:8083/api/dashboard/summary)
echo "Authorized CODE: $CODE2"

echo "Testing save draft..."
DRAFT_SAVE_RESP=$(curl -s -X POST -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"payload":"{\"key\":\"value\"}"}' http://localhost:8083/api/draft)
echo "Draft Save Response: $DRAFT_SAVE_RESP"

echo "Testing get draft..."
DRAFT_GET_RESP=$(curl -s -X GET -H "Authorization: Bearer $TOKEN" http://localhost:8083/api/draft)
echo "Draft Get Response: $DRAFT_GET_RESP"

