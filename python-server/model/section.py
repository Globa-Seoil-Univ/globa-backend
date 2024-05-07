from util.database import Base
from sqlalchemy import Column, ForeignKey
from sqlalchemy.sql import func
from sqlalchemy.dialects.mysql import INTEGER, VARCHAR, TIMESTAMP


class Section(Base):
    __tablename__ = "section"

    section_id = Column(INTEGER(unsigned=True), primary_key=True, autoincrement=True)
    record_id = Column(INTEGER(unsigned=True), ForeignKey("record.record_id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False)
    title = Column(VARCHAR(100), nullable=False)
    start_time = Column(INTEGER(unsigned=True), nullable=False)
    end_time = Column(INTEGER(unsigned=True), nullable=False)
    created_time = Column(TIMESTAMP, default=func.now())

    def __init__(self, section_id, record_id, title, start_time, end_time):
        self.section_id = section_id
        self.record_id = record_id
        self.title = title
        self.start_time = start_time
        self.end_time = end_time

    @property
    def serialize(self):
        return {
            'section_id': self.section_id,
            'record_id': self.record_id,
            'user_id': self.user_id,
            'title': self.title,
            'start_time': self.start_time,
            'end_time': self.end_time,
            'created_time': self.created_time
        }
