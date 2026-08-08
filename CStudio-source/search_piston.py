import urllib.request
import json
req = urllib.request.Request("https://api.github.com/repos/engineer-man/piston/readme")
try:
    with urllib.request.urlopen(req) as response:
        data = json.loads(response.read().decode('utf-8'))
        import base64
        readme = base64.b64decode(data['content']).decode('utf-8')
        for line in readme.split('\n'):
            if 'http' in line:
                print(line)
except Exception as e:
    print(e)
