from flask import Flask, jsonify, flash, request, redirect, url_for, session
# from flask.ext.session import Session
from flask_restful import Api, Resource
from dataclasses import dataclass
from werkzeug.utils import secure_filename

# UPLOAD_FOLDER = '/uploads'
ALLOWED_EXTENSIONS = {'mp3', 'mp4'}

app = Flask(__name__)
# app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER
app.config.from_pyfile('config.py')
# sess = Session()
api = Api(app)


@dataclass
class Location:
    chapter: int
    verse: int
    
def allowed_file(filename):
    return '.' in filename and \
        filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


class Prediction(Resource):
    def post(self):
        if 'file' not in request.files:
            flash('No file part')
            return redirect(request.url)
        file = request.files['file']
        # If the user does not select a file, the browser submits an
        # empty file without a filename.
        if file.filename == '':
            flash('No selected file')
            return redirect(request.url)
        if file and allowed_file(file.filename):
            filename = secure_filename(file.filename)
            file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
            return redirect(url_for('download_file', name=filename))
        return jsonify({"location_1": Location(1, 1), 
                        "location_2": Location(2, 2), 
                        "location_3": Location(3, 3)})


@app.route('/')
def home():
    return "Flask heroku app."

api.add_resource(Prediction, '/predict')

# @app.route("/predict", methods=["POST"])
# def predict():
#     return jsonify({"location_1": (1, 1), "location_2": (2, 2), "location_3": (3, 3)})


if __name__ == "__main__":
    # app.secret_key = 'super secret key'
    # app.config['SESSION_TYPE'] = 'filesystem'

    # sess.init_app(app)

    # app.debug = True
    # app.run()
    app.run()
