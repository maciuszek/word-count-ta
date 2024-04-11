#!/bin/sh

# Usage: env OUTPUT=/path/to/file generate_random_text_file_with_words.sh

OUTPUT="${OUTPUT:-generated_big_input.txt}"

for i in {1..10}
do 
	shuf -n 1000000 /usr/share/dict/words | fmt -w 72 >> "$OUTPUT" # found https://gitlab.archlinux.org/archlinux/packaging/packages/words
done
