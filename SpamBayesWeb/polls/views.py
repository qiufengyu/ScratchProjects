import os
import shutil

import re
from django.shortcuts import render

from django.http import HttpResponse, JsonResponse, HttpResponseServerError
from django.views.decorators.csrf import requires_csrf_token

from spam.bayes import BayesSpam
from spam.preprocess_data import *

bs_global = BayesSpam(train_file_list=None, test_file_list=None, spam_set=None, user_data=True, flag=True)

def index(request):
  if request.method == 'GET':
    print(request.GET)
    if len(request.GET.dict().keys()) == 0:
      return render(request, 'index.html')
    total_fold = int(request.GET.get('name_totalfold', default=5))
    fold = int(request.GET.get('name_fold', default=1))
    if total_fold and fold:
      # 记录下原本应该是垃圾邮件，但预测为正常邮件
      spam_but_ham = []
      # 记录下原本应该是垃圾邮件，但预测为正常邮件
      ham_but_spam = []
      file_list = get_all_files('spam/data', 'spam/')
      print("合计有效文件：", len(file_list))
      spam_set = get_index('spam/index', 'spam/')
      print(len(spam_set))
      train_file_list, test_file_list = split_data(file_list, total_fold, fold)

      # 这里为了展示方便就用了 2000 个训练样本
      # 如果是正常的全部数据，使用如下代码
      """
      bs = BayesSpam(train_file_list, test_file_list, spam_set)
      for file_i in test_file_list:
        下面的代码一样
      """
      print("训练集文件数：", len(train_file_list))
      print("测试集文件数：", len(test_file_list))
      # 统计平均准确率
      correct = 0
      total_count = 0
      # 统计垃圾邮件的 precision, recall 和 f1
      ground_spam = 0
      my_spam = 0
      correct_spam = 0
      bs = BayesSpam(train_file_list, test_file_list, spam_set, meta_path='spam')
      for file_i in test_file_list:
        total_count += 1
        my_result = bs.test_email(file_i)
        if my_result == is_spam(file_i, spam_set):
          correct += 1
          if is_spam(file_i, spam_set):
            ground_spam += 1
            correct_spam += 1
            # print(file_i, "是垃圾邮件，预测正确！")
          # else:
            # print(file_i, "不是垃圾邮件，预测正确！")
        else:
          if is_spam(file_i, spam_set):
            # print(file_i, "是垃圾邮件，预测错误！")
            spam_but_ham.append(file_i)
            ground_spam += 1
          else:
            # print(file_i, "不是垃圾邮件，预测错误！")
            ham_but_spam.append(file_i)
        if my_result:
          my_spam += 1

      acc = float(correct) / total_count * 100.0
      precision = correct_spam / my_spam * 100.0
      recall = correct_spam / ground_spam * 100.0
      f1 = 2.0 * precision * recall / (precision+recall)

      return render(request, 'index.html', context={'spam_but_ham': spam_but_ham, 'ham_but_spam': ham_but_spam,
                                                    'acc': f"{acc:.4f}", 'spam_but_ham_count': len(spam_but_ham),
                                                    'ham_but_spam_count': len(ham_but_spam),
                                                    'precision': f"{precision:.4f}", 'recall': f"{recall:.4f}", 'f1': f"{f1:.4f}"})

  return render(request, 'index.html')


def add(request):
  if request.method == 'POST':
    print(request.POST)
    label = request.POST.get('labelradios')
    content = request.POST.get('emailtext')
    if label == 'spam':
      if not os.path.exists('spam/myspam'):
        os.mkdir('spam/myspam')
      current_files = len([f for f in os.listdir('spam/myspam') if os.path.isfile(os.path.join('spam/myspam', f))]) + 1
      with open('spam/myspam/{}'.format(current_files), 'w', encoding='utf-8') as f:
        for line in re.split('\s', content):
          f.write(line.strip()+'\n')
    else:
      if not os.path.exists('spam/myham'):
        os.mkdir('myham')
      current_files = len([f for f in os.listdir('spam/myham/') if os.path.isfile(os.path.join('spam/myham', f))]) + 1
      with open('spam/myham/{}'.format(current_files), 'w', encoding='utf-8') as f:
        for line in re.split('\s', content):
          f.write(line.strip() + '\n')
    info = "成功添加用户样例！"
    return render(request, 'add.html', context={'info': info, 'last_content': content})
  return render(request, 'add.html')


