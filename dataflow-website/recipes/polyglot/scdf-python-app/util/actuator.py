import sys
import threading

from flask import Flask, Response


class Actuator:
    """Actuator is used to expose operational information about the running application, such as `health/liveliness`,
    `info`, `env`, etc. It uses HTTP endpoints to enable us to interact with it.

    The `/actuator/health` and `/actuator/info` handles the Kubernetes liveness and readiness probes requests.
    Kubernetes expects HTTP 200 status code to consider the application live and ready.

     :param port: the HTTP port used by the Actuator.
     :param info_content: The text response to be returned by the /actuator/info endpoint.
     """

    def __init__(self, port=8080, info_content='Info'):
        self.http_app = Actuator.__create_http_app(info_content)
        self.port = port
        print(info_content)
        sys.stdout.flush()

    def __run(self):
        self.http_app.run(port=self.port, host='0.0.0.0')

    @staticmethod
    def __create_http_app(info_description):
        app = Flask(__name__)
        app.debug = False
        app.use_reloader = False

        @app.route('/actuator/health')
        def health():
            return Response('Alive', status=200)

        @app.route('/actuator/info')
        def info():
            return Response(info_description, status=200, content_type='text/plain')

        return app

    @staticmethod
    def start(port=8080, info='Info'):
        """Starts the `Actuator` in a separate thread.

        :param port: the HTTP port used by the Actuator. Defaults to 8080.
        :param info: The text response to be returned by the /actuator/info endpoint.
        """
        try:
            thread = threading.Thread(target=Actuator(port, info).__run)
            thread.setDaemon(True)
            thread.start()
            print('Actuator started!')
        except KeyboardInterrupt:
            sys.exit(0)
