#!/bin/bash
# Пытаемся найти подходящую Java (Spring Boot 3.2 не работает на Java 25)

if [ -d "/usr/lib/jvm/java-17-openjdk" ]; then
    export JAVA_HOME="/usr/lib/jvm/java-17-openjdk"
    echo "Используем Java 17: $JAVA_HOME"
elif [ -d "/usr/lib/jvm/java-21-openjdk" ]; then
    export JAVA_HOME="/usr/lib/jvm/java-21-openjdk"
    echo "Используем Java 21: $JAVA_HOME"
else
    echo "ВНИМАНИЕ: Не найдена Java 17 или 21 в стандартных путях Arch Linux."
    echo "Текущая версия Java:"
    java -version
fi

# Проверка наличия Maven
if ! command -v mvn &> /dev/null; then
    echo "ОШИБКА: Maven не установлен. Установите его: sudo pacman -S maven"
    exit 1
fi

echo "Запуск приложения..."
mvn clean spring-boot:run
