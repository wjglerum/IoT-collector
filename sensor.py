#!/usr/bin/python2.7
import json
import datetime
import paho.mqtt.publish as publish

color = "red"
topic = "iot"
mqtt_hostname = "localhost"
mqtt_port = 1883

while True:
    humidity = round(random.uniform(0,100), 1)
    temperature = round(random.uniform(-10,30), 1)
    timestamp = str(datetime.datetime.now())
    log.info('Timestamp: {} Temp: {:0.1f} C  Humidity: {:0.1f} %'.format(timestamp, temperature, humidity))
    measurement = {'sensor': color, 'temperature': temperature, 'humidity': humidity, 'timestamp': timestamp}
    payload = json.dumps(measurement)
    publish.single(topic, payload, hostname=mqtt_hostname, port=mqtt_port)