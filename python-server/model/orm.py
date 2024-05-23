from typing import List, Optional

from sqlalchemy.orm import relationship, Mapped, mapped_column

from util.database import Base
from sqlalchemy import CheckConstraint, event, ForeignKey, DECIMAL, Text
from sqlalchemy.sql import func
from sqlalchemy.dialects.mysql import INTEGER, VARCHAR, BOOLEAN, TIMESTAMP


class AppUser(Base):
    __tablename__ = "app_user"
    __table_args__ = (
        CheckConstraint("sns_kind IN ('1001', '1002', '1003', '1004')"),
    )

    user_id: Mapped[int] = mapped_column(INTEGER(unsigned=True), primary_key=True, autoincrement=True)
    sns_kind: Mapped[str] = mapped_column(VARCHAR(4), nullable=False)
    sns_id: Mapped[str] = mapped_column(VARCHAR(100), unique=True, nullable=False)
    name: Mapped[str] = mapped_column(VARCHAR(20), nullable=False)
    primary_nofi: Mapped[Optional[bool]] = mapped_column(BOOLEAN, default=True, nullable=False)
    upload_nofi: Mapped[Optional[bool]] = mapped_column(BOOLEAN, default=True, nullable=False)
    share_nofi: Mapped[Optional[bool]] = mapped_column(BOOLEAN, default=True, nullable=False)
    event_nofi: Mapped[Optional[bool]] = mapped_column(BOOLEAN, default=True, nullable=False)
    profile_path: Mapped[Optional[str]] = mapped_column(VARCHAR(200))
    profile_size: Mapped[Optional[int]] = mapped_column(INTEGER(unsigned=True))
    profile_type: Mapped[Optional[str]] = mapped_column(VARCHAR(20))
    notification_token: Mapped[Optional[str]] = mapped_column(VARCHAR(300), unique=True)
    notification_token_time: Mapped[Optional[str]] = mapped_column(TIMESTAMP)
    created_time: Mapped[Optional[str]] = mapped_column(TIMESTAMP, default=func.now())
    deleted: Mapped[Optional[bool]] = mapped_column(BOOLEAN, default=False)
    deleted_time: Mapped[Optional[str]] = mapped_column(TIMESTAMP)

    folders: Mapped[List["Folder"]] = relationship(back_populates="user", cascade="all, delete-orphan")
    records: Mapped[List["Record"]] = relationship(back_populates="user", cascade="all, delete-orphan")

    folder_share_owners: Mapped[List["FolderShare"]] = relationship(
        back_populates="owner",
        foreign_keys="[FolderShare.owner_id]",
        cascade="all, delete-orphan"
    )
    folder_share_targets: Mapped[List["FolderShare"]] = relationship(
        back_populates="target",
        foreign_keys="[FolderShare.target_id]",
        cascade="all, delete-orphan"
    )

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


class Folder(Base):
    __tablename__ = "folder"

    folder_id: Mapped[int] = mapped_column(INTEGER(unsigned=True), primary_key=True, autoincrement=True)
    user_id: Mapped[Optional[int]] = mapped_column(INTEGER(unsigned=True), ForeignKey("app_user.user_id", ondelete="CASCADE", onupdate="CASCADE"))
    title: Mapped[str] = mapped_column(VARCHAR(32), nullable=False)
    created_time: Mapped[Optional[str]] = mapped_column(TIMESTAMP, default=func.now())

    user: Mapped["AppUser"] = relationship(back_populates="folders")

    records: Mapped[List["Record"]] = relationship(back_populates="folder", cascade="all, delete-orphan")
    folder_shares: Mapped[List["FolderShare"]] = relationship(back_populates="folder", cascade="all, delete-orphan")

    @property
    def serialize(self):
        return {
            'folder_id': self.folder_id,
            'user_id': self.user_id,
            'title': self.title,
            'created_time': self.created_time,
        }


class FolderRole(Base):
    __tablename__ = "folder_role"
    __table_args__ = (
        CheckConstraint("role_id IN ('1', '2', '3')"),
    )

    role_id: Mapped[int] = mapped_column(VARCHAR(1), primary_key=True)
    role_name: Mapped[str] = mapped_column(VARCHAR(3), unique=True, nullable=False)
    created_time: Mapped[Optional[str]] = mapped_column(TIMESTAMP, default=func.now())

    folder_shares: Mapped[List["FolderShare"]] = relationship(back_populates="folder_role")

    @property
    def serialize(self):
        return {
            'role_id': self.role_id,
            'role_name': self.role_name,
            'created_time': self.created_time,
        }


