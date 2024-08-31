import os

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, DeclarativeBase
from dotenv import load_dotenv

load_dotenv()

DATABASE_URL = os.getenv('database-url')
engine = create_engine(DATABASE_URL)
SessionMaker = sessionmaker(autoflush=False, bind=engine)


class Base(DeclarativeBase):
    pass
