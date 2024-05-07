from util.database import Base
from sqlalchemy import Column, ForeignKey, DECIMAL
from sqlalchemy.sql import func
from sqlalchemy.dialects.mysql import INTEGER, VARCHAR, TIMESTAMP


class Keyword(Base):
    __tablename__ = "keyword"

    keyword_id = Column(INTEGER(unsigned=True), primary_key=True, autoincrement=True)
    record_id = Column(INTEGER(unsigned=True), ForeignKey("record.record_id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False)
    word = Column(VARCHAR(30), nullable=False)
    importance = Column(DECIMAL(5, 4), nullable=False)
    created_time = Column(TIMESTAMP, default=func.now())

    def __init__(self, keyword_id, record_id, word, importance):
        self.keyword_id = keyword_id
        self.record_id = record_id
        self.word = word
        self.importance = importance

    @property
    def serialize(self):
        return {
            'keyword_id': self.keyword_id,
            'record_id': self.record_id,
            'word': self.word,
            'importance': self.importance,
            'created_time': self.created_time
        }
