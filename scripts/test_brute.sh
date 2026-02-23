#!/bin/bash
rm -f test_brute_log.txt
for i in {10..29}; do
  echo "192.168.1.99 - - [01/Jul/1995:00:00:$i -0400] \"GET /totally-unique-brute-path/ HTTP/1.0\" 401 6245" >> test_brute_log.txt
done
