import os

from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from dotenv import load_dotenv

load_dotenv()

DATABASE_URL = os.getenv('database-url')
engine = create_engine(DATABASE_URL)
session = sessionmaker(bind=engine)
Base = declarative_base()


def get_session():
    return session()
