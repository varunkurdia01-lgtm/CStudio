ID=$(curl -s "http://api.paiza.io/runners/create" -d "source_code=#include<stdio.h>%0Aint main(){printf(\"Hello World\");return 0;}&language=c&api_key=guest" | jq -r .id)
sleep 2
curl -s "http://api.paiza.io/runners/get_details?id=$ID&api_key=guest" | jq .
