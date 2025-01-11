
run:
	#make build
	make test
	make view

.PHONY: build
build:
	make clean_build
	mkdir -p ./build/
	javac -d ./build/ src/AsciiPixels.java

clean_build:
	rm -rf ./build/

TXTS = $(wildcard ./examples_txt2img/*txt)
test:
	make clean_test
	mkdir -p ./temp/
	for TXT in $(TXTS); do \
		(cd ./temp/; cat ../"$$TXT" | java -cp ../build/ AsciiPixels); \
		done

clean_test:
	rm -rf ./temp/

view:
	sxiv -t ./temp/
