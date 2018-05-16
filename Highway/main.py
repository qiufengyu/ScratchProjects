import re
import pprint
import urllib.request

from dateutil.parser import *
from dateutil.tz import *

import pymysql

from bs4 import BeautifulSoup


def datetime_to_utc(datestring):
  date = parse(datestring)
  """Returns date in UTC w/o tzinfo"""
  date_utc = date.astimezone(gettz('UTC')).replace(tzinfo=None) if date.tzinfo else date
  return date_utc.strftime("%Y-%m-%d %H:%M:%S")

# Connecting to MySQL
db = pymysql.connect(host='localhost',
                     user='root',
                     password='root',
                     db='highways',
                     charset='utf8mb4',
                     cursorclass=pymysql.cursors.DictCursor)

c = db.cursor()

# Open file
req = urllib.request.Request(url='http://m.highways.gov.uk/feeds/rss/AllEvents.xml')
response = urllib.request.urlopen(req, timeout=30)
response_read = response.read()
soup = BeautifulSoup(response_read, 'lxml')
# soup = BeautifulSoup(open('events.xml'), 'lxml')
# Extracting
items = soup.find_all('item')

insert_statement = """
INSERT INTO `highways` (guid, author, category1, category2, 
location, lane_closures, reason, status, period, schedule, lanes_closed,
link, pub_date, title, reference, road, region, county, 
latitude, longitude, 
overall_start, overall_end, event_start, event_end) 
VALUES ({guid}, '{author}', '{category1}', '{category2}', 
'{location}', '{lane_closures}', '{reason}', '{status}', '{period}', '{schedule}', '{lanes_closed}',
'{link}', '{pub_date}', '{title}', '{reference}', '{road}', '{region}', '{county}',
{latitude}, {longitude}, 
'{overall_start}', '{overall_end}', '{event_start}', '{event_end}');
"""

delete_statement = """
DELETE FROM `highways` WHERE `guid` = {};
"""

total = len(items)
j = 0
for i in items:
  item = {}
  guid = i.find('guid')
  item['guid'] = int(guid.text[6:]) if guid else -1
  title = i.find('title')
  item['title'] = title.text.strip() if title else None
  author = i.find('author')
  item['author'] = author.text.strip() if author else None
  categories = i.find_all('category')
  item['category1'] = None
  item['category2'] = None
  category_index = 1
  for category in categories:
    item['category{}'.format(category_index)] = category.text.strip()
    category_index += 1
  description = i.find('description')
  if description:
    # location, lane_closures, reason, status, period, schedule, lanes_closed
    item['location'] = None
    item['lane_closures'] = None
    item['reason'] = None
    item['status'] = None
    item['period'] = None
    item['schedule'] = None
    item['lanes_closed'] = None
    des_text = description.text
    des_list = [x.strip() for x in des_text.split('.')]
    for des in des_list:
      if re.search('location', des, re.IGNORECASE):
        if item['location']:
          item['location'] += ('. ' + des[des.index(':')+1:].strip())
        else:
          item['location'] = des[des.index(':')+1:].strip()
      elif re.search('lane closures', des, re.IGNORECASE):
        if item['lane_closures']:
          item['lane_closures'] += ('. ' + des[des.index(':')+1:].strip())
        else:
          item['lane_closures'] = des[des.index(':')+1:].strip()
      elif re.search('reason', des, re.IGNORECASE):
        if item['reason']:
          item['reason'] += ('. ' + des[des.index(':')+1:].strip())
        else:
          item['reason'] = des[des.index(':')+1:].strip()
      elif re.search('status', des, re.IGNORECASE):
        if item['status']:
          item['status'] += ('. ' + des[des.index(':')+1:].strip())
        else:
          item['status'] = des[des.index(':')+1:].strip()
      elif re.search('period', des, re.IGNORECASE):
        if item['period']:
          item['period'] += ('. ' + des[des.index(':')+1:].strip())
        else:
          item['period'] = des[des.index(':')+1:].strip()
      elif re.search('schedule', des, re.IGNORECASE):
        if item['schedule']:
          item['schedule'] += ('. ' + des[des.index(':')+1:].strip())
        else:
          item['schedule'] = des[des.index(':')+1:].strip()
      elif re.search('lanes closed', des, re.IGNORECASE):
        if item['lanes_closed']:
          item['lanes_closed'] += ('. ' + des[des.index(':')+1:].strip())
        else:
          item['lanes_closed'] = des[des.index(':')+1:].strip()
  link = i.find('link')
  item['link'] = link.text.strip() if link else None
  if len(item['link']) < 1:
    item['link'] = 'http://www.trafficengland.com/?evtID='+str(item['guid'])
  pub_date = i.find('pubdate')
  item['pub_date'] = datetime_to_utc(pub_date.text.strip()) if pub_date else None
  reference = i.find('reference')
  item['reference'] = reference.text.strip() if reference else None
  road = i.find('road')
  item['road'] = road.text.strip() if road else None
  region = i.find('region')
  item['region'] = region.text.strip() if region else None
  county = i.find('county')
  item['county'] = county.text.strip() if county else None
  latitude = i.find('latitude')
  item['latitude'] = float(latitude.text.strip()) if latitude else None
  longitude = i.find('longitude')
  item['longitude'] = float(longitude.text.strip()) if longitude else None
  overall_start = i.find('overallstart')
  item['overall_start'] = datetime_to_utc(overall_start.text) if overall_start else None
  overall_end = i.find('overallend')
  item['overall_end'] = datetime_to_utc(overall_end.text) if overall_end else None
  event_start = i.find('eventstart')
  item['event_start'] = datetime_to_utc(event_start.text) if event_start else None
  event_end = i.find('eventend')
  item['event_end'] = datetime_to_utc(event_end.text) if event_end else None
  # print('='*80)
  # To check the item
  # pprint.pprint(item)
  # print(insert_statement.format(**item))
  try:
    c.execute(insert_statement.format(**item))
  except:
    # print("updating the information...")
    c.execute(delete_statement.format(item['guid']))
    c.execute(insert_statement.format(**item))
  j += 1
  if j % 100 == 0:
    print("{}/{} items inserted.".format(j, total))

db.commit()
c.close()
db.close()

print('In all,', len(items), 'items are inserted.')
