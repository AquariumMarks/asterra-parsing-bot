#!/bin/bash

# Параметры подключения к серверу
SERVER_USER=
SERVER_HOST=
SERVER_PATH="/opt/asterra-parsing-bot"

# Получаем версию из pom.xml
VERSION=$(grep -m1 "<version>" pom.xml | sed 's/[[:space:]]*<version>\(.*\)<\/version>/\1/')

# Создаем директорию для новой версии
DEPLOY_DIR="${SERVER_PATH}/versions/${VERSION}"

echo "Deploying version ${VERSION} to server..."

# Создаем необходимые директории на сервере
ssh $SERVER_USER@$SERVER_HOST "mkdir -p ${DEPLOY_DIR} ${SERVER_PATH}/logs ${SERVER_PATH}/config"

# Копирование файлов на сервер
scp -r Dockerfile docker-compose.yml mvnw .mvn pom.xml src $SERVER_USER@$SERVER_HOST:$DEPLOY_DIR

# Подключение к серверу и запуск приложения
ssh $SERVER_USER@$SERVER_HOST << ENDSSH
    cd ${DEPLOY_DIR}
    export VERSION=${VERSION}

    # Останавливаем старый контейнер
    docker-compose down || true

    # Собираем и запускаем новый контейнер
    docker-compose build --no-cache
    docker-compose up -d

    # Создаем символическую ссылку на текущую версию
    ln -sfn ${DEPLOY_DIR} ${SERVER_PATH}/current

    # Очистка старых образов
    docker image prune -f
ENDSSH

echo "Deployment of version ${VERSION} completed!"
