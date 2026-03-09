![image](https://github.com/user-attachments/assets/00f4c3fb-fb21-4821-8771-2092f133b9e4)
<h2 align=center>소리에서 시작하는 무한한 가능성</h2>
<h1 align=center>$\huge{\color{#C67C4E}G}loba$</h1>
<br /> <br />

## 목차
- [프로젝트 소개](#프로젝트-소개)
- [개발 기간](#개발-기간)
- [팀원 소개](#팀원-소개)
- [역할](#역할)
- [사용 기술](#사용-기술)
- [아키텍쳐](#아키텍쳐)
- [프로젝트 구조 (Spring)](#프로젝트-구조-spring)
- [프로젝트 구조 (Python)](#프로젝트-구조-python)
- [Demo 영상](#Demo-영상)
<br />

## 프로젝트 소개
- AI 시장의 폭발적인 성장 및 디지털 수업을 이용한 교육 방식의 변화에 따라, AI를 활용한 STT 모바일 앱입니다.
- STT 기술을 활용하여 사용자의 음성을 텍스트로 변환하고, 이를 기반으로 다양한 기능으 제공합니다.
<br />

## 개발 기간
- 1차 개발 (졸업 작품) : 2024.03 ~ 2024.09
- 2차 개발 (서비스): 2025.01 ~ 2025.09
<br />

## 팀원 소개
<div align="center">

| **김승용** | **김인태** | **윤성빈** |
| :------: |  :------: | :------: |
| [<img src="https://avatars.githubusercontent.com/u/44765636?v=4" height=150 width=150> <br/> @seungyong](https://github.com/seungyong) | [<img src="https://avatars.githubusercontent.com/u/22989582?v=4" height=150 width=150> <br/> @dbstjdqls14](https://github.com/dbstjdqls14) | [<img src="https://avatars.githubusercontent.com/u/62525605?v=4" height=150 width=150> <br/> @HaeBun](https://github.com/HaeBun) |

</div>

<br />

## 역할
### 김승용 (팀장, 백엔드)
|         구분         | 담당 내용                                                                                                                                       |
| :----------------: | :------------------------------------------------------------------------------------------------------------------------------------------ |
|   🧠 **기획 및 설계**   | - 아이디어 제공<br> - DB 및 API 구조 설계 및 문서화<br> - UI/UX 디자인                                                                                                     |
|  🗣️ **AI 기능 구현**  | - **Whisper**를 활용한 음성 인식 (STT)<br> - **Kiwi**를 통한 중요 키워드 추출<br> - **OpenAI GPT API**를 통한 요약, 단락 분리, 퀴즈 생성                                   |
|    ⚙️ **백엔드 개발**   | - **사용자 인증 (JWT)**, 문서·댓글·공유·권한 관리 등 API 개발<br> - **우리말샘 Excel 데이터** 기반 단어 검색 기능 구현<br> - **Spring Boot API 서버 리팩토링 (Clean Architecture 적용)** |
| ☁️ **인프라 구축 및 배포** | - **AWS EC2, RDS, SQS**를 활용한 서버 환경 구축 및 배포                                                                                                  |
|   🧱 **기타 주요 업무**  | - 성능 최적화 및 코드 구조 개선                                                                                              |

### 김인태 (프론트)
|         구분         | 담당 내용                                                                                                                                       |
| :----------------: | :------------------------------------------------------------------------------------------------------------------------------------------ |
|   📱 **Android 앱 개발**   | - Activity/Fragment 구조 설계 및 공통 UI 컴포넌트 정의<br> - 업로드, 오디오 플레이어 등 핵심 사용자 플로우 구현 |
|  🧩 **MVVM 아키텍처 적용**  | - MVVM 아키텍처 적용<br> - DataBinding 을 활용한 UI-데이터 바인딩 적용 |
|    🔗 **API 연동 및 데이터 처리**   | - 백엔드 API 연동 모듈 구현<br> - STT 결과, 요약, 퀴즈 데이터 등 응답 모델 정의 및 파싱<br> - 네트워크 상태/에러 처리 |
| 🎨 **UI/UX 구현 및 개선** | - 일본어, 영어, 한국어 UI/텍스트 지원 구현<br> - 시스템 언어 변경 기능 및 리소스 관리<br> - 언어별 레이아웃, 문구 검수 및 테스트 진행 |
|   🧪 **품질 및 성능 개선**  | - 화면 전환 및 리스트 스크롤 성능 최적화<br> - 크래시 로그 분석 및 예외 처리 보강<br> - 기기별 해상도 대응 및 실제 단말 테스트 진행 |

### 윤성빈 (백엔드)
| 구분                   | 담당 내용                                                                                              |
| -------------------- | -------------------------------------------------------------------------------------------------- |
| **설계**               | • DB 및 API 일부 설계                                                                                   |
| **CRUD (Spring 서버)** | • Redis 기반 JWT 토큰 사용자 인증<br>• 검색 요청 Throttle 처리<br>• 권한 기반 사용자 / 문서 / 폴더 CRUD                      |
| **AI (Python 서버)**   | • Whisper 기반 영어 / 일본어 STT<br>• STT 결과 기반 섹션 분리, 요약 및 퀴즈 생성<br>• TextBlob, MeCab 기반 영어 / 일본어 형태소 추출 |
| **배포 환경 구축**         | • Vast.ai 기반 Linux GPU 서버에서 Docker로 AI 서버 호스팅<br>• AWS SQS 기반 비동기 처리                               |
| **성능 개선**            | • WER, CER 기준 STT 정확도 개선 (0.6 → 0.3)                                                               |


<br />

## 사용 기술

| 구분 | 기술 스택 |
| :-- | :-- |
| **Android** | <img src="https://img.shields.io/badge/Android%20(Java)-3DDC84?logo=android&logoColor=white" /> |
| **Backend** | <img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=springboot&logoColor=white" /> <img src="https://img.shields.io/badge/Spring%20Data%20JPA-007396?logo=spring&logoColor=white" /> <img src="https://img.shields.io/badge/Python-3776AB?logo=python&logoColor=white" /> <img src="https://img.shields.io/badge/openAI/gpt-412991?logo=openai&logoColor=white" /> <img src="https://img.shields.io/badge/Whisper-4B6EAF?logo=whisper&logoColor=white" /> <img src="https://img.shields.io/badge/Kiwi-00BFFF?logoColor=white" /> |
| **DB** | <img src="https://img.shields.io/badge/MariaDB-003545?logo=mariadb&logoColor=white" /> <img src="https://img.shields.io/badge/Redis-DC382D?logo=redis&logoColor=white" /> |
| **인프라 및 배포** | <img src="https://img.shields.io/badge/AWS%20EC2-FF9900?logo=amazon-aws&logoColor=white" /> <img src="https://img.shields.io/badge/AWS%20RDS-527FFF?logo=amazon-aws&logoColor=white" /> <img src="https://img.shields.io/badge/AWS%20SQS-232F3E?logo=amazon-aws&logoColor=white" /> <img src="https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white" /> <img src="https://img.shields.io/badge/Docker%20Compose-2496ED?logo=docker&logoColor=white" /> |
| **형상 관리** | <img src="https://img.shields.io/badge/Git-181717?logo=git&logoColor=white" /> <img src="https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white" /> |
| **디자인** | <img src="https://img.shields.io/badge/Figma-F24E1E?logo=figma&logoColor=white" /> |

<br />

## 아키텍쳐

<img width="1214" height="750" alt="image" src="https://github.com/user-attachments/assets/d999e43c-c8ca-4bee-94f0-d4abec64fa00" />
<br />

## 프로젝트 구조 (Spring)

```text
├─api
├─application
│  ├─comment
│  │  ├─command
│  │  ├─dto
│  │  │  ├─common
│  │  │  ├─request
│  │  │  └─response
│  │  ├─mapper
│  │  ├─service
│  │  └─usecase
│  ├─analysis
│  ├─answer
│  ├─dictionary
│  ├─dummyimage
│  ├─folder
│  ├─folderrole
│  ├─foldershare
│  ├─hightlight
│  ├─inquiry
│  ├─keyword
│  ├─notice
│  ├─noticeimage
│  ├─notification
│  ├─notificationread
│  ├─quiz
│  ├─quizattemp
│  ├─record
│  ├─section
│  ├─sqs
│  ├─study
│  ├─summary
│  ├─survey
│  ├─user
│  └─userrole
├─common
│  ├─annotation
│  ├─aspect
│  ├─config
│  ├─event
│  ├─exception
│  ├─filter
│  ├─scheduler
│  ├─type
│  ├─usecase
│  ├─util
│  └─validation
├─domain
│  ├─comment
│  │  └─repository
│  ├─analysis
│  ├─answer
│  ├─dictionary
│  ├─dlq
│  ├─dummyimage
│  ├─folder
│  ├─folderrole
│  ├─foldershare
│  ├─highlight
│  ├─inquiry
│  ├─keyword
│  ├─notice
│  ├─noticeimage
│  ├─notification
│  ├─notificationread
│  ├─quiz
│  ├─quizattemp
│  ├─record
│  ├─role
│  ├─section
│  ├─study
│  ├─summary
│  ├─survey
│  ├─user
│  └─userrole
├─infrastructure
│   └─persistence
│       ├─comment
│       │  ├─entity
│       │  ├─projection
│       │  └─repository
│       ├─analysis
│       ├─answer
│       ├─dictionary
│       ├─dlq
│       ├─dummyimage
│       ├─folder
│       ├─folderrole
│       ├─foldershare
│       ├─highlight
│       ├─inquiry
│       ├─keyword
│       ├─notice
│       ├─noticeimage
│       ├─notification
│       ├─notificationread
│       ├─quiz
│       ├─quizattemp
│       ├─record
│       ├─role
│       ├─section
│       ├─study
│       ├─summary
│       ├─survey
│       ├─user
│       └─userrole
├─Dockerfile
└─build.gradle
```
<br />

## 프로젝트 구조 (Python)

```text
├─analyze
├─downloads
├─exception
├─log
├─mode
├─util
├─Dockerfile
├─consumer.py
├─keyword.json
├─main.py
├─producer.py
├─requirements.txt
└─resource.txt
```

<br />

## Demo 영상
<div align=center>
  
[![Globa Demo Video](https://img.youtube.com/vi/NHepQN2UuM8/0.jpg)](https://youtu.be/NHepQN2UuM8)

</div>
