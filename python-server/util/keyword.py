import os
import json

from transformers import BertModel
from keybert import KeyBERT
from kiwipiepy import Kiwi


class KeywordUtil:
    def __init__(self):
        self.model = BertModel.from_pretrained("google-bert/bert-base-uncased")
        self.kw_model = KeyBERT(self.model)
        self.kiwi = Kiwi()

        project_path = os.getcwd()
        keyword_file_path = project_path + "/keyword.json"

        if os.path.exists(keyword_file_path) and os.path.isfile(keyword_file_path):
            with open(keyword_file_path, 'r', encoding="utf-8") as file:
                json_data = json.load(file)

            for data in json_data["words"]:
                self.kiwi.add_user_word(data["word"], data["tag"])

    # 명사만 가져오기
    def __noun_extractor(self, text: str):
        results = []
        result = self.kiwi.analyze(text)

        for token, pos, _, _ in result[0][0]:
            # 체언과 알파벳만 추출 (일반, 고유, 의존 명사 or 수사 or 대명사 or 영단어)
            # 수사 : 체언에 속하면서 사물의 수량이나 순서를 나타내는 품사 ex) 하나, 둘
            if len(token) != 1 and pos.startswith('N') or pos.startswith('SL'):
                results.append(token)

        return results

    def __preprocess(self, text: str):
        nouns = self.__noun_extractor(text)
        return ' '.join(nouns)

    def __split_into_sentences(self, text: str):
        # 문장 분리 없이 전체 텍스트를 한 문장으로 간주
        return [text.replace("\n", " ")], [self.__preprocess(text)]

    def get_keywords(self, text: str):
        sentences, pre_sentences = self.__split_into_sentences(text)

        # 하나의 문장으로 키워드 추출
        keywords = self.kw_model.extract_keywords(pre_sentences[0], keyphrase_ngram_range=(1, 1), stop_words=None,
                                                  use_maxsum=True, use_mmr=True, diversity=0.3, top_n=10)

        return keywords
