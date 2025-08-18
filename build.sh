#!/bin/bash
echo "🔨 Building EduLink Backend..."
mvn clean package -DskipTests
echo "✅ Build completed!"
echo "📦 JAR file: target/edulink-backend-*.jar"