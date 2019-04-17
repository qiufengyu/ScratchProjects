from django.core.paginator import Paginator
from django.http import HttpResponseServerError, HttpResponse, JsonResponse
from django.shortcuts import render, redirect
from django.views.decorators.csrf import csrf_exempt

from app.mysqldb import MySQLDB

# 创建数据库连接对象
db = MySQLDB('localhost', 'root', 'root', 'messages')

# 获取网页内容的一些常量，因为下面很多都要用到，写成一个函数
def get_info(request):
  ctx = {}
  login = request.session.get('is_login', None)
  if login is None:
    return None
  username = request.session.get('username', None)
  if username is None:
    return None
  ctx["username"] = username
  unreads = db.get_user_message_count(username, 1)
  spams = db.get_user_message_count(username, 4)
  notify_counts = unreads + spams
  ctx["notify_counts"] = notify_counts
  ctx["unreads"] = unreads
  ctx["spams"] = spams
  return ctx

def login(request):
  ctx = {}
  if request.session.get('is_login', None):
    # 登录状态不允许重复登陆
    return redirect("/")
  if request.method == "POST":
    username = request.POST['username'].strip()
    password1 = request.POST['password'].strip()
    v_password = db.valid_user(username)
    if v_password and password1 == v_password:
      request.session['username'] = username
      request.session['is_login'] = 1
      print("{} Login OK".format(username))
      return redirect("/")
    else:
      ctx['message'] = "密码不正确或用户名不存在！"
      print("{} Login Failed".format(username))
    return render(request, 'login.html', context=ctx)
  return render(request, 'login.html')

def logout(request):
  uname = request.session['username']
  del request.session['is_login']
  del request.session['username']
  print("{} Logout".format(uname))
  return render(request, 'login.html')

def register(request):
  ctx = {}
  if request.session.get('is_login', None):
    # 登录状态不允许注册
    return redirect("/")
  if request.method == "POST":
    username = request.POST['username'].strip()
    password1 = request.POST['password1'].strip()
    password2 = request.POST['password2'].strip()
    nickname = request.POST['nickname'].strip()
    if password1 == password2:
      if db.check_user(username, password1, nickname):
        request.session['username'] = username
        request.session['is_login'] = 1
        print("New registered {}".format(username))
        return redirect("/")
      ctx['message'] = "用户名已存在！"
    else:
      ctx['message'] = "密码不符！"
    print("{} failed to register".format(username))
    return render(request, 'register.html', context=ctx)
  return render(request, 'register.html')

def index(request):
  # 从对应的数据库中获取数据
  # 0. 从全区的邮件中，获取收件人为自己，且状态为 0 的邮件
  username = request.session.get('username', None)
  if username is None:
    return render(request, 'login.html')
  newcount = db.get_new_messages(username)
  print("{} get {} new messages.".format(username, newcount))
  # 获取一些用户相关的信息
  ctx = get_info(request)
  if ctx is None:
    return render(request, 'login.html')
  _messages = db.get_user_inbox(username)
  message_list = []
  if _messages is not None:
    delay = 0
    for m in _messages:
      message = {}
      delay += 1
      message["identify"] = int(m[0])
      message["sender"] = str(m[1])
      message["abstract"] = str(m[3])
      message["dt"] = str(m[4].month) + " 月 " + str(m[4].day) + " 日"
      message["status"] = int(m[5])
      message["delay"] = delay
      message_list.append(message)
  # 每 6 项内容为一页
  paginator = Paginator(message_list, 6)
  page = request.GET.get("page", 1)
  messages = paginator.get_page(page)
  ctx["messages"] = messages
  return render(request, 'index.html', context=ctx)

def message(request):
  return render(request, 'index.html')

def message_id(request, mid=None, *arg, **kwargs):
  ctx = get_info(request)
  if ctx is None:
    return render(request, 'login.html')
  username = ctx["username"]
  try:
    midint = int(mid)
  except (TypeError, ValueError, OverflowError):
    midint = -1
  if request.method == 'POST' or request.method == 'GET':
    if midint > 0:
      m = db.get_user_message_by_id(username, midint)
      if m:
        status = int(m[5])
        ctx["identify"] = int(m[0])
        ctx["sender"] = str(m[1])
        ctx["receiver"] = str(m[2])
        # 根据状态码，计算所属类别
        prefix = "收件箱"
        if status == 3:
          prefix = "已发信息"
        elif status == 4:
          prefix = "垃圾短信"
        elif status == 5:
          prefix = "已删除 - 收件箱"
        elif status == 6:
          prefix = "已删除 - 发件箱"
        ctx["prefix"] = prefix
        ctx["subject"] = str(m[3])
        ctx["dt"] = str(m[4].month) + " 月 " + str(m[4].day) + " 日"
        ctx["sendorreceive"] = 1 if int(m[5]) == 3 else 0
        if status == 1:
          # 未读信息自动更新为已读信息
          db.update_message_status(username, midint, 2)
          ctx["status"] = 2
        else:
          ctx["status"] = status
        return render(request, 'messagecontent.html', context=ctx)
  return render(request, 'index.html')

