#
import time

from util.actuator import Actuator
from util.arguments import  get_env_info

Actuator.start(9191, get_env_info())
#
print('OK')
time.sleep(10000)
print('STOPPED')