JAVA_HOTSPOT ?= /opt/jdk1.8.0_241
TARGET ?= Bench

ifeq (,$(wildcard $(JAVA_HOTSPOT)))
	JAVA_CMD := java
else
	JAVA_CMD := $(JAVA_HOTSPOT)/bin/java -XX:+UnlockCommercialFeatures -XX:+UnlockDiagnosticVMOptions -XX:+FlightRecorder -XX:+DebugNonSafepoints -XX:FlightRecorderOptions=defaultrecording=true,disk=true,repository=/tmp,dumponexit=true
endif

.PHONY: help
.DEFAULT_GOAL := help
help:
	@echo  $$(fgrep -h "## " $(MAKEFILE_LIST) | fgrep -v fgrep | sed -e 's/^\([a-z][a-z_\-]*\): .*##/\\nmake \\033[1;34m\1\\033[0m \\t:/g')


build: ## build and install
	mvn clean install

scapegoat: ## compile with scapegoat enabled
	mvn clean compile -Pscapegoat -DskipTests

coverage: ## run test coverage
	mvn scoverage:check


bench: ## build with JMH and run bench tests
	mvn clean package -Pjmh -DskipTests
	rm -f *.jfr
	$(JAVA_CMD) -cp . -jar target/xgboost-predictor-*-SNAPSHOT-jmh.jar -f 1 -i 5 -wi 3 $(TARGET)
