import pprint

import pymysql
import datetime


def tuple_to_dict(tuple):
  temp = {}
  keys = ['guid', 'author', 'category1', 'category2',
  'location', 'lane_closures', 'reason', 'status', 'period',
  'link', 'pub_date', 'title', 'reference', 'road', 'region', 'county',
  'latitude', 'longitude',
  'overall_start', 'overall_end', 'event_start', 'event_end', 'schedule', 'lanes_closed']
  for i, k in enumerate(keys):
      temp[k] = None if tuple[k] == 'None' else tuple[k]
      if isinstance(tuple[k], datetime.datetime):
        temp[k] = tuple[k].strftime("%Y-%m-%d, %H:%M:%S")
  if 'Active' in temp['status']:
    temp['status'] = 'ACTIVE'
    temp['status_style'] = 'badge badge-danger'
  elif 'Pending' in temp['status']:
    temp['status'] = 'PENDING'
    temp['status_style'] = 'badge badge-warning'
  elif 'Cleared' in temp['status']:
    temp['status'] = 'CLEARED'
    temp['status_style'] = 'badge badge-success'
  else:
    temp['status'] = 'NA'
    temp['status_style'] = 'badge badge-info'

  if 'Severe' in temp['category2']:
    temp['category2'] = 'Severe'
    temp['delay_style'] = 'text-danger'
  elif 'Moderate' in temp['category2']:
    temp['category2'] = 'Moderate'
    temp['delay_style'] = 'text-warning'
  elif 'Minor' in temp['category2']:
    temp['category2'] = 'Minor'
    temp['delay_style'] = 'text-primary'
  else:
    temp['category2'] = 'No Delay'
    temp['delay_style'] = 'text-success'
  return temp



select_by_guid_statement = """
SELECT * FROM `highways` WHERE `guid` = {}
"""
select_newest_statement = """
SELECT * FROM `highways` ORDER BY `pub_date` DESC LIMIT {}
"""
select_this_month_count_statement = """
SELECT COUNT(*) FROM `highways` WHERE (`event_start` >= '{}' AND `event_start` <= '{}')
"""
select_this_month_accidents_statement = """
SELECT COUNT(*) FROM `highways` WHERE (`event_start` >= '{}' AND `event_start` <= '{}') AND (`category1` LIKE '%Accident%' OR `category1` LIKE '%Congestion%')
"""
select_this_month_brokens_statement = """
SELECT COUNT(*) FROM `highways` WHERE (`event_start` >= '{}' AND `event_start` <= '{}') AND (`category1` LIKE '%Broken%' OR `category1` LIKE 'Vehicle%')
"""
select_this_month_roadworks_statement = """
SELECT COUNT(*) FROM `highways` WHERE (`event_start` >= '{}' AND `event_start` <= '{}') AND (`category1` LIKE '%Road%')
"""
select_this_month_severe_statement = """
SELECT COUNT(*) FROM `highways` WHERE (`event_start` >= '{}' AND `event_start` <= '{}') AND (`category2` LIKE '%Severe%')
"""
select_this_month_moderate_statement = """
SELECT COUNT(*) FROM `highways` WHERE (`event_start` >= '{}' AND `event_start` <= '{}') AND (`category2` LIKE '%Moderate%')
"""
select_this_month_minor_statement = """
SELECT COUNT(*) FROM `highways` WHERE (`event_start` >= '{}' AND `event_start` <= '{}') AND (`category2` LIKE '%Minor%')
"""
select_this_month_nodelay_statement = """
SELECT COUNT(*) FROM `highways` WHERE (`event_start` >= '{}' AND `event_start` <= '{}') AND (`category2` LIKE '%No Delay%')
"""
filter_statement = """
SELECT * FROM `highways` {} ORDER BY `pub_date` DESC, `guid`;
"""

