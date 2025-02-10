from textblob import TextBlob


def analyze_text(text):
    # TextBlob 객체 생성
    blob = TextBlob(text)

    # 형태소 분석 및 품사 태깅
    analysis = blob.tags  # 품사 태깅

    # 분석 결과 출력
    for word, pos in analysis:
        print(f'{word}: {pos}')


if __name__ == "__main__":
    # 분석할 영어 문장 입력
    text = "TextBlob is a simple library for processing textual data."

    print("Shape and Part-of-Speech Tagging of the Text:")
    analyze_text(text)
