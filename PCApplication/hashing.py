import hashlib
def hashing(data):
        h = hashlib.sha512(data.encode("ASCII"))
        data = h.hexdigest()
        return data