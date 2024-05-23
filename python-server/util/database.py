import os

from sqlalchemy import create_engine
from sqlalchemy.orm import Session, DeclarativeBase
from dotenv import load_dotenv

load_dotenv()


DATABASE_URL = os.getenv('database-url')
engine = create_engine(DATABASE_URL)
session = Session(engine)


class Base(DeclarativeBase):
    pass


def get_session():
    return session
