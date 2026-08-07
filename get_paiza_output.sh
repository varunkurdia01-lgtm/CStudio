#!/bin/bash
OUTPUT=$(curl -s "http://api.paiza.io/runners/create" -d "source_code=#include<stdio.h>%0Aint main(){printf(\"Hello World\");return 0;}&language=c&api_key=guest")
ID=$(echo $OUTPUT | jq -r .id)
echo "Created ID: $ID"
sleep 3
curl -s "http://api.paiza.io/runners/get_details?id=$ID&api_key=guest" > output.json
cat output.json
