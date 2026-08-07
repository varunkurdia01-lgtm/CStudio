import urllib.request
import urllib.parse
import json
import time

try:
    url = 'http://api.paiza.io/runners/create'
    data = urllib.parse.urlencode({'source_code': '#include<stdio.h>\nint main(){printf("Hello World\\n");return 0;}', 'language': 'c', 'api_key': 'guest'}).encode('utf-8')
    req = urllib.request.Request(url, data=data, headers={'User-Agent': 'Mozilla/5.0'})
    with urllib.request.urlopen(req) as response:
        res = json.loads(response.read().decode('utf-8'))
        id = res['id']
        print('Created ID:', id)

    time.sleep(2)

    url2 = f'http://api.paiza.io/runners/get_details?id={id}&api_key=guest'
    req2 = urllib.request.Request(url2, headers={'User-Agent': 'Mozilla/5.0'})
    with urllib.request.urlopen(req2) as response:
        res2 = json.loads(response.read().decode('utf-8'))
        print(res2)
except Exception as e:
    print("Error:", e)
