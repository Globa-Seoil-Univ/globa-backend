from util.database import Base
from sqlalchemy import Column, ForeignKey
from sqlalchemy.sql import func
from sqlalchemy.dialects.mysql import INTEGER, VARCHAR, TIMESTAMP


class Record(Base):
    __tablename__ = "record"

    record_id = Column(INTEGER(unsigned=True), name="record_id", primary_key=True, autoincrement=True)
    folder_id = Column(INTEGER(unsigned=True), ForeignKey("folder.folder_id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False)
    user_id = Column(INTEGER(unsigned=True), ForeignKey("user.user_id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False)
    title = Column(VARCHAR(32), nullable=False)
    path = Column(VARCHAR(300), nullable=False)
    size = Column(VARCHAR(100), nullable=False)
    created_time = Column(TIMESTAMP, default=func.now())

    def __init__(self, record_id, folder_id, user_id, title, path, size):
        self.record_id = record_id
        self.folder_id = folder_id
        self.user_id = user_id
        self.title = title
        self.path = path
        self.size = size

    @property
    def serialize(self):
        return {
            'record_id': self.record_id,
            'folder_id': self.folder_id,
            'user_id': self.user_id,
            'title': self.title,
            'path': self.path,
            'size': self.size,
            'created_time': self.created_time
        }
