import sys
import time

from util.task_status import TaskStatus
from util.task_args import get_task_id, get_db_url, get_task_name, get_cmd_arg

try:
    print('sys arguments {}'.format(sys.argv))

    status = TaskStatus(get_task_id(), get_db_url())

    # Set task status to RUNNING
    status.running()

    # Do something
    print('Start task:{}, id:{}, sqlalchemy-url:{}'.format(get_task_name(), get_task_id(), get_db_url()))
    print('Wait for 60 seconds ...')
    sys.stdout.flush()
    time.sleep(60)

    # if you add --error.message=Bla to the launch properties, an exception is thrown
    if get_cmd_arg('error.message') is not None:
        raise Exception(get_cmd_arg('error.message'))

    print("Goodbye!")

    # Mark task completion status change
    status.completed()

except Exception as exp:
    error_message = 'Task failed: {}'.format(exp)
    status.failed(1, error_message, error_message)
    print(error_message)


