import os
from flask import Flask, jsonify, request
from flask_restful import Api, Resource
from werkzeug.utils import secure_filename
from predictor import predict, Location
import firebase_admin
from firebase_admin import credentials
from firebase_admin import db


app = Flask(__name__)
app.config.from_pyfile('config.py')
api = Api(app)


class Prediction(Resource):
    # some of this code was taken from Flask's documentation for uploading
    # files at https://flask.palletsprojects.com/en/2.1.x/patterns/fileuploads/
    def post(self):
        # no file sent
        if 'file' not in request.files:
            return jsonify({"location_0": Location(-2, -1)})
        
        file = request.files['file']
        # file is not an mp3 or mp4
        _, file_extension = os.path.splitext(file.filename)
        print(file_extension)
        if file_extension not in ['.mp3', '.mp4']:
            return jsonify({"location_0": Location(-1, -1)})
        
        filename = secure_filename(file.filename)
        file.save(os.path.join(os.getcwd(), filename))
        print(f"saving file: {os.path.join(os.getcwd(), filename)}")
        location_1, location_2, location_3 = predict(filename)
        print(f"prediction done")
        os.remove(os.path.join(os.getcwd(), filename))
        print(f"deleting file: {os.path.join(os.getcwd(), filename)}")
        return jsonify({"location_1": location_1, 
                        "location_2": location_2, 
                        "location_3": location_3})
        
        
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
    return "ProofPlus is up and running."

api.add_resource(Prediction, '/predict')
api.add_resource(EmailClient, "/email")
api.add_resource(VerseCounter, "/verse")


if __name__ == "__main__":
    app.run()
