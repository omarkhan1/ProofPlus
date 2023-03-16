from flask import Flask, jsonify, request
from flask_restful import Api, Resource
from dataclasses import dataclass
from werkzeug.utils import secure_filename
import os

ALLOWED_EXTENSIONS = {'mp3', 'mp4'}

app = Flask(__name__)
app.config.from_pyfile('config.py')
api = Api(app)


@dataclass
class Location:
    chapter: int
    verse: int


class Prediction(Resource):
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
        # file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
        return jsonify({"location_1": Location(1, 1), 
                        "location_2": Location(2, 2), 
                        "location_3": Location(3, 3)})


@app.route('/')
def home():
    return "ProofPlus is up and running."

api.add_resource(Prediction, '/predict')


if __name__ == "__main__":
    app.run()
