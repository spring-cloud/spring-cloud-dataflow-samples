import sys
from collections import defaultdict


def get_cmd_arg(name):
    """Extracts argument value by name. (@author: Chris Schaefer)
  
    Assumes the exec (default) spring-cloud-deployer-k8s argument passing mode.
  
    Args:
      name: argument name.
    Returns:
      value of the requested argument.
    """
    d = defaultdict(list)
    for k, v in ((k.lstrip('-'), v) for k, v in (a.split('=') for a in sys.argv[1:])):
        d[k].append(v)

    if bool(d[name]):
        return d[name][0]
    else:
        return None

def get_db_url():
    """Computes sqlalchemy connection URL
  
    Uses the s.d.username, s.d.password and s.d.url properties to compute the sqlalchemy url.
    This provides access to the SCDF internal DB and tables such as TASK_EXECUTION. 

    Returns:
      sqlalchemy compatible URL compatible with the target DB.
    """
    username = get_cmd_arg('spring.datasource.username')
    password = get_cmd_arg('spring.datasource.password')
    jdbc_url = get_cmd_arg('spring.datasource.url')

    return str(jdbc_url) \
        .replace('jdbc:', '') \
        .replace('sqlserver:', 'mssql+pyodbc:') \
        .replace('//', '//{username}:{password}@'.format(username=username, password=password))


def get_task_id():
    """Task ID as handled inside SCDF.
  
    When launching tasks SCDF provides the spring.cloud.task.executionid as command line argument.  

    Returns:
      The task id as handled inside SCDF.
    """
    return get_cmd_arg('spring.cloud.task.executionid')


def get_task_name():
    return get_cmd_arg('spring.cloud.task.name')
