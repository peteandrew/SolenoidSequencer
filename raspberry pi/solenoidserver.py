import threading
import socket

PORT = 5320

class SolenoidServer(threading.Thread):

    def __init__(self, numSolenoids, numSteps, commandQueue, responseQueue):
        threading.Thread.__init__(self)
        self.numSolenoids = numSolenoids
        self.numSteps = numSteps
        self.commandQueue = commandQueue
        self.responseQueue = responseQueue

    def run(self):
        serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        serversocket.bind(('', PORT))
        serversocket.listen(1)
        while True:
            (clientsocket, address) = serversocket.accept()
            ClientThread(clientsocket, address, self.numSolenoids, self.numSteps, self.commandQueue, self.responseQueue).start()


class ClientThread(threading.Thread):

    def __init__(self, clientSocket, address, numSolenoids, numSteps, commandQueue, responseQueue):
        threading.Thread.__init__(self)
        self.clientSocket = clientSocket
        self.address = address
        self.numSolenoids = numSolenoids
        self.numSteps = numSteps
        self.commandQueue = commandQueue
        self.responseQueue = responseQueue


    def run(self):
        print "Received connection:", self.address
        self.clientSocket.send(str(self.numSolenoids) + ',' + str(self.numSteps) + '\n')

	exit = False

	while not exit:

                inputvals = self.clientSocket.recv(1024).strip().split(' ')

        	if inputvals[0] == 'GET':
	            # Return values of all solenoid steps
	            self.commandQueue.put((inputvals[0],))
        	    response = self.responseQueue.get(True, None)
	            self.clientSocket.send(response + '\n')
	        elif inputvals[0] == 'SET':
	            # Set single solenoid / step value
	            if len(inputvals) <> 4:
	                self.clientSocket.send('ERROR: incorrect number of arguments\n')
	            elif int(inputvals[1]) < 1 or int(inputvals[1]) > self.numSolenoids:
	                self.clientSocket.send('ERROR: invalid solenoid number\n')
	            elif int(inputvals[2]) < 1 or int(inputvals[2]) > self.numSteps:
	                self.clientSocket.send('ERROR: invalid step number\n')
	            elif int(inputvals[3]) not in [0,1]:
	                self.clientSocket.send('ERROR: invalid value\n')
	            else:
	                self.commandQueue.put((inputvals[0], int(inputvals[1]), int(inputvals[2]), int(inputvals[3])))
	                response = self.responseQueue.get(True, None)
	                if not response:
	                    self.clientSocket.send('ERROR: setting value\n')
	                else:
	                    self.clientSocket.send('OK\n')
	        elif inputvals[0] == 'START':
	        	# Start solenoid stepping
	        	self.commandQueue.put((inputvals[0],))
	        elif inputvals[0] == 'STOP':
	        	# Stop solenoid stepping
	            	self.commandQueue.put((inputvals[0],))
		elif inputvals[0] == 'EXIT':
			exit = True
	        else:
	        	self.clientSocket.send('ERROR: unknown command\n')

	self.clientSocket.close()
