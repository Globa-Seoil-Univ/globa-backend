from util.database import Base
from sqlalchemy import Column, ForeignKey, CheckConstraint
from sqlalchemy.sql import func
from sqlalchemy.dialects.mysql import INTEGER, VARCHAR, TIMESTAMP


class FolderShare(Base):
    __tablename__ = "folder_share"
    __table_args__ = (
        CheckConstraint("invitation_status IN ('PENDING', 'ACCEPT')"),
    )

    share_id = Column(INTEGER(unsigned=True), primary_key=True, autoincrement=True)
    folder_id = Column(INTEGER(unsigned=True), ForeignKey("folder.folder_id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False)
    owner_id = Column(INTEGER(unsigned=True), ForeignKey("user.user_id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False)
    target_id = Column(INTEGER(unsigned=True), ForeignKey("user.user_id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False)
    role_id = Column(VARCHAR(1), ForeignKey("folder_role.role_id", ondelete="CASCADE", onupdate="CASCADE"))
    invitation_status = Column(VARCHAR(7), default="PENDING")
    invitation_time = Column(TIMESTAMP, default=func.now())
    created_time = Column(TIMESTAMP, default=func.now())

    def __init__(self, share_id, folder_id, owner_id, target_id, role_id, invitation_status="PENDING"):
        self.share_id = share_id
        self.folder_id = folder_id
        self.owner_id = owner_id
        self.target_id = target_id
        self.role_id = role_id
        self.invitation_status = invitation_status

        if self.invitation_status == "ACCEPT":
            self.invitation_time = func.now()
        else:
            self.invitation_time = None

    @property
    def serialize(self):
        return {
            'share_id': self.share_id,
            'folder_id': self.folder_id,
            'owner_id': self.owner_id,
            'target_id': self.target_id,
            'role_id': self.role_id,
            'invitation_status': self.invitation_status,
            'invitation_time': self.invitation_time,
            'created_time': self.created_time
        }
