import pika
import json
import time

connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
channel = connection.channel()

for i in range(15):
    event = {
        "timestamp": "2026-02-22T10:10:10Z",
        "sourceIp": "192.168.1.100",
        "eventType": "HTTP",
        "endpoint": "/login",
        "statusCode": 401,
        "severity": "WARNING",
        "rawLog": "fake login attempt"
    }
    channel.basic_publish(
        exchange='logs.exchange',
        routing_key='logs.ingress.key',
        body=json.dumps(event)
    )
    print(f"Sent {i+1}")
    
connection.close()
