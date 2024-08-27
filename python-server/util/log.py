import logging
import os


class Logger:
    def __init__(self, name):
        self.directory = os.getcwd() + "/log/"
        if not os.path.exists(self.directory):
            os.makedirs(self.directory)

        self.logger = logging.getLogger(name)
        if not self.logger.hasHandlers():
            self.logger.setLevel(logging.INFO)
            formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')

            # Stream handler
            stream_handler = logging.StreamHandler()
            stream_handler.setFormatter(formatter)
            self.logger.addHandler(stream_handler)

            # File handler
            file_handler = logging.FileHandler(f'{self.directory}/{name}.log')
            file_handler.setFormatter(formatter)
            self.logger.addHandler(file_handler)
