import requests
import json

base_url = "http://localhost:8080"
login_url = f"{base_url}/api/auth/login"
order_url_no_slash = f"{base_url}/api/order"
order_url_with_slash = f"{base_url}/api/order/"

# 1. Login to get token
login_data = {"username": "repro-user", "password": "password"}
# We might need to register first if the DB was reset
register_url = f"{base_url}/api/auth/register"
register_data = {
    "username": "repro-user",
    "password": "password",
    "passwordConfirm": "password",
    "email": "repro@example.com",
    "name": "Repro User"
}

print("Attempting to register...")
reg_resp = requests.post(register_url, json=register_data)
if reg_resp.status_code in [200, 201]:
    token = reg_resp.json().get("accessToken")
    user_id = reg_resp.json().get("userId")
else:
    print(f"Register failed (maybe already exists): {reg_resp.status_code}")
    print("Attempting to login...")
    login_resp = requests.post(login_url, json=login_data)
    token = login_resp.json().get("accessToken")
    user_id = login_resp.json().get("userId")

print(f"Token: {token[:20]}...")

headers = {
    "Authorization": f"Bearer {token}",
    "Content-Type": "application/json"
}

order_data = {
    "customerId": user_id,
    "productsOrdered": {"1": 1}
}

print("\nTesting POST WITH trailing slash (/api/order/):")
resp1 = requests.post(order_url_with_slash, headers=headers, json=order_data)
print(f"Status Code: {resp1.status_code}")
if resp1.status_code != 201:
    print(f"Response: {resp1.text}")

print("\nTesting POST WITHOUT trailing slash (/api/order):")
resp2 = requests.post(order_url_no_slash, headers=headers, json=order_data)
print(f"Status Code: {resp2.status_code}")
if resp2.status_code != 201:
    print(f"Response: {resp2.text}")

print("\nTesting GET WITH trailing slash (/api/order/):")
resp3 = requests.get(order_url_with_slash, headers=headers)
print(f"Status Code: {resp3.status_code}")
if resp3.status_code != 200:
    print(f"Response: {resp3.text}")

print("\nTesting GET WITHOUT trailing slash (/api/order):")
resp4 = requests.get(order_url_no_slash, headers=headers)
print(f"Status Code: {resp4.status_code}")
if resp4.status_code != 200:
    print(f"Response: {resp4.text}")

print("\nTesting WITHOUT token (should be 403):")
resp5 = requests.get(order_url_with_slash)
print(f"Status Code: {resp5.status_code}")
