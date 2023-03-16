from flask import Flask
from flask_restful import Api, Resource, jsonify

app = Flask(__name__)
api = Api(app)


class Prediction(Resource):
    def post(self):
        return jsonify({"location_1": (1, 1), "location_2": (2, 2), "location_3": (3, 3)})


@app.route('/')
def home():
    return "Flask heroku app."

api.add_resource(Prediction, '/prediction')

# @app.route("/predict", methods=["POST"])
# def predict():
#     return jsonify({"location_1": (1, 1), "location_2": (2, 2), "location_3": (3, 3)})


if __name__ == "__main__":
    app.run()
