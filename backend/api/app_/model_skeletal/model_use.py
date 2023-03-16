# A snippet code used as a reference.

import tensorflow as tf
from tensorflow import keras
import numpy as np
import pydub


# Get the wave from file as a tensor, with sample rate unified to 22050 Hz and standard deviation unified to 1.
def wave_as_tensor(file_name):
    sound_file = pydub.AudioSegment.from_mp3(file_name)
    if sound_file.sample_width == 2:
        samples = np.frombuffer(sound_file._data, dtype=np.int16)
    else:
        samples = np.frombuffer(sound_file._data, dtype=np.int32)
    samples = samples.astype('float32')

    # Subsample the audio to 22050 Hz and 1-channel
    length = samples.shape[0]
    downsample = sound_file.channels * sound_file.frame_rate
    if downsample < 22050:
        downsample = 22050 // downsample
        samples = np.reshape(samples, (1, -1))
        samples = np.broadcast_to(samples, (downsample, length))
        samples = np.reshape(samples, length * downsample)
    else:
        downsample //= 22050
        if length % downsample != 0:
            samples = np.pad(samples,
                    (0, downsample - length % downsample), 'constant')
        samples = np.reshape(samples, (-1, downsample))
        samples = np.average(samples, axis=1)

    # Unify the data to float16 with standard deviation 1
    samples = samples / np.std(samples)
    samples = samples.astype('float16')

    # Reshape the data to be used by the model
    samples = np.pad(samples, (0, 1000 - samples.shape[0] % 1000), 'constant')
    samples = np.reshape(samples, (1, 1000, -1))
    samples = np.transpose(samples, (0, 2, 1))
    return tf.convert_to_tensor(samples)


data = [0, 7, 293, 493, 669, 789, 954, 1160, 1235, 1364, 1473, 1596, 1707, 1750, 1802, 1901, 2029, 2140, 2250, 2348, 2483, 2595, 2673, 2791, 2855, 2932, 3159, 3252, 3340, 3409, 3469, 3503, 3533, 3606, 3660, 3705, 3788, 3970, 4058, 4133, 4218, 4272, 4325, 4414, 4473, 4510, 4545, 4583, 4612, 4630, 4675, 4735, 4784, 4846, 4901, 4979, 5075, 5104, 5126, 5150, 5163, 5177, 5188, 5199, 5217, 5229, 5241, 5271, 5323, 5375, 5419, 5447, 5475, 5495, 5551, 5591, 5622, 5672, 5712, 5758, 5800, 5829, 5848, 5884, 5909, 5931, 5948, 5967, 5993, 6023, 6043, 6058, 6079, 6090, 6098, 6106, 6125, 6130, 6138, 6146, 6157, 6168, 6176, 6179, 6188, 6193, 6197, 6204, 6207, 6213, 6216, 6221, 6225, 6230, 6236]

def get_surah_and_verse(label):
    for i in range(114):
        if data[i] <= label and data[i+1] > label:
            surah = i + 1
            verse = label - data[i] + 1
            return (surah, verse)


# Predict the three most probable surah and verse from recording.
def predict_surah_and_verse(file_name):
    model = keras.models.load_model('model_skeletal')
    wave = wave_as_tensor(file_name)
    probabilities = model.predict(wave)[0, :]
    results = []
    for i in range(3):
        label = np.argmax(probabilities)
        probabilities[label] = -1  # Will not be considered later
        results.append(get_surah_and_verse(label))
    return results

print(predict_surah_and_verse('data_raw/Alafasy/001001.mp3'))
