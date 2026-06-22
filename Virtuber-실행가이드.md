# Virtuber 실행 방법

## 1. DB 실행

프로젝트 루트에서 MySQL 컨테이너를 실행합니다.

```bash
docker compose up -d
```

## 2. 백엔드 실행

프로젝트 루트에서 Spring Boot 서버를 실행합니다.

```bash
./gradlew bootRun
```

백엔드 주소:

```text
http://localhost:8000
```

## 3. 프론트엔드 실행

프론트 폴더로 이동합니다.

```bash
cd frontend
```

처음 실행할 때만 패키지를 설치합니다.

```bash
npm install
```

프론트 서버를 실행합니다.

```bash
npm run dev
```

프론트 주소:

```text
http://localhost:5173
```

## 4. 환경 변수

`frontend/.env` 값은 아래처럼 되어 있어야 합니다.

```env
VITE_API_BASE_URL=http://localhost:8000
```

`.env`를 수정했다면 프론트 서버를 껐다가 다시 실행합니다.

## 5. 종료

백엔드/프론트 서버는 실행 중인 터미널에서 `Ctrl + C`로 종료합니다.

DB 컨테이너는 프로젝트 루트에서 종료합니다.

```bash
docker compose down
```
