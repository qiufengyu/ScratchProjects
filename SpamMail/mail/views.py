import jieba
from django.core.paginator import Paginator
from django.http import HttpResponseServerError, HttpResponse, JsonResponse
from django.shortcuts import render, redirect
from django.views.decorators.csrf import csrf_exempt

from mail.mysqldb import MySQLDB

import logging

# Create your views here.
db = MySQLDB('localhost', 'root', 'root', 'mail')
logger = logging.getLogger(__name__)

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
  unreads = db.get_user_mail_count(username, 1)
  spams = db.get_user_mail_count(username, 4)
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
      logger.info("{} Login OK".format(username))
      return redirect("/")
    else:
      ctx['message'] = "密码不正确或用户名不存在！"
      logger.warning("{} Login Failed".format(username))
    return render(request, 'login.html', context=ctx)
  return render(request, 'login.html')

def logout(request):
  uname = request.session['username']
  del request.session['is_login']
  del request.session['username']
  logger.info("{} Logout".format(uname))
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
        logger.info("New registered {}".format(username))
        return redirect("/")
      ctx['message'] = "用户名已存在！"
    else:
      ctx['message'] = "密码不符！"
    logger.warning("{} failed to register".format(username))
    return render(request, 'register.html', context=ctx)
  return render(request, 'register.html')

def index(request):
  # 从对应的数据库中获取数据
  # 0. 从全区的邮件中，获取收件人为自己，且状态为 0 的邮件
  username = request.session.get('username', None)
  if username is None:
    return render(request, 'login.html')
  newcount = db.get_new_mails(username)
  logger.info("{} get {} new mails.".format(username, newcount))
  # 获取一些用户相关的信息
  ctx = get_info(request)
  if ctx is None:
    return render(request, 'login.html')
  _mails = db.get_user_inbox(username)
  mail_list = []
  if _mails is not None:
    for m in _mails:
      mail = {}
      mail["identify"] = int(m[0])
      mail["sender"] = str(m[1])
      mail["abstract"] = str(m[3]) + " - " + str(m[4], encoding="utf-8")
      mail["dt"] = str(m[5].month) + " 月 " + str(m[5].day) + " 日"
      mail["status"] = int(m[6])
      mail_list.append(mail)
  paginator = Paginator(mail_list, 12)
  page = request.GET.get("page", 1)
  mails = paginator.get_page(page)
  ctx["mails"] = mails
  return render(request, 'index.html', context=ctx)

def mail(request):
  return render(request, 'index.html')

def mail_id(request, mid=None, *arg, **kwargs):
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
      m = db.get_user_mail_by_id(username, midint)
      if m:
        status = int(m[6])
        ctx["identify"] = int(m[0])
        ctx["sender"] = str(m[1])
        ctx["receiver"] = str(m[2])
        # 根据状态码，计算所属类别
        prefix = "收件箱"
        if status == 3:
          prefix = "发件箱"
        elif status == 4:
          prefix = "垃圾邮件"
        elif status == 5:
          prefix = "已删除 - 收件箱"
        elif status == 6:
          prefix = "已删除 - 发件箱"
        ctx["prefix"] = prefix
        ctx["subject"] = str(m[3])
        raw_content = str(m[4], encoding="utf-8")
        ctx["contents"] = raw_content.split("\n")
        ctx["dt"] = str(m[5].month) + " 月 " + str(m[5].day) + " 日"
        ctx["sendorreceive"] = 1 if int(m[6]) == 3 else 0
        if status == 1:
          db.update_mail_status(username, midint, 2)
          ctx["status"] = 2
        else:
          ctx["status"] = status
        return render(request, 'mailcontent.html', context=ctx)
  return render(request, 'index.html')

