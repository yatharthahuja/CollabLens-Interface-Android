import os
import cv2

#reading image
img = cv2.imread('input.jpeg')

#encoding
encode_param = [int(cv2.IMWRITE_JPEG_QUALITY), 50]
result, encimg = cv2.imencode('jpeg', img, encode_param)

#converting array to bytes


#obtaining array from bytes
ret_encimg

#decoding image from array
decimg = cv2.imdecode(ret_encimg, cv2.IMREAD_COLOR)
cv2.imwrite("final.jpeg", decimg)