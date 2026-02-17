import pika
import json
import time
import os
import threading
from http.server import BaseHTTPRequestHandler, HTTPServer
import sib_api_v3_sdk
from sib_api_v3_sdk.rest import ApiException

# Brevo configuration (Transactional Email)
BREVO_API_KEY = os.getenv('BREVO_API_KEY', '')
BREVO_FROM_EMAIL = os.getenv('BREVO_FROM_EMAIL', '')
BREVO_FROM_NAME = os.getenv('BREVO_FROM_NAME', 'AtSea Shop Confirmation')

health_state = {
    "rabbitmq_connected": False
}

def send_confirmation_email(order_event):
    if not order_event.get('customerEmail'):
        print(" [!] No customer email found, skipping email.")
        return

    if not BREVO_API_KEY or not BREVO_FROM_EMAIL:
        print(" [!] Brevo is not configured (missing BREVO_API_KEY or BREVO_FROM_EMAIL).")
        return

    try:
        configuration = sib_api_v3_sdk.Configuration()
        configuration.api_key['api-key'] = BREVO_API_KEY
        api_instance = sib_api_v3_sdk.TransactionalEmailsApi(
            sib_api_v3_sdk.ApiClient(configuration)
        )

        # Prepare products list for email
        products_html = "<ul>"
        products_text = ""
        for p in order_event.get('products', []):
            line = f"<li>{p['name']} - Qty: {p['quantity']} - ${p['price']:.2f} each</li>"
            products_html += line
            products_text += f"- {p['name']} x {p['quantity']} @ ${p['price']:.2f}\n"
        products_html += "</ul>"

        total = order_event.get('totalPrice', 0.0)
        customer_name = order_event.get('customerName', 'Customer')
        order_id = order_event.get('orderId', 'N/A')

        # Construct email body
        payment_link = f"http://localhost:8080/checkout?order={order_id}"
        html_content = f"""
        <h3>Hello {customer_name},</h3>
        <p>Thank you for your order <b>#{order_id}</b>!</p>
        <p><b>Items purchased:</b></p>
        {products_html}
        <p><b>Total: ${total:.2f}</b></p>
        <hr>
        <p>We are currently performing background checks. Once completed, you can use the link below to pay:</p>
        <p><a href='{payment_link}' style='background-color: #008CBA; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Pay Now</a></p>
        <p>Best regards,<br>The AtSea Team</p>
        """

        text_content = f"""
        Hello {customer_name},
        
        Thank you for your order #{order_id}!
        
        Items:
        {products_text}
        Total: ${total:.2f}
        
        Pay now: {payment_link}
        
        Best regards,
        The AtSea Team
        """

        print(f" [ ] Attempting to send email to {order_event['customerEmail']}...")
        email = sib_api_v3_sdk.SendSmtpEmail(
            to=[{"email": order_event['customerEmail'], "name": customer_name}],
            subject=f"Order Confirmation #{order_id}",
            html_content=html_content,
            text_content=text_content,
            sender={"name": BREVO_FROM_NAME, "email": BREVO_FROM_EMAIL}
        )
        result = api_instance.send_transac_email(email)
        print(f" [v] Email sent via Brevo. {result}")

    except ApiException as e:
        print(f" [!] Brevo API error: {e}")
    except Exception as e:
        print(f" [!] Error sending email via Brevo: {e}")

def callback(ch, method, properties, body):
    try:
        order_event = json.loads(body)
        print(f" [x] RECEIVED ORDER EVENT - ID: {order_event.get('orderId')}")
        print(f" [x] Customer: {order_event.get('customerName')} ({order_event.get('customerEmail')})")
        for p in order_event.get('products', []):
            print(f"     - {p.get('name')}: {p.get('quantity')} x ${p.get('price')}")
        print(" [x] Processing background checks...")
        
        # Send confirmation email
        send_confirmation_email(order_event)
        
        time.sleep(1)
        print(" [v] BACKGROUND CHECKS COMPLETED!")
        ch.basic_ack(delivery_tag=method.delivery_tag)
    except Exception as e:
        print(f" [!] Error processing event: {e}")
        ch.basic_nack(delivery_tag=method.delivery_tag, requeue=True)


class HealthHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path not in ('/health', '/healthz', '/'):
            self.send_response(404)
            self.end_headers()
            return

        rabbitmq_ok = health_state.get("rabbitmq_connected", False)
        status_code = 200 if rabbitmq_ok else 503
        payload = {
            "status": "ok" if rabbitmq_ok else "down",
            "rabbitmq": "up" if rabbitmq_ok else "down"
        }
        body = json.dumps(payload).encode("utf-8")

        self.send_response(status_code)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, format, *args):
        # Silence default HTTP server logs.
        return