def sendmail(request):
  ctx = get_info(request)
  if ctx is None:
    return redirect("/")
  username = ctx["username"]
  if request.method == 'POST':
    toreceiver = str(request.POST['toreceiver']).strip().lower()
    subject = str(request.POST['subject']).strip()
    content = request.POST['message']
    db.send_an_mail(username, toreceiver, subject, content)
    ctx["message"] = "已将消息发送至 " + toreceiver +" ！（如果该用户存在）"
    logger.info("{} send a message to {}".format(username, toreceiver))
    return render(request, 'sendmail.html', context=ctx)
  return render(request, 'sendmail.html', context=ctx)

def outbox(request):
  ctx = get_info(request)
  if ctx is None:
    return redirect("/")
  username = ctx["username"]
  _mails = db.get_user_outbox(username)
  mail_list = []
  if _mails is not None:
    for m in _mails:
      mail = {}
      mail["identify"] = int(m[0])
      mail["sender"] = str(m[1])
      mail["receiver"] = str(m[2])
      mail["abstract"] = str(m[3]) + " - " + str(m[4], encoding="utf-8")
      mail["dt"] = str(m[5].month) + " 月 " + str(m[5].day) + " 日"
      mail["status"] = int(m[6])
      mail_list.append(mail)
  paginator = Paginator(mail_list, 12)
  page = request.GET.get("page", 1)
  mails = paginator.get_page(page)
  ctx["mails"] = mails
  return render(request, 'outbox.html', context=ctx)

def deletebox(request):
  ctx = get_info(request)
  if ctx is None:
    return redirect("/")
  username = ctx["username"]
  _mails = db.get_user_deletebox(username)
  mail_list = []
  if _mails is not None:
    for m in _mails:
      mail = {}
      mail["identify"] = int(m[0])
      mail["sender"] = str(m[1])
      mail["receiver"] = str(m[2])
      mail["abstract"] = str(m[3]) + " - " + str(m[4], encoding="utf-8")
      mail["dt"] = str(m[5].month) + " 月 " + str(m[5].day) + " 日"
      mail["status"] = int(m[6])
      mail_list.append(mail)
  paginator = Paginator(mail_list, 12)
  page = request.GET.get("page", 1)
  mails = paginator.get_page(page)
  ctx["mails"] = mails
  return render(request, 'deletebox.html', context=ctx)

def spambox(request):
  ctx = get_info(request)
  if ctx is None:
    return redirect("/")
  username = ctx["username"]
  _mails = db.get_user_spambox(username)
  mail_list = []
  if _mails is not None:
    for m in _mails:
      mail = {}
      mail["identify"] = int(m[0])
      mail["sender"] = str(m[1])
      mail["receiver"] = str(m[2])
      mail["abstract"] = str(m[3]) + " - " + str(m[4], encoding="utf-8")
      mail["dt"] = str(m[5].month) + " 月 " + str(m[5].day) + " 日"
      mail["status"] = int(m[6])
      mail_list.append(mail)
  paginator = Paginator(mail_list, 12)
  page = request.GET.get("page", 1)
  mails = paginator.get_page(page)
  ctx["mails"] = mails
  return render(request, 'spambox.html', context=ctx)

@csrf_exempt
def markspam(request):
  if request.method == 'POST':
    if request.POST["username"] and request.POST["mid"]:
      username = str(request.POST["username"]).strip()
      mid = int(request.POST["mid"])
      try:
        db.update_mail_status(username, mid, 4)
        logger.info("{} marked mail-{} spam".format(username, mid))
        return JsonResponse({"info": "OK"})
      except:
        logger.warning("{} failed to mark mail-{} spam".format(username, mid))
        return HttpResponseServerError("Error", content_type="text/plain")
  return HttpResponseServerError("Error", content_type="text/plain")

@csrf_exempt
def markdelete(request):
  if request.method == 'POST':
    if request.POST["username"] and request.POST["mid"]:
      username = str(request.POST["username"]).strip()
      mid = int(request.POST["mid"])
      try:
        db.update_mail_status(username, mid, 5)
        logger.info("{} deleted mail-{}".format(username, mid))
        return JsonResponse({"info": "OK"})
      except:
        logger.warning("{} failed to delete mail-{}".format(username, mid))
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
        db.update_mail_status(username, mid, 5+sor)
        logger.info("{} deleted mail-{}".format(username, mid))
        return JsonResponse({"info": "OK"})
      except:
        logger.warning("{} failed to delete mail-{}".format(username, mid))
        return HttpResponseServerError("Error", content_type="text/plain")
  return HttpResponseServerError("Error", content_type="text/plain")