class SQLSelector(object):
  # Connecting to MySQL
  def __init__(self):
    self.db = pymysql.connect(
      host='localhost',
      user='root',
      password='root',
      db='highways',
      charset='utf8mb4',
      cursorclass=pymysql.cursors.DictCursor)
    self.c = self.db.cursor()

  def select_by_guid(self, guid):
    self.c.execute(select_by_guid_statement.format(guid))
    result_guid = self.c.fetchone()
    if result_guid:
      return tuple_to_dict(result_guid)
    else:
      return None
    return tuple_to_dict(result_guid)

  def select_newest(self, limit=10):
    self.c.execute(select_newest_statement.format(limit))
    result_newest = self.c.fetchall()
    result_list = []
    if result_newest:
      # print(type(result_newest))
      for r in result_newest:
        result_list.append(tuple_to_dict(r))
    else:
      return None
    return result_list

  def select_this_month_count(self):
    this_year = datetime.datetime.utcnow().year
    this_month = datetime.datetime.utcnow().month
    start_datetime = datetime.datetime(year=this_year, month=this_month, day=1)
    end_datetime = datetime.datetime(year=(this_year+this_month // 12), month=((this_month+1)%12), day=1)
    # print(start_datetime)
    # print(end_datetime)
    start_datetime_string = start_datetime.strftime("%Y-%m-%d %H:%M:%S")
    end_datetime_string = end_datetime.strftime("%Y-%m-%d %H:%M:%S")
    self.c.execute(select_this_month_count_statement.format(start_datetime_string, end_datetime_string))
    result_set = self.c.fetchone()
    print(result_set)
    if result_set:
      # print(type(result_set))
      # print(result_set)
      return result_set['COUNT(*)']
    else:
      return 0

  def select_this_month_accidents_count(self):
    this_year = datetime.datetime.utcnow().year
    this_month = datetime.datetime.utcnow().month
    start_datetime = datetime.datetime(year=this_year, month=this_month, day=1)
    end_datetime = datetime.datetime(year=(this_year+this_month // 12), month=((this_month+1)%12), day=1)
    # print(start_datetime)
    # print(end_datetime)
    start_datetime_string = start_datetime.strftime("%Y-%m-%d %H:%M:%S")
    end_datetime_string = end_datetime.strftime("%Y-%m-%d %H:%M:%S")
    self.c.execute(select_this_month_accidents_statement.format(start_datetime_string, end_datetime_string))
    result_set = self.c.fetchone()
    if result_set:
      # print(type(result_set))
      # print(result_set)
      return result_set['COUNT(*)']
    else:
      return 0

  def select_this_month_brokens_count(self):
    this_year = datetime.datetime.utcnow().year
    this_month = datetime.datetime.utcnow().month
    start_datetime = datetime.datetime(year=this_year, month=this_month, day=1)
    end_datetime = datetime.datetime(year=(this_year + this_month // 12), month=((this_month + 1) % 12), day=1)
    # print(start_datetime)
    # print(end_datetime)
    start_datetime_string = start_datetime.strftime("%Y-%m-%d %H:%M:%S")
    end_datetime_string = end_datetime.strftime("%Y-%m-%d %H:%M:%S")
    self.c.execute(select_this_month_brokens_statement.format(start_datetime_string, end_datetime_string))
    result_set = self.c.fetchone()
    if result_set:
      # print(type(result_set))
      # print(result_set)
      return result_set['COUNT(*)']
    else:
      return 0

  def select_this_month_roadworks_count(self):
    this_year = datetime.datetime.utcnow().year
    this_month = datetime.datetime.utcnow().month
    start_datetime = datetime.datetime(year=this_year, month=this_month, day=1)
    end_datetime = datetime.datetime(year=(this_year + this_month // 12), month=((this_month + 1) % 12), day=1)
    # print(start_datetime)
    # print(end_datetime)
    start_datetime_string = start_datetime.strftime("%Y-%m-%d %H:%M:%S")
    end_datetime_string = end_datetime.strftime("%Y-%m-%d %H:%M:%S")
    self.c.execute(select_this_month_roadworks_statement.format(start_datetime_string, end_datetime_string))
    result_set = self.c.fetchone()
    if result_set:
      # print(type(result_set))
      # print(result_set)
      return result_set['COUNT(*)']
    else:
      return 0

  def select_this_month_others_count(self):
    total = self.select_this_month_count()
    accidents = self.select_this_month_accidents_count()
    brokens = self.select_this_month_brokens_count()
    roadworks = self.select_this_month_roadworks_count()
    others = total - accidents - brokens - roadworks
    return others if others > 0 else 0

  def select_this_month_severe_count(self):
    this_year = datetime.datetime.utcnow().year
    this_month = datetime.datetime.utcnow().month
    start_datetime = datetime.datetime(year=this_year, month=this_month, day=1)
    end_datetime = datetime.datetime(year=(this_year + this_month // 12), month=((this_month + 1) % 12), day=1)
    # print(start_datetime)
    # print(end_datetime)
    start_datetime_string = start_datetime.strftime("%Y-%m-%d %H:%M:%S")
    end_datetime_string = end_datetime.strftime("%Y-%m-%d %H:%M:%S")
    self.c.execute(select_this_month_severe_statement.format(start_datetime_string, end_datetime_string))
    result_set = self.c.fetchone()
    if result_set:
      # print(type(result_set))
      # print(result_set)
      return result_set['COUNT(*)']
    else:
      return 0

  def select_this_month_moderate_count(self):
    this_year = datetime.datetime.utcnow().year
    this_month = datetime.datetime.utcnow().month
    start_datetime = datetime.datetime(year=this_year, month=this_month, day=1)
    end_datetime = datetime.datetime(year=(this_year + this_month // 12), month=((this_month + 1) % 12), day=1)
    # print(start_datetime)
    # print(end_datetime)
    start_datetime_string = start_datetime.strftime("%Y-%m-%d %H:%M:%S")
    end_datetime_string = end_datetime.strftime("%Y-%m-%d %H:%M:%S")
    self.c.execute(select_this_month_moderate_statement.format(start_datetime_string, end_datetime_string))
    result_set = self.c.fetchone()
    if result_set:
      # print(type(result_set))
      # print(result_set)
      return result_set['COUNT(*)']
    else:
      return 0

  def select_this_month_minor_count(self):
    this_year = datetime.datetime.utcnow().year
    this_month = datetime.datetime.utcnow().month
    start_datetime = datetime.datetime(year=this_year, month=this_month, day=1)
    end_datetime = datetime.datetime(year=(this_year + this_month // 12), month=((this_month + 1) % 12), day=1)
    # print(start_datetime)
    # print(end_datetime)
    start_datetime_string = start_datetime.strftime("%Y-%m-%d %H:%M:%S")
    end_datetime_string = end_datetime.strftime("%Y-%m-%d %H:%M:%S")
    self.c.execute(select_this_month_minor_statement.format(start_datetime_string, end_datetime_string))
    result_set = self.c.fetchone()
    if result_set:
      # print(type(result_set))
      # print(result_set)
      return result_set['COUNT(*)']
    else:
      return 0

  def select_this_month_nodelay_count(self):
    this_year = datetime.datetime.utcnow().year
    this_month = datetime.datetime.utcnow().month
    start_datetime = datetime.datetime(year=this_year, month=this_month, day=1)
    end_datetime = datetime.datetime(year=(this_year + this_month // 12), month=((this_month + 1) % 12), day=1)
    # print(start_datetime)
    # print(end_datetime)
    start_datetime_string = start_datetime.strftime("%Y-%m-%d %H:%M:%S")
    end_datetime_string = end_datetime.strftime("%Y-%m-%d %H:%M:%S")
    self.c.execute(select_this_month_nodelay_statement.format(start_datetime_string, end_datetime_string))
    result_set = self.c.fetchone()
    if result_set:
      # print(type(result_set))
      # print(result_set)
      return result_set['COUNT(*)']
    else:
      return 0

  def filter_by_condition(self, conditions):
    print('executing')
    print(filter_statement.format(conditions))
    self.c.execute(filter_statement.format(conditions))
    result_tuples = self.c.fetchall()
    if result_tuples:
      result_list = []
      for result in result_tuples:
        result_list.append(tuple_to_dict(result))
      return result_list
    else:
      return None

if __name__ == '__main__':
  sql_selector = SQLSelector()
  all = sql_selector.select_this_month_severe_count()
  # print(all)


