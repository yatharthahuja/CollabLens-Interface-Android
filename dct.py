import cv2
#import sys
import numpy as np 
from io import BytesIO

def array_to_bytes(x: np.ndarray) -> bytes:
    np_bytes = BytesIO()
    np.save(np_bytes, x, allow_pickle=True)
    return np_bytes.getvalue()


def bytes_to_array(b: bytes) -> np.ndarray:
    np_bytes = BytesIO(b)
    return np.load(np_bytes, allow_pickle=True)

if __name__ == "__main__":
	#camera = cv2.VideoCapture(0)

	#at sending end
	#return_value, img = camera.read()
	img = cv2.imread("/home/pi/Desktop/lena.png", cv2.IMREAD_COLOR)
	cv2.imwrite("/home/pi/Desktop/rawmg.png", img)
	print(type(img))
	encode_param = [int(cv2.IMWRITE_JPEG_QUALITY), 50]
	result, encimg = cv2.imencode('.jpg', img, encode_param)
	print(type(encimg))
	#to be transmitted
	enc_bytes = array_to_bytes(encimg)
	#print(sys.getsizeof(enc_bytes))

	#at receiving end
	ret_encimg = bytes_to_array(enc_bytes)

	decimg = cv2.imdecode(ret_encimg, cv2.IMREAD_COLOR)
	cv2.imwrite("/home/pi/Desktop/decimg.png", decimg)

	#print(img.shape)
	#print(decimg.shape)
	#print(type(decimg))
	#print(np.array_equal(img,decimg))
	#print(type(encimg))
	#print(encimg.nbytes)