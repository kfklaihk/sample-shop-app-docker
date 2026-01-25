#!/bin/bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"username":"testuser_2", "password":"password"}' | jq -r .accessToken)
if [ "$TOKEN" == "null" ] || [ -z "$TOKEN" ]; then
  echo "Login failed"
  exit 1
fi
echo "Login success. Token: ${TOKEN:0:20}..."

echo "Adding item to cart..."
curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/api/cart/ \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "name": "Standard Tablet", "price": 499.0, "quantity": 1, "image": "tablet.jpg"}'
echo " (HTTP Status)"

echo "Fetching cart..."
curl -s -X GET http://localhost:8080/api/cart/ -H "Authorization: Bearer $TOKEN"
echo ""
