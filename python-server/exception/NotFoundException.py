class NotFoundException(Exception):
    message = ""
    status_code = 404

    def __init__(self, message):
        super().__init__(self)
        self.message = message
