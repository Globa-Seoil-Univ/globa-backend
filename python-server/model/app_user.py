from util.database import Base
from sqlalchemy import CheckConstraint, Column
from sqlalchemy.sql import func
from sqlalchemy.dialects.mysql import INTEGER, VARCHAR, BOOLEAN, TIMESTAMP


class AppUser(Base):
    __tablename__ = "app_user"
    __table_args__ = (
        CheckConstraint("sns_kind IN ('1001', '1002', '1003', '1004')"),
    )

    user_id = Column(INTEGER(unsigned=True), primary_key=True, autoincrement=True)
    sns_kind = Column(VARCHAR(4), nullable=False)
    sns_id = Column(VARCHAR(100), unique=True, nullable=False)
    name = Column(VARCHAR(20), nullable=False)
    primary_nofi = Column(BOOLEAN, default=False, nullable=False)
    upload_nofi = Column(BOOLEAN, default=False, nullable=False)
    share_nofi = Column(BOOLEAN, default=False, nullable=False)
    event_nofi = Column(BOOLEAN, default=False, nullable=False)
    profile_path = Column(VARCHAR(200))
    profile_size = Column(INTEGER(unsigned=True))
    profile_type = Column(VARCHAR(20))
    notification_token = Column(VARCHAR(300), unique=True)
    notification_token_time = Column(TIMESTAMP)
    created_time = Column(TIMESTAMP, default=func.now())
    deleted = Column(BOOLEAN, default=False)
    deleted_time = Column(TIMESTAMP)

    def __init__(self, sns_id, sns_kind, name, profile_path=None, primary_nofi=True, upload_nofi=True, share_nofi=True,
                 event_nofi=True):
        self.sns_id = sns_id
        self.sns_kind = sns_kind
        self.name = name
        self.primary_nofi = primary_nofi
        self.upload_nofi = upload_nofi
        self.share_nofi = share_nofi
        self.event_nofi = event_nofi
        self.profile_path = profile_path

    @property
    def serialize(self):
        return {
            'user_id': self.user_id,
            'sns_id': self.sns_id,
            'sns_kind': self.sns_kind,
            'name': self.name,
            'primary_nofi': self.primary_nofi,
            'upload_nofi': self.upload_nofi,
            'share_nofi': self.share_nofi,
            'event_nofi': self.event_nofi,
            'profile_path': self.profile_path,
            'profile_type': self.profile_type,
            'profile_size': self.profile_size,
            'notification_token': self.notification_token,
            'notification_token_time': self.notification_token_time,
            'created_time': self.created_time,
            'deleted': self.deleted,
            'deleted_time': self.deleted_time
        }
