import RPi.GPIO as GPIO
from time import sleep
import Queue

from solenoidserver import SolenoidServer

NUM_SOLENOIDS = 4
NUM_STEPS = 16

ON_PERIOD = 0.05
CNT_CLK_PERIOD = 0.001

CNT_CLK_PIN = 24
CNT_RST_PIN = 25

pins = [17,18,21,22]
pattern = []

step = 0
running = False


def setup():
    GPIO.setmode(GPIO.BCM)
    for i in pins:
        GPIO.setup(i, GPIO.OUT)
        GPIO.output(i, GPIO.HIGH)
        sleep(ON_PERIOD)
        GPIO.output(i, GPIO.LOW)
        sleep(0.4)
    GPIO.setup(CNT_CLK_PIN, GPIO.OUT)
    GPIO.output(CNT_CLK_PIN, GPIO.LOW)
    GPIO.setup(CNT_RST_PIN, GPIO.OUT)
    global pattern
    for i in range(NUM_SOLENOIDS):
        pattern += [ [0] * NUM_STEPS ]


def counter_reset():
    GPIO.output(CNT_RST_PIN, GPIO.HIGH)
    sleep(CNT_CLK_PERIOD)
    GPIO.output(CNT_RST_PIN, GPIO.LOW)


def counter_count():
    GPIO.output(CNT_CLK_PIN, GPIO.HIGH)
    sleep(CNT_CLK_PERIOD)
    GPIO.output(CNT_CLK_PIN, GPIO.LOW)


def activate(vals):
    for i in range(NUM_SOLENOIDS):
        if vals[i] == 1:
            GPIO.output(pins[i], GPIO.HIGH)
        else:
            GPIO.output(pins[i], GPIO.LOW)
    sleep(ON_PERIOD)
    for i in range(NUM_SOLENOIDS):
        GPIO.output(pins[i], GPIO.LOW)


setup()
counter_reset()

commandQueue = Queue.Queue()
responseQueue = Queue.Queue()
solenoidserver = SolenoidServer(NUM_SOLENOIDS, NUM_STEPS, commandQueue, responseQueue)
solenoidserver.start()

while(True):

        if running:
            if step >= NUM_STEPS:
                step = 0
                counter_reset()
            counter_count()
            vals = []
            for sol in range(NUM_SOLENOIDS):
                vals += [pattern[sol][step]]
            activate(vals)
            step += 1

        try:
            command = commandQueue.get(False)
        except Queue.Empty:
            command = ('',)

        if command[0] == 'GET':
            # Return values of all solenoid steps
            first = True
            valstr = ''
            for jj in range(NUM_SOLENOIDS):
                for ii in range(NUM_STEPS):
                    if not first:
                        valstr += ','
                    valstr += str(pattern[jj][ii])
                    first = False
            responseQueue.put(valstr)
        elif command[0] == 'SET':
            # Set single solenoid / step value
            solenoidNum = command[1] - 1
            stepNum = command[2] - 1
            pattern[solenoidNum][stepNum] = command[3]
            responseQueue.put(True)
        elif command[0] == 'START':
            # Start solenoid stepping
            running = True
        elif command[0] == 'STOP':
            # Stop solenoid stepping
            running = False
            step = 0
            counter_reset()

        sleep(0.5)

GPIO.cleanup()
