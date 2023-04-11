from flask import Flask, request
from flask_restful import Api, Resource
import firebase_admin
from firebase_admin import credentials
from firebase_admin import db


app = Flask(__name__)
api = Api(app)


class EmailClient(Resource):
    # add emails from frontend so that we can send users emails
    def post(self):
        if not firebase_admin._apps:
            cred = credentials.Certificate("cred.json")
            firebase_admin.initialize_app(cred, {
                "databaseURL": "https://proofplus-14b46-default-rtdb.firebaseio.com/"
            })
        ref = db.reference("/email")
        
        data = request.json
        ref.push(data)
        return


class VerseCounter(Resource):
    # increase count for verse each time it is selected
    def post(self):
        if not firebase_admin._apps:
            cred = credentials.Certificate("cred.json")
            firebase_admin.initialize_app(cred, {
                "databaseURL": "https://proofplus-14b46-default-rtdb.firebaseio.com/"
            })
            
        data = request.json
        verse = int(data["verse"])
        ref = db.reference(f"/verses/{verse}")
        curr_count = int(ref.get())
        ref.set(curr_count + 1)
        return
        

@app.route('/')
def home():
    return "ProofPlus email client is up and running."

api.add_resource(EmailClient, "/email")
api.add_resource(VerseCounter, "/verse")


if __name__ == "__main__":
    app.run()
