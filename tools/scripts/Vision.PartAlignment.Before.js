var width = part.getPackage().getFootprint().getBodyWidth()
var height = part.getPackage().getFootprint().getBodyHeight()

length = Math.max(width,height)

print("NEODEN UP camera part size:"+length)


var camera = machine.getCameras()[0]

if length < 10:
	camera.width = 512
	camera.height = 512
	camera.shiftX = 256
	camera.shiftY = 256
	print("NEODEN UP camera selected medium")
else:
	camera.width = 1024
	camera.height = 1024
	camera.shiftX = 0
	camera.shiftY = 0
	print("NEODEN UP camera selected big")
	