def sendmessage(request):
  ctx = get_info(request)
  if ctx is None:
    return redirect("/")
  username = ctx["username"]
  if request.method == 'POST':
    toreceiver = str(request.POST['toreceiver']).strip().lower()
    subject = str(request.POST['subject']).strip()
    db.send_a_message(username, toreceiver, subject)
    ctx["message"] = "已将消息发送至 " + toreceiver +" ！（如果该用户存在）"
    print("{} send a message to {}".format(username, toreceiver))
    return render(request, 'sendmessage.html', context=ctx)
  return render(request, 'sendmessage.html', context=ctx)

def outbox(request):
  ctx = get_info(request)
  if ctx is None:
    return redirect("/")
  username = ctx["username"]
  _messages = db.get_user_outbox(username)
  message_list = []
  if _messages is not None:
    delay = 0
    for m in _messages:
      message = {}
      delay += 1
      message["delay"] = delay
      message["identify"] = int(m[0])
      message["sender"] = str(m[1])
      message["receiver"] = str(m[2])
      message["abstract"] = str(m[3])
      message["dt"] = str(m[4].month) + " 月 " + str(m[4].day) + " 日"
      message["status"] = int(m[5])
      message_list.append(message)
  paginator = Paginator(message_list, 6)
  page = request.GET.get("page", 1)
  messages = paginator.get_page(page)
  ctx["messages"] = messages
  return render(request, 'outbox.html', context=ctx)

def deletebox(request):
  ctx = get_info(request)
  if ctx is None:
    return redirect("/")
  username = ctx["username"]
  _messages = db.get_user_deletebox(username)
  message_list = []
  if _messages is not None:
    delay = 0
    for m in _messages:
      message = {}
      delay += 1
      message["delay"] = delay
      message["identify"] = int(m[0])
      message["sender"] = str(m[1])
      message["receiver"] = str(m[2])
      message["abstract"] = str(m[3])
      message["dt"] = str(m[4].month) + " 月 " + str(m[4].day) + " 日"
      message["status"] = int(m[5])
      message_list.append(message)
  paginator = Paginator(message_list, 6)
  page = request.GET.get("page", 1)
  messages = paginator.get_page(page)
  ctx["messages"] = messages
  return render(request, 'deletebox.html', context=ctx)

def spambox(request):
  ctx = get_info(request)
  if ctx is None:
    return redirect("/")
  username = ctx["username"]
  _messages = db.get_user_spambox(username)
  message_list = []
  if _messages is not None:
    delay = 0
    for m in _messages:
      message = {}
      delay += 1
      message["delay"] = delay
      message["identify"] = int(m[0])
      message["sender"] = str(m[1])
      message["receiver"] = str(m[2])
      message["abstract"] = str(m[3])
      message["dt"] = str(m[4].month) + " 月 " + str(m[4].day) + " 日"
      message["status"] = int(m[5])
      message_list.append(message)
  paginator = Paginator(message_list, 6)
  page = request.GET.get("page", 1)
  messages = paginator.get_page(page)
  ctx["messages"] = messages
  return render(request, 'spambox.html', context=ctx)

# 以下关于界面交互，信息的交互处理
@csrf_exempt
def markspam(request):
  if request.method == 'POST':
    if request.POST["username"] and request.POST["mid"]:
      username = str(request.POST["username"]).strip()
      mid = int(request.POST["mid"])
      try:
        db.update_message_status(username, mid, 4)
        print("{} marked message-{} spam".format(username, mid))
        return JsonResponse({"info": "OK"})
      except:
        print("{} failed to mark message-{} spam".format(username, mid))
        return HttpResponseServerError("Error", content_type="text/plain")
  return HttpResponseServerError("Error", content_type="text/plain")

@csrf_exempt
def markdelete(request):
  if request.method == 'POST':
    if request.POST["username"] and request.POST["mid"]:
      username = str(request.POST["username"]).strip()
      mid = int(request.POST["mid"])
      try:
        db.update_message_status(username, mid, 5)
        print("{} deleted message-{}".format(username, mid))
        return JsonResponse({"info": "OK"})
      except:
        print("{} failed to delete message-{}".format(username, mid))
        return HttpResponseServerError("Error", content_type="text/plain")
  return HttpResponseServerError("Error", content_type="text/plain")

