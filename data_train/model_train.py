import numpy as np
import tensorflow as tf
from tensorflow import keras


#load_train_data
def get_label(file_path):
    parts = tf.strings.split(file_path, '/')
    label = parts[-2]
    label = tf.strings.to_number(label, tf.int32)
    return tf.cast(label, tf.uint16)


def process_item(file_path):
    label = get_label(file_path)
    audio = tf.io.read_file(file_path)
    audio = tf.io.decode_raw(audio, tf.float16)
    # make it a multiple of 1000
    audio = tf.pad(audio, tf.constant([[0,1000]]))
    length = tf.shape(audio)[0] // 1000
    audio = audio[:length * 1000]
    audio = tf.reshape(audio, [1000, length])
    audio = tf.transpose(audio)
    #audio = tf.expand_dims(audio, axis=1)
    return audio, label


def load_dataset(path):
    dataset = tf.data.Dataset.list_files(path + '/*/*', shuffle=True)
    dataset = dataset.map(process_item)
    return dataset


initializer = tf.keras.initializers.RandomNormal(mean=0., stddev=1.)

data_train = load_dataset('data_skeletal/train')
print('\n\nData loading finished.\n')
'''
Model structure:
    1. Input (Audio recording converted into half-precision float arrays)
    2. LSTM layer (Output dimension 128)
    3. Dense layers (Output dimension 64, 16)
    4. Output layers (6236 classes)
'''
'''
model = keras.Sequential()
model.add(keras.Input(shape=(None, 1000)))
model.add(keras.layers.Dense(32, activation='relu',
    kernel_initializer=initializer))
model.add(keras.layers.LSTM(128, kernel_initializer=initializer))
model.add(keras.layers.Dense(64, activation='relu',
    kernel_initializer=initializer))
model.add(keras.layers.Dense(32, activation='relu',
    kernel_initializer=initializer))
model.add(keras.layers.Dense(6236, activation='softmax',
    kernel_initializer=initializer))
'''
model = keras.models.load_model('model_skeletal_3')
model.compile(optimizer=keras.optimizers.Adam(learning_rate=1e-5),
        loss='sparse_categorical_crossentropy', metrics=['accuracy'])
model.fit(data_train.batch(1), epochs=8)
model.save('model_skeletal_4')
