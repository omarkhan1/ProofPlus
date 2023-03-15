from pydantic import BaseModel
from fastapi import FastAPI, UploadFile
from fastapi.exceptions import HTTPException
# from model.model import predict_pipeline
import os

app = FastAPI()




class Item(BaseModel):
    name: str
    price: float
    
class Location(BaseModel):
    chapter: int
    verse: int
    
class Prediction(BaseModel):
    location_1: Location
    location_2: Location
    location_3: Location


@app.get("/")
def home():
    return {"Hello": "World"}


@app.put("/model/predict")
async def predict(file: UploadFile) -> Prediction:
    # check if we are actually receiving an mp4 file form the user
    if file.content_type != "application/mp4":
        raise HTTPException(400, detail="Invalid file type, must be mp4.")
    
    file.filename = "recording.mp4"
    contents = await file.read()
    
    
    
    # get predictions
    # c_1, v_1, c_2, v_2, c_3, v_3 = predict_pipeline(file)
    c_1, v_1, c_2, v_2, c_3, v_3 = 1, 2, 3, 4, 5, 6
    location_1 = Location(c_1, v_1)
    location_2 = Location(c_2, v_2)
    location_3 = Location(c_3, v_3)
    
    return {"location_1": location_1, "location_2": location_2, "location_3": location_3}

@app.get("/login")
def login():
    return {"Hello": "World"}