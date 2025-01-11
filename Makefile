
run:
	make clean
	make build
	#make execute_1
	make execute_2
	make execute_3
	make view

clean:
	rm -rf ./build/
	rm -rf ./temp/
	mkdir -p ./build/
	mkdir -p ./temp/

.PHONY: build
build:
	javac -d ./build/ src/AsciiPixels.java
	javac -d ./build/ src/Img2AsciiPixels.java

TXTS = $(wildcard ./examples_txt2img/*txt)
execute_1:
	for TXT in $(TXTS); do \
		(cd ./temp/; cat ../"$$TXT" | java -cp ../build/ AsciiPixels); \
	done

execute_2:
	cp examples_img2txt/chickens.png ./temp/
	(cd ./temp; echo ./chickens.png | java -cp ../build/ Img2AsciiPixels)

execute_3: # part 1 and part 2 together.
	(cd ./temp/; cat ./chickens_converted.txt | java -cp ../build/ AsciiPixels)

view:
	sxiv -t ./temp/*png
	less ./temp/chickens_converted.txt
