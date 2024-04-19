# Используем базовый образ JDK 8
FROM openjdk:8

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем файлы, если это необходимо (в случае, если у вас есть какие-то дополнительные файлы)
COPY . .

# Определяем команду, которая будет выполнена при запуске контейнера
ENTRYPOINT ["bash", "-c", "java -server -Dfile.encoding=UTF-8 -Xincgc -Xms8G -Xmx12G -cp ./../libs/json-simple-1.1.1.jar:./../libs/lameguard-1.9.5.jar:./../libs/bsf.jar:./../libs/bsh-2.0b4.jar:./../libs/bonecp-0.8.0.jar:./../libs/slf4j-api-1.7.12.jar:./../libs/slf4j-nop-1.7.12.jar:./../libs/guava-17.0.jar:./../libs/c30-0.91.2.jar:./../libs/jython.jar:./../libs/ecj.jar:./../libs/java-engine.jar:./../libs/commons-logging-1.1.jar:./../libs/commons-pool-1.5.4.jar:./../libs/netty-3.6.3.Final.jar:./../libs/javolution-5.5.1.jar:./../libs/mysql-connector-java-5.1.30-bin.jar:./../libs/core-gs.jar ru.agecold.gameserver.GameServer > log/stdout.log"]