def start_health_server():
    port = int(os.getenv('PAYMENT_GATEWAY_HEALTH_PORT', os.getenv('PORT', '8080')))
    server = HTTPServer(('0.0.0.0', port), HealthHandler)
    thread = threading.Thread(target=server.serve_forever, daemon=True)
    thread.start()
    print(f" [*] Health server listening on 0.0.0.0:{port}")


def build_rabbitmq_parameters():
    amqp_url = os.getenv('SPRING_RABBITMQ_URI')
    if amqp_url:
        print(" [*] Using SPRING_RABBITMQ_URI for RabbitMQ connection.")
        return pika.URLParameters(amqp_url)

    amqp_url = os.getenv('AMQP_URL')
    if amqp_url:
        print(" [*] Using AMQP_URL for RabbitMQ connection.")
        return pika.URLParameters(amqp_url)

    amqp_url = os.getenv('RABBITMQ_URL')
    if amqp_url:
        print(" [*] Using RABBITMQ_URL for RabbitMQ connection.")
        return pika.URLParameters(amqp_url)

    host = os.getenv('RABBITMQ_HOST', 'rabbitmq')
    port = int(os.getenv('RABBITMQ_PORT', '5672'))
    username = os.getenv('RABBITMQ_USER') or os.getenv('RABBITMQ_USERNAME')
    password = os.getenv('RABBITMQ_PASSWORD')
    vhost = os.getenv('RABBITMQ_VHOST', '/')

    credentials = None
    if username and password:
        credentials = pika.PlainCredentials(username, password)
        print(" [*] Using explicit RabbitMQ credentials.")
    elif username or password:
        print(" [!] RabbitMQ username or password missing.")

    if host == 'rabbitmq' and os.getenv('RAILWAY_PROJECT_ID') and not os.getenv('RABBITMQ_HOST'):
        print(" [!] RabbitMQ host not configured. On Railway, attach the RabbitMQ")
        print("     service variables (RABBITMQ_URL/AMQP_URL) to payment_gateway.")

    user_label = "set" if username else "not set"
    print(f" [*] Using RabbitMQ host={host} port={port} vhost={vhost} user={user_label}")

    return pika.ConnectionParameters(
        host=host,
        port=port,
        virtual_host=vhost,
        credentials=credentials
    )

def connect_with_retries(parameters, max_retries, retry_interval):
    retry_count = 0
    while retry_count < max_retries:
        try:
            return pika.BlockingConnection(parameters)
        except Exception as ex:
            retry_count += 1
            print(f" [!] Connection to RabbitMQ failed: {ex}. Retrying... ({retry_count}/{max_retries})")
            time.sleep(retry_interval)
    return None


def main():
    print(" [*] Payment Gateway Listener starting...")
    start_health_server()

    parameters = build_rabbitmq_parameters()
    max_retries = int(os.getenv('PAYMENT_GATEWAY_MAX_RETRIES', '20'))
    retry_interval = int(os.getenv('PAYMENT_GATEWAY_RETRY_INTERVAL_SECONDS', '5'))
    retry_mode = os.getenv('PAYMENT_GATEWAY_RETRY_MODE', 'bounded').lower()
    cooldown = int(os.getenv('PAYMENT_GATEWAY_RETRY_COOLDOWN_SECONDS', '60'))

    while True:
        health_state["rabbitmq_connected"] = False
        connection = connect_with_retries(parameters, max_retries, retry_interval)
        if not connection:
            message = f" [!] Could not connect to RabbitMQ after {max_retries} attempts."
            if retry_mode == 'loop':
                print(f"{message} Sleeping {cooldown} seconds before retrying.")
                time.sleep(cooldown)
                continue
            print(f"{message} Exiting.")
            return

        try:
            health_state["rabbitmq_connected"] = True
            channel = connection.channel()

            # Ensure queue exists (Matching appserver configuration with TTL)
            arguments = {'x-message-ttl': 86400000}
            channel.queue_declare(queue='orders.created', durable=True, arguments=arguments)

            print(' [*] Waiting for orders on queue "orders.created". To exit press CTRL+C')

            channel.basic_qos(prefetch_count=1)
            channel.basic_consume(queue='orders.created', on_message_callback=callback)
            channel.start_consuming()
        except KeyboardInterrupt:
            print(" [*] Payment Gateway Listener shutting down.")
            break
        except Exception as ex:
            print(f" [!] RabbitMQ connection lost: {ex}.")
        finally:
            health_state["rabbitmq_connected"] = False
            if connection is not None:
                try:
                    if connection.is_open:
                        connection.close()
                except Exception:
                    pass

if __name__ == '__main__':
    main()
