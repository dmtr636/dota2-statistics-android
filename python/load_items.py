import requests, json
import urllib.request


with open('items.json') as json_file:
    data = json.load(json_file)
    for hero in data.keys():
        try:
            url = "https://cdn.cloudflare.steamstatic.com" + str(data[hero]['img'])
            urllib.request.urlretrieve(url, "items/" +  str(data[hero]['img']).split("/")[-1].split("?")[0])
        except:
            print("ERROR " + url)

