import os
import glob
import time
import cv2
import sys
import numpy as np 
from io import BytesIO
#import RPi.GPIO as GPIO
#from google.cloud import storage
import google.cloud.storage as storage
from bluetooth import *
"""
def array_to_bytes(x: np.ndarray) -> bytes:
    np_bytes = BytesIO()
    np.save(np_bytes, x, allow_pickle=True)
    return np_bytes.getvalue()
"""
os.system('sudo modprobe w1-gpio')
#os.system('modprobe w1-therm')

#GPIO.setmode(GPIO.BCM)
#GPIO.setup(17, GPIO.OUT)

base_dir = '~/CollabLens'
#device_folder = glob.glob(base_dir + '28*')[0]
#device_file = device_folder + '/w1_slave'

server_sock=BluetoothSocket(RFCOMM)
server_sock.bind(("",PORT_ANY))
server_sock.listen(1)

port = server_sock.getsockname()[1]

uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"

advertise_service( server_sock, "StreamPiServer",
                   service_id = uuid,
                   service_classes = [ uuid, SERIAL_PORT_CLASS ],
                   profiles = [ SERIAL_PORT_PROFILE ], 
#                   protocols = [ OBEX_UUID ] 
                    )
while True:          
	print("Waiting for connection on RFCOMM channel " + str(port))

	client_sock, client_info = server_sock.accept()
	print("Accepted connection from "+ str(client_info))

	try:
		byte_data = client_sock.recv(1024)
		data = strvalue = byte_data.decode('utf-8')
		
		if len(data) == 0: 
			break
		#print("received "+str(data))
		#print(type(data))
		print(data)

		if data == 'start_record!':
			print("Recording via raspivid!")
			#https://janakiev.com/blog/python-shell-commands/
			#os.system("raspivid -o vid.h264")
		elif data == 'stop_record!':
			print("Recording via raspivid!")
			#https://janakiev.com/blog/python-shell-commands/
			#os.system("raspivid -o vid.h264")
		elif data == 'upload!':
			print("Uploading video to cloud bucket!")
			"""
			#upload command: https://stackoverflow.com/questions/3777301/how-to-call-a-shell-script-from-python-code
			# Setting credentials using the downloaded JSON file
			client = storage.Client.from_service_account_json(json_credentials_path='radiant-pilot-331416-17fec45662a8.json')
			# Creating bucket object
			bucket = client.get_bucket('test')
			# Name of the object to be stored in the bucket
			object_name_in_gcs_bucket = bucket.blob('vid.h264')#date time context inclusion for better management
			# Name of the object in local file system
			object_name_in_gcs_bucket.upload_from_filename('vid.h264')
			"""
		elif data == 'delete!':
			print("Discarding video!")
			#no action, essentially owerwrite/delete video recorded
		elif data == 'start_stream!':
			print("Streaming input video!")
			img = cv2.imread("/home/pi/Downloads/8_1.png", cv2.IMREAD_COLOR)
			#img = [[[18, 18, 18],[122, 122, 122],[27, 27, 27],[15, 15, 15],[52, 52, 52],[34, 34, 34],[90, 90, 90],[50, 50, 50]]]
			#img = img-130
			#cv2.imwrite("/home/pi/Desktop/rawmg.png", img)
			encode_param = [int(cv2.IMWRITE_JPEG_QUALITY), 50]
			result, encimg = cv2.imencode('.jpg', img, encode_param)
			np_bytes = BytesIO()
			np.save(np_bytes, encimg, allow_pickle=True)
			enc_bytes = np_bytes.getvalue()
			#enc_bytes = array_to_bytes(encimg)
			#print(sys.getsizeof(enc_bytes))
			#data = encimg
			#print(sys.getsizeof(len(encimg)))
			#print(sys.getsizeof(encimg))
			#print(encimg.nbytes)
			#print(type(encimg.nbytes))
			#print(len(encimg).bit_length) 
			#data = len(encimg).to_bytes(2, byteorder='big')
			data = img.tobytes()
			#print(encimg.shape)
			#data = encimg
			print(img)
			print(type(data))
			#data = [ 0, 1, 2, 3, 4, 5, 6, 7]
			print(data)
			print(sys.getsizeof(data))
		elif data == 'stop_stream!':
			print("Stopping Stream!")
			#no action, essentially stop stream
			
		else:
			data = 'Incorrect Input!' 
			print(data)
                
		client_sock.send(data)
									
	except IOError:
		pass

	except KeyboardInterrupt:

		print("disconnected")

		client_sock.close()
		server_sock.close()
		print("all done")

		break 