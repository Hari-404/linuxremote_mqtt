import pyautogui as pyauto

import os
import fcntl
import time
import subprocess
import logging
import socket
import sys
import paho.mqtt.client as mqtt
import json
from Crypto.Cipher import AES
from binascii import unhexlify

from time import sleep
from zeroconf import ServiceInfo, Zeroconf
from settings import settings


def on_connect(client, userdata, flags, rc):
        print("rc: " + str(rc))


def on_message(client, obj, msg):
        print(msg.topic + " " + str(msg.qos) + " " + str(msg.payload))
        key = str(msg.payload.decode('utf-8'))
        if key.isnumeric():
                keyBoard(int(key))
        else:
                mouse(key)


def on_publish(client, obj, mid):
        print("mid: " + str(mid))


def on_subscribe(client, obj, mid, granted_qos):
        print("Subscribed: " + str(mid) + " " + str(granted_qos))


def on_log(client, obj, level, string):
        print(string)


def mouse(key):
        global drag
        sp = key.split(" ")
        print(key)
        mouseClicks = ['left', 'middle', 'right']
        if sp[1].isnumeric():
                pyauto.click(button=mouseClicks[int(sp[1])], clicks=int(sp[2]))
        else:
                if sp[1] == 's':
                        a = int(float(sp[2]))
                        pyauto.scroll(-a)

                elif sp[1] == 'x':
                        if drag:
                                pyauto.mouseUp()
                                drag = False
                                print("Drag released...")
                        try:
                                pyauto.moveRel(float(sp[2]), float(sp[3]))
                        except Exception:
                                pyauto.FAILSAFE = False
                                print("Exception while moving cursor")

                else:

                        if not drag:
                                pyauto.mouseDown()
                                drag = True
                                print("Drag...")
                        try:
                                pyauto.moveRel(float(sp[2]), float(sp[3]))
                        except Exception:
                                pyauto.FAILSAFE = False
                                print("Exception while moving cursor")


drag = False


def keyBoard(key):
        global shift
        global ctrl
        global alt
        if 1 <= key <= 127:
                s = chr(key)
                if key == 60:
                        pyauto.hotkey("shift", ',')
                else:
                        pyauto.press(s)

        else:
                if 260 <= key < 300:
                        try:
                                subprocess.Popen(commands[key - 260])
                        except Exception:
                                print("Exception while controlling volume")
                else:
                        if key == 300:
                                if alt == 1:
                                        print("alt up")
                                        mqttc.publish(publishTopic, "alt up")
                                        pyauto.keyUp('alt')
                                        alt = 0
                                else:
                                        print("alt down")
                                        pyauto.keyDown('alt')
                                        alt = 1
                                        mqttc.publish(publishTopic, "alt down")
                        elif key == 301:
                                if ctrl == 1:
                                        print("ctrl up")
                                        mqttc.publish(publishTopic, "ctrl up")
                                        pyauto.keyUp('ctrl')
                                        ctrl = 0
                                else:
                                        print("ctrl down")
                                        pyauto.keyDown('ctrl')
                                        ctrl = 1
                                        mqttc.publish(publishTopic, "ctrl down")
                        elif key == 302:
                                if shift == 1:
                                        print("shift up")
                                        mqttc.publish(publishTopic, "shift up")
                                        pyauto.keyUp('shift')
                                        shift = 0
                                else:
                                        print("shift down")
                                        pyauto.keyDown('shift')
                                        shift = 1
                                        mqttc.publish(publishTopic, "shift down")
                        else:
                                print(keyDict[key - 300])
                                pyauto.press(keyDict[key - 300])


def get_instance():
        global fh
        global bundle_dir
        fh = open(sys.argv[0], 'r')
        try:
                fcntl.flock(fh, fcntl.LOCK_EX | fcntl.LOCK_NB)
        except:
                print("Execption while acquiring lock.")
                os._exit(0)
fh = 0

if getattr(sys, 'frozen', False):
        bundle_dir = sys._MEIPASS
else:
        bundle_dir = os.path.dirname(os.path.abspath(__file__))

if __name__ == '__main__':
        get_instance()
        log = False
        logging.basicConfig(level=logging.DEBUG)
        if len(sys.argv) > 1:
                argv = sys.argv[1]
                if argv == "-h":
                        help = """Usage linuxremote [OPTIONS]\n-h show ths help message\n-s settings\n-d debug"""
                        print(help)
                        os._exit(0)
                elif argv == "-d":
                        log = True
                elif argv == "-s":
                        settings()
                else:
                        print("linuxremote -h show ths help message\n-s settings\n-d debug")
                        os._exit(0)

        if not os.path.isfile("./config/mosquitto.conf"):
                print("Configuration file is not found.")
                os._exit(0)
        shift = 0
        ctrl = 0
        alt = 0
        publishTopic = "keyReply"

        keyDict = ['alt', 'ctrl', 'shift', 'del', 'down', 'right', 'left', 'up', 'end', 'home', 'insert', '',
                   'pagedown', 'pageup', '', 'pgdn', 'pgup', '', '', '', '', '',
                   'prtscr', '', '', '', 'winleft', '', '', '',
                   'escape', 'f1', 'f2', 'f3', 'f4', 'f5', 'f6', 'f7', 'f8', 'f9', 'f10']
        commands = [("amixer", "-q", "sset", "Master", "10%+"), ("amixer", "-q", "sset", "Master", "10%-"),
                    ("amixer", "-q", "sset", "Master", "toggle"), "poweroff", "reboot"]
        desc = {'path': '/~hari/'}
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        s = s.getsockname()[0].split('.')
        hostName = socket.gethostname()
        name = "" + hostName + " " + s[0] + " " + s[1] + " " + s[2] + " " + s[3] + "._mqtt._tcp.local."
        info = ServiceInfo("_mqtt._tcp.local.", name, 0, 80, 0, 0, desc, "hari.local.")
        zeroconf = Zeroconf()
        print("Registration of a service, press Ctrl-C to exit...")
        zeroconf.register_service(info)
        # MQTT Broker
        f = os.getcwd()
        proc = subprocess.Popen(["mosquitto", "-c", f + "/config/mosquitto.conf"])
        print(proc)
        sleep(1)
        # MQTT Client
        mqttc = mqtt.Client()
        mqttc.on_message = on_message
        mqttc.on_connect = on_connect
        mqttc.on_publish = on_publish
        mqttc.on_subscribe = on_subscribe
        if log:
                mqttc.on_log = on_log
        topic = 'key'
        try:
                f = open("./config/client.json", "r")
        except:
                print("client.json file not found")
                os._exit(0)
        json_file = json.loads(f.read())
        f.close()
        u = json_file["username"]
        p = json_file["password"]
        port = json_file["port"]
        u = unhexlify(u.encode("ascii"))
        p = unhexlify(p.encode("ascii"))
        obj2 = AES.new("c801927ec3de19b69d4c5ee03584cca3", AES.MODE_CFB, 'This is an IV456')
        username = obj2.decrypt(u).decode("ascii")
        password = obj2.decrypt(p).decode("ascii")
        mqttc.username_pw_set(username, password)
        mqttc.connect("localhost", port)
        mqttc.subscribe(topic, 0)
        rc = 0
        try:
                while rc == 0:
                        rc = mqttc.loop()
        except KeyboardInterrupt:
                pass
        finally:
                print("Unregistering...")
                zeroconf.unregister_service(info)
                zeroconf.close()
                proc.terminate()
                print("rc: " + str(rc))
