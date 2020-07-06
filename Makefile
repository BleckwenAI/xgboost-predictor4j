JAVA_HOTSPOT ?= /opt/jdk1.8.0_241
TARGET ?= Bench
SONAR_URL ? = https://yoururlto/sonarqube

ifeq (,$(wildcard $(JAVA_HOTSPOT)))
	JAVA_CMD := java
else
	JAVA_CMD := $(JAVA_HOTSPOT)/bin/java -XX:+UnlockCommercialFeatures -XX:+UnlockDiagnosticVMOptions -XX:+FlightRecorder -XX:+DebugNonSafepoints -XX:FlightRecorderOptions=defaultrecording=true,disk=true,repository=/tmp,dumponexit=true
endif

.PHONY: help
.DEFAULT_GOAL := help

help:
	@echo  $$(fgrep -h "## " $(MAKEFILE_LIST) | fgrep -v fgrep | sed -e 's/^\([a-z][a-z0-9]*\): .*##/\\nmake \\033[1;34m\1\\033[0m \\t:/g')

build: ## build and install
	mvn clean install

scapegoat: ## run scapegoat
	mvn clean compile -Pscapegoat -DskipTests

coverage: ## run coverage and update the README
	mvn clean scoverage:report
	coverage=$$(sed -nE 's/ *<td>([0-9]*).[0-9]*%<.*/\1/p' target/site/scoverage/packages.html | head -1); sed -i "s/coverage-.*%25/coverage-$$coverage%25/" README.md
	git status

build12: ## build and install with Scala 2.12
	mvn clean install -Pscala12

bench: ## build with JMH and run bench tests
	rm -f *.jfr
	mvn clean package -Pjmh -DskipTests
	$(JAVA_CMD) -cp . -jar target/xgboost-predictor4j-*-SNAPSHOT-jmh.jar -f 1 -i 5 -wi 3 $(TARGET)

sonar: ## run scapegoat, scoverage, dependency check and and publish results on sonarqube
	mvn clean
	mvn compile -Pscapegoat -DskipTests
	mvn scoverage:report
	mvn dependency-check:check
	mvn sonar:sonar -Dsonar.host.url=$(SONAR_URL) -Dsonar.exclusions=pom.xml

release: ## release on Maven central
	mvn release:prepare -DignoreSnapshots -P release
	mvn release:perform -P release
