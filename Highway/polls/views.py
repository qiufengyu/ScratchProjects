from django.http import HttpResponseRedirect
from django.views import View
from django.shortcuts import render, reverse
from django.core.paginator import Paginator, EmptyPage, PageNotAnInteger

from polls.select_sql import SQLSelector
from polls.select_incidents import IncidentSelector

sql_selector = SQLSelector()
incident_selector = IncidentSelector()


def index(request):
  ctx = {}
  ctx['limits'] = 10  # select 10 latest news
  # 事件总数
  ctx['total'] = incident_selector.select_total_count()
  # 事件的严重程度计数
  ctx['severe'] = incident_selector.select_severe_count()
  ctx['moderate'] = incident_selector.select_moderate_count()
  ctx['minor'] = incident_selector.select_minor_count()
  # 最新的一些事件
  ctx['incidents'] = incident_selector.select_newest(limit=ctx['limits'])
  return render(request, 'index.html', context=ctx)

class AnalysisView(View):
  template_name = 'analysis/index.html'
  ctx = {}

  def get(self, request, *args, **kwargs):
    print("analysis get", request)
    return render(request, self.template_name, self.ctx)

  def post(self, request, *args, **kwargs):
    print("analysis post", request)
    return render(request, self.template_name, self.ctx)


def analysis(request):
  return render(request, 'analysis/index.html')

def about(request):
  return render(request, 'about.html')

def help(request):
  return render(request, 'help/index.html')

def detail(request):
  # 根据提供的事件 ID，返回对应的事件具体情况
  if request.method == 'POST':
    print(request.POST)
    eventID = request.POST['evtID']
    if eventID:
      event = incident_selector.select_by_id(int(eventID))
      if event:
        return render(request, 'incidents/detail.html', context=event)
  return render(request, 'incidents/detail.html', context=None)

def detail_with_id(request, event=None, *arg, **kwargs):
  print("detail_with_id")
  print(request)
  try:
    eventID = int(event)
  except (TypeError, ValueError, OverflowError):
    eventID = -1
  if request.method == 'POST' or request.method == 'GET':
    print(request.POST)
    if eventID:
      event = incident_selector.select_by_id(eventID)
      if event:
        return render(request, 'incidents/detail.html', context=event)
  return render(request, 'incidents/detail.html', context=None)

def add(request):
  # 根据用户的输入和选择，向数据库中插入事件
  if request.method == 'GET':
    print(request.GET)
  elif request.method == 'POST':
    print('add incidents')
    print(request.POST)
    # 对每一个元素的内容进行处理，没有填写的就保留默认值
    positive_agent = request.POST.get('positive_agent', None)
    if positive_agent == 'Default':
      positive_agent = 'NA'
    else:
      positive_agent = positive_agent.lower()

    passive_agent = request.POST.get('passive_agent', None)
    if passive_agent == 'Default':
      passive_agent = 'NA'
    else:
      passive_agent = passive_agent.lower()

    vehicle = request.POST.get('vehicle', None)
    if vehicle == 'Default':
      vehicle = 'NA'
    else:
      vehicle = vehicle.lower()

    time = request.POST.get('datetime', None)
    location = request.POST.get('location', None)

    road_name = request.POST.get('road_name', None)
    if road_name and len(road_name) > 0:
      road_name = road_name.lower()
    else:
      road_name = None

    road_type = request.POST.get('road_type', None)
    if road_type == 'Other':
      road_type = None
    else:
      road_type = road_type

    road_condition = request.POST.get('road_condition', None)
    if road_condition == 'Default':
      road_condition = 'normal'
    else:
      road_condition = road_condition.lower()

    speed_limit = request.POST.get('speed_limit', None)
    if speed_limit:
      try:
        speed_limit = int(speed_limit)
        print("Try")
      except:
        speed_limit = 0
    else:
      speed_limit = 0

    weather = request.POST.get('weather', None)
    if weather == 'Default':
      weather = 'normal'
    else:
      weather = weather.lower()

    temperature = request.POST.get('temperature', None)
    if temperature:
      try:
        temperature = float(temperature)
      except:
        temperature = None
    else:
      temperature = None

    light_condition = request.POST.get('light_condition', None)
    if light_condition == 'Default':
      light_condition = 'normal'
    else:
      light_condition = light_condition.lower()

    planned = 'unplanned'
    if 'plannedCheck' in request.POST:
      planned = request.POST.get('planned', None)
      if planned:
        planned = planned.lower()

    type = request.POST.get('type', None)
    if type == 'Default':
      type = None
    else:
      type = type.lower()

    severity = request.POST.get('severity', None)
    if severity == 'Default':
      severity = 'minor'
    else:
      severity = severity.lower()

    description = request.POST.get('description', None)
    if description:
      description = description.strip()

    # 所有内容处理完成，准备向数据库中插入
    data = (positive_agent, passive_agent, vehicle, time, location, road_name, road_type, road_condition, speed_limit,
              weather, temperature, light_condition, planned, type, severity, description)
    ctx = {}
    # 插入是否成功？
    try:
      incident_selector.insert_incident(data)
    except:
      ctx['insert'] = -1
    else:
      ctx['insert'] = 1
    return render(request, 'incidents/add.html', context=ctx)
  return render(request, 'incidents/add.html', context=None)

