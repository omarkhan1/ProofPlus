import os
from flask import Flask, jsonify, request
from flask_restful import Api, Resource
from predictor import predict, Location


app = Flask(__name__)
app.config.from_pyfile('config.py')
api = Api(app)


class Prediction(Resource):
    # some of this code was taken from Flask's documentation for uploading
    # files at https://flask.palletsprojects.com/en/2.1.x/patterns/fileuploads/
    def post(self):
        # no file sent
        if 'file' not in request.files:
            return jsonify({"location_1": Location(0, 0, 0),
                            "location_2": Location(0, 0, 0),
                            "location_3": Location(0, 0, 0)})
        
        file = request.files['file']
        # file is not an mp3 or mp4
        _, file_extension = os.path.splitext(file.filename)
        print(file_extension)
        if file_extension not in ['.mp3', '.mp4']:
            return jsonify({"location_1": Location(0, 0, 0),
                            "location_2": Location(0, 0, 0),
                            "location_3": Location(0, 0, 0)})
            
        encoded_data = file.read()
        location_1, location_2, location_3 = predict(encoded_data)
        print(f"prediction done")
        return jsonify({"location_1": location_1, 
                        "location_2": location_2, 
                        "location_3": location_3})


@app.route('/')
def home():
    return "ProofPlus is up and running."

api.add_resource(Prediction, '/predict')


if __name__ == "__main__":
    app.run()