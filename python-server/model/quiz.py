from util.database import Base
from sqlalchemy import Column, ForeignKey, DECIMAL, Text, BOOLEAN
from sqlalchemy.sql import func
from sqlalchemy.dialects.mysql import INTEGER, VARCHAR, TIMESTAMP


class Quiz(Base):
    __tablename__ = "quiz"

    quiz_id = Column(INTEGER(unsigned=True), primary_key=True, autoincrement=True)
    record_id = Column(INTEGER(unsigned=True), ForeignKey("record.record_id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False)
    question = Column(Text, nullable=False)
    answer = Column(BOOLEAN, nullable=False)
    created_time = Column(TIMESTAMP, default=func.now())

    def __init__(self, record_id, question, answer):
        self.record_id = record_id
        self.question = question
        self.answer = answer

    @property
    def serialize(self):
        return {
            'quiz_id': self.quiz_id,
            'record_id': self.record_id,
            'question': self.question,
            'answer': self.answer,
            'created_time': self.created_time
        }
