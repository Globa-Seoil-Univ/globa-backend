from util.database import Base
from sqlalchemy import Column, ForeignKey
from sqlalchemy.sql import func
from sqlalchemy.dialects.mysql import INTEGER, VARCHAR, TIMESTAMP


class Folder(Base):
    __tablename__ = "folder"

    folder_id = Column(INTEGER(unsigned=True), primary_key=True, autoincrement=True)
    user_id = Column(INTEGER(unsigned=True), ForeignKey("user.user_id", ondelete="CASCADE", onupdate="CASCADE"))
    title = Column(VARCHAR(32), nullable=False)
    created_time = Column(TIMESTAMP, default=func.now())

    def __init__(self, folder_id, user_id, title):
        self.folder_id = folder_id
        self.user_id = user_id
        self.title = title

    @property
    def serialize(self):
        return {
            'folder_id': self.folder_id,
            'user_id': self.user_id,
            'title': self.title,
            'created_time': self.created_time,
        }
