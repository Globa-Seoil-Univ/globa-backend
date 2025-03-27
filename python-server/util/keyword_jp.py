import os
import json
import MeCab
from transformers import BertModel
from keybert import KeyBERT

from util.log import Logger

class KeywordUtilJP:
    def __init__(self):
        self.logger = Logger(name="keyword_jp").logger

        # 일본어 BERT 모델 사용 (한국어 모델 대신)
        try:
            self.model = BertModel.from_pretrained("cl-tohoku/bert-base-japanese-v2")
            self.kw_model = KeyBERT(self.model)
        except Exception as e:
            self.logger.error(f"모델 로드 오류: {e}")
            # 백업 옵션: 기본 모델 사용
            self.kw_model = KeyBERT()
        self.mecab = MeCab.Tagger("-d \"C:/Program Files/MeCab/dic/ipadic\"")

        # MeCab 테스트
        try:
            test_result = self.mecab.parse("テスト")
            self.logger.info(f"MeCab 초기화 성공")
        except Exception as e:
            self.logger.error(f"MeCab 초기화 오류: {e}")

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
            if line == "EOS" or not line:
                continue

            parts = line.split("\t")
            if len(parts) >= 2:
                token = parts[0]
                info = parts[1].split(",") if len(parts) > 1 else []

                # 일본어 명사 체크
                if len(info) > 0 and "名詞" in info[0]:  # 명사 확인 방식 수정
                    if len(token) > 1 or token in self.user_words:
                        results.append(token)

        self.logger.info(f"추출된 명사: {results}")
        return results

    def __preprocess(self, text: str):
        nouns = self.__noun_extractor(text)
        return ' '.join(nouns)

    def __split_into_sentences(self, text: str):
        return [text.replace("\n", " ")], [self.__preprocess(text)]

    def get_keywords(self, text: str):
        sentences, pre_sentences = self.__split_into_sentences(text)

        # 전처리된 텍스트 확인
        if not pre_sentences[0].strip():
            self.logger.warning("전처리된 텍스트가 비어 있습니다.")
            return []

        try:
            keywords = self.kw_model.extract_keywords(
                pre_sentences[0], keyphrase_ngram_range=(1, 1), stop_words=None,
                use_maxsum=True, use_mmr=True, diversity=0.3, top_n=10
            )

            if not keywords:
                self.logger.warning("추출된 키워드가 없습니다.")
                return []

            for keyword in keywords:
                try:
                    self.logger.info(f"keyword result: {keyword[0]} :: {keyword[1]}")
                except:
                    pass  # 로깅 오류 무시

            return keywords
        except Exception as e:
            self.logger.error(f"키워드 추출 중 오류 발생: {e}")
            return []


# if __name__ == "__main__":
#     print("가보장")
#     keyword_util = KeywordUtilJP()
#
#     # 일본어 샘플 텍스트
#     sample_text = """日本の首都は東京です。東京は日本の経済、政治、文化の中心地です。
#     世界中から観光客が訪れる人気の都市です。美味しい食べ物や歴史的な建物も多くあります。"""
#
#     # 명사 추출 결과 확인
#     nouns = keyword_util._KeywordUtilJP__noun_extractor(sample_text)
#     print("추출된 명사:", nouns)
#
#     # 전처리된 텍스트 확인
#     preprocessed = keyword_util._KeywordUtilJP__preprocess(sample_text)
#     print("전처리된 텍스트:", preprocessed)
#
#     # 키워드 추출
#     keywords = keyword_util.get_keywords(sample_text)
#
#     print("\n입력 텍스트:")
#     print(sample_text)
#     print("\n추출된 키워드:")
#     print(keywords)
