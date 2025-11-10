# Picture Z - vacation, trip picture sharing platform

## How to run (docker compose)

```bash
docker compose up --build
```

## How to run (docker)

```bash
docker run -d -e MARIADB_ROOT_PASSWORD=root -p 3306:3306 --name mariadb mariadb
docker build --tag backend-picz backend
docker run -d -e MARIADB_HOST=host.docker.internal -e GOOGLE_MAPS_APIKEY=$GOOGLE_MAPS_APIKEY -p 8080:8080 --name backend-picz backend-picz
docker build --tag frontend-picz frontend
docker run -d -p 5173:5173 --name frontend-picz frontend-picz
```

## How to run (the good old-fashioned way)

### DB

```bash
docker run -d -e MARIADB_ROOT_PASSWORD=root -p 3306:3306 --name mariadb mariadb
```

### Backend

```bash
export GOOGLE_MAPS_APIKEY=???
./gradlew bootRun
```

### Frontend

```bash
npm i
npm run dev
```

## How to access

Frontend: http://localhost:5173

Backend: http://localhost:8080/swagger-ui/index.html