class FolderShare(Base):
    __tablename__ = "folder_share"
    __table_args__ = (
        CheckConstraint("invitation_status IN ('PENDING', 'ACCEPT')"),
    )

    share_id: Mapped[int] = mapped_column(INTEGER(unsigned=True), primary_key=True, autoincrement=True)
    folder_id: Mapped[int] = mapped_column(INTEGER(unsigned=True), ForeignKey("folder.folder_id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False)
    owner_id: Mapped[int] = mapped_column(INTEGER(unsigned=True), ForeignKey("app_user.user_id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False)
    target_id: Mapped[int] = mapped_column(INTEGER(unsigned=True), ForeignKey("app_user.user_id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False)
    role_id: Mapped[Optional[int]] = mapped_column(VARCHAR(1), ForeignKey("folder_role.role_id", ondelete="SET NULL", onupdate="CASCADE"))
    invitation_status: Mapped[Optional[str]] = mapped_column(VARCHAR(7), default="PENDING")
    invitation_time: Mapped[Optional[str]] = mapped_column(TIMESTAMP, default=func.now())
    created_time: Mapped[Optional[str]] = mapped_column(TIMESTAMP, default=func.now())

    owner: Mapped["AppUser"] = relationship(
        back_populates="folder_share_owners",
        foreign_keys="[FolderShare.owner_id]",
        cascade="all, delete-orphan",
        single_parent=True
    )
    target: Mapped["AppUser"] = relationship(
        back_populates="folder_share_targets",
        foreign_keys="[FolderShare.target_id]",
        cascade="all, delete-orphan",
        single_parent=True
    )
    folder: Mapped["Folder"] = relationship(back_populates="folder_shares")
    folder_role: Mapped["FolderRole"] = relationship(back_populates="folder_shares")

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


@event.listens_for(FolderShare, "before_insert")
def before_insert(mapper, connection):
    if mapper.invitation_status == "ACCEPT":
        mapper.invitation_time = func.now()
    else:
        mapper.invitation_time = None


class Highlight(Base):
    __tablename__ = "highlight"
    __table_args__ = (
        CheckConstraint("type IN ('1', '2')"),
    )

    highlight_id: Mapped[int] = mapped_column(INTEGER(unsigned=True), primary_key=True, autoincrement=True)
    section_id = mapped_column(INTEGER(unsigned=True), ForeignKey("section.section_id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False)
    start_index: Mapped[int] = mapped_column(INTEGER(unsigned=True), nullable=False)
    end_index: Mapped[int] = mapped_column(INTEGER(unsigned=True), nullable=False)
    type: Mapped[Optional[str]] = mapped_column(VARCHAR(1), default="1")
    created_time: Mapped[Optional[str]] = mapped_column(TIMESTAMP, default=func.now())

    section: Mapped["Section"] = relationship(back_populates="highlights")

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


class Keyword(Base):
    __tablename__ = "keyword"

    keyword_id: Mapped[int] = mapped_column(INTEGER(unsigned=True), primary_key=True, autoincrement=True)
    record_id: Mapped[int] = mapped_column(INTEGER(unsigned=True), ForeignKey("record.record_id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False)
    word: Mapped[str] = mapped_column(VARCHAR(30), nullable=False)
    importance: Mapped[float] = mapped_column(DECIMAL(5, 4), nullable=False)
    created_time: Mapped[Optional[str]] = mapped_column(TIMESTAMP, default=func.now())

    record: Mapped["Record"] = relationship(back_populates="keywords")

    @property
    def serialize(self):
        return {
            'keyword_id': self.keyword_id,
            'record_id': self.record_id,
            'word': self.word,
            'importance': self.importance,
            'created_time': self.created_time
        }


class Quiz(Base):
    __tablename__ = "quiz"

    quiz_id: Mapped[int] = mapped_column(INTEGER(unsigned=True), primary_key=True, autoincrement=True)
    record_id: Mapped[int] = mapped_column(INTEGER(unsigned=True), ForeignKey("record.record_id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False)
    question: Mapped[str] = mapped_column(Text, nullable=False)
    answer: Mapped[bool] = mapped_column(BOOLEAN, nullable=False)
    created_time: Mapped[Optional[str]] = mapped_column(TIMESTAMP, default=func.now())

    record: Mapped["Record"] = relationship(back_populates="quizs")

    @property
    def serialize(self):
        return {
            'quiz_id': self.quiz_id,
            'record_id': self.record_id,
            'question': self.question,
            'answer': self.answer,
            'created_time': self.created_time
        }


class Record(Base):
    __tablename__ = "record"

    record_id: Mapped[int] = mapped_column(INTEGER(unsigned=True), name="record_id", primary_key=True, autoincrement=True)
    folder_id: Mapped[int] = mapped_column(INTEGER(unsigned=True), ForeignKey("folder.folder_id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False)
    user_id: Mapped[int] = mapped_column(INTEGER(unsigned=True), ForeignKey("app_user.user_id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False)
    title: Mapped[str] = mapped_column(VARCHAR(32), nullable=False)
    path: Mapped[str] = mapped_column(VARCHAR(300), nullable=False)
    size: Mapped[str] = mapped_column(VARCHAR(100), nullable=False)
    created_time: Mapped[Optional[str]] = mapped_column(TIMESTAMP, default=func.now())

    user: Mapped["AppUser"] = relationship(back_populates="records")
    folder: Mapped["Folder"] = relationship(back_populates="records")

    keywords: Mapped[List["Keyword"]] = relationship(back_populates="record", cascade="all, delete-orphan")
    quizs: Mapped[List["Quiz"]] = relationship(back_populates="record", cascade="all, delete-orphan")
    sections: Mapped[List["Section"]] = relationship(back_populates="record", cascade="all, delete-orphan")

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


class Section(Base):
    __tablename__ = "section"

    section_id: Mapped[int] = mapped_column(INTEGER(unsigned=True), primary_key=True, autoincrement=True)
    record_id: Mapped[int] = mapped_column(INTEGER(unsigned=True), ForeignKey("record.record_id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False)
    title: Mapped[str] = mapped_column(VARCHAR(100), nullable=False)
    start_time: Mapped[int] = mapped_column(INTEGER(unsigned=True), nullable=False)
    end_time: Mapped[int] = mapped_column(INTEGER(unsigned=True), nullable=False)
    created_time: Mapped[Optional[str]] = mapped_column(TIMESTAMP, default=func.now())

    record: Mapped["Record"] = relationship(back_populates="sections")
    summaries: Mapped[List["Summary"]] = relationship(back_populates="section")
    analysis: Mapped["Analysis"] = relationship(back_populates="section")
    highlights: Mapped[List["Highlight"]] = relationship(back_populates="section", cascade="all, delete-orphan")

    @property
    def serialize(self):
        return {
            'section_id': self.section_id,
            'record_id': self.record_id,
            'title': self.title,
            'start_time': self.start_time,
            'end_time': self.end_time,
            'created_time': self.created_time
        }

class Summary(Base):
    __tablename__ = "summary"

    summary_id: Mapped[int] = mapped_column(INTEGER(unsigned=True), primary_key=True, autoincrement=True)
    section_id: Mapped[int] = mapped_column(INTEGER(unsigned=True), ForeignKey("section.section_id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False)
    content: Mapped[str] = mapped_column(VARCHAR(1000), nullable=False)
    created_time: Mapped[Optional[str]] = mapped_column(TIMESTAMP, default=func.now())

    section: Mapped["Section"] = relationship(back_populates="summaries")

    @property
    def serialize(self):
        return {
            'summary_id': self.summary_id,
            'section_id': self.section_id,
            'content': self.content,
            'created_time': self.created_time
        }


class Analysis(Base):
    __tablename__ = "analysis"

    analysis_id: Mapped[int] = mapped_column(INTEGER(unsigned=True), primary_key=True, autoincrement=True)
    section_id: Mapped[int] = mapped_column(INTEGER(unsigned=True), ForeignKey("section.section_id", ondelete="CASCADE", onupdate="CASCADE"), nullable=False)
    content: Mapped[str] = mapped_column(Text, nullable=False)
    created_time: Mapped[Optional[str]] = mapped_column(TIMESTAMP, default=func.now())

    section: Mapped["Section"] = relationship(back_populates="analysis")

    @property
    def serialize(self):
        return {
            'analysis_id': self.analysis_id,
            'section_id': self.section_id,
            'content': self.content,
            'created_time': self.created_time
        }
