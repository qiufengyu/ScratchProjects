import sys
import json
import time
import random
import pprint

import urllib.request

import pymysql
from bs4 import BeautifulSoup
from selenium import webdriver


def config_driver():
  # set up chrome service
  if sys.platform.lower().startswith('win'):
    driver = webdriver.Chrome('chromedriver')
  elif sys.platform.lower().startswith('darwin'):
    driver = webdriver.Chrome('/usr/local/bin/chromedriver')
  else:
    print("Not supported platform")
    exit(-1)
  driver.set_page_load_timeout(1000)
  return driver

# test for accidents no longer available
test_url = 'http://www.trafficengland.com/?evtID=1501479' # no longer available
request_url = 'http://www.trafficengland.com/api/events/getEventById?evtId={}' # available 1490537

# Connecting to MySQL
db = pymysql.connect(host='localhost',
                     user='root',
                     password='root',
                     db='highways',
                     charset='utf8mb4',
                     cursorclass=pymysql.cursors.DictCursor)
c = db.cursor()
update_clear_statement = """
UPDATE `highways` SET `status` = 'Cleared' WHERE `guid` = {};
"""
select_not_cleared_statement = """
SELECT `guid` FROM `highways` WHERE `status` NOT LIKE 'Cleared' ORDER BY `pub_date`;
"""
# Open file

c.execute(select_not_cleared_statement)
not_cleared_incidents = c.fetchall()

print(not_cleared_incidents)

headers = [{'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8',
           # 'Accept-Encoding': 'gzip, deflate',
            'Accept-Language': 'zh-CN,zh;q=0.9,en;q=0.8;',
            'Host': 'www.trafficengland.com',},
           {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8',
            # 'Accept-Encoding': 'gzip, deflate',
            'Accept-Language': 'zh-CN,zh;q=0.9,en;q=0.8;',
            'Host': 'www.trafficengland.com',},
           ]

cookies = {
  'f5avrbbbbbbbbbbbbbbbb': 'ONLJNBDGMDOENNHBANCLDFLDJOIPCLDAEPPKFHDJGFDDDGAEDMHHFNELADEABLJJJJDABNHEKOCHLJIPHEDOLFKIPHNCDACGIHJNONGACGLFMLIFNKOCHCOLGBDHMMCA',
  '_ga': 'GA1.2.824784322.1514984339',
  'cc_cookie_accept': 'cc_cookie_accept',
  'JSESSIONID' : '7B82997B3F392705BC809D80C32FCEEE.ha-ntis-app141',
  'f5avrbbbbbbbbbbbbbbbb': 'MKFKHDIBJLECCPNAHECEKMPFGKGDBOIDBFJOGBHNFOBDKCDAODHJFLPMIIMAMMCFJJEFCBHGHFOJELJFHECDLCJPBCNCBGFPACEGDLEFJLGENJEEKNJPIIMDBIEOEGKI',
}

j = 0
driver = config_driver()
for incident in not_cleared_incidents:
  j += 1
  eventID = int(incident['guid'])
  print("Checking", eventID)
  #  = urllib.request.Request(url=request_url.format(eventID), headers=random.choice(headers))
  # response = urllib.request.urlopen(req, timeout=30)
  driver.get(request_url.format(eventID))
  # response_read = response.read()# .decode('utf-8')
  response = driver.page_source
  soup = BeautifulSoup(response, 'lxml')
  response_json = json.loads(soup.text.strip())
  # if response_json:
    # pprint.pprint(response_json[0])
  if response_json:
    current_status = response_json[0]['current']
    if current_status:
      time.sleep(random.random() * 10)
      continue
  # The incident is cleared!
  print('Updating status of event', eventID, 'to Cleared!')
  c.execute(update_clear_statement.format(eventID))
  # sleep for sometime
  time.sleep(random.random()*10)
  if j % 25 == 0:
    print('='*80)
    print("writing to db every 25 items")
    print('='*80)
    db.commit()

db.commit()
c.close()
db.close()