@csrf_exempt
def markdeletesendorreceive(request):
  if request.method == 'POST':
    if request.POST["username"] and request.POST["mid"]:
      username = str(request.POST["username"]).strip()
      mid = int(request.POST["mid"])
      sor = int(request.POST["sendorreceive"])
      try:
        db.update_message_status(username, mid, 5+sor)
        print("{} deleted message-{}".format(username, mid))
        return JsonResponse({"info": "OK"})
      except:
        print("{} failed to delete message-{}".format(username, mid))
        return HttpResponseServerError("Error", content_type="text/plain")
  return HttpResponseServerError("Error", content_type="text/plain")

@csrf_exempt
def markdeletesend(request):
  if request.method == 'POST':
    if request.POST["username"] and request.POST["mid"]:
      username = str(request.POST["username"]).strip()
      mid = int(request.POST["mid"])
      try:
        db.update_message_status(username, mid, 6)
        print("{} deleted message-{}".format(username, mid))
        return JsonResponse({"info": "OK"})
      except:
        print("{} failed to delete message-{}".format(username, mid))
        return HttpResponseServerError("Error", content_type="text/plain")
  return HttpResponseServerError("Error", content_type="text/plain")

@csrf_exempt
def markread(request):
  if request.method == 'POST':
    if request.POST["username"] and request.POST["mid"]:
      username = str(request.POST["username"]).strip()
      mid = int(request.POST["mid"])
      try:
        db.update_message_status(username, mid, 2)
        print("{} marked message-{} read".format(username, mid))
        return JsonResponse({"info": "OK"})
      except:
        print("{} failed to mark message-{} read".format(username, mid))
        return HttpResponseServerError("Error", content_type="text/plain")
  return HttpResponseServerError("Error", content_type="text/plain")

@csrf_exempt
def restorespam(request):
  if request.method == 'POST':
    if request.POST["username"] and request.POST["mid"]:
      username = str(request.POST["username"]).strip()
      mid = int(request.POST["mid"])
      try:
        db.update_message_status(username, mid, 2)
        print("{} restored message-{} from spam".format(username, mid))
        return JsonResponse({"info": "OK"})
      except:
        print("{} failed to restore message-{} from spam".format(username, mid))
        return HttpResponseServerError("Error", content_type="text/plain")
  return HttpResponseServerError("Error", content_type="text/plain")

@csrf_exempt
def restoredelete(request):
  if request.method == 'POST':
    if request.POST["username"] and request.POST["mid"]:
      username = str(request.POST["username"]).strip()
      mid = int(request.POST["mid"])
      try:
        oldm = db.get_user_message_by_id(username, mid)
        if oldm is not None:
          status = int(oldm[5])
          if status == 6:
            db.update_message_status(username, mid, 3)
            print("{} restored sended message-{} from delete".format(username, mid))
          else:
            db.update_message_status(username, mid, 2)
            print("{} restored received message-{} from delete".format(username, mid))
          return JsonResponse({"info": "OK"})
      except:
        print("{} failed to restore message-{} from delete".format(username, mid))
        return HttpResponseServerError("Error", content_type="text/plain")
  return HttpResponseServerError("Error", content_type="text/plain")

@csrf_exempt
def deleteforever(request):
  if request.method == 'POST':
    if request.POST["username"] and request.POST["mid"]:
      username = str(request.POST["username"]).strip()
      mid = int(request.POST["mid"])
      try:
        db.delete_user_message(username, mid)
        print("{} delete message-{} forever".format(username, mid))
        return JsonResponse({"info": "OK"})
      except:
        print("{} failed to delete message-{} forever".format(username, mid))
        return HttpResponseServerError("Error", content_type="text/plain")
  return HttpResponseServerError("Error", content_type="text/plain")

# 以下关于增加黑名单用户
def config(request):
  ctx = get_info(request)
  if ctx is None:
    return redirect("/")
  username = ctx["username"]
  words = db.get_user_dict_list(username)
  wordlist = list(words)
  ctx["wordlist"] = wordlist
  return render(request, 'config.html', context=ctx)

def configadd(request):
  if request.method == 'POST':
    username = request.session.get('username', None)
    newword = str(request.POST["newword"]).strip()
    if username and newword:
      # 判断是否已经存在
      current_dict = db.get_user_dict_set(username)
      if newword not in current_dict:
        if db.insert_user_dict(username, newword):
          print("{} added a word".format(username))
          return redirect("config")
  return redirect("config")

def configdelete(request):
  if request.method == 'POST':
    username = request.POST.get("username", None)
    wordid = request.POST.get('wid', None)
    if username and wordid:
      if db.delete_user_dict(username, int(wordid)):
        print("{} deleted a word".format(username))
        return JsonResponse({"info": "OK"})
  return JsonResponse({"info": "Error"})
