import requests, json
import urllib.request


with open('heroes.json') as json_file:
    data = json.load(json_file)
    for hero in data.keys():
        url = "https://cdn.cloudflare.steamstatic.com" + str(data[hero]['img'])
        urllib.request.urlretrieve(url, str(data[hero]['img']).split("/")[-1].replace("?", ""))

