import pprint

import pymysql
import datetime

def tuple_to_dict(tuple):
  # 将 MySQL 得到的查询结果对象，转换为便于 Python 处理的字典数据结构
  temp = {}
  keys = ['id', 'positive_agent', 'passive_agent', 'vehicle', 'time', 'location',
          'road_name', 'road_type', 'road_condition','speed_limit',
          'weather', 'temperature', 'light_condition', 'planned', 'type', 'severity', 'description']
  for i, k in enumerate(keys):
      temp[k] = None if not tuple[k] else tuple[k]
  temptime = temp['time'].strftime("%Y-%m-%d")
  temp['time'] = temptime
  if 'severe' in temp['severity']:
    temp['severity_text'] = 'SEVERE'
    temp['severity_style'] = 'badge badge-danger'
  elif 'moderate' in temp['severity']:
    temp['severity_text'] = 'MODERATE'
    temp['severity_style'] = 'badge badge-warning'
  elif 'minor' in temp['severity']:
    temp['severity_text'] = 'MINOR'
    temp['severity_style'] = 'badge badge-primary'
  else:
    temp['severity_text'] = 'NA'
    temp['delay_style'] = 'text-success'

  if temp['planned'] and 'notplanned' in temp['planned']:
    temp['planned_text'] = 'Not Planned'
    temp['planned_style'] = 'badge badge-info'
  else:
    temp['planned_text'] = temp['planned'].upper()
    temp['planned_style'] = 'badge badge-dark'
  return temp

select_by_id_statement = """
SELECT * FROM `incidents` WHERE `id` = {};
"""
select_newest_statement = """
SELECT * FROM `incidents` ORDER BY `id` DESC LIMIT {};
"""
filter_statement = """
SELECT * FROM `incidents` {} ORDER BY `id` DESC;
"""

select_total_count = """
SELECT COUNT(*) FROM `incidents`;
"""
select_severe_statement = """
SELECT COUNT(*) FROM `incidents` WHERE `severity` LIKE '%severe%';
"""
select_moderate_statement = """
SELECT COUNT(*) FROM `incidents` WHERE `severity` LIKE '%moderate%';
"""
select_minor_statement = """
SELECT COUNT(*) FROM `incidents` WHERE `severity` LIKE '%minor%';
"""

insert_incident_statement = """
INSERT INTO `incidents` (positive_agent, passive_agent, vehicle, time, location,
 road_name, road_type, road_condition, speed_limit,
 weather, temperature, light_condition, planned, type, severity, description) VALUES 
 (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s);
"""

class IncidentSelector(object):
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

  def select_by_id(self, id):
    self.c.execute(select_by_id_statement.format(id))
    result_id = self.c.fetchone()
    if result_id:
      return tuple_to_dict(result_id)
    else:
      return None
    return tuple_to_dict(result_id)

  def select_newest(self, limit=25):
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

  def select_total_count(self):
    self.c.execute(select_total_count)
    result_set = self.c.fetchone()
    if result_set:
      return result_set['COUNT(*)']
    else:
      return 0

  def select_severe_count(self):
    self.c.execute(select_severe_statement)
    result_set = self.c.fetchone()
    if result_set:
      # print(type(result_set))
      # print(result_set)
      return result_set['COUNT(*)']
    else:
      return 0

  def select_moderate_count(self):
    self.c.execute(select_moderate_statement)
    result_set = self.c.fetchone()
    if result_set:
      # print(type(result_set))
      # print(result_set)
      return result_set['COUNT(*)']
    else:
      return 0

  def select_minor_count(self):
    self.c.execute(select_minor_statement)
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

  def insert_incident(self, data: tuple):
    print('insert')
    print(data)
    self.c.execute(insert_incident_statement, data)
    self.db.commit()

if __name__ == '__main__':
  sql_selector = IncidentSelector()
  all = sql_selector.select_this_month_severe_count()
  # print(all)


