### Description

File word count to stdout implementation with Spring in a reactive programming paradigm. Structured with extensibility for alternative data ingress/egress and non-reactive implementations.

### Build Dependencies
* jdk17
* gnumake (optional)
* docker and an active daemon (optional)

### Build The App As A Jar
`./mvnw -U clean package`
or if you have gnumake
`make jar`

### Run The Jar
`java -jar target/wordcount-*-SNAPSHOT.jar ./src/test/resources/input.txt`

### Build A Container To Run The Jar (requires all dependencies including optional)
`make image`

### Run The Container (requires docker)
```
docker run --rm \
-v ./src/test/resources/input.txt:/app/./src/test/resources/input.txt \
-e ARGS='/app/./src/test/resources/input.txt' \
com.maciuszek/wordcount:0.0.2-SNAPSHOT
```

### More Info
* Supports counting words from multiple files through additional command line arguments e.g. `java -jar target/wordcount-*-SNAPSHOT.jar ./src/test/resources/input.txt ./src/test/resources/tupni.txt`
* Doesn't natively support reading for stdin but this can be overcome with process substitution https://tldp.org/LDP/abs/html/process-sub.html
* To enable full debug logging run the jvm with `-Dlogging.level.com.maciuszek.wordcount=DEBUG`
* The Makefile has a help page. Run `make help` or just `make`

### Experimental
An alternative more efficient sorting implementation (based around an O(1) lfu algorithm) can be activated by setting `wordcount.sorted: true` in the application properties 

### Todo
* For CI setup GitHub Actions to run tests
* For CI setup GitHub Actions to push to Docker Hub