def analysis(request):
  if request.method == 'POST':
    print(request.POST)
    content = request.POST.get('emailtext')
    if content:
      bs_global.load_model('spam/model')
      result = {}
      is_spam = bs_global.test_single_email(content)
      if is_spam:
        result['label'] = '垃圾邮件'
        result['value'] = 'spam'
        result['style'] = 'badge badge-danger'
      else:
        result['label'] = '正常邮件'
        result['value'] = 'ham'
        result['style'] = 'badge badge-success'
      last_email_text = content
      return render(request, 'analysis.html', context={'result': result, 'last_email_text': last_email_text})

  return render(request, 'analysis.html')

def view_wrong(request, path=None, file=None, *arg, **kwargs):
  try:
    arg1 = path
    arg2 = file
  except Exception:
    arg1 = ''
    arg2 = ''
  email = {}
  email['content'] = []
  file_path = os.path.join(os.path.join('spam/data', arg1), arg2)
  # 在 Windows 系统中使用
  file_path = file_path.replace('\\', '/')
  with open(file_path, 'r', encoding='utf-8') as f:
    while True:
      line = f.readline()
      if not line:
        break
      email['content'].append(line)
  email['path'] = file_path
  spam_set = get_index('spam/index', 'spam/')
  if file_path in spam_set:
    email['label'] = '垃圾邮件'
    email['class'] = 'badge badge-danger'
  else:
    email['label'] = '正常邮件'
    email['class'] = 'badge badge-success'
  return render(request, 'email.html', context={'email': email})


def change(request):
  if request.method == 'GET':
    change_file = request.GET.get('email_path')
    print("修改", change_file, "的标签...")
    change_file = change_file.replace('\\', '/')
    print(change_file)
    with open('spam/index', 'r', encoding='utf-8') as f_old:
      with open('spam/new_index', 'w', encoding='utf-8') as f_new:
        while True:
          line = f_old.readline()
          if not line:
            break
          line = line.strip()
          parts = line.split(' ')
          if parts[1].replace('../', 'spam/') == change_file:
            if parts[0] == 'ham':
              f_new.write('spam '+ change_file + '\n')
            else:
              f_new.write('ham '+change_file+'\n')
          else:
            f_new.write(line+'\n')
    paths = change_file.split('/')
    shutil.move('spam/new_index', 'spam/index')
    return view_wrong(request, paths[2], paths[3])
  return HttpResponseServerError("修改出错！", content_type="text/plain")

# 仅接受 post 请求
@requires_csrf_token
def addone(request):
  if request.method == 'POST':
    print(request.POST)
    ret_val = {}
    lb = request.POST['label']
    cont = request.POST['content']
    if lb and cont:
      if lb == 'spam':
        if not os.path.exists('spam/myspam'):
          os.mkdir('spam/myspam')
        current_files = len(
          [f for f in os.listdir('spam/myspam') if os.path.isfile(os.path.join('spam/myspam', f))]) + 1
        with open('spam/myspam/{}'.format(current_files), 'w', encoding='utf-8') as f:
          for line in re.split('\s', cont):
            f.write(line.strip() + '\n')
      else:
        if not os.path.exists('spam/myham'):
          os.mkdir('myham')
        current_files = len([f for f in os.listdir('spam/myham/') if os.path.isfile(os.path.join('spam/myham', f))]) + 1
        with open('spam/myham/{}'.format(current_files), 'w', encoding='utf-8') as f:
          for line in re.split('\s', cont):
            f.write(line.strip() + '\n')
      ret_val['ret'] = 1
    else:
      ret_val['ret'] = -1
    return JsonResponse(ret_val)
  elif request.method == 'GET':
    ret_val = {}
    lb = request.GET.get('label')
    cont = request.GET.get('content')
    if lb and cont:
      if lb == 'spam':
        if not os.path.exists('spam/myspam'):
          os.mkdir('spam/myspam')
        current_files = len(
          [f for f in os.listdir('spam/myspam') if os.path.isfile(os.path.join('spam/myspam', f))]) + 1
        with open('spam/myspam/{}'.format(current_files), 'w', encoding='utf-8') as f:
          for line in re.split('\s', cont):
            f.write(line.strip() + '\n')
      else:
        if not os.path.exists('spam/myham'):
          os.mkdir('myham')
        current_files = len([f for f in os.listdir('spam/myham/') if os.path.isfile(os.path.join('spam/myham', f))]) + 1
        with open('spam/myham/{}'.format(current_files), 'w', encoding='utf-8') as f:
          for line in re.split('\s', cont):
            f.write(line.strip() + '\n')
      ret_val['ret'] = 1
    else:
      ret_val['ret'] = -2
    return JsonResponse(ret_val)
  return HttpResponseServerError('非法提交！', content_type="text/plain")
