import MeCab

# mecabrc 파일의 경로를 명시적으로 지정
mecab = MeCab.Tagger("-d \"C:/Program Files/MeCab/dic/ipadic\"")
text = "これはテストです。"
result = mecab.parse(text)
print(result)
