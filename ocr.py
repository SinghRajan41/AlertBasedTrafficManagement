import easyocr
reader = easyocr.Reader(['en'])
path = input()
#path = "/home/rajan41/Desktop/workspace/Projects/DIP_Project/images/plate4.png"
#'/home/rajan41/Desktop/workspace/Projects/AI_Project/plate3.png'
result = reader.readtext(path)
s = result[0][-2]
print("Detected License Plate = " , s)

