from util.database import Base
from sqlalchemy import Column, ForeignKey, CheckConstraint
from sqlalchemy.sql import func
from sqlalchemy.dialects.mysql import INTEGER, VARCHAR, TIMESTAMP


class Highlight(Base):
    __tablename__ = "highlight"
    __table_args__ = (
        CheckConstraint("type IN ('1', '2')"),
    )

    highlight_id = Column(INTEGER(unsigned=True), primary_key=True, autoincrement=True)
    section_id = Column(INTEGER(unsigned=True), ForeignKey("section.section_id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False)
    start_index = Column(INTEGER(unsigned=True), nullable=False)
    end_index = Column(INTEGER(unsigned=True), nullable=False)
    type = Column(VARCHAR(1), default="1")
    created_time = Column(TIMESTAMP, default=func.now())

    def __init__(self, highlight_id, section_id, start_index, end_index, type):
        self.highlight_id = highlight_id
        self.section_id = section_id
        self.start_index = start_index
        self.end_index = end_index
        self.type = type

    @property
    def serialize(self):
        return {
            'highlight_id': self.highlight_id,
            'section_id': self.section_id,
            'start_index': self.start_index,
            'end_index': self.end_index,
            'type': self.type,
            'created_time': self.created_time
        }