def filtering(request):
  # 根据用户的选择、输入查询对应的事件
  if request.method == 'GET':
    print(request.GET)
    if len(request.GET.dict().keys()) == 0:
      # the very first visit
      return render(request, 'incidents/filtering.html')
    query_conditions = []
    condition_values = []

    # 收集用户的查询条件，某些 factor 为空的话，默认是对这个 factor 不加以限制
    # agent
    positive_agent = request.GET.get('positive_agent', default=None)
    if positive_agent and len(positive_agent) > 0 and positive_agent != 'All':
      positive_agent_query = "(`positive_agent` LIKE '%" + positive_agent.lower() +"%')"
      query_conditions.append(positive_agent_query)
      condition_values.append('PositiveAgent: {}'.format(positive_agent))

    passive_agent = request.GET.get('passive_agent', default=None)
    if passive_agent and len(passive_agent) > 0 and passive_agent != 'All':
      passive_agent_query = "(`passive_agent` LIKE '%" + passive_agent.lower() + "%')"
      query_conditions.append(passive_agent_query)
      condition_values.append('PassiveAgent: {}'.format(passive_agent))

    vehicle = request.GET.get('vehicle', default=None)
    if vehicle and len(vehicle) > 0 and vehicle != 'All':
        vehicle_query = "(`vehicle` LIKE '%" + vehicle.lower() + "%')"
        query_conditions.append(vehicle_query)
        condition_values.append('Vehicle: {}'.format(vehicle))

    datetime = request.GET.get('datetime', default=None)
    if datetime and len(datetime) > 0:
      datetime_query = "(`time` = '" + str(datetime) + "')"
      query_conditions.append(datetime_query)
      condition_values.append("Date: {}".format(datetime))

    location = request.GET.get('location', None)
    if location and len(location) > 0:
      location_query = "(`location` LIKE '%" + location.lower() +"%')"
      query_conditions.append(location_query)
      condition_values.append("Location: {}".format(location))

    road_name = request.GET.get('road_name', None)
    if road_name and len(road_name) > 0:
      road_name_query = "(`road_name` LIKE '%" + road_name.lower() + "%')"
      query_conditions.append(road_name_query)
      condition_values.append('Road Name: {}'.format(road_name))

    # Road Type
    road_type = request.GET.get('road_type', default=None)
    if road_type and len(road_type) > 0 and road_type != 'All':
      road_type_query = "(`road_type` LIKE '%" + road_type.lower() + "%')"
      query_conditions.append(road_type_query)
      condition_values.append('Road Type: {}'.format(road_type))

    # temperature
    temperature = request.GET.get('temperature', default=None)
    if temperature and len(temperature) > 0:
      tem_int = 0
      try:
        tem_int = int(temperature)
      except:
        tem_int = 0
      temperature_query = "(`temperature` = {})".format(tem_int)
      query_conditions.append(temperature_query)
      condition_values.append('Temperature: {}℃'.format(tem_int))

    # weather
    weather = request.GET.get('weather', default=None)
    if weather and len(weather) > 0 and weather != 'All':
      weather_query = "(`weather` LIKE '%" + weather.lower() + "%')"
      query_conditions.append(weather_query)
      condition_values.append('Weather: {}'.format(weather))

    # road condition
    road_condition = request.GET.get('road_condition', default=None)
    if road_condition and len(road_condition) > 0 and road_condition != 'All':
      road_condition_query = "(`road_condition` LIKE '%" + road_condition.lower() + "%')"
      query_conditions.append(road_condition_query)
      condition_values.append('Road Condition: {}'.format(road_condition))

    # speed limit
    speed_limit = request.GET.get('speed_limit', default=None)
    if speed_limit and len(speed_limit) > 0:
      speed_limit_int = 0
      try:
        speed_limit_int = int(speed_limit)
      except:
        speed_limit_int = 0
      speed_limit_query = "(`speed_limit` = {})".format(speed_limit_int)
      query_conditions.append(speed_limit_query)
      condition_values.append('Speed Limit: {} km/h'.format(speed_limit))

    # facilities
    type = request.GET.get('type', default=None)
    if type and len(type) > 0 and type != 'All':
      type_query = "(`type` LIKE '%" + type.lower() + "%')"
      query_conditions.append(type_query)
      condition_values.append('Type: {}'.format(type))

    # light condition
    light_condition = request.GET.get('light_condition', default=None)
    if light_condition and len(light_condition) > 0 and light_condition != 'All':
      light_condition_query = "(`light_condition` LIKE '%" + light_condition.lower() + "%')"
      query_conditions.append(light_condition_query)
      condition_values.append('Light Condition: {}'.format(light_condition))

    # damaged
    severity = request.GET.get('severity', default=None)
    if severity and len(severity) > 0 and severity != 'All':
      severity_query = "(`severity` LIKE '%" + severity.lower() + "%')"
      query_conditions.append(severity_query)
      condition_values.append('Severity: {}'.format(severity))

    # planned
    if 'plannedCheck' in request.GET:
      planned = request.GET.get('planned')
      if planned:
        if planned and len(planned) > 0 and planned != 'All':
          planned_query = "(`planned` LIKE '%" + planned.lower() + "%')"
          query_conditions.append(planned_query)
          condition_values.append('Planned: {}'.format(planned))
        else:
          planned_query = "(`planned` NOT LIKE '%unplanned%')"
          query_conditions.append(planned_query)
          condition_values.append('Planned: Yes')
    else:
      planned_query = "(`planned` LIKE '%unplanned%')"
      query_conditions.append(planned_query)
      condition_values.append('Planned: No')

    # 从 conditions 构造 SQL 查询语句
    if len(query_conditions) >= 1:
      conditions = 'WHERE ( ' + ' AND '.join(query_conditions) +' )'
    else:
      conditions = ''
    # print(conditions)

    # 向数据库中提交查询请求，得到返回结果
    filter_results = incident_selector.filter_by_condition(conditions)
    total_count = len(filter_results) if filter_results else 0
    # 如果数量多的话，分页显示
    page = int(request.GET.get('page', default=1))
    incidents_per_page = request.GET.get('perpage', default='10')
    if filter_results:
      paginator = Paginator(filter_results, int(incidents_per_page))
      try:
        incidents = paginator.page(page)
      except PageNotAnInteger:
        incidents = paginator.page(1)
      except EmptyPage:
        incidents = paginator.page(paginator.num_pages)
      # return HttpResponseRedirect(reverse('filtering'), {'incidents': incidents, 'paginator': paginator, 'total_count': total_count})
      return render(request, 'incidents/filtering.html', {'incidents': incidents, 'paginator': paginator, 'total_count': total_count, 'condition_values': condition_values})
  return render(request, 'incidents/filtering.html')
