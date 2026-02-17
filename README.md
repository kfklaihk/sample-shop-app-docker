#  AtSea Shop Enhanced Application
## https://sample-shop-app-docker-production.up.railway.app/

The AtSea Shop is a demonstration application comprised of: 

* Java REST application backend written using Spring-Boot, 
* a Postgres database for product inventory, customer data, and orders,
* a React shopping cart
* a NGINX reverse proxy implementing https,
* a payment gateway to simulate certificate management , not processing order

My enhancements
* add login and signup page
* add products for selection
* modify shopping cart to send order to payment gateway
* add redis to cache shopping cart
* add rabbit mq to facilitate order flow
* modify payment gateway to process order
* add screening logic and email order confirmation to payment gateway
* add api/health monitor
* cloud version
# Login screen
<img width="579" height="527" alt="image" src="https://github.com/user-attachments/assets/09c12c4d-f273-4d59-a158-24b39cb6867f" />

# Main Page
<img width="908" height="620" alt="image" src="https://github.com/user-attachments/assets/a26143b5-ecd7-41b2-8606-89974afe472b" />

# More Products
<img width="827" height="617" alt="image" src="https://github.com/user-attachments/assets/1f1a656e-4d1d-4fcb-8c11-4f4c657fe7d9" />

# Modified Shopping Cart
<img width="539" height="457" alt="image" src="https://github.com/user-attachments/assets/6f28e15b-7f81-46b7-a5ab-c9377e28c9d7" />

# Email Confirmation from Payment Gateway
<img width="641" height="412" alt="image" src="https://github.com/user-attachments/assets/0d578836-c238-4ca8-acfa-20f3ce15e7c2" />

# Cloud Architecture
<img width="757" height="515" alt="image" src="https://github.com/user-attachments/assets/a7b480f2-ee82-470e-af38-3171ccac3de6" />







