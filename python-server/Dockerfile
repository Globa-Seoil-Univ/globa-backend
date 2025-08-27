FROM python:3.10-slim as builder

RUN apt-get update && apt-get install -y \
    mecab mecab-ipadic-utf8 libmecab-dev \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

RUN pip install --no-cache-dir \
    torch==2.3.1+cpu \
    --index-url https://download.pytorch.org/whl/cpu && \
    find /usr/local/lib/python3.10/site-packages/torch -name "*.so" -exec strip {} \; && \
    rm -rf /usr/local/lib/python3.10/site-packages/torch/test && \
    rm -rf /usr/local/lib/python3.10/site-packages/torch/testing && \
    rm -rf /usr/local/lib/python3.10/site-packages/torch/bin && \
    rm -rf /usr/local/lib/python3.10/site-packages/torch/include && \
    rm -rf /usr/local/lib/python3.10/site-packages/torch/share

RUN pip install --no-cache-dir \
    mecab-python3==1.0.10 \
    transformers \
    numpy \
    && pip cache purge

RUN find /usr/local -size +50M -type f -delete && \
    find /usr/local -name "*.a" -delete && \
    find /usr/local -name "*.la" -delete

FROM python:3.10-slim

RUN apt-get update && apt-get install -y \
    mecab mecab-ipadic-utf8 libmecab2 \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* \
    && rm -rf /usr/share/locale/* \
    && rm -rf /usr/share/doc/* \
    && rm -rf /usr/share/man/*

WORKDIR /app

COPY --from=builder /usr/local/lib/python3.10/site-packages /usr/local/lib/python3.10/site-packages

COPY . .
CMD ["python", "main.py"]
