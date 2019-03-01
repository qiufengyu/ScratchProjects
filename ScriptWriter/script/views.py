import os
import tensorflow as tf

from django.shortcuts import render
from django.http import HttpResponse

from rnnwriter.writertest import api_write

graph = tf.Graph()

def index(request):
  if request.method == 'POST':
    print(request.POST)
    words = int(request.POST.get("words", 100))
    start_string = request.POST.get("startstring", "")
    ctx = {}
    ctx["words"] = words
    ctx["startstring"] = start_string.strip()
    if len(start_string) <= 0:
      ctx["message"] = "请输入主题起始词！"
      return render(request, "index.html", context=ctx)
    res = api_write(graph, start_string, words)
    if res:
      ctx["res"] = res
    else:
      ctx["message"] = "没有剧本创作模型！"
    return render(request, "index.html", context=ctx)
  return render(request, "index.html")

def about(request):
  return render(request, "about.html")
