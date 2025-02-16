import os
import json
import MeCab
from transformers import BertModel
from keybert import KeyBERT


class KeywordUtilJP:
    def __init__(self):
        self.model = BertModel.from_pretrained("skt/kobert-base-v1")
        self.kw_model = KeyBERT(self.model)
        self.mecab = MeCab.Tagger("-d \"C:/Program Files/MeCab/dic/ipadic\"")


        project_path = os.getcwd()
        keyword_file_path = os.path.join(project_path, "keyword_jp.json")

        if os.path.exists(keyword_file_path) and os.path.isfile(keyword_file_path):
            with open(keyword_file_path, 'r', encoding="utf-8") as file:
                json_data = json.load(file)

            self.user_words = [data["word"] for data in json_data["words"]]
        else:
            self.user_words = []

    def __noun_extractor(self, text: str):
        results = []
        parsed = self.mecab.parse(text)

        for line in parsed.split("\n"):
            parts = line.split("\t")
            if len(parts) > 3 and "名詞" in parts[3]:  # 명사 체크
                token = parts[0]
                if len(token) > 1 or token in self.user_words:
                    results.append(token)

        return results

    def __preprocess(self, text: str):
        nouns = self.__noun_extractor(text)
        return ' '.join(nouns)

    def __split_into_sentences(self, text: str):
        return [text.replace("\n", " ")], [self.__preprocess(text)]

    def get_keywords(self, text: str):
        sentences, pre_sentences = self.__split_into_sentences(text)
        keywords = self.kw_model.extract_keywords(
            pre_sentences[0], keyphrase_ngram_range=(1, 1), stop_words=None,
            use_maxsum=True, use_mmr=True, diversity=0.3, top_n=10
        )
        return keywords.parse()
# if __name__ == "__main__":
#     print("가보장")
#     keyword_util = KeywordUtilJP()
#
#     # 일본어 샘플 텍스트 (3문장 이상)
#     sample_text = """日本の首都は東京です。東京は日本の経済、政治、文化の中心地です。
#     世界中から観光客が訪れる人気の都市です。美味しい食べ物や歴史的な建物も多くあります。"""
#
#     keywords = keyword_util.get_keywords(sample_text)
#
#     print(" 입력 텍스트:")
#     print(sample_text)
#     print("\n 추출된 키워드:")
#     print(keywords)
