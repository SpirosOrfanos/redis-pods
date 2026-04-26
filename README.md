cd redis-lib

mvn clean install

cd aw-player

mvn clean package

start player 1: java -jar target/aw-player-0.0.1-SNAPSHOT.jar --server.port=8080

start player 2: java -jar target/aw-player-0.0.1-SNAPSHOT.jar --server.port=8081

have fun
