### Description

File word count to stdout implementation with Spring in a reactive programming paradigm. Structured with extensibility for alternative data ingress/egress and non-reactive implementations.

### Dependencies
* JDK 17
* gnumake (optional)
* docker and an active daemon listening (optional)

### Build Jar
`./mvnw package`
or if you have gnumake
`make jar`

### Run Jar
`java -jar target/wordcount-*-SNAPSHOT.jar ./src/test/resources/input.txt ./src/test/resources/input2.txt`

### Build Container (requires optional dependencies)
`make image`

### Run Container (requires optional dependencies)
```
docker run --rm \
-v ./src/test/resources/input.txt:/app/./src/test/resources/input.txt \
-v ./src/test/resources/input2.txt:/app/./src/test/resources/input2.txt \
-e ARGS='/app/./src/test/resources/input.txt /app/./src/test/resources/input2.txt' \
com.maciuszek/wordcount:local
```

### More Info
The Makefile has a help page. Run `make help` or just `make`

### TODO
Setup GitHub Actions for CI and push to Docker Hub
