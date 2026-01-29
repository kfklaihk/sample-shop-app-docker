import pika
import json
import time
import os
from mailersend import MailerSendClient, EmailBuilder

# MailerSend configuration
MAILERSEND_API_KEY = os.getenv('MAILERSEND_API_KEY', 'mlsn.ad8c53a9a5e94c59dce0d29ebebd04ca6848835ced95fcbbd3300f1bd3d4976f')
MAIL_FROM_EMAIL = os.getenv('MAIL_FROM_EMAIL', 'MS_Zz26ZQ@test-2p0347zo9x7lzdrn.mlsender.net')
MAIL_FROM_NAME = os.getenv('MAIL_FROM_NAME', 'AtSea Shop Confirmation')

def send_confirmation_email(order_event):
    if not order_event.get('customerEmail'):
        print(" [!] No customer email found, skipping email.")
        return

    try:
        ms = MailerSendClient(api_key=MAILERSEND_API_KEY)
        builder = EmailBuilder()
        
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

        builder.from_email(MAIL_FROM_EMAIL, MAIL_FROM_NAME)
        builder.to(order_event['customerEmail'], customer_name)
        builder.subject(f"Order Confirmation #{order_id}")
        builder.html(html_content)
        builder.text(text_content)

        print(f" [ ] Attempting to send email to {order_event['customerEmail']}...")
        result = ms.emails.send(builder.build())
        print(f" [v] Email sent via MailerSend. {result}")

    except Exception as e:
        print(f" [!] Error sending email via MailerSend: {e}")
        print(" [!] This usually happens because 'MAIL_FROM_EMAIL' domain is not verified in MailerSend.")
        # Print full response details if available
        if hasattr(e, 'response') and e.response:
            try:
                print(f" [!] Full API Response JSON: {e.response.json()}")
            except:
                print(f" [!] Response text: {e.response.text}")
        print(" [!] --- MOCK EMAIL OUTPUT FOR DEBUGGING ---")
        print(f" [!] TO: {order_event.get('customerEmail')}")
        print(f" [!] SUBJECT: Order Confirmation #{order_id}")
        print(f" [!] CONTENT:\n{text_content}")
        print(" [!] ---------------------------------------")

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

def main():
    print(" [*] Payment Gateway Listener starting...")
    
    # Wait for RabbitMQ to be ready
    max_retries = 20
    retry_count = 0
    connection = None
    
    while retry_count < max_retries:
        try:
            connection = pika.BlockingConnection(
                pika.ConnectionParameters(host='rabbitmq', port=5672)
            )
            break
        except Exception:
            retry_count += 1
            print(f" [!] Connection to RabbitMQ failed. Retrying... ({retry_count}/{max_retries})")
            time.sleep(5)
    
    if not connection:
        print(" [!] Could not connect to RabbitMQ. Exiting.")
        return

    channel = connection.channel()

    # Ensure queue exists (Matching appserver configuration with TTL)
    arguments = {'x-message-ttl': 86400000}
    channel.queue_declare(queue='orders.created', durable=True, arguments=arguments)
    
    print(' [*] Waiting for orders on queue "orders.created". To exit press CTRL+C')

    channel.basic_qos(prefetch_count=1)
    channel.basic_consume(queue='orders.created', on_message_callback=callback)

    try:
        channel.start_consuming()
    except KeyboardInterrupt:
        channel.stop_consuming()
        connection.close()

if __name__ == '__main__':
    main()
