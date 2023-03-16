from fastapi import File, UploadFile

def predict_pipeline(file: UploadFile):
    return ((1, 1), (1, 2), (1, 3))
