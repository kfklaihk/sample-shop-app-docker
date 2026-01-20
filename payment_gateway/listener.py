import pika
import json
import time
import os

def callback(ch, method, properties, body):
    try:
        order = json.loads(body)
        print(f" [x] RECEIVED ORDER - ID: {order.get('orderId')}")
        print(f" [x] Customer ID: {order.get('customerId')}")
        print(f" [x] Products count: {len(order.get('productsOrdered', {}))}")
        print(" [x] Processing payment...")
        time.sleep(1)
        print(" [v] PAYMENT SUCCESSFUL!")
        ch.basic_ack(delivery_tag=method.delivery_tag)
    except Exception as e:
        print(f" [!] Error processing order: {e}")
        # Reject and requeue
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

    # Ensure queue exists
    channel.queue_declare(queue='orders.created', durable=True)
    
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
