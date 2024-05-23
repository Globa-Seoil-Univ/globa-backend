import logging
import os


class Logger:
    logger = None

    def __init__(self, name):
        self.directory = os.getcwd() + "/log/"
        if not os.path.exists(self.directory):
            os.makedirs(self.directory)

        self.logger = logging.getLogger(name)
        self.logger.setLevel(logging.INFO)
        formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')

        stream_handler = logging.StreamHandler()
        stream_handler.setFormatter(formatter)
        self.logger.addHandler(stream_handler)

        file_handler = logging.FileHandler('./log/{}.log'.format(name))
        file_handler.setFormatter(formatter)
        self.logger.addHandler(file_handler)
