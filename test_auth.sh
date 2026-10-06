JWT_SECRET=super_secret_for_local_testing_that_is_at_least_32_bytes ./mvnw spring-boot:run -Dspring-boot.run.profiles=test > app.log 2>&1 &
APP_PID=$!
sleep 15
DEV_PASSWORD=$(grep "developer user" app.log | awk -F "password: " '{print $2}' | awk '{print $1}')
DEV_API_KEY=$(grep "developer user" app.log | awk -F "apiKey: " '{print $2}')

ADMIN_API_KEY=$(grep "admin user" app.log | awk -F "apiKey: " '{print $2}')

echo "DEV_PASSWORD: $DEV_PASSWORD"
echo "DEV_API_KEY: $DEV_API_KEY"

TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"username\":\"developer\",\"password\":\"$DEV_PASSWORD\"}" | grep -o '"token":"[^"]*' | cut -d'"' -f4)

echo "TOKEN: $TOKEN"

echo -e "\n1. Test DEV accessing their own key:"
curl -s -w "\nHTTP_STATUS: %{http_code}\n" -X GET http://localhost:8080/api/usage/$DEV_API_KEY -H "Authorization: Bearer $TOKEN"

echo -e "\n2. Test DEV accessing ADMIN key:"
curl -s -w "\nHTTP_STATUS: %{http_code}\n" -X GET http://localhost:8080/api/usage/$ADMIN_API_KEY -H "Authorization: Bearer $TOKEN"

echo -e "\n3. Test DEV accessing summary:"
curl -s -w "\nHTTP_STATUS: %{http_code}\n" -X GET http://localhost:8080/api/usage/summary -H "Authorization: Bearer $TOKEN"

kill $APP_PID
