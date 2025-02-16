import os
import json
from transformers import BertModel
from keybert import KeyBERT
from textblob import TextBlob


class KeywordUtilEn:
    def __init__(self):
        self.model = BertModel.from_pretrained("sentence-transformers/paraphrase-MiniLM-L6-v2") # 모델 유명한거 넣어놓기는 했는데 잘 돌아가려나
        self.kw_model = KeyBERT(self.model)

        project_path = os.getcwd()
        keyword_file_path = os.path.join(project_path, "keyword_en.json")

        if os.path.exists(keyword_file_path) and os.path.isfile(keyword_file_path):
            with open(keyword_file_path, 'r', encoding="utf-8") as file:
                json_data = json.load(file)

            self.user_words = [data["word"] for data in json_data["words"]]
        else:
            self.user_words = []

    def __noun_extractor(self, text: str):
        results = []
        blob = TextBlob(text)

        for word, pos in blob.tags:
            # NN (명사), NNP (고유 명사)만 추출
            if pos in ["NN", "NNS", "NNP", "NNPS"] or word.lower() in self.user_words:
                results.append(word.lower())

        return results

    def __preprocess(self, text: str):
        nouns = self.__noun_extractor(text)
        return ' '.join(nouns)

    def __split_into_sentences(self, text: str):
        return [text.replace("\n", " ")], [self.__preprocess(text)]

    def get_keywords(self, text: str):
        sentences, pre_sentences = self.__split_into_sentences(text)
        keywords = self.kw_model.extract_keywords(
            pre_sentences[0], keyphrase_ngram_range=(1, 1), stop_words="english",
            use_maxsum=True, use_mmr=True, diversity=0.3, top_n=10
        )
        return keywords


