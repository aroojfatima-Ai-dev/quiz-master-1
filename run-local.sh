#!/bin/sh
# ---------------------------------------------------------------------------
# Quiz Master — local start.
# Maven install karne ki zaroorat nahi: Maven Wrapper (./mvnw) khud Maven
# download kar leta hai (pehli baar internet chahiye).
#   macOS/Linux :  ./run-local.sh
#   Rukne ke liye: Ctrl + C
# ---------------------------------------------------------------------------
set -e
cd "$(dirname "$0")"

if ! command -v java >/dev/null 2>&1; then
  echo ""
  echo "  Java nahi mila."
  echo "  JDK 17 install karo (LOCAL-SETUP.md dekho), phir naya terminal khol ke dobara chalao."
  echo ""
  exit 1
fi

echo "Java version:"
java -version 2>&1 | head -1
echo ""
echo "App start ho raha hai → http://localhost:8080"
echo "Admin login: admin@quizmaster.app / Admin@12345"
echo ""

[ -x ./mvnw ] || chmod +x ./mvnw 2>/dev/null || true
if [ -x ./mvnw ]; then
  exec ./mvnw spring-boot:run
elif command -v mvn >/dev/null 2>&1; then
  exec mvn spring-boot:run
else
  exec sh ./mvnw spring-boot:run
fi
