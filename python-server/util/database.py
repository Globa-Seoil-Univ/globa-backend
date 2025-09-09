import os

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, DeclarativeBase
from dotenv import load_dotenv

load_dotenv()

DATABASE_URL = os.getenv('database-url')
engine = create_engine(DATABASE_URL, pool_recycle=3600, pool_pre_ping=True, pool_timeout=30, connect_args={"connect_timeout": 600, "read_timeout": 600, "write_timeout": 600})
SessionMaker = sessionmaker(autoflush=False, bind=engine)


class Base(DeclarativeBase):
    pass
