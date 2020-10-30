import os
import json
from Crypto.Cipher import AES
from binascii import hexlify
from hashing import hashing


def n1():

        if not os.path.isdir("config"):
                os.system("mkdir config")

        if not os.path.isfile("./config/.master"):
                newmasterkey()

        if not checkmasterkey():
                print("Wrong password.")
                return

        directory = os.getcwd()
        password_file = ".password.txt"
        user = input("Enter user name [Default root]: ") or "root"
        password = input("Enter password [Default toor]: ") or "toor"
        try:
                port = int(input("Enter port [Default 2511]: ") or "2511")
        except:
                print("Input can only be integer.")

        #log = input("Enter path for log: [Default "+directory+"/linuxremote.log]: ") or ""+directory+"/linuxremote.log"

        os.system("touch ./config/" + password_file)
        os.system("mosquitto_passwd -b ./config/" + password_file + " " + user + " " + password)

        conf = "password_file " + directory + "/config/" + password_file
        conf = conf + "\n" + "port " + str(port)
        #conf = conf + "\n" + "log_dest file " + log

        f = open("./config/mosquitto.conf", 'w')
        f.write(conf)
        f.close()

        if not os.path.isfile("./config/client.json"):
                createclientfile()
                editclientfile(user, password, str(port))


def n2():
        try:
                f = open("./config/.master", "r")
        except:
                print("Master key is not set")
                return 

        temp = f.read()
        f.close()

        if not os.path.isfile("./config/client.json"):
                createclientfile()

        master = input("Enter master key: ")

        master = hashing(master)

        if temp == master:
                username = input("Enter user name: ")
                password = input("Enter password: ")
                port = input("Enter port: ")
                editclientfile(username, password, port)


def n3():
        if os.path.isdir("config"):
                if not os.path.isfile("./config/.master"):
                        newmasterkey()
                else:

                        if checkmasterkey():
                                master = input("Enter new key: ")
                                master = hashing(master)
                                f = open("./config/.master", 'w')
                                f.write(master)
                                f.close()
                                print("Successfully changed.")
                        else:
                                print("Wrong key.")
        else:
                print("Configuration files are missing.")


def createclientfile():
        f = open("./config/client.json", "w")
        print("client.json file not found")
        print("Creating client.json")
        f.write("{}")
        f.close()


def editclientfile(username, password, port):
        f = open("./config/client.json", "r")
        json_file = json.loads(f.read())
        f.close()
        try:
                if len(username) > 0 and len(password) > 0:
                        obj = AES.new("c801927ec3de19b69d4c5ee03584cca3", AES.MODE_CFB, 'This is an IV456')

                        a = obj.encrypt(username)
                        b = hexlify(a)
                        c = b.decode("ASCII")
                        json_file["username"] = c

                        a = obj.encrypt(password)
                        b = hexlify(a)
                        c = b.decode("ASCII")
                        json_file["password"] = c
        except Exception:
                print("Exception while encrypting." + Exception)
        try:
                if len(port) > 0:
                        port = int(port)
                        json_file["port"] = port
        except:
                print("Input should be integer:")
        print(json_file)
        f = open("./config/client.json", "w")
        f.write(json.dumps(json_file))
        f.close()


def checkmasterkey():
        master = input("Enter master key: ")
        f = open("./config/.master", "r")
        temp = f.read()
        f.close()
        master = hashing(master)
        if temp == master:
                return True
        return False


def newmasterkey():
        master = input("Enter new master key: ")
        master = hashing(master)
        f = open("./config/.master", 'w')
        f.write(master)
        f.close()


def settings():
        while True:
                print("\n1 Configure MQTT Broker.")
                print("2 Configure MQTT Client.")
                print("3 Chage master key.")
                print("4 Exit.\n")
                n = int(input("Enter your choice: "))
                if n > 4 or n < 1:
                        print("Invalid input")
                        os._exit(0)
                if n == 1:
                        n1()
                elif n == 2:
                        n2()
                elif n == 3:
                        n3()
                else:
                        os._exit(0)


if __name__ == '__main__':
        settings()

