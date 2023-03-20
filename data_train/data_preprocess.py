# Preprocess the audio file so that:
# 1. Data is downcasted from 32-bit floats to 16-bit ones;
# 2. Tracks are merged into one;
# 3. Subsampling all files down to 11025 Hz (the minimum in the downloaded reccordings);
# 4. Pad the recordings less than 5 seconds with 0.
import glob
import pydub
import numpy as np
import os


def to_3_letters(orig):
    upper = orig[9:].upper().split(' ')
    if len(upper) == 1:
        return upper[0][0:3]
    if len(upper) == 2:
        if upper[0] == 'ABDULLAH':
            return 'ABB'
        return upper[0][0] + upper[1][0:2]
    return ''.join(word[0] for word in upper[0:3])


reciters = glob.glob('data_raw/*')
#reciters = ['data_raw/Ahmed ibn Ali al-Ajamy']
for name_reciter in reciters:
    print('Processing folder %s...' % name_reciter)
    name_id = to_3_letters(name_reciter)
    for filename in glob.glob(name_reciter + '/*.mp3'):
        new_dir = 'data/' + filename[-10:-4] + name_id + '.raw'
        if (os.path.isfile(new_dir)):
            continue
        print('\tProcessing file %s...' % filename)
        sound_file = pydub.AudioSegment.from_mp3(filename)
        if sound_file.sample_width == 2:
            samples = np.frombuffer(sound_file._data, dtype=np.int16)
        else:
            samples = np.frombuffer(sound_file._data, dtype=np.int32)
        samples = samples.astype('float32')

        # Subsample the audio to 11025 Hz and 1-channel
        length = samples.shape[0]
        downsample = sound_file.channels * sound_file.frame_rate
        if downsample < 11025:  # Just in case there is a lower one
            downsample = 11025 // downsample
            samples = np.reshape(samples, (1, -1))
            samples = np.broadcast_to(samples, (downsample, length))
            samples = np.reshape(samples, length * downsample)
        else:
            downsample //= 11025
            if length % downsample != 0:
                samples = np.pad(samples,
                        (0, downsample - length % downsample), 'constant')
            samples = np.reshape(samples, (-1, downsample))
            samples = np.average(samples, axis=1)

        # Unify the data to float16 with standard deviation 1
        samples = samples / np.std(samples)
        samples = samples.astype('float16')

        np.save(new_dir, samples)
        samples.tofile(new_dir)
