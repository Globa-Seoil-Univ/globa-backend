from util.database import Base
from sqlalchemy import Column, CheckConstraint
from sqlalchemy.sql import func
from sqlalchemy.dialects.mysql import VARCHAR, TIMESTAMP


class FolderRole(Base):
    __tablename__ = "folder_role"
    __table_args__ = (
        CheckConstraint("role_id IN ('1', '2', '3')"),
    )

    role_id = Column(VARCHAR(1), primary_key=True)
    role_name = Column(VARCHAR(3), unique=True, nullable=False)
    created_time = Column(TIMESTAMP, default=func.now())

    def __init__(self, role_id, role_name):
        self.role_id = role_id
        self.role_name = role_name

    @property
    def serialize(self):
        return {
            'role_id': self.role_id,
            'role_name': self.role_name,
            'created_time': self.created_time,
        }
