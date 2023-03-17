import tensorflow as tf
from tensorflow import keras
import numpy as np
import pydub
from dataclasses import dataclass
import os


@dataclass
class Location:
    chapter: int
    verse: int


# transform mp3 into tensor, with a sample rate of 11025 Hz and 
# a standard deviation of 1
def audio_to_tensor(filename: str) -> tf.Tensor:
    filepath = os.path.join(os.getcwd(), filename)
    audio = pydub.AudioSegment.from_mp3(filepath)
    if audio.sample_width == 2:
        samples = np.frombuffer(audio._data, dtype=np.int16)
    else:
        samples = np.frombuffer(audio._data, dtype=np.int32)
    samples = samples.astype("float32")
    
    # Subsample the audio to 11025 Hz and 1-channel
    length = samples.shape[0]
    downsample = audio.channels * audio.frame_rate
    if downsample < 11025:
        downsample = 11025 // downsample
        samples = np.reshape(samples, (1, -1))
        samples = np.broadcast_to(samples, (downsample, length))
        samples = np.reshape(samples, length * downsample)
    else:
        downsample //= 11025
        if length % downsample != 0:
            samples = np.pad(samples,
                    (0, downsample - length % downsample), "constant")
        samples = np.reshape(samples, (-1, downsample))
        samples = np.average(samples, axis=1)
        
    # make float16 with standard deviation 1
    samples = samples / np.std(samples)
    samples = samples.astype("float16")
    
    # reshape for use by the model
    samples = np.pad(samples, (0, 1000 - samples.shape[0] % 1000), "constant")
    samples = np.reshape(samples, (1, 1000, -1))
    samples = np.transpose(samples, (0, 2, 1))
    return tf.convert_to_tensor(samples)

def get_surah_and_verse(label: int) -> Location:
    data = [0, 7, 293, 493, 669, 789, 954, 1160, 1235, 1364, 1473, 1596, 1707, 1750, 
            1802, 1901, 2029, 2140, 2250, 2348, 2483, 2595, 2673, 2791, 2855, 2932, 
            3159, 3252, 3340, 3409, 3469, 3503, 3533, 3606, 3660, 3705, 3788, 3970, 
            4058, 4133, 4218, 4272, 4325, 4414, 4473, 4510, 4545, 4583, 4612, 4630, 
            4675, 4735, 4784, 4846, 4901, 4979, 5075, 5104, 5126, 5150, 5163, 5177, 
            5188, 5199, 5217, 5229, 5241, 5271, 5323, 5375, 5419, 5447, 5475, 5495, 
            5551, 5591, 5622, 5672, 5712, 5758, 5800, 5829, 5848, 5884, 5909, 5931, 
            5948, 5967, 5993, 6023, 6043, 6058, 6079, 6090, 6098, 6106, 6125, 6130, 
            6138, 6146, 6157, 6168, 6176, 6179, 6188, 6193, 6197, 6204, 6207, 6213, 
            6216, 6221, 6225, 6230, 6236]

    for i in range(114):
        if data[i] <= label and data[i+1] > label:
            surah = i + 1
            verse = label - data[i] + 1
            return Location(int(surah), int(verse))
        
def predict(filename: str):
    model = keras.models.load_model("model_skeletal")
    wave = audio_to_tensor(filename)
    probabilities = model.predict(wave)[0, :]
    results = []
    for _ in range(3):
        label = np.argmax(probabilities)
        probabilities[label] = -1  # verse will not be considered later
        results.append(get_surah_and_verse(label))
    return results
