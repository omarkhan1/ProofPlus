import firebase_admin
from firebase_admin import credentials
from firebase_admin import db
import numpy as np


def get_surah_and_verse(label):
    chapter_start_loc = [0, 7, 293, 493, 669, 789, 954, 1160, 1235, 1364, 1473, 
                         1596, 1707, 1750, 1802, 1901, 2029, 2140, 2250, 2348, 
                         2483, 2595, 2673, 2791, 2855, 2932, 3159, 3252, 3340, 
                         3409, 3469, 3503, 3533, 3606, 3660, 3705, 3788, 3970, 
                         4058, 4133, 4218, 4272, 4325, 4414, 4473, 4510, 4545, 
                         4583, 4612, 4630, 4675, 4735, 4784, 4846, 4901, 4979, 
                         5075, 5104, 5126, 5150, 5163, 5177, 5188, 5199, 5217, 
                         5229, 5241, 5271, 5323, 5375, 5419, 5447, 5475, 5495, 
                         5551, 5591, 5622, 5672, 5712, 5758, 5800, 5829, 5848, 
                         5884, 5909, 5931,  5948, 5967, 5993, 6023, 6043, 6058, 
                         6079, 6090, 6098, 6106, 6125, 6130, 6138, 6146, 6157,
                         6168, 6176, 6179, 6188, 6193, 6197, 6204, 6207, 6213, 
                         6216, 6221, 6225, 6230, 6236]

    for i in range(114):
        if chapter_start_loc[i] <= label and chapter_start_loc[i+1] > label:
            surah = i + 1
            verse = label - chapter_start_loc[i] + 1
            return (int(surah), int(verse))


def reset_verse_counts():
    # sets all verse counts to zero to start the new week
    if not firebase_admin._apps:
        cred = credentials.Certificate("cred.json")
        firebase_admin.initialize_app(cred, {
            "databaseURL": "https://proofplus-14b46-default-rtdb.firebaseio.com/"
        })

    for i in range(6236):
        ref = db.reference(f"/verses/{i}")
        ref.set(0)   
        
    return 

def get_highest_verse_counts():
    # gets the verses with the highest counts from the past week
    if not firebase_admin._apps:
        cred = credentials.Certificate("cred.json")
        firebase_admin.initialize_app(cred, {
            "databaseURL": "https://proofplus-14b46-default-rtdb.firebaseio.com/"
        })
   
    ref = db.reference("/verses") 
    data = ref.get()
    results = []
    for _ in range(3):
        label = np.argmax(data)
        data[label] = -1  # verse will not be considered later
        results.append(get_surah_and_verse(label))
    
    
if __name__ == "__main__":    
    get_highest_verse_counts()
    reset_verse_counts()
    


