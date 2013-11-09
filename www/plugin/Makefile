CLASSPATH = -cp .:commons-lang3.jar

build: DOMjudgeFeed.class

DOMjudgeFeed.class: DOMjudgeFeed.java
	javac $(CLASSPATH) $<

clean:
	rm -f *.class

.PHONY: build clean