@csrf_exempt
def markdeletesend(request):
  if request.method == 'POST':
    if request.POST["username"] and request.POST["mid"]:
      username = str(request.POST["username"]).strip()
      mid = int(request.POST["mid"])
      try:
        db.update_mail_status(username, mid, 6)
        logger.info("{} deleted mail-{}".format(username, mid))
        return JsonResponse({"info": "OK"})
      except:
        logger.warning("{} failed to delete mail-{}".format(username, mid))
        return HttpResponseServerError("Error", content_type="text/plain")
  return HttpResponseServerError("Error", content_type="text/plain")

@csrf_exempt
def markread(request):
  if request.method == 'POST':
    if request.POST["username"] and request.POST["mid"]:
      username = str(request.POST["username"]).strip()
      mid = int(request.POST["mid"])
      try:
        db.update_mail_status(username, mid, 2)
        logger.info("{} marked mail-{} read".format(username, mid))
        return JsonResponse({"info": "OK"})
      except:
        logger.warning("{} failed to mark mail-{} read".format(username, mid))
        return HttpResponseServerError("Error", content_type="text/plain")
  return HttpResponseServerError("Error", content_type="text/plain")

@csrf_exempt
def restorespam(request):
  if request.method == 'POST':
    if request.POST["username"] and request.POST["mid"]:
      username = str(request.POST["username"]).strip()
      mid = int(request.POST["mid"])
      try:
        db.update_mail_status(username, mid, 2)
        logger.info("{} restored mail-{} from spam".format(username, mid))
        return JsonResponse({"info": "OK"})
      except:
        logger.warning("{} failed to restore mail-{} from spam".format(username, mid))
        return HttpResponseServerError("Error", content_type="text/plain")
  return HttpResponseServerError("Error", content_type="text/plain")

@csrf_exempt
def restoredelete(request):
  if request.method == 'POST':
    if request.POST["username"] and request.POST["mid"]:
      username = str(request.POST["username"]).strip()
      mid = int(request.POST["mid"])
      try:
        oldm = db.get_user_mail_by_id(username, mid)
        if oldm is not None:
          status = int(oldm[6])
          if status == 6:
            db.update_mail_status(username, mid, 3)
            logger.info("{} restored sended mail-{} from delete".format(username, mid))
          else:
            db.update_mail_status(username, mid, 2)
            logger.info("{} restored received mail-{} from delete".format(username, mid))
          return JsonResponse({"info": "OK"})
      except:
        logger.warning("{} failed to restore mail-{} from delete".format(username, mid))
        return HttpResponseServerError("Error", content_type="text/plain")
  return HttpResponseServerError("Error", content_type="text/plain")

@csrf_exempt
def deleteforever(request):
  if request.method == 'POST':
    if request.POST["username"] and request.POST["mid"]:
      username = str(request.POST["username"]).strip()
      mid = int(request.POST["mid"])
      try:
        db.delete_user_mail(username, mid)
        logger.info("{} delete mail-{} forever".format(username, mid))
        return JsonResponse({"info": "OK"})
      except:
        logger.warning("{} failed to delete mail-{} forever".format(username, mid))
        return HttpResponseServerError("Error", content_type="text/plain")
  return HttpResponseServerError("Error", content_type="text/plain")

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
          logger.info("{} added a word".format(username))
          return redirect("config")
  return redirect("config")

def configdelete(request):
  if request.method == 'POST':
    username = request.POST.get("username", None)
    wordid = request.POST.get('wid', None)
    if username and wordid:
      if db.delete_user_dict(username, int(wordid)):
        logger.info("{} deleted a word".format(username))
        return JsonResponse({"info": "OK"})
  return JsonResponse({"info": "Error"})