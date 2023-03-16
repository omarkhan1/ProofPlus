import os

SECRET_KEY = os.urandom(24)
UPLOAD_FOLDER = os.getcwd() + "/uploads